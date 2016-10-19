package com.balinasoft.clever.util;

import com.balinasoft.clever.R;

import org.androidannotations.annotations.EBean;

import java.util.TreeMap;

@EBean
public class AvatarMapper {
    private final TreeMap<Integer,Integer> AVATARS = new TreeMap<Integer, Integer>() {{
        put(R.drawable.first_girl_profile_flat_icon_game,1);
        put(R.drawable.first_man_profile_flat_icon_game,2);
        put(R.drawable.second_girl_profile_flat_icon_game,3);
        put(R.drawable.second_man_profile_flat_icon_game,4);
        put(R.drawable.third_man_profile_flat_icon_game,5);
    }};

    public int getAvatarId(int avatarResId) {
        return AVATARS.get(avatarResId);
    }

    public int getAvatar(int avatarId) {
        return AVATARS.keySet().toArray(new Integer[0])[avatarId-1];
    }
}
