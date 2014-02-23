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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

import li.barter.R;

/**
 * @author Vinay S Shenoy Custom AutoCompleteTextView to apply a font
 */
public class TypefacedAutoCompleteTextView extends AutoCompleteTextView {

    public TypefacedAutoCompleteTextView(final Context context,
                    final AttributeSet attrs) {

        super(context, attrs);

        if (attrs != null) {
            // Get Custom Attribute Name and value
            final TypedArray styledAttrs = context.obtainStyledAttributes(
                            attrs, R.styleable.TypefacedAutoCompleteTextView);
            final int typefaceCode = styledAttrs
                            .getInt(R.styleable.TypefacedAutoCompleteTextView_fontStyle,
                                            -1);
            styledAttrs.recycle();

            // Typeface.createFromAsset doesn't work in the layout editor.
            // Skipping...
            if (isInEditMode()) {
                return;
            }

            final Typeface typeface = TypefaceCache.get(context.getAssets(),
                            typefaceCode);
            setTypeface(typeface);
        }
    }

    public TypefacedAutoCompleteTextView(final Context context) {
        super(context);
    }
}
