/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.DeprecatedConfigurationProperty;
import org.springframework.validation.annotation.Validated;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Validated
@ConfigurationProperties("azure.activedirectory")
@Data
public class AADAuthenticationProperties {

    private static final String DEFAULT_SERVICE_ENVIRONMENT = "global";

    private UserGroupProperties userGroup = new UserGroupProperties();
    private String environment = DEFAULT_SERVICE_ENVIRONMENT;
    private String clientId;
    private String clientSecret;
    private List<String> activeDirectoryGroups = new ArrayList<>();
    private int jwtConnectTimeout = RemoteJWKSet.DEFAULT_HTTP_CONNECT_TIMEOUT;
    private int jwtReadTimeout = RemoteJWKSet.DEFAULT_HTTP_READ_TIMEOUT;
    private int jwtSizeLimit = RemoteJWKSet.DEFAULT_HTTP_SIZE_LIMIT;
    private String tenantId;
    private boolean allowTelemetry = true;

    @DeprecatedConfigurationProperty(reason = "Configuration moved to UserGroup class",
            replacement = "azure.activedirectory.user-group.allowed-groups")
    public List<String> getActiveDirectoryGroups() {
        return activeDirectoryGroups;
    }

    @Data
    public static class UserGroupProperties {
        private List<String> allowedGroups = new ArrayList<>();
        @NotEmpty
        private String key = "objectType";
        @NotEmpty
        private String value = "Group";
        @NotEmpty
        private String objectIDKey = "objectId";
    }

    @PostConstruct
    public void validateUserGroupProperties() {
        if (this.activeDirectoryGroups.isEmpty() && this.getUserGroup().getAllowedGroups().isEmpty()) {
            throw new IllegalStateException("One of the User Group Properties must be populated. "
                    + "Please populate azure.activedirectory.user-group.allowed-groups");
        }
    }
}
