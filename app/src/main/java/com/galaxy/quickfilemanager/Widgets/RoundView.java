package com.galaxy.quickfilemanager.Widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.galaxy.quickfilemanager.R;

/**
 * Created by Umiya Mataji on 2/15/2017.
 */

public class RoundView extends View {

    Paint p;
    int color ;
    public RoundView(Context context) {
        this(context, null);
    }

    public RoundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // real work here
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.NewCircleView,
                0, 0
        );

        try {

            color = a.getColor(R.styleable.NewCircleView_circlecolor, 0xff000000);
        } finally {
            // release the TypedArray so that it can be reused.
            a.recycle();
        }
        init();
    }

    public void init()
    {
        p = new Paint();
        p.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // TODO Auto-generated method stub
        super.onDraw(canvas);
        if(canvas!=null)
        {
            //canvas.drawCircle(0, 0, 30, p);
            int w = getWidth();
            int h = getHeight();

            int pl = getPaddingLeft();
            int pr = getPaddingRight();
            int pt = getPaddingTop();
            int pb = getPaddingBottom();

            int usableWidth = w - (pl + pr);
            int usableHeight = h - (pt + pb);

            int radius = Math.min(usableWidth, usableHeight) / 2;
            int cx = pl + (usableWidth / 2);
            int cy = pt + (usableHeight / 2);

            p.setColor(color);
            canvas.drawCircle(cx, cy, radius, p);
        }
    }

}