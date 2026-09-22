package com.cloud.NetworkCloudDrive.Controllers.Filesystem.List;

import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONObjectArrayResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "filesystem/marked")
public class MarkedController {
    private final Logger logger = LoggerFactory.getLogger(com.cloud.NetworkCloudDrive.Controllers.Filesystem.List.RecentsController.class);
    private final FileSystemRepository fileSystemRepository;

    public MarkedController(FileSystemRepository fileSystemRepository) {
        this.fileSystemRepository = fileSystemRepository;
    }

    @GetMapping(version = "1.0")
    public @ResponseBody ResponseEntity<?> listAllMarked() {
        try {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONObjectArrayResponse(
                            new Object[]{
                                    fileSystemRepository.collectAllRecents()
                            }, "Recent Files and Folders"));
        } catch (Exception e) {
            logger.error("Failed to list recents, reason: {}!", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(e, "Failed to list recents, reason: %s!", e.getMessage())
            );
        }
    }

    @GetMapping(version = "2.0")
    public @ResponseBody ResponseEntity<?> listMarked(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok()
                    .body(new JSONObjectArrayResponse(new Object[]{
                            fileSystemRepository.collectAllRecentsPageable(pageable),
                            pageable},
                            "Paged files and folders list"
                    ));
        } catch (Exception e) {
            logger.error("Failed to list recents, page: {}, size: {}, reason: {}!", page, size, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(e, "Failed to list recents, page: %d, size: %d, reason: %s!", page, size, e.getMessage())
            );
        }
    }

    @GetMapping(value = "folder", version = "2.0")
    public @ResponseBody ResponseEntity<?> listMarkedFolders(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONObjectArrayResponse(new Object[]{
                            Map.of("folders", fileSystemRepository.getRecentFoldersPageable(pageable)),
                            pageable}, "Paged folders list"
                    ));
        } catch (Exception e) {
            logger.error("Failed to list recent folders, page: {}, size: {}, reason: {}!", page, size, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(e, "Failed to list recent folders, page: %d, size: %d, reason: %s!", page, size, e.getMessage())
            );
        }
    }

    @GetMapping(value = "file", version = "2.0")
    public @ResponseBody ResponseEntity<?> listMarkedFiles(int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONObjectArrayResponse(new Object[]{
                            Map.of("files", fileSystemRepository.getRecentFilesPageable(pageable)),
                            pageable}, "Paged files list"
                    ));
        } catch (Exception e) {
            logger.error("Failed to list recent files, page: {}, size: {}, reason: {}!", page, size, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(e, "Failed to list recent files, page: %d, size: %d, reason: %s!", page, size, e.getMessage())
            );
        }
    }
}
