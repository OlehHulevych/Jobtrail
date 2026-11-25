package com.huleych.jobtrail.identity.application.service;


import com.huleych.jobtrail.identity.application.commands.UploadCvCommand;
import com.huleych.jobtrail.identity.application.result.UploadResult;
import com.huleych.jobtrail.identity.domain.entity.CvDocument;
import com.huleych.jobtrail.identity.domain.entity.User;
import com.huleych.jobtrail.identity.domain.exceptions.UserNotFoundException;
import com.huleych.jobtrail.identity.infrastructure.repository.CvDocumentRepository;
import com.huleych.jobtrail.identity.infrastructure.repository.UserRepository;
import com.huleych.jobtrail.identity.infrastructure.storage.FileStorage;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;


@Service
public class CvUploadService {
    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private final FileStorage storage;
    private final CvDocumentRepository cvDocumentRepository;
    private final UserRepository userRepository;

    public CvUploadService(FileStorage storage, CvDocumentRepository cvDocumentRepository, UserRepository userRepository){
        this.storage = storage;
        this.cvDocumentRepository = cvDocumentRepository;
        this.userRepository = userRepository;
    }

    public UploadResult execute(UploadCvCommand command){
        if(command==null) throw new IllegalArgumentException("Command request is required");
        if(command.userId()==null) throw new IllegalArgumentException("Id of User is required");
        if(command.content()==null) throw new IllegalArgumentException("File content is required");
        if(command.contentType()==null || !command.contentType().equals(PDF_CONTENT_TYPE)) throw new IllegalArgumentException("Only PDF files are supported");
        if(command.size()<=0) throw new IllegalArgumentException("File is empty");

        User user = userRepository.findById(command.userId()).orElse(null);
        if(user==null) throw new UserNotFoundException(command.userId());

        byte[] bytes;
        try {
            bytes = command.content().readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if(!hasPdfHeader(bytes)) throw new IllegalArgumentException("File is not a valid PDF");

        String objectKey = "cv/" + command.userId() + '/' + UUID.randomUUID();
        CvDocument newCvDocument =  new CvDocument(user,objectKey, command.originalName(), command.contentType());
        extractText(newCvDocument, bytes);

        storage.store(objectKey,new ByteArrayInputStream(bytes),bytes.length,command.contentType());
        CvDocument saved;
        try {
            saved = cvDocumentRepository.save(newCvDocument);
        } catch (RuntimeException e) {
            storage.delete(objectKey);
            throw e;
        }
        return new UploadResult(saved.getId(),saved.getOriginalName(),saved.getContentType(),saved.getExtractionStatus());
    }

    private boolean hasPdfHeader(byte[] bytes){
        return bytes.length >= 4 && bytes[0]=='%' && bytes[1]=='P' && bytes[2]=='D' && bytes[3]=='F';
    }

    private void extractText(CvDocument cvDocument, byte[] pdf){
        try (PDDocument document = Loader.loadPDF(pdf)) {
            String text = new PDFTextStripper().getText(document).replace("\u0000", "");
            if(text.isBlank()) cvDocument.markExtractionFailed();
            else cvDocument.markExtracted(text);
        } catch (IOException e) {
            cvDocument.markExtractionFailed();
        }
    }
}
