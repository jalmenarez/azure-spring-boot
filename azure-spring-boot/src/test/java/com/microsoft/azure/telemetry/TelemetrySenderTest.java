/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.telemetry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.spring.support.GetHashMac;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TelemetrySenderTest {

    private TelemetrySender telemetrySender;
    private RestTemplate mockRestTemplate;

    @Before
    public void setUp() {
        telemetrySender = new TelemetrySender();
        mockRestTemplate = mock(RestTemplate.class);
        ReflectionTestUtils.setField(TelemetrySender.class, "REST_TEMPLATE", mockRestTemplate);
    }

    @Test
    public void testSendWithValidEventData() {
        ResponseEntity<String> successResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        when(mockRestTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(successResponse);

        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");

        telemetrySender.send("TestEvent", properties);

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockRestTemplate).exchange(urlCaptor.capture(), any(), any(), eq(String.class));

        assertEquals("https://dc.services.visualstudio.com/v2/track", urlCaptor.getValue());
    }

    @Test
    public void testSendAddsInstallationIdToProperties() {
        ResponseEntity<String> successResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        when(mockRestTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(successResponse);

        Map<String, String> properties = new HashMap<>();
        properties.put("key1", "value1");

        telemetrySender.send("TestEvent", properties);

        assertTrue(properties.containsKey(TelemetryData.INSTALLATION_ID));
        assertTrue(properties.containsKey(TelemetryData.PROJECT_VERSION));
    }

    @Test
    public void testSendWithNullProperties() {
        ResponseEntity<String> successResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        when(mockRestTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(successResponse);

        assertThrows(IllegalArgumentException.class, () -> {
            telemetrySender.send("TestEvent", null);
        });
    }

    @Test
    public void testSendWithEmptyEventName() {
        Map<String, String> properties = new HashMap<>();

        assertThrows(IllegalArgumentException.class, () -> {
            telemetrySender.send("", properties);
        });
    }

    @Test
    public void testSendWithNullEventName() {
        Map<String, String> properties = new HashMap<>();

        assertThrows(IllegalArgumentException.class, () -> {
            telemetrySender.send(null, properties);
        });
    }

    @Test
    public void testSendRetryOnFailure() {
        ResponseEntity<String> failureResponse = new ResponseEntity<>("Error", HttpStatus.INTERNAL_SERVER_ERROR);
        when(mockRestTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(failureResponse);

        Map<String, String> properties = new HashMap<>();
        telemetrySender.send("TestEvent", properties);

        verify(mockRestTemplate, times(3)).exchange(anyString(), any(), any(), eq(String.class));
    }

    @Test
    public void testSendStopsOnSuccess() {
        ResponseEntity<String> successResponse = new ResponseEntity<>("OK", HttpStatus.OK);
        when(mockRestTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenReturn(successResponse);

        Map<String, String> properties = new HashMap<>();
        telemetrySender.send("TestEvent", properties);

        verify(mockRestTemplate, times(1)).exchange(anyString(), any(), any(), eq(String.class));
    }

    @Test
    public void testSendWithException() {
        when(mockRestTemplate.exchange(anyString(), any(), any(), eq(String.class)))
            .thenThrow(new RuntimeException("Connection error"));

        Map<String, String> properties = new HashMap<>();
        telemetrySender.send("TestEvent", properties);

        verify(mockRestTemplate, times(3)).exchange(anyString(), any(), any(), eq(String.class));
    }
}
