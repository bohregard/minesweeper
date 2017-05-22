package com.bohregard.minesweeper.util;

import android.content.Context;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;


/**
 * Created by bohregard on 5/19/2017.
 */

public class SquareBoard extends GridLayout {

    public SquareBoard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SquareBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareBoard(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthSpec, int heightSpec) {
        super.onMeasure(widthSpec, heightSpec);
    }
}
