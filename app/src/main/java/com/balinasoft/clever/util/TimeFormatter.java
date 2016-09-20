package com.balinasoft.clever.util;

import org.androidannotations.annotations.EBean;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@EBean
public class TimeFormatter {
    public String formatTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(ConstantsManager.DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date(time));
    }
}
