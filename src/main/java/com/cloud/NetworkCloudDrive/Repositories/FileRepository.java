package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Repository
public interface FileRepository {
    Map<String, ?> uploadFiles(MultipartFile[] files, String folderPath, long folderId) throws Exception;

    @Deprecated
    Map<String, ?> uploadFilesOld(MultipartFile[] files, String folderPath, long folderId) throws IOException, ExecutionException, InterruptedException;

    CompletableFuture<Path> storeFile(InputStream inputStream, String fileName, String parentPath) throws IOException;

    CompletableFuture<Resource> getFile(FileMetadata file, String path) throws Exception;

    @Async
    @Deprecated
    CompletableFuture<Resource> getFileOld(FileMetadata file, String path) throws Exception;

    FolderMetadata createFolder(String folderName, long folderId) throws Exception;

    @Deprecated
    FolderMetadata createFolderOld(String folderName, long folderId) throws Exception;

    void markFile(boolean mark, long fileId) throws SQLException;

    void markFolder(boolean mark, long folder) throws SQLException;
}
