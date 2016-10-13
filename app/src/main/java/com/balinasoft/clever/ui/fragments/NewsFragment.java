package com.balinasoft.clever.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.R;
import com.balinasoft.clever.storage.model.News;
import com.balinasoft.clever.ui.adapters.NewsAdapter;
import com.balinasoft.clever.util.VerticalSpaceItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_news)
public class NewsFragment extends Fragment {

    @ViewById(R.id.news_list)
    RecyclerView mNewsList;

    @ViewById(R.id.swipe)
    SwipeRefreshLayout mSwipe;

    @Bean
    DataManager mDataManager;

    @Bean
    NewsAdapter mNewsAdapter;

    private Subscription mSubscription;

    @AfterViews
    void setUpViews() {
        setUpList();
        setUpSwipe();
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleSwipe(true);
        mSubscription = getNews();
    }

    @Override
    public void onPause() {
        super.onPause();
        toggleSwipe(false);
        unSubscribe();
    }

    private void unSubscribe() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
    }

    private void setUpList() {
        mNewsList.setLayoutManager(new LinearLayoutManager(getContext()));
        mNewsList.addItemDecoration(new VerticalSpaceItemDecoration(8));
    }

    private void setUpSwipe() {
        mSwipe.setColorSchemeResources(R.color.colorPrimary);
        mSwipe.setOnRefreshListener(() -> {
            unSubscribe();
            mSubscription = getNews();
        });
    }

    private Subscription getNews() {
        return mDataManager.getNews()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(() -> toggleSwipe(false))
                .subscribe(this::setAdapter, throwable -> {
                });
    }

    private void setAdapter(List<News> newsList) {
        mNewsAdapter.setData(newsList);
        mNewsList.setAdapter(mNewsAdapter);
    }

    private void toggleSwipe(boolean isRefreshing) {
        if(mSwipe != null) mSwipe.setRefreshing(isRefreshing);
    }

}
