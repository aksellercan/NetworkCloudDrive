package com.cloud.NetworkCloudDrive.Controllers.Maintenance;

import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import com.cloud.NetworkCloudDrive.Utilities.APIComponent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "health")
public class APIHealthController {
    private final APIComponent apiComponent;

    public APIHealthController(APIComponent apiComponent) {
        this.apiComponent = apiComponent;
    }

    @GetMapping(path = "version", version = "1.0")
    public ResponseEntity<JSONResponse> getAPIVersion() {
        return new ResponseEntity<>(new JSONResponse(apiComponent.getAPIVersion()), HttpStatus.OK);
    }
}
