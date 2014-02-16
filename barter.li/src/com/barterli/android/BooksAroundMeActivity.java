package com.barterli.android;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class BooksAroundMeActivity extends AbstractBarterLiActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_books_around_me);
		setActionBarDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
		getActionBar().setHomeButtonEnabled(false);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.menu_books_around_me, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.action_scan_book: {
			startActivity(new Intent(this, ScanIsbnActivity.class));
			return true;
		}

		case R.id.action_my_books: {
			// TODO do we have a screen to show here
			return true;
		}

		case R.id.action_profile: {
			// TODO Show profile screen
			return true;
		}

		case R.id.action_add_book: {
			startActivity(new Intent(this, AddOrEditBookActivity.class));
			return true;
		}

		default: {
			return super.onOptionsItemSelected(item);
		}
		}
	}

}
