package com.quotamanagesys.tools;

import java.util.List;

import org.springframework.stereotype.Component;

import com.bstek.bdf2.job.model.JobDefinition;
import com.bstek.bdf2.job.service.IJobDataService;

@Component
public class TestJobDataService implements IJobDataService {
    @Override
    public List<JobDefinition> filterJobs(List<JobDefinition> jobs) {
        return jobs;
    }
    @Override
    public String getCompanyId() {
        return "bstek";
    }
}