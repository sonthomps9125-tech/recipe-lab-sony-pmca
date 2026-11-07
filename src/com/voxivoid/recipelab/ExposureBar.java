package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

/**
 * Live Camera Exposure Bar rendered at the bottom of the 3:2 viewfinder.
 * Shows real shutter speed, aperture, EV, and ISO with authentic Sony OSD styling.
 */
public class ExposureBar extends View {
    private final Paint text = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tagText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tagLine = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fade = new Paint();
    private final RectF r = new RectF();

    private String shutter = "--";
    private String aperture = "--";
    private String ev = "--";
    private String iso = "--";
    private final float density;

    public ExposureBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        density = context.getResources().getDisplayMetrics().density;

        fade.setColor(0x59000000); // 65% transparent dark background
        fade.setStyle(Paint.Style.FILL);

        text.setColor(0xFFDDDDDD);
        text.setFakeBoldText(true);
        text.setTextSize(13.0f * density);

        tagText.setColor(0xFFDDDDDD);
        tagText.setFakeBoldText(true);
        tagText.setTextSize(9.5f * density);

        tagLine.setColor(0x88FFFFFF);
        tagLine.setStyle(Paint.Style.STROKE);
        tagLine.setStrokeWidth(1.2f * density);
    }

    public void setTypeface(Typeface tf) {
        text.setTypeface(tf);
        tagText.setTypeface(tf);
        invalidate();
    }

    public void show(String shutter, String aperture, String ev, String iso) {
        this.shutter = shutter != null ? shutter : "--";
        this.aperture = aperture != null ? aperture : "--";
        this.ev = ev != null ? ev : "--";
        this.iso = iso != null ? iso : "--";
        invalidate();
    }

    private void tag(Canvas c, String label, float cx, float baseline, float width, float height) {
        r.set(cx - width / 2, baseline - height + 2.0f * density, cx + width / 2, baseline + 2.0f * density);
        c.drawRoundRect(r, 2.0f * density, 2.0f * density, tagLine);
        Paint.Align prev = tagText.getTextAlign();
        tagText.setTextAlign(Paint.Align.CENTER);
        c.drawText(label, cx, baseline - 1.5f * density, tagText);
        tagText.setTextAlign(prev);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Draw translucent dark background across the bar
        canvas.drawRect(0, 0, w, h, fade);

        float baseline = h / 2 + text.getTextSize() * 0.36f;
        float x = 14.0f * density;

        // 1. Shutter speed
        canvas.drawText(shutter, x, baseline, text);
        x += text.measureText(shutter) + 20.0f * density;

        // 2. Aperture
        canvas.drawText(aperture, x, baseline, text);
        x += text.measureText(aperture) + 22.0f * density;

        // 3. EV with tag box
        float evTagW = 22.0f * density, evTagH = 13.0f * density;
        tag(canvas, "EV", x + evTagW / 2, baseline, evTagW, evTagH);
        x += evTagW + 6.0f * density;
        canvas.drawText(ev, x, baseline, text);
        x += text.measureText(ev) + 22.0f * density;

        // 4. ISO with tag box
        float isoTagW = 24.0f * density, isoTagH = 13.0f * density;
        tag(canvas, "ISO", x + isoTagW / 2, baseline, isoTagW, isoTagH);
        x += isoTagW + 6.0f * density;
        canvas.drawText(iso, x, baseline, text);
    }
}
