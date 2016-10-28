package com.balinasoft.clever.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import com.balinasoft.clever.R;
import com.balinasoft.clever.eventbus.EventBus;
import com.balinasoft.clever.eventbus.events.AvatarSelectedEvent;
import com.balinasoft.clever.eventbus.events.UserNameSelectedEvent;
import com.balinasoft.clever.services.UserStatsService_;
import com.balinasoft.clever.util.ConstantsManager;
import com.balinasoft.clever.util.NetworkStateChecker;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import static com.balinasoft.clever.GameApplication.getOnlineName;
import static com.balinasoft.clever.GameApplication.getUserName;
import static com.balinasoft.clever.GameApplication.isAuthTokenExists;
import static com.balinasoft.clever.GameApplication.setOnlineName;
import static com.balinasoft.clever.GameApplication.setUserImage;
import static com.balinasoft.clever.GameApplication.setUserName;

@EFragment
public class AvatarDialog extends DialogFragment implements DialogInterface.OnDismissListener {

    @ViewById(R.id.userName_field)
    EditText mUserNameField;

    @FragmentArg
    boolean isOfflineMode;

    @Bean
    EventBus mEventBus;

    @Bean
    NetworkStateChecker mNetworkStateChecker;

    private int mCurrentSelection;

    @Click(R.id.avatar_one)
    void onFirstAvatarSelected() {
        onAvatarSelected(R.drawable.first_man_profile_flat_icon_game);
    }

    @Click(R.id.avatar_two)
    void onSecondAvatarSelected() {
        onAvatarSelected(R.drawable.second_man_profile_flat_icon_game);
    }

    @Click(R.id.avatar_three)
    void onThirdAvatarSelected() {
        onAvatarSelected(R.drawable.third_man_profile_flat_icon_game);
    }

    @Click(R.id.avatar_four)
    void onFourthAvatarSelected() {
        onAvatarSelected(R.drawable.first_girl_profile_flat_icon_game);
    }

    @Click(R.id.avatar_five)
    void onFifthAvatarSelected() {
        onAvatarSelected(R.drawable.second_girl_profile_flat_icon_game);
    }

    @AfterViews
    void setUpDialog() {
        mUserNameField.setText(isOfflineMode ? getUserName() : getOnlineName());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_avatar, container, false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_dialog);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialogInterface) {
        saveSelections();
        Context context = getContext();
        if (context != null && mNetworkStateChecker.isNetworkAvailable() && isAuthTokenExists() && !isOfflineMode) {
            UserStatsService_.intent(context).start();
        }
        super.onDismiss(dialogInterface);
    }

    private void saveSelections() {
        if(mCurrentSelection != 0) setUserImage(mCurrentSelection);
        String userName = mUserNameField.getText().toString();
        if(userName.isEmpty()) userName = isOfflineMode
                ? ConstantsManager.DEFAULT_USER_NAME
                : ConstantsManager.DEFAULT_USER_NAME_ONLINE;
        mEventBus.post(new UserNameSelectedEvent(userName));
        if(isOfflineMode) setUserName(userName);
        else setOnlineName(userName);
    }

    private void onAvatarSelected(int selectedResId) {
        mCurrentSelection = selectedResId;
        mEventBus.post(new AvatarSelectedEvent(mCurrentSelection));
    }
}
