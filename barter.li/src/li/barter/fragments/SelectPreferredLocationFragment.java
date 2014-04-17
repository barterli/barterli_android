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

import com.android.volley.Request.Method;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerDragListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.Map;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.http.BlRequest;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.parcelables.Hangout;
import li.barter.utils.AppConstants.DeviceInfo;
import li.barter.utils.AppConstants.FragmentTags;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.Logger;
import li.barter.utils.SharedPreferenceHelper;

/**
 * @author Vinay S Shenoy Fragment for selecting a preferred location for
 *         exchanging books
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class SelectPreferredLocationFragment extends AbstractBarterLiFragment
                implements CancelableCallback, OnInfoWindowClickListener,
                OnMarkerDragListener {

    private static final String  TAG                      = "SelectPreferredLocationFragment";

    /**
     * Zoom level for the map when the location is retrieved
     */
    private static final float   MAP_ZOOM_LEVEL           = 15;

    /**
     * Hue for custom marker, to match with the app theme
     */
    private final float          mCustomMarkerHue         = 196.0f;

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
    private final String         mMarkerTitleFormat       = "%s, %s";

    /**
     * Title format for the custom marker
     */
    private final String         mCustomMarkerTitleFormat = "%.5f, %.5f";

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
        moveMapToLocation(DeviceInfo.INSTANCE.getLatestLocation());

        if (savedInstanceState != null) {
            mHangouts = (Hangout[]) savedInstanceState
                            .getParcelableArray(Keys.LOCATIONS);
        }

        if ((mHangouts == null) || (mHangouts.length == 0)) {
            fetchHangoutsForLocation(DeviceInfo.INSTANCE.getLatestLocation(), 1000);
        } else {
            addMarkersToMap(mHangouts);
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
    private void fetchHangoutsForLocation(final Location location,
                    final int radius) {

        final BlRequest request = new BlRequest(Method.GET, HttpConstants.getApiBaseUrl()
                        + ApiEndpoints.HANGOUTS, null, mVolleyCallbacks);
        request.setRequestId(RequestId.HANGOUTS);

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
        outState.putParcelableArray(Keys.LOCATIONS, mHangouts);
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
            map.setOnMarkerDragListener(this);
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
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {
        if (requestId == RequestId.HANGOUTS) {
            mHangouts = (Hangout[]) response.responseBundle
                            .getParcelableArray(HttpConstants.LOCATIONS);
            addMarkersToMap(mHangouts);
        } else if (requestId == RequestId.SET_USER_PREFERRED_LOCATION) {

            SharedPreferenceHelper
                            .set(getActivity(), R.string.pref_location, response.responseBundle
                                            .getString(HttpConstants.ID_LOCATION));
            onUpNavigate();
        }
    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {
        if (requestId == RequestId.HANGOUTS) {
            showCrouton(R.string.unable_to_fetch_hangouts, AlertStyle.ERROR);
        } else if (requestId == RequestId.SET_USER_PREFERRED_LOCATION) {
            showCrouton(R.string.error_unable_to_set_preferred_location, AlertStyle.ERROR);
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

    /**
     * Parses the LatLng values from the Hangouts passed, adds Map markers
     */
    private void addMarkersToMap(final Hangout[] hangouts) {
        final GoogleMap map = getMap();

        if (map != null) {
            mMarkerHangoutMap.clear();
            final String snippet = getString(R.string.tap_to_set_preferred_location);
            Marker marker = null;
            for (final Hangout aHangout : hangouts) {

                marker = map.addMarker(new MarkerOptions()
                                .position(new LatLng(aHangout.latitude, aHangout.longitude))
                                .title(String.format(mMarkerTitleFormat, aHangout.name, aHangout.address))
                                .snippet(snippet));
                mMarkerHangoutMap.put(marker, aHangout);
            }

            if (mCustomMarker == null) {
                final Location location = DeviceInfo.INSTANCE
                                .getLatestLocation();
                mCustomMarker = map
                                .addMarker(new MarkerOptions()
                                                .draggable(true)
                                                .icon(BitmapDescriptorFactory
                                                                .defaultMarker(mCustomMarkerHue))
                                                .title(String.format(mCustomMarkerTitleFormat, location
                                                                .getLatitude(), location
                                                                .getLongitude()))
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
    public void onInfoWindowClick(final Marker marker) {

        if (marker.equals(mCustomMarker)) {
            final LatLng markerPosition = marker.getPosition();
            Logger.v(TAG, "Custom Marker Clicked - Latitude %f Longitude %f", markerPosition.latitude, markerPosition.longitude);
            //TODO Reverse geocode the marker position to get the location info
        } else {

            final Hangout selectedHangout = mMarkerHangoutMap.get(marker);
            Logger.v(TAG, "Marker Clicked: %s, %s", selectedHangout.name, selectedHangout.address);
            setUserPreferredLocation(selectedHangout.name, selectedHangout.address, selectedHangout.latitude, selectedHangout.longitude);
        }
    }

    /**
     * Uploads the user preferred location to server
     * 
     * @param name The name of the location
     * @param address The address of the location
     * @param latitude The latitude of the location
     * @param longitude The longitude of the location
     */
    private void setUserPreferredLocation(final String name,
                    final String address, final double latitude,
                    final double longitude) {

        final JSONObject requestBody = new JSONObject();
        try {
            requestBody.put(HttpConstants.NAME, name);
            requestBody.put(HttpConstants.ADDRESS, address);
            requestBody.put(HttpConstants.LATITUDE, latitude);
            requestBody.put(HttpConstants.LONGITUDE, longitude);

            final BlRequest request = new BlRequest(Method.POST, HttpConstants.getApiBaseUrl()
                            + ApiEndpoints.USER_PREFERRED_LOCATION, requestBody.toString(), mVolleyCallbacks);
            addRequestToQueue(request, true, 0);
            request.setRequestId(RequestId.SET_USER_PREFERRED_LOCATION);
        } catch (final JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onMarkerDrag(final Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(final Marker marker) {
        if (marker.equals(mCustomMarker)) {
            final LatLng position = marker.getPosition();
            marker.setTitle(String
                            .format(mCustomMarkerTitleFormat, position.latitude, position.longitude));
            marker.showInfoWindow();
        }
    }

    @Override
    public void onMarkerDragStart(final Marker marker) {

    }
}
