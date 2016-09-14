package com.valevich.game.eventbus;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import org.androidannotations.annotations.EBean;

@EBean(scope = EBean.Scope.Singleton)
public class EventBus extends Bus {
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    @Override
    public void post(final Object event) {
        if (Looper.myLooper() == Looper.getMainLooper()) super.post(event);
        else mHandler.post(() -> EventBus.super.post(event));
    }
}