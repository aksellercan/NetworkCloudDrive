package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Controllers.Filesystem.List.MarkedController;
import com.cloud.NetworkCloudDrive.Models.DTO.FileListItemDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.FolderListItemDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.FilterListEnum;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONObjectArrayResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarkedControllerTest {

    @Mock
    private FileSystemRepository fileSystemRepository;

    private MarkedController controller;

    @BeforeEach
    void setUp() {
        controller = new MarkedController(fileSystemRepository);
    }

    @Test
    void markedFilesystem_WithoutSortOrFilter_ReturnsGeneralMessage() {
        Map<String, List<?>> marked = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.collectAllMarked(null, null, null)).thenReturn(marked);

        ResponseEntity<Map<String, Map<String, List<?>>>> result =
                controller.markedFilesystem(null, null, null, 0, 0);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().containsKey("Marked Files and Folders"));
        assertSame(marked, result.getBody().get("Marked Files and Folders"));
        verify(fileSystemRepository).collectAllMarked(null, null, null);
    }

    @Test
    void markedFilesystem_FilesOnlyFilter_ReturnsFilesMessage() {
        Map<String, List<?>> marked = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.collectAllMarked(eq(SortListEnum.NEWEST), eq(FilterListEnum.FILES_ONLY), eq("query")))
                .thenReturn(marked);

        ResponseEntity<Map<String, Map<String, List<?>>>> result =
                controller.markedFilesystem(SortListEnum.NEWEST, FilterListEnum.FILES_ONLY, "query", 0, 0);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().containsKey("Marked Files"));
        assertSame(marked, result.getBody().get("Marked Files"));
        verify(fileSystemRepository).collectAllMarked(SortListEnum.NEWEST, FilterListEnum.FILES_ONLY, "query");
    }

    @Test
    void markedFilesystem_FoldersOnlyFilter_ReturnsFoldersMessage() {
        Map<String, List<?>> marked = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.collectAllMarked(eq(null), eq(FilterListEnum.FOLDERS_ONLY), eq(null)))
                .thenReturn(marked);

        ResponseEntity<Map<String, Map<String, List<?>>>> result =
                controller.markedFilesystem(null, FilterListEnum.FOLDERS_ONLY, null, 0, 0);

        assertEquals(200, result.getStatusCode().value());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().containsKey("Marked Folders"));
        assertSame(marked, result.getBody().get("Marked Folders"));
    }

    @Test
    void markedFilesystem_DelegatesWithSortOnly() {
        Map<String, List<?>> marked = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.collectAllMarked(SortListEnum.ALPHABETICAL, null, null)).thenReturn(marked);

        ResponseEntity<Map<String, Map<String, List<?>>>> result =
                controller.markedFilesystem(SortListEnum.ALPHABETICAL, null, null, 0, 0);

        assertEquals(200, result.getStatusCode().value());
        assertTrue(result.getBody().containsKey("Marked Files and Folders"));
    }

    @Test
    void listAllMarked_ReturnsOkWithRecentsResponse() {
        Map<String, List<?>> recents = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.collectAllRecents()).thenReturn(recents);

        ResponseEntity<?> result = controller.listAllMarked();

        assertEquals(200, result.getStatusCode().value());
        assertInstanceOf(JSONObjectArrayResponse.class, result.getBody());
        JSONObjectArrayResponse response = (JSONObjectArrayResponse) result.getBody();
        Object[] objects = (Object[]) response.getObject();
        assertEquals(1, objects.length);
        assertEquals(recents, objects[0]);
        verify(fileSystemRepository).collectAllRecents();
    }

    @Test
    void listAllMarked_RepositoryError_ReturnsBadRequest() {
        when(fileSystemRepository.collectAllRecents()).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> result = controller.listAllMarked();

        assertEquals(400, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
    }

    @Test
    void listMarked_ReturnsPagedResponseWithPageable() {
        Map<String, List<?>> paged = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.collectAllRecentsPageable(any(Pageable.class))).thenReturn(paged);

        ResponseEntity<?> result = controller.listMarked(1, 5);

        assertEquals(200, result.getStatusCode().value());
        assertInstanceOf(JSONObjectArrayResponse.class, result.getBody());
        JSONObjectArrayResponse response = (JSONObjectArrayResponse) result.getBody();
        Object[] objects = (Object[]) response.getObject();
        assertEquals(2, objects.length);
        assertEquals(paged, objects[0]);
        assertInstanceOf(Pageable.class, objects[1]);
        Pageable pageable = (Pageable) objects[1];
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        verify(fileSystemRepository).collectAllRecentsPageable(any(Pageable.class));
    }

    @Test
    void listMarkedFolders_ReturnsOkWithFoldersPayload() {
        FileMetadata file = new FileMetadata("enc_file", 1L, 1L, "text/plain", 100L);
        file.setId(10L);
        FolderMetadata folder = new FolderMetadata("enc_folder", "0/5");
        folder.setId(5L);
        when(fileSystemRepository.getRecentFilesPageable(any(Pageable.class)))
                .thenReturn(List.of(new FileListItemDTO(file)));
        doReturn(List.of(new FolderListItemDTO(folder)))
                .when(fileSystemRepository).getRecentFoldersPageable(any(Pageable.class));

        ResponseEntity<?> foldersResult = controller.listMarkedFolders(0, 10);
        ResponseEntity<?> filesResult = controller.listMarkedFiles(0, 10);

        assertEquals(200, foldersResult.getStatusCode().value());
        assertInstanceOf(JSONObjectArrayResponse.class, foldersResult.getBody());
        JSONObjectArrayResponse foldersResponse = (JSONObjectArrayResponse) foldersResult.getBody();
        Object[] foldersObjects = (Object[]) foldersResponse.getObject();
        @SuppressWarnings("unchecked")
        Map<String, List<?>> foldersPayload = (Map<String, List<?>>) foldersObjects[0];
        assertEquals(1, ((List<?>) foldersPayload.get("folders")).size());

        assertEquals(200, filesResult.getStatusCode().value());
        assertInstanceOf(JSONObjectArrayResponse.class, filesResult.getBody());
        JSONObjectArrayResponse filesResponse = (JSONObjectArrayResponse) filesResult.getBody();
        Object[] filesObjects = (Object[]) filesResponse.getObject();
        @SuppressWarnings("unchecked")
        Map<String, List<?>> filesPayload = (Map<String, List<?>>) filesObjects[0];
        assertEquals(1, ((List<?>) filesPayload.get("files")).size());

        verify(fileSystemRepository).getRecentFoldersPageable(any(Pageable.class));
        verify(fileSystemRepository).getRecentFilesPageable(any(Pageable.class));
    }

    @Test
    void listMarkedFolders_RepositoryError_ReturnsBadRequest() {
        doThrow(new RuntimeException("boom"))
                .when(fileSystemRepository).getRecentFoldersPageable(any(Pageable.class));

        ResponseEntity<?> result = controller.listMarkedFolders(0, 10);

        assertEquals(400, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
    }

    @Test
    void listMarkedFiles_RepositoryError_ReturnsBadRequest() {
        when(fileSystemRepository.getRecentFilesPageable(any(Pageable.class)))
                .thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> result = controller.listMarkedFiles(0, 10);

        assertEquals(400, result.getStatusCode().value());
        assertInstanceOf(JSONErrorResponse.class, result.getBody());
    }
}
