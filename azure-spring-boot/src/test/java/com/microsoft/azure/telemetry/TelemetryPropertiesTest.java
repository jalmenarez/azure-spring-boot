/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.telemetry;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TelemetryPropertiesTest {

    private TelemetryProperties telemetryProperties;

    @Before
    public void setUp() {
        telemetryProperties = new TelemetryProperties();
    }

    @Test
    public void testTelemetryPropertiesCreation() {
        assertNotNull(telemetryProperties);
    }

    @Test
    public void testSetAndGetInstrumentationKey() {
        final String key = "test-instrumentation-key-12345";
        telemetryProperties.setInstrumentationKey(key);

        assertEquals(key, telemetryProperties.getInstrumentationKey());
    }

    @Test
    public void testInstrumentationKeyIsNullByDefault() {
        assertNull(telemetryProperties.getInstrumentationKey());
    }

    @Test
    public void testSetInstrumentationKeyToNull() {
        telemetryProperties.setInstrumentationKey("some-key");
        telemetryProperties.setInstrumentationKey(null);

        assertNull(telemetryProperties.getInstrumentationKey());
    }

    @Test
    public void testSetInstrumentationKeyToEmptyString() {
        telemetryProperties.setInstrumentationKey("");

        assertEquals("", telemetryProperties.getInstrumentationKey());
    }

    @Test
    public void testMultipleInstrumentationKeyUpdates() {
        telemetryProperties.setInstrumentationKey("key1");
        assertEquals("key1", telemetryProperties.getInstrumentationKey());

        telemetryProperties.setInstrumentationKey("key2");
        assertEquals("key2", telemetryProperties.getInstrumentationKey());

        telemetryProperties.setInstrumentationKey("key3");
        assertEquals("key3", telemetryProperties.getInstrumentationKey());
    }

    @Test
    public void testInstrumentationKeyWithSpecialCharacters() {
        final String keyWithSpecialChars = "key-with-!@#$%^&*()-_=+[]{}|;:',.<>?/";
        telemetryProperties.setInstrumentationKey(keyWithSpecialChars);

        assertEquals(keyWithSpecialChars, telemetryProperties.getInstrumentationKey());
    }
}
