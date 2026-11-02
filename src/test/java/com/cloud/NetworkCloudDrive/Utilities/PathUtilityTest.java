package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PathUtilityTest {

    @Mock
    private UserUtility userUtility;
    @Mock
    private FileStorageProperties fileStorageProperties;
    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private UserSession userSession;
    @Mock
    private EncodingUtility encodingUtility;

    private PathUtility pathUtility;

    @BeforeEach
    void setUp() {
        pathUtility = new PathUtility(userUtility, fileStorageProperties, sqLiteDAO, userSession, encodingUtility);
    }

    @Test
    void isFilenameAllowed_withValidName_ReturnsTrue() {
        assertTrue(pathUtility.isFilenameAllowed("hello.txt"));
    }

    @Test
    void isFilenameAllowed_withValidNameNoExtension_ReturnsTrue() {
        assertTrue(pathUtility.isFilenameAllowed("readme"));
    }

    @Test
    void isFilenameAllowed_withValidComplexName_ReturnsTrue() {
        assertTrue(pathUtility.isFilenameAllowed("my-file.name_v2.ext"));
    }

    @Test
    void isFilenameAllowed_withNull_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed(null));
    }

    @Test
    void isFilenameAllowed_withEmptyString_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed(""));
    }

    @Test
    void isFilenameAllowed_withPathTraversal_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed("../hello.txt"));
    }

    @Test
    void isFilenameAllowed_withForwardSlash_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed("dir/file.txt"));
    }

    @Test
    void isFilenameAllowed_withBackslash_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed("dir\\file.txt"));
    }

    @Test
    void isFilenameAllowed_withLeadingDot_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed(".hidden"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"..", "..file.txt", "a/../b.txt", ".config.swp"})
    void isFilenameAllowed_withVariousInvalidNames_ReturnsFalse(String invalid) {
        assertFalse(pathUtility.isFilenameAllowed(invalid));
    }

    @ParameterizedTest
    @ValueSource(strings = {".gitignore", ".env", ".recycleBin", ".hidden", ".config"})
    void isFilenameAllowed_dotfiles_ReturnsFalse(String dotfile) {
        assertFalse(pathUtility.isFilenameAllowed(dotfile));
    }

    @Test
    void getBasePath_ReturnsPathFromProperties() {
        when(fileStorageProperties.getBasePath()).thenReturn("/root");
        Path result = pathUtility.getBasePath();
        assertEquals(Path.of("/root"), result);
    }

    @Test
    void getBasePathToString_ReturnsStringFromProperties() {
        when(fileStorageProperties.getBasePath()).thenReturn("/root");
        assertEquals("/root", pathUtility.getBasePathToString());
    }

    @Test
    void getFullPathToString_ReturnsFullPath() {
        when(fileStorageProperties.getFullPath("some/path")).thenReturn("/root/some/path");
        assertEquals("/root/some/path", pathUtility.getFullPathToString("some/path"));
    }

    @Test
    void getFullPath_ReturnsPath() {
        when(fileStorageProperties.getFullPath("some/path")).thenReturn("/root/some/path");
        assertEquals(Path.of("/root/some/path"), pathUtility.getFullPath("some/path"));
    }

    @Test
    void isPathAllowed_WhenPathUnderUserFolder_ReturnsTrue() throws Exception {
        Path userFolder = Path.of("/base/user_enc");
        when(userUtility.returnUserFolderasPath()).thenReturn(userFolder);

        Path allowedPath = Path.of("/base/user_enc/subdir/file.txt");
        boolean result = pathUtility.isPathAllowed(allowedPath);

        assertTrue(result);
    }

    @Test
    void isPathAllowed_WhenPathOutsideUserFolder_ReturnsFalse() throws Exception {
        when(userUtility.returnUserFolderasPath()).thenReturn(Path.of("/base/user_enc"));

        Path outsidePath = Path.of("/other/dir/file.txt");
        boolean result = pathUtility.isPathAllowed(outsidePath);

        assertFalse(result);
    }

    @Test
    void isPathAllowed_WhenPathIsParentOfUserFolder_ReturnsFalse() throws Exception {
        when(userUtility.returnUserFolderasPath()).thenReturn(Path.of("/base/user_enc"));

        Path outsidePath = Path.of("/base/other/file.txt");
        boolean result = pathUtility.isPathAllowed(outsidePath);

        assertFalse(result);
    }

    @Test
    void getFolderPath_WhenFolderIdIsZero_ReturnsEncodedUserFolder() throws Exception {
        when(userSession.getId()).thenReturn(1L);
        when(userSession.getName()).thenReturn("testuser");
        when(userSession.getMail()).thenReturn("test@example.com");
        when(encodingUtility.encodeBase32UserFolderName(1L, "testuser", "test@example.com")).thenReturn("user_encoded");

        String result = pathUtility.getFolderPath(0L);

        assertEquals("user_encoded", result);
    }

    @Test
    void returnParentFolderPathFromFolderID_ReturnsParentPath() throws Exception {
        com.cloud.NetworkCloudDrive.Models.FolderMetadata folder =
                new com.cloud.NetworkCloudDrive.Models.FolderMetadata("folder", "0/5/3");
        folder.setId(3L);
        folder.setParentFolderId(0L);
        when(sqLiteDAO.queryFolderMetadata(3L, 1L)).thenReturn(folder);
        when(userSession.getId()).thenReturn(1L);

        com.cloud.NetworkCloudDrive.Models.FolderMetadata parentFolder =
                new com.cloud.NetworkCloudDrive.Models.FolderMetadata("parent", "0/5");
        parentFolder.setId(5L);
        parentFolder.setParentFolderId(0L);
        when(sqLiteDAO.queryFolderMetadata(5L, 1L)).thenReturn(parentFolder);
        when(encodingUtility.unifiedEncodeBase64(parentFolder)).thenReturn("parent");
        when(userUtility.returnUserFolder()).thenReturn(new File("user_enc"));

        String result = pathUtility.returnParentFolderPathFromFolderID(3L);

        assertEquals("user_enc" + File.separator + "parent", result);
    }

    @Test
    void resolvePathFromIdString_withStringIdPath_ReturnsSystemPath() throws Exception {
        when(userSession.getId()).thenReturn(1L);
        when(userSession.getName()).thenReturn("testuser");
        when(userSession.getMail()).thenReturn("test@example.com");
        when(encodingUtility.encodeBase32UserFolderName(1L, "testuser", "test@example.com")).thenReturn("user_enc");

        com.cloud.NetworkCloudDrive.Models.FolderMetadata folder1 =
                new com.cloud.NetworkCloudDrive.Models.FolderMetadata("folder1", "0/1");
        folder1.setId(1L);
        com.cloud.NetworkCloudDrive.Models.FolderMetadata folder2 =
                new com.cloud.NetworkCloudDrive.Models.FolderMetadata("folder2", "0/1/2");
        folder2.setId(2L);

        when(sqLiteDAO.findAllByIdInSQLFolderMetadata(List.of(0L, 1L, 2L), 1L)).thenReturn(List.of(folder1, folder2));

        String result = pathUtility.resolvePathFromIdString("0/1/2");

        assertEquals("user_enc" + File.separator + "folder1" + File.separator + "folder2", result);
    }

    @Test
    void resolvePathFromIdString_withUserDTOAndStringIdPath_ReturnsSystemPath() throws Exception {
        com.cloud.NetworkCloudDrive.Models.DTO.UserDTO userDTO =
                new com.cloud.NetworkCloudDrive.Models.DTO.UserDTO(5L, "alice", "alice@example.com");
        when(encodingUtility.encodeBase32UserFolderName(5L, "alice", "alice@example.com")).thenReturn("alice_enc");

        com.cloud.NetworkCloudDrive.Models.FolderMetadata folder1 =
                new com.cloud.NetworkCloudDrive.Models.FolderMetadata("docs", "0/10");
        folder1.setId(10L);

        when(sqLiteDAO.findAllByIdInSQLFolderMetadata(List.of(0L, 10L), 5L)).thenReturn(List.of(folder1));

        String result = pathUtility.resolvePathFromIdString(userDTO, "0/10");

        assertEquals("alice_enc" + File.separator + "docs", result);
    }

    @Test
    void getFolderPath_whenFolderIdGreaterThanZero_ReturnsResolvedPath() throws Exception {
        when(userSession.getId()).thenReturn(2L);

        com.cloud.NetworkCloudDrive.Models.FolderMetadata folder =
                new com.cloud.NetworkCloudDrive.Models.FolderMetadata("work", "0/7");
        folder.setId(7L);
        folder.setParentFolderId(0L);
        when(sqLiteDAO.queryFolderMetadata(7L, 2L)).thenReturn(folder);
        when(encodingUtility.unifiedEncodeBase64(folder)).thenReturn("encoded_7");
        when(userUtility.returnUserFolder()).thenReturn(new File("bob_enc"));

        String result = pathUtility.getFolderPath(7L);

        assertEquals("bob_enc" + File.separator + "encoded_7", result);
    }

    @Test
    void getFolderPath_whenFolderIdIsZero_ReturnsEncodedUserFolderWithoutDb() throws Exception {
        when(userSession.getId()).thenReturn(1L);
        when(userSession.getName()).thenReturn("testuser");
        when(userSession.getMail()).thenReturn("test@example.com");
        when(encodingUtility.encodeBase32UserFolderName(1L, "testuser", "test@example.com")).thenReturn("user_encoded");

        String result = pathUtility.getFolderPath(0L);

        assertEquals("user_encoded", result);
        verifyNoInteractions(sqLiteDAO);
    }
}
