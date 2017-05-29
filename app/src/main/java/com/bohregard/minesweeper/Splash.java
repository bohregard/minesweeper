package com.bohregard.minesweeper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.bohregard.minesweeper.util.Utils;

import java.lang.ref.WeakReference;

/**
 * Created by bohregard on 5/19/2017.
 */

public class Splash extends Activity {

    private static final String TAG = Splash.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

//        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
//        if (tabletSize) {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        } else {
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Utils.hideSystemUI(getWindow().getDecorView());
        }
        new SplashScreenTask(this).execute();
    }

    static class SplashScreenTask extends AsyncTask<Void, Void, Void> {

        private WeakReference<Context> splashActivity;

        SplashScreenTask(Context splash) {
            splashActivity = new WeakReference<>(splash);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for (int i = 0; i < 5; i++) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (splashActivity.get() != null) {
                splashActivity.get().startActivity(
                        new Intent(splashActivity.get().getApplicationContext(), Main.class));
            }
        }
    }
}
