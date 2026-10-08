package com.cloud.NetworkCloudDrive.Models.Generics;

import java.time.Instant;

public class ListItemSuperClass {
    private long id;
    private String name;
    private Instant createdAt;
    private Instant lastAccessedAt;
    private boolean marked;

    public ListItemSuperClass() {
    }

    public ListItemSuperClass(long id, String name, Instant createdAt, Instant lastAccessedAt, boolean marked) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.lastAccessedAt = lastAccessedAt;
        this.marked = marked;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(Instant lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    public boolean isMarked() {
        return marked;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }
}
