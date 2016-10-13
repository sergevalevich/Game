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

public class PlayersAdapterTopCoins extends RecyclerView.Adapter<PlayersAdapterTopCoins.PlayersHolder> {

    private List<Player> mPlayers;

    public PlayersAdapterTopCoins(List<Player> players) {
        mPlayers = players;
    }

    @Override
    public PlayersAdapterTopCoins.PlayersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_item_top_coins, parent,
                false);
        return new PlayersAdapterTopCoins.PlayersHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlayersAdapterTopCoins.PlayersHolder holder, int position) {
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

        @BindView(R.id.coins)
        TextView mCoinsLabel;

        PlayersHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(Player player) {
            mPlaceLabel.setText(String.valueOf(getAdapterPosition() + 1));
            mPlayerImage.setBackgroundResource(player.getImageResId());
            mPlayerName.setText(player.getName());
            mCoinsLabel.setText(String.valueOf(player.getCoinsPortion()));
            if (player.getId().equals(getUserId())) mPlaceLabel.setBackgroundResource(R.drawable.place2);
            else mPlaceLabel.setBackgroundResource(R.drawable.place);
        }

    }
}
