/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.spring;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.microsoft.azure.telemetry.TelemetrySender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.microsoft.azure.telemetry.TelemetryData.SERVICE_NAME;
import static com.microsoft.azure.telemetry.TelemetryData.getClassPackageSimpleName;

class KeyVaultEnvironmentPostProcessorHelper {
    private static final Logger LOG = LoggerFactory.getLogger(KeyVaultEnvironmentPostProcessorHelper.class);

    private final ConfigurableEnvironment environment;

    public KeyVaultEnvironmentPostProcessorHelper(final ConfigurableEnvironment environment) {
        this.environment = environment;
        sendTelemetry();
    }

    public void addKeyVaultPropertySource() {
        final String vaultUri = getProperty(this.environment, Constants.AZURE_KEYVAULT_VAULT_URI);
        final Long refreshInterval = Optional.ofNullable(
                this.environment.getProperty(Constants.AZURE_KEYVAULT_REFRESH_INTERVAL))
                .map(Long::valueOf).orElse(Constants.DEFAULT_REFRESH_INTERVAL_MS);

        final TokenCredential credential = getCredentials();

        final SecretClient secretClient = new SecretClientBuilder()
                .vaultUrl(vaultUri)
                .credential(credential)
                .buildClient();

        try {
            final MutablePropertySources sources = this.environment.getPropertySources();
            final KeyVaultOperation kvOperation = new KeyVaultOperation(secretClient, vaultUri, refreshInterval);

            if (sources.contains(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME)) {
                sources.addAfter(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                        new KeyVaultPropertySource(kvOperation));
            } else {
                sources.addFirst(new KeyVaultPropertySource(kvOperation));
            }

        } catch (final Exception ex) {
            throw new IllegalStateException("Failed to configure KeyVault property source", ex);
        }
    }

    TokenCredential getCredentials() {
        final String clientId = this.environment.getProperty(Constants.AZURE_KEYVAULT_CLIENT_ID);
        final String clientKey = this.environment.getProperty(Constants.AZURE_KEYVAULT_CLIENT_KEY);
        final String tenantId = this.environment.getProperty(Constants.AZURE_KEYVAULT_TENANT_ID);
        final String certPath = this.environment.getProperty(Constants.AZURE_KEYVAULT_CERTIFICATE_PATH);
        final String certPwd = this.environment.getProperty(Constants.AZURE_KEYVAULT_CERTIFICATE_PASSWORD);

        if (clientId != null && clientKey != null && tenantId != null) {
            LOG.debug("Will use client secret credentials");
            return new AzureKeyVaultCredential(tenantId, clientId, clientKey).getTokenCredential();
        }

        if (clientId != null && certPath != null && tenantId != null) {
            LOG.info("Read certificate from {}...", certPath);
            final Resource certResource = new DefaultResourceLoader().getResource(certPath);
            return new KeyVaultCertificateCredential(tenantId, clientId, certResource, certPwd)
                    .getTokenCredential();
        }

        if (clientId != null) {
            LOG.debug("Will use MSI credentials with specified clientId");
            return new AzureKeyVaultMSICredential(clientId).getTokenCredential();
        }

        LOG.debug("Will use system-assigned managed identity credentials");
        return new AzureKeyVaultMSICredential().getTokenCredential();
    }

    private String getProperty(final ConfigurableEnvironment env, final String propertyName) {
        Assert.notNull(env, "env must not be null!");
        Assert.notNull(propertyName, "propertyName must not be null!");

        final String property = env.getProperty(propertyName);

        if (property == null || property.isEmpty()) {
            throw new IllegalArgumentException("property " + propertyName + " must not be null");
        }
        return property;
    }

    private boolean allowTelemetry(final ConfigurableEnvironment env) {
        Assert.notNull(env, "env must not be null!");
        return env.getProperty(Constants.AZURE_KEYVAULT_ALLOW_TELEMETRY, Boolean.class, true);
    }

    private void sendTelemetry() {
        if (allowTelemetry(environment)) {
            final Map<String, String> events = new HashMap<>();
            final TelemetrySender sender = new TelemetrySender();
            events.put(SERVICE_NAME, getClassPackageSimpleName(KeyVaultEnvironmentPostProcessorHelper.class));
            sender.send(ClassUtils.getUserClass(getClass()).getSimpleName(), events);
        }
    }
}
