/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.storage;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.microsoft.azure.telemetry.TelemetrySender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.ClassUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.telemetry.TelemetryData.HASHED_ACCOUNT_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.SERVICE_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.getClassPackageSimpleName;
import static org.apache.commons.codec.digest.DigestUtils.sha256Hex;

@Configuration
@ConditionalOnClass(BlobServiceClient.class)
@EnableConfigurationProperties(StorageProperties.class)
@ConditionalOnProperty(prefix = "azure.storage", value = {"account-name", "account-key"})
public class StorageAutoConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(StorageAutoConfiguration.class);

    private final StorageProperties properties;

    public StorageAutoConfiguration(final StorageProperties properties) {
        this.properties = properties;
    }

    @Bean
    public BlobServiceClient blobServiceClient() {
        LOG.debug("Creating BlobServiceClient bean...");
        final String connectionString = String.format(
                "DefaultEndpointsProtocol=%s;AccountName=%s;AccountKey=%s;EndpointSuffix=core.windows.net",
                properties.isEnableHttps() ? "https" : "http",
                properties.getAccountName(),
                properties.getAccountKey());
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Bean
    @ConditionalOnProperty(prefix = "azure.storage", value = "container-name")
    public BlobContainerClient blobContainerClient(final BlobServiceClient blobServiceClient) {
        return blobServiceClient.getBlobContainerClient(properties.getContainerName());
    }

    @PostConstruct
    private void sendTelemetry() {
        if (properties.isAllowTelemetry()) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();

            events.put(SERVICE_NAME, getClassPackageSimpleName(StorageAutoConfiguration.class));
            events.put(HASHED_ACCOUNT_NAME, sha256Hex(properties.getAccountName()));

            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }
}
