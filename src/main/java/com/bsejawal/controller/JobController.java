package com.bsejawal.controller;

import com.bsejawal.service.WithoutSpringBatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {
    private final JobLauncher jobLauncher;
    private final Job job;
    private final WithoutSpringBatch withoutSpringBatch;

    @PostMapping("/batch")
    public void importCsvDBJob(){
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis())
                .toJobParameters();
        try{
            jobLauncher.run(job, jobParameters);
        }catch (Exception e){
            log.error("Failed to start job {} with parameters {}", job.getName(), jobParameters, e);
        }
    }
    @PostMapping("/no-batch")
    public void importCsvDBJobWithoutBatch(){
        withoutSpringBatch.loaCSV();
    }
}
