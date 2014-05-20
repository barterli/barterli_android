/**
 * Copyright 2014, barter.li
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package li.barter.widgets;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Picasso.LoadedFrom;
import com.squareup.picasso.Target;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.ComposeShader;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import li.barter.R;
import li.barter.utils.Logger;

/**
 * Custom ImageView to draw the content in a circular way. Works ONLY when
 * {@link #setImageBitmap(android.graphics.Bitmap)} method is used
 * 
 * @author Vinay S Shenoy
 */
public class CircleImageView extends ImageView {

    private static final int     DEFAULT_CORNER_RADIUS = 25;               //dips
    private static final int     DEFAULT_MARGIN        = 0;                //dips
    private static final boolean DEFAULT_USE_VIGNETTE  = false;

    private static final String  TAG                   = "CircleImageView";

    private CircleTarget         mCircleTarget;
    private int                  mCornerRadius;
    private int                  mMargin;
    private boolean              mUseVignette;

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
        mUseVignette = DEFAULT_USE_VIGNETTE;

        if (attrs != null) {
            final TypedArray styledAttrs = context
                            .obtainStyledAttributes(attrs, R.styleable.CircleImageView);
            mCornerRadius = (int) styledAttrs
                            .getDimension(R.styleable.CircleImageView_cornerRadius, mCornerRadius);
            mMargin = (int) styledAttrs
                            .getDimension(R.styleable.CircleImageView_margin, mMargin);
            mUseVignette = styledAttrs
                            .getBoolean(R.styleable.CircleImageView_useVignette, mUseVignette);
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

        private final float   mCornerRadius;
        private final RectF   mRect = new RectF();
        private BitmapShader  mBitmapShader;
        private final Paint   mPaint;
        private final int     mMargin;
        private final boolean mUseVignette;

        //TODO Add border for cropped image
        StreamDrawable(Bitmap bitmap, float cornerRadius, int margin, boolean useVignette) {
            mCornerRadius = cornerRadius;
            mUseVignette = useVignette;
            mBitmapShader = getShaderForBitmap(bitmap);

            mPaint = new Paint();
            mPaint.setAntiAlias(true);
            mPaint.setShader(mBitmapShader);

            mMargin = margin;
        }

        /**
         * Creates a bitmap shader with a bitmap
         * 
         * @param bitmap
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

            if (mUseVignette) {
                RadialGradient vignette = new RadialGradient(mRect.centerX(), mRect
                                .centerY() * 1.0f / 0.7f, mRect.centerX() * 1.3f, new int[] {
                        0, 0, 0x7f000000
                }, new float[] {
                        0.0f, 0.7f, 1.0f
                }, Shader.TileMode.CLAMP);

                Matrix oval = new Matrix();
                oval.setScale(1.0f, 0.7f);
                vignette.setLocalMatrix(oval);

                mPaint.setShader(new ComposeShader(mBitmapShader, vignette, PorterDuff.Mode.SRC_OVER));
            }
        }

        @Override
        public void draw(Canvas canvas) {
            canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mPaint);
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

        Logger.v(TAG, "Set Image Bitmap");
        final Drawable content = getDrawable();

        if (content instanceof StreamDrawable) {

            ((StreamDrawable) content).updateBitmap(bm);
        } else {
            setImageDrawable(null);
            setImageDrawable(new StreamDrawable(bm, mCornerRadius, mMargin, mUseVignette));
        }
    }

    /**
     * Custom {@link Target} implementation for loading images via
     * {@link Picasso}
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
