/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.keyvault.spring;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;

public class AzureKeyVaultMSICredential {
    private final TokenCredential tokenCredential;

    public AzureKeyVaultMSICredential() {
        this.tokenCredential = new ManagedIdentityCredentialBuilder().build();
    }

    public AzureKeyVaultMSICredential(final String clientId) {
        this.tokenCredential = new ManagedIdentityCredentialBuilder()
                .clientId(clientId)
                .build();
    }

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }
}
