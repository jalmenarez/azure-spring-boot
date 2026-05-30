/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.microsoft.azure.telemetry.TelemetrySender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.util.ClassUtils;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.telemetry.TelemetryData.HASHED_NAMESPACE;
import static com.microsoft.azure.telemetry.TelemetryData.SERVICE_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.getClassPackageSimpleName;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@Lazy
@Configuration
@ConditionalOnClass(ServiceBusSenderClient.class)
@EnableConfigurationProperties(ServiceBusProperties.class)
@ConditionalOnProperty(prefix = "azure.servicebus", value = "connection-string")
public class ServiceBusAutoConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceBusAutoConfiguration.class);

    private final ServiceBusProperties properties;

    public ServiceBusAutoConfiguration(final ServiceBusProperties properties) {
        this.properties = properties;
    }

    @Bean("queueSenderClient")
    @ConditionalOnMissingBean(name = "queueSenderClient")
    @ConditionalOnProperty(prefix = "azure.servicebus", value = "queue-name")
    public ServiceBusSenderClient queueSenderClient() {
        return new ServiceBusClientBuilder()
                .connectionString(properties.getConnectionString())
                .sender()
                .queueName(properties.getQueueName())
                .buildClient();
    }

    @Bean("topicSenderClient")
    @ConditionalOnMissingBean(name = "topicSenderClient")
    @ConditionalOnProperty(prefix = "azure.servicebus", value = "topic-name")
    public ServiceBusSenderClient topicSenderClient() {
        return new ServiceBusClientBuilder()
                .connectionString(properties.getConnectionString())
                .sender()
                .topicName(properties.getTopicName())
                .buildClient();
    }

    @Bean("queueReceiverClient")
    @ConditionalOnMissingBean(name = "queueReceiverClient")
    @ConditionalOnProperty(prefix = "azure.servicebus", value = {"queue-name", "queue-receive-mode"})
    public ServiceBusReceiverClient queueReceiverClient() {
        return new ServiceBusClientBuilder()
                .connectionString(properties.getConnectionString())
                .receiver()
                .queueName(properties.getQueueName())
                .receiveMode(properties.getQueueReceiveMode())
                .buildClient();
    }

    @Bean("subscriptionReceiverClient")
    @ConditionalOnMissingBean(name = "subscriptionReceiverClient")
    @ConditionalOnProperty(prefix = "azure.servicebus",
            value = {"topic-name", "subscription-name", "subscription-receive-mode"})
    public ServiceBusReceiverClient subscriptionReceiverClient() {
        return new ServiceBusClientBuilder()
                .connectionString(properties.getConnectionString())
                .receiver()
                .topicName(properties.getTopicName())
                .subscriptionName(properties.getSubscriptionName())
                .receiveMode(properties.getSubscriptionReceiveMode())
                .buildClient();
    }

    private String getHashNamespace() {
        final String namespace = properties.getConnectionString()
                .replaceFirst("^.*//", "")
                .replaceAll("\\..*$", "");

        if (!namespace.matches("[a-zA-Z][a-zA-Z-0-9]{4,48}[a-zA-Z0-9]")) {
            LOG.warn("Unexpected namespace name {}, please check if it's valid.", namespace);
        }

        return sha256Hex(namespace);
    }

    @PostConstruct
    private void sendTelemetry() {
        if (properties.isAllowTelemetry()) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();

            events.put(SERVICE_NAME, getClassPackageSimpleName(ServiceBusAutoConfiguration.class));
            events.put(HASHED_NAMESPACE, getHashNamespace());

            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }
}
