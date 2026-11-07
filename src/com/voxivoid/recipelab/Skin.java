package com.voxivoid.recipelab;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Sony NEX-6 camera UI skin drawing utilities: metallic marks, colors, and scaling.
 */
public final class Skin {
    public static final int ORANGE = 0xFFF2B85C; // Sony Amber accent
    public static final int WHITE  = 0xFFFFFFFF;
    public static final int BLACK  = 0xFF000000;
    public static final int DIM    = 0x80FFFFFF;
    public static final int TEXT   = 0xCCFFFFFF;
    public static final float W    = 640.0f;
    public static final float H    = 480.0f;

    private Skin() {}

    /** Computes display scale factor relative to standard 640x480 resolution */
    public static float scale(View view) {
        float sx = view.getWidth() / W;
        float sy = view.getHeight() / H;
        return Math.min(sx, sy);
    }

    /** Creates 3 paints for rendering authentic Sony softkey metallic indicators */
    public static Paint[] metalMark() {
        Paint outer = new Paint(Paint.ANTI_ALIAS_FLAG);
        outer.setColor(0xFF222222);
        outer.setStyle(Paint.Style.FILL);

        Paint ring = new Paint(Paint.ANTI_ALIAS_FLAG);
        ring.setColor(0xFF666666);
        ring.setStyle(Paint.Style.STROKE);

        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        dot.setColor(0xFFE8E8E8);
        dot.setStyle(Paint.Style.FILL);

        return new Paint[] { outer, ring, dot };
    }

    /** Draws the authentic Sony metallic softkey circular indicator and label */
    public static void softKey(Canvas c, String label, float x, float y, Paint textPaint, Paint[] mark, float density) {
        float r = 6.5f * density; // Sony standard 13dp diameter
        mark[1].setStrokeWidth(1.2f * density);

        // Draw outer dark metallic circle, border ring, and center dot
        c.drawCircle(x, y, r, mark[0]);
        c.drawCircle(x, y, r, mark[1]);
        c.drawCircle(x, y, 2.5f * density, mark[2]);

        // Draw text label to the left of the button
        if (label != null && !label.isEmpty()) {
            Paint.Align prevAlign = textPaint.getTextAlign();
            textPaint.setTextAlign(Paint.Align.RIGHT);
            float tx = x - r - 6.0f * density;
            float ty = y + textPaint.getTextSize() * 0.36f;
            c.drawText(label, tx, ty, textPaint);
            textPaint.setTextAlign(prevAlign);
        }
    }

    /** Draws high-contrast stroked outline around text for live view readability */
    public static void edged(Canvas c, String text, float x, float y, Paint fillPaint, float strokeWidth) {
        int origColor = fillPaint.getColor();
        Paint.Style origStyle = fillPaint.getStyle();
        float origStroke = fillPaint.getStrokeWidth();

        fillPaint.setStyle(Paint.Style.STROKE);
        fillPaint.setStrokeJoin(Paint.Join.ROUND);
        fillPaint.setStrokeWidth(strokeWidth);
        fillPaint.setColor(BLACK);
        c.drawText(text, x, y, fillPaint);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setStrokeWidth(origStroke);
        fillPaint.setColor(origColor);
        c.drawText(text, x, y, fillPaint);

        fillPaint.setStyle(origStyle);
    }
}
