/*******************************************************************************
\ * Copyright 2014, barter.li
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
package li.barter;

import li.barter.utils.GooglePlayClientWrapper;
import li.barter.utils.UtilityMethods;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class BooksAroundMeActivity extends AbstractBarterLiActivity implements
		LocationListener, SnapshotReadyCallback, CancelableCallback,
		OnMapLoadedCallback {

	private static final String TAG = "BooksAroundMeActivity";

	/**
	 * Zoom level for the map when the location is retrieved
	 */
	private static final float MAP_ZOOM_LEVEL = 15;

	private static final int MAP_BLUR = 18;

	private GooglePlayClientWrapper mGooglePlayClientWrapper;

	private MapFragment mMapFragment;

	private View mBackground;

	private AutoCompleteTextView mBooksAroundMeAutoCompleteTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_books_around_me);
		mBackground = findViewById(R.id.layout_books_container);
		mBooksAroundMeAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.auto_complete_books_around_me);
		setActionBarDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
		getActionBar().setHomeButtonEnabled(false);

		mGooglePlayClientWrapper = new GooglePlayClientWrapper(this, this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		mGooglePlayClientWrapper.onActivityStart();
	}

	@Override
	protected void onStop() {
		mGooglePlayClientWrapper.onActivityStop();
		super.onStop();
	}

	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		mMapFragment = (MapFragment) getFragmentManager().findFragmentById(
				R.id.map_books_around_me);
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

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG,
				"Location update:" + location.getLatitude() + " "
						+ location.getLongitude());

		if (mMapFragment != null && mMapFragment.isVisible()) {

			GoogleMap googleMap = mMapFragment.getMap();

			if (googleMap != null) {
				googleMap.setMyLocationEnabled(false);
				googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						new LatLng(location.getLatitude(), location
								.getLongitude()), MAP_ZOOM_LEVEL), this);

			}
		}
	}

	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onSnapshotReady(Bitmap snapshot) {

		BitmapDrawable backgroundDrawable = new BitmapDrawable(getResources(),
				UtilityMethods.blurImage(this, snapshot, MAP_BLUR));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mBackground.setBackground(backgroundDrawable);
		} else {
			mBackground.setBackgroundDrawable(backgroundDrawable);
		}

		snapshot.recycle();
		snapshot = null;

	}

	@Override
	public void onCancel() {

	}

	@Override
	public void onFinish() {

		if (mMapFragment != null && mMapFragment.isVisible()) {
			GoogleMap googleMap = mMapFragment.getMap();

			if (googleMap != null) {
				Log.d(TAG, "Adding On Loaded Callback!");
				googleMap.setOnMapLoadedCallback(this);
			}
		}
	}

	@Override
	public void onMapLoaded() {
		if (mMapFragment != null && mMapFragment.isVisible()) {
			GoogleMap googleMap = mMapFragment.getMap();

			if (googleMap != null) {
				Log.d(TAG, "Taking Snapshot!");
				googleMap.snapshot(this);
			}
		}

	}

}
