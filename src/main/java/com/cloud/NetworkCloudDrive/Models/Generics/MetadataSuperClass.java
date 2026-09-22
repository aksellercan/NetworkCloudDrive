package com.cloud.NetworkCloudDrive.Models.Generics;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@MappedSuperclass
public class MetadataSuperClass {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private Long userid;

    @CreationTimestamp
    private Instant createdAt;

    private Instant lastUpdated;

    @ColumnDefault("false")
    private boolean marked = false;

    public MetadataSuperClass() {
    }

    public MetadataSuperClass(Long id, String name, Long userid, Instant createdAt, Instant lastUpdated, boolean marked) {
        this.id = id;
        this.name = name;
        this.userid = userid;
        this.createdAt = createdAt;
        this.lastUpdated = lastUpdated;
        this.marked = marked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void updateLastUpdated() {
        this.lastUpdated = Instant.now();
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }
}
