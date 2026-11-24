package com.huleych.jobtrail.identity.infrastructure.repository;

import com.huleych.jobtrail.identity.domain.entity.CvDocument;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaCvDocumentRepository implements CvDocumentRepository {
    private final SpringDataCvDocumentRepository repository;

    public JpaCvDocumentRepository(SpringDataCvDocumentRepository repository){
        this.repository = repository;
    }

    @Override
    public CvDocument save(CvDocument cvDocument) {
        return repository.save(cvDocument);
    }

    @Override
    public Optional<CvDocument> findById(UUID id) {
        return repository.findById(id);
    }
}
