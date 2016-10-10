package com.balinasoft.clever.ui.fragments;

import android.graphics.Color;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.balinasoft.clever.R;
import com.balinasoft.clever.ui.adapters.SectionsPagerAdapter;
import com.balinasoft.clever.util.OnPageSelectedListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import timber.log.Timber;

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

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private void setupViewPager() {
        mSectionsPagerAdapter = new SectionsPagerAdapter(getChildFragmentManager(), getActivity());
        mPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout.setupWithViewPager(mPager);
        mTabLayout.setTabTextColors(Color.WHITE, Color.WHITE);
        mTabLayout.setSelectedTabIndicatorHeight(0);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                Timber.d("onPageSelected");
                OnPageSelectedListener fragment = (OnPageSelectedListener) mSectionsPagerAdapter.getItem(position);
                fragment.onPageSelected();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }
}
