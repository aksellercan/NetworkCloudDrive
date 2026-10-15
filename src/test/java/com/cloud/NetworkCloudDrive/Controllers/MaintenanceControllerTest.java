package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Controllers.Maintenance.MaintenanceController;
import com.cloud.NetworkCloudDrive.Models.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONObjectResponse;
import com.cloud.NetworkCloudDrive.Models.Response.ScanTaskResponse;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.MaintenanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceControllerTest {

    @Mock
    private MaintenanceRepository maintenanceRepository;

    private MaintenanceController controller;

    @BeforeEach
    void setUp() {
        controller = new MaintenanceController(maintenanceRepository);
    }

    @Test
    void scanDirectory_QueuesScan_ReturnsOk() throws Exception {
        ScanTaskResponse task = new ScanTaskResponse("Maintenance Job", null, null, null);
        when(maintenanceRepository.queueScan(eq(0L), eq(ScanOptions.NORMAL))).thenReturn(task);

        ResponseEntity<?> response = controller.scanDirectory(ScanOptions.NORMAL);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(JSONObjectResponse.class, response.getBody());
        JSONObjectResponse body = (JSONObjectResponse) response.getBody();
        assertTrue(body.isSuccess());
        assertEquals("Scan queued successfully", body.getMessage());
        assertEquals(task, body.getObject());
    }

    @Test
    void scanDirectory_RepositoryThrows_ReturnsBadRequest() throws Exception {
        when(maintenanceRepository.queueScan(anyLong(), eq(ScanOptions.NORMAL)))
                .thenThrow(new SQLException("Database down"));

        ResponseEntity<?> response = controller.scanDirectory(ScanOptions.NORMAL);

        assertEquals(400, response.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, response.getBody());
        JSONErrorResponse body = (JSONErrorResponse) response.getBody();
        assertFalse(body.isSuccess());
        assertEquals("Error scanning", body.getMessage());
    }

    @Test
    void scanDirectoryNestedFolders_UsesNormalScan_ReturnsOk() throws Exception {
        ScanTaskResponse task = new ScanTaskResponse("Maintenance Job", null, null, null);
        when(maintenanceRepository.queueScan(eq(7L), eq(ScanOptions.NORMAL))).thenReturn(task);

        ResponseEntity<?> response = controller.scanDirectoryNestedFolders(7L);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(JSONObjectResponse.class, response.getBody());
        assertEquals(task, ((JSONObjectResponse) response.getBody()).getObject());
        verify(maintenanceRepository).queueScan(7L, ScanOptions.NORMAL);
    }

    @Test
    void scanDirectoryNestedFolders_RepositoryThrows_ReturnsBadRequest() throws Exception {
        when(maintenanceRepository.queueScan(anyLong(), eq(ScanOptions.NORMAL)))
                .thenThrow(new IllegalStateException("Executor shut down"));

        ResponseEntity<?> response = controller.scanDirectoryNestedFolders(3L);

        assertEquals(400, response.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, response.getBody());
        assertEquals("Error scanning", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void scanDirectoryOptions_PassesFolderIdAndOptions_ReturnsOk() throws Exception {
        ScanTaskResponse task = new ScanTaskResponse("Maintenance Job", null, null, null);
        when(maintenanceRepository.queueScan(eq(12L), eq(ScanOptions.ONLY_THUMBNAILS))).thenReturn(task);

        ResponseEntity<?> response = controller.scanDirectoryOptions(12L, ScanOptions.ONLY_THUMBNAILS);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(JSONObjectResponse.class, response.getBody());
        JSONObjectResponse body = (JSONObjectResponse) response.getBody();
        assertTrue(body.isSuccess());
        assertEquals(task, body.getObject());
        verify(maintenanceRepository).queueScan(12L, ScanOptions.ONLY_THUMBNAILS);
    }

    @Test
    void scanDirectoryOptions_RepositoryThrows_ReturnsBadRequest() throws Exception {
        when(maintenanceRepository.queueScan(anyLong(), eq(ScanOptions.ONLY_FILES)))
                .thenThrow(new SQLException("lock timeout"));

        ResponseEntity<?> response = controller.scanDirectoryOptions(1L, ScanOptions.ONLY_FILES);

        assertEquals(400, response.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, response.getBody());
        assertFalse(((JSONErrorResponse) response.getBody()).isSuccess());
    }
}
