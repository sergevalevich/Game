package com.balinasoft.clever.scheduling;

import android.content.Context;

import com.balinasoft.clever.scheduling.jobs.QuestionsStatsJob_;
import com.balinasoft.clever.scheduling.jobs.UsersStatsJob;
import com.balinasoft.clever.scheduling.jobs.UsersStatsJob_;
import com.balinasoft.clever.util.ConstantsManager;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

@EBean
public class GameJobCreator implements JobCreator {

    @RootContext
    Context mContext;

    @Override
    public Job create(String tag) {
        switch (tag) {
            case ConstantsManager.QUESTION_STATS_JOB_TAG:
                return QuestionsStatsJob_.getInstance_(mContext);
            case ConstantsManager.USERS_STATS_JOB_TAG:
                return UsersStatsJob_.getInstance_(mContext);
            default:
                return null;
        }
    }
}
