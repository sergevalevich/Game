package com.balinasoft.clever.util;


import android.view.animation.Animation;

import org.androidannotations.annotations.EBean;

@EBean
public class AnimationHelper {

    public interface AnimationStartListener {
        void onAnimationStart();
    }

    public interface AnimationEndListener {
        void onAnimationEnd();
    }

    public interface AnimationRepeatListener {
        void onAnimationRepeat();
    }

    public void setAnimationListener(Animation animation,
                                     AnimationEndListener endListener,
                                     AnimationStartListener startListener,
                                     AnimationRepeatListener repeatListener) {

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if(startListener != null) startListener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(endListener != null) endListener.onAnimationEnd();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if(repeatListener != null) repeatListener.onAnimationRepeat();
            }
        });

    }
}
