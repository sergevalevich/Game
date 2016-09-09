package com.valevich.game.scheduling.jobs;


import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.valevich.game.DataManager;
import com.valevich.game.util.ConstantsManager;
import com.valevich.game.util.Preferences_;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

@EBean
public class UpdateQuestionsJob extends Job {

    @Bean
    DataManager mDataManager;

    @Pref
    Preferences_ mPreferences;

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        Timber.d("onRunJob");

        mDataManager.downloadQuestions()
                .flatMap(questionApiModels -> mDataManager.saveQuestions(questionApiModels))
                .subscribe(o -> {},throwable -> {},() -> mPreferences.isDataFresh().put(true));

        return Result.SUCCESS;
    }

    public int schedule() {
        return new JobRequest.Builder(ConstantsManager.UPDATE_QUESTIONS_JOB_TAG)
                .setPeriodic(TimeUnit.DAYS.toMillis(1))
                .setPersisted(true)
                .setRequiredNetworkType(JobRequest.NetworkType.UNMETERED)
                .setRequirementsEnforced(true)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }
}
