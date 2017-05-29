package com.bohregard.minesweeper.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bohregard.minesweeper.Main;
import com.bohregard.minesweeper.R;
import com.bohregard.minesweeper.util.SquareBoard;
import com.google.android.gms.games.Games;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by bohregard on 5/28/2017.
 */

public class MineSweeper extends Fragment implements
        View.OnClickListener,
        View.OnLongClickListener {

    private static final String TAG = MineSweeper.class.getSimpleName();
    private boolean isChronometerRunning = false;

    /*
     ******************************************************************************************
     *   Fragment Methods
     ******************************************************************************************
     */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mine_sweeper, container, false);
        resetButton = (ImageButton) v.findViewById(R.id.smiley_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isChronometerRunning) {
                    timeView.stop();
                    timeView.setBase(SystemClock.elapsedRealtime());
                    isChronometerRunning = false;
                }
                squareBoard.removeAllViews();
                gameBoardArray = null;
                setupBoard();
            }
        });
        squareBoard = (SquareBoard) v.findViewById(R.id.grid);
        timeView = (Chronometer) v.findViewById(R.id.time);
        minesLeftView = (TextView) v.findViewById(R.id.mines_left);

        buildSounds();
        setupBoard();
        return v;
    }

    /*
     ******************************************************************************************
     *   Game Board Methods
     ******************************************************************************************
     */

    private SquareBoard squareBoard;
    private Chronometer timeView;
    private TextView minesLeftView;
    private ImageButton resetButton;
    private int[] gameBoardArray = null;
    private int minesLeft = 0;
    private SoundPool sp;
    private int clickSound;
    private int flagSound;
    private int mineSound;
    private int gameMode;

    private static int COLUMN_COUNT = 12;
    private static int ROW_COUNT = 18;
    private int NUM_MINES = 32;

    private final int MINE_MASK = 10;
    private final int FLAG_MASK = 11;
    private final int MASK = 30;

    /**
     * Use a sound pool to build our sounds so that we can play multiple sounds without issues
     */
    private void buildSounds() {
        sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        clickSound = sp.load(getActivity(), R.raw.click, 1);
        flagSound = sp.load(getActivity(), R.raw.flag, 1);
        mineSound = sp.load(getActivity(), R.raw.mine, 1);
    }

    private void setDifficulty(int mode, SharedPreferences sharedPreferences) {
        switch (mode) {
            case 0: //Easy
                COLUMN_COUNT = 8;
                ROW_COUNT = 12;
                NUM_MINES = 10;
                break;
            case 1: //Medium
                COLUMN_COUNT = 12;
                ROW_COUNT = 18;
                NUM_MINES = 32;
                break;
            case 2: //Hard
                COLUMN_COUNT = 14;
                ROW_COUNT = 22;
                NUM_MINES = 100;
                break;
            case 3: //Custom
                COLUMN_COUNT = sharedPreferences.getInt(getString(R.string.custom_row), 12);
                ROW_COUNT = sharedPreferences.getInt(getString(R.string.custom_column), 18);
                NUM_MINES = sharedPreferences.getInt(getString(R.string.custom_mine), 32);
                break;
            default:
                COLUMN_COUNT = 12;
                ROW_COUNT = 18;
                NUM_MINES = 32;
                break;
        }
    }

    /**
     * Set the board configuration up. Generate an empty array and fill it with the amount of
     * mines set. Shuffle the mines, and calculate the board numbers.
     */
    private void setupBoard() {
        setViewBackgroundDrawable(resetButton, R.drawable.smiley_regular);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.shared_pref), MODE_PRIVATE
        );

        gameMode = sharedPreferences.getInt(getString(R.string.game_mode), 0);

        setDifficulty(gameMode, sharedPreferences);

        // If the boardArray are null, we need to build the board from scratch
        if (gameBoardArray == null) {
            gameBoardArray = new int[ROW_COUNT * COLUMN_COUNT];

            for (int i = 0; i < ROW_COUNT * COLUMN_COUNT; i++) {
                if (i < NUM_MINES) {
                    gameBoardArray[i] = MINE_MASK;
                }
            }
            minesLeft = NUM_MINES;
            setMineLeftText(minesLeft);
            shuffle(gameBoardArray);
        }

        squareBoard.setRowCount(ROW_COUNT);
        squareBoard.setColumnCount(COLUMN_COUNT);

        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                setupChildView(row, column);
                if (gameBoardArray[getIndex(row, column)] != MINE_MASK) {
                    mineSearch(row, column, MINE_MASK);
                }
            }
        }
    }

    /**
     * Setup the individual squares
     *
     * @param row    int
     * @param column int
     */
    private void setupChildView(int row, int column) {
        GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();
        layoutParams.height = 0;
        layoutParams.width = 0;
        layoutParams.columnSpec = GridLayout.spec(column, 1f);
        layoutParams.rowSpec = GridLayout.spec(row, 1f);

        TextView mine = new TextView(getActivity());
        mine.setTextSize(50);
        mine.setGravity(Gravity.CENTER);
        setViewBackgroundDrawable(mine, R.drawable.mine_unclicked);
        mine.setLayoutParams(layoutParams);
        mine.setOnClickListener(this);
        mine.setOnLongClickListener(this);
        mine.setTag(new int[]{row, column});
        mine.setId(getIndex(row, column));
        mine.setSoundEffectsEnabled(false);

        squareBoard.addView(mine);
    }

    private void mineSearch(int row, int column, int searchParam) {
        int startPosX = (row - 1 < 0) ? row : row - 1;
        int startPosY = (column - 1 < 0) ? column : column - 1;
        int endPosX = (row + 1 >= ROW_COUNT) ? row : row + 1;
        int endPosY = (column + 1 >= COLUMN_COUNT) ? column : column + 1;

        for (int rowNum = startPosX; rowNum <= endPosX; rowNum++) {
            for (int colNum = startPosY; colNum <= endPosY; colNum++) {
                if (gameBoardArray[getIndex(rowNum, colNum)] == searchParam) {
                    gameBoardArray[getIndex(row, column)] += 1;
                }
            }
        }
    }

    private void zeroReveal(int row, int column) {
        int startPosX = (row - 1 < 0) ? row : row - 1;
        int startPosY = (column - 1 < 0) ? column : column - 1;
        int endPosX = (row + 1 >= ROW_COUNT) ? row : row + 1;
        int endPosY = (column + 1 >= COLUMN_COUNT) ? column : column + 1;

        for (int rowNum = startPosX; rowNum <= endPosX; rowNum++) {
            for (int colNum = startPosY; colNum <= endPosY; colNum++) {
                TextView v = (TextView) squareBoard.findViewById(getIndex(rowNum, colNum));
                if (gameBoardArray[getIndex(rowNum, colNum)] == 0) {
                    showSquare(v, getIndex(rowNum, colNum));
                    zeroReveal(rowNum, colNum);
                } else if (gameBoardArray[getIndex(rowNum, colNum)] < 10) {
                    showSquare(v, getIndex(rowNum, colNum));
                }
            }
        }
    }

    private void showSquare(TextView mine, int index) {
        gameBoardArray[index] += MASK;
        //setMineText(mine, gameBoardArray[index]);
        mine.setTypeface(null, Typeface.BOLD);
        //setViewBackgroundDrawable(mine, R.drawable.mine_clicked);
        mine.setOnClickListener(null);
        mine.setOnLongClickListener(null);

        switch (gameBoardArray[index]) {
            case 0:
            case MASK:
                setViewBackgroundDrawable(mine, R.drawable.ic_0);
                break;
            case 1:
            case 1 + MASK:
                setViewBackgroundDrawable(mine, R.drawable.ic_1);
                break;
            case 2:
            case 2 + MASK:
                setViewBackgroundDrawable(mine, R.drawable.ic_2);
                break;
            case 3:
            case 3 + MASK:
                setViewBackgroundDrawable(mine, R.drawable.ic_3);
                break;
            case 4:
            case 4 + MASK:
                setViewBackgroundDrawable(mine, R.drawable.ic_4);
                break;
            case 5:
            case 5 + MASK:
                setViewBackgroundDrawable(mine, R.drawable.ic_5);
                break;
            case 6:
            case 6 + MASK:
                setViewBackgroundDrawable(mine, R.drawable.ic_6);
                break;
            case 7:
            case 7 + MASK:
                setViewBackgroundDrawable(mine, R.drawable.ic_7);
                break;
            case 8:
            case 8 + MASK:
                setViewBackgroundDrawable(mine, R.drawable.ic_8);
                break;
            case MINE_MASK:
            case MINE_MASK + MASK:
                mine.setTextColor(ContextCompat.getColor(getActivity(), R.color.transparent));
                setViewBackgroundDrawable(mine, R.drawable.mine_border);
                break;
        }
    }

    private void showBoard(int index) {
        for (int i = 0; i < COLUMN_COUNT * ROW_COUNT; i++) {
            TextView v = (TextView) squareBoard.findViewById(i);
            int[] pos = (int[]) v.getTag();
            int value = gameBoardArray[i];
            if (value <= MASK) {
                if (index == i) {
                    setViewBackgroundDrawable(v, R.drawable.mine_exploded);
                    v.setOnClickListener(null);
                    v.setOnLongClickListener(null);
                } else if (value > MINE_MASK && value < MINE_MASK + FLAG_MASK) {
                    setViewBackgroundDrawable(v, R.drawable.mine_wrong);
                    achievementUnlock(R.string.achievement_not_quite_a_mine);
                } else if (value != MINE_MASK + FLAG_MASK) {
                    showSquare(v, getIndex(pos));
                }
            }
        }
    }

    /*
     ******************************************************************************************
     *   Game Methods
     ******************************************************************************************
     */

    private void gameLose(int index) {
        timeView.stop();
        isChronometerRunning = false;

        setViewBackgroundDrawable(resetButton, R.drawable.smiley_loss);
        achievementUnlock(R.string.achievement_babys_first_mine);
        if (checkBoard() > 0) {
            achievementIncrement(R.string.achievement_flagged_10_mines, checkBoard());
        }
        long time1 = SystemClock.elapsedRealtime();
        showBoard(index);
        long time2 = SystemClock.elapsedRealtime();
        Log.e(TAG, "Lose Time: " + (time2 - time1));
        Main.showInterstitialAd();
    }

    private void gameWin() {
        timeView.stop();
        isChronometerRunning = false;

        achievementUnlock(R.string.achievement_a_whole_new_world);
        achievementIncrement(R.string.achievement_baby_bomb_sweeper, 1);
        achievementIncrement(R.string.achievement_amateur_bomb_analyst, 1);
        achievementIncrement(R.string.achievement_junior_bomb_detective, 1);
        achievementIncrement(R.string.achievement_bomb_senpai, 1);
        achievementIncrement(R.string.achievement_flagged_10_mines, NUM_MINES);

        switch (gameMode) {
            case 0:
                leaderBoardSubmit(R.string.leaderboard_easy_mode,
                        SystemClock.elapsedRealtime() - timeView.getBase());
                break;
            case 1:
                leaderBoardSubmit(R.string.leaderboard_medium_mode,
                    SystemClock.elapsedRealtime() - timeView.getBase());
                break;
            case 2:
                leaderBoardSubmit(R.string.leaderboard_hard_mode,
                    SystemClock.elapsedRealtime() - timeView.getBase());
                break;
        }

        if (SystemClock.elapsedRealtime() - timeView.getBase() < 60000) {
            achievementUnlock(R.string.achievement_fast_sweeper);
        }
        Toast.makeText(getActivity(),
                "Game WON! Time: " + timeView.getText(),
                Toast.LENGTH_SHORT).show();
        showBoard(-1);
        Main.showInterstitialAd();
    }

    private int checkBoard() {
        int count = 0;
        for (int i = 0; i < ROW_COUNT * COLUMN_COUNT; i++) {
            if (gameBoardArray[i] == MINE_MASK + FLAG_MASK) {
                count++;
            }
        }

        return count;
    }

    //todo, Refactor flag toggling
    private void toggleFlag(TextView mine) {

        int[] pos = (int[]) mine.getTag();
        int value = gameBoardArray[getIndex(pos)];

        if (value >= 11 && value <= 21) {
            minesLeft += 1;
            setMineLeftText(minesLeft);
            setViewBackgroundDrawable(mine, R.drawable.mine_unclicked);
            mine.setTextColor(ContextCompat.getColor(getActivity(), R.color.transparent));
            setMineText(mine, gameBoardArray[getIndex(pos)]);
            gameBoardArray[getIndex(pos)] = value - FLAG_MASK;
            mine.setOnClickListener(this);
        } else {
            minesLeft -= 1;
            setMineLeftText(minesLeft);
            setViewBackgroundDrawable(mine, R.drawable.mine_flag);
            gameBoardArray[getIndex(pos)] = value + FLAG_MASK;
            mine.setOnClickListener(null);
            if (minesLeft == 0) {
                //check board
                if (checkBoard() == NUM_MINES) {
                    gameWin();
                }
            }
        }
    }

    /*
     ******************************************************************************************
     *   Click Methods
     ******************************************************************************************
     */

    @Override
    public void onClick(View v) {
        if(!isChronometerRunning) {
            timeView.setBase(SystemClock.elapsedRealtime());
            timeView.start();
            isChronometerRunning = true;
        }
        TextView mine = (TextView) v;
        final int[] pos = (int[]) v.getTag();
        int value = gameBoardArray[getIndex(pos)];
        switch (value) {
            case MINE_MASK:
                gameLose(getIndex(pos));
                sp.play(mineSound, 1, 1, 0, 0, 1);
                break;
            case 0:
                long time1 = SystemClock.elapsedRealtime();
                zeroReveal(pos[0], pos[1]);
                long time2 = SystemClock.elapsedRealtime();
                Log.e(TAG, "Time: " + (time2 - time1));
                sp.play(clickSound, 1, 1, 0, 0, 1);
                break;
            default:
                showSquare(mine, getIndex(pos));
                sp.play(clickSound, 1, 1, 0, 0, 1);
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        toggleFlag((TextView) v);
        sp.play(flagSound, 1, 1, 0, 0, 1);
        return true;
    }

    /*
     ******************************************************************************************
     *   Game Board Utility Methods
     ******************************************************************************************
     */

    /**
     * Utility method to set a background drawable
     *
     * @param view       view
     * @param drawableId int
     */
    private void setViewBackgroundDrawable(View view, int drawableId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(ContextCompat.getDrawable(getActivity(), drawableId));
        } else {
            view.setBackgroundDrawable(ContextCompat.getDrawable(getActivity(), drawableId));
        }
    }

    /**
     * Sets the TextView text
     *
     * @param view view
     * @param num  int
     */
    private void setMineText(TextView view, int num) {
        if (num - MASK > 0) {
            num = num - MASK;
        }
        view.setText(
                String.format(
                        getString(R.string.mine_text),
                        num)
        );
    }

    /**
     * Set the amount of mines remaining. This may not be accurate since the user can place mines
     * where ever.
     *
     * @param minesLeft int
     */
    private void setMineLeftText(int minesLeft) {
        minesLeftView.setText(
                String.format(
                        getString(R.string.mines_left),
                        minesLeft)
        );
    }

    /**
     * Utility method to shuffle the array
     *
     * @param a array
     */
    private void shuffle(int[] a) {
        Random random = new Random();

        for (int i = a.length - 1; i > 0; i--) {
            int m = random.nextInt(i + 1);
            int temp = a[i];
            a[i] = a[m];
            a[m] = temp;
        }
    }

    /**
     * Returns an index from the 2d array
     *
     * @param row    integer
     * @param column integer
     * @return an index integer
     */
    private int getIndex(int row, int column) {
        //Log.d(TAG, "Index: " + row * COLUMN_COUNT + column);
        return row * COLUMN_COUNT + column;
    }

    private int getIndex(int[] pos) {
        return pos[0] * COLUMN_COUNT + pos[1];
    }

    /**
     * Utility method to print the minelocations
     */
    private void printArray() {
        for (int i = 0; i < ROW_COUNT * COLUMN_COUNT; i++) {
            Log.i(TAG + "_MINELOCATIONS", "BOARD_ARRAY: " + gameBoardArray[i]);
        }
    }

    /*
     ******************************************************************************************
     *   Google Play Game Services Utility Methods
     ******************************************************************************************
     */

    private void achievementUnlock(int id) {
        if (Main.getGoogleApiClient() != null && Main.getGoogleApiClient().isConnected()) {
            Games.Achievements.unlock(Main.getGoogleApiClient(), getString(id));
        }
    }

    private void achievementIncrement(int id, int increment) {
        if (Main.getGoogleApiClient() != null && Main.getGoogleApiClient().isConnected()) {
            Games.Achievements.increment(Main.getGoogleApiClient(), getString(id), increment);
        }
    }

    private void leaderBoardSubmit(int id, long time) {
        if (Main.getGoogleApiClient() != null && Main.getGoogleApiClient().isConnected()) {
            Games.Leaderboards.submitScore(Main.getGoogleApiClient(), getString(id), time);
        }
    }
}
