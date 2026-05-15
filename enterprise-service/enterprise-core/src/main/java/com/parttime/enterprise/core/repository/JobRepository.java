package com.parttime.enterprise.core.repository;

import com.parttime.enterprise.core.domain.Job;
import com.parttime.enterprise.core.domain.JobCategory;
import com.parttime.enterprise.core.domain.JobRate;
import com.parttime.enterprise.core.domain.JobSchedule;

import java.util.List;
import java.util.Optional;

public interface JobRepository {

    void save(Job job);

    Optional<Job> findById(Long id);

    List<Job> findByCompanyId(Long companyId);

    List<Job> findByCompanyIdAndStatus(Long companyId, String status);

    void update(Job job);

    void updateStatus(Long id, String status);

    List<JobRate> findRatesByJobId(Long jobId);

    List<JobSchedule> findSchedulesByJobId(Long jobId);

    void saveRate(JobRate rate);

    void saveSchedule(JobSchedule schedule);

    void deleteRatesByJobId(Long jobId);

    void deleteSchedulesByJobId(Long jobId);

    Optional<JobRate> findRateById(Long id);

    void updateRate(JobRate rate);

    void deleteRate(Long id);

    Optional<JobSchedule> findScheduleById(Long id);

    void updateSchedule(JobSchedule schedule);

    void deleteSchedule(Long id);

    List<JobCategory> findAllCategories();

    Optional<JobCategory> findCategoryById(Long id);

    void saveCategory(JobCategory category);

    void updateCategory(JobCategory category);

    void deleteCategory(Long id);
}
