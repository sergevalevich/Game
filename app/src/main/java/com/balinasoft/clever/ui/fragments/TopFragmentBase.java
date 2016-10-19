package com.balinasoft.clever.ui.fragments;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.network.model.RatingModel;
import com.balinasoft.clever.util.AvatarMapper;
import com.balinasoft.clever.util.NetworkStateChecker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.ArrayList;
import java.util.List;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getOnlineName;
import static com.balinasoft.clever.GameApplication.getUserId;
import static com.balinasoft.clever.GameApplication.getUserImage;

@EFragment(R.layout.fragment_top)
public abstract class TopFragmentBase extends Fragment {

    @ViewById(R.id.root)
    FrameLayout mRootView;

    @ViewById(R.id.players_list)
    RecyclerView mRecyclerView;

    @ViewById(R.id.swipe)
    SwipeRefreshLayout mSwipe;

    @ViewById(R.id.user_image)
    ImageView mUserImage;

    @ViewById(R.id.user_name)
    TextView mUserNameLabel;

    @ViewById(R.id.place_label)
    TextView mPlaceLabel;

    @Bean
    DataManager mDataManager;

    @Bean
    NetworkStateChecker mNetworkStateChecker;

    @Bean
    AvatarMapper mAvatarMapper;

    @StringRes(R.string.network_unavailbale_message)
    String mNetworkUnavailableMessage;

    @StringRes(R.string.network_error_message)
    String mNetworkErrorMessage;

    private Subscription mSubscription;

    private int mUserPlace;

    @AfterViews
    void setUpViews() {
        setUpList();
        setUpSwipe();
        setUpBottomBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
        toggleSwipe(false);
        unSubscribe();
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleSwipe(true);
        getRating();
    }

    private void unSubscribe() {
        if(mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
    }

    private void toggleSwipe(boolean isRefreshing) {
        if(mSwipe != null) mSwipe.setRefreshing(isRefreshing);
    }

    private void getRating() {
        if(mNetworkStateChecker.isNetworkAvailable()) {
            mSubscription = fetchRating();
        } else {
            toggleSwipe(false);
            notifyUserWith(mNetworkUnavailableMessage);
        }
    }

    private void setUpList() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void setUpSwipe() {
        mSwipe.setColorSchemeResources(R.color.colorPrimary);
        mSwipe.setOnRefreshListener(() -> {
            unSubscribe();
            getRating();
        });
    }

    private void setUpBottomBar() {
        mUserNameLabel.setText(getOnlineName());
        mUserImage.setImageResource(getUserImage());
    }

    private Subscription fetchRating() {
        return mDataManager.getRatings(getFilter())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResult,this::handleError);
    }

    private void notifyUserWith(String message) {
        Snackbar.make(mRootView,message,Snackbar.LENGTH_LONG).show();
    }

    private void handleResult(RatingModel ratingModel) {
        toggleSwipe(false);
        if(ratingModel.getSuccess() == 1) {
            List<Player> players = getPlayers(ratingModel.getUsers());
            mRecyclerView.setAdapter(getAdapter(players));
            mPlaceLabel.setText(String.valueOf(mUserPlace));
        } else {
            notifyUserWith(mNetworkErrorMessage);
        }
    }

    private void handleError(Throwable throwable) {
        toggleSwipe(false);
        String message = throwable.getLocalizedMessage();
        notifyUserWith(message == null || message.isEmpty()
                ? mNetworkErrorMessage
                : message);
    }

    private List<Player> getPlayers(List<RatingModel.UserModel> apiPlayers) {
        List<Player> players = new ArrayList<>();

        for(int i = 0; i<apiPlayers.size(); i++) {
            RatingModel.UserModel apiPlayer = apiPlayers.get(i);
            Player player = new Player();
            player.setName(apiPlayer.getUserName());
            player.setCoinsPortion(apiPlayer.getCoins());
            player.setTotalScore(apiPlayer.getScore());
            String id = apiPlayer.getId();
            player.setId(id);
            if(id.equals(getUserId())) {
                mUserPlace = i + 1;
                player.setImageResId(getUserImage());
            } else player.setImageResId(mAvatarMapper.getAvatar(apiPlayer.getAvatar()));
            players.add(player);
        }

        return players;
    }

    abstract RecyclerView.Adapter getAdapter(List<Player> players);

    abstract String getFilter();

}
