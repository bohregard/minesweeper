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
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = Main.class.getSimpleName();
    private static InterstitialAd interstitialAd;
    private static long adTimeOut = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

        setupAds();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "RESULT!");
        Log.d(TAG, "Request Code: " + requestCode);
        Log.d(TAG, "Result Int: " + resultCode);
        Log.d(TAG, "Intent: " + data);
        googleApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();

        //todo check that the user does not want to sign in ever again...
        setupGameApi();
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
    private boolean resolvingConnectionFailure = false;
    private boolean autoStartSignInFlow = true;
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
        findViewById(R.id.sign_in_button).setVisibility(View.GONE);
        findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signInClicked = true;
                googleApiClient.connect();
                break;
            case R.id.sign_out_button:
                signInClicked = false;
                Games.signOut(googleApiClient);
                googleApiClient.disconnect();
                findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_out_button).setVisibility(View.GONE);
                break;
        }
    }

    /*
     ******************************************************************************************
     *   Public Methods
     ******************************************************************************************
     */

    public static GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }
}
