package com.cloud.NetworkCloudDrive.Controllers.Maintenance;

import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import com.cloud.NetworkCloudDrive.Utilities.APIComponent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class APIHealthControllerTest {

    @Mock
    private APIComponent apiComponent;

    @Test
    void getAPIVersion_ReturnsOkWithVersionFromBuildProperties() {
        when(apiComponent.getAPIVersion()).thenReturn("1.2.3");

        APIHealthController controller = new APIHealthController(apiComponent);
        ResponseEntity<JSONResponse> response = controller.getAPIVersion();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("1.2.3", response.getBody().getMessage());
        verify(apiComponent).getAPIVersion();
    }

    @Test
    void getAPIVersion_HandlesNullVersion_ReturnsOkWithNullMessage() {
        when(apiComponent.getAPIVersion()).thenReturn(null);

        APIHealthController controller = new APIHealthController(apiComponent);
        ResponseEntity<JSONResponse> response = controller.getAPIVersion();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNull(response.getBody().getMessage());
        verify(apiComponent).getAPIVersion();
    }
}
