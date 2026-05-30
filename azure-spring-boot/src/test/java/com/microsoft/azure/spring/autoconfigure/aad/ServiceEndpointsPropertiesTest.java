/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ServiceEndpointsProperties}.
 */
public class ServiceEndpointsPropertiesTest {

    private static final String GLOBAL = "global";
    private static final String CHINA = "cn";

    private ServiceEndpointsProperties properties;
    private ServiceEndpoints globalEndpoints;

    @BeforeEach
    public void setUp() {
        properties = new ServiceEndpointsProperties();

        globalEndpoints = new ServiceEndpoints();
        globalEndpoints.setAadSigninUri("https://login.microsoftonline.com/");
        globalEndpoints.setAadGraphApiUri("https://graph.windows.net/");
        globalEndpoints.setAadKeyDiscoveryUri("https://login.microsoftonline.com/common/discovery/keys");
        globalEndpoints.setAadMembershipRestUri("https://graph.windows.net/me/memberOf?api-version=1.6");
    }

    @Test
    public void getServiceEndpointsReturnsConfiguredEndpoints() {
        final Map<String, ServiceEndpoints> endpoints = new HashMap<>();
        endpoints.put(GLOBAL, globalEndpoints);
        properties.getEndpoints().putAll(endpoints);

        final ServiceEndpoints result = properties.getServiceEndpoints(GLOBAL);

        assertThat(result).isNotNull();
        assertThat(result).isSameAs(globalEndpoints);
        assertThat(result.getAadSigninUri()).isEqualTo("https://login.microsoftonline.com/");
    }

    @Test
    public void getServiceEndpointsThrowsWhenNoEndpointsConfigured() {
        assertThatThrownBy(() -> properties.getServiceEndpoints(GLOBAL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No service endpoints found");
    }

    @Test
    public void getServiceEndpointsThrowsForUnknownEnvironment() {
        properties.getEndpoints().put(GLOBAL, globalEndpoints);

        assertThatThrownBy(() -> properties.getServiceEndpoints(CHINA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(CHINA)
                .hasMessageContaining("only following environments are supported");
    }

    @Test
    public void unknownEnvironmentMessageListsSupportedEnvironments() {
        properties.getEndpoints().put(GLOBAL, globalEndpoints);

        assertThatThrownBy(() -> properties.getServiceEndpoints(CHINA))
                .hasMessageContaining(GLOBAL);
    }

    @Test
    public void getEndpointsIsInitiallyEmptyButMutable() {
        assertThat(properties.getEndpoints()).isEmpty();

        properties.getEndpoints().put(GLOBAL, globalEndpoints);

        assertThat(properties.getEndpoints()).hasSize(1);
        assertThat(properties.getEndpoints()).containsKey(GLOBAL);
    }
}
