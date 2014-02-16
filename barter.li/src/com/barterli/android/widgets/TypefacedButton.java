package com.barterli.android.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

import com.barterli.android.R;

/**
 * @author Vinay S Shenoy Custom Buton to apply a font
 */
public class TypefacedButton extends Button {

	public TypefacedButton(Context context, AttributeSet attrs) {

		super(context, attrs);

		if (attrs != null) {
			// Get Custom Attribute Name and value
			TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
					R.styleable.TypefacedButton);
			int typefaceCode = styledAttrs.getInt(
					R.styleable.TypefacedButton_fontStyle, -1);
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

	public TypefacedButton(Context context) {
		super(context);
	}
}
