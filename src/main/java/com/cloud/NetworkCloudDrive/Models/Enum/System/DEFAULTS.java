package com.cloud.NetworkCloudDrive.Models.Enum.System;

public enum DEFAULTS {
    MAX_SIZE_OF_KEY(16),
    ;

    private final int max_size;

    DEFAULTS(int max_size) {
        this.max_size = max_size;
    }

    public int getMax_size() {
        return max_size;
    }
}
