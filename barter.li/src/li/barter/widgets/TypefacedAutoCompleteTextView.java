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

	public TypefacedAutoCompleteTextView(Context context, AttributeSet attrs) {

		super(context, attrs);

		if (attrs != null) {
			// Get Custom Attribute Name and value
			TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
					R.styleable.TypefacedAutoCompleteTextView);
			int typefaceCode = styledAttrs.getInt(
					R.styleable.TypefacedAutoCompleteTextView_fontStyle, -1);
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

	public TypefacedAutoCompleteTextView(Context context) {
		super(context);
	}
}
