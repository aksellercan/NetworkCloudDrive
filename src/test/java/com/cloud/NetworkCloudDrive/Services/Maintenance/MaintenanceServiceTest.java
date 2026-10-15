package com.cloud.NetworkCloudDrive.Services.Maintenance;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Domain.ScanResults;
import com.cloud.NetworkCloudDrive.Models.Domain.ThumbnailScanResults;
import com.cloud.NetworkCloudDrive.Models.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Jobs.MaintenanceJob;
import com.cloud.NetworkCloudDrive.Models.Response.ScanTaskResponse;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Properties.ThumbnailProperties;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.JobExecutorInterface;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceServiceTest {

    @Mock
    private FileUtility fileUtility;
    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private PathUtility pathUtility;
    @Mock
    private ThumbnailRepository thumbnailRepository;
    @Mock
    private ThumbnailProperties thumbnailProperties;
    @Mock
    private JobExecutorInterface jobExecutor;

    private MaintenanceService maintenanceService;

    private final UserDTO userDTO = new UserDTO(1L, "testuser", "test@example.com");

    @TempDir
    Path tempDir;
    private Path rootDir;
    private Path fileA;
    private Path subDir;

    private static final long FILE_ID = 42L;
    private static final long FOLDER_ID = 77L;

    @BeforeEach
    void setUp() {
        maintenanceService = new MaintenanceService(
                fileUtility, encodingUtility, sqLiteDAO, pathUtility,
                thumbnailRepository, thumbnailProperties, jobExecutor);
        rootDir = tempDir.resolve("user_root");
        fileA = rootDir.resolve("a.jpg");
        subDir = rootDir.resolve("sub");
    }

    private void prepareScanFixture() throws IOException {
        Files.createDirectories(rootDir);
        Files.writeString(fileA, "photo-bytes");
        Files.createDirectories(subDir);
    }

    /**
     * Shared stubs for a full directory scan of the fixture tree:
     * user_root/{a.jpg, sub/}. The parent directory name "user_root" is treated as
     * a user directory, so file/folder entries land in root folder id 0.
     */
    private void stubCommonScanBehaviour() throws Exception {
        lenient().when(fileUtility.getFileAndFolderPathsFromFolder(rootDir))
                .thenReturn(List.of(fileA, subDir));
        lenient().when(fileUtility.isIgnoredFile(anyString())).thenReturn(false);
        lenient().when(fileUtility.getMimeTypeFromExtensionUsingTikaCore(any(File.class)))
                .thenReturn("image/jpeg");
        lenient().when(encodingUtility.isEncodedStringUserDirectory("user_root")).thenReturn(true);
        lenient().when(encodingUtility.isBase32Decodable(anyString())).thenReturn(false);
        lenient().when(encodingUtility.encodeBase32FileName(FILE_ID, "a.jpg", 1L))
                .thenReturn("ENCODED_A.jpg");
        lenient().when(encodingUtility.encodeBase32FolderName(FOLDER_ID, "sub", 1L))
                .thenReturn("ENCODED_SUB");
        // Simulate Hibernate assigning generated ids on persist
        lenient().doAnswer(invocation -> {
            Object argument = invocation.getArgument(0);
            if (argument instanceof FileMetadata fileMetadata) fileMetadata.setId(FILE_ID);
            if (argument instanceof FolderMetadata folderMetadata) folderMetadata.setId(FOLDER_ID);
            return null;
        }).when(sqLiteDAO).persistObjects(any());
        lenient().when(sqLiteDAO.getIdPath(0L, 1L)).thenReturn("0");
        lenient().when(pathUtility.getBasePath()).thenReturn(tempDir);
    }

    private void stubSuccessfulThumbnailCreation() throws Exception {
        when(thumbnailProperties.isAllowedImageFormat("image/jpeg")).thenReturn(true);
        when(thumbnailRepository.createAndSaveThumbnail(
                any(Path.class), eq("ENCODED_A.jpg"), eq(FILE_ID), eq(userDTO)))
                .thenReturn(CompletableFuture.completedFuture(
                        new ThumbnailMetadata("ENCODED_A.jpg", 1L, "image/jpeg", 10L, FILE_ID, false)));
    }

    // ---------- queueScan ----------

    @Test
    void queueScan_QueuesJobAndReturnsTaskResponse() throws Exception {
        when(pathUtility.getFolderPath(5L)).thenReturn("user_root");
        when(pathUtility.getFullPath("user_root")).thenReturn(rootDir);

        ScanTaskResponse response = maintenanceService.queueScan(5L, ScanOptions.NORMAL);

        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobExecutor).queueJobs(jobCaptor.capture());
        Job queuedJob = jobCaptor.getValue();

        assertInstanceOf(MaintenanceJob.class, queuedJob);
        assertEquals(rootDir, ((MaintenanceJob) queuedJob).getStartingDirectory());
        assertEquals(ScanOptions.NORMAL, ((MaintenanceJob) queuedJob).getScanOptions());
        assertEquals(queuedJob.getId(), response.getJobId());
        assertEquals(queuedJob.getJobName(), response.getTaskName());
        assertEquals(JobStatus.QUEUED, response.getJobStatus());
    }

    @Test
    void queueScan_WhenFolderPathLookupFails_ThrowsWithoutQueueing() throws Exception {
        when(pathUtility.getFolderPath(5L)).thenThrow(new SQLException("database down"));

        assertThrows(SQLException.class, () -> maintenanceService.queueScan(5L, ScanOptions.NORMAL));

        verify(jobExecutor, never()).queueJobs(any());
    }

    // ---------- scanOptionsController / scanDirectory ----------

    @Test
    void scanOptionsController_NormalScansFilesAndFolders_ReturnsScanResults() throws Exception {
        prepareScanFixture();
        stubCommonScanBehaviour();
        stubSuccessfulThumbnailCreation();

        Object result = maintenanceService.scanOptionsController(userDTO, rootDir, 0L, ScanOptions.NORMAL);

        assertInstanceOf(ScanResults.class, result);
        ScanResults scanResults = (ScanResults) result;
        assertEquals(1, scanResults.getDiscoveredFiles());
        assertEquals(1, scanResults.getCreatedFiles());
        assertEquals(1, scanResults.getDiscoveredFolders());
        assertEquals(1, scanResults.getCreatedFolders());

        // Both entries were encoded and renamed on disk
        assertTrue(Files.exists(rootDir.resolve("ENCODED_A.jpg")));
        assertFalse(Files.exists(fileA));
        assertTrue(Files.exists(rootDir.resolve("ENCODED_SUB")));
        assertFalse(Files.exists(subDir));

        verify(thumbnailRepository).createAndSaveThumbnail(
                any(Path.class), eq("ENCODED_A.jpg"), eq(FILE_ID), eq(userDTO));
        verify(sqLiteDAO).saveFile(any(FileMetadata.class));
        verify(sqLiteDAO).saveFolder(any(FolderMetadata.class));
    }

    @Test
    void scanOptionsController_OnlyFiles_SkipsFolders() throws Exception {
        prepareScanFixture();
        stubCommonScanBehaviour();

        Object result = maintenanceService.scanOptionsController(userDTO, rootDir, 0L, ScanOptions.ONLY_FILES);

        assertInstanceOf(ScanResults.class, result);
        ScanResults scanResults = (ScanResults) result;
        assertEquals(1, scanResults.getDiscoveredFiles());
        assertEquals(1, scanResults.getCreatedFiles());
        assertEquals(0, scanResults.getDiscoveredFolders());
        assertEquals(0, scanResults.getCreatedFolders());

        // Folder was filtered out and left untouched
        assertTrue(Files.exists(subDir));
        assertFalse(Files.exists(rootDir.resolve("ENCODED_SUB")));
        // No thumbnails requested for this option
        verify(thumbnailRepository, never()).createAndSaveThumbnail(any(), anyString(), anyLong(), any());
    }

    @Test
    void scanOptionsController_DontCreateThumbnails_EncodesButSkipsThumbnails() throws Exception {
        prepareScanFixture();
        stubCommonScanBehaviour();

        Object result = maintenanceService.scanOptionsController(userDTO, rootDir, 0L, ScanOptions.DONT_CREATE_THUMBNAILS);

        assertInstanceOf(ScanResults.class, result);
        assertEquals(1, ((ScanResults) result).getCreatedFiles());
        assertTrue(Files.exists(rootDir.resolve("ENCODED_A.jpg")));
        verify(thumbnailRepository, never()).createAndSaveThumbnail(any(), anyString(), anyLong(), any());
        verifyNoInteractions(thumbnailProperties);
    }

    @Test
    void scanOptionsControllerConverter_DispatchesMaintenanceJob_ScanResults() throws Exception {
        prepareScanFixture();
        stubCommonScanBehaviour();
        MaintenanceJob job = new MaintenanceJob(rootDir, 0L, ScanOptions.NORMAL);
        job.setUserDTO(userDTO);

        Object result = maintenanceService.scanOptionsControllerConverter(job);

        assertInstanceOf(ScanResults.class, result);
        assertEquals(1, ((ScanResults) result).getCreatedFiles());
        assertEquals(1, ((ScanResults) result).getCreatedFolders());
    }

    @Test
    void scanDirectory_WhenListingFails_ReturnsFalse() throws Exception {
        when(fileUtility.getFileAndFolderPathsFromFolder(rootDir)).thenThrow(new IOException("disk error"));

        boolean result = maintenanceService.scanDirectory(userDTO, rootDir, path -> true, true, true);

        assertFalse(result);
    }

    @Test
    void scanDirectory_WhenAlreadyEncodedEntriesExist_SkipsWithoutRecreating() throws Exception {
        prepareScanFixture();
        stubCommonScanBehaviour();
        // A file already encoded AND present in the database is skipped
        when(encodingUtility.isBase32Decodable("a.jpg")).thenReturn(true);
        when(sqLiteDAO.fileMetadataByNameExists("a.jpg")).thenReturn(true);

        Object result = maintenanceService.scanOptionsController(userDTO, rootDir, 0L, ScanOptions.DONT_CREATE_THUMBNAILS);

        // The file was skipped: no new entry created, count unchanged
        assertEquals(0, ((ScanResults) result).getCreatedFiles());
        verify(sqLiteDAO, never()).saveFile(any(FileMetadata.class));
        assertTrue(Files.exists(fileA));
    }

    // ---------- thumbnail scanning ----------

    @Test
    void recursiveThumbnailScanInvoker_CreatesThumbnailsForMatchingFolders() throws Exception {
        FileMetadata file = new FileMetadata("ENCODED_A.jpg", 1L, 1L, "image/jpeg", 10L);
        file.setId(FILE_ID);
        when(sqLiteDAO.getIdPath(0L, 1L)).thenReturn("0/0");
        // Mutable list: invoker appends the root folder id when folderId <= 0
        when(sqLiteDAO.findAllStartsWithIdPathReturnsLongList("0/0", 1L))
                .thenReturn(new ArrayList<>(List.of(1L)));
        when(sqLiteDAO.findAllFilesWithoutThumbnailsInFolder(1L, 1L)).thenReturn(List.of(file));
        when(sqLiteDAO.getIdPath(1L, 1L)).thenReturn("0/1");
        when(pathUtility.resolvePathFromIdString(userDTO, "0/1")).thenReturn("/resolved");
        when(thumbnailProperties.isAllowedImageFormat("image/jpeg")).thenReturn(true);
        when(thumbnailRepository.createAndSaveThumbnail(
                eq(Path.of("/resolved", "ENCODED_A.jpg")), eq("ENCODED_A.jpg"), eq(FILE_ID), eq(userDTO)))
                .thenReturn(CompletableFuture.completedFuture(
                        new ThumbnailMetadata("ENCODED_A.jpg", 1L, "image/jpeg", 10L, FILE_ID, false)));

        ThumbnailScanResults results = maintenanceService.recursiveThumbnailScanInvoker(userDTO, 0L);

        assertEquals(1, results.getDiscoveredFiles());
        assertEquals(1, results.getCreatedThumbnails());
        assertEquals(0, results.getFailedThumbnails());
        assertTrue(file.isHasThumbnail());
        verify(sqLiteDAO).saveFile(file);
    }

    @Test
    void recursiveThumbnailScanInvoker_WhenFormatNotAllowed_CountsAsFailure() throws Exception {
        FileMetadata file = new FileMetadata("ENCODED_A.jpg", 1L, 1L, "application/pdf", 10L);
        file.setId(FILE_ID);
        when(sqLiteDAO.getIdPath(0L, 1L)).thenReturn("0/0");
        when(sqLiteDAO.findAllStartsWithIdPathReturnsLongList("0/0", 1L))
                .thenReturn(new ArrayList<>(List.of(1L)));
        when(sqLiteDAO.findAllFilesWithoutThumbnailsInFolder(1L, 1L)).thenReturn(List.of(file));
        when(sqLiteDAO.getIdPath(1L, 1L)).thenReturn("0/1");
        when(pathUtility.resolvePathFromIdString(userDTO, "0/1")).thenReturn("/resolved");
        when(thumbnailProperties.isAllowedImageFormat("application/pdf")).thenReturn(false);

        ThumbnailScanResults results = maintenanceService.recursiveThumbnailScanInvoker(userDTO, 0L);

        assertEquals(1, results.getDiscoveredFiles());
        assertEquals(0, results.getCreatedThumbnails());
        assertEquals(1, results.getFailedThumbnails());
        assertFalse(file.isHasThumbnail());
        verify(thumbnailRepository, never()).createAndSaveThumbnail(any(), anyString(), anyLong(), any());
        verify(sqLiteDAO).saveFile(file);
    }

    @Test
    void scanAndCreateThumbnails_SavesAllProcessedFiles() throws Exception {
        FileMetadata file = new FileMetadata("ENCODED_A.jpg", 1L, 1L, "image/jpeg", 10L);
        file.setId(FILE_ID);
        when(sqLiteDAO.findAllFilesWithoutThumbnailsInFolder(5L, 1L))
                .thenReturn(new ArrayList<>(List.of(file)));
        when(sqLiteDAO.getIdPath(1L, 1L)).thenReturn("0/1");
        when(pathUtility.resolvePathFromIdString(userDTO, "0/1")).thenReturn("/resolved");
        when(thumbnailProperties.isAllowedImageFormat("image/jpeg")).thenReturn(true);
        when(thumbnailRepository.createAndSaveThumbnail(
                eq(Path.of("/resolved", "ENCODED_A.jpg")), eq("ENCODED_A.jpg"), eq(FILE_ID), eq(userDTO)))
                .thenReturn(CompletableFuture.completedFuture(
                        new ThumbnailMetadata("ENCODED_A.jpg", 1L, "image/jpeg", 10L, FILE_ID, false)));

        maintenanceService.scanAndCreateThumbnails(5L, userDTO);

        ArgumentCaptor<List<FileMetadata>> savedCaptor = ArgumentCaptor.forClass(List.class);
        verify(sqLiteDAO).saveAllFiles(savedCaptor.capture());
        List<FileMetadata> saved = savedCaptor.getValue();
        assertEquals(1, saved.size());
        assertTrue(saved.get(0).isHasThumbnail());
    }

    @Test
    void scanAndCreateThumbnailsRecursive_WhenIndexBeyondSize_ReturnsTrueImmediately() {
        ThumbnailScanResults results = new ThumbnailScanResults();

        assertTrue(maintenanceService.scanAndCreateThumbnailsRecursive(userDTO, List.of(1L, 2L), 2, results));

        assertEquals(0, results.getDiscoveredFiles());
        verifyNoInteractions(sqLiteDAO);
    }
}
