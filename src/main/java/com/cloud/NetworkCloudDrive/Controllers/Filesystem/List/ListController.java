package com.cloud.NetworkCloudDrive.Controllers.Filesystem.List;

import com.cloud.NetworkCloudDrive.Models.Enum.FilterListEnum;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping(value = "filesystem/list")
public class ListController {
    private final FileSystemRepository fileSystemRepository;
    private final Logger logger = LoggerFactory.getLogger(ListController.class);

    public ListController(FileSystemRepository fileSystemRepository) {
        this.fileSystemRepository = fileSystemRepository;
    }

    //Replaces old endpoints
    @GetMapping(version = "2.0")
    public @ResponseBody ResponseEntity<?> listFilesInclusive(
            @RequestParam long folder_id,
            @RequestParam(required = false) SortListEnum sort,
            @RequestParam(required = false) FilterListEnum filter,
            @RequestParam(required = false) String filterQuery) throws IOException {
        return ResponseEntity.ok().body(fileSystemRepository.listFilesV2(folder_id, sort, filter, filterQuery));
    }
}
