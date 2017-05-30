package com.bohregard.minesweeper.fragment;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.graphics.Color;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bohregard.minesweeper.Main;
import com.bohregard.minesweeper.R;
import com.bohregard.minesweeper.util.SquareBoard;
import com.google.android.gms.games.Games;

import java.util.Random;

import static android.content.Context.MODE_PRIVATE;
import static android.widget.RelativeLayout.CENTER_IN_PARENT;
import static android.widget.RelativeLayout.TRUE;

/**
 * Created by bohregard on 5/29/2017.
 */

public class Tutorial extends Fragment implements
        View.OnClickListener,
        View.OnLongClickListener {

    private static final String TAG = Tutorial.class.getSimpleName();
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
        containerView = (RelativeLayout) v.findViewById(R.id.container);

        resetButton = (ImageButton) v.findViewById(R.id.smiley_reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isChronometerRunning) {
                    timeView.stop();
                    timeView.setBase(SystemClock.elapsedRealtime());
                    isChronometerRunning = false;
                }
                squareBoard.removeAllViews();
                containerView.removeAllViews();
                containerView.addView(squareBoard);
                gameBoardArray = null;
                setupBoard();
                tutorialStep1();
            }
        });
        squareBoard = (SquareBoard) v.findViewById(R.id.grid);
        timeView = (Chronometer) v.findViewById(R.id.time);
        minesLeftView = (TextView) v.findViewById(R.id.mines_left);

        buildSounds();
        setupBoard();
        tutorialStep1();
        return v;
    }

    /*
     ******************************************************************************************
     *   Tutorial Variables and Methods
     ******************************************************************************************
     */

    private int intermediateStep = 0;
    private TextView firstStep;
    private TextView secondStep;
    private TextView thirdStep;
    private TextView fourthStep;
    private TextView fifthStep;

    private void tutorialStep1() {
        blankBoard(new int[]{5, 0});
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(CENTER_IN_PARENT, TRUE);

        firstStep = new TextView(getActivity());
        firstStep.setText("Click the Highlighted square to get started!");
        firstStep.setLayoutParams(layoutParams);

        containerView.addView(firstStep);

        squareBoard.findViewById(getIndex(5, 0))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isChronometerRunning) {
                            timeView.setBase(SystemClock.elapsedRealtime());
                            timeView.start();
                            isChronometerRunning = true;
                        }
                        final int[] pos = (int[]) v.getTag();
                        zeroReveal(pos[0], pos[1]);
                        sp.play(clickSound, 1, 1, 0, 0, 1);
                        tutorialStep2();
                    }
                });
    }

    private void tutorialStep2() {
        blankBoard(new int[]{3, 2});
        containerView.removeView(firstStep);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(CENTER_IN_PARENT, TRUE);

        secondStep = new TextView(getActivity());
        secondStep.setText("The highlighted square contains a mine. (Touch to continue...)");
        secondStep.setLayoutParams(layoutParams);

        containerView.addView(secondStep);

        containerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (intermediateStep) {
                    case 0:
                        intermediateStep += 1;
                        secondStep.setText("We know this because the 1 in the lower right of the highlighted square is only adjacent to one uncovered square.");
                        break;
                    case 1:
                        intermediateStep += 1;
                        secondStep.setText("Long press the highlighted square to drop a flag!");
                        break;
                    case 2:
                        FrameLayout mine1 = (FrameLayout) squareBoard.findViewById(getIndex(3, 2));
                        mine1.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                toggleFlag((FrameLayout) v);
                                sp.play(flagSound, 1, 1, 0, 0, 1);
                                intermediateStep = 0;
                                tutorialStep3();
                                return true;
                            }
                        });
                }
            }
        });
    }

    private void tutorialStep3() {
        blankBoard(new int[]{2, 2});
        highlightCell(3, 1);
        highlightCell(2, 1);
        containerView.removeView(secondStep);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(CENTER_IN_PARENT, TRUE);

        thirdStep = new TextView(getActivity());
        thirdStep.setText("Our bottom 2 indicates another mine adjacent.");
        thirdStep.setLayoutParams(layoutParams);

        containerView.addView(thirdStep);

        containerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intermediateStep += 1;
                thirdStep.setText("Mark the empty square with a flag!");
                FrameLayout mine1 = (FrameLayout) squareBoard.findViewById(getIndex(2, 2));
                mine1.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        toggleFlag((FrameLayout) v);
                        sp.play(flagSound, 1, 1, 0, 0, 1);
                        tutorialStep4();
                        return true;
                    }
                });
            }
        });
    }

    private void tutorialStep4() {
        highlightCell(1, 2);
        containerView.removeView(thirdStep);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(CENTER_IN_PARENT, TRUE);

        fourthStep = new TextView(getActivity());
        fourthStep.setText("Our Second highlighted 2 also has two mines adjacent now. This newly highlighted square can be clicked.");
        fourthStep.setLayoutParams(layoutParams);

        containerView.addView(fourthStep);

        squareBoard.findViewById(getIndex(1, 2))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int[] pos = (int[]) v.getTag();
                        showSquare((FrameLayout) v, getIndex(pos[0], pos[1]));
                        sp.play(clickSound, 1, 1, 0, 0, 1);
                        tutorialStep5();
                    }
                });
    }

    private void tutorialStep5() {
        blankBoard(new int[]{0, 1});
        highlightCell(0, 2);
        highlightCell(0, 3);
        highlightCell(1, 2);
        highlightCell(1, 3);
        highlightCell(2, 3);

        containerView.removeView(fourthStep);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.addRule(CENTER_IN_PARENT, TRUE);

        fifthStep = new TextView(getActivity());
        fifthStep.setText("These newly highlighted cells can also be clicked since there can only be one adjacent mine.");
        fifthStep.setLayoutParams(layoutParams);

        containerView.addView(fifthStep);

        squareBoard.findViewById(getIndex(0, 2)).setOnClickListener(this);
        squareBoard.findViewById(getIndex(0, 3)).setOnClickListener(this);
        squareBoard.findViewById(getIndex(1, 3)).setOnClickListener(this);
        squareBoard.findViewById(getIndex(2, 3)).setOnClickListener(this);
    }

    private void blankBoard(int[] cell) {
        for (int i = 0; i < ROW_COUNT * COLUMN_COUNT; i++) {
            FrameLayout v = (FrameLayout) squareBoard.findViewById(i);
            v.setForeground(getActivity().getDrawable(R.drawable.tutorial_transparent));
        }
        highlightCell(cell[0], cell[1]);
    }

    private void highlightCell(int row, int column) {
        FrameLayout v = (FrameLayout) squareBoard.findViewById(getIndex(row, column));
        v.setForeground(null);
    }

    /*
     ******************************************************************************************
     *   Game Board Methods
     ******************************************************************************************
     */

    private RelativeLayout containerView;
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

    private static int COLUMN_COUNT = 4;
    private static int ROW_COUNT = 6;
    private int NUM_MINES = 4;

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

    /**
     * Set the board configuration up. Generate an empty array and fill it with the amount of
     * mines set. Shuffle the mines, and calculate the board numbers.
     */
    private void setupBoard() {
        setViewBackgroundDrawable(resetButton, R.drawable.smiley_regular);

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(
                getString(R.string.shared_pref), MODE_PRIVATE
        );

        gameBoardArray = new int[]{MINE_MASK, 0, 0, 0,
                0, 0, 0, 0,
                0, 0, MINE_MASK, 0,
                0, 0, MINE_MASK, 0,
                0, 0, 0, 0,
                0, 0, 0, MINE_MASK};

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

        FrameLayout mine = new FrameLayout(getActivity());
        setViewBackgroundDrawable(mine, R.drawable.mine_unclicked);
        mine.setLayoutParams(layoutParams);
        mine.setForeground(getActivity().getDrawable(R.drawable.tutorial_transparent));
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
                FrameLayout v = (FrameLayout) squareBoard.findViewById(getIndex(rowNum, colNum));
                if (gameBoardArray[getIndex(rowNum, colNum)] == 0) {
                    showSquare(v, getIndex(rowNum, colNum));
                    zeroReveal(rowNum, colNum);
                } else if (gameBoardArray[getIndex(rowNum, colNum)] < 10) {
                    showSquare(v, getIndex(rowNum, colNum));
                }
            }
        }
    }

    private void showSquare(FrameLayout mine, int index) {
        gameBoardArray[index] += MASK;
        //setMineText(mine, gameBoardArray[index]);
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
                setViewBackgroundDrawable(mine, R.drawable.mine_border);
                break;
        }
    }

    private void showBoard(int index) {
        for (int i = 0; i < COLUMN_COUNT * ROW_COUNT; i++) {
            FrameLayout v = (FrameLayout) squareBoard.findViewById(i);
            int[] pos = (int[]) v.getTag();
            int value = gameBoardArray[i];
            if (value <= MASK) {
                if (index == i) {
                    setViewBackgroundDrawable(v, R.drawable.mine_exploded);
                    v.setOnClickListener(null);
                    v.setOnLongClickListener(null);
                } else if (value > MINE_MASK && value < MINE_MASK + FLAG_MASK) {
                    setViewBackgroundDrawable(v, R.drawable.mine_wrong);
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
        long time1 = SystemClock.elapsedRealtime();
        showBoard(index);
        long time2 = SystemClock.elapsedRealtime();
        Log.e(TAG, "Lose Time: " + (time2 - time1));
        Main.showInterstitialAd();
    }

    private void gameWin() {
        timeView.stop();
        isChronometerRunning = false;

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
    private void toggleFlag(FrameLayout mine) {

        int[] pos = (int[]) mine.getTag();
        int value = gameBoardArray[getIndex(pos)];

        if (value >= 11 && value <= 21) {
            minesLeft += 1;
            setMineLeftText(minesLeft);
            setViewBackgroundDrawable(mine, R.drawable.mine_unclicked);
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
        if (!isChronometerRunning) {
            timeView.setBase(SystemClock.elapsedRealtime());
            timeView.start();
            isChronometerRunning = true;
        }
        FrameLayout mine = (FrameLayout) v;
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
        toggleFlag((FrameLayout) v);
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
}
