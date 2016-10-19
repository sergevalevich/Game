package com.balinasoft.clever.ui.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.model.Room;
import com.balinasoft.clever.util.RoomsClickListener;

import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomHolder> {

    private List<Room> mRooms;

    private RoomsClickListener mRoomsClickListener;

    public RoomAdapter(List<Room> rooms, RoomsClickListener clickListener) {
        mRooms = rooms;
        mRoomsClickListener = clickListener;
    }

    @Override
    public RoomAdapter.RoomHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_item, parent,
                false);
        return new RoomHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RoomAdapter.RoomHolder holder, int position) {
        holder.bind(mRooms.get(position));
    }

    @Override
    public int getItemCount() {
        return mRooms.size();
    }

//    public void refresh(List<Room> rooms) {
//        mRooms.clear();
//        mRooms.addAll(rooms);
//        notifyDataSetChanged();
//    }

    class RoomHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.host_image)
        ImageView mHostImage;

        @BindView(R.id.name)
        TextView mHostName;

        @BindView(R.id.bet)
        TextView mBetLabel;

        @BindView(R.id.play_button)
        TextView mPlayButton;

        @BindViews({R.id.player_one, R.id.player_two, R.id.player_three, R.id.player_four, R.id.player_five})
        List<ImageView> mPlayersImages;

        RoomHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bind(Room room) {
            List<Player> players = room.getPlayers();
            Player host = players.get(0);
            mHostImage.setImageResource(host.getImageResId());
            mHostName.setText(host.getName());
            mBetLabel.setText(String.valueOf(room.getBet()));
            for (int i = 0; i < mPlayersImages.size(); i++) {
                int visibility = View.VISIBLE;
                ImageView imageView = mPlayersImages.get(i);
                if (i > room.getMaxPlayers() - 2) visibility = View.INVISIBLE;
                else if (i >= players.size() - 1) imageView.setImageResource(R.drawable.newgamer);
                else imageView.setImageResource(players.get(i + 1).getImageResId());
                imageView.setVisibility(visibility);
            }
            mPlayButton.setOnClickListener(view -> mRoomsClickListener.onRoomClicked(
                    room.getNumber(),
                    room.getBet()));
        }

    }
}
