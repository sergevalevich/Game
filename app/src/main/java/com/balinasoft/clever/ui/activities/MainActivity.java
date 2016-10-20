package com.balinasoft.clever.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.R;
import com.balinasoft.clever.eventbus.EventBus;
import com.balinasoft.clever.eventbus.events.AvatarSelectedEvent;
import com.balinasoft.clever.eventbus.events.UserNameSelectedEvent;
import com.balinasoft.clever.storage.model.News;
import com.balinasoft.clever.ui.dialogs.AvatarDialog;
import com.balinasoft.clever.ui.dialogs.AvatarDialog_;
import com.balinasoft.clever.ui.fragments.NewsFragment_;
import com.balinasoft.clever.ui.fragments.OnlineConfigFragment_;
import com.balinasoft.clever.ui.fragments.StatsFragment_;
import com.balinasoft.clever.util.ConstantsManager;
import com.balinasoft.clever.util.SocketErrorListener;
import com.balinasoft.clever.util.TimeFormatter;
import com.squareup.otto.Subscribe;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;
import org.json.JSONException;
import org.json.JSONObject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getOnlineCoins;
import static com.balinasoft.clever.GameApplication.getOnlineName;
import static com.balinasoft.clever.GameApplication.getOnlineScore;
import static com.balinasoft.clever.GameApplication.getUserImage;

