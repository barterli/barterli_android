package com.barterli.android;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.SpannableString;
import android.text.Spanned;

import com.barterli.android.widgets.TypefaceCache;
import com.barterli.android.widgets.TypefacedSpan;

/**
 * @author Vinay S Shenoy Base class for inheriting all other Activities from
 */
public class AbstractBarterLiActivity extends FragmentActivity {

	private static final int ACTION_BAR_DISPLAY_MASK = ActionBar.DISPLAY_HOME_AS_UP
			| ActionBar.DISPLAY_SHOW_TITLE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getActionBar() != null) {
			getActionBar().setDisplayOptions(ACTION_BAR_DISPLAY_MASK);
			setActionBarTitle(getTitle().toString());
		}
	}

	protected final void setActionBarTitle(final String title) {

		final SpannableString s = new SpannableString(title);
		s.setSpan(new TypefacedSpan(this, TypefaceCache.ALEGREYA_BLACK_ITALIC),
				0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		// Update the action bar title with the TypefaceSpan instance
		final ActionBar actionBar = getActionBar();
		actionBar.setTitle(s);
	}

}
