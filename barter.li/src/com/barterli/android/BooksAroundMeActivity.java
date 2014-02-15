package com.barterli.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class BooksAroundMeActivity extends AbstractBarterLiActivity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_books_around_me);
		findViewById(R.id.tap_to_add).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v.getId() == R.id.tap_to_add) {
			startActivity(new Intent(this, AddBookActivity.class));
		}
	}
}
