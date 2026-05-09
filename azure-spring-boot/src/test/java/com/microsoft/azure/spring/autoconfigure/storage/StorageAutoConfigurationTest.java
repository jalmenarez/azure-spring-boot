/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.storage;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class StorageAutoConfigurationTest {
    private static final String ACCOUNT_KEY = "ZmFrZUFjY291bnRLZXk="; /* Base64 encoded for string fakeAccountKey */
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(StorageAutoConfiguration.class));

    @Test
    public void blobServiceClientBeanNotCreatedByDefault() {
        contextRunner.run(context ->
            assertThatThrownBy(() -> context.getBean(BlobServiceClient.class))
                .isInstanceOf(NoSuchBeanDefinitionException.class)
        );
    }

    @Test
    public void blobServiceClientBeanCreatedCorrectly() {
        contextRunner.withPropertyValues("azure.storage.account-name=fakeStorageAccountName",
                "azure.storage.account-key=" + ACCOUNT_KEY)
                .run(context -> {
                    final BlobServiceClient client = context.getBean(BlobServiceClient.class);
                    assertThat(client).isNotNull();
                    assertThat(client.getAccountName()).isEqualToIgnoringCase("fakeStorageAccountName");
                });
    }

    @Test
    public void containerClientNotCreatedIfNotConfigured() {
        contextRunner.withPropertyValues("azure.storage.account-name=fakeStorageAccountName",
                "azure.storage.account-key=" + ACCOUNT_KEY)
                .run(context ->
                    assertThatThrownBy(() -> context.getBean(BlobContainerClient.class))
                        .isInstanceOf(NoSuchBeanDefinitionException.class)
                );
    }

    @Test
    public void containerClientCreatedIfConfigured() {
        contextRunner.withPropertyValues("azure.storage.account-name=fakeStorageAccountName",
                "azure.storage.account-key=" + ACCOUNT_KEY,
                "azure.storage.container-name=fakestoragecontainername")
                .run(context -> {
                    final BlobContainerClient containerClient = context.getBean(BlobContainerClient.class);
                    assertThat(containerClient).isNotNull();
                    assertThat(containerClient.getBlobContainerName()).isEqualTo("fakestoragecontainername");
                });
    }
}
