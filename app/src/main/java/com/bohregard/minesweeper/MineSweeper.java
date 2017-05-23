package com.bohregard.minesweeper;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.bohregard.minesweeper.util.AutoResizeTextView;
import com.bohregard.minesweeper.util.SquareBoard;
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

import java.util.Random;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/**
 * todo: if no mines left, disable longclicking
 * todo: full screen
 * todo: back button
 * Created by awtod on 5/18/2017.
 */

public class MineSweeper extends Activity implements
        View.OnClickListener,
        View.OnLongClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MineSweeper.class.getSimpleName();
    private static int COLUMN_COUNT = 12;
    private static int ROW_COUNT = 18;
    private static int MINES = (int) Math.floor((COLUMN_COUNT * ROW_COUNT) * .15);
    //    private static final int MINES = (int) Math.floor((ROW_COUNT*ROW_COUNT)*.40);
    private static int[][] mineLocations;
    private static boolean configChange = false;

    private static final int MINE = 10;
    private static final int MASK = 20;
    private static final int FLAG_SET = 40;

    private static int minesLeft;
    private static long adTimeOut = 0;

    private SquareBoard squareBoard;
    private TextView minesLeftView;
    private Chronometer timeView;
    private SoundPool sp;
    private int clickSound;
    private int flagSound;
    private int mineSound;

    private SharedPreferences sharedPreferences;

    private InterstitialAd interstitialAd;
    private GoogleApiClient googleApiClient;

    /*
    ******************************************************************************************
    *   Ratios
    ******************************************************************************************
     */
    private static final float PIXEL_RATIO = (180f / 299f);


    /*
    ******************************************************************************************
    *   Activity Methods
    ******************************************************************************************
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_sweeper);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        if (savedInstanceState != null) {
            Log.d(TAG, "Saved instance restoring...");
        }

        setupAds();
        setupGameApi();
        buildSounds();
        buildBoard();
        setupBoard();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "Result of activity!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Utils.hideSystemUI(getWindow().getDecorView());
        }
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
        timeView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timeView.stop();
        Log.d(TAG, "On Pause");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "New Configuration!");
        configChange = true;
        squareBoard.removeAllViews();

        printArray();
        setupBoard();
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
    private void showInterstitialAd() {
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
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, connectionResult.getErrorCode());
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Connection suspended, retrying");
        googleApiClient.connect();
    }

    /*
    ******************************************************************************************
    *   Private Methods
    ******************************************************************************************
     */

    /**
     * Use a sound pool to build our sounds so that we can play multiple sounds without issues
     */
    private void buildSounds() {
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        clickSound = sp.load(this, R.raw.click, 1);
        flagSound = sp.load(this, R.raw.flag, 1);
        mineSound = sp.load(this, R.raw.mine, 1);
    }

    /**
     * We build the board based off of configuration and settings
     */
    private void buildBoard() {
        boolean tabletSize = getResources().getBoolean(R.bool.isTablet);
        if (tabletSize) {
            COLUMN_COUNT = 20;
            ROW_COUNT = 12;
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    /**
     * Set the board configuration up. Generate an empty array and fill it with the amount of
     * mines set. Shuffle the mines, and calculate the board numbers.
     */
    private void setupBoard() {
        squareBoard = (SquareBoard) findViewById(R.id.grid);
        minesLeftView = (TextView) findViewById(R.id.mines_left);

        minesLeft = MINES;
        setMineLeftText(minesLeft);

        // If the mineLocations are null, we need to build the board from scratch
        if (mineLocations == null) {
            mineLocations = new int[ROW_COUNT][COLUMN_COUNT];

            for (int i = 0; i < ROW_COUNT; i++) {
                for (int j = 0; j < COLUMN_COUNT; j++) {
                    if (getIndex(i, j) < MINES) {
                        mineLocations[i][j] = MINE;
                    }
                }
            }
            shuffle(mineLocations);
        }

        squareBoard.setRowCount(ROW_COUNT);
        squareBoard.setColumnCount(COLUMN_COUNT);

        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                setupChildView(row, column);
            }
        }

        //todo change this to when the user presses the board
        timeView = (Chronometer) findViewById(R.id.time);
        timeView.start();
        configChange = false;
    }

    /**
     * Instantiate each view
     *
     * @param row    integer
     * @param column integer
     */
    private void setupChildView(int row, int column) {
        Log.v(TAG, "CURRENT CENTER: " + row + ", " + column);
        Log.v(TAG, "Mine? " + mineLocations[row][column]);

        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.height = 0;
        layoutParams.width = 0;
        layoutParams.columnSpec = GridLayout.spec(column, 1f);
        layoutParams.rowSpec = GridLayout.spec(row, 1f);

        AutoResizeTextView mine = new AutoResizeTextView(this);
        mine.setTextSize(500);
        mine.setGravity(Gravity.CENTER);
        setViewBackgroundDrawable(mine, R.drawable.mine_unclicked);
        mine.setLayoutParams(layoutParams);
        mine.setOnClickListener(this);
        mine.setOnLongClickListener(this);
        mine.setTag(new int[]{row, column});
        mine.setId(getIndex(row, column));
        mine.setSoundEffectsEnabled(false);

        if (!configChange) {
            if (mineLocations[row][column] != MINE) {
                search(row, column, MINE);
            } else {
                mine.setText("M");
                mine.setTextColor(ContextCompat.getColor(this, R.color.transparent));
            }
        }

        if (mineLocations[row][column] >= MASK) {
            showSquare(mine, new int[]{row, column});
        }

        squareBoard.addView(mine);
    }

    private void search(int row, int column, int searchParam) {
        int startPosX = (row - 1 < 0) ? row : row - 1;
        int startPosY = (column - 1 < 0) ? column : column - 1;
        int endPosX = (row + 1 >= ROW_COUNT) ? row : row + 1;
        int endPosY = (column + 1 >= COLUMN_COUNT) ? column : column + 1;

        Log.v(TAG, "Start: " + startPosX + ", " + startPosY);
        Log.v(TAG, "End: " + endPosX + ", " + endPosY);

        for (int rowNum = startPosX; rowNum <= endPosX; rowNum++) {
            for (int colNum = startPosY; colNum <= endPosY; colNum++) {
                // All the neighbors will be grid[rowNum][colNum]
                if (searchParam == 0) {
                    if (mineLocations[rowNum][colNum] == searchParam) {
                        AutoResizeTextView v = (AutoResizeTextView) squareBoard.findViewById(getIndex(rowNum, colNum));
                        if (v.getText() == ("F") || v.getText() == "" || v.getText() == null) {
                            showSquare(v, new int[]{rowNum, colNum});
                            search(rowNum, colNum, 0);
                        }
                    } else {
                        AutoResizeTextView v = (AutoResizeTextView) squareBoard.findViewById(getIndex(rowNum, colNum));
                        if (v.getText() == "" || v.getText() == null) {
                            showSquare(v, new int[]{rowNum, colNum});
                        }
                    }
                } else {
                    Log.v(TAG, "loc: " + rowNum + ", " + colNum);
                    if (mineLocations[rowNum][colNum] == searchParam) {
                        mineLocations[row][column] += 1;
                    }
                }
            }
        }
    }

    private void showSquare(AutoResizeTextView mine, int[] pos) {
        if (!configChange) {
            setMineText(mine, mineLocations[pos[0]][pos[1]]);
            mineLocations[pos[0]][pos[1]] = mineLocations[pos[0]][pos[1]] + MASK;
        } else {
            setMineText(mine, mineLocations[pos[0]][pos[1]] - MASK);
        }
        Log.v(TAG, "Show the square at: " + pos[0] + ", " + pos[1]);
        mine.setTypeface(null, Typeface.BOLD);
        setViewBackgroundDrawable(mine, R.drawable.mine_clicked);
        mine.setOnClickListener(null);
        mine.setOnLongClickListener(null);
        switch (mineLocations[pos[0]][pos[1]]) {
            case 0:
            case 20:
                mine.setTextColor(ContextCompat.getColor(this, R.color.transparent));
                break;
            case 1:
            case 21:
                mine.setTextColor(Color.BLUE);
                break;
            case 2:
            case 22:
                mine.setTextColor(Color.GREEN);
                break;
            case 3:
            case 23:
                mine.setTextColor(Color.RED);
                break;
            case 4:
            case 24:
                mine.setTextColor(Color.parseColor("#000099"));
                break;
            case 5:
            case 25:
                mine.setTextColor(Color.parseColor("#DEB887"));
                break;
            case MINE:
            case MINE + MASK:
                setViewBackgroundDrawable(mine, R.drawable.mine_border);
                break;
        }
    }

    /**
     * Utility method to shuffle the array
     * @param a 2d array
     */
    private void shuffle(int[][] a) {
        Random random = new Random();

        for (int i = a.length - 1; i > 0; i--) {
            for (int j = a[i].length - 1; j > 0; j--) {
                int m = random.nextInt(i + 1);
                int n = random.nextInt(j + 1);

                int temp = a[i][j];
                a[i][j] = a[m][n];
                a[m][n] = temp;
            }
        }
    }

    /**
     * Returns an index from the 2d array
     * @param row integer
     * @param column integer
     * @return an index integer
     */
    private int getIndex(int row, int column) {
        return row * COLUMN_COUNT + column;
    }

    private void toggleFlag(AutoResizeTextView mine) {
        if (mine.getText() == "F") {
            minesLeft += 1;
            setMineLeftText(minesLeft);
            setViewBackgroundDrawable(mine, R.drawable.mine_unclicked);
            mine.setTextColor(ContextCompat.getColor(this, R.color.transparent));
            int[] pos = (int[]) mine.getTag();
            setMineText(mine, mineLocations[pos[0]][pos[1]]);
            mine.setOnClickListener(this);
        } else {
            minesLeft -= 1;
            setMineLeftText(minesLeft);
            setViewBackgroundDrawable(mine, R.drawable.mine_flag);
            mine.setText("F");
            mine.setTextColor(Color.parseColor("#00000000"));
            mine.setOnClickListener(null);
            if (minesLeft == 0) {
                //check board
                Log.d(TAG, "NO MINES LEFT");
                checkBoard();
            }
        }
    }

    private void showBoard(int[] explodedMine) {
        for (int i = 0; i < COLUMN_COUNT * ROW_COUNT; i++) {
            AutoResizeTextView v = (AutoResizeTextView) squareBoard.findViewById(i);
            int[] pos = (int[]) v.getTag();
            int mineLocation = mineLocations[pos[0]][pos[1]];
            if (explodedMine == pos) {
                setViewBackgroundDrawable(v, R.drawable.mine_exploded);
                v.setOnClickListener(null);
                v.setOnLongClickListener(null);
            } else if ((mineLocation != MINE && v.getText() == "F")) {
                setViewBackgroundDrawable(v, R.drawable.mine_wrong);
            } else if (!(mineLocation == MINE &&
                    v.getText() == "F" ||
                    mineLocation >= MASK)) {
                showSquare(v, pos);
            }
        }
    }

    private void checkBoard() {
        int count = 0;
        for (int i = 0; i < COLUMN_COUNT * ROW_COUNT; i++) {
            AutoResizeTextView v = (AutoResizeTextView) squareBoard.findViewById(i);
            int[] pos = (int[]) v.getTag();
            if (mineLocations[pos[0]][pos[1]] == MINE && v.getText() == "F") {
                Log.d(TAG, "Mine success...");
                count++;
            }
        }
        if (count == MINES) {
            Log.d(TAG, "GAME WON!");
            timeView.stop();
            Toast.makeText(this,
                    "Game WON! Time: " + timeView.getText(),
                    Toast.LENGTH_SHORT).show();
            showBoard(null);
            showInterstitialAd();
        } else {
            Log.e(TAG, "Game still has uncovered mines");
        }
    }

    /**
     * Utility method to print the minelocations
     */
    private void printArray() {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                Log.i(TAG + "_MINELOCATIONS", "MINESLOCATIONS: " + mineLocations[row][column]);
            }
        }
    }

    private void setMineLeftText(int minesLeft) {
        minesLeftView.setText(
                String.format(
                        getString(R.string.mines_left),
                        minesLeft)
        );
    }

    private void setMineText(TextView view, int num) {
        view.setText(
                String.format(
                        getString(R.string.mine_text),
                        num)
        );
    }

    private void setViewBackgroundDrawable(View view, int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(ContextCompat.getDrawable(this, drawableId));
        } else {
            view.setBackgroundDrawable(ContextCompat.getDrawable(this, drawableId));
        }
    }

    /*
    ******************************************************************************************
    *   Public Methods
    ******************************************************************************************
     */

    public void resetGame(View v) {
        squareBoard.removeAllViews();
        timeView.setBase(SystemClock.elapsedRealtime());
        mineLocations = null;
        setupBoard();
    }

    @Override
    public void onClick(View v) {
        AutoResizeTextView mine = (AutoResizeTextView) v;
        int[] pos = (int[]) v.getTag();
        Log.d(TAG, "Position: " +
                pos[0] + ", " + pos[1] +
                " with id: " + v.getId() +
                " Mine Locations: " + mineLocations[pos[0]][pos[1]]);
        if (mineLocations[pos[0]][pos[1]] == MINE) {
            Log.e(TAG, "Game Ends! Show the board...");
            timeView.stop();
            showBoard(pos);
            sp.play(mineSound, 1, 1, 0, 0, 1);
            Games.Achievements.unlock(googleApiClient, getString(R.string.babys_first_mine));
            showInterstitialAd();
        } else if (mineLocations[pos[0]][pos[1]] == 0) {
            search(pos[0], pos[1], 0);
            sp.play(clickSound, 1, 1, 0, 0, 1);
            printArray();
        } else {
            showSquare(mine, pos);
            sp.play(clickSound, 1, 1, 0, 0, 1);
//            printArray();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        sp.play(flagSound, 1, 1, 0, 0, 1);
        toggleFlag((AutoResizeTextView) v);
        return true;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(false);
    }
}