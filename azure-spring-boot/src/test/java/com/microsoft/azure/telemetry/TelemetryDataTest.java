/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.telemetry;

import org.junit.Test;

import static org.junit.Assert.*;

public class TelemetryDataTest {

    @Test
    public void testTelemetryDataConstants() {
        assertEquals("installationId", TelemetryData.INSTALLATION_ID);
        assertEquals("version", TelemetryData.PROJECT_VERSION);
        assertEquals("serviceName", TelemetryData.SERVICE_NAME);
        assertEquals("hashedAccountName", TelemetryData.HASHED_ACCOUNT_NAME);
        assertEquals("hashedNamespace", TelemetryData.HASHED_NAMESPACE);
        assertEquals("tenantName", TelemetryData.TENANT_NAME);
    }

    @Test
    public void testGetClassPackageSimpleNameWithValidClass() {
        final String result = TelemetryData.getClassPackageSimpleName(String.class);
        assertEquals("lang", result);
    }

    @Test
    public void testGetClassPackageSimpleNameWithCustomClass() {
        final String result = TelemetryData.getClassPackageSimpleName(TelemetryData.class);
        assertEquals("telemetry", result);
    }

    @Test
    public void testGetClassPackageSimpleNameWithNullClass() {
        final String result = TelemetryData.getClassPackageSimpleName(null);
        assertEquals("unknown", result);
    }

    @Test
    public void testGetClassPackageSimpleNameWithDeeplyNestedClass() {
        final Class<?> clazz = com.microsoft.azure.telemetry.TelemetryData.class;
        final String result = TelemetryData.getClassPackageSimpleName(clazz);
        assertEquals("telemetry", result);
    }

    @Test
    public void testGetClassPackageSimpleNameRemovesAllQualifiers() {
        final String packageName = "com.microsoft.azure.telemetry";
        final Class<?> testClass = com.microsoft.azure.telemetry.TelemetryData.class;

        final String result = TelemetryData.getClassPackageSimpleName(testClass);

        assertNotNull(result);
        assertFalse(result.contains("."));
        assertFalse(result.contains("com"));
        assertFalse(result.contains("microsoft"));
        assertFalse(result.contains("azure"));
    }

    @Test
    public void testGetClassPackageSimpleNameWithBuiltInClass() {
        final String result = TelemetryData.getClassPackageSimpleName(Integer.class);
        assertEquals("lang", result);
    }

    @Test
    public void testGetClassPackageSimpleNameWithUtilClass() {
        final String result = TelemetryData.getClassPackageSimpleName(java.util.HashMap.class);
        assertEquals("util", result);
    }

    @Test
    public void testTelemetryDataHasOnlyStaticMembers() {
        assertNotNull(TelemetryData.INSTALLATION_ID);
        assertNotNull(TelemetryData.SERVICE_NAME);
    }

    @Test
    public void testConstantsAreNotNull() {
        assertNotNull(TelemetryData.INSTALLATION_ID);
        assertNotNull(TelemetryData.PROJECT_VERSION);
        assertNotNull(TelemetryData.SERVICE_NAME);
        assertNotNull(TelemetryData.HASHED_ACCOUNT_NAME);
        assertNotNull(TelemetryData.HASHED_NAMESPACE);
        assertNotNull(TelemetryData.TENANT_NAME);
    }

    @Test
    public void testConstantsAreNotEmpty() {
        assertFalse(TelemetryData.INSTALLATION_ID.isEmpty());
        assertFalse(TelemetryData.PROJECT_VERSION.isEmpty());
        assertFalse(TelemetryData.SERVICE_NAME.isEmpty());
        assertFalse(TelemetryData.HASHED_ACCOUNT_NAME.isEmpty());
        assertFalse(TelemetryData.HASHED_NAMESPACE.isEmpty());
        assertFalse(TelemetryData.TENANT_NAME.isEmpty());
    }
}
