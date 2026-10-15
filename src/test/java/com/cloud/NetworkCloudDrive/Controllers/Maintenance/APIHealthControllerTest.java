package com.cloud.NetworkCloudDrive.Controllers.Maintenance;

import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class APIHealthControllerTest {

    @Mock
    private BuildProperties buildProperties;

    @Test
    void getAPIVersion_ReturnsOkWithVersionFromBuildProperties() {
        when(buildProperties.getVersion()).thenReturn("1.2.3");

        APIHealthController controller = new APIHealthController(buildProperties);
        ResponseEntity<JSONResponse> response = controller.getAPIVersion();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("1.2.3", response.getBody().getMessage());
        verify(buildProperties).getVersion();
    }

    @Test
    void getAPIVersion_HandlesNullVersion_ReturnsOkWithNullMessage() {
        when(buildProperties.getVersion()).thenReturn(null);

        APIHealthController controller = new APIHealthController(buildProperties);
        ResponseEntity<JSONResponse> response = controller.getAPIVersion();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertNull(response.getBody().getMessage());
        verify(buildProperties).getVersion();
    }
}
