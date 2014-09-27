/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.utils;

import android.graphics.Bitmap;

import com.squareup.picasso.Transformation;

import li.barter.BarterLiApplication;
import li.barter.R;

/**
 * Class that transforms a Bitmap into a square based on the shortest dimension and a desired avatar
 * size.
 * <p/>
 * This class works by taking desired avatar size(in pixels) in the constructor. When the Bitmap is
 * passed to it, it scales the Bitmap's shortest dimension to the avatar size, maintaining the
 * aspect ratio. In then crops the Bitmap to form a square image
 * <p/>
 * Created by vinay.shenoy on 22/07/14.
 */
public class AvatarBitmapTransformation implements Transformation {

    /**
     * We test if an image is square or not by comparing the width to the height. In some cases, the
     * image might be almost square in which case, it is okay not to crop it and just scale it.
     * <p/>
     * This field will be used to control how to decide whether an image is square or not. In case
     * the ratio of the shortest is >= this value, it is taken as a square image and not cropped
     */
    private static final float MAX_ALLOWED_SQUARE_FACTOR = 0.95f;

    /**
     * An enum that indicates the class of avatar this transformation generates
     */
    public enum AvatarSize {
        LARGE(R.dimen.avatar_large, "avatar_large"),
        MEDIUM(R.dimen.avatar_medium, "avatar_medium"),
        SMALL(R.dimen.avatar_small, "avatar_small"),
        AB_CHAT(R.dimen.avatar_ab_chat, "avatar_ab_chat"),
        X_SMALL(R.dimen.avatar_x_small, "avatar_x_small");

        public final int    dimenResId;
        public final String key;

        private AvatarSize(int dimenResId, String key) {
            this.dimenResId = dimenResId;
            this.key = key;
        }
    }

    /**
     * Desired avatar size
     */
    private AvatarSize mAvatarSize;

    /**
     * Construct an instance of AvatarTransformation method, setting the desired avatar size
     *
     * @param avatarSize The class of avatar size you want to generate
     */
    public AvatarBitmapTransformation(final AvatarSize avatarSize) {
        mAvatarSize = avatarSize;
    }

    @Override
    public Bitmap transform(final Bitmap source) {

        final boolean alreadySquare = isSquareImage(source.getWidth(), source.getHeight());

        final int avatarSizeInPixels = BarterLiApplication
                .getStaticContext()
                .getResources().getDimensionPixelSize(mAvatarSize.dimenResId);
        final int shortestWidth = Math.min(source.getWidth(), source.getHeight());
        final float scaleFactor = avatarSizeInPixels / (float) shortestWidth;

        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                source,
                (int) (source.getWidth() * scaleFactor),
                (int) (source.getHeight() * scaleFactor),
                true);

        if (scaledBitmap != source) {
            source.recycle();
        }

        if (alreadySquare) {
            //Already square, no need to crop
            return scaledBitmap;
        } else {
            final int size = Math.min(scaledBitmap.getWidth(), scaledBitmap.getHeight());

            //Start x,y positions to crop the bitmap
            int x = 0;
            int y = 0;

            if (size == scaledBitmap.getWidth()) {
            /* Portrait picture, crop the bottom and top parts */
                y = (scaledBitmap.getHeight() - size) / 2;

            } else {

            /* Landscape picture, crop the right and left zones */
                x = (scaledBitmap.getWidth() - size) / 2;
            }
            final Bitmap result = Bitmap.createBitmap(scaledBitmap, x, y, size, size);
            if (result != scaledBitmap) {
                scaledBitmap.recycle();
            }
            return result;
        }
    }

    @Override
    public String key() {
        return mAvatarSize.key;
    }

    /**
     * Checks if the given image is a square or not
     *
     * @param sourceWidth  The width of the source image
     * @param sourceHeight The height of the source image
     */
    private static boolean isSquareImage(final int sourceWidth, final int sourceHeight) {

        if(sourceHeight > sourceWidth) {
            return (sourceWidth/sourceHeight) >= MAX_ALLOWED_SQUARE_FACTOR;
        } else {
            return (sourceHeight/sourceWidth) >= MAX_ALLOWED_SQUARE_FACTOR;
        }

    }
}
