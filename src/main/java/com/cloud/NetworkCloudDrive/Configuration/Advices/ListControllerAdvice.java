package com.cloud.NetworkCloudDrive.Configuration.Advices;

import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;

@ControllerAdvice("com.cloud.NetworkCloudDrive.Controllers.Filesystem.List")
public class ListControllerAdvice {
    Logger logger = LoggerFactory.getLogger(ListControllerAdvice.class);

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException exception) {
        logger.error("Some files couldn't be listed, reason: {}", exception.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JSONErrorResponse(exception, "IO exception occurred"));
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> handleFileNotFoundException(FileNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONErrorResponse(exception, "Requested file not found"));
    }

    @ExceptionHandler(FileSystemException.class)
    public ResponseEntity<?> handleFilesystemException(FileSystemException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JSONErrorResponse(exception, "FileSystem exception occurred"));
    }

    @ExceptionHandler(FileAlreadyExistsException.class)
    public ResponseEntity<?> handleFileAlreadyExistsException(FileAlreadyExistsException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JSONErrorResponse(exception, "File already exists"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JSONErrorResponse(exception));
    }
}
