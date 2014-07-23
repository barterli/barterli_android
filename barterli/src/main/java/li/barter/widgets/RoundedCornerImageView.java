/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import li.barter.R;

/**
 * Custom ImageView to draw the content in a circular way. Works ONLY when {@link
 * #setImageBitmap(android.graphics.Bitmap)} method is used
 *
 * @author Vinay S Shenoy
 */
public class RoundedCornerImageView extends ImageView {

    private static final int     DEFAULT_CORNER_RADIUS = 5;               //dips
    private static final int     DEFAULT_BORDER_WIDTH  = 0;                //dips
    private static final int     DEFAULT_BORDER_COLOR  = Color.BLACK;
    private static final int     DEFAULT_SHADOW_WIDTH  = 0; //dips
    private static final int     DEFAULT_SHADOW_COLOR  = 0xB3444444; //70% dark gray
    private static final boolean DEFAULT_FULL_CIRCLE   = false;
    private static final float   DEFAULT_SHADOW_RADIUS = 0.5f;


    private static final String TAG = "CircleImageView";

    private CircleTarget mCircleTarget;
    private int          mCornerRadius;
    private int          mBorderWidth;
    private int          mBorderColor;
    private int          mShadowWidth;
    private float        mShadowRadius;
    private int          mShadowColor;
    private boolean      mFullCircle;

    /**
     * @param context
     */
    public RoundedCornerImageView(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public RoundedCornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public RoundedCornerImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     */
    private void init(Context context, AttributeSet attrs) {

        mCornerRadius = dpToPx(DEFAULT_CORNER_RADIUS);
        int borderWidthInDips = DEFAULT_BORDER_WIDTH;
        mBorderColor = DEFAULT_BORDER_COLOR;
        mFullCircle = DEFAULT_FULL_CIRCLE;
        int shadowWidthInDips = DEFAULT_SHADOW_WIDTH;
        mShadowColor = DEFAULT_SHADOW_COLOR;
        mShadowRadius = DEFAULT_SHADOW_RADIUS;


        if (attrs != null) {
            TypedArray styledAttrs = context
                    .obtainStyledAttributes(attrs, R.styleable.RoundedCornerImageView);
            mCornerRadius = (int) styledAttrs
                    .getDimension(R.styleable.RoundedCornerImageView_cornerRadius, mCornerRadius);
            mBorderColor = styledAttrs
                    .getColor(R.styleable.RoundedCornerImageView_borderColor, mBorderColor);
            mFullCircle = styledAttrs
                    .getBoolean(R.styleable.RoundedCornerImageView_fullCircle, mFullCircle);
            mShadowColor = styledAttrs
                    .getColor(R.styleable.RoundedCornerImageView_shadowColor, mShadowColor);
            mShadowRadius = styledAttrs
                    .getFloat(R.styleable.RoundedCornerImageView_shadowRadius, mShadowRadius);

            int dimension = (int) styledAttrs
                    .getDimension(R.styleable.RoundedCornerImageView_borderWidth, borderWidthInDips);
            borderWidthInDips = pxToDp(dimension);
            dimension = (int) styledAttrs
                    .getDimension(R.styleable.RoundedCornerImageView_shadowWidth, shadowWidthInDips);
            shadowWidthInDips = pxToDp(dimension);

            styledAttrs.recycle();
        }

        clampBorderAndShadowWidths(borderWidthInDips, shadowWidthInDips);
        mBorderWidth = dpToPx(borderWidthInDips);
        mShadowWidth = dpToPx(shadowWidthInDips);

    }

    /**
     * Converts a raw pixel value to a dp value, based on the device density
     */
    private static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    /**
     * Converts a raw dp value to a pixel value, based on the device density
     */
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }


