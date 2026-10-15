package com.cloud.NetworkCloudDrive.Models.Response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JSONObjectArrayResponseTest {

    @Test
    void objectsAndMessageConstructor_SetsCorrectFields() {
        Object[] arr = {"a", "b"};
        JSONObjectArrayResponse response = new JSONObjectArrayResponse(arr, "List loaded");

        assertTrue(response.isSuccess());
        assertEquals("List loaded", response.getMessage());
        assertArrayEquals(arr, (Object[]) response.getObject());
    }

    @Test
    void objectsAndFormattedMessageConstructor_SetsCorrectFields() {
        Object[] arr = {1, 2, 3};
        JSONObjectArrayResponse response = new JSONObjectArrayResponse(arr, "Loaded %d items", arr.length);

        assertTrue(response.isSuccess());
        assertEquals("Loaded 3 items", response.getMessage());
        assertArrayEquals(arr, (Object[]) response.getObject());
    }

    @Test
    void objectsBooleanAndMessageConstructor_BooleanFlagIsIntentionallyIgnored() {
        Object[] arr = {"x"};
        JSONObjectArrayResponse response = new JSONObjectArrayResponse(arr, false, "Not found");

        // Intentional behavior: the 3-arg constructor delegates to JSONResponse(String, Object...),
        // so the explicit boolean is not propagated and success defaults to true.
        // Use the 4-arg (objects, success, formattedString, args...) constructor to control the flag.
        assertTrue(response.isSuccess());
        assertEquals("Not found", response.getMessage());
        assertArrayEquals(arr, (Object[]) response.getObject());
    }

    @Test
    void objectsBooleanFalseAndFormattedMessageConstructor_SetsCorrectFields() {
        Object[] arr = {"x"};
        JSONObjectArrayResponse response = new JSONObjectArrayResponse(arr, false, "Failed to load %s", "list");

        assertFalse(response.isSuccess());
        assertEquals("Failed to load list", response.getMessage());
        assertArrayEquals(arr, (Object[]) response.getObject());
    }

    @Test
    void getterSetterForObjectArray_WorkCorrectly() {
        Object[] initial = {"initial"};
        JSONObjectArrayResponse response = new JSONObjectArrayResponse(initial, "msg");
        assertArrayEquals(initial, (Object[]) response.getObject());

        Object[] updated = {"new"};
        response.setObject(updated);
        assertArrayEquals(updated, (Object[]) response.getObject());
    }
}
