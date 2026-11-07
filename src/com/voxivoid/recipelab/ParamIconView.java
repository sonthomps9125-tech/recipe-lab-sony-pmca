package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

/**
 * Sony NEX-6 OSD pure graphic vector icon tile for film recipe parameters.
 * Displays a clean vector icon at center/top-left and a compact value string at bottom-right.
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
        setMeasuredDimension((int) (48 * density), (int) (34 * density));
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight();
        if (w <= 0 || h <= 0) return;

        // Background & Border colors
        if (focused) {
            bg.setColor(ACCENT);
            border.setColor(0xFFFFFFFF);
            border.setStrokeWidth(1.5f * density);
            iconPaint.setColor(INK);
            iconFill.setColor(INK);
            textPaint.setColor(INK);
        } else if (selected) {
            bg.setColor(0x40F2B85C);
            border.setColor(ACCENT);
            border.setStrokeWidth(1.5f * density);
            iconPaint.setColor(ACCENT);
            iconFill.setColor(ACCENT);
            textPaint.setColor(ACCENT);
        } else {
            bg.setColor(0xCC1A1A1A);
            border.setColor(0x40FFFFFF);
            border.setStrokeWidth(1.0f * density);
            iconPaint.setColor(0xEEFFFFFF);
            iconFill.setColor(0xEEFFFFFF);
            textPaint.setColor(dirty ? ACCENT : 0xDDFFFFFF);
        }

        // Draw tile background and border
        r.set(1.5f * density, 1.5f * density, w - 1.5f * density, h - 1.5f * density);
        c.drawRoundRect(r, 3 * density, 3 * density, bg);
        c.drawRoundRect(r, 3 * density, 3 * density, border);

        // Draw Vector Graphic Icon at (cx, cy)
        float cx = 13 * density, cy = 13 * density;
        drawIcon(c, cx, cy);

        // Draw numerical/string value text at bottom right
        textPaint.setTextSize(9.5f * density);
        float tx = w - 4 * density;
        float ty = h - 4 * density;
        c.drawText(valueText, tx, ty, textPaint);

        // If currently focused for editing, draw left/right nudge arrows
        if (focused) {
            float aw = 3 * density, ah = 3.5f * density;
            path.reset();
            path.moveTo(3 * density, h / 2);
            path.lineTo(3 * density + aw, h / 2 - ah);
            path.lineTo(3 * density + aw, h / 2 + ah);
            path.close();
            c.drawPath(path, iconFill);

            path.reset();
            path.moveTo(w - 3 * density, h / 2);
            path.lineTo(w - 3 * density - aw, h / 2 - ah);
            path.lineTo(w - 3 * density - aw, h / 2 + ah);
            path.close();
            c.drawPath(path, iconFill);
        }
    }

    private void drawIcon(Canvas c, float cx, float cy) {
        iconPaint.setStrokeWidth(1.2f * density);
        switch (row) {
            case Params.R_QUAL: { // [RAW] box
                r.set(cx - 8 * density, cy - 6 * density, cx + 8 * density, cy + 6 * density);
                c.drawRoundRect(r, 1.5f * density, 1.5f * density, iconPaint);
                Paint p = new Paint(textPaint);
                p.setTextSize(6.5f * density);
                p.setTextAlign(Paint.Align.CENTER);
                c.drawText("RAW", cx, cy + 2.5f * density, p);
                break;
            }
            case Params.R_STYLE: { // Film reel frame
                r.set(cx - 7 * density, cy - 7 * density, cx + 7 * density, cy + 7 * density);
                c.drawRect(r, iconPaint);
                // sprockets
                c.drawRect(cx - 6 * density, cy - 6 * density, cx - 3.5f * density, cy - 3.5f * density, iconFill);
                c.drawRect(cx + 3.5f * density, cy - 6 * density, cx + 6 * density, cy - 3.5f * density, iconFill);
                c.drawRect(cx - 6 * density, cy + 3.5f * density, cx - 3.5f * density, cy + 6 * density, iconFill);
                c.drawRect(cx + 3.5f * density, cy + 3.5f * density, cx + 6 * density, cy + 6 * density, iconFill);
                break;
            }
            case Params.R_SAT: { // Color droplet
                path.reset();
                path.moveTo(cx, cy - 7 * density);
                path.cubicTo(cx - 5 * density, cy - 1 * density, cx - 5 * density, cy + 6 * density, cx, cy + 6 * density);
                path.cubicTo(cx + 5 * density, cy + 6 * density, cx + 5 * density, cy - 1 * density, cx, cy - 7 * density);
                path.close();
                c.drawPath(path, iconPaint);
                break;
            }
            case Params.R_CON: { // Contrast half circle
                c.drawCircle(cx, cy, 6 * density, iconPaint);
                r.set(cx - 6 * density, cy - 6 * density, cx + 6 * density, cy + 6 * density);
                c.drawArc(r, 90, 180, true, iconFill);
                break;
            }
            case Params.R_SHARP: { // Sharpness delta triangle
                path.reset();
                path.moveTo(cx, cy - 7 * density);
                path.lineTo(cx - 6 * density, cy + 5 * density);
                path.lineTo(cx + 6 * density, cy + 5 * density);
                path.close();
                c.drawPath(path, iconPaint);
                break;
            }
            case Params.R_WBMODE:
            case Params.R_KELVIN: { // Sun icon
                c.drawCircle(cx, cy, 3 * density, iconFill);
                for (int i = 0; i < 8; i++) {
                    double ang = i * Math.PI / 4.0;
                    float x1 = cx + (float) Math.cos(ang) * 4.5f * density;
                    float y1 = cy + (float) Math.sin(ang) * 4.5f * density;
                    float x2 = cx + (float) Math.cos(ang) * 6.5f * density;
                    float y2 = cy + (float) Math.sin(ang) * 6.5f * density;
                    c.drawLine(x1, y1, x2, y2, iconPaint);
                }
                break;
            }
            case Params.R_AB: { // A-B axis
                c.drawLine(cx - 6 * density, cy, cx + 6 * density, cy, iconPaint);
                Paint p = new Paint(textPaint);
                p.setTextSize(5.5f * density);
                p.setTextAlign(Paint.Align.CENTER);
                c.drawText("A", cx - 4 * density, cy - 2 * density, p);
                c.drawText("B", cx + 4 * density, cy - 2 * density, p);
                break;
            }
            case Params.R_GM: { // G-M axis
                c.drawLine(cx, cy - 6 * density, cx, cy + 6 * density, iconPaint);
                Paint p = new Paint(textPaint);
                p.setTextSize(5.5f * density);
                p.setTextAlign(Paint.Align.CENTER);
                c.drawText("G", cx + 3 * density, cy - 2 * density, p);
                c.drawText("M", cx + 3 * density, cy + 5 * density, p);
                break;
            }
            case Params.R_DRO: { // DRO split square
                r.set(cx - 6 * density, cy - 6 * density, cx + 6 * density, cy + 6 * density);
                c.drawRect(r, iconPaint);
                c.drawRect(cx - 6 * density, cy - 6 * density, cx, cy + 6 * density, iconFill);
                break;
            }
            case Params.R_EV: { // [±] compensation
                r.set(cx - 6 * density, cy - 6 * density, cx + 6 * density, cy + 6 * density);
                c.drawRoundRect(r, 1.5f * density, 1.5f * density, iconPaint);
                c.drawLine(cx - 3.5f * density, cy - 2 * density, cx + 3.5f * density, cy - 2 * density, iconPaint);
                c.drawLine(cx, cy - 5.5f * density, cx, cy + 1.5f * density, iconPaint);
                c.drawLine(cx - 3.5f * density, cy + 3.5f * density, cx + 3.5f * density, cy + 3.5f * density, iconPaint);
                break;
            }
            default: { // Star / sparkle
                c.drawCircle(cx, cy, 3 * density, iconFill);
                break;
            }
        }
    }
}
