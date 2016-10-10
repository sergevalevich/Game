package com.balinasoft.clever.ui.adapters;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.balinasoft.clever.R;
import com.balinasoft.clever.ui.fragments.TopCoinsFragment_;
import com.balinasoft.clever.ui.fragments.TopPointsFragment_;

public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;

    public SectionsPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new TopPointsFragment_();
            case 1:
                return new TopCoinsFragment_();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return mContext.getString(R.string.by_points);
            case 1:
                return mContext.getString(R.string.by_coins);
            default: return null;
        }
    }
}
