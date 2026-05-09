/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.servicebus;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;

import javax.validation.constraints.NotEmpty;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties("azure.servicebus")
public class ServiceBusProperties {
    /**
     * Service Bus connection string.
     */
    @NotEmpty
    private String connectionString;

    /**
     * Queue name. Entity path of the queue.
     */
    private String queueName;

    /**
     * Queue receive mode.
     */
    private ServiceBusReceiveMode queueReceiveMode = ServiceBusReceiveMode.PEEK_LOCK;

    /**
     * Topic name. Entity path of the topic.
     */
    private String topicName;

    /**
     * Subscription name.
     */
    private String subscriptionName;

    /**
     * Subscription receive mode.
     */
    private ServiceBusReceiveMode subscriptionReceiveMode = ServiceBusReceiveMode.PEEK_LOCK;

    private boolean allowTelemetry = true;

    public boolean isAllowTelemetry() {
        return allowTelemetry;
    }

    public void setAllowTelemetry(final boolean allowTelemetry) {
        this.allowTelemetry = allowTelemetry;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(final String connectionString) {
        this.connectionString = connectionString;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(final String queueName) {
        this.queueName = queueName;
    }

    public ServiceBusReceiveMode getQueueReceiveMode() {
        return queueReceiveMode;
    }

    public void setQueueReceiveMode(final ServiceBusReceiveMode queueReceiveMode) {
        this.queueReceiveMode = queueReceiveMode;
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(final String topicName) {
        this.topicName = topicName;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(final String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public ServiceBusReceiveMode getSubscriptionReceiveMode() {
        return subscriptionReceiveMode;
    }

    public void setSubscriptionReceiveMode(final ServiceBusReceiveMode subscriptionReceiveMode) {
        this.subscriptionReceiveMode = subscriptionReceiveMode;
    }
}
