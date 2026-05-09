/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.spring;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ManagedIdentityCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.Map;

import static com.microsoft.azure.keyvault.spring.Constants.AZURE_KEYVAULT_CLIENT_ID;
import static com.microsoft.azure.keyvault.spring.Constants.AZURE_KEYVAULT_CERTIFICATE_PATH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class KeyVaultEnvironmentPostProcessorTest {
    private KeyVaultEnvironmentPostProcessorHelper keyVaultEnvironmentPostProcessorHelper;
    private ConfigurableEnvironment environment;
    private MutablePropertySources propertySources;
    private final Map<String, Object> testProperties = new HashMap<>();

    @BeforeEach
    public void setup() {
        environment = new MockEnvironment();
        testProperties.clear();
        propertySources = environment.getPropertySources();
    }

    @Test
    public void testGetCredentialsWhenNoConfigUsesSystemMSI() {
        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);
        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();
        assertThat(credentials).isInstanceOf(ManagedIdentityCredential.class);
    }

    @Test
    public void testGetCredentialsWhenUsingClientAndKey() {
        testProperties.put("azure.keyvault.client-id", "aaaa-bbbb-cccc-dddd");
        testProperties.put("azure.keyvault.client-key", "mySecret");
        testProperties.put("azure.keyvault.tenant-id", "eeee-ffff-0000-1111");
        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);
        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();
        assertThat(credentials).isInstanceOf(ClientSecretCredential.class);
    }

    @Test
    public void testGetCredentialsWhenMSIEnabledWithClientId() {
        testProperties.put("azure.keyvault.client-id", "aaaa-bbbb-cccc-dddd");
        propertySources.addLast(new MapPropertySource("Test_Properties", testProperties));

        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);
        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();
        assertThat(credentials).isInstanceOf(ManagedIdentityCredential.class);
    }

    @Test
    public void testGetCredentialsWhenMSIEnabledWithoutClientId() {
        keyVaultEnvironmentPostProcessorHelper = new KeyVaultEnvironmentPostProcessorHelper(environment);
        final TokenCredential credentials = keyVaultEnvironmentPostProcessorHelper.getCredentials();
        assertThat(credentials).isInstanceOf(ManagedIdentityCredential.class);
    }

    @Test
    public void postProcessorHasConfiguredOrder() {
        final KeyVaultEnvironmentPostProcessor processor = new KeyVaultEnvironmentPostProcessor();
        assertEquals(processor.getOrder(), KeyVaultEnvironmentPostProcessor.DEFAULT_ORDER);
    }

    @Test
    public void postProcessorOrderConfigurable() {
        final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(OrderedProcessConfig.class))
                .withPropertyValues("azure.keyvault.uri=fakeuri", "azure.keyvault.enabled=true");

        contextRunner.run(context -> {
            assertThat(KeyVaultEnvironmentPostProcessor.DEFAULT_ORDER)
                    .isNotEqualTo(OrderedProcessConfig.TEST_ORDER);
            assertEquals(OrderedProcessConfig.TEST_ORDER,
                    context.getBean(KeyVaultEnvironmentPostProcessor.class).getOrder(),
                    "KeyVaultEnvironmentPostProcessor order should be changed.");
        });
    }
}

@Configuration
class OrderedProcessConfig {
    static final int TEST_ORDER = KeyVaultEnvironmentPostProcessor.DEFAULT_ORDER + 1;

    @Bean
    @Primary
    public KeyVaultEnvironmentPostProcessor getProcessor() {
        final KeyVaultEnvironmentPostProcessor processor = new KeyVaultEnvironmentPostProcessor();
        processor.setOrder(TEST_ORDER);
        return processor;
    }
}
