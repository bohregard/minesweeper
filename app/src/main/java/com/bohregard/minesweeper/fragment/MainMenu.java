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
 * todo: pause the time
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
        } else {
            v.findViewById(R.id.achievements).setVisibility(View.GONE);
            v.findViewById(R.id.leaderboards).setVisibility(View.GONE);
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
                Main.getGoogleApiClient().connect();
                Main.setSignInClicked(true);
                break;
            case R.id.sign_out_button:
                Games.signOut(Main.getGoogleApiClient());
                Main.getGoogleApiClient().disconnect();
                getActivity().findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                getActivity().findViewById(R.id.sign_out_button).setVisibility(View.GONE);
                getActivity().findViewById(R.id.achievements).setVisibility(View.GONE);
                getActivity().findViewById(R.id.leaderboards).setVisibility(View.GONE);
                signIn.setVisibility(View.VISIBLE);
                signOut.setVisibility(View.GONE);
                break;
            case R.id.start_game:
                SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                        getString(R.string.shared_pref),
                        Context.MODE_PRIVATE);
                if(sharedPreferences.getBoolean(getString(R.string.tutorial), false)) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment, new Tutorial(), "TUTORIAL")
                            .addToBackStack(null)
                            .commit();
                } else {
                    getFragmentManager().popBackStack();
                }
                break;
            case R.id.achievements:
                startActivityForResult(
                        Games.Achievements.getAchievementsIntent(Main.getGoogleApiClient()),
                        0
                );
                break;
            case R.id.leaderboards:
                startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(
                        Main.getGoogleApiClient()), 0);
                break;
            case R.id.settings:
                getFragmentManager().beginTransaction()
                        .replace(R.id.fragment, new Settings(), "SETTINGS")
                        .addToBackStack(null)
                        .commit();
                break;
        }
    }
}
