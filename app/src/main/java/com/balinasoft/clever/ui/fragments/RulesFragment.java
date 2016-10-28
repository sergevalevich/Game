package com.balinasoft.clever.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.R;
import com.balinasoft.clever.storage.model.Rule;
import com.balinasoft.clever.ui.adapters.RulesAdapter;
import com.balinasoft.clever.util.VerticalSpaceItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

@EFragment(R.layout.fragment_rules)
public class RulesFragment extends Fragment {

    @ViewById(R.id.rules_list)
    RecyclerView mRecyclerView;

    @Bean
    RulesAdapter mRulesAdapter;

    @Bean
    DataManager mDataManager;

    private Subscription mSubscription;

    @AfterViews
    void setUpViews() {
        setUpRulesList();
        getRules();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unSubscribe();
    }

    private void setUpRulesList() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(16));
        mRecyclerView.setHasFixedSize(true);
    }

    private void getRules() {
        unSubscribe();
        mSubscription = mDataManager.getRules()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setData, this::handleError);
    }

    private void setData(List<Rule> rules) {
        mRulesAdapter.setData(rules);
        mRecyclerView.setAdapter(mRulesAdapter);
    }

    private void handleError(Throwable throwable) {

    }

    private void unSubscribe() {
        if (mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
    }
}
