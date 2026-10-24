package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.domain.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.JobApplication;
import com.huleych.jobtrail.tracking.domain.JobApplicationRepository;

import java.util.*;
import java.util.stream.Collectors;

public class FakeJobApplicationRepository implements JobApplicationRepository {
    public List<JobApplication> storage;

    public FakeJobApplicationRepository(){
        storage = new ArrayList<JobApplication>();
    }
    @Override
    public JobApplication save(JobApplication jobApplication) {
        Optional<JobApplication> application = findById(jobApplication.getId());
        if(application.isEmpty()) throw new IllegalArgumentException("Application is already existing");
        storage.add(jobApplication);
        return jobApplication;
    }

    @Override
    public List<JobApplication> findAllByOwnerId(Long ownerId) {
        return storage.stream().filter(a-> Objects.equals(a.getOwnerId(), ownerId)).toList();
    }

    @Override
    public List<JobApplication> findAllByOwnerIdAndStatus(Long ownerId, ApplicationStatus status) {
        return storage.stream().filter(a-> Objects.equals(a.getOwnerId(), ownerId) && a.getStatus()==status).toList();
    }

    @Override
    public Optional<JobApplication> findById(Long id) {
        return storage.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst();
    }
}
