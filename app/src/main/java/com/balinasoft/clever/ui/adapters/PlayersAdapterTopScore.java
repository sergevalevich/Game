package com.balinasoft.clever.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.balinasoft.clever.GameApplication.getUserId;

public class PlayersAdapterTopScore extends RecyclerView.Adapter<PlayersAdapterTopScore.PlayersHolder>{

    private List<Player> mPlayers;

    public PlayersAdapterTopScore(List<Player> players) {
        mPlayers = players;
    }

    @Override
    public PlayersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_item_top_score, parent,
                false);
        return new PlayersAdapterTopScore.PlayersHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlayersHolder holder, int position) {
        holder.bind(mPlayers.get(position));
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    class PlayersHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.place_label)
        TextView mPlaceLabel;

        @BindView(R.id.image)
        ImageView mPlayerImage;

        @BindView(R.id.name)
        TextView mPlayerName;

        @BindView(R.id.points)
        TextView mPointsLabel;

        PlayersHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(Player player) {
            mPlaceLabel.setText(String.valueOf(getAdapterPosition() + 1));
            mPlayerImage.setBackgroundResource(player.getImageResId());
            mPlayerName.setText(player.getName());
            mPointsLabel.setText(String.valueOf(player.getTotalScore()));
            if (player.getId().equals(getUserId())) mPlaceLabel.setBackgroundResource(R.drawable.place2);
        }

    }
}
