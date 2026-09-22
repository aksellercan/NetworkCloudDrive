package com.cloud.NetworkCloudDrive.Models;

import com.cloud.NetworkCloudDrive.Models.Generics.MetadataSuperClass;
import jakarta.persistence.Entity;
import org.hibernate.annotations.ColumnDefault;

//DONE Last updated

@Entity
public class FileMetadata extends MetadataSuperClass {
    private Long folderId;

    private String mimiType;

    private Long size;

    @ColumnDefault("false")
    private boolean hasThumbnail = false;

    public FileMetadata(String name, Long folderId, Long userid, String mimiType, Long size) {
        setName(name);
        this.folderId = folderId;
        setUserid(userid);
        this.mimiType = mimiType;
        this.size = size;
    }

    public FileMetadata() {
    }

    public FileMetadata(FileMetadata fileMetadata) {
        setId(fileMetadata.getId());
        setName(fileMetadata.getName());
        this.folderId = fileMetadata.folderId;
        setUserid(fileMetadata.getUserid());
        this.mimiType = fileMetadata.mimiType;
        this.size = fileMetadata.size;
        setCreatedAt(fileMetadata.getCreatedAt());
        this.hasThumbnail = fileMetadata.hasThumbnail;
        setLastUpdated(fileMetadata.getLastUpdated());
        setMarked(fileMetadata.isMarked());
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getMimiType() {
        return mimiType;
    }

    public void setMimiType(String mimiType) {
        this.mimiType = mimiType;
    }

    public boolean isHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "folderId=" + folderId +
                ", mimiType='" + mimiType + '\'' +
                ", size=" + size +
                ", hasThumbnail=" + hasThumbnail +
                '}';
    }
}
