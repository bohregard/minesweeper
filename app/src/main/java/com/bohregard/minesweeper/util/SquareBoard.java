package com.bohregard.minesweeper.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridLayout;

/**
 * Created by bohregard on 5/19/2017.
 */

public class SquareBoard extends GridLayout {
    public SquareBoard(Context context) {
        super(context);
    }

    public SquareBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareBoard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SquareBoard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
    }
}
