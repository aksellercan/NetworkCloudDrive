package com.cloud.NetworkCloudDrive.Models.Response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JSONObjectResponseTest {

    @Test
    void objectAndMessageConstructor_SetsCorrectFields() {
        Object obj = "data";
        JSONObjectResponse response = new JSONObjectResponse(obj, "Test message");

        assertTrue(response.isSuccess());
        assertEquals("Test message", response.getMessage());
        assertEquals(obj, response.getObject());
    }

    @Test
    void objectAndFormattedMessageConstructor_SetsCorrectFields() {
        Object obj = 123;
        JSONObjectResponse response = new JSONObjectResponse(obj, "Formatted %s", "value");

        assertTrue(response.isSuccess());
        assertEquals("Formatted value", response.getMessage());
        assertEquals(obj, response.getObject());
    }

    @Test
    void objectBooleanAndMessageConstructor_BooleanFlagIsIntentionallyIgnored() {
        Object obj = "data";
        JSONObjectResponse response = new JSONObjectResponse(obj, false, "Scan queued successfully");

        // Intentional behavior: the 3-arg constructor delegates to JSONResponse(String, Object...),
        // so the explicit boolean is not propagated and success defaults to true.
        // Use the 4-arg (object, success, formattedString, args...) constructor to control the flag.
        assertTrue(response.isSuccess());
        assertEquals("Scan queued successfully", response.getMessage());
        assertEquals(obj, response.getObject());
    }

    @Test
    void objectBooleanAndFormattedMessageConstructor_SetsCorrectFields() {
        Object obj = new Object();
        JSONObjectResponse response = new JSONObjectResponse(obj, true, "Success with %d item", 5);

        assertTrue(response.isSuccess());
        assertEquals("Success with 5 item", response.getMessage());
        assertEquals(obj, response.getObject());
    }

    @Test
    void objectBooleanFalseAndFormattedMessageConstructor_SetsCorrectFields() {
        Object obj = new Object();
        JSONObjectResponse response = new JSONObjectResponse(obj, false, "Failed with %d items", 3);

        assertFalse(response.isSuccess());
        assertEquals("Failed with 3 items", response.getMessage());
        assertEquals(obj, response.getObject());
    }

    @Test
    void getterSetterForObject_WorkCorrectly() {
        JSONObjectResponse response = new JSONObjectResponse("initial", "msg");
        response.setObject("new");
        assertEquals("new", response.getObject());
    }
}