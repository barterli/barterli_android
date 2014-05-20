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

import android.content.Context;
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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import li.barter.utils.Logger;

/**
 * Custom ImageView to draw the content in a circular way. Works ONLY when
 * {@link #setImageBitmap(android.graphics.Bitmap)} method is used
 * 
 * @author Vinay S Shenoy
 */
public class CircleImageView extends ImageView {

    private static final String TAG = "CircleImageView";

    /**
     * @param context
     */
    public CircleImageView(Context context) {
        super(context);
    }

    /**
     * @param context
     * @param attrs
     */
    public CircleImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param context
     * @param attrs
     * @param defStyle
     */
    public CircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private class StreamDrawable extends Drawable {
        private static final boolean USE_VIGNETTE = true;

        private final float          mCornerRadius;
        private final RectF          mRect        = new RectF();
        private BitmapShader         mBitmapShader;
        private final Paint          mPaint;
        private final int            mMargin;

        StreamDrawable(Bitmap bitmap, float cornerRadius, int margin) {
            mCornerRadius = cornerRadius;

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

            if (USE_VIGNETTE) {
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
            //TODO Update corner radius and margin to be configurable
            setImageDrawable(new StreamDrawable(bm, 25.0f, 10));
        }
    }

}
