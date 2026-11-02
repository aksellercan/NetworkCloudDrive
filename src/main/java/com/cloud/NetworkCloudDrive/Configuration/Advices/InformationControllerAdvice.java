package com.cloud.NetworkCloudDrive.Configuration.Advices;

import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice("com.cloud.NetworkCloudDrive.Controllers")
public class InformationControllerAdvice {
    private final Logger logger = LoggerFactory.getLogger(InformationControllerAdvice.class);

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIOException(IOException exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JSONErrorResponse(exception, "IO exception occurred"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGlobalException(Exception exception) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JSONErrorResponse(exception));
    }
}
