package com.bohregard.minesweeper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Window;
import android.view.WindowManager;

import com.bohregard.minesweeper.util.Utils;

import java.lang.ref.WeakReference;

/**
 * Created by bohregard on 5/19/2017.
 */

public class Splash extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        Utils.hideSystemUI(getWindow().getDecorView());
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
            if(splashActivity.get() != null) {
                splashActivity.get().startActivity(
                        new Intent(splashActivity.get().getApplicationContext(), MineSweeper.class));
            }
        }
    }
}
