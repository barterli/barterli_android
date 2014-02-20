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

import li.barter.adapters.BooksAroundMeAdapter;
import li.barter.utils.GooglePlayClientWrapper;
import li.barter.utils.UtilityMethods;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.AutoCompleteTextView;
import android.widget.GridView;

import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnMapLoadedCallback;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class BooksAroundMeActivity extends AbstractBarterLiActivity implements
		LocationListener, SnapshotReadyCallback, CancelableCallback,
		OnMapLoadedCallback {

	private static final String TAG = "BooksAroundMeActivity";

	/**
	 * Zoom level for the map when the location is retrieved
	 */
	private static final float MAP_ZOOM_LEVEL = 15;

	private static final int MAP_BLUR = 20;

	private static final int TRANSITION_DURATION = 1200;

	private GooglePlayClientWrapper mGooglePlayClientWrapper;

	private MapFragment mMapFragment;

	private View mBackgroundView;

	private View mMapView;

	private AutoCompleteTextView mBooksAroundMeAutoCompleteTextView;

	private GridView mBooksAroundMeGridView;

	private BooksAroundMeAdapter mBooksAroundMeAdapter;

	private SwingBottomInAnimationAdapter mSwingBottomInAnimationAdapter;

	private Drawable[] mTransitionDrawableLayers;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_books_around_me);

		mBackgroundView = findViewById(R.id.layout_books_container);
		mBooksAroundMeAutoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.auto_complete_books_around_me);
		mBooksAroundMeGridView = (GridView) findViewById(R.id.grid_books_around_me);
		mMapView = findViewById(R.id.map_books_around_me);

		mBooksAroundMeAdapter = new BooksAroundMeAdapter();
		mSwingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(
				mBooksAroundMeAdapter, 150, 500);

		setActionBarDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
		getActionBar().setHomeButtonEnabled(false);

		mTransitionDrawableLayers = new Drawable[2];
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

		case R.id.action_toggle_map: {

			toggleMap();
			return true;
		}
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

	/**
	 * Toggle Map
	 */
	private void toggleMap() {

		if (mBackgroundView.getAlpha() == 1.0f) {
			mBackgroundView.animate().alpha(0.0f).setDuration(500).start();
			setMapMyLocationEnabled(true);
			mMapView.bringToFront();
		} else {
			mBackgroundView.animate().alpha(1.0f).setDuration(500).start();
			setMapMyLocationEnabled(false);
			mBackgroundView.bringToFront();
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

		mTransitionDrawableLayers[0] = mBackgroundView.getBackground();
		mTransitionDrawableLayers[1] = backgroundDrawable;

		final TransitionDrawable transitionDrawable = new TransitionDrawable(
				mTransitionDrawableLayers);
		transitionDrawable.setCrossFadeEnabled(true);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mBackgroundView.setBackground(transitionDrawable);
		} else {
			mBackgroundView.setBackgroundDrawable(transitionDrawable);
		}

		transitionDrawable.startTransition(TRANSITION_DURATION);

		snapshot.recycle();
		snapshot = null;

		if (mBooksAroundMeGridView.getAdapter() == null) {
			mSwingBottomInAnimationAdapter
					.setAbsListView(mBooksAroundMeGridView);
			mBooksAroundMeGridView.setAdapter(mSwingBottomInAnimationAdapter);
		}

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

	/**
	 * Sets the My location enabled for the Map
	 */
	private void setMapMyLocationEnabled(boolean enabled) {

		if (mMapFragment != null && mMapFragment.isVisible()) {

			GoogleMap googleMap = mMapFragment.getMap();

			if (googleMap != null) {
				googleMap.setMyLocationEnabled(enabled);
			}
		}
	}

}
