package com.cloud.NetworkCloudDrive.Controllers.Filesystem.Actions;

import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderPathDTO;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONMapResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "filesystem/actions/folder")
public class FolderActionsController {
    private final InformationRepository informationRepository;
    private final EncodingUtility encodingUtility;
    private final FileSystemRepository fileSystemRepository;
    private final PathUtility pathUtility;

    public FolderActionsController(InformationRepository informationRepository, EncodingUtility encodingUtility, FileSystemRepository fileSystemRepository, PathUtility pathUtility) {
        this.informationRepository = informationRepository;
        this.encodingUtility = encodingUtility;
        this.fileSystemRepository = fileSystemRepository;
        this.pathUtility = pathUtility;
    }

    @GetMapping(version = "2.0", value = "rename")
    public @ResponseBody ResponseEntity<?> rename(@RequestBody UpdateFolderNameDTO updateFolderNameDTO) throws Exception {
        FolderMetadata oldFolder = informationRepository.getFolderMetadata(updateFolderNameDTO.getFolder_id());
        String oldName = encodingUtility.decodedBase32SplitArray(oldFolder.getName())[1];
        String updatedPath = fileSystemRepository.updateFolderName(updateFolderNameDTO.getName(), oldFolder);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                body(new JSONResponse("Updated folder name with Id %d from %s to %s. Updated path %s",
                        updateFolderNameDTO.getFolder_id(), oldName, updateFolderNameDTO.getName(), updatedPath));
    }

    @GetMapping(version = "2.0", value = "move")
    public @ResponseBody ResponseEntity<?> move(@RequestBody UpdateFolderPathDTO updateFolderPathDTO) throws Exception {
        FolderMetadata folderToMove = informationRepository.getFolderMetadata(updateFolderPathDTO.getFormer_folder_id());
        String oldPath = pathUtility.resolvePathFromIdString(folderToMove.getPath());
        String newPath = fileSystemRepository.moveFolder(folderToMove, updateFolderPathDTO.getDestination_folder_id());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                body(new JSONMapResponse(
                        Map.of("old_path", oldPath, "new_path", newPath),
                        "Successfully moved folder with Id %d", updateFolderPathDTO.getFormer_folder_id()));
    }

    @DeleteMapping(version = "2.0", value = "remove")
    public @ResponseBody ResponseEntity<JSONResponse> remove(@RequestParam @BindParam("fold") long folderId) throws Exception {
        FolderMetadata folderToRemove = informationRepository.getFolderMetadata(folderId);
        String oldPath = fileSystemRepository.removeFolder(folderToRemove);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                body(new JSONResponse("Folder with Id %d at path %s was successfully removed", folderToRemove.getId(), oldPath));
    }
}
