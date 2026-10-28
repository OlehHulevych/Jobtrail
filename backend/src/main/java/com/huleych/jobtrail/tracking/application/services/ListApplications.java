package com.huleych.jobtrail.tracking.application.services;

import com.huleych.jobtrail.tracking.application.queries.ListApplicationsQuery;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListApplications {
    private final JobApplicationRepository repository;

    public ListApplications(JobApplicationRepository repository){
        this.repository = repository;
    }

    @Transactional
    public List<JobApplication> execute(ListApplicationsQuery query){
        if(query==null) throw new IllegalArgumentException("Query is required");
        if(query.status()==null){
            return repository.findAllByOwnerId(query.ownerId());
        }
        return repository.findAllByOwnerIdAndStatus(query.ownerId(), query.status());
    }



}
