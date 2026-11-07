package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Sony NEX-6 Theme Selection style Recipe Picker (16:9 Widescreen).
 * - Left (3:2 zone): Recipe title, subtitle, cascading sample photos from assets/samples/, summary badges.
 * - Right: Vertical carousel of recipes scrolling with control wheel.
 * - Header: App icon + "Recipe Selection" + Group selector + BACK ● (SK1).
 * - Footer: [◄►] Brand [▲▼] Recipe [●] Select + OPTION · Edit ● (SK2).
 */
public class PickerView extends View {
    private static final int ACCENT = 0xFFF2B85C, INK = 0xFF1A1208, BLUE_ICON = 0xFF1E88E5;

    private final Paint bg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint sel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint head = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint subPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint item = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint small = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint rule = new Paint();
    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumb = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint frameFill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint frameStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint frameShadow = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint blueIcon = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint star = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tagBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outline = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint photoPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint[] softKeyMark;

    private final RectF r = new RectF();
    private final Rect srcRect = new Rect();
    private final RectF dstRect = new RectF();
    private final Path path = new Path();
    private final float d;
    private final Legend legend;

    private Keys.Caps caps = Keys.Caps.UNKNOWN;
    private int selected = 0, column = 1, group = 0;
    private List<Integer> favs = new ArrayList<Integer>();
    private Library lib = new Library();
    private boolean card = true;

    private Bitmap cachedSample = null;
    private int cachedRecipe = -1;

    public PickerView(Context c, AttributeSet a) {
        super(c, a);
        d = c.getResources().getDisplayMetrics().density;
        legend = new Legend(d);
        softKeyMark = Skin.metalMark();

        bg.setColor(0xFF101010);
        sel.setColor(0x33F2B85C);
        outline.setColor(ACCENT);
        outline.setStyle(Paint.Style.STROKE);
        outline.setStrokeWidth(1.5f * d);

        blueIcon.setColor(BLUE_ICON);
        blueIcon.setStyle(Paint.Style.FILL);

        head.setColor(0xFFFFFFFF);
        head.setTextSize(11 * d);
        head.setFakeBoldText(true);

        titlePaint.setColor(ACCENT);
        titlePaint.setTextSize(16 * d);
        titlePaint.setFakeBoldText(true);

        subPaint.setColor(0xCCFFFFFF);
        subPaint.setTextSize(9.5f * d);

        item.setColor(0xFFFFFFFF);
        item.setTextSize(12 * d);

        small.setColor(0x99FFFFFF);
        small.setTextSize(9 * d);

        rule.setColor(0x33FFFFFF);
        track.setColor(0x26FFFFFF);
        thumb.setColor(0xCCF2B85C);

        frameFill.setStyle(Paint.Style.FILL);
        frameFill.setColor(0xFF1A1A1A);

        frameStroke.setStyle(Paint.Style.STROKE);
        frameStroke.setColor(0xFFEEEEEE);
        frameStroke.setStrokeWidth(1.5f * d);

        frameShadow.setStyle(Paint.Style.FILL);
        frameShadow.setColor(0x88000000);

        tagBg.setStyle(Paint.Style.FILL);
    }

    public void setCaps(Keys.Caps k) { caps = k; invalidate(); }

    public void setTypeface(Typeface tf) {
        head.setTypeface(tf);
        titlePaint.setTypeface(tf);
        subPaint.setTypeface(tf);
        item.setTypeface(tf);
        small.setTypeface(tf);
        legend.setTypeface(tf);
        invalidate();
    }

    public void set(int recipe, int col, int grp, List<Integer> favorites, Library library, boolean cardIn) {
        selected = recipe;
        column = col;
        group = grp;
        favs = favorites;
        lib = library;
        card = cardIn;
        invalidate();
    }

