package com.balinasoft.clever.ui.fragments;

import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.R;
import com.balinasoft.clever.storage.model.Rule;
import com.balinasoft.clever.ui.adapters.RulesAdapter;
import com.balinasoft.clever.util.VerticalSpaceItemDecoration;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.ColorRes;
import org.androidannotations.annotations.res.StringRes;

import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static com.balinasoft.clever.util.ConstantsManager.SEARCH_ID;


@OptionsMenu(R.menu.rules_menu)
@EFragment(R.layout.fragment_rules)
public class RulesFragment extends Fragment {

    @ViewById(R.id.rules_list)
    RecyclerView mRecyclerView;

    @Bean
    RulesAdapter mRulesAdapter;

    @Bean
    DataManager mDataManager;

    @OptionsMenuItem(R.id.action_search)
    MenuItem mSearchMenuItem;

    @StringRes(R.string.search_hint)
    String mSearchHint;

    @ColorRes(R.color.colorPrimary)
    int mPrimaryColor;

    private Subscription mSubscription;

    @AfterViews
    void setUpViews() {
        setUpRulesList();
        getRules("");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unSubscribe();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        SearchView searchView = (SearchView) mSearchMenuItem.getActionView();
        customizeSearchView(searchView);

        searchView.setQueryHint(mSearchHint);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return false;
            }
        });
    }

    @UiThread(delay = 700, id = SEARCH_ID)
    void search(String text) {
        mRulesAdapter.setSearchQuery(text);
        getRules(text);
    }

    private void setUpRulesList() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(16));
        mRecyclerView.setHasFixedSize(true);
    }

    private void getRules(String filter) {
        unSubscribe();
        mSubscription = mDataManager.getRules(filter)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::setData, this::handleError);
    }

    private void customizeSearchView(SearchView searchView) {
        int searchPlateId = searchView.getContext().getResources().getIdentifier("android:id/search_plate", null, null);
        View searchPlateView = searchView.findViewById(searchPlateId);

        if (searchPlateView != null) {
            searchPlateView.setBackgroundColor(mPrimaryColor);
        }

        int searchImgId = getResources().getIdentifier("android:id/search_button", null, null);
        ImageView search = (ImageView) searchView.findViewById(searchImgId);

        if (search != null) {
            search.setImageResource(R.drawable.search);
        }
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
