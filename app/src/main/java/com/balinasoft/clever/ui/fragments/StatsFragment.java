package com.balinasoft.clever.ui.fragments;

import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.balinasoft.clever.R;
import com.balinasoft.clever.ui.adapters.SectionsPagerAdapter;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import static com.balinasoft.clever.R.id.pager;
import static com.balinasoft.clever.R.id.tabLayout;

@EFragment(R.layout.fragment_stats)
public class StatsFragment extends Fragment {

    @ViewById(pager)
    ViewPager mPager;

    @ViewById(tabLayout)
    TabLayout mTabLayout;

    @AfterViews
    void setUpViews() {
        setupViewPager();
    }

    private void setupViewPager() {
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager(), getActivity());
        mPager.setAdapter(sectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mPager);
        mTabLayout.setTabTextColors(Color.WHITE, Color.WHITE);
        mTabLayout.setSelectedTabIndicatorHeight(0);
    }
}
