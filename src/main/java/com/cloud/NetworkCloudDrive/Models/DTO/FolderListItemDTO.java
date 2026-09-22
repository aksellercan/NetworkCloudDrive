package com.cloud.NetworkCloudDrive.Models.DTO;

import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Generics.ListItemSuperClass;

public class FolderListItemDTO extends ListItemSuperClass {
    private String path;

    public FolderListItemDTO() {
    }

    public FolderListItemDTO(FolderMetadata folderMetadata) {
        setId(folderMetadata.getId());
        setName(folderMetadata.getName());
        this.path = folderMetadata.getPath();
        setCreatedAt(folderMetadata.getCreatedAt());
        setLastAccessedAt(folderMetadata.getLastUpdated());
        setMarked(folderMetadata.isMarked());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
