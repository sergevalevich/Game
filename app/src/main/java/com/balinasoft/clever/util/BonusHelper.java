package com.balinasoft.clever.util;

import org.androidannotations.annotations.EBean;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static com.balinasoft.clever.GameApplication.getBonus;
import static com.balinasoft.clever.GameApplication.getLastAppLaunchDay;
import static com.balinasoft.clever.GameApplication.getUserCoins;
import static com.balinasoft.clever.GameApplication.isAuthTokenExists;
import static com.balinasoft.clever.GameApplication.isFirstLaunch;
import static com.balinasoft.clever.GameApplication.setBonus;
import static com.balinasoft.clever.GameApplication.setLastAppLaunchDay;
import static com.balinasoft.clever.GameApplication.setOnlineCoins;
import static com.balinasoft.clever.GameApplication.setUserCoins;

@EBean
public class BonusHelper {

    private GregorianCalendar mCalendar = (GregorianCalendar) Calendar.getInstance();

    private int mCurrentDay = getCurrentDay();

    public interface BonusListener {
        void success();
        void fail();
    }

    private BonusListener mBonusListener;

    public void setBonusListener(BonusListener bonusListener) {
        mBonusListener = bonusListener;
    }

    public void checkBonus() {

        int dayRange = isFirstLaunch() ? 1 :  getCurrentDay() - getLastAppLaunchDay();

        if (isOneDayPassed(dayRange)) {

            int currentBonus = getBonus();
            if (!isMaxBonus(currentBonus)) setBonus(++currentBonus);
            int newCoinsCount = getUserCoins() + currentBonus;
            if(isAuthTokenExists()) setOnlineCoins(newCoinsCount);
            else setUserCoins(newCoinsCount);
            setLastAppLaunchDay(mCurrentDay);
            mBonusListener.success();

        } else {

            if(isMoreThanOneDayPassed(dayRange)) {
                setBonus(0);
                setLastAppLaunchDay(mCurrentDay);
            }
            mBonusListener.fail();
        }

    }

    private boolean isOneDayPassed(int dayRange) {
        return dayRange == 1
                || (dayRange == -364
                && !mCalendar.isLeapYear(mCalendar.get(Calendar.YEAR) - 1))
                || dayRange == -365;
    }

    private boolean isMoreThanOneDayPassed(int dayRange) {
        return Math.abs(dayRange) > 1;
    }

    private boolean isMaxBonus(int currentBonus) {
        return currentBonus >= ConstantsManager.MAX_BONUS;
    }

    private int getCurrentDay() {
        return mCalendar.get(Calendar.DAY_OF_YEAR);
    }
}
