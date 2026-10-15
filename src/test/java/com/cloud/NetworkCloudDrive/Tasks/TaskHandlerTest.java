package com.cloud.NetworkCloudDrive.Tasks;

import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Jobs.MaintenanceJob;
import com.cloud.NetworkCloudDrive.Models.Jobs.ThumbnailJob;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobType;
import com.cloud.NetworkCloudDrive.Tasks.Implementations.MaintenanceBackgroundTask;
import com.cloud.NetworkCloudDrive.Tasks.Implementations.ThumbnailBackgroundTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskHandlerTest {

    @Mock
    private ThumbnailBackgroundTask thumbnailBackgroundTask;

    @Mock
    private MaintenanceBackgroundTask maintenanceBackgroundTask;

    private TaskHandler taskHandler;

    @BeforeEach
    void setUp() {
        taskHandler = new TaskHandler(thumbnailBackgroundTask, maintenanceBackgroundTask);
    }

    @Test
    void handle_IoFunctionJob_CallsMaintenanceTaskOnly() {
        Job job = new Job();
        job.setJobType(JobType.IO_FUNCTION);
        when(maintenanceBackgroundTask.runTask(job)).thenReturn(true);

        taskHandler.handle(job);

        verify(maintenanceBackgroundTask).runTask(job);
        verify(thumbnailBackgroundTask, never()).runTask(any());
    }

    @Test
    void handle_ThumbnailFunctionJob_CallsThumbnailTaskOnly() {
        Job job = new Job();
        job.setJobType(JobType.THUMBNAIL_FUNCTION);
        when(thumbnailBackgroundTask.runTask(job)).thenReturn(true);

        taskHandler.handle(job);

        verify(thumbnailBackgroundTask).runTask(job);
        verify(maintenanceBackgroundTask, never()).runTask(any());
    }

    @Test
    void handle_RecurrentFunctionJob_CallsNeitherTask() {
        Job job = new Job();
        job.setJobType(JobType.RECURRENT_FUNCTION);

        taskHandler.handle(job);

        verify(maintenanceBackgroundTask, never()).runTask(any());
        verify(thumbnailBackgroundTask, never()).runTask(any());
    }

    @Test
    void handle_JobWithNullJobType_ThrowsNullPointerException() {
        Job job = new Job();
        job.setJobType(null);

        assertThrows(NullPointerException.class, () -> taskHandler.handle(job));
    }

    @Test
    void handle_MaintenanceJobSubType_CallsMaintenanceTask() {
        MaintenanceJob job = new MaintenanceJob(Path.of("/tmp"), 0L, com.cloud.NetworkCloudDrive.Models.Enum.ScanOptions.NORMAL);
        when(maintenanceBackgroundTask.runTask(job)).thenReturn(false);

        taskHandler.handle(job);

        verify(maintenanceBackgroundTask).runTask(job);
        verify(thumbnailBackgroundTask, never()).runTask(any());
    }

    @Test
    void handle_ThumbnailJobSubType_CallsThumbnailTask() {
        ThumbnailJob job = new ThumbnailJob(Path.of("/tmp"), "file.jpg", 1L);
        when(thumbnailBackgroundTask.runTask(job)).thenReturn(true);

        taskHandler.handle(job);

        verify(thumbnailBackgroundTask).runTask(job);
        verify(maintenanceBackgroundTask, never()).runTask(any());
    }

    @Test
    void getProgress_ReturnsZero() {
        assertEquals(0, taskHandler.getProgress());
    }

    @Test
    void halt_ReturnsFalse() {
        assertFalse(taskHandler.halt());
    }

    @Test
    void getJob_ReturnsNullByDefault() {
        assertNull(taskHandler.getJob());
    }
}