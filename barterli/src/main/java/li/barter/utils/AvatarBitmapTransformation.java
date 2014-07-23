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

/**
 * Class that transforms a Bitmap into a square based on the shortest dimension and a desired avatar
 * size.
 * <p/>
 * This class works by taking desired avatar size(in pixels) in the constructor. When the Bitmap is
 * passed to it, it scales the Bitmap's shortest dimension to the avatar size, maintaining the aspect
 * ratio. In then crops the Bitmap to form a square image
 * <p/>
 * Created by vinay.shenoy on 22/07/14.
 */
public class AvatarBitmapTransformation implements Transformation {

    /**
     * Desired avatar size in pixels
     */
    private int mAvatarSize;

    /**
     * Construct an instance of AvatarTransformation method, setting the desired avatar size
     *
     * @param avatarSize The size(in pixels) to transform the avatar into
     */
    public AvatarBitmapTransformation(final int avatarSize) {
        mAvatarSize = avatarSize;
    }

    @Override
    public Bitmap transform(final Bitmap source) {

        final int shortestWidth = Math.min(source.getWidth(), source.getHeight());
        final float scaleFactor = mAvatarSize / (float) shortestWidth;

        final Bitmap scaledBitmap = Bitmap.createScaledBitmap(
                source,
                (int) (source.getWidth() * scaleFactor),
                (int) (source.getHeight() * scaleFactor),
                true);

        if(scaledBitmap != source) {
            source.recycle();
        }
        final int size = Math.min(scaledBitmap.getWidth(), scaledBitmap.getHeight());
        final int x = (scaledBitmap.getWidth() - size) / 2;
        final int y = (scaledBitmap.getHeight() - size) / 2;
        final Bitmap result = Bitmap.createBitmap(scaledBitmap, x, y, size, size);
        if (result != scaledBitmap) {
            scaledBitmap.recycle();
        }
        return result;
    }

    @Override
    public String key() {
        return "avatar";
    }
}
