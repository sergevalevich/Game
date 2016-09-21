package com.balinasoft.clever.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.balinasoft.clever.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_game_final)
public class GameFinalActivity extends BaseActivity {

    @ViewById(R.id.root)
    RelativeLayout mRoot;

    @ViewById(R.id.place)
    TextView mPlaceLabel;

    @ViewById(R.id.points)
    TextView mPointsLabel;

    @ViewById(R.id.coins)
    TextView mCoinsLabel;

    @ViewById(R.id.cup)
    ImageView mCupImage;

    @Extra
    int coins;

    @Extra
    int points;

    @Extra
    int place;

    @AfterViews
    void setUpViews() {
        setUpPlace();
        setUpCoins();
        setUpPoints();
        setUpCup();
    }

    private void setUpCup() {
        switch (place) {
            case 0:
                mCupImage.setImageResource(R.drawable.firstplace);
                break;
            case 1:
                mCupImage.setImageResource(R.drawable.secondplace);
                break;
            case 2:
                mCupImage.setImageResource(R.drawable.thirdplace);
                break;
            case 3:
                mCupImage.setImageResource(R.drawable.fourthplace);
                break;
            case 4:
                mCupImage.setImageResource(R.drawable.fifthplace);
                break;
            case 5:
                mCupImage.setImageResource(R.drawable.sixthplace);
                break;
        }
    }

    private void setUpPoints() {
        mPointsLabel.setText(String.valueOf(points));
    }

    private void setUpCoins() {
        mCoinsLabel.setText(String.valueOf(coins));
    }

    private void setUpPlace() {
        mPlaceLabel.setText(String.valueOf(place+1));
    }

    @Click(R.id.root)
    void showGameSummary() {
        finish();
    }

    @Override
    public void onBackPressed() {

    }
}
