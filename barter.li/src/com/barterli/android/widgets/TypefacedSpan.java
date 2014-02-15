package com.barterli.android.widgets;

import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

/**
 * @author Vinay S Shenoy 
 * Custom typefaced text for action bar
 */
public class TypefacedSpan extends MetricAffectingSpan {

    private final Typeface mTypeface;

    public TypefacedSpan(final Context context, final String typefaceName) {

        mTypeface = TypefaceCache.get(context.getAssets(), typefaceName);
    }

    @Override
    public void updateMeasureState(final TextPaint p) {

        p.setTypeface(mTypeface);

        // Note: This flag is required for proper typeface rendering
        p.setFlags(p.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);

    }

    @Override
    public void updateDrawState(final TextPaint tp) {

        tp.setTypeface(mTypeface);

        // Note: This flag is required for proper typeface rendering
        tp.setFlags(tp.getFlags() | Paint.SUBPIXEL_TEXT_FLAG);
    }

}
