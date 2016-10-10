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

public class PlayersAdapterRoom extends RecyclerView.Adapter<PlayersAdapterRoom.PlayersHolder> {

    private List<Player> mPlayers;

    public PlayersAdapterRoom(List<Player> players) {
        mPlayers = players;
    }

    @Override
    public PlayersAdapterRoom.PlayersHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_item_room, parent,
                false);
        return new PlayersAdapterRoom.PlayersHolder(itemView);
    }

    @Override
    public void onBindViewHolder(PlayersAdapterRoom.PlayersHolder holder, int position) {
        holder.bind(mPlayers.get(position));
    }

    @Override
    public int getItemCount() {
        return mPlayers.size();
    }

    class PlayersHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image)
        ImageView mPlayerImage;

        @BindView(R.id.name)
        TextView mPlayerName;


        PlayersHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(Player player) {
            mPlayerImage.setImageResource(player.getImageResId());
            mPlayerName.setText(player.getName());
        }

    }

}
