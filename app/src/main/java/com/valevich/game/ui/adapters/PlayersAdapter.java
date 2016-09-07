package com.valevich.game.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.valevich.game.R;
import com.valevich.game.model.Player;
import com.valevich.game.util.ConstantsManager;

import butterknife.BindView;
import butterknife.ButterKnife;


public class PlayersAdapter extends RecyclerView.Adapter<PlayersAdapter.PlayersHolder> {

    private Player[] mPlayers;

    private int mTourNumber;

    public PlayersAdapter(Player[] players) {
        Player[] pl = new Player[players.length + 1];
        pl[0] = null;
        System.arraycopy(players, 0, pl, 1, pl.length - 1);
        mPlayers = pl;
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
            holder.mPlayerImage.setImageResource(R.drawable.first_man_profile_flat_icon_game);
            holder.mPlayerImage.setVisibility(View.INVISIBLE);
            holder.mCoinPicture.setVisibility(View.INVISIBLE);
            holder.mPlaceLabel.setVisibility(View.INVISIBLE);
            holder.mAnsweredQuestionsLabel.setVisibility(View.INVISIBLE);
            holder.mCoinsLabel.setVisibility(View.INVISIBLE);
            holder.mPlayerName.setVisibility(View.INVISIBLE);
            holder.mMaxQuestionsLabel.setVisibility(View.INVISIBLE);
            holder.mPointsLabel.setVisibility(View.INVISIBLE);
            holder.mStar.setVisibility(View.INVISIBLE);
            holder.mFrom.setVisibility(View.INVISIBLE);
            holder.mAnswersLabel.setVisibility(View.INVISIBLE);
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

            if (player.getName().equals(ConstantsManager.DEFAULT_USER_NAME))
                mPlaceLabel.setBackgroundResource(R.drawable.place2);
            mPlayerImage.setBackgroundResource(player.getImageResId());
            mPlayerName.setText(player.getName());

            if (wasLastRound()) {
                mMaxQuestionsLabel.setText(String.valueOf(ConstantsManager.MAX_QUESTIONS_COUNT));
                mAnsweredQuestionsLabel.setText(String.valueOf(player.getTotalRightAnswersCount()));
                mPointsLabel.setText(String.valueOf(player.getTotalScore()));
                mCoinsLabel.setText(String.valueOf(player.getCoinsPortion(mPlayers)));
            } else {
                mMaxQuestionsLabel.setText(String.valueOf(ConstantsManager.ROUND_QUESTIONS_COUNT));
                mAnsweredQuestionsLabel.setText(String.valueOf(player.getLastRoundAnswersCount()));
                mPointsLabel.setText(String.valueOf(player.getLastRoundScore()));
                mCoinPicture.setVisibility(View.INVISIBLE);
                mCoinsLabel.setVisibility(View.INVISIBLE);
            }
        }

    }

    public void setTourNumber(int tourNumber) {
        mTourNumber = tourNumber;
        notifyDataSetChanged();
    }

    private boolean wasLastRound() {
        return mTourNumber == 4;
    }
}