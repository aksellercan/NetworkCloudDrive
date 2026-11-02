package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.DTO.CreateFolderDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.UploadOptions;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileRepository fileRepository;
    @Mock
    private InformationRepository informationRepository;
    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private PathUtility pathUtility;

    private FileController controller;

    @BeforeEach
    void setUp() {
        controller = new FileController(informationRepository, encodingUtility, pathUtility, fileRepository);
    }

    @Test
    void uploadFile_Success_ReturnsOk() throws Exception {
        MockMultipartFile[] files = new MockMultipartFile[]{
                new MockMultipartFile("files", "test.txt", "text/plain", "data".getBytes())
        };
        when(pathUtility.getFolderPath(1L)).thenReturn("user_folder");
        Map<String, Object> response = Map.of("uploaded_file_count", 1);
        when(fileRepository.uploadFiles(any(), anyString(), eq(1L))).thenReturn((Map) response);

        ResponseEntity<?> result = controller.uploadFile(files, 1L);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void uploadFile_WithOptions_Success_ReturnsOk() throws Exception {
        MockMultipartFile[] files = new MockMultipartFile[]{
                new MockMultipartFile("files", "test.txt", "text/plain", "data".getBytes())
        };
        when(pathUtility.getFolderPath(1L)).thenReturn("user_folder");
        Map<String, Object> response = Map.of("uploaded_file_count", 1);
        when(fileRepository.uploadFiles(any(), anyString(), eq(1L))).thenReturn((Map) response);

        ResponseEntity<?> result = controller.uploadFile(files, 1L, UploadOptions.ALLOW_DUPLICATES);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void uploadFile_FileAlreadyExists_ReturnsBadRequest() throws Exception {
        MockMultipartFile[] files = new MockMultipartFile[]{
                new MockMultipartFile("files", "test.txt", "text/plain", "data".getBytes())
        };
        when(pathUtility.getFolderPath(1L)).thenReturn("user_folder");
        when(fileRepository.uploadFiles(any(), anyString(), eq(1L)))
                .thenThrow(new FileAlreadyExistsException("test.txt already exists"));

        ResponseEntity<?> result = controller.uploadFile(files, 1L);

        assertEquals(400, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
        JSONErrorResponse body = (JSONErrorResponse) result.getBody();
        assertFalse(body.isSuccess());
        assertEquals(FileAlreadyExistsException.class.getName(), body.getException_type());
    }

    @Test
    void uploadFile_SQLException_ReturnsInternalServerError() throws Exception {
        MockMultipartFile[] files = new MockMultipartFile[]{
                new MockMultipartFile("files", "test.txt", "text/plain", "data".getBytes())
        };
        when(pathUtility.getFolderPath(1L)).thenReturn("user_folder");
        when(fileRepository.uploadFiles(any(), anyString(), eq(1L)))
                .thenThrow(new SQLException("db down"));

        ResponseEntity<?> result = controller.uploadFile(files, 1L);

        assertEquals(500, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
        JSONErrorResponse body = (JSONErrorResponse) result.getBody();
        assertEquals("SQL error occurred", body.getMessage());
    }

    @Test
    void uploadFile_GenericException_ReturnsInternalServerError() throws Exception {
        MockMultipartFile[] files = new MockMultipartFile[]{
                new MockMultipartFile("files", "test.txt", "text/plain", "data".getBytes())
        };
        when(pathUtility.getFolderPath(1L)).thenThrow(new RuntimeException("unexpected failure"));

        ResponseEntity<?> result = controller.uploadFile(files, 1L);

        assertEquals(500, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
        JSONErrorResponse body = (JSONErrorResponse) result.getBody();
        assertEquals("Failed to upload file", body.getMessage());
    }

    @Test
    void uploadFile_NoFilesProvided_ReturnsInternalServerError() {
        MockMultipartFile[] files = new MockMultipartFile[0];

        ResponseEntity<?> result = controller.uploadFile(files, 1L);

        assertEquals(500, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
        JSONErrorResponse body = (JSONErrorResponse) result.getBody();
        assertEquals("Failed to upload file", body.getMessage());
        assertEquals("No files provided", body.getException_message());
    }

    @Test
    void uploadFile_WithOptions_FileAlreadyExists_ReturnsBadRequest() throws Exception {
        MockMultipartFile[] files = new MockMultipartFile[]{
                new MockMultipartFile("files", "test.txt", "text/plain", "data".getBytes())
        };
        when(pathUtility.getFolderPath(1L)).thenReturn("user_folder");
        when(fileRepository.uploadFiles(any(), anyString(), eq(1L)))
                .thenThrow(new FileAlreadyExistsException("test.txt already exists"));

        ResponseEntity<?> result = controller.uploadFile(files, 1L, UploadOptions.DONT_ALLOW_DUPLICATES);

        assertEquals(400, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
    }

    @Test
    void downloadFile_Success_ReturnsFile() throws Exception {
        FileMetadata metadata = new FileMetadata("report.pdf", 1L, 1L, "application/pdf", 100L);
        metadata.setId(10L);
        when(informationRepository.getFileMetadata(10L)).thenReturn(metadata);
        when(pathUtility.getFolderPath(1L)).thenReturn("user_folder");
        when(fileRepository.getFile(metadata, "user_folder"))
                .thenReturn(CompletableFuture.completedFuture(new ByteArrayResource("data".getBytes())));

        ResponseEntity<?> result = controller.downloadFile(10L);

        assertEquals(200, result.getStatusCode().value());
        assertInstanceOf(ByteArrayResource.class, result.getBody());
        assertEquals("attachment; filename=\"report.pdf\" ", result.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION));
        assertEquals("application/pdf", result.getHeaders().getContentType().toString());
        assertEquals(100L, result.getHeaders().getContentLength());
        verify(fileRepository).getFile(metadata, "user_folder");
    }

    @Test
    void downloadFile_FileSystemException_PropagatesException() throws Exception {
        FileMetadata metadata = new FileMetadata("encoded", 1L, 1L, "application/pdf", 100L);
        metadata.setId(10L);
        when(informationRepository.getFileMetadata(10L)).thenReturn(metadata);
        when(pathUtility.getFolderPath(1L)).thenThrow(new FileSystemException("missing_folder"));

        assertThrows(FileSystemException.class, () -> controller.downloadFile(10L));
    }

    @Test
    void downloadFile_MetadataNotFound_PropagatesException() throws Exception {
        when(informationRepository.getFileMetadata(999L))
                .thenThrow(new FileNotFoundException("Not found"));

        assertThrows(FileNotFoundException.class, () -> controller.downloadFile(999L));
    }

    @Test
    void createFolder_Success_ReturnsOk() throws Exception {
        CreateFolderDTO dto = new CreateFolderDTO();
        dto.setFolder_id(0L);
        dto.getPath("docs"); // CreateFolderDTO's name setter is (confusingly) named getPath
        FolderMetadata created = new FolderMetadata("docs", "0/7");
        created.setId(7L);
        created.setParentFolderId(0L);
        when(fileRepository.createFolder("docs", 0L)).thenReturn(created);
        when(pathUtility.renovedPathConstructor(7L)).thenReturn("user_root/docs");

        ResponseEntity<?> result = controller.createFolder(dto);

        assertEquals(200, result.getStatusCode().value());
        assertInstanceOf(FolderMetadata.class, result.getBody());
        FolderMetadata body = (FolderMetadata) result.getBody();
        assertEquals(7L, body.getId());
        assertEquals("docs", body.getName());
        assertEquals("user_root/docs", body.getPath());
        verify(fileRepository).createFolder("docs", 0L);
        verify(pathUtility).renovedPathConstructor(7L);
    }

    @Test
    void createFolder_AlreadyExists_PropagatesException() throws Exception {
        CreateFolderDTO dto = new CreateFolderDTO();
        dto.setFolder_id(0L);
        dto.getPath("docs");
        when(fileRepository.createFolder("docs", 0L))
                .thenThrow(new FileAlreadyExistsException("docs already exists"));

        assertThrows(FileAlreadyExistsException.class, () -> controller.createFolder(dto));
    }

    @Test
    void createFolder_SecurityException_PropagatesException() throws Exception {
        CreateFolderDTO dto = new CreateFolderDTO();
        dto.setFolder_id(0L);
        dto.getPath("docs");
        when(fileRepository.createFolder("docs", 0L))
                .thenThrow(new SecurityException("path not allowed"));

        assertThrows(SecurityException.class, () -> controller.createFolder(dto));
    }

    @Test
    void createFolder_GenericException_PropagatesException() throws Exception {
        CreateFolderDTO dto = new CreateFolderDTO();
        dto.setFolder_id(0L);
        dto.getPath("docs");
        when(fileRepository.createFolder("docs", 0L))
                .thenThrow(new RuntimeException("disk failure"));

        assertThrows(RuntimeException.class, () -> controller.createFolder(dto));
    }

    @Test
    void markFile_Success_ReturnsOk() throws Exception {
        ResponseEntity<?> result = controller.markFile(true, 10L);

        assertEquals(200, result.getStatusCode().value());
        verify(fileRepository).markFile(true, 10L);
        assertInstanceOf(JSONResponse.class, result.getBody());
        JSONResponse body = (JSONResponse) result.getBody();
        assertTrue(body.isSuccess());
        assertEquals("Marked file as true successfully", body.getMessage());
    }

    @Test
    void markFile_SQLException_ReturnsInternalServerError() throws Exception {
        doThrow(new SQLException("db down")).when(fileRepository).markFile(false, 10L);

        ResponseEntity<?> result = controller.markFile(false, 10L);

        assertEquals(500, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
        JSONErrorResponse body = (JSONErrorResponse) result.getBody();
        assertEquals("Internal Error", body.getMessage());
    }

    @Test
    void markFolder_Success_ReturnsOk() throws Exception {
        ResponseEntity<?> result = controller.markFolder(true, 5L);

        assertEquals(200, result.getStatusCode().value());
        verify(fileRepository).markFolder(true, 5L);
        assertInstanceOf(JSONResponse.class, result.getBody());
        JSONResponse body = (JSONResponse) result.getBody();
        assertTrue(body.isSuccess());
        assertEquals("Marked folder as true successfully", body.getMessage());
    }

    @Test
    void markFolder_SQLException_ReturnsInternalServerError() throws Exception {
        doThrow(new SQLException("db down")).when(fileRepository).markFolder(false, 5L);

        ResponseEntity<?> result = controller.markFolder(false, 5L);

        assertEquals(500, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
        JSONErrorResponse body = (JSONErrorResponse) result.getBody();
        assertEquals("Internal Error", body.getMessage());
    }
}
