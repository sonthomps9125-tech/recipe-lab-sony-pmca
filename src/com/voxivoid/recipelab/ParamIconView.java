package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

/**
 * Sony NEX-6 OSD compact pure graphic vector icon tile for film recipe parameters.
 * Optimized for compact 54dp vertical sidebar with 35% translucent background.
 */
public class ParamIconView extends View {
    private static final int ACCENT = 0xFFF2B85C, INK = 0xFF1A1208;

    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint border = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF r = new RectF();
    private final Path path = new Path();

    private int row = 0;
    private String valueText = "";
    private boolean selected = false;
    private boolean focused = false;
    private boolean dirty = false;
    private final float density;

    public ParamIconView(Context context) {
        super(context);
        density = context.getResources().getDisplayMetrics().density;

        border.setStyle(Paint.Style.STROKE);
        iconPaint.setStyle(Paint.Style.STROKE);
        iconFill.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.RIGHT);
        textPaint.setFakeBoldText(true);
    }

    public void setTypeface(Typeface tf) {
        textPaint.setTypeface(tf);
        invalidate();
    }

    public void set(int row, String value, boolean selected, boolean focused, boolean dirty) {
        this.row = row;
        this.valueText = value != null ? value : "";
        this.selected = selected;
        this.focused = focused;
        this.dirty = dirty;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension((int) (48 * density), (int) (28 * density));
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Background & Border styling
        if (focused) {
            bg.setColor(ACCENT);
            border.setColor(0xFFFFFFFF);
            border.setStrokeWidth(1.5f * density);
            iconPaint.setColor(INK);
            iconFill.setColor(INK);
            textPaint.setColor(INK);
        } else if (selected) {
            bg.setColor(0x50F2B85C);
            border.setColor(ACCENT);
            border.setStrokeWidth(1.5f * density);
            iconPaint.setColor(ACCENT);
            iconFill.setColor(ACCENT);
            textPaint.setColor(ACCENT);
        } else {
            bg.setColor(0x59000000); // 35% alpha translucent background
            border.setColor(0x33FFFFFF);
            border.setStrokeWidth(1.0f * density);
            iconPaint.setColor(0xEEFFFFFF);
            iconFill.setColor(0xEEFFFFFF);
            textPaint.setColor(dirty ? ACCENT : 0xDDFFFFFF);
        }

        // Draw tile background and border
        r.set(1.0f * density, 1.0f * density, w - 1.0f * density, h - 1.0f * density);
        c.drawRoundRect(r, 2.5f * density, 2.5f * density, bg);
        c.drawRoundRect(r, 2.5f * density, 2.5f * density, border);

        // Draw Vector Graphic Icon at (cx, cy)
        float cx = 11 * density, cy = h / 2;
        drawIcon(c, cx, cy);

        // Draw value text at bottom right
        textPaint.setTextSize(8.5f * density);
        float tx = w - 3 * density;
        float ty = h - 3.5f * density;
        c.drawText(valueText, tx, ty, textPaint);

        // If currently focused for editing, draw left/right nudge arrows
        if (focused) {
            float aw = 2.5f * density, ah = 3.0f * density;
            path.reset();
            path.moveTo(2 * density, h / 2);
            path.lineTo(2 * density + aw, h / 2 - ah);
            path.lineTo(2 * density + aw, h / 2 + ah);
            path.close();
            c.drawPath(path, iconFill);

            path.reset();
            path.moveTo(w - 2 * density, h / 2);
            path.lineTo(w - 2 * density - aw, h / 2 - ah);
            path.lineTo(w - 2 * density - aw, h / 2 + ah);
            path.close();
            c.drawPath(path, iconFill);
        }
    }

    private void drawIcon(Canvas c, float cx, float cy) {
        iconPaint.setStrokeWidth(1.1f * density);
        switch (row) {
            case Params.R_QUAL: { // [RAW] box
                r.set(cx - 7 * density, cy - 5 * density, cx + 7 * density, cy + 5 * density);
                c.drawRoundRect(r, 1.5f * density, 1.5f * density, iconPaint);
                Paint p = new Paint(textPaint);
                p.setTextSize(5.5f * density);
                p.setTextAlign(Paint.Align.CENTER);
                c.drawText("RAW", cx, cy + 2.0f * density, p);
                break;
            }
            case Params.R_STYLE: { // Film reel frame
                r.set(cx - 6 * density, cy - 6 * density, cx + 6 * density, cy + 6 * density);
                c.drawRect(r, iconPaint);
                c.drawRect(cx - 5 * density, cy - 5 * density, cx - 3 * density, cy - 3 * density, iconFill);
                c.drawRect(cx + 3 * density, cy - 5 * density, cx + 5 * density, cy - 3 * density, iconFill);
                c.drawRect(cx - 5 * density, cy + 3 * density, cx - 3 * density, cy + 5 * density, iconFill);
                c.drawRect(cx + 3 * density, cy + 3 * density, cx + 5 * density, cy + 5 * density, iconFill);
                break;
            }
            case Params.R_SAT: { // Color droplet
                path.reset();
                path.moveTo(cx, cy - 6 * density);
                path.cubicTo(cx - 4.5f * density, cy - 1 * density, cx - 4.5f * density, cy + 5 * density, cx, cy + 5 * density);
                path.cubicTo(cx + 4.5f * density, cy + 5 * density, cx + 4.5f * density, cy - 1 * density, cx, cy - 6 * density);
                path.close();
                c.drawPath(path, iconPaint);
                break;
            }
            case Params.R_CON: { // Contrast half circle
                c.drawCircle(cx, cy, 5.5f * density, iconPaint);
                r.set(cx - 5.5f * density, cy - 5.5f * density, cx + 5.5f * density, cy + 5.5f * density);
                c.drawArc(r, 90, 180, true, iconFill);
                break;
            }
            case Params.R_SHARP: { // Sharpness delta triangle
                path.reset();
                path.moveTo(cx, cy - 6 * density);
                path.lineTo(cx - 5 * density, cy + 4.5f * density);
                path.lineTo(cx + 5 * density, cy + 4.5f * density);
                path.close();
                c.drawPath(path, iconPaint);
                break;
            }
            case Params.R_WBMODE:
            case Params.R_KELVIN: { // Sun icon
                c.drawCircle(cx, cy, 2.5f * density, iconFill);
                for (int i = 0; i < 8; i++) {
                    double ang = i * Math.PI / 4.0;
                    float x1 = cx + (float) Math.cos(ang) * 4.0f * density;
                    float y1 = cy + (float) Math.sin(ang) * 4.0f * density;
                    float x2 = cx + (float) Math.cos(ang) * 5.8f * density;
                    float y2 = cy + (float) Math.sin(ang) * 5.8f * density;
                    c.drawLine(x1, y1, x2, y2, iconPaint);
                }
                break;
            }
            case Params.R_AB: { // A-B axis
                c.drawLine(cx - 5 * density, cy, cx + 5 * density, cy, iconPaint);
                Paint p = new Paint(textPaint);
                p.setTextSize(5.0f * density);
                p.setTextAlign(Paint.Align.CENTER);
                c.drawText("A", cx - 3 * density, cy - 1.5f * density, p);
                c.drawText("B", cx + 3 * density, cy - 1.5f * density, p);
                break;
            }
            case Params.R_GM: { // G-M axis
                c.drawLine(cx, cy - 5 * density, cx, cy + 5 * density, iconPaint);
                Paint p = new Paint(textPaint);
                p.setTextSize(5.0f * density);
                p.setTextAlign(Paint.Align.CENTER);
                c.drawText("G", cx + 2.5f * density, cy - 1.5f * density, p);
                c.drawText("M", cx + 2.5f * density, cy + 4.5f * density, p);
                break;
            }
            case Params.R_DRO: { // DRO split square
                r.set(cx - 5.5f * density, cy - 5.5f * density, cx + 5.5f * density, cy + 5.5f * density);
                c.drawRect(r, iconPaint);
                c.drawRect(cx - 5.5f * density, cy - 5.5f * density, cx, cy + 5.5f * density, iconFill);
                break;
            }
            case Params.R_EV: { // [±] compensation
                r.set(cx - 5.5f * density, cy - 5.5f * density, cx + 5.5f * density, cy + 5.5f * density);
                c.drawRoundRect(r, 1.5f * density, 1.5f * density, iconPaint);
                c.drawLine(cx - 3 * density, cy - 1.5f * density, cx + 3 * density, cy - 1.5f * density, iconPaint);
                c.drawLine(cx, cy - 4.5f * density, cx, cy + 1.5f * density, iconPaint);
                c.drawLine(cx - 3 * density, cy + 3.0f * density, cx + 3 * density, cy + 3.0f * density, iconPaint);
                break;
            }
            default: {
                c.drawCircle(cx, cy, 2.5f * density, iconFill);
                break;
            }
        }
    }
}
