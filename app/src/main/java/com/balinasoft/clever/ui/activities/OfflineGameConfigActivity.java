package com.balinasoft.clever.ui.activities;

import android.os.Bundle;

import com.balinasoft.clever.R;
import com.balinasoft.clever.ui.fragments.OfflineConfigFragment_;

import org.androidannotations.annotations.EActivity;

@EActivity
public class OfflineGameConfigActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_config);
        if (savedInstanceState == null) replaceFragment();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_fade_in, R.anim.exit_push_out);
    }

    private void replaceFragment() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_container, new OfflineConfigFragment_(), OfflineConfigFragment_.class.getName())
                .commit();
    }
}