@EActivity
public class MainActivity extends BaseActivity implements
        FragmentManager.OnBackStackChangedListener,
        SocketErrorListener {

    @ViewById(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;

    @ViewById(R.id.toolbar)
    Toolbar mToolbar;

    @ViewById(R.id.navigation_view)
    NavigationView mNavigationView;

    @StringRes(R.string.nav_drawer_game)
    String mGameTitle;

    @StringRes(R.string.nav_drawer_stats)
    String mStatsTitle;

    @StringRes(R.string.nav_drawer_news)
    String mNewsTitle;

    @StringRes(R.string.nav_drawer_rules)
    String mRulesTitle;

    @StringRes(R.string.network_unavailbale_message)
    String mNetworkUnavailableMessage;

    @StringRes(R.string.unknown_error_message)
    String mUnknownErrorMessage;

    @Bean
    DataManager mDataManager;

    @Bean
    TimeFormatter mTimeFormatter;

    @Bean
    EventBus mEventBus;

    private FragmentManager mFragmentManager;

    private AvatarDialog mDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        String action = intent.getAction();
        if(action != null && action.equals(ConstantsManager.NOTIFICATION_ACTION) && intent.hasExtra("new"))
            try {
                saveNews(createNews(intent));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        if (savedInstanceState == null) {
            replaceFragment(new OnlineConfigFragment_());
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        subscribeBus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateScore();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unSubscribeBus();
    }

    @Subscribe
    public void onAvatarSelected(AvatarSelectedEvent event) {
        ((ImageView) mNavigationView
                .getHeaderView(0)
                .findViewById(R.id.image))
                .setImageResource(event.getAvatarResId());
    }

    @Subscribe
    public void onUserNameSelected(UserNameSelectedEvent event) {
        ((TextView) mNavigationView
                .getHeaderView(0)
                .findViewById(R.id.name))
                .setText(event.getUserName());
    }

    @AfterViews
    void setupViews() {
        setupActionBar();
        setupDrawerLayout();
        setupFragmentManager();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (mFragmentManager.getBackStackEntryCount() == 1) {
            finish();
        } else {
            super.onBackPressed();
        }

    }

    @Override
    public void onBackStackChanged() {

        Fragment f = mFragmentManager
                .findFragmentById(R.id.main_container);

        if (f != null) {
            changeToolbarTitle(f.getClass().getName());
        }

    }

    @Override
    public void onSocketError(String message) {
        if(RESUMED_ACTIVITIES_COUNT > 0) {
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
            Timber.d("toast main");
        }
    }

    private void exit() {
        EnterActivity_.intent(this)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }

//    private void prepareLogout() {
//        if (mNetworkStateChecker.isNetworkAvailable()) sendUserStatsImmediately();
//        else notifyUserWith(mNetworkUnavailableMessage);
//    }

    private void subscribeBus() {
        mEventBus.register(this);
    }

    private void unSubscribeBus() {
        mEventBus.unregister(this);
    }

    private void setupNavigationContent() {
        mNavigationView.setNavigationItemSelectedListener(item -> {
            if (mDrawerLayout != null) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
            int itemId = item.getItemId();
            switch (itemId) {
                case R.id.drawer_game:
                    replaceFragment(new OnlineConfigFragment_());
                    break;
                case R.id.drawer_news:
                    replaceFragment(new NewsFragment_());
                    break;
                case R.id.drawer_rules:
                    Toast.makeText(this, mRulesTitle, Toast.LENGTH_SHORT).show();
                    break;
                case R.id.drawer_stats:
                    replaceFragment(new StatsFragment_());
                    break;
                case R.id.drawer_exit:
                    exit();
                    break;
            }
            return true;
        });
        setUpHeader();
    }

    private void setUpHeader() {
        View headerView = mNavigationView.getHeaderView(0);
        headerView.setOnClickListener(view -> showDialog());
        ImageView profileImage = (ImageView) headerView.findViewById(R.id.image);
        TextView nameField = (TextView) headerView.findViewById(R.id.name);
        nameField.setText(getOnlineName());
        profileImage.setImageResource(getUserImage());
    }

    private void updateScore() {
        View headerView = mNavigationView.getHeaderView(0);
        TextView coins = (TextView) headerView.findViewById(R.id.coins);
        TextView points = (TextView) headerView.findViewById(R.id.points);
        coins.setText(String.valueOf(getOnlineCoins()));
        points.setText(String.valueOf(getOnlineScore()));
    }

    private void showDialog() {
        if(mDialog == null)
            mDialog = createDialog();
        mDialog.setCurrentName(getOnlineName());
        mDialog.show(getSupportFragmentManager(), ConstantsManager.AVATAR_DIALOG_TAG);
    }

    private AvatarDialog createDialog() {
        return AvatarDialog_.builder().isOfflineMode(false).build();
    }

    private void changeToolbarTitle(String backStackEntryName) {
        if (backStackEntryName.equals(OnlineConfigFragment_.class.getName())) {
            setTitle(mGameTitle);
            mNavigationView.setCheckedItem(R.id.drawer_game);
        } else if (backStackEntryName.equals(StatsFragment_.class.getName())) {
            setTitle(mStatsTitle);
            mNavigationView.setCheckedItem(R.id.drawer_stats);
        } else if (backStackEntryName.equals(NewsFragment_.class.getName())) {
            setTitle(mNewsTitle);
            mNavigationView.setCheckedItem(R.id.drawer_news);
        }
//        else {
//            setTitle(mStatisticsTitle);
//            mNavigationView.setCheckedItem(R.id.drawer_statistics);
//        }
    }

    private void setupDrawerLayout() {
        setupNavigationContent();
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, mDrawerLayout
                , mToolbar
                , R.string.navigation_drawer_open
                , R.string.navigation_drawer_close);
        toggle.syncState();
        mDrawerLayout.addDrawerListener(toggle);
        setTitle(mGameTitle);
    }

    private void setupActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void replaceFragment(Fragment fragment) {
        String backStackName = fragment.getClass().getName();

        boolean isFragmentPopped = mFragmentManager.popBackStackImmediate(backStackName, 0);

        if (!isFragmentPopped && mFragmentManager.findFragmentByTag(backStackName) == null) {

            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            transaction.replace(R.id.main_container, fragment, backStackName);
            transaction.addToBackStack(backStackName);
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.commit();

        }
    }

    private void setupFragmentManager() {
        mFragmentManager = getSupportFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(this);
    }

    private News createNews(Intent intent) throws JSONException {
        JSONObject apiNews = new JSONObject(intent.getStringExtra("new"));
        Timber.d("news %s",apiNews.toString());
        News news = new News();
        news.setDate(mTimeFormatter.formatServerTime(apiNews.getString("date")));
        news.setDescription(apiNews.getString("text"));
        news.setTopic(apiNews.getString("title"));
        news.setImageUrl(apiNews.getString("image"));
        return news;
    }

    private void saveNews(News news) {
        mDataManager.insertNews(news)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((n) -> {
                    replaceFragment(new NewsFragment_());
                }, throwable ->
                {
                    Timber.d("error saving news %s", throwable.getLocalizedMessage());
                });
    }

//    private void notifyUserWith(String message) {
//        Snackbar.make(mDrawerLayout, message, Snackbar.LENGTH_LONG).show();
//    }

//    private void sendUserStatsImmediately() {
//        setSessionTime(getCurrentTime() - getLaunchTime());
//        mDataManager.sendUserStats()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(response -> {
//                    if (response.body().getSuccess() == 1) {
//                        clearAccountData();
//                        exit();
//                    } else notifyUserWith(response.body().getMessage());
//                }, throwable -> {
//                    notifyUserWith(mUnknownErrorMessage);
//                });
//    }

//    private void clearAccountData() {
//        saveCleverToken("");
//        saveFacebookToken("");
//        saveVkToken("");
//        setUserEmail("");
//        setOnlineName("");
//        setOnlineCoins(0);
//        setOnlineScore(0);
//        Toast.makeText(this, "Вы вышли из учётной записи", Toast.LENGTH_SHORT).show();
//    }
}
