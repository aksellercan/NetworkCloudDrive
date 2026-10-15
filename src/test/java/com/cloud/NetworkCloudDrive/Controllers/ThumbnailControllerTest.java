package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Controllers.Maintenance.ThumbnailController;
import com.cloud.NetworkCloudDrive.Models.Domain.DeletionResults;
import com.cloud.NetworkCloudDrive.Models.Enum.DeletionOptions;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONObjectResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThumbnailControllerTest {

    @Mock
    private ThumbnailRepository thumbnailRepository;

    private ThumbnailController controller;

    @BeforeEach
    void setUp() {
        controller = new ThumbnailController(thumbnailRepository);
    }

    private static ThumbnailMetadata metadata(String name) {
        return new ThumbnailMetadata(name, 1L, "image/jpeg", 42L, 10L, true);
    }

    @Test
    void getThumbnailByID_Found_ReturnsImageResource() throws Exception {
        ThumbnailMetadata metadata = metadata("thumb_abc.jpg");
        Resource resource = new ByteArrayResource(new byte[]{1, 2, 3});
        when(thumbnailRepository.getThumbnailByID(5L)).thenReturn(metadata);
        when(thumbnailRepository.getThumbnail("thumb_abc.jpg", true)).thenReturn(resource);

        ResponseEntity<?> response = controller.getThumbnailByID(5L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(resource, response.getBody());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
        assertEquals(42L, response.getHeaders().getContentLength());
        assertTrue(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION).contains("thumb_abc.jpg"));
    }

    @Test
    void getThumbnailByID_FileSystemException_ReturnsInternalServerError() throws Exception {
        when(thumbnailRepository.getThumbnailByID(5L)).thenReturn(metadata("thumb_abc.jpg"));
        when(thumbnailRepository.getThumbnail("thumb_abc.jpg", true))
                .thenThrow(new FileAlreadyExistsException("/thumbs/thumb_abc.jpg"));

        ResponseEntity<?> response = controller.getThumbnailByID(5L);

        assertEquals(500, response.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, response.getBody());
        JSONErrorResponse body = (JSONErrorResponse) response.getBody();
        assertFalse(body.isSuccess());
        assertEquals("Internal error occurred", body.getMessage());
    }

    @Test
    void getThumbnailByID_GenericException_ReturnsInternalServerError() throws Exception {
        when(thumbnailRepository.getThumbnailByID(5L)).thenThrow(new SQLException("db gone"));

        ResponseEntity<?> response = controller.getThumbnailByID(5L);

        assertEquals(500, response.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, response.getBody());
        assertEquals("Failed to get thumbnail", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void getThumbnailByFileID_Found_ReturnsImageResource() throws Exception {
        ThumbnailMetadata metadata = metadata("file_thumb.png");
        Resource resource = new ByteArrayResource(new byte[]{9, 9});
        when(thumbnailRepository.getThumbnailByFileID(11L)).thenReturn(metadata);
        when(thumbnailRepository.getThumbnail("file_thumb.png", true)).thenReturn(resource);

        ResponseEntity<?> response = controller.getThumbnailByFileID(11L);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(resource, response.getBody());
        assertEquals("image/jpeg", response.getHeaders().getContentType().toString());
    }

    @Test
    void getThumbnailByFileID_FileSystemException_ReturnsInternalServerError() throws Exception {
        when(thumbnailRepository.getThumbnailByFileID(11L)).thenReturn(metadata("file_thumb.png"));
        when(thumbnailRepository.getThumbnail("file_thumb.png", true))
                .thenThrow(new FileAlreadyExistsException("/thumbs/file_thumb.png"));

        ResponseEntity<?> response = controller.getThumbnailByFileID(11L);

        assertEquals(500, response.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, response.getBody());
        assertEquals("Internal error occurred", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void getThumbnailByFileID_GenericException_ReturnsInternalServerError() throws Exception {
        when(thumbnailRepository.getThumbnailByFileID(11L)).thenThrow(new SQLException("no such table"));

        ResponseEntity<?> response = controller.getThumbnailByFileID(11L);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Failed to get thumbnail", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteThumbnailByID_Success_ReturnsOk() throws Exception {
        ResponseEntity<?> response = controller.deleteThumbnailByID(5L);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(JSONResponse.class, response.getBody());
        JSONResponse body = (JSONResponse) response.getBody();
        assertTrue(body.isSuccess());
        assertEquals("Thumbnail with Id 5 was successfully removed", body.getMessage());
        verify(thumbnailRepository).deleteThumbnailByThumbnailID(5L);
    }

    @Test
    void deleteThumbnailByID_FileNotFound_ReturnsInternalServerError() throws Exception {
        doThrow(new FileNotFoundException("missing")).when(thumbnailRepository).deleteThumbnailByThumbnailID(5L);

        ResponseEntity<?> response = controller.deleteThumbnailByID(5L);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal error occurred", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteThumbnailByID_SqlException_ReturnsInternalServerErrorWithFormattedMessage() throws Exception {
        doThrow(new SQLException("no row")).when(thumbnailRepository).deleteThumbnailByThumbnailID(9L);

        ResponseEntity<?> response = controller.deleteThumbnailByID(9L);

        assertEquals(500, response.getStatusCode().value());
        JSONErrorResponse body = (JSONErrorResponse) response.getBody();
        assertEquals("Thumbnail with ID 9 does not exists in database", body.getMessage());
    }

    @Test
    void deleteThumbnailByID_GenericException_ReturnsInternalServerError() throws Exception {
        doThrow(new IllegalStateException("unexpected")).when(thumbnailRepository).deleteThumbnailByThumbnailID(1L);

        ResponseEntity<?> response = controller.deleteThumbnailByID(1L);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Failed to delete thumbnail", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteAllThumbnails_NuclearOption_CallsNuclearDelete() throws Exception {
        DeletionResults results = new DeletionResults();
        results.incrementSuccessfulRemovals();
        when(thumbnailRepository.nuclearDeleteAllThumbnails()).thenReturn(results);

        ResponseEntity<?> response = controller.deleteAllThumbnails(DeletionOptions.NUCLEAR);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(JSONObjectResponse.class, response.getBody());
        JSONObjectResponse body = (JSONObjectResponse) response.getBody();
        assertEquals("Removed all thumbnails", body.getMessage());
        assertEquals(results, body.getObject());
        verify(thumbnailRepository).nuclearDeleteAllThumbnails();
        verify(thumbnailRepository, never()).deleteOnlyFromIO();
        verify(thumbnailRepository, never()).deleteAllThumbnails();
    }

    @Test
    void deleteAllThumbnails_OnlyIOOption_CallsOnlyIODelete() throws Exception {
        DeletionResults results = new DeletionResults();
        when(thumbnailRepository.deleteOnlyFromIO()).thenReturn(results);

        ResponseEntity<?> response = controller.deleteAllThumbnails(DeletionOptions.ONLY_IO);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(JSONObjectResponse.class, response.getBody());
        verify(thumbnailRepository).deleteOnlyFromIO();
        verify(thumbnailRepository, never()).nuclearDeleteAllThumbnails();
    }

    @Test
    void deleteAllThumbnails_DefaultOption_CallsPlainDelete() throws Exception {
        DeletionResults results = new DeletionResults();
        when(thumbnailRepository.deleteAllThumbnails()).thenReturn(results);

        ResponseEntity<?> response = controller.deleteAllThumbnails(DeletionOptions.NORMAL);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(JSONObjectResponse.class, response.getBody());
        verify(thumbnailRepository).deleteAllThumbnails();
        verify(thumbnailRepository, never()).nuclearDeleteAllThumbnails();
    }

    @Test
    void deleteAllThumbnails_FileNotFound_ReturnsInternalServerError() throws Exception {
        when(thumbnailRepository.nuclearDeleteAllThumbnails()).thenThrow(new FileNotFoundException("gone"));

        ResponseEntity<?> response = controller.deleteAllThumbnails(DeletionOptions.NUCLEAR);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal error occurred", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteAllThumbnails_GenericException_ReturnsInternalServerError() throws Exception {
        when(thumbnailRepository.deleteOnlyFromIO()).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> response = controller.deleteAllThumbnails(DeletionOptions.ONLY_IO);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Failed to delete thumbnail", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteAllThumbnailsNoArgs_Success_ReturnsOk() throws Exception {
        ResponseEntity<?> response = controller.deleteAllThumbnails();

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(JSONResponse.class, response.getBody());
        assertEquals("Removed all thumbnails", ((JSONResponse) response.getBody()).getMessage());
        verify(thumbnailRepository).deleteAllThumbnails();
    }

    @Test
    void deleteAllThumbnailsNoArgs_FileNotFound_ReturnsInternalServerError() throws Exception {
        doThrow(new FileNotFoundException("gone")).when(thumbnailRepository).deleteAllThumbnails();

        ResponseEntity<?> response = controller.deleteAllThumbnails();

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal error occurred", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteAllThumbnailsNoArgs_GenericException_ReturnsInternalServerError() throws Exception {
        doThrow(new SQLException("locked")).when(thumbnailRepository).deleteAllThumbnails();

        ResponseEntity<?> response = controller.deleteAllThumbnails();

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Failed to delete thumbnail", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteThumbnailByFileID_Success_ReturnsOk() throws Exception {
        ResponseEntity<?> response = controller.deleteThumbnailByFileID(11L);

        assertEquals(200, response.getStatusCode().value());
        JSONResponse body = (JSONResponse) response.getBody();
        assertTrue(body.isSuccess());
        assertEquals("Thumbnail with related to File Id 11 was successfully removed", body.getMessage());
        verify(thumbnailRepository).deleteThumbnailByFileIDAndSetThumbnailStatus(11L);
    }

    @Test
    void deleteThumbnailByFileID_FileNotFound_ReturnsInternalServerError() throws Exception {
        doThrow(new FileNotFoundException("missing")).when(thumbnailRepository)
                .deleteThumbnailByFileIDAndSetThumbnailStatus(11L);

        ResponseEntity<?> response = controller.deleteThumbnailByFileID(11L);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Internal error occurred", ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteThumbnailByFileID_SqlException_ReturnsInternalServerErrorWithFormattedMessage() throws Exception {
        doThrow(new SQLException("no row")).when(thumbnailRepository)
                .deleteThumbnailByFileIDAndSetThumbnailStatus(11L);

        ResponseEntity<?> response = controller.deleteThumbnailByFileID(11L);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("No thumbnail related to File ID 11 was found in database",
                ((JSONErrorResponse) response.getBody()).getMessage());
    }

    @Test
    void deleteThumbnailByFileID_GenericException_ReturnsInternalServerError() throws Exception {
        doThrow(new IllegalStateException("unexpected")).when(thumbnailRepository)
                .deleteThumbnailByFileIDAndSetThumbnailStatus(11L);

        ResponseEntity<?> response = controller.deleteThumbnailByFileID(11L);

        assertEquals(500, response.getStatusCode().value());
        assertEquals("Failed to delete thumbnail", ((JSONErrorResponse) response.getBody()).getMessage());
    }
}
