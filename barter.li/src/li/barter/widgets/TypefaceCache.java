/*******************************************************************************
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
 ******************************************************************************/

package li.barter.widgets;

import android.content.res.AssetManager;
import android.graphics.Typeface;

import java.util.Hashtable;

/**
 * @author Vinay S Shenoy Typeface cache to cache the typefaces
 */
public class TypefaceCache {

    private static final Hashtable<String, Typeface> CACHE             = new Hashtable<String, Typeface>();

    public static final String                       BOLD_ITALIC       = "fonts/Roboto-BoldItalic.ttf";
    public static final String                       BOLD              = "fonts/Roboto-Bold.ttf";
    public static final String                       ITALIC            = "fonts/Roboto-Italic.ttf";
    public static final String                       REGULAR           = "fonts/Roboto-Regular.ttf";
    public static final String                       CONDENSED_REGULAR = "fonts/RobotoCondensed-Regular.ttf";

    public static Typeface get(final AssetManager manager,
                    final int typefaceCode) {
        synchronized (CACHE) {

            final String typefaceName = getTypefaceName(typefaceCode);

            if (!CACHE.containsKey(typefaceName)) {
                final Typeface t = Typeface
                                .createFromAsset(manager, typefaceName);
                CACHE.put(typefaceName, t);
            }
            return CACHE.get(typefaceName);
        }
    }

    public static Typeface get(final AssetManager manager,
                    final String typefaceName) {
        return get(manager, getCodeForTypefaceName(typefaceName));
    }

    private static int getCodeForTypefaceName(final String typefaceName) {

        if (typefaceName.equals(BOLD_ITALIC)) {
            return 0;
        } else if (typefaceName.equals(BOLD)) {
            return 1;
        } else if (typefaceName.equals(ITALIC)) {
            return 2;
        } else if (typefaceName.equals(REGULAR)) {
            return 3;
        } else if (typefaceName.equals(CONDENSED_REGULAR)) {
            return 4;
        } else {
            return 3;
        }
    }

    private static String getTypefaceName(final int typefaceCode) {
        switch (typefaceCode) {
            case 0:
                return BOLD_ITALIC;

            case 1:
                return BOLD;

            case 2:
                return ITALIC;

            case 3:
                return REGULAR;

            case 4:
                return CONDENSED_REGULAR;

            default:
                return REGULAR;
        }
    }

}
