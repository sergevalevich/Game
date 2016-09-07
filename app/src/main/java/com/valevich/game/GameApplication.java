package com.valevich.game;

import android.app.Application;

import com.evernote.android.job.JobManager;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.valevich.game.scheduling.GameJobCreator;
import com.valevich.game.scheduling.jobs.UpdateQuestionsJob;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.EBean;

import timber.log.Timber;

@EApplication
public class GameApplication extends Application {

    @Bean
    GameJobCreator mGameJobCreator;

    @Bean
    UpdateQuestionsJob mUpdateQuestionsJob;

    @Override
    public void onCreate() {
        super.onCreate();

        //android-job
        JobManager.create(this).addJobCreator(mGameJobCreator);
        mUpdateQuestionsJob.schedule();

        //DbFlow
        FlowManager.init(new FlowConfig.Builder(this)
                .openDatabasesOnInit(true).build());

        //Timber
        if(BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree() {
                @Override
                protected String createStackElementTag(StackTraceElement element) {
                    return super.createStackElementTag(element) + ":" + element.getLineNumber();
                }
            });
        }
    }
}
