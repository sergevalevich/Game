package com.balinasoft.clever.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.ui.adapters.RoomAdapter;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.test)
public class Test extends AppCompatActivity {

    @ViewById(R.id.rooms_list)
    RecyclerView mRoomsList;

    @AfterViews
    void setUpOtherViews() {
        setUpRooms();
    }

    private void setUpRooms() {
        mRoomsList.setLayoutManager(new LinearLayoutManager(this));
        mRoomsList.setAdapter(new RoomAdapter(ConstantsManager.STUB_ROOMS,this));
    }
}
