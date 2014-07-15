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
public class CircleImageView extends ImageView {

    private static final int   DEFAULT_CORNER_RADIUS = 25;               //dips
    private static final int   DEFAULT_MARGIN        = 0;                //dips
    private static final int   DEFAULT_BORDER_WIDTH  = 0;                //dips
    private static final int   DEFAULT_BORDER_COLOR  = Color.BLACK;
    private static final float DEFAULT_SHADOW_RADIUS = 0f;
    private static final float DEFAULT_SHADOW_DX     = 0f;
    private static final float DEFAULT_SHADOW_DY     = 2.0f;
    private static final int   DEFAULT_SHADOW_COLOR  = Color.DKGRAY;

    private static final String TAG = "CircleImageView";

    private CircleTarget mCircleTarget;
    private int          mCornerRadius;
    private int          mMargin;
    private int          mBorderWidth;
    private int          mBorderColor;
    private float        mShadowRadius;
    private float        mShadowDx;
    private float        mShadowDy;
    private int          mShadowColor;

    /**
     * @param context
     */
    public CircleImageView(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * @param context
     * @param attrs
     */
    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
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
        mMargin = (int) (DEFAULT_MARGIN * density + 0.5f);
        mBorderWidth = (int) (DEFAULT_BORDER_WIDTH * density + 0.5f);
        mBorderColor = DEFAULT_BORDER_COLOR;

        if (attrs != null) {
            TypedArray styledAttrs = context
                    .obtainStyledAttributes(attrs, R.styleable.CircleImageView);
            mCornerRadius = (int) styledAttrs
                    .getDimension(R.styleable.CircleImageView_cornerRadius, mCornerRadius);
            mMargin = (int) styledAttrs
                    .getDimension(R.styleable.CircleImageView_margin, mMargin);
            mBorderWidth = (int) styledAttrs
                    .getDimension(R.styleable.CircleImageView_borderWidth, mBorderWidth);
            mBorderColor = styledAttrs
                    .getColor(R.styleable.CircleImageView_borderColor, mBorderColor);
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

    public CircleTarget getTarget() {
        if (mCircleTarget == null) {
            mCircleTarget = new CircleTarget(this);
        }
        return mCircleTarget;
    }

    private class StreamDrawable extends Drawable {

        private final float mCornerRadius;
        private final RectF mRect = new RectF();

        /* Rect used for drawing the actual inner image if a border has been set */
        private       RectF        mBorderRect;
        private       RectF        mImageRect;
        private       BitmapShader mBitmapShader;
        private final Paint        mPaint;
        private final int          mMargin;
        private final int          mBorderWidth;
        private final int          mBorderColor;

        StreamDrawable(Bitmap bitmap, float cornerRadius, int margin, int borderWidth, int borderColor) {
            mCornerRadius = cornerRadius;
            mBitmapShader = getShaderForBitmap(bitmap);
            mBorderWidth = borderWidth;
            mBorderColor = borderColor;

            if (borderWidth > 0) {
                mBorderRect = new RectF();
                mImageRect = new RectF();
            }

            if (mShadowRadius > 0f) {

                //We need to set layer type for shadows to work
                setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setShader(mBitmapShader);

            mMargin = margin;
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
            mRect.set(mMargin, mMargin, bounds.width() - mMargin, bounds
                    .height() - mMargin);

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
                if(mShadowRadius > 0f) {
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

    @Override
    public void setImageBitmap(Bitmap bm) {

        final Drawable content = getDrawable();

        if (content instanceof StreamDrawable) {
            ((StreamDrawable) content).updateBitmap(bm);
        } else {
            setImageDrawable(null);
            setImageDrawable(new StreamDrawable(bm, mCornerRadius, mMargin, mBorderWidth, mBorderColor));
        }
    }

    /**
     * Custom {@link Target} implementation for loading images via {@link Picasso}
     *
     * @author Vinay S Shenoy
     */
    public static class CircleTarget implements Target {

        private CircleImageView mCircleImageView;

        public CircleTarget(CircleImageView circleImageView) {
            if (circleImageView == null) {
                throw new IllegalArgumentException("ImageView is null");
            }

            mCircleImageView = circleImageView;
        }

        @Override
        public boolean equals(Object o) {

            if (o == null || !(o instanceof CircleTarget)) {
                return false;
            } else {
                CircleImageView theirImageView = ((CircleTarget) o)
                        .getImageView();
                return mCircleImageView.equals(theirImageView);
            }
        }

        public CircleImageView getImageView() {
            return mCircleImageView;
        }

        @Override
        public int hashCode() {
            return 41 * mCircleImageView.hashCode();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            // TODO Use custom StreamDrawable instead

        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, LoadedFrom from) {
            mCircleImageView.setImageBitmap(bitmap);
        }

        @Override
        public void onPrepareLoad(Drawable prepareDrawable) {
            // TODO what to do here?

        }

    }

}