    private Bitmap loadSample(int idx) {
        if (idx < 0 || idx >= 77) return null;
        try {
            String pth = String.format(Locale.US, "samples/%02d.jpg", idx + 1);
            InputStream is = getContext().getAssets().open(pth);
            Bitmap bm = BitmapFactory.decodeStream(is);
            is.close();
            return bm;
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), pad = 12 * d;
        c.drawRect(0, 0, w, h, bg);

        // Update cached sample photo for current selection
        if (selected != cachedRecipe) {
            cachedRecipe = selected;
            if (cachedSample != null && !cachedSample.isRecycled()) {
                cachedSample.recycle();
                cachedSample = null;
            }
            cachedSample = loadSample(selected);
        }

        int g = group;
        int count = Favorites.groupCount(g, favs, lib);
        float colX = w * 0.65f; // Boundary between 3:2 detail area and right vertical carousel
        float topH = 34 * d, footH = 32 * d;
        float listTop = topH + 8 * d, listBottom = h - footH - 4 * d, listH = listBottom - listTop;

        // ---- 1. Top Header Bar: App Icon + Title + Category Indicator + BACK ●
        r.set(pad, pad - 2 * d, pad + 18 * d, pad + 16 * d);
        c.drawRoundRect(r, 2 * d, 2 * d, blueIcon);
        r.set(pad + 4 * d, pad + 2 * d, pad + 14 * d, pad + 12 * d);
        c.drawRect(r, frameStroke);

        c.drawText("Recipe Selection", pad + 24 * d, pad + 12 * d, head);

        // Category selector indicator: ◄ ALL 3 / 18 ►
        String grpLabel = "◄  " + Favorites.groupName(g).toUpperCase() + "  " + (Favorites.positionIn(g, selected, favs, lib) + 1) + " / " + count + "  ►";
        small.setColor(0xCCFFFFFF);
        c.drawText(grpLabel, pad + 130 * d, pad + 12 * d, small);

        // Top right SoftKey 1: BACK ●
        Skin.softKey(c, "BACK", w - 16 * d, pad + 7 * d, head, softKeyMark, d);

        c.drawLine(pad, topH, w - pad, topH, rule);

        // ---- 2. Left / Center Area: Recipe Details & Cascading Sample Photos
        if (lib.valid(selected)) {
            Recipes.Recipe rc = lib.get(selected);
            float lx = pad, ly = topH + 16 * d;

            // Big Recipe Title (Sony Amber)
            c.drawText(Recipes.displayName(rc), lx, ly, titlePaint);

            // Subtitle description & parameter summary
            String orig = Recipes.originalName(rc);
            String desc = Recipes.groupLabel(rc.group) + "  ·  " + rc.summary();
            if (orig != null && !orig.isEmpty()) desc = orig + "  (" + desc + ")";
            c.drawText(desc, lx, ly + 13 * d, subPaint);

            // Cascading Sample Photo Showcase in center of 3:2 zone
            float cx = colX * 0.48f, cy = topH + 85 * d;
            drawCascadingSampleFrames(c, cx, cy);

            // Bottom summary badges row: [RAW+J] [Style: ...] [WB: ...] [DRO: ...] [EV: ...]
            float by = listBottom - 12 * d;
            drawSummaryRow(c, lx, by, rc);
        }

        // ---- 3. Vertical Divider Rule
        c.drawLine(colX, topH + 6 * d, colX, listBottom, rule);

        // ---- 4. Right Column: Vertical Carousel of Recipes
        if (count > 0) {
            float rh = 28 * d;
            int visible = Math.max(1, (int) (listH / rh));
            int selPos = Math.max(0, Favorites.positionIn(g, selected, favs, lib));
            int first = Math.max(0, Math.min(selPos - visible / 2, count - visible));
            float xr = w - pad - 6 * d;
            float y = listTop;

            for (int k = first; k < Math.min(count, first + visible); k++, y += rh) {
                int idx = Favorites.recipeAt(g, k, favs, lib);
                boolean on = (idx == selected);
                r.set(colX + 8 * d, y, xr, y + rh - 3 * d);
                if (on) {
                    c.drawRoundRect(r, 3 * d, 3 * d, sel);
                    c.drawRoundRect(r, 3 * d, 3 * d, outline);
                    item.setColor(ACCENT);
                    item.setFakeBoldText(true);
                } else {
                    item.setColor(0xCCFFFFFF);
                    item.setFakeBoldText(false);
                }
                Recipes.Recipe rk = lib.get(idx);
                c.drawText(Recipes.displayName(rk), colX + 14 * d, y + 16 * d, item);

                if (favs.contains(idx)) {
                    star.setColor(on ? ACCENT : 0x88FFFFFF);
                    Legend.star(c, xr - 8 * d, y + 12 * d, 4.5f * d, star);
                }
            }

            if (count > visible) {
                scrollbar(c, w - pad - 3 * d, listTop, listH, 3 * d, first, visible, count);
            }
        }

        // ---- 5. Footer: [◄►] Brand [▲▼] Recipe [●] Select & OPTION · Edit ●
        c.drawLine(pad, h - footH, w - pad, h - footH, rule);

        // Left footer navigation hints
        float legY = h - footH / 2 + 4 * d;
        small.setColor(0xCCFFFFFF);
        c.drawText("[◄►] Brand   [▲▼] Recipe   [●] Select", pad, legY, small);

        // Right footer SoftKey 2: OPTION · Edit ●
        head.setColor(ACCENT);
        Skin.softKey(c, "OPTION · Edit", w - 16 * d, h - footH / 2, head, softKeyMark, d);
        head.setColor(0xFFFFFFFF);
    }

