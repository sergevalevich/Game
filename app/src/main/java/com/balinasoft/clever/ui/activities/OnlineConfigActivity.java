package com.balinasoft.clever.ui.activities;


import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.ui.adapters.RoomAdapter;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_online_config)
public class OnlineConfigActivity extends ConfigActivityBase {

    @ViewById(R.id.rooms_list)
    RecyclerView mRoomsList;

    @AfterViews
    @Override
    void setUpOtherViews() {
        setUpRooms();
    }

    @Override
    void createGame() {

    }

    private void setUpRooms() {
        mRoomsList.setLayoutManager(new LinearLayoutManager(this));
        mRoomsList.setAdapter(new RoomAdapter(ConstantsManager.STUB_ROOMS,this));
    }

}
