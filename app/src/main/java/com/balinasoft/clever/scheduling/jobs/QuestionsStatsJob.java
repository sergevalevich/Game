package com.balinasoft.clever.scheduling.jobs;

import android.support.annotation.NonNull;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.util.ConstantsManager;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

@EBean
public class QuestionsStatsJob extends Job {

    @Bean
    DataManager mDataManager;

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        mDataManager.sendQuestionsStats().subscribe(quote -> {}, throwable -> {});
        return Result.SUCCESS;
    }

    public int schedule() {
        return new JobRequest.Builder(ConstantsManager.QUESTION_STATS_JOB_TAG)
                .setExecutionWindow(2_000L, 4_000L)
                .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }
}
