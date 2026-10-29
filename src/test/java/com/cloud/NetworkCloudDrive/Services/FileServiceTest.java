package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.DTO.JobDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Jobs.ThumbnailJob;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Properties.ThumbnailProperties;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Tasks.SequentialJobExecutor;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private UserSession userSession;
    @Mock
    private FileUtility fileUtility;
    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private PathUtility pathUtility;
    @Mock
    private ThumbnailProperties thumbnailProperties;
    @Mock
    private ThumbnailRepository thumbnailRepository;
    @Mock
    private SequentialJobExecutor sequentialJobExecutor;

    private FileService fileService;

    @TempDir
    Path tempDir;

    private static String encode(long id, String name, long userId) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((id + ":" + name + ":" + userId).getBytes());
    }

    @BeforeEach
    void setUp() {
        fileService = new FileService(sqLiteDAO, sequentialJobExecutor, userSession, fileUtility,
                encodingUtility, pathUtility, thumbnailProperties);
    }

    @Test
    void storeFile_CreatesDirectoriesAndWritesFile() throws Exception {
        String fileName = "encoded_test_file.abc";
        String parentPath = tempDir.resolve("user_folder_encoded").toString();
        String content = "test file content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        Path userDirectory = Path.of(parentPath);
        Path filePath = userDirectory.resolve(fileName);

        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(pathUtility.isFilenameAllowed(fileName)).thenReturn(true);

        Path result = fileService.storeFile(inputStream, fileName, parentPath).get();

        assertTrue(Files.exists(filePath));
        assertEquals(content, Files.readString(filePath));
        assertEquals(tempDir.relativize(filePath), result);
    }

    @Test
    void storeFile_WhenPathNotAllowed_ThrowsSecurityException() throws Exception {
        String fileName = "test.txt";
        String parentPath = tempDir.resolve("user_folder").toString();

        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> fileService.storeFile(new ByteArrayInputStream(new byte[0]), fileName, parentPath));
    }

    @Test
    void storeFile_WhenFilenameNotAllowed_ThrowsIOException() throws Exception {
        String fileName = "../evil.txt";
        String parentPath = tempDir.resolve("user_folder").toString();

        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(pathUtility.isFilenameAllowed(fileName)).thenReturn(false);

        assertThrows(IOException.class,
                () -> fileService.storeFile(new ByteArrayInputStream(new byte[0]), fileName, parentPath));
    }

    @Test
    void storeFile_IsAnnotatedWithAsync() throws Exception {
        java.lang.reflect.Method method = FileService.class.getMethod(
                "storeFile", InputStream.class, String.class, String.class);
        assertNotNull(method.getAnnotation(Async.class));
    }

    @Test
    void storeFile_ReturnsCompletableFuture() throws Exception {
        Path parentDirectory = Files.createDirectories(tempDir.resolve("dir"));

        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(pathUtility.isFilenameAllowed(anyString())).thenReturn(true);

        CompletableFuture<Path> future = fileService.storeFile(
                new ByteArrayInputStream("data".getBytes()), "file.txt", parentDirectory.toString());

        assertNotNull(future);
        assertTrue(future.isDone(), "completedFuture should already be done");
    }

    @Test
    void getFile_WhenFileExists_ReturnsResource() throws Exception {
        String fileName = "encoded_file.xyz";
        String path = tempDir.resolve("user_folder").toString();
        String content = "test data";

        Path fullPath = Path.of(path).resolve(fileName);
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, content);

        when(encodingUtility.unifiedEncodeBase64(any(FileMetadata.class))).thenReturn(fileName);
        when(pathUtility.getBasePath()).thenReturn(Path.of("/unrelated/base"));

        FileMetadata fileMetadata = new FileMetadata(fileName, 0L, 1L, "text/plain", 9L);
        fileMetadata.setId(10L);
        fileMetadata.setKey("KEY");

        Resource resource = fileService.getFile(fileMetadata, path).get();

        assertNotNull(resource);
        assertTrue(resource.exists());
        try (var is = resource.getInputStream()) {
            assertEquals(content, new String(is.readAllBytes()));
        }
        verify(sqLiteDAO).saveFile(fileMetadata);
    }

    @Test
    void getFile_WhenFileDoesNotExist_ThrowsException() throws Exception {
        String fileName = "nonexistent.txt";
        String path = tempDir.resolve("user_folder").toString();

        when(encodingUtility.unifiedEncodeBase64(any(FileMetadata.class))).thenReturn(fileName);
        when(pathUtility.getBasePath()).thenReturn(Path.of("/unrelated/base"));

        FileMetadata fileMetadata = new FileMetadata(fileName, 0L, 1L, "text/plain", 0L);

        assertThrows(Exception.class,
                () -> fileService.getFile(fileMetadata, path));
    }

    @Test
    void getFile_WhenFilePathUnderBase_ThrowsSecurityException() throws Exception {
        String fileName = "test.txt";
        String path = Files.createDirectories(tempDir.resolve("subdir")).resolve(fileName).toString();

        when(encodingUtility.unifiedEncodeBase64(any(FileMetadata.class))).thenReturn(fileName);
        when(pathUtility.getBasePath()).thenReturn(tempDir);

        FileMetadata fileMetadata = new FileMetadata(fileName, 0L, 1L, "text/plain", 4L);

        assertThrows(SecurityException.class,
                () -> fileService.getFile(fileMetadata, path));
    }

    @Test
    void getFile_IsAnnotatedWithAsync() throws Exception {
        java.lang.reflect.Method method = FileService.class.getMethod(
                "getFile", FileMetadata.class, String.class);
        assertNotNull(method.getAnnotation(Async.class));
    }

    @Test
    void getFile_ReturnsCompletableFuture() throws Exception {
        String fileName = "existing.txt";
        Path dir = Files.createDirectories(tempDir.resolve("some_dir"));
        Files.writeString(dir.resolve(fileName), "content");

        when(encodingUtility.unifiedEncodeBase64(any(FileMetadata.class))).thenReturn(fileName);
        when(pathUtility.getBasePath()).thenReturn(Path.of("/unrelated/base"));

        FileMetadata fileMetadata = new FileMetadata(fileName, 0L, 1L, "text/plain", 7L);

        CompletableFuture<Resource> future = fileService.getFile(fileMetadata, dir.toString());

        assertNotNull(future);
        assertTrue(future.isDone(), "completedFuture should already be done");
    }

    @Test
    void uploadFiles_SingleFile_ReturnsUploadedCount() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain", "hello world".getBytes());
        MockMultipartFile[] files = new MockMultipartFile[]{file};
        String folderPath = Files.createDirectories(tempDir.resolve("user").resolve("folder")).toString();

        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of());
        when(pathUtility.isFilenameAllowed(anyString())).thenReturn(true);
        when(encodingUtility.generateKey()).thenReturn("ABC");
        when(encodingUtility.unifiedEncodeBase64(anyLong(), anyString(), anyLong())).thenReturn(encode(1, "test.txt", 1));
        when(userSession.getId()).thenReturn(1L);
        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(thumbnailProperties.isAllowedImageFormat("text/plain")).thenReturn(false);

        doAnswer(invocation -> {
            FileMetadata m = invocation.getArgument(0);
            m.setId(1L);
            return null;
        }).when(sqLiteDAO).persistObjects(any());
        doAnswer(invocation -> invocation.getArgument(0)).when(sqLiteDAO).saveFile(any());

        Map<String, ?> result = fileService.uploadFiles(files, folderPath, 1L);

        assertEquals(1, result.get("uploaded_file_count"));
        assertTrue(result.containsKey("files"));
        verify(sequentialJobExecutor, never()).queueJobs(any());
    }

    @Test
    void uploadFiles_WhenDuplicate_ThrowsException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain", "data".getBytes());
        MockMultipartFile[] files = new MockMultipartFile[]{file};
        Path existingFile = tempDir.resolve("existing_encoded_file");
        String folderPath = Files.createDirectories(tempDir.resolve("user").resolve("folder")).toString();

        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of(existingFile));
        when(pathUtility.isFilenameAllowed("test.txt")).thenReturn(true);
        when(fileUtility.checkDuplicate(anyList(), eq("test.txt"))).thenReturn(true);

        assertThrows(java.nio.file.FileAlreadyExistsException.class,
                () -> fileService.uploadFiles(files, folderPath, 1L));
    }

    @Test
    void uploadFiles_WhenInvalidFilename_SkipsFile() throws Exception {
        MockMultipartFile valid = new MockMultipartFile("files", "valid.txt", "text/plain", "ok".getBytes());
        MockMultipartFile invalid = new MockMultipartFile("files", "../bad.txt", "text/plain", "nope".getBytes());
        MockMultipartFile[] files = new MockMultipartFile[]{valid, invalid};
        String folderPath = Files.createDirectories(tempDir.resolve("user").resolve("folder")).toString();

        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of());
        when(pathUtility.isFilenameAllowed("valid.txt")).thenReturn(true);
        when(pathUtility.isFilenameAllowed("../bad.txt")).thenReturn(false);
        when(pathUtility.isFilenameAllowed(encode(1, "valid.txt", 1))).thenReturn(true);
        when(encodingUtility.generateKey()).thenReturn("ABC");
        when(encodingUtility.unifiedEncodeBase64(anyLong(), anyString(), anyLong())).thenReturn(encode(1, "valid.txt", 1));
        when(userSession.getId()).thenReturn(1L);
        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(thumbnailProperties.isAllowedImageFormat("text/plain")).thenReturn(false);

        doAnswer(invocation -> {
            FileMetadata m = invocation.getArgument(0);
            m.setId(1L);
            return null;
        }).when(sqLiteDAO).persistObjects(any());
        doAnswer(invocation -> invocation.getArgument(0)).when(sqLiteDAO).saveFile(any());

        Map<String, ?> result = fileService.uploadFiles(files, folderPath, 1L);

        assertEquals(1, result.get("uploaded_file_count"));
        assertTrue(result.containsKey("files"));
    }

    @Test
    void uploadFiles_AllFilesSkipped_ThrowsException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "../evil.txt", "text/plain", "data".getBytes());
        MockMultipartFile[] files = new MockMultipartFile[]{file};
        String folderPath = tempDir.resolve("user").resolve("folder").toString();

        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of());
        when(pathUtility.isFilenameAllowed("../evil.txt")).thenReturn(false);

        assertThrows(java.nio.file.FileAlreadyExistsException.class,
                () -> fileService.uploadFiles(files, folderPath, 1L));
    }

    @Test
    void createFolder_Success_ReturnsFolderMetadata() throws Exception {
        String folderName = "myfolder";
        long folderId = 5L;

        Path parentDir = Files.createDirectories(tempDir.resolve("user").resolve("folder_5"));

        when(pathUtility.getFolderPath(folderId)).thenReturn("user/folder_5");
        when(pathUtility.getFullPathToString("user/folder_5")).thenReturn(parentDir.toString());
        when(pathUtility.isFilenameAllowed(folderName)).thenReturn(true);

        doAnswer(invocation -> {
            FolderMetadata m = invocation.getArgument(0);
            m.setId(10L);
            return null;
        }).when(sqLiteDAO).persistObjects(any(FolderMetadata.class));

        when(encodingUtility.generateKey()).thenReturn("ABC");
        when(encodingUtility.unifiedEncodeBase64(10L, "ABC", 1L)).thenReturn(encode(10, "myfolder", 1));
        when(userSession.getId()).thenReturn(1L);
        when(fileUtility.checkIfFileExistsDecodeNames("user/folder_5", folderName)).thenReturn(false);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);

        FolderMetadata saved = new FolderMetadata(encode(10, "myfolder", 1), "0/5/10");
        saved.setId(10L);
        saved.setUserid(1L);
        when(sqLiteDAO.saveFolder(any(FolderMetadata.class))).thenReturn(saved);

        FolderMetadata result = fileService.createFolder(folderName, folderId);

        assertNotNull(result);
        assertEquals(encode(10, "myfolder", 1), result.getName());
        assertEquals(1L, result.getUserid());
        verify(sqLiteDAO).persistObjects(any(FolderMetadata.class));
        verify(sqLiteDAO).saveFolder(any(FolderMetadata.class));
        assertTrue(Files.exists(parentDir.resolve(encode(10, "myfolder", 1))));
    }

    @Test
    void createFolder_WhenDuplicate_ThrowsException() throws Exception {
        String folderName = "existing";

        when(pathUtility.getFolderPath(5L)).thenReturn("user/folder");
        when(pathUtility.getFullPathToString("user/folder")).thenReturn(tempDir.resolve("user/folder").toString());
        when(pathUtility.isFilenameAllowed(folderName)).thenReturn(true);

        doAnswer(invocation -> {
            FolderMetadata m = invocation.getArgument(0);
            m.setId(10L);
            return null;
        }).when(sqLiteDAO).persistObjects(any(FolderMetadata.class));

        when(encodingUtility.generateKey()).thenReturn("ABC");
        when(encodingUtility.unifiedEncodeBase64(10L, "ABC", 1L)).thenReturn(encode(10, "existing", 1));
        when(userSession.getId()).thenReturn(1L);
        when(fileUtility.checkIfFileExistsDecodeNames("user/folder", folderName)).thenReturn(true);

        assertThrows(java.nio.file.FileAlreadyExistsException.class,
                () -> fileService.createFolder(folderName, 5L));
    }

    @Test
    void createFolder_WhenInvalidName_ThrowsException() throws Exception {
        when(pathUtility.isFilenameAllowed("../invalid")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> fileService.createFolder("../invalid", 5L));
    }

    @Test
    void markFile_MarkTrue_SetsMarkedAndSaves() throws Exception {
        FileMetadata file = new FileMetadata("enc.txt", 1L, 1L, "text/plain", 10L);
        file.setId(10L);

        when(userSession.getId()).thenReturn(1L);
        when(sqLiteDAO.queryFileMetadata(10L, 1L)).thenReturn(file);
        when(sqLiteDAO.saveFile(any(FileMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fileService.markFile(true, 10L);

        assertTrue(file.isMarked());
        verify(sqLiteDAO).queryFileMetadata(10L, 1L);
        verify(sqLiteDAO).saveFile(file);
    }

    @Test
    void markFile_MarkFalse_ClearsMarkedAndSaves() throws Exception {
        FileMetadata file = new FileMetadata("enc.txt", 1L, 1L, "text/plain", 10L);
        file.setId(10L);
        file.setMarked(true);

        when(userSession.getId()).thenReturn(1L);
        when(sqLiteDAO.queryFileMetadata(10L, 1L)).thenReturn(file);
        when(sqLiteDAO.saveFile(any(FileMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fileService.markFile(false, 10L);

        assertFalse(file.isMarked());
        verify(sqLiteDAO).saveFile(file);
    }

    @Test
    void markFolder_MarkTrue_SetsMarkedAndSaves() throws Exception {
        FolderMetadata folder = new FolderMetadata("enc_folder", "0/5");
        folder.setId(5L);

        when(userSession.getId()).thenReturn(1L);
        when(sqLiteDAO.queryFolderMetadata(5L, 1L)).thenReturn(folder);
        when(sqLiteDAO.saveFolder(any(FolderMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fileService.markFolder(true, 5L);

        assertTrue(folder.isMarked());
        verify(sqLiteDAO).queryFolderMetadata(5L, 1L);
        verify(sqLiteDAO).saveFolder(folder);
    }

    @Test
    void markFolder_MarkFalse_ClearsMarkedAndSaves() throws Exception {
        FolderMetadata folder = new FolderMetadata("enc_folder", "0/5");
        folder.setId(5L);
        folder.setMarked(true);

        when(userSession.getId()).thenReturn(1L);
        when(sqLiteDAO.queryFolderMetadata(5L, 1L)).thenReturn(folder);
        when(sqLiteDAO.saveFolder(any(FolderMetadata.class))).thenAnswer(invocation -> invocation.getArgument(0));

        fileService.markFolder(false, 5L);

        assertFalse(folder.isMarked());
        verify(sqLiteDAO).saveFolder(folder);
    }

    @Test
    void uploadFiles_WhenAllowedImageFormat_QueuesThumbnailJob() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "photo.png", "image/png", "png bytes".getBytes());
        MockMultipartFile[] files = new MockMultipartFile[]{file};
        String encodedFileName = encode(1, "photo.png", 1);
        String folderPath = Files.createDirectories(tempDir.resolve("user").resolve("folder")).toString();

        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of());
        when(pathUtility.isFilenameAllowed(anyString())).thenReturn(true);
        when(encodingUtility.generateKey()).thenReturn("ABC");
        when(encodingUtility.unifiedEncodeBase64(anyLong(), anyString(), anyLong())).thenReturn(encodedFileName);
        when(userSession.getId()).thenReturn(1L);
        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(thumbnailProperties.isAllowedImageFormat("image/png")).thenReturn(true);

        doAnswer(invocation -> {
            FileMetadata m = invocation.getArgument(0);
            m.setId(1L);
            return null;
        }).when(sqLiteDAO).persistObjects(any());
        doAnswer(invocation -> invocation.getArgument(0)).when(sqLiteDAO).saveFile(any());

        Map<String, ?> result = fileService.uploadFiles(files, folderPath, 1L);

        assertEquals(1, result.get("uploaded_file_count"));
        assertTrue(result.containsKey("created_jobs"));
        List<JobDTO> createdJobs = (List<JobDTO>) result.get("created_jobs");
        assertEquals(1, createdJobs.size());
        assertNotNull(createdJobs.get(0).getUUID());
        verify(sequentialJobExecutor).queueJobs(any(ThumbnailJob.class));
    }

    @Test
    void uploadFiles_WhenDisallowedFormat_CreatesNoJobs() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "doc.pdf", "application/pdf", "pdf bytes".getBytes());
        MockMultipartFile[] files = new MockMultipartFile[]{file};
        String encodedFileName = encode(1, "doc.pdf", 1);
        String folderPath = Files.createDirectories(tempDir.resolve("user").resolve("folder")).toString();

        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of());
        when(pathUtility.isFilenameAllowed(anyString())).thenReturn(true);
        when(encodingUtility.generateKey()).thenReturn("ABC");
        when(encodingUtility.unifiedEncodeBase64(anyLong(), anyString(), anyLong())).thenReturn(encodedFileName);
        when(userSession.getId()).thenReturn(1L);
        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(thumbnailProperties.isAllowedImageFormat("application/pdf")).thenReturn(false);

        doAnswer(invocation -> {
            FileMetadata m = invocation.getArgument(0);
            m.setId(1L);
            return null;
        }).when(sqLiteDAO).persistObjects(any());
        doAnswer(invocation -> invocation.getArgument(0)).when(sqLiteDAO).saveFile(any());

        Map<String, ?> result = fileService.uploadFiles(files, folderPath, 1L);

        assertEquals(1, result.get("uploaded_file_count"));
        assertFalse(result.containsKey("created_jobs"));
        verifyNoInteractions(sequentialJobExecutor);
    }
}
