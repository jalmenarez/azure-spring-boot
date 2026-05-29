/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.autoconfigure.b2c;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link AADB2CProperties} covering defaults, accessors and
 * {@link AADB2CProperties#getLoginProcessingUrl()} URL parsing.
 */
public class AADB2CPropertiesTest {

    @Test
    public void defaultValuesAreSet() {
        final AADB2CProperties properties = new AADB2CProperties();

        assertThat(properties.getOidcEnabled()).isTrue();
        assertThat(properties.isAllowTelemetry()).isTrue();
        assertThat(properties.getLogoutSuccessUrl()).isEqualTo(AADB2CProperties.DEFAULT_LOGOUT_SUCCESS_URL);
        assertThat(properties.getUserFlows()).isNotNull();
    }

    @Test
    public void gettersReturnSetValues() {
        final AADB2CProperties properties = new AADB2CProperties();
        properties.setTenant("my-tenant");
        properties.setClientId("my-client-id");
        properties.setClientSecret("my-client-secret");
        properties.setReplyUrl("http://localhost:8080/home");
        properties.setLogoutSuccessUrl("http://localhost:8080/logout");
        properties.setOidcEnabled(false);
        properties.setAllowTelemetry(false);

        assertThat(properties.getTenant()).isEqualTo("my-tenant");
        assertThat(properties.getClientId()).isEqualTo("my-client-id");
        assertThat(properties.getClientSecret()).isEqualTo("my-client-secret");
        assertThat(properties.getReplyUrl()).isEqualTo("http://localhost:8080/home");
        assertThat(properties.getLogoutSuccessUrl()).isEqualTo("http://localhost:8080/logout");
        assertThat(properties.getOidcEnabled()).isFalse();
        assertThat(properties.isAllowTelemetry()).isFalse();
    }

    @Test
    public void getLoginProcessingUrlReturnsPathOfReplyUrl() {
        final AADB2CProperties properties = new AADB2CProperties();
        properties.setReplyUrl("http://localhost:8080/login/oauth2/code/");

        assertThat(properties.getLoginProcessingUrl()).isEqualTo("/login/oauth2/code/");
    }

    @Test
    public void getLoginProcessingUrlReturnsEmptyPathForRootUrl() {
        final AADB2CProperties properties = new AADB2CProperties();
        properties.setReplyUrl("https://example.com");

        assertThat(properties.getLoginProcessingUrl()).isEmpty();
    }

    @Test
    public void getLoginProcessingUrlThrowsForMalformedUrl() {
        final AADB2CProperties properties = new AADB2CProperties();
        properties.setReplyUrl("not-a-valid-url");

        assertThatThrownBy(properties::getLoginProcessingUrl)
                .isInstanceOf(AADB2CConfigurationException.class)
                .hasMessageContaining("Failed to get path of given URL");
    }

    @Test
    public void userFlowsAccessors() {
        final AADB2CProperties properties = new AADB2CProperties();
        final AADB2CProperties.UserFlows userFlows = properties.getUserFlows();
        userFlows.setSignUpOrSignIn("B2C_1_signupsignin");
        userFlows.setProfileEdit("B2C_1_profileedit");
        userFlows.setPasswordReset("B2C_1_passwordreset");

        assertThat(properties.getUserFlows().getSignUpOrSignIn()).isEqualTo("B2C_1_signupsignin");
        assertThat(properties.getUserFlows().getProfileEdit()).isEqualTo("B2C_1_profileedit");
        assertThat(properties.getUserFlows().getPasswordReset()).isEqualTo("B2C_1_passwordreset");
    }
}
