/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.keyvault.spring;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.microsoft.azure.keyvault.spring.certificate.KeyCert;
import com.microsoft.azure.keyvault.spring.certificate.KeyCertReader;
import com.microsoft.azure.keyvault.spring.certificate.KeyCertReaderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class KeyVaultCertificateCredential {
    private final TokenCredential tokenCredential;

    public KeyVaultCertificateCredential(final String tenantId, final String clientId,
                                         final Resource certResource, final String certPassword) {
        Assert.isTrue(certResource.exists(),
                String.format("Certificate file %s should exist.", certResource.getFilename()));

        final String certFileName = certResource.getFilename();
        final KeyCertReader certReader = KeyCertReaderFactory.getReader(certFileName);
        final KeyCert keyCert = certReader.read(certResource, certPassword);

        try (final InputStream certStream = certResource.getInputStream()) {
            this.tokenCredential = new ClientCertificateCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .pemCertificate(keyCert.getKey().toString())
                    .build();
        } catch (IOException e) {
            throw new IllegalStateException(
                    String.format("Failed to read certificate from %s", certFileName), e);
        }
    }

    public TokenCredential getTokenCredential() {
        return tokenCredential;
    }
}
