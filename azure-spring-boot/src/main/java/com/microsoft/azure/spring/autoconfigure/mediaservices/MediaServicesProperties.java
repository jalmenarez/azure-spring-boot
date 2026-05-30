/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.mediaservices;

import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties("azure.mediaservices")
public class MediaServicesProperties {
    @NotEmpty(message = "azure.mediaservices.tenant property must be configured.")
    private String tenant;

    @NotEmpty(message = "azure.mediaservices.client-id property must be configured.")
    private String clientId;

    @NotEmpty(message = "azure.mediaservices.client-secret property must be configured.")
    private String clientSecret;

    @NotEmpty(message = "azure.mediaservices.rest-api-endpoint property must be configured.")
    private String restApiEndpoint;

    private String proxyHost;
    private Integer proxyPort;
    private String proxyScheme = "http";
    private boolean allowTelemetry = true;
    private Integer connectTimeout;
    private Integer readTimeout;
}
