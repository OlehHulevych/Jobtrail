package com.huleych.jobtrail;

import com.huleych.jobtrail.tracking.domain.enums.ApplicationStatus;
import com.huleych.jobtrail.tracking.domain.entity.JobApplication;
import com.huleych.jobtrail.tracking.infrastructure.repository.JobApplicationRepository;

import java.util.*;

public class FakeJobApplicationRepository implements JobApplicationRepository {
    public List<JobApplication> storage;

    public FakeJobApplicationRepository(){
        storage = new ArrayList<JobApplication>();
    }
    @Override
    public JobApplication save(JobApplication jobApplication) {
        JobApplication application = storage.stream()
                .filter(a-> Objects.equals(a.getOwnerId(), jobApplication
                        .getOwnerId()) && Objects.equals(a.getPosition(), jobApplication.getPosition()) && Objects.equals(a.getCompany(),
                        jobApplication.getCompany())).findFirst().orElse(null);
        if(application!=null) throw new IllegalArgumentException("Application is already existing");
        storage.add(jobApplication);
        return jobApplication;
    }

    @Override
    public List<JobApplication> findAllByOwnerId(UUID ownerId) {
        return storage.stream().filter(a-> Objects.equals(a.getOwnerId(), ownerId)).toList();
    }

    @Override
    public List<JobApplication> findAllByOwnerIdAndStatus(UUID ownerId, ApplicationStatus status) {
        return storage.stream().filter(a-> Objects.equals(a.getOwnerId(), ownerId) && a.getStatus()==status).toList();
    }

    @Override
    public Optional<JobApplication> findById(UUID id) {
        return storage.stream().filter(a -> Objects.equals(a.getId(), id)).findFirst();
    }

    @Override
    public void delete(JobApplication jobApplication) {
        storage.remove(jobApplication);
    }
}
