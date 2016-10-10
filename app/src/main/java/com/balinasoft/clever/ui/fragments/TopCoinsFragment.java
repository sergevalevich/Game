package com.balinasoft.clever.ui.fragments;

import android.support.v7.widget.RecyclerView;

import com.balinasoft.clever.R;
import com.balinasoft.clever.model.Player;
import com.balinasoft.clever.ui.adapters.PlayersAdapterTopCoins;
import com.balinasoft.clever.util.ConstantsManager;

import org.androidannotations.annotations.EFragment;

import java.util.List;

@EFragment(R.layout.fragment_top)
public class TopCoinsFragment extends TopFragmentBase {

    @Override
    RecyclerView.Adapter getAdapter(List<Player> players) {
        return new PlayersAdapterTopCoins(players);
    }

    @Override
    String getFilter() {
        return ConstantsManager.COINS_FILTER;
    }
}
