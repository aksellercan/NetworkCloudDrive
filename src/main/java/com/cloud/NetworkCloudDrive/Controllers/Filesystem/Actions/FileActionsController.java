package com.cloud.NetworkCloudDrive.Controllers.Filesystem.Actions;

import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFileNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFilePathDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "filesystem/actions/file")
public class FileActionsController {
    private final InformationRepository informationRepository;
    private final FileSystemRepository fileSystemRepository;
    private final PathUtility pathUtility;
    private final UserSession userSession;

    public FileActionsController(
            InformationRepository informationRepository,
            FileSystemRepository fileSystemRepository,
            PathUtility pathUtility,
            UserSession userSession) {
        this.informationRepository = informationRepository;
        this.fileSystemRepository = fileSystemRepository;
        this.pathUtility = pathUtility;
        this.userSession = userSession;
    }

    @PatchMapping(version = "2.0", value = "rename")
    public @ResponseBody ResponseEntity<JSONResponse> rename(@RequestBody UpdateFileNameDTO updateFileNameDTO) throws Exception {
        FileMetadata oldFile = informationRepository.getFileMetadata(updateFileNameDTO.getFile_id());
        String oldName = oldFile.getName();
        String updatedPath = fileSystemRepository.updateFileName(updateFileNameDTO.getName(), oldFile);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                body(new JSONResponse("Updated file with Id %d from %s to %s. Updated path %s",
                        updateFileNameDTO.getFile_id(), oldName, updateFileNameDTO.getName(), updatedPath));
    }

    @PostMapping(version = "2.0", value = "move")
    public @ResponseBody ResponseEntity<JSONResponse> moveFile(@RequestBody UpdateFilePathDTO updateFilePathDTO) throws Exception {
        FileMetadata fileToMove = informationRepository.getFileMetadata(updateFilePathDTO.getFile_id());
        String oldPath = (updateFilePathDTO.getFolder_id() > 0 ?
                pathUtility.resolvePathFromIdString(informationRepository.getFolderMetadata(updateFilePathDTO.getFolder_id()).getPath())
                :
                userSession.getName());
        String newPath = fileSystemRepository.moveFile(fileToMove, updateFilePathDTO.getFolder_id());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                body(new JSONResponse("Moved file with Id %d from %s to %s", updateFilePathDTO.getFile_id(), oldPath, newPath));
    }

    @DeleteMapping(version = "2.0", value = "remove")
    public @ResponseBody ResponseEntity<JSONResponse> removeFile(@RequestParam @BindParam("file") long fileId) throws Exception {
        FileMetadata fileToRemove = informationRepository.getFileMetadata(fileId);
        String oldPath = fileSystemRepository.removeFile(fileToRemove);
        return ResponseEntity.ok().body(new JSONResponse("file with Id %d at path %s was successfully removed", fileToRemove.getId(), oldPath));
    }
}
