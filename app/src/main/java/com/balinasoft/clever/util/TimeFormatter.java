package com.balinasoft.clever.util;

import org.androidannotations.annotations.EBean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@EBean
public class TimeFormatter {
    public String formatTime(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat(ConstantsManager.SERVER_DATE_FORMAT, Locale.getDefault());
        return sdf.format(new Date(time));
    }
    public String formatServerTime(String serverTime) {
        SimpleDateFormat serverSdf = new SimpleDateFormat(ConstantsManager.SERVER_DATE_FORMAT, Locale.getDefault());
        SimpleDateFormat clientSdf = new SimpleDateFormat(ConstantsManager.CLIENT_DATE_FORMAT, Locale.getDefault());
        String time = "";
        try {
            Date date = serverSdf.parse(serverTime);
            time = clientSdf.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return time;
    }
}
