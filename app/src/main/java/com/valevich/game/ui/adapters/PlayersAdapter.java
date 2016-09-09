package com.valevich.game.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.valevich.game.GameApplication;
import com.valevich.game.R;
import com.valevich.game.model.Player;
import com.valevich.game.util.ConstantsManager;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayersHolder> {

    private Player[] mPlayers;

    private int mTourNumber;

    private boolean mIsGameFinished = false;

    public PlayersAdapter(Player[] players, int tourNumber) {
        Player[] pl = new Player[players.length + 1];
        pl[0] = null;
        System.arraycopy(players, 0, pl, 1, pl.length - 1);
        mPlayers = pl;
        mTourNumber = tourNumber;
    }

    @Override
    public PlayersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_item, parent,
                false);
        return new PlayersHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlayersHolder holder, int position) {
        Player player = mPlayers[position];
        if (player == null) {
            holder.itemView.setVisibility(View.INVISIBLE);
        } else holder.bind(player);
    }

    @Override
    public int getItemCount() {
        return mPlayers.length;
    }

    class PlayersHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.place_label)
        TextView mPlaceLabel;

        @BindView(R.id.image)
        ImageView mPlayerImage;

        @BindView(R.id.name)
        TextView mPlayerName;

        @BindView(R.id.answered_value)
        TextView mAnsweredQuestionsLabel;

        @BindView(R.id.question_count)
        TextView mMaxQuestionsLabel;

        @BindView(R.id.points)
        TextView mPointsLabel;

        @BindView(R.id.coin)
        ImageView mCoinPicture;

        @BindView(R.id.coins)
        TextView mCoinsLabel;

        @BindView(R.id.star)
        ImageView mStar;

        @BindView(R.id.answered_from)
        TextView mFrom;

        @BindView(R.id.answers_label)
        TextView mAnswersLabel;

        public PlayersHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Player player) {
            mPlaceLabel.setText(String.valueOf(getAdapterPosition()));

            if (player.getName().equals(GameApplication.getUserName()))
                mPlaceLabel.setBackgroundResource(R.drawable.place2);
            mPlayerImage.setBackgroundResource(player.getImageResId());
            mPlayerName.setText(player.getName());
            mAnsweredQuestionsLabel.setText(String.valueOf(player.getTotalRightAnswersCount()));
            mPointsLabel.setText(String.valueOf(player.getTotalScore()));
            mMaxQuestionsLabel.setText(String.valueOf(ConstantsManager.ROUND_QUESTIONS_COUNT*mTourNumber));

            if (mIsGameFinished) {
                mCoinsLabel.setText(String.valueOf(player.getCoinsPortion(mPlayers)));
            } else {
                mCoinPicture.setVisibility(View.INVISIBLE);
                mCoinsLabel.setVisibility(View.INVISIBLE);
            }
        }

    }

    public void setGameFinished(boolean gameFinished) {
        mIsGameFinished = gameFinished;
        notifyDataSetChanged();
    }
}