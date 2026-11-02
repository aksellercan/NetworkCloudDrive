package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Controllers.Filesystem.List.ListController;
import com.cloud.NetworkCloudDrive.Models.Enum.FilterListEnum;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListControllerTest {

    @Mock
    private FileSystemRepository fileSystemRepository;

    private ListController controller;

    @BeforeEach
    void setUp() {
        controller = new ListController(fileSystemRepository);
    }

    @Test
    void listFilesInclusive_WithoutSortOrFilter_ReturnsOk() throws Exception {
        Map<String, List<?>> expected = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.listFilesV2(0L, null, null, null)).thenReturn(expected);

        ResponseEntity<?> result = controller.listFilesInclusive(0L, null, null, null);

        assertEquals(200, result.getStatusCode().value());
        assertSame(expected, result.getBody());
        verify(fileSystemRepository).listFilesV2(0L, null, null, null);
    }

    @Test
    void listFilesInclusive_WithSort_DelegatesToRepository() throws Exception {
        Map<String, List<?>> expected = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.listFilesV2(5L, SortListEnum.ALPHABETICAL, null, null)).thenReturn(expected);

        ResponseEntity<?> result = controller.listFilesInclusive(5L, SortListEnum.ALPHABETICAL, null, null);

        assertEquals(200, result.getStatusCode().value());
        assertSame(expected, result.getBody());
        verify(fileSystemRepository).listFilesV2(5L, SortListEnum.ALPHABETICAL, null, null);
    }

    @Test
    void listFilesInclusive_WithFilterAndQuery_DelegatesToRepository() throws Exception {
        Map<String, List<?>> expected = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.listFilesV2(2L, null, FilterListEnum.KEYWORD, "query")).thenReturn(expected);

        ResponseEntity<?> result = controller.listFilesInclusive(2L, null, FilterListEnum.KEYWORD, "query");

        assertEquals(200, result.getStatusCode().value());
        assertSame(expected, result.getBody());
        verify(fileSystemRepository).listFilesV2(2L, null, FilterListEnum.KEYWORD, "query");
    }

    @Test
    void listFilesInclusive_RepositoryError_PropagatesException() throws Exception {
        when(fileSystemRepository.listFilesV2(0L, null, null, null))
                .thenThrow(new java.io.IOException("fs error"));

        org.junit.jupiter.api.Assertions.assertThrows(java.io.IOException.class,
                () -> controller.listFilesInclusive(0L, null, null, null));
    }
}
