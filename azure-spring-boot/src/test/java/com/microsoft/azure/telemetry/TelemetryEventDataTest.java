/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TelemetryEventDataTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void testTelemetryEventDataCreation() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");
        properties.put("key2", "value2");

        final TelemetryEventData eventData = new TelemetryEventData("TestEvent", properties);

        assertNotNull(eventData);
        assertEquals("Microsoft.ApplicationInsights.Event", eventData.getName());
        assertNotNull(eventData.getInstrumentationKey());
        assertNotNull(eventData.getTags());
        assertNotNull(eventData.getData());
        assertNotNull(eventData.getTime());
    }

    @Test
    public void testTelemetryEventDataWithEmptyProperties() {
        final Map<String, String> properties = new HashMap<>();

        final TelemetryEventData eventData = new TelemetryEventData("TestEvent", properties);

        assertNotNull(eventData);
        assertEquals("Microsoft.ApplicationInsights.Event", eventData.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTelemetryEventDataWithNullEventName() {
        final Map<String, String> properties = new HashMap<>();
        new TelemetryEventData(null, properties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTelemetryEventDataWithEmptyEventName() {
        final Map<String, String> properties = new HashMap<>();
        new TelemetryEventData("", properties);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTelemetryEventDataWithNullProperties() {
        new TelemetryEventData("TestEvent", null);
    }

    @Test
    public void testTelemetryEventDataTimeFormat() {
        final Map<String, String> properties = new HashMap<>();
        final TelemetryEventData eventData = new TelemetryEventData("TestEvent", properties);

        final String time = eventData.getTime();
        assertNotNull(time);
        assertTrue(time.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*Z?"));
    }

    @Test
    public void testTelemetryEventDataSerialization() throws Exception {
        final Map<String, String> properties = new HashMap<>();
        properties.put("testKey", "testValue");

        final TelemetryEventData eventData = new TelemetryEventData("TestEvent", properties);
        final String json = MAPPER.writeValueAsString(eventData);

        assertNotNull(json);
        assertTrue(json.contains("Microsoft.ApplicationInsights.Event"));
        assertTrue(json.contains("TestEvent"));
        assertTrue(json.contains("iKey"));
    }

    @Test
    public void testTelemetryEventDataWithMultipleProperties() {
        final Map<String, String> properties = new HashMap<>();
        properties.put("prop1", "value1");
        properties.put("prop2", "value2");
        properties.put("prop3", "value3");

        final TelemetryEventData eventData = new TelemetryEventData("MultiPropEvent", properties);

        assertNotNull(eventData);
        assertEquals("Microsoft.ApplicationInsights.Event", eventData.getName());
    }

    @Test
    public void testTelemetryEventDataTagsContent() {
        final Map<String, String> properties = new HashMap<>();
        final TelemetryEventData eventData = new TelemetryEventData("TestEvent", properties);

        assertNotNull(eventData.getTags());
    }

    @Test
    public void testTelemetryEventDataBaseData() {
        final Map<String, String> properties = new HashMap<>();
        final TelemetryEventData eventData = new TelemetryEventData("TestEvent", properties);

        assertNotNull(eventData.getData());
    }
}
