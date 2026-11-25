package com.huleych.jobtrail.identity.web.controller;

import com.huleych.jobtrail.identity.application.commands.UploadCvCommand;
import com.huleych.jobtrail.identity.application.result.UploadResult;
import com.huleych.jobtrail.identity.application.service.CvUploadService;
import com.huleych.jobtrail.identity.domain.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;

@RestController
@RequestMapping("/api/cv/file")
public class FileController {
    private final CvUploadService cvUploadService;
    public FileController(CvUploadService cvUploadService){
        this.cvUploadService = cvUploadService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public UploadResult upload(@AuthenticationPrincipal User user, @RequestParam("cv") MultipartFile file) {
        try {
            return cvUploadService.execute(new UploadCvCommand(user.getId(),file.getOriginalFilename(),file.getContentType(),file.getSize(),file.getInputStream()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
