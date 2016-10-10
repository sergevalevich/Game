package com.balinasoft.clever.ui.fragments;

import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.balinasoft.clever.DataManager;
import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.network.model.RatingModel;
import com.balinasoft.clever.util.NetworkStateChecker;
import com.balinasoft.clever.util.OnPageSelectedListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.res.StringRes;

import java.util.ArrayList;
import java.util.List;

import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.balinasoft.clever.GameApplication.getOnlineName;
import static com.balinasoft.clever.GameApplication.getUserId;
import static com.balinasoft.clever.GameApplication.getUserImage;

@EFragment(R.layout.fragment_top)
public abstract class TopFragmentBase extends Fragment implements OnPageSelectedListener {

    @ViewById(R.id.root)
    FrameLayout mRootView;

    @ViewById(R.id.progress_bar)
    MaterialProgressBar mProgressBar;

    @ViewById(R.id.players_list)
    RecyclerView mRecyclerView;

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

    @StringRes(R.string.network_unavailbale_message)
    String mNetworkUnavailableMessage;

    @StringRes(R.string.network_error_message)
    String mNetworkErrorMessage;

    private Subscription mSubscription;

    private int mUserPlace;

    private boolean mIsFetchNeeded = false;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser) {
            if(mNetworkStateChecker != null && mProgressBar != null) {
                mIsFetchNeeded = false;
                Timber.d("VISIBLE");
                setUpViews();
                getRating();
            }
            else mIsFetchNeeded = true;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Timber.d("onPause");
        if(mSubscription != null && !mSubscription.isUnsubscribed()) mSubscription.unsubscribe();
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onPageSelected() {
        //getRating();
    }

    @Override
    public void onResume() {
        super.onResume();
        Timber.d("onResume");
        if(mIsFetchNeeded) {
            setUpViews();
            getRating();
        }
    }

    private void setUpViews() {
        setUpList();
        setUpBottomBar();
    }

    private void getRating() {
        if(mNetworkStateChecker.isNetworkAvailable()) {
            mProgressBar.setVisibility(View.VISIBLE);
            mSubscription = fetchRating();
        }
        else notifyUserWith(mNetworkUnavailableMessage);
    }

    private void setUpList() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
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
        mProgressBar.setVisibility(View.GONE);
        if(ratingModel.getSuccess() == 1) {
            List<Player> players = getPlayers(ratingModel.getUsers());
            mRecyclerView.setAdapter(getAdapter(players));
            mPlaceLabel.setText(String.valueOf(mUserPlace));
        } else {
            notifyUserWith(mNetworkErrorMessage);
        }
    }

    private void handleError(Throwable throwable) {
        mProgressBar.setVisibility(View.GONE);
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
            } else player.setImageResId(Player.getRandomImage());
            players.add(player);
        }

        return players;
    }

    abstract RecyclerView.Adapter getAdapter(List<Player> players);

    abstract String getFilter();

}
