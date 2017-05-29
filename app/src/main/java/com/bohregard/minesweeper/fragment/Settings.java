package com.bohregard.minesweeper.fragment;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;

import com.bohregard.minesweeper.R;

/**
 * Created by bohregard on 5/28/2017.
 */

public class Settings extends Fragment {

    private static final String TAG = Settings.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private EditText boardWidth;
    private EditText boardHeight;
    private EditText boardMines;
    private int gameMode;

    /*
     ******************************************************************************************
     *   Fragment Methods
     ******************************************************************************************
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        sharedPreferences = getActivity().getSharedPreferences(getString(R.string.shared_pref), Context.MODE_PRIVATE);
        gameMode = sharedPreferences.getInt(getString(R.string.game_mode), 0);

        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        Spinner gameModes = (Spinner) v.findViewById(R.id.game_modes);
        boardWidth = (EditText) v.findViewById(R.id.board_width);
        boardHeight = (EditText) v.findViewById(R.id.board_height);
        boardMines = (EditText) v.findViewById(R.id.mine_number);

        if(gameMode == 3) {
            setCustomFocusable(true);
        } else {
            setCustomFocusable(false);
        }

        gameModes.setSelection(gameMode);
        gameModes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Something");
                Log.d(TAG, "Position: " + position);
                sharedPreferences.edit().putInt(getString(R.string.game_mode), position).apply();

                switch(position) {
                    case 0:
                        setHint(8, 12, 10);
                        break;
                    case 1:
                        setHint(12, 18, 32);
                        break;
                    case 2:
                        setHint(14, 22, 100);
                        break;
                    case 3:
                        break;
                }

                if(position == 3) {
                    setCustomFocusable(true);
                } else {
                    setCustomFocusable(false);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return v;
    }

    private void setHint(int width, int height, int mines) {
        boardWidth.setHint("" + width);
        boardHeight.setHint("" + height);
        boardMines.setHint("" + mines);
    }

    private void setCustomFocusable(boolean focusable) {
            boardWidth.setFocusable(focusable);
            boardHeight.setFocusable(focusable);
            boardMines.setFocusable(focusable);
    }
}
