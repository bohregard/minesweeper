package com.bohregard.minesweeper.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bohregard.minesweeper.Main;
import com.bohregard.minesweeper.R;
import com.google.android.gms.games.Games;

/**
 * Created by bohregard on 5/28/2017.
 */

public class MainMenu extends Fragment implements View.OnClickListener {

    private RelativeLayout signIn;
    private RelativeLayout signOut;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main_menu, container, false);
        signIn = (RelativeLayout) v.findViewById(R.id.sign_in_button);
        signIn.setOnClickListener(this);

        signOut = (RelativeLayout) v.findViewById(R.id.sign_out_button);
        signOut.setOnClickListener(this);

        v.findViewById(R.id.start_game).setOnClickListener(this);
        v.findViewById(R.id.achievements).setOnClickListener(this);
        v.findViewById(R.id.leaderboards).setOnClickListener(this);
        v.findViewById(R.id.settings).setOnClickListener(this);

        if (Main.getGoogleApiClient() != null && Main.getGoogleApiClient().isConnected()) {
            signIn.setVisibility(View.GONE);
            signOut.setVisibility(View.VISIBLE);
        }

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Main.googlePlaySignIn();
                break;
            case R.id.sign_out_button:
                Main.googlePlaySignOut();
                signIn.setVisibility(View.VISIBLE);
                signOut.setVisibility(View.GONE);
                break;
            case R.id.start_game:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new MineSweeper(), null)
                        .addToBackStack(null)
                        .commit();
                break;
            case R.id.achievements:
                startActivityForResult(
                        Games.Achievements.getAchievementsIntent(Main.getGoogleApiClient()),
                        0
                );
                break;
            case R.id.leaderboards:
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                        getString(R.string.shared_pref),
                        Context.MODE_PRIVATE);

                startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(
                        Main.getGoogleApiClient()), 0);

//                switch (sharedPreferences.getInt(getString(R.string.game_mode), 0)) {
//                    case 0:
//                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
//                                Main.getGoogleApiClient(),
//                                getString(R.string.leaderboard_easy_mode)),
//                                0);
//                        break;
//                    case 1:
//                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
//                                Main.getGoogleApiClient(),
//                                getString(R.string.leaderboard_medium_mode)),
//                                0);
//                        break;
//                    case 2:
//                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
//                                Main.getGoogleApiClient(),
//                                getString(R.string.leaderboard_hard_mode)),
//                                0);
//                        break;
//                    case 3:
//                        Games.Leaderboards.getAllLeaderboardsIntent()
//                        startActivityForResult(Games.Leaderboards.getLeaderboardIntent(
//                                Main.getGoogleApiClient(),
//                                getString(R.string.leaderboard_easy_mode)),
//                                0);
//                        break;
//                }
                break;
            case R.id.settings:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new Settings(), null)
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }
}
