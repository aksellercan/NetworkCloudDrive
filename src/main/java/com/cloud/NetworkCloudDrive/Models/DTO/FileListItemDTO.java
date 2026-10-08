package com.cloud.NetworkCloudDrive.Models.DTO;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.Generics.ListItemSuperClass;

public class FileListItemDTO extends ListItemSuperClass {
    private String mimeType;
    private long size;
    private boolean hasThumbnail;

    public FileListItemDTO() {
    }

    public FileListItemDTO(FileMetadata fileMetadata) {
        setId(fileMetadata.getId());
        setName(fileMetadata.getName());
        this.mimeType = fileMetadata.getMimiType();
        this.size = fileMetadata.getSize();
        setCreatedAt(fileMetadata.getCreatedAt());
        this.hasThumbnail = fileMetadata.isHasThumbnail();
        setLastAccessedAt(fileMetadata.getLastUpdated());
        setMarked(fileMetadata.isMarked());
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }
}
