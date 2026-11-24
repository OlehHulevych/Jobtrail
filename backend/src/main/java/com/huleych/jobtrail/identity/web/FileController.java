package com.huleych.jobtrail.identity.web;

import com.huleych.jobtrail.identity.infrastructure.storage.S3FileStorage;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/cv/file")
public class FileController {
    private final S3FileStorage storage;
    public FileController(S3FileStorage storage){
        this.storage = storage;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public void upload(@RequestParam("cv")MultipartFile avatar){

    }
}
