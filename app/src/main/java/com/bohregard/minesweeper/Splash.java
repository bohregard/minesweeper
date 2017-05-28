package com.bohregard.minesweeper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.bohregard.minesweeper.util.Utils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;

import java.lang.ref.WeakReference;

/**
 * Created by bohregard on 5/19/2017.
 */

public class Splash extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = Splash.class.getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        //setupGameApi();

        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Utils.hideSystemUI(getWindow().getDecorView());
        }
        new SplashScreenTask(this).execute();
    }

    /*
    ******************************************************************************************
    *   Google Play Game Methods
    ******************************************************************************************
     */

    private GoogleApiClient googleApiClient;
    private boolean resolvingConnectionFailure = false;
    private boolean autoStartSignin = true;
    private boolean signInClicked = false;

    private void setupGameApi() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        googleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connection established");

        Player p = Games.Players.getCurrentPlayer(googleApiClient);
        String displayname;
        if (p == null) {
            Log.w(TAG, "Current player is null!");
            displayname = "???";
        } else {
            displayname = p.getDisplayName();
        }

        Toast.makeText(this, "Hello: " + displayname, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Error signing in...");
        Log.e(TAG, "Connection Result: " + connectionResult.getErrorMessage());
        Log.e(TAG, "Connection Code: " + connectionResult.getErrorCode());
        Log.e(TAG, "Resolution: " + connectionResult.hasResolution());

        if (resolvingConnectionFailure) {
            return;
        }

        if (signInClicked || autoStartSignin) {
            autoStartSignin = false;
            signInClicked = false;
            resolvingConnectionFailure = true;

            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(this, connectionResult.getErrorCode());
                    resolvingConnectionFailure = false;
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended, retrying");
        googleApiClient.connect();
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
