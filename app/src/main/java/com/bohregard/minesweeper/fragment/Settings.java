package com.bohregard.minesweeper.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bohregard.minesweeper.R;

/**
 * Created by bohregard on 5/28/2017.
 */

public class Settings extends Fragment {

    /*
     ******************************************************************************************
     *   Fragment Methods
     ******************************************************************************************
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_settings, container, false);
        return v;
    }
}
