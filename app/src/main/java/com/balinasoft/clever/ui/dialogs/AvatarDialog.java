package com.balinasoft.clever.ui.dialogs;

import android.app.Dialog;
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

import com.balinasoft.clever.GameApplication;
import com.balinasoft.clever.R;
import com.balinasoft.clever.eventbus.EventBus;
import com.balinasoft.clever.eventbus.events.AvatarSelectedEvent;
import com.balinasoft.clever.eventbus.events.UserNameSelectedEvent;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import static com.balinasoft.clever.GameApplication.setUserImage;
import static com.balinasoft.clever.GameApplication.setUserName;

@EFragment
public class AvatarDialog extends DialogFragment implements DialogInterface.OnDismissListener {

    @ViewById(R.id.userName_field)
    EditText mUserNameField;

    @FragmentArg
    String currentName;

    @Bean
    EventBus mEventBus;

    private int mCurrentSelection;

    @Click(R.id.avatar_one)
    void onFirstAvatarSelected() {
        onAvatarSelected(R.drawable.first_man_profile_flat_icon_med);
    }

    @Click(R.id.avatar_two)
    void onSecondAvatarSelected() {
        onAvatarSelected(R.drawable.second_man_profile_flat_icon_med);
    }

    @Click(R.id.avatar_three)
    void onThirdAvatarSelected() {
        onAvatarSelected(R.drawable.third_man_profile_flat_icon_med);
    }

    @Click(R.id.avatar_four)
    void onFourthAvatarSelected() {
        onAvatarSelected(R.drawable.first_girl_profile_flat_icon_med);
    }

    @Click(R.id.avatar_five)
    void onFifthAvatarSelected() {
        onAvatarSelected(R.drawable.second_girl_profile_flat_icon_med);
    }

    @AfterViews
    void setUpDialog() {
        mUserNameField.setText(currentName);
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
        super.onDismiss(dialogInterface);
    }

    private void saveSelections() {
        if(mCurrentSelection != 0) setUserImage(mCurrentSelection);
        String userName = mUserNameField.getText().toString();
        if(userName.isEmpty()) userName = ConstantsManager.DEFAULT_USER_NAME;
        mEventBus.post(new UserNameSelectedEvent(userName));
        setUserName(userName);
    }

    private void onAvatarSelected(int selectedResId) {
        mCurrentSelection = selectedResId;
        mEventBus.post(new AvatarSelectedEvent(mCurrentSelection));
    }
}
