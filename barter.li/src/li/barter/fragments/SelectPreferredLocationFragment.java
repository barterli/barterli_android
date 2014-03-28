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

package li.barter.fragments;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.android.gms.internal.ah;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.ResponseInfo;
import li.barter.parcelables.Hangout;
import li.barter.utils.Logger;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.UserInfo;

/**
 * @author Vinay S Shenoy Fragment for selecting a preferred location for
 *         exchanging books
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class SelectPreferredLocationFragment extends AbstractBarterLiFragment
                implements Listener<ResponseInfo>, ErrorListener,
                CancelableCallback, OnInfoWindowClickListener {

    private static final String  TAG                = "SelectPreferredLocationFragment";

    /**
     * Zoom level for the map when the location is retrieved
     */
    private static final float   MAP_ZOOM_LEVEL     = 15;

    /**
     * Hue for custom marker, to match with the app theme
     */
    private final float          mCustomMarkerHue   = 196.0f;

    /**
     * {@link MapView} used to display the Map
     */
    private MapView              mMapView;

    /**
     * {@link Hangout}s used to place nearby marker locations on Map
     */
    private Hangout[]            mHangouts;

    /**
     * {@link Marker} that can be used to set a custom location
     */
    private Marker               mCustomMarker;

    /**
     * Title format for the marker
     */
    private final String         mMarkerTitleFormat = "%s, %s";

    /**
     * A 1-to-1 mapping between the Map markers and the hangouts to resolve
     * which Hangout was selected when user tapped the marker info window
     */
    private Map<Marker, Hangout> mMarkerHangoutMap;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container);

        final View contentView = inflater
                        .inflate(R.layout.fragment_select_location, container, false);

        mMarkerHangoutMap = new HashMap<Marker, Hangout>();
        /*
         * The Google Maps V2 API states that when using MapView, we need to
         * forward the onCreate(Bundle) method to the MapView, but since we are
         * in a fragment, the onCreateView() gets called AFTER the
         * onCreate(Bundle) method, which makes forwarding that method
         * impossible. This is the workaround for that
         */
        MapsInitializer.initialize(getActivity());
        mMapView = (MapView) contentView
                        .findViewById(R.id.map_preferred_location);
        mMapView.onCreate(savedInstanceState);
        setUpMapListeners();
        moveMapToLocation(UserInfo.INSTANCE.latestLocation);
        if (savedInstanceState == null || mHangouts == null) {
            fetchHangoutsForLocation(UserInfo.INSTANCE.latestLocation, 1000);
        } else {
            //Set fetched hangouts to adapter
        }
        setActionBarDrawerToggleEnabled(false);
        return contentView;
    }

    /**
     * Fetch all the hangouts centered at the current location with the given
     * radius
     * 
     * @param location The location to search for hangouts
     * @param radius Tghe search radius(in meters)
     */
    private void fetchHangoutsForLocation(Location location, int radius) {

        final BlRequest request = new BlRequest(Method.GET, RequestId.HANGOUTS, HttpConstants.getApiBaseUrl()
                        + ApiEndpoints.HANGOUTS, null, this, this);

        final Map<String, String> params = new HashMap<String, String>(3);

        params.put(HttpConstants.LATITUDE, String.valueOf(location
                        .getLatitude()));
        params.put(HttpConstants.LONGITUDE, String.valueOf(location
                        .getLongitude()));
        params.put(HttpConstants.METERS, String.valueOf(radius));

        request.setParams(params);
        addRequestToQueue(request, true, 0);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onUpNavigate();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    /**
     * Moves the map to a location
     * 
     * @param location The {@link Location} to move the map to
     */
    private void moveMapToLocation(final Location location) {

        /*
         * For the initial launch, move the Map to the user's current position
         * as soon as the location is fetched
         */
        final GoogleMap googleMap = getMap();

        if (googleMap != null) {
            googleMap.setMyLocationEnabled(false);
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location
                            .getLatitude(), location.getLongitude()), MAP_ZOOM_LEVEL), this);

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMapView != null) {
            mMapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    /**
     * Sets up the listeners for the Map
     */
    private void setUpMapListeners() {
        final GoogleMap map = getMap();

        if (map != null) {
            map.setOnInfoWindowClickListener(this);
        }
    }

    @Override
    public void onCancel() {

    }

    @Override
    public void onFinish() {
        //Draw the markers
    }

    @Override
    public void onErrorResponse(VolleyError error, Request<?> request) {
        onRequestFinished();

        if (request instanceof BlRequest) {

            if (((BlRequest) request).getRequestId() == RequestId.HANGOUTS) {
                showToast(R.string.unable_to_fetch_hangouts, true);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (getTag().equals(FragmentTags.SELECT_PREFERRED_LOCATION_FROM_LOGIN)) {
            onUpNavigate();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResponse(ResponseInfo response, Request<ResponseInfo> request) {
        onRequestFinished();

        if (request instanceof BlRequest) {

            if (((BlRequest) request).getRequestId() == RequestId.HANGOUTS) {
                mHangouts = (Hangout[]) response.responseBundle
                                .getParcelableArray(HttpConstants.LOCATIONS);
                addMarkersToMap(mHangouts);
            }
        }

    }

    /**
     * Parses the LatLng values from the Hangouts passed, adds Map markers
     */
    private void addMarkersToMap(Hangout[] hangouts) {
        final GoogleMap map = getMap();

        if (map != null) {
            mMarkerHangoutMap.clear();
            final String snippet = getString(R.string.tap_to_set_preferred_location);
            Marker marker = null;
            for (Hangout aHangout : hangouts) {

                marker = map.addMarker(new MarkerOptions()
                                .position(new LatLng(aHangout.latitude, aHangout.longitude))
                                .title(String.format(mMarkerTitleFormat, aHangout.name, aHangout.address))
                                .snippet(snippet));
                mMarkerHangoutMap.put(marker, aHangout);
            }

            if (mCustomMarker == null) {
                final Location location = UserInfo.INSTANCE.latestLocation;
                mCustomMarker = map
                                .addMarker(new MarkerOptions()
                                                .draggable(true)
                                                .icon(BitmapDescriptorFactory
                                                                .defaultMarker(196.0f))
                                                .title(getString(R.string.custom_location))
                                                .snippet(snippet)
                                                .position(new LatLng(location
                                                                .getLatitude(), location
                                                                .getLongitude())));
            }
        }
    }

    /**
     * Gets a reference of the Google Map
     */
    private GoogleMap getMap() {

        return mMapView.getMap();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (marker.equals(mCustomMarker)) {
            final LatLng markerPosition = marker.getPosition();
            Logger.v(TAG, "Custom Marker Clicked - Latitude %f Longitude %f", markerPosition.latitude, markerPosition.longitude);
            //TODO Reverse geocode the marker position to get the location info
        } else {

            final Hangout selectedHangout = mMarkerHangoutMap.get(marker);
            Logger.v(TAG, "Marker Clicked: %s, %s", selectedHangout.name, selectedHangout.address);
            //TODO Read the location info from the Hangout and return
        }
    }
}
