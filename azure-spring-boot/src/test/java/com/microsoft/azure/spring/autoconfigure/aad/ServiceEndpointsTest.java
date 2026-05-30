/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.aad;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ServiceEndpoints} POJO (getters, setters, equals, hashCode).
 */
public class ServiceEndpointsTest {

    private static final String SIGNIN_URI = "https://login.microsoftonline.com/";
    private static final String GRAPH_API_URI = "https://graph.windows.net/";
    private static final String KEY_DISCOVERY_URI = "https://login.microsoftonline.com/common/discovery/keys";
    private static final String MEMBERSHIP_REST_URI = "https://graph.windows.net/me/memberOf?api-version=1.6";

    private ServiceEndpoints buildEndpoints() {
        final ServiceEndpoints endpoints = new ServiceEndpoints();
        endpoints.setAadSigninUri(SIGNIN_URI);
        endpoints.setAadGraphApiUri(GRAPH_API_URI);
        endpoints.setAadKeyDiscoveryUri(KEY_DISCOVERY_URI);
        endpoints.setAadMembershipRestUri(MEMBERSHIP_REST_URI);
        return endpoints;
    }

    @Test
    public void gettersReturnSetValues() {
        final ServiceEndpoints endpoints = buildEndpoints();

        assertThat(endpoints.getAadSigninUri()).isEqualTo(SIGNIN_URI);
        assertThat(endpoints.getAadGraphApiUri()).isEqualTo(GRAPH_API_URI);
        assertThat(endpoints.getAadKeyDiscoveryUri()).isEqualTo(KEY_DISCOVERY_URI);
        assertThat(endpoints.getAadMembershipRestUri()).isEqualTo(MEMBERSHIP_REST_URI);
    }

    @Test
    public void newInstanceHasNullFields() {
        final ServiceEndpoints endpoints = new ServiceEndpoints();

        assertThat(endpoints.getAadSigninUri()).isNull();
        assertThat(endpoints.getAadGraphApiUri()).isNull();
        assertThat(endpoints.getAadKeyDiscoveryUri()).isNull();
        assertThat(endpoints.getAadMembershipRestUri()).isNull();
    }

    @Test
    public void equalsAndHashCodeForEqualEndpoints() {
        final ServiceEndpoints first = buildEndpoints();
        final ServiceEndpoints second = buildEndpoints();

        assertThat(first).isEqualTo(second);
        assertThat(first.hashCode()).isEqualTo(second.hashCode());
    }

    @Test
    public void notEqualWhenFieldDiffers() {
        final ServiceEndpoints first = buildEndpoints();
        final ServiceEndpoints second = buildEndpoints();
        second.setAadSigninUri("https://login.chinacloudapi.cn/");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    public void toStringIncludesFieldValues() {
        final ServiceEndpoints endpoints = buildEndpoints();

        assertThat(endpoints.toString()).contains(SIGNIN_URI);
        assertThat(endpoints.toString()).contains(GRAPH_API_URI);
    }
}
