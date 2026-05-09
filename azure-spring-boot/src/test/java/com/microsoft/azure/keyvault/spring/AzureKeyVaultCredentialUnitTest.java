/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.keyvault.spring;

import com.azure.identity.ClientSecretCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AzureKeyVaultCredentialUnitTest {

    private AzureKeyVaultCredential keyVaultCredential;

    @BeforeEach
    public void setup() {
        keyVaultCredential = new AzureKeyVaultCredential("fakeTenantId", "fakeClientId", "fakeClientKey");
    }

    @Test
    public void testGetTokenCredentialReturnsNonNull() {
        assertThat(keyVaultCredential.getTokenCredential()).isNotNull();
    }

    @Test
    public void testGetTokenCredentialReturnsClientSecretCredential() {
        assertThat(keyVaultCredential.getTokenCredential()).isInstanceOf(ClientSecretCredential.class);
    }
}
