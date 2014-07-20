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
    private static final float   DEFAULT_SHADOW_RADIUS = 0f;
    private static final float   DEFAULT_SHADOW_DX     = 1.0f;
    private static final float   DEFAULT_SHADOW_DY     = 2.0f;
    private static final int     DEFAULT_SHADOW_COLOR  = Color.DKGRAY;
    private static final boolean DEFAULT_FULL_CIRCLE   = false;


    private static final String TAG = "CircleImageView";

    private CircleTarget mCircleTarget;
    private int          mCornerRadius;
    private int          mBorderWidth;
    private int          mBorderColor;
    private float        mShadowRadius;
    private float        mShadowDx;
    private float        mShadowDy;
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

        final float density = context.getResources().getDisplayMetrics().density;
        mCornerRadius = (int) (DEFAULT_CORNER_RADIUS * density + 0.5f);
        mBorderWidth = (int) (DEFAULT_BORDER_WIDTH * density + 0.5f);
        mBorderColor = DEFAULT_BORDER_COLOR;
        mFullCircle = DEFAULT_FULL_CIRCLE;

        if (attrs != null) {
            TypedArray styledAttrs = context
                    .obtainStyledAttributes(attrs, R.styleable.RoundedCornerImageView);
            mCornerRadius = (int) styledAttrs
                    .getDimension(R.styleable.RoundedCornerImageView_cornerRadius, mCornerRadius);
            mBorderWidth = (int) styledAttrs
                    .getDimension(R.styleable.RoundedCornerImageView_borderWidth, mBorderWidth);
            mBorderColor = styledAttrs
                    .getColor(R.styleable.RoundedCornerImageView_borderColor, mBorderColor);
            mFullCircle = styledAttrs
                    .getBoolean(R.styleable.RoundedCornerImageView_fullCircle, mFullCircle);
            styledAttrs.recycle();

            final int[] shadowAttributes = new int[]{
                    android.R.attr.shadowColor,
                    android.R.attr.shadowDx,
                    android.R.attr.shadowDy,
                    android.R.attr.shadowRadius
            };
            styledAttrs = context.obtainStyledAttributes(
                    attrs,
                    shadowAttributes
            );

            //The attributes have to parsed in the same order
            mShadowColor = styledAttrs.getColor(0, DEFAULT_SHADOW_COLOR);
            mShadowDx = styledAttrs.getFloat(1, DEFAULT_SHADOW_DX);
            mShadowDy = styledAttrs.getFloat(2, DEFAULT_SHADOW_DY);
            mShadowRadius = styledAttrs.getFloat(3, DEFAULT_SHADOW_RADIUS);

            styledAttrs.recycle();
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        final float density = getContext().getResources().getDisplayMetrics().density;
        int requiredWidth = getPaddingLeft() + getPaddingRight() + getSuggestedMinimumWidth();
        int requiredHeight = getPaddingBottom() + getPaddingTop() + getSuggestedMinimumHeight();
        int wMeasureSpec = widthMeasureSpec;
        int hMeasureSpec = heightMeasureSpec;

        //We can use normal measuring in this case
        requiredWidth = resolveSizeAndState(
                requiredWidth,
                wMeasureSpec,
                1
        );
        requiredHeight = resolveSizeAndState(
                requiredHeight,
                hMeasureSpec,
                0
        );

        /* If it's required to be a circle, set both height & width to be the
         * minimum of the two. This needs to be done BEFORE the bounds calculation
         * for shadows
         * */
        if (mFullCircle) {

            if (requiredHeight > requiredWidth) {
                requiredHeight = requiredWidth;
                hMeasureSpec = widthMeasureSpec;
            } else {
                requiredWidth = requiredHeight;
                wMeasureSpec = hMeasureSpec;
            }

        }

        if (mShadowDx != 0) {
            //The shadow has some x-offset. We need to set the new width
            final int absShadowDx = (int) (mShadowDx * density + 0.5f);
            requiredWidth += absShadowDx;
            requiredWidth = resolveSizeAndState(
                    requiredWidth,
                    wMeasureSpec,
                    1);
        }

        if (mShadowDy != 0) {
            //The shadow has some y-offset. We need to set the new height
            final int absShadowDy = (int) (mShadowDy * density + 0.5f);
            requiredHeight += absShadowDy;
            requiredHeight = resolveSizeAndState(
                    requiredHeight,
                    hMeasureSpec,
                    0
            );
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
            setImageDrawable(new StreamDrawable(bm, mCornerRadius, mFullCircle, mBorderWidth, mBorderColor));
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

        /** Rect used for drawing the shadow */
        private RectF mShadowRect;

        /** Rect used for drawing the border */
        private RectF mBorderRect;

        /** Rect used for drawing the actual image */
        private RectF mImageRect;

        private       BitmapShader mBitmapShader;
        private final Paint        mPaint;
        private       int          mBorderWidth;
        private       int          mBorderColor;
        private       boolean      mFullCircle;
        private       float        mCornerRadius;


        StreamDrawable(Bitmap bitmap, float cornerRadius, boolean fullCircle, int borderWidth, int borderColor) {
            mCornerRadius = cornerRadius;
            mBitmapShader = getShaderForBitmap(bitmap);
            mBorderWidth = borderWidth;
            mBorderColor = borderColor;
            mFullCircle = fullCircle;

            if (borderWidth > 0) {
                mBorderRect = new RectF();
                mImageRect = new RectF();
            }

            if (mShadowRadius > 0f) {

                mShadowRect = new RectF();
                if (Build.VERSION.SDK_INT >= 14) {
                    /* We need to set layer type for shadows to work
                     * on ICS and above
                     */
                    setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                }
            }

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setShader(mBitmapShader);

        }

        /**
         * Creates a bitmap shader with a bitmap
         */
        private BitmapShader getShaderForBitmap(Bitmap bitmap) {
            return new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        }

        public void updateBitmap(Bitmap bitmap) {

            mBitmapShader = getShaderForBitmap(bitmap);
            mPaint.setShader(mBitmapShader);
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

            //Needs to be called before initializing the border rects
            if (mShadowRadius > 0f) {
                adjustOuterRectForShadows();
            }

            if (mBorderWidth > 0) {
                initRectsForBorders();
            }

        }

        /**
         * For borders, we draw both the border and the bitmap separately to prevent overdraw.
         * <p/>
         * We need to create separate Rects for drawing both items. This method initializes them
         */
        private void initRectsForBorders() {

            mBorderRect.set(mRect.left + mBorderWidth, mRect.top
                    + mBorderWidth, mRect.right - mBorderWidth, mRect.bottom
                                    - mBorderWidth);
            mImageRect.set(mBorderRect.left + mBorderWidth, mBorderRect.top
                    + mBorderWidth, mBorderRect.right - mBorderWidth, mBorderRect.bottom
                                   - mBorderWidth);
        }

        /**
         * If we enable shadows, we need to adjust the outer rect to account for the shadow size in
         * order to prevent clipping
         */
        private void adjustOuterRectForShadows() {

            //Adjust for dX
            if (mShadowDx > 0f) {

                /*Shadows will be offset to the right, we need to increase the right bounds*/
                mRect.right -= mShadowRadius;

            } else if (mShadowDx < 0f) {

                /*Shadows will be offset to the left, we need to increase the left bounds*/
                mRect.left += mShadowRadius;
            }

            //Adjust for dY
            if (mShadowDy > 0f) {

                /*Shadows will be offset to the bottom, we need to increase the bottom bounds*/
                mRect.bottom -= mShadowRadius;
            } else if (mShadowDy < 0f) {

                /*Shadows will be offset to the top, we need to increase the top bounds*/
                mRect.top += mShadowRadius;
            }


        }

        @Override
        public void draw(Canvas canvas) {

            Shader curShader = mPaint.getShader();
            if (mShadowRadius > 0f) {
                mPaint.setShadowLayer(mShadowRadius, mShadowDx, mShadowDy, mShadowColor);
            }

            if (mBorderWidth > 0) {

                //Draw the border
                mPaint.setShader(null);
                mPaint.setColor(mBorderColor);
                mPaint.setStrokeWidth(mBorderWidth);
                mPaint.setStyle(Paint.Style.STROKE);
                canvas.drawRoundRect(mBorderRect, mCornerRadius, mCornerRadius, mPaint);

                //Disable shadows because we don't want the shadow to be drawn again using the bitmap shader
                mPaint.setShadowLayer(0f, 0f, 0f, mShadowColor);

                mPaint.setShader(curShader);
                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawRoundRect(mImageRect, mCornerRadius, mCornerRadius, mPaint);

            } else {
                //Draw an empty rect to draw the shadow
                if (mShadowRadius > 0f) {
                    mPaint.setShader(null);
                    mPaint.setStyle(Paint.Style.STROKE);
                    mPaint.setStrokeWidth(0);
                    canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
                    mPaint.setShader(curShader);
                }

                mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
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