    /**
     * Clamps the widths of the border & shadow(if set) to a sane max. This modifies the passed in
     * paramters if needed
     * <p/>
     * Currently, border is allowed to be [1,5] dp and shadow is allowed to be [1,3] dp
     * <p/>
     * If they exceed their maximums, they are set to the max value. If they go below the minimums,
     * they are set to 0
     *
     * @param borderWidthInDips The set border width in dips
     * @param shadowWidthInDips The set shadow width in dips
     */
    private void clampBorderAndShadowWidths(int borderWidthInDips, int shadowWidthInDips) {

        if (borderWidthInDips > 5) {
            borderWidthInDips = 5;
        } else if (borderWidthInDips < 0) {
            borderWidthInDips = 0;
        }

        if (shadowWidthInDips > 3) {
            shadowWidthInDips = 3;
        } else if (shadowWidthInDips < 0) {
            shadowWidthInDips = 0;
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        int requiredWidth = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int requiredHeight = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();

        //We can use normal measuring in this case
        requiredWidth = resolveSizeAndState(
                requiredWidth,
                widthMeasureSpec,
                1
        );
        requiredHeight = resolveSizeAndState(
                requiredHeight,
                heightMeasureSpec,
                0
        );

        /* If it's required to be a circle, set both height & width to be the
         * minimum of the two.
         * */
        if (mFullCircle) {

            if (requiredHeight > requiredWidth) {
                requiredHeight = requiredWidth;
            } else {
                requiredWidth = requiredHeight;
            }

        }

        setMeasuredDimension(requiredWidth, requiredHeight);

    }

    public CircleTarget getTarget() {
        if (mCircleTarget == null) {
            mCircleTarget = new CircleTarget(this);
        }
        return mCircleTarget;
    }

    @Override
    public void setImageBitmap(Bitmap bm) {

        final Drawable content = getDrawable();

        if (content instanceof StreamDrawable) {
            ((StreamDrawable) content).updateBitmap(bm);
        } else {
            setImageDrawable(null);
            setImageDrawable(new StreamDrawable(bm, mCornerRadius, mFullCircle, mBorderWidth, mBorderColor, mShadowWidth, mShadowColor, mShadowRadius));
        }
    }

    /**
     * Custom {@link Target} implementation for loading images via {@link Picasso}
     *
     * @author Vinay S Shenoy
     */
    public static class CircleTarget implements Target {

        private RoundedCornerImageView mRoundedCornerImageView;

        public CircleTarget(RoundedCornerImageView roundedCornerImageView) {
            if (roundedCornerImageView == null) {
                throw new IllegalArgumentException("ImageView is null");
            }

            mRoundedCornerImageView = roundedCornerImageView;
        }

        @Override
        public boolean equals(Object o) {

            if (o == null || !(o instanceof CircleTarget)) {
                return false;
            } else {
                RoundedCornerImageView theirImageView = ((CircleTarget) o)
                        .getImageView();
                return mRoundedCornerImageView.equals(theirImageView);
            }
        }

        public RoundedCornerImageView getImageView() {
            return mRoundedCornerImageView;
        }

        @Override
        public int hashCode() {
            return 41 * mRoundedCornerImageView.hashCode();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            // TODO Use custom StreamDrawable instead

        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
            mRoundedCornerImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onPrepareLoad(Drawable prepareDrawable) {
            // TODO what to do here?

        }

    }

    /** Custom drawable class that takes care of the actual drawing */
    private class StreamDrawable extends Drawable {

        private final RectF mRect = new RectF();

        /** Rect used for drawing the border */
        private RectF mBorderRect;

        /** Rect used for drawing the actual image */
        private RectF mImageRect;

        /** Rect used for drawing shadows */
        private RectF mShadowRect;

        private       BitmapShader mBitmapShader;
        private final Paint        mPaint;
        private       int          mBorderWidth;
        private       int          mBorderColor;
        private       boolean      mFullCircle;
        private       float        mCornerRadius;
        private       int          mShadowWidth;
        private       int          mShadowColor;
        private       float        mShadowRadius;


        StreamDrawable(Bitmap bitmap, float cornerRadius, boolean fullCircle, int borderWidth, int borderColor, int shadowWidth, int shadowColor, float shadowRadius) {
            mCornerRadius = cornerRadius;
            mBitmapShader = getShaderForBitmap(bitmap);
            mBorderWidth = borderWidth;
            mBorderColor = borderColor;
            mFullCircle = fullCircle;
            mShadowColor = shadowColor;
            mShadowRadius = shadowRadius;
            mShadowWidth = shadowWidth;

            mBorderRect = new RectF();
            mImageRect = new RectF();
            mShadowRect = new RectF();

            if (mShadowWidth > 0f) {

                if (Build.VERSION.SDK_INT >= 14) {
                    /* We need to set layer type for shadows to work
                     * on ICS and above
                     */
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
            }

            mPaint = new Paint();
            mPaint.setAntiAlias(true);

        }

        /**
         * Creates a bitmap shader with a bitmap
         */
        private BitmapShader getShaderForBitmap(Bitmap bitmap) {
            return new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        }

        public void updateBitmap(Bitmap bitmap) {

            mBitmapShader = getShaderForBitmap(bitmap);
            invalidate();
        }

        @Override
        protected void onBoundsChange(Rect bounds) {

            super.onBoundsChange(bounds);
            mRect.set(0, 0, bounds.width(), bounds
                    .height());

            if (mFullCircle) {
                mCornerRadius = Math.abs(mRect.left - mRect.right) / 2;
            }

            if(mShadowWidth > 0) {
                initShadowRect();
            }

            if (mBorderWidth > 0) {
                initRectsWithBorders();
            } else {
                initRectsWithoutBorders();
            }

        }

        /**
         * Initializes the Rect for drawing shadows */
        private void initShadowRect() {

            mShadowRect.set(mRect);
            mShadowRect.right -= mShadowWidth;
            mShadowRect.left += mShadowWidth;
            mShadowRect.bottom -= mShadowWidth;
            mShadowRect.top += mShadowWidth;

        }

        /**
         * Initializes the rects without borders, taking shadows into account
         */
        private void initRectsWithoutBorders() {

            mImageRect.set(mRect);
            if (mShadowWidth > 0) {

                /* Shadows will be drawn to the right & bottom,
                 * so adjust the image rect on the right & bottom
                 */
                mImageRect.right -= mShadowWidth;
                mImageRect.bottom -= mShadowWidth;
            }
        }

        /**
         * Initialize the rects with borders, taking shadows into account
         */
        private void initRectsWithBorders() {

            mBorderRect.set(mRect);
            mBorderRect.left += mBorderWidth;
            mBorderRect.top += mBorderWidth;
            mBorderRect.right -= mBorderWidth;
            mBorderRect.bottom -= mBorderWidth;

            if (mShadowWidth > 0) {


                /* Shadows will be drawn to the right & bottom,
                 * so adjust the border rect on the right & bottom.
                 *
                 * Since the image rect is calculated from the
                 * border rect, the dimens will be accounted for.
                 */
                mBorderRect.right -= mShadowWidth;
                mBorderRect.bottom -= mShadowWidth;
            }

            mImageRect.set(
                    mBorderRect.left + mBorderWidth,
                    mBorderRect.top + mBorderWidth,
                    mBorderRect.right - mBorderWidth,
                    mBorderRect.bottom - mBorderWidth);
        }

        @Override
        public void draw(Canvas canvas) {

            mPaint.setShader(null);
            drawBordersAndShadow(canvas);
            drawImage(canvas);

        }

        /**
         * Draw the image on the canvas based on the View attributes
         *
         * @param canvas The canvas to draw the image on
         */
        private void drawImage(final Canvas canvas) {

            mPaint.setShader(mBitmapShader);
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            canvas.drawRoundRect(mImageRect, mCornerRadius, mCornerRadius, mPaint);
        }

        /**
         * Draw the borders & shadows on the canvas based on the view attributes
         *
         * @param canvas The canvas to draw the borders on
         */
        private void drawBordersAndShadow(final Canvas canvas) {


            if (mBorderWidth > 0) {
                mPaint.setColor(mBorderColor);
                mPaint.setStrokeWidth(mBorderWidth);
                mPaint.setStyle(Paint.Style.STROKE);

                if (mShadowWidth > 0) {

                    //mPaint.setShadowLayer(mShadowRadius, mShadowWidth, mShadowWidth, mShadowColor);
                }
                canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius, mPaint);
                mPaint.setShadowLayer(0f, 0f, 0f, mShadowColor);
            }

        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(ColorFilter cf) {
            mPaint.setColorFilter(cf);
        }
    }

}
