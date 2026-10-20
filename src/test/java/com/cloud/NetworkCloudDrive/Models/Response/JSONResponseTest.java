package com.cloud.NetworkCloudDrive.Models.Response;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JSONResponseTest {

    @Test
    void booleanAndMessageConstructor_SetsFields() {
        JSONResponse response = new JSONResponse(false, "Something failed");

        assertFalse(response.isSuccess());
        assertEquals("Something failed", response.getMessage());
        assertNotNull(response.getDate_time());
    }

    @Test
    void messageConstructor_DefaultsSuccessToTrue() {
        JSONResponse response = new JSONResponse("All good");

        assertTrue(response.isSuccess());
        assertEquals("All good", response.getMessage());
    }

    @Test
    void formattedMessageConstructor_FormatsAndDefaultsSuccessToTrue() {
        JSONResponse response = new JSONResponse("Hello %s, you have %d messages", "aksell", 3);

        assertTrue(response.isSuccess());
        assertEquals("Hello aksell, you have 3 messages", response.getMessage());
    }

    @Test
    void booleanAndFormattedMessageConstructor_SetsFields() {
        JSONResponse response = new JSONResponse(true, "Answer is %d", 42);

        assertTrue(response.isSuccess());
        assertEquals("Answer is 42", response.getMessage());
    }

    @Test
    void setters_Work() {
        JSONResponse response = new JSONResponse("original");
        response.setSuccess(false);
        response.setMessage("changed");

        assertFalse(response.isSuccess());
        assertEquals("changed", response.getMessage());
    }
}
