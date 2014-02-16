package com.barterli.android.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

import com.barterli.android.R;

/**
 * @author Vinay S Shenoy
 * Custom EditText to apply a font
 */
public class TypefacedEditText extends EditText {

	public TypefacedEditText(Context context, AttributeSet attrs) {

		super(context, attrs);

		if (attrs != null) {
			// Get Custom Attribute Name and value
			TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
					R.styleable.TypefacedEditText);
			int typefaceCode = styledAttrs.getInt(
					R.styleable.TypefacedEditText_fontStyle, -1);
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

	public TypefacedEditText(Context context) {
		super(context);
	}
}
