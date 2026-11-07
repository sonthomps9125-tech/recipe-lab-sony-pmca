package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * Renders authentic Sony camera standard softkey indicators on the right edge:
 * Top (SK1): EXIT ● / BACK ● (White text)
 * Bottom (SK2): OPTION · Edit ● (Sony Amber text #F2B85C)
 */
public class SoftKeyHints extends View {
    private final Paint topText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint botText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint[] mark;
    private String top = "EXIT";
    private String bottom = "OPTION · Edit";
    private final float density;

    public SoftKeyHints(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;
        mark = Skin.metalMark();

        topText.setColor(Skin.WHITE);
        topText.setFakeBoldText(true);
        topText.setTextSize(12.0f * density);

        botText.setColor(Skin.ORANGE); // Sony Amber #F2B85C
        botText.setFakeBoldText(true);
        botText.setTextSize(12.0f * density);
    }

    public void setTypeface(Typeface tf) {
        topText.setTypeface(tf);
        botText.setTypeface(tf);
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

        // Both buttons align at the exact same horizontal position x = w - 18dp
        float buttonX = w - 18.0f * density;

        // Top softkey (SK1): e.g. EXIT ● or BACK ●
        if (top != null && !top.isEmpty()) {
            float topY = 18.0f * density;
            Skin.softKey(canvas, top, buttonX, topY, topText, mark, density);
        }

        // Bottom softkey (SK2): e.g. OPTION · Edit ● in Sony Amber
        if (bottom != null && !bottom.isEmpty()) {
            float botY = h - 18.0f * density;
            Skin.softKey(canvas, bottom, buttonX, botY, botText, mark, density);
        }
    }
}
