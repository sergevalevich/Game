package com.balinasoft.clever.scheduling.jobs;

import android.support.annotation.NonNull;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.util.ConstantsManager;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

@EBean
public class UsersStatsJob extends Job {

    @Bean
    DataManager mDataManager;

    private int mId;

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        mDataManager.sendUserStats().subscribe(response -> {}, throwable -> {});
        return Result.SUCCESS;
    }

    public void schedule() {
        mId = new JobRequest.Builder(ConstantsManager.USERS_STATS_JOB_TAG)
                .setExecutionWindow(2_000L,3_000L)
                .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setRequirementsEnforced(true)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

    public int getId() {
        return mId;
    }
}
