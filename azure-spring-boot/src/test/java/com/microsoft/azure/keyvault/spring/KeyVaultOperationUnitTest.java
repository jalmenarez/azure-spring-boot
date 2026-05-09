/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.keyvault.spring;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class KeyVaultOperationUnitTest {

    private static final String TEST_PROPERTY_NAME_1 = "testpropertynamе1";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_0 = "acme.my-project.person.first-name";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_1 = "acme.myProject.person.firstName";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_2 = "acme.my_project.person.first_name";

    private static final String TEST_SPRING_RELAXED_BINDING_NAME_3 = "ACME_MYPROJECT_PERSON_FIRSTNAME";

    private static final String TEST_AZURE_KEYVAULT_NAME = "acme-myproject-person-firstname";

    private static final List<String> TEST_SPRING_RELAXED_BINDING_NAMES = Arrays.asList(
            TEST_SPRING_RELAXED_BINDING_NAME_0,
            TEST_SPRING_RELAXED_BINDING_NAME_1,
            TEST_SPRING_RELAXED_BINDING_NAME_2,
            TEST_SPRING_RELAXED_BINDING_NAME_3
    );

    private static KeyVaultOperation buildOperation(final String secretName, final String secretValue) {
        final Map<String, String> store = new HashMap<>();
        store.put(secretName.toLowerCase(Locale.US), secretValue);
        return new KeyVaultOperation(
            action -> Collections.singletonList(secretName).forEach(action),
            store::get,
            Constants.DEFAULT_REFRESH_INTERVAL_MS
        );
    }

    @Test
    public void testGet() {
        final KeyVaultOperation op = buildOperation(TEST_PROPERTY_NAME_1, TEST_PROPERTY_NAME_1);
        assertThat(op.get(TEST_PROPERTY_NAME_1)).isEqualTo(TEST_PROPERTY_NAME_1);
    }

    @Test
    public void testList() {
        final KeyVaultOperation op = buildOperation(TEST_PROPERTY_NAME_1, TEST_PROPERTY_NAME_1);
        final String[] result = op.list();
        assertThat(result).contains(TEST_PROPERTY_NAME_1.toLowerCase(Locale.US));
    }

    @Test
    public void setTestSpringRelaxedBindingNames() {
        final KeyVaultOperation op = buildOperation(TEST_AZURE_KEYVAULT_NAME, TEST_AZURE_KEYVAULT_NAME);
        TEST_SPRING_RELAXED_BINDING_NAMES.forEach(
            n -> assertThat(op.get(n)).isEqualTo(TEST_AZURE_KEYVAULT_NAME)
        );
    }
}
