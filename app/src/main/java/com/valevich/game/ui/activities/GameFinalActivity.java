package com.valevich.game.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.valevich.game.R;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_game_final)
public class GameFinalActivity extends AppCompatActivity {

    @ViewById(R.id.root)
    LinearLayout mRoot;

    @Click(R.id.root)
    void showGameSummary() {
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}
