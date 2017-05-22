package com.bohregard.minesweeper;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bohregard.minesweeper.util.AutoResizeTextView;
import com.bohregard.minesweeper.util.SquareBoard;
import com.bohregard.minesweeper.util.Utils;

import java.util.Random;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;

/**
 * todo: if no mines left, disable longclicking
 * Created by awtod on 5/18/2017.
 */

public class MineSweeper extends Activity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = MineSweeper.class.getSimpleName();
    private static final int COLUMN_COUNT = 12;
    private static final int ROW_COUNT = 18;
    private static final int MINES = (int) Math.floor((COLUMN_COUNT * ROW_COUNT) * .20);
    //    private static final int MINES = (int) Math.floor((ROW_COUNT*ROW_COUNT)*.40);
    private static int[][] mineLocations;

    private static final int MINE = 10;
    private static final int MASK = 20;
    private static final int FLAG_SET = 40;

    private static int minesLeft;

    private SquareBoard squareBoard;
    private TextView minesLeftView;
    private Chronometer timeView;

    private SharedPreferences sharedPreferences;

    /*
    ******************************************************************************************
    *   Ratios
    ******************************************************************************************
     */
    private static final float PIXEL_RATIO = (180f/299f);


    /*
    ******************************************************************************************
    *   Activity Methods
    ******************************************************************************************
     */

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mine_sweeper);
        Utils.hideSystemUI(getWindow().getDecorView());
        getWindow().addFlags(FLAG_KEEP_SCREEN_ON);

        sharedPreferences = getSharedPreferences("sharedPrefs", MODE_PRIVATE);

        if(savedInstanceState != null) {
            Log.d(TAG, "Saved instance restoring...");
        }

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        float ratio = ((float)metrics.widthPixels / (float)metrics.heightPixels);
        Log.d(TAG, "Width: " + metrics.widthPixels);
        Log.d(TAG, "Height: " + metrics.heightPixels);
        Log.d(TAG, "Ratio: " + ratio);

        if(PIXEL_RATIO == ratio) {
            Log.d(TAG, "Pixel Ratio detected...");
        }

        setupBoard();
    }

    @Override
    protected void onResume() {
        super.onResume();
        timeView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        timeView.stop();
        Log.d(TAG, "On Pause");
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
    *   Private Methods
    ******************************************************************************************
     */

    /**
     * Set the board configuration up. Generate an empty array and fill it with the amount of
     * mines set. Shuffle the mines, and calculate the board numbers.
     */
    private void setupBoard() {
        squareBoard = (SquareBoard) findViewById(R.id.grid);
        minesLeft = MINES;

        minesLeftView = (TextView) findViewById(R.id.mines_left);
        minesLeftView.setText(": " + minesLeft);

        // If the mineLocations are null, we need to build the board from scratch
        if(mineLocations == null) {
            mineLocations = new int[ROW_COUNT][COLUMN_COUNT];

            int mineCount = 0;

            for (int i = 0; i < ROW_COUNT; i++) {
                for (int j = 0; j < COLUMN_COUNT; j++) {
                    if (mineCount < MINES) {
                        mineLocations[i][j] = 10;
                        mineCount++;
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

        timeView = (Chronometer) findViewById(R.id.time);
        timeView.start();
    }

    /**
     * Instantiate
     * @param row
     * @param column
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
        mine.setBackground(getDrawable(R.drawable.mine_unclicked));
        mine.setLayoutParams(layoutParams);
        mine.setOnClickListener(this);
        mine.setOnLongClickListener(this);
        mine.setTag(new int[]{row, column});
        mine.setId(getIndex(row, column));

        if (mineLocations[row][column] != MINE) {
            search(row, column, MINE);
        } else {
            mine.setText("M");
            mine.setTextColor(getColor(R.color.transparent));
        }

        squareBoard.addView(mine);
    }

    private void printArray() {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                Log.i(TAG + "_MINELOCATIONS", "MINESLOCATIONS: " + mineLocations[row][column]);
            }
        }
    }

    private void search(int row, int column, int searchParam) {
        int mineCount = 0;
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
                        mineCount += 1;
                    }
                }
            }
        }

        mineLocations[row][column] = mineCount;
    }

    private void showSquare(AutoResizeTextView mine, int[] pos) {
        Log.d(TAG, "Show the square at: " + pos[0] + ", " + pos[1]);
        mine.setText("" + mineLocations[pos[0]][pos[1]]);
        mine.setTypeface(null, Typeface.BOLD);
        mine.setBackground(getDrawable(R.drawable.mine_clicked));
        mine.setOnClickListener(null);
        mine.setOnLongClickListener(null);
        switch (mineLocations[pos[0]][pos[1]]) {
            case 0:
                mine.setTextColor(getColor(R.color.transparent));
                break;
            case 1:
                mine.setTextColor(Color.BLUE);
                break;
            case 2:
                mine.setTextColor(Color.GREEN);
                break;
            case 3:
                mine.setTextColor(Color.RED);
                break;
            case 4:
                mine.setTextColor(Color.parseColor("#000099"));
                break;
            case 5:
                mine.setTextColor(Color.parseColor("#DEB887"));
                break;
            case MINE:
                mine.setBackground(getDrawable(R.drawable.mine_border));
                break;
        }
        mineLocations[pos[0]][pos[1]] = mineLocations[pos[0]][pos[1]] + MASK;
    }

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

    private int getIndex(int row, int column) {
//        if (ROW_COUNT > COLUMN_COUNT) {
//            return row * ROW_COUNT + column;
//        } else {
//            return row * COLUMN_COUNT + column;
//        }
        return row * COLUMN_COUNT + column;
    }

    private void toggleFlag(AutoResizeTextView mine) {
        if (mine.getText() == "F") {
            minesLeft += 1;
            minesLeftView.setText("Mines Left: " + minesLeft);
            mine.setBackground(getDrawable(R.drawable.mine_unclicked));
            mine.setTextColor(getColor(R.color.transparent));
            int[] pos = (int[]) mine.getTag();
            mine.setText("" + mineLocations[pos[0]][pos[1]]);
            mine.setOnClickListener(this);
        } else {
            minesLeft -= 1;
            minesLeftView.setText("Mines Left: " + minesLeft);
            mine.setBackground(getDrawable(R.drawable.mine_flag));
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
            if (explodedMine == pos) {
                v.setBackground(getDrawable(R.drawable.mine_exploded));
                v.setOnClickListener(null);
                v.setOnLongClickListener(null);
            } else if (mineLocations[pos[0]][pos[1]] == MINE && v.getText() == "F" || mineLocations[pos[0]][pos[1]] >= MASK) {
                //do nothing
            } else if ((mineLocations[pos[0]][pos[1]] != MINE && v.getText() == "F")) {
                v.setBackground(getDrawable(R.drawable.mine_wrong));
            } else {
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
            Toast.makeText(this, "Game WON! Time: " + timeView.getText(), Toast.LENGTH_SHORT).show();
            showBoard(null);
        } else {
            Log.e(TAG, "Game still has uncovered mines");
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
        Log.d(TAG, "Position: " + pos[0] + ", " + pos[1] + " with id: " + v.getId() + " Mine Locations: " + mineLocations[pos[0]][pos[1]]);
        if (mineLocations[pos[0]][pos[1]] == MINE) {
            Log.e(TAG, "Game Ends! Show the board...");
            timeView.stop();
            showBoard(pos);
        } else if (mineLocations[pos[0]][pos[1]] == 0) {
            search(pos[0], pos[1], 0);
            printArray();
        } else {
            showSquare(mine, pos);
            printArray();
        }
    }

    @Override
    public boolean onLongClick(View v) {
        toggleFlag((AutoResizeTextView) v);
        return true;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(false);
    }
}