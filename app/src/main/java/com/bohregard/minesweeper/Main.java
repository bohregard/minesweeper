package com.bohregard.minesweeper;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bohregard.minesweeper.fragment.MainMenu;
import com.bohregard.minesweeper.fragment.MineSweeper;
import com.bohregard.minesweeper.util.Utils;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/**
 * Created by bohregard on 5/28/2017.
 */

public class Main extends Activity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = Main.class.getSimpleName();
    private static InterstitialAd interstitialAd;
    private static long adTimeOut = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "SetupGameApi");
        setupGameApi();

        getFragmentManager().beginTransaction()
                .add(R.id.fragment, new MineSweeper(), "MINE")
                .commit();

        getFragmentManager().beginTransaction()
                .add(R.id.fragment, new MainMenu(), "MENU")
                .addToBackStack(null)
                .commit();
        setupAds();
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "FManager Size: " + getFragmentManager().getBackStackEntryCount());
        if (getFragmentManager().findFragmentByTag("MINE").isVisible()
                && getFragmentManager().getBackStackEntryCount() == 0) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fragment, new MainMenu(), "MENU")
                    .addToBackStack(null)
                    .commit();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Result...");
        Log.d(TAG, "Result Code: " + resultCode); //0 failed? -1 passed?
        Log.d(TAG, "Request Code: " + requestCode); //4 connection code?
        Log.d(TAG, "Intent Data: " + data);
        googleApiClient.connect();
        if (resultCode == RESULT_OK) {
            findViewById(R.id.sign_in_button).setVisibility(View.GONE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.achievements).setVisibility(View.VISIBLE);
            findViewById(R.id.leaderboards).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Utils.hideSystemUI(getWindow().getDecorView());
        }
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "Restoring instance...");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Saving State!");
        outState.putInt("Something", 1);
    }

    /*
     ******************************************************************************************
     *   Ad Methods
     ******************************************************************************************
     */

    /**
     * Build the ad request
     *
     * @return adRequest
     */
    private AdRequest adBuilder() {
        return new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("A91891F9A4FC34214AD2B99ED921E03A") // Pixel
                .addTestDevice("B166285402A23C59DCF55A1F254983B6") // Pixel O Preview
                .addTestDevice("2D54046DE254BD2B4FC1A8619316F2D4") // Samsung
                .build();
    }

    /**
     * Request a new interstitial ad
     */
    private void requestNewAd() {
        AdRequest adRequest = adBuilder();
        interstitialAd.loadAd(adRequest);
    }

    /**
     * Setup the ads
     */
    private void setupAds() {
        MobileAds.initialize(getApplicationContext(), getString(R.string.banner_ad_unit_id));

        AdView mAdView = (AdView) findViewById(R.id.adView);

        AdRequest adRequest = adBuilder();
        mAdView.loadAd(adRequest);

        interstitialAd = new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.inters_ad_unit_id));
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                requestNewAd();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "Failed to load ad...");
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "Ad Loaded");
            }
        });

        requestNewAd();
    }

    /**
     * Show an interstitial ad every 5 minutes based on the app activity
     */
    public static void showInterstitialAd() {
        if (adTimeOut == 0) {
            adTimeOut = System.currentTimeMillis();
        }
        Log.d(TAG, "Time: " + System.currentTimeMillis());

        if (System.currentTimeMillis() - adTimeOut > (1000 * 60 * 5)) {
            if (interstitialAd.isLoaded()) {
                interstitialAd.show();
            }
            adTimeOut = System.currentTimeMillis();
        }
    }

    /*
     ******************************************************************************************
     *   Google Play Game Methods
     ******************************************************************************************
     */

    private static GoogleApiClient googleApiClient;
    private static boolean resolvingConnectionFailure = false;
    private static boolean autoStartSignInFlow = true;
    private static boolean signInClicked = false;

    public static GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public static void setSignInClicked(boolean clicked) {
        signInClicked = clicked;
    }

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

        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
        findViewById(R.id.achievements).setVisibility(View.VISIBLE);
        findViewById(R.id.leaderboards).setVisibility(View.VISIBLE);

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

        if (autoStartSignInFlow || signInClicked) {
            autoStartSignInFlow = false;
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

}
