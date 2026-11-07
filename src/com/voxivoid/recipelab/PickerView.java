package com.voxivoid.recipelab;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
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
 * Optimized with fast memory caching (RGB_565) for 0ms recipe switching.
 */
public class PickerView extends View {
    private static final int ACCENT = 0xFFF2B85C, INK = 0xFF1A1208;

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
    private final Paint star = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tagBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outline = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint photoPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint[] softKeyMark;

    private final RectF r = new RectF();
    private final Rect srcRect = new Rect();
    private final RectF dstRect = new RectF();
    private final float d;
    private final Legend legend;

    private Keys.Caps caps = Keys.Caps.UNKNOWN;
    private int selected = 0, column = 1, group = 0;
    private List<Integer> favs = new ArrayList<Integer>();
    private Library lib = new Library();
    private boolean card = true;

    // Fast in-memory cache for decoded sample bitmaps (RGB_565)
    // Fast in-memory cache for decoded sample bitmaps (RGB_565, 160x107)
    private final Bitmap[] sampleCache = new Bitmap[78];
    private Bitmap appIconBitmap = null;

    public PickerView(Context c, AttributeSet a) {
        super(c, a);
        d = c.getResources().getDisplayMetrics().density;
        legend = new Legend(d);
        softKeyMark = Skin.metalMark();

        bg.setColor(0xFF101010); // Solid opaque dark background (no alpha lag)
        sel.setColor(0x33F2B85C);
        outline.setColor(ACCENT);
        outline.setStyle(Paint.Style.STROKE);
        outline.setStrokeWidth(1.5f * d);

        head.setColor(0xFFFFFFFF);
        head.setTextSize(12 * d);
        head.setFakeBoldText(true);

        titlePaint.setColor(ACCENT);
        titlePaint.setTextSize(17 * d);
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

        try {
            appIconBitmap = BitmapFactory.decodeResource(c.getResources(), R.drawable.ic_launcher);
        } catch (Throwable t) {}
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

    private Bitmap getSample(int idx) {
        if (idx < 0 || idx >= 77) return null;
        if (sampleCache[idx] != null && !sampleCache[idx].isRecycled()) {
            return sampleCache[idx];
        }
        try {
            String pth = String.format(Locale.US, "samples/%02d.jpg", idx + 1);
            InputStream is = getContext().getAssets().open(pth);
            byte[] buf = new byte[is.available()];
            int read = 0;
            while (read < buf.length) {
                int r = is.read(buf, read, buf.length - read);
                if (r < 0) break;
                read += r;
            }
            is.close();
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            opts.inSampleSize = 2; // 160x107 exact 1:1 screen preview match (only 34KB)
            Bitmap bm = BitmapFactory.decodeByteArray(buf, 0, read, opts);
            sampleCache[idx] = bm;
            return bm;
        } catch (Throwable t) {
            return null;
        }
    }

    @Override
    protected void onDraw(Canvas c) {
        float w = getWidth(), h = getHeight(), pad = 12 * d;
        c.drawRect(0, 0, w, h, bg);

        int g = group;
        int count = Favorites.groupCount(g, favs, lib);
        float colX = w * 0.50f; // Boundary between 3:2 detail area and right vertical carousel
        float topH = 34 * d, footH = 32 * d;
        float listTop = topH + 8 * d, listBottom = h - footH - 4 * d, listH = listBottom - listTop;

        // ---- 1. Top Header Bar: App Icon + Title + Category Indicator + BACK ●
        if (appIconBitmap != null && !appIconBitmap.isRecycled()) {
            r.set(pad, pad - 2 * d, pad + 18 * d, pad + 16 * d);
            srcRect.set(0, 0, appIconBitmap.getWidth(), appIconBitmap.getHeight());
            c.drawBitmap(appIconBitmap, srcRect, r, photoPaint);
        }

        c.drawText("Recipe Selection", pad + 24 * d, pad + 12 * d, head);

        // Category selector indicator: ◄ ALL 3 / 18 ►
        String grpLabel = "◄  " + Favorites.groupName(g).toUpperCase() + "  " + (Favorites.positionIn(g, selected, favs, lib) + 1) + " / " + count + "  ►";
        small.setColor(0xCCFFFFFF);
        c.drawText(grpLabel, pad + 130 * d, pad + 12 * d, small);

        // Top right SoftKey 1: BACK ● (2.5x enlarged)
        Skin.softKey(c, "BACK", w - 18 * d, 18 * d, head, softKeyMark, d);

        c.drawLine(pad, topH, w - pad, topH, rule);

        // ---- 2. Left / Center Area: Recipe Title, Center Sample Photo & 3x3 Summary Badges
        if (lib.valid(selected)) {
            Recipes.Recipe rc = lib.get(selected);
            float lx = pad, ly = topH + 16 * d;
            float maxTitleW = colX - lx - 8 * d;

            // Big Recipe Title (Sony Amber) with automatic 2-line wrap (especially for "(...)" suffixes)
            String fullTitle = Recipes.displayName(rc);
            float titleBottomY = drawWrappedTitle(c, fullTitle, lx, ly, maxTitleW);

            // Single center sample photo frame positioned exactly between title bottom and 3x3 summary badges
            float topBound = titleBottomY + 4 * d;
            float bottomBound = listBottom - 54 * d; // Top of the 3-row summary badge block
            float cx = colX * 0.50f;
            float cy = (topBound + bottomBound) / 2.0f;
            float maxH = Math.max(48 * d, bottomBound - topBound - 4 * d);
            drawCenterSampleFrame(c, cx, cy, maxH, colX - 2 * pad, selected);

            // Bottom summary badges (3 rows x 3 boxes)
            drawSummaryRow(c, lx, listBottom, rc, colX);
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
                boolean isFav = favs.contains(idx);
                float maxItemW = (xr - (isFav ? 16 * d : 5 * d)) - (colX + 12 * d);
                String rName = Recipes.displayName(rk);
                float baseItemSize = 11.5f * d;
                item.setTextSize(baseItemSize);
                float rw = item.measureText(rName);
                if (rw > maxItemW && rw > 0) {
                    float scaled = Math.max(8.2f * d, baseItemSize * maxItemW / rw);
                    item.setTextSize(scaled);
                    if (item.measureText(rName) > maxItemW) {
                        while (rName.length() > 3 && item.measureText(rName + "..") > maxItemW) {
                            rName = rName.substring(0, rName.length() - 1);
                        }
                        rName = rName + "..";
                    }
                }
                c.drawText(rName, colX + 12 * d, y + 16 * d, item);
                item.setTextSize(baseItemSize);

                if (isFav) {
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

        // Right footer SoftKey 2: OPTION · Edit ● (2.5x enlarged)
        head.setColor(ACCENT);
        Skin.softKey(c, "OPTION", w - 18 * d, h - 18 * d, head, softKeyMark, d);
        head.setColor(0xFFFFFFFF);
    }

    /** Draws single center sample photo frame positioned exactly between subtitle and summary badges */
    private void drawCenterSampleFrame(Canvas c, float cx, float cy, float maxH, float maxW, int currentIdx) {
        float fh = Math.max(60 * d, Math.min(maxH, 115 * d));
        float fw = fh * 1.5f; // 3:2 aspect ratio
        if (fw > maxW) {
            fw = maxW;
            fh = fw / 1.5f;
        }

        float mx = cx - fw / 2.0f, my = cy - fh / 2.0f;
        r.set(mx - 2 * d, my - 2 * d, mx + fw + 2 * d, my + fh + 2 * d);
        c.drawRoundRect(r, 3 * d, 3 * d, frameShadow);

        // Draw sample photograph
        Bitmap sampleBm = getSample(currentIdx);
        if (sampleBm != null && !sampleBm.isRecycled()) {
            srcRect.set(0, 0, sampleBm.getWidth(), sampleBm.getHeight());
            dstRect.set(mx, my, mx + fw, my + fh);
            c.drawBitmap(sampleBm, srcRect, dstRect, photoPaint);
        } else {
            r.set(mx, my, mx + fw, my + fh);
            c.drawRoundRect(r, 2.5f * d, 2.5f * d, frameFill);
            Paint p = new Paint(small);
            p.setTextAlign(Paint.Align.CENTER);
            c.drawText("No Preview", cx, cy + 3 * d, p);
        }

        // Crisp white photo border frame
        frameStroke.setColor(0xFFFFFFFF);
        frameStroke.setStrokeWidth(1.2f * d);
        r.set(mx, my, mx + fw, my + fh);
        c.drawRoundRect(r, 2.5f * d, 2.5f * d, frameStroke);
    }

    /** Draws big recipe title, moving "(...)" or overflowing words to line 2 so it never touches the right column */
    private float drawWrappedTitle(Canvas c, String title, float x, float y, float maxW) {
        if (title == null) return y;
        String line1 = title;
        String line2 = null;

        int parenIdx = title.indexOf(" (");
        if (parenIdx > 0 && titlePaint.measureText(title) > maxW) {
            line1 = title.substring(0, parenIdx).trim();
            line2 = title.substring(parenIdx + 1).trim();
        } else if (titlePaint.measureText(title) > maxW) {
            int splitSpace = -1;
            for (int i = 1; i < title.length(); i++) {
                if (title.charAt(i) == ' ') {
                    if (titlePaint.measureText(title, 0, i) <= maxW) {
                        splitSpace = i;
                    } else {
                        break;
                    }
                }
            }
            if (splitSpace > 0) {
                line1 = title.substring(0, splitSpace).trim();
                line2 = title.substring(splitSpace + 1).trim();
            }
        }

        drawScaledTitleLine(c, line1, x, y, maxW);
        if (line2 != null && !line2.isEmpty()) {
            float y2 = y + 15 * d;
            drawScaledTitleLine(c, line2, x, y2, maxW);
            return y2 + 4 * d;
        }
        return y + 4 * d;
    }

    private void drawScaledTitleLine(Canvas c, String text, float x, float y, float maxW) {
        float origSize = 15 * d;
        titlePaint.setTextSize(origSize);
        float tw = titlePaint.measureText(text);
        if (tw > maxW && tw > 0) {
            titlePaint.setTextSize(Math.max(10.5f * d, origSize * maxW / tw));
        }
        c.drawText(text, x, y, titlePaint);
        titlePaint.setTextSize(origSize);
    }

    /** Parameter summary badges arranged into THREE rows, with 3 boxes per row (3x3 grid) */
    private void drawSummaryRow(Canvas c, float x, float listBottom, Recipes.Recipe rc, float colX) {
        float totalW = colX - x - 8 * d;
        float gap = 3.5f * d;
        float boxW = (totalW - 2 * gap) / 3.0f;
        float boxH = 14.5f * d;

        float y1 = listBottom - 37 * d;
        float y2 = listBottom - 20.5f * d;
        float y3 = listBottom - 4 * d;

        // Row 1 (3 boxes): [RAW+J] [Style / PE] [WB]
        fixedBadge(c, x, y1, boxW, boxH, "RAW+J", 0xFF262626, 0xCCFFFFFF);
        String styleOrPe = rc.pe != 0 ? Recipes.peLabel(rc.pe) : Recipes.styleLabel(rc.style);
        fixedBadge(c, x + boxW + gap, y1, boxW, boxH, styleOrPe, 0xFF262626, ACCENT);
        String wbText = rc.wbMode == 0 ? "WB: Keep" : rc.wbMode == 1 ? "WB: Auto" : rc.kelvin + "K";
        fixedBadge(c, x + (boxW + gap) * 2, y1, boxW, boxH, wbText, 0xFF262626, 0xCCFFFFFF);

        // Row 2 (3 boxes): [A-B] [G-M] [Sat/Con/Shp]
        String abVal = rc.ab == 0 ? "0" : (rc.ab > 0 ? "A" + rc.ab : "B" + (-rc.ab));
        fixedBadge(c, x, y2, boxW, boxH, "A-B: " + abVal, 0xFF262626, rc.ab != 0 ? ACCENT : 0xCCFFFFFF);
        String gmVal = rc.gm == 0 ? "0" : (rc.gm > 0 ? "G" + rc.gm : "M" + (-rc.gm));
        fixedBadge(c, x + boxW + gap, y2, boxW, boxH, "G-M: " + gmVal, 0xFF262626, rc.gm != 0 ? ACCENT : 0xCCFFFFFF);
        String scsText = (rc.sat > 0 ? "+" : "") + rc.sat + "/" + (rc.con > 0 ? "+" : "") + rc.con + "/" + (rc.sharp > 0 ? "+" : "") + rc.sharp;
        fixedBadge(c, x + (boxW + gap) * 2, y2, boxW, boxH, scsText, 0xFF262626, 0xCCFFFFFF);

        // Row 3 (3 boxes): [DRO] [EV] [Brand]
        String droText = rc.dro == 0 ? "DRO: Off" : rc.dro == Recipes.DRO_AUTO ? "DRO: Auto" : "DRO: Lv" + rc.dro;
        fixedBadge(c, x, y3, boxW, boxH, droText, 0xFF262626, 0xCCFFFFFF);
        String evText = "EV " + Params.fmt(Params.R_EV, rc.ev, null);
        fixedBadge(c, x + boxW + gap, y3, boxW, boxH, evText, 0xFF262626, rc.ev != 0 ? ACCENT : 0xCCFFFFFF);
        String brandText = Recipes.groupLabel(rc.group);
        fixedBadge(c, x + (boxW + gap) * 2, y3, boxW, boxH, brandText, 0xFF262626, 0xCCFFFFFF);
    }

    private void fixedBadge(Canvas c, float bx, float by, float bw, float bh, String text, int bgCol, int txtCol) {
        r.set(bx, by - bh + 2 * d, bx + bw, by + 2 * d);
        tagBg.setColor(bgCol);
        c.drawRoundRect(r, 2.5f * d, 2.5f * d, tagBg);
        frameStroke.setColor(0x44FFFFFF);
        frameStroke.setStrokeWidth(0.8f * d);
        c.drawRoundRect(r, 2.5f * d, 2.5f * d, frameStroke);

        Paint p = new Paint(small);
        p.setTextAlign(Paint.Align.CENTER);
        p.setColor(txtCol);
        float tw = p.measureText(text);
        if (tw > bw - 4 * d) p.setTextSize(Math.max(6.5f * d, p.getTextSize() * (bw - 4 * d) / tw));
        c.drawText(text, bx + bw / 2, by - 2.5f * d, p);
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
