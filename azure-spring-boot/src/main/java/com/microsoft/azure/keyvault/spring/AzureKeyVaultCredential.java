/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.spring;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;

public class AzureKeyVaultCredential {
    private final TokenCredential tokenCredential;

    public AzureKeyVaultCredential(final String tenantId, final String clientId, final String clientKey) {
        this.tokenCredential = new ClientSecretCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .clientSecret(clientKey)
                .build();
    }

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }
}
