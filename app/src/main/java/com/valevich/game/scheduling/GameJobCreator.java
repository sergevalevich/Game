package com.valevich.game.scheduling;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;
import com.valevich.game.scheduling.jobs.UpdateQuestionsJob;
import com.valevich.game.util.ConstantsManager;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

@EBean
public class GameJobCreator implements JobCreator {

    @Bean
    UpdateQuestionsJob mUpdateQuestionsJob;

    @Override
    public Job create(String tag) {
        switch (tag) {
            case ConstantsManager.UPDATE_QUESTIONS_JOB_TAG:
                return mUpdateQuestionsJob;
            default:
                return null;
        }
    }
}
