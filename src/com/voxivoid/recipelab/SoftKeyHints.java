package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * Renders authentic Sony camera standard softkey indicators on the right edge:
 * Top (SK1): EXIT ● / BACK ●
 * Bottom (SK2): OPTION ● / Edit ●
 */
public class SoftKeyHints extends View {
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint[] mark;
    private String top = "EXIT";
    private String bottom = "OPTION";
    private final float density;

    public SoftKeyHints(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        mark = Skin.metalMark();
        text.setColor(Skin.WHITE);
        text.setFakeBoldText(true);
        text.setTextSize(12.0f * density); // Standard Sony font size
    }

    public void setTypeface(Typeface tf) {
        text.setTypeface(tf);
        invalidate();
    }

    public void setLabels(String topLabel, String bottomLabel) {
        if ((top == null ? topLabel == null : top.equals(topLabel)) &&
            (bottom == null ? bottomLabel == null : bottom.equals(bottomLabel))) {
            return;
        }
        this.top = topLabel != null ? topLabel : "";
        this.bottom = bottomLabel != null ? bottomLabel : "";
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Both top and bottom buttons align at the exact same horizontal distance from the right edge
        float buttonX = w - 18.0f * density;

        // Top softkey (SK1): e.g. EXIT ● or BACK ●
        if (top != null && !top.isEmpty()) {
            float topY = 18.0f * density;
            Skin.softKey(canvas, top, buttonX, topY, text, mark, density);
        }

        // Bottom softkey (SK2): e.g. OPTION ● or Edit ●
        if (bottom != null && !bottom.isEmpty()) {
            float botY = h - 18.0f * density;
            Skin.softKey(canvas, bottom, buttonX, botY, text, mark, density);
        }
    }
}
