/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.spring;

import com.azure.security.keyvault.secrets.SecretClient;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

public class KeyVaultOperation {
    private final long cacheRefreshIntervalInMs;
    private final Object refreshLock = new Object();
    private final Consumer<Consumer<String>> secretNamesIterator;
    private final Function<String, String> secretValueRetriever;
    private ConcurrentHashMap<String, Object> propertyNamesHashMap = new ConcurrentHashMap<>();
    private final AtomicLong lastUpdateTime = new AtomicLong();
    private final ReadWriteLock rwLock = new ReentrantReadWriteLock();

    public KeyVaultOperation(final SecretClient secretClient, final String vaultUri) {
        this(secretClient, vaultUri, Constants.DEFAULT_REFRESH_INTERVAL_MS);
    }

    public KeyVaultOperation(final SecretClient secretClient, final String vaultUri, final long refreshInterval) {
        this(
            action -> secretClient.listPropertiesOfSecrets().forEach(s -> action.accept(s.getName())),
            name -> secretClient.getSecret(name).getValue(),
            refreshInterval
        );
    }

    KeyVaultOperation(
            final Consumer<Consumer<String>> secretNamesIterator,
            final Function<String, String> secretValueRetriever,
            final long refreshInterval) {
        this.cacheRefreshIntervalInMs = refreshInterval;
        this.secretNamesIterator = secretNamesIterator;
        this.secretValueRetriever = secretValueRetriever;
        fillSecretsHashMap();
    }

    public String[] list() {
        try {
            this.rwLock.readLock().lock();
            return Collections.list(propertyNamesHashMap.keys()).toArray(new String[propertyNamesHashMap.size()]);
        } finally {
            this.rwLock.readLock().unlock();
        }
    }

    private String getKeyvaultSecretName(@NonNull final String property) {
        if (property.matches("[a-z0-9A-Z-]+")) {
            return property.toLowerCase(Locale.US);
        } else if (property.matches("[A-Z0-9_]+")) {
            return property.toLowerCase(Locale.US).replaceAll("_", "-");
        } else {
            return property.toLowerCase(Locale.US)
                    .replaceAll("-", "")
                    .replaceAll("_", "")
                    .replaceAll("\\.", "-");
        }
    }

    /**
     * For convention we need to support all relaxed binding format from spring, these may include:
     * <ul>
     * <li>acme.my-project.person.first-name</li>
     * <li>acme.myProject.person.firstName</li>
     * <li>acme.my_project.person.first_name</li>
     * <li>ACME_MYPROJECT_PERSON_FIRSTNAME</li>
     * </ul>
     * But azure keyvault only allows ^[0-9a-zA-Z-]+$ and case insensitive, so there must be some conversion
     * between spring names and azure keyvault names.
     * For example, the 4 properties stated above should be convert to acme-myproject-person-firstname in keyvault.
     *
     * @param property of secret instance.
     * @return the value of secret with given name or null.
     */
    public String get(final String property) {
        Assert.hasText(property, "property should contain text.");
        final String secretName = getKeyvaultSecretName(property);

        if (System.currentTimeMillis() - this.lastUpdateTime.get() > this.cacheRefreshIntervalInMs) {
            synchronized (this.refreshLock) {
                if (System.currentTimeMillis() - this.lastUpdateTime.get() > this.cacheRefreshIntervalInMs) {
                    this.lastUpdateTime.set(System.currentTimeMillis());
                    fillSecretsHashMap();
                }
            }
        }

        if (this.propertyNamesHashMap.containsKey(secretName)) {
            return this.secretValueRetriever.apply(secretName);
        } else {
            return null;
        }
    }

    private void fillSecretsHashMap() {
        try {
            this.rwLock.writeLock().lock();
            this.propertyNamesHashMap.clear();

            this.secretNamesIterator.accept(name -> {
                final String secretName = name.toLowerCase(Locale.US);
                propertyNamesHashMap.putIfAbsent(secretName, name);
                propertyNamesHashMap.putIfAbsent(secretName.replaceAll("-", "."), name);
            });

            this.lastUpdateTime.set(System.currentTimeMillis());
        } finally {
            this.rwLock.writeLock().unlock();
        }
    }
}
