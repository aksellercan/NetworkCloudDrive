package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InformationControllerTest {

    @Mock
    private InformationRepository informationRepository;
    @Mock
    private UserSession userSession;
    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private UserUtility userUtility;
    @Mock
    private PathUtility pathUtility;

    private InformationController controller;

    @BeforeEach
    void setUp() {
        controller = new InformationController(informationRepository, userSession, encodingUtility, userUtility, pathUtility);
    }

    @Test
    void getFile_Found_ReturnsOkWithDecodedName() throws Exception {
        FileMetadata metadata = new FileMetadata("encodedName", 1L, 1L, "text/plain", 100L);
        metadata.setId(10L);
        when(informationRepository.getFileMetadata(10L)).thenReturn(metadata);
        when(encodingUtility.decodeBase32StringNoPadding("encodedName")).thenReturn("10:report.txt");

        ResponseEntity<?> result = controller.getFile(10L);

        assertEquals(200, result.getStatusCode().value());
        assertInstanceOf(FileMetadata.class, result.getBody());
        FileMetadata body = (FileMetadata) result.getBody();
        assertEquals(10L, body.getId());
        assertEquals("report.txt", body.getName());
        assertEquals("text/plain", body.getMimiType());
    }

    @Test
    void getFile_NotFound_ReturnsInternalServerError() throws Exception {
        when(informationRepository.getFileMetadata(999L))
                .thenThrow(new FileNotFoundException("Not found"));

        ResponseEntity<?> result = controller.getFile(999L);

        assertEquals(500, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
        JSONErrorResponse body = (JSONErrorResponse) result.getBody();
        assertEquals(false, body.isSuccess());
        assertEquals(FileNotFoundException.class.getName(), body.getException_type());
        assertEquals("Not found", body.getException_message());
    }

    @Test
    void getFolder_Found_ReturnsOkWithResolvedPath() throws Exception {
        FolderMetadata folder = new FolderMetadata("encodedFolder", "0/5");
        folder.setId(5L);
        when(informationRepository.getFolderMetadata(5L)).thenReturn(folder);
        when(pathUtility.resolvePathFromIdString("0/5")).thenReturn("/root/user/docs");
        when(encodingUtility.decodeBase32StringNoPadding("encodedFolder")).thenReturn("5:docs");

        ResponseEntity<?> result = controller.getFolder(5L);

        assertEquals(200, result.getStatusCode().value());
        assertInstanceOf(FolderMetadata.class, result.getBody());
        FolderMetadata body = (FolderMetadata) result.getBody();
        assertEquals(5L, body.getId());
        assertEquals("docs", body.getName());
        assertEquals("/root/user/docs", body.getPath());
    }

    @Test
    void getFolder_RootFolder_ReturnsOkWithSessionUser() throws Exception {
        File rootFolder = new File("/root/testuser");
        when(userUtility.returnUserFolder()).thenReturn(rootFolder);
        when(userSession.getId()).thenReturn(1L);
        when(encodingUtility.decodeBase32StringNoPadding("testuser")).thenReturn("user:testuser");

        ResponseEntity<?> result = controller.getFolder(0L);

        assertEquals(200, result.getStatusCode().value());
        assertInstanceOf(FolderMetadata.class, result.getBody());
        FolderMetadata body = (FolderMetadata) result.getBody();
        assertEquals(0L, body.getId());
        assertEquals(1L, body.getUserid());
        assertEquals("testuser", body.getName());
        assertEquals(rootFolder.getPath(), body.getPath());
    }

    @Test
    void getFolder_RootFolder_DecodingFails_ReturnsInternalServerError() throws Exception {
        File rootFolder = new File("/root/testuser");
        when(userUtility.returnUserFolder()).thenReturn(rootFolder);
        when(userSession.getId()).thenReturn(1L);
        when(encodingUtility.decodeBase32StringNoPadding("testuser"))
                .thenThrow(new IllegalArgumentException("Not valid base32"));

        ResponseEntity<?> result = controller.getFolder(0L);

        assertEquals(500, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
        JSONErrorResponse body = (JSONErrorResponse) result.getBody();
        assertEquals(false, body.isSuccess());
    }

    @Test
    void getFolder_NotFound_ReturnsInternalServerError() throws Exception {
        when(informationRepository.getFolderMetadata(999L))
                .thenThrow(new FileNotFoundException("Not found"));

        ResponseEntity<?> result = controller.getFolder(999L);

        assertEquals(500, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
        JSONErrorResponse body = (JSONErrorResponse) result.getBody();
        assertEquals(false, body.isSuccess());
        assertEquals(FileNotFoundException.class.getName(), body.getException_type());
        assertEquals("Failed to get folder metadata for fileId: 999. Not found", body.getMessage());
    }
}
