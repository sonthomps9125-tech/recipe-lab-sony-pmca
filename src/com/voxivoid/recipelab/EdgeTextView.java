package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * TextView with black stroke edge outline for live view contrast and readability.
 */
public class EdgeTextView extends TextView {
    private boolean drawing = false;
    private float edge = 2.0f;

    public EdgeTextView(Context context) {
        super(context);
        init(context);
    }

    public EdgeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EdgeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        edge = 2.0f * context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void invalidate() {
        if (!drawing) {
            super.invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        TextPaint paint = getPaint();
        int textColor = getCurrentTextColor();
        drawing = true;

        // Draw black stroke outline
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(edge);
        setTextColor(Color.BLACK);
        super.onDraw(canvas);

        // Draw foreground text
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(0);
        setTextColor(textColor);
        super.onDraw(canvas);

        drawing = false;
    }
}
