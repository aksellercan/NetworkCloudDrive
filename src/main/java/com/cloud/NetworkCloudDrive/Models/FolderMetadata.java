package com.cloud.NetworkCloudDrive.Models;

import com.cloud.NetworkCloudDrive.Models.Generics.MetadataSuperClass;
import jakarta.persistence.Entity;

//TODO add folder permissions
//DONE Last updated

@Entity
public class FolderMetadata extends MetadataSuperClass {
    private String path;

    public FolderMetadata(String name, String path) {
        setName(name);
        this.path = path;
    }

    public FolderMetadata() {
    }

    public FolderMetadata(FolderMetadata folderMetadata) {
        setId(folderMetadata.getId());
        setName(folderMetadata.getName());
        this.path = folderMetadata.path;
        setUserid(folderMetadata.getUserid());
        setCreatedAt(folderMetadata.getCreatedAt());
        setLastUpdated(folderMetadata.getLastUpdated());
        setMarked(folderMetadata.isMarked());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return "FolderMetadata{" +
                "path='" + path + '\'' +
                '}';
    }
}