    /** Draws cascading photo frames matching Sony Picture Effect+ style */
    private void drawCascadingSampleFrames(Canvas c, float cx, float cy) {
        float fw = 150 * d, fh = 100 * d; // 3:2 main frame dimensions

        // 1. Background side frames (cascading shadow effect)
        r.set(cx - fw / 2 - 28 * d, cy - fh / 2 + 6 * d, cx + fw / 2 - 28 * d, cy + fh / 2 + 6 * d);
        c.drawRoundRect(r, 3 * d, 3 * d, frameShadow);
        c.drawRoundRect(r, 3 * d, 3 * d, frameFill);
        frameStroke.setColor(0x66FFFFFF);
        c.drawRoundRect(r, 3 * d, 3 * d, frameStroke);

        r.set(cx - fw / 2 + 28 * d, cy - fh / 2 + 6 * d, cx + fw / 2 + 28 * d, cy + fh / 2 + 6 * d);
        c.drawRoundRect(r, 3 * d, 3 * d, frameShadow);
        c.drawRoundRect(r, 3 * d, 3 * d, frameFill);
        c.drawRoundRect(r, 3 * d, 3 * d, frameStroke);

        // 2. Main Center Frame
        float mx = cx - fw / 2, my = cy - fh / 2;
        r.set(mx - 3 * d, my - 3 * d, mx + fw + 3 * d, my + fh + 3 * d);
        c.drawRoundRect(r, 4 * d, 4 * d, frameShadow);

        // Draw central sample photograph
        if (cachedSample != null && !cachedSample.isRecycled()) {
            srcRect.set(0, 0, cachedSample.getWidth(), cachedSample.getHeight());
            dstRect.set(mx, my, mx + fw, my + fh);
            c.drawBitmap(cachedSample, srcRect, dstRect, photoPaint);
        } else {
            r.set(mx, my, mx + fw, my + fh);
            c.drawRoundRect(r, 3 * d, 3 * d, frameFill);
            Paint p = new Paint(small);
            p.setTextAlign(Paint.Align.CENTER);
            c.drawText("No Preview", cx, cy + 3 * d, p);
        }

        // Crisp white border frame
        frameStroke.setColor(0xFFFFFFFF);
        r.set(mx, my, mx + fw, my + fh);
        c.drawRoundRect(r, 3 * d, 3 * d, frameStroke);
    }

    /** Summary row of badges at bottom-left: [RAW+J] [Style: ...] [WB: ...] [DRO: ...] [EV: ...] */
    private void drawSummaryRow(Canvas c, float x, float y, Recipes.Recipe rc) {
        float rx = x;
        rx = badge(c, rx, y, "RAW+J", 0xFF282828, 0xCCFFFFFF);
        rx = badge(c, rx + 6 * d, y, "Style: " + Recipes.styleLabel(rc.style), 0xFF282828, ACCENT);
        rx = badge(c, rx + 6 * d, y, rc.wbMode == 0 ? "WB: Keep" : rc.wbMode == 1 ? "WB: Auto" : "WB: " + rc.kelvin + "K", 0xFF282828, 0xCCFFFFFF);
        rx = badge(c, rx + 6 * d, y, rc.dro == 0 ? "DRO: Off" : rc.dro == 1 ? "DRO: Auto" : "DRO: Lv" + rc.dro, 0xFF282828, 0xCCFFFFFF);
        if (rc.ev != 0) {
            badge(c, rx + 6 * d, y, "EV: " + Params.fmt(Params.R_EV, rc.ev, null), 0xFF282828, 0xCCFFFFFF);
        }
    }

    private float badge(Canvas c, float x, float y, String text, int bgCol, int txtCol) {
        float tw = small.measureText(text) + 10 * d;
        r.set(x, y - 11 * d, x + tw, y + 4 * d);
        tagBg.setColor(bgCol);
        c.drawRoundRect(r, 2 * d, 2 * d, tagBg);
        small.setColor(txtCol);
        c.drawText(text, x + 5 * d, y, small);
        return x + tw;
    }

    private void scrollbar(Canvas c, float x, float top, float height, float width, int first, int visible, int total) {
        r.set(x, top, x + width, top + height);
        c.drawRoundRect(r, width / 2, width / 2, track);
        float thumbH = Math.max(12 * d, height * visible / total);
        float thumbY = top + (height - thumbH) * first / Math.max(1, total - visible);
        r.set(x, thumbY, x + width, thumbY + thumbH);
        c.drawRoundRect(r, width / 2, width / 2, thumb);
    }
}
