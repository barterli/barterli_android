package com.barterli.android.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import com.barterli.android.R;

/**
 * @author Vinay S Shenoy Custom TextView to apply a font
 */
public class TypefacedTextView extends TextView {

	public TypefacedTextView(Context context, AttributeSet attrs) {

		super(context, attrs);

		if (attrs != null) {
			// Get Custom Attribute Name and value
			TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
					R.styleable.TypefacedTextView);
			int typefaceCode = styledAttrs.getInt(
					R.styleable.TypefacedTextView_fontStyle, -1);
			styledAttrs.recycle();

			// Typeface.createFromAsset doesn't work in the layout editor.
			// Skipping...
			if (isInEditMode()) {
				return;
			}

			Typeface typeface = TypefaceCache.get(context.getAssets(),
					typefaceCode);
			setTypeface(typeface);
		}
	}

	public TypefacedTextView(Context context) {
		super(context);
	}
}
