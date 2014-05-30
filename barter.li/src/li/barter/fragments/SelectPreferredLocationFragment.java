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

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import li.barter.R;
import li.barter.activities.AbstractBarterLiActivity.AlertStyle;
import li.barter.adapters.SelectLocationAdapter;
import li.barter.fragments.dialogs.AlertDialogFragment;
import li.barter.http.BlRequest;
import li.barter.http.FoursquareCategoryBuilder;
import li.barter.http.HttpConstants;
import li.barter.http.HttpConstants.ApiEndpoints;
import li.barter.http.HttpConstants.RequestId;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.models.Venue;
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
                implements OnItemClickListener {

    private static final String   TAG                     = "SelectPreferredLocationFragment";

    /**
     * Radius for searching locations
     */
    private static final int      SEARCH_RADIUS_IN_METERS = 50000;

    /**
     * Foursquare Api versioning parameter. Visit
     * https://developer.foursquare.com/overview/versioning for more info
     */
    private static final String   FOURSQUARE_API_VERSION  = "20140526";

    /**
     * {@link Venue}s used to display nearby places
     */
    private Venue[]               mVenues;

    /**
     * Adapter for displaying list of locations nearby for selecting as user's
     * preferred location
     */
    private SelectLocationAdapter mSelectLocationAdapter;

    private ListView              mVenueListView;
    private String                mFoursquareClientId;
    private String                mFoursquareClientSecret;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        setHasOptionsMenu(true);
        final View contentView = inflater
                        .inflate(R.layout.fragment_select_location, container, false);

        mVenueListView = (ListView) contentView
                        .findViewById(R.id.list_locations);
        mSelectLocationAdapter = new SelectLocationAdapter(getActivity(), null, true);
        mVenueListView.setAdapter(mSelectLocationAdapter);
        mVenueListView.setOnItemClickListener(this);

        //showInfiniteCrouton(R.string.crouton_prefferedlocation_message, AlertStyle.INFO);
        mFoursquareClientId = getString(R.string.foursquare_client_id);
        mFoursquareClientSecret = getString(R.string.foursquare_client_secret);

        if (savedInstanceState != null) {
            mVenues = (Venue[]) savedInstanceState
                            .getParcelableArray(Keys.LOCATIONS);
        } else {
            
            //TODO Show this only if the user has come here after sign in instead of editing the location
            showAboutSelectLocationDialog();
        }

        if ((mVenues == null) || (mVenues.length == 0)) {
            fetchVenuesForLocation(DeviceInfo.INSTANCE.getLatestLocation(), SEARCH_RADIUS_IN_METERS);
        } else {
            mSelectLocationAdapter.setVenues(mVenues);
        }
        setActionBarDrawerToggleEnabled(false);
        return contentView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_select_location, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_about_select_location) {
            showAboutSelectLocationDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Display the Dialog for showing info about this screen
     */
    private void showAboutSelectLocationDialog() {
        AlertDialogFragment fragment = new AlertDialogFragment();
        fragment.show(AlertDialog.THEME_HOLO_LIGHT, 0, R.string.about, R.string.help_select_location, R.string.ok, 0, 0, getFragmentManager(), true, FragmentTags.DIALOG_ABOUT_LOCATION);
    }

    /**
     * Fetch all the hangouts centered at the current location with the given
     * radius
     * 
     * @param location The location to search for hangouts
     * @param radius Tghe search radius(in meters)
     */
    private void fetchVenuesForLocation(final Location location,
                    final int radius) {

        final BlRequest request = new BlRequest(Method.GET, HttpConstants.getFoursquareUrl()
                        + ApiEndpoints.FOURSQUARE_VENUES, null, mVolleyCallbacks);
        request.setRequestId(RequestId.FOURSQUARE_VENUES);

        final Map<String, String> params = new HashMap<String, String>(7);

        params.put(HttpConstants.LL, String.format(Locale.US, "%f,%f", location
                        .getLatitude(), location.getLongitude()));
        params.put(HttpConstants.RADIUS, String.valueOf(radius));

        final String foursquareCategoryFilter = FoursquareCategoryBuilder
                        .init()
                        .with(FoursquareCategoryBuilder.ARTS_AND_ENTERTAINMENT)
                        .with(FoursquareCategoryBuilder.COLLEGE_AND_UNIVERSITY)
                        .with(FoursquareCategoryBuilder.FOOD)
                        .with(FoursquareCategoryBuilder.NIGHTLIFE_SPOT)
                        .with(FoursquareCategoryBuilder.OUTDOORS_AND_RECREATION)
                        .with(FoursquareCategoryBuilder.PROFESSIONAL_AND_OTHER_PLACES)
                        .with(FoursquareCategoryBuilder.SHOP_AND_SERVICE)
                        .with(FoursquareCategoryBuilder.TRAVEL_AND_TRANSPORT)
                        .build();

        Logger.v(TAG, "Foursquare Category Filter - %s", foursquareCategoryFilter);
        params.put(HttpConstants.CATEGORY_ID, foursquareCategoryFilter);
        params.put(HttpConstants.INTENT, HttpConstants.BROWSE);
        params.put(HttpConstants.CLIENT_ID, mFoursquareClientId);
        params.put(HttpConstants.CLIENT_SECRET, mFoursquareClientSecret);
        params.put(HttpConstants.V, FOURSQUARE_API_VERSION);

        request.setParams(params);
        addRequestToQueue(request, true, 0, false);
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(Keys.LOCATIONS, mVenues);
    }

    @Override
    protected Object getVolleyTag() {
        return hashCode();
    }

    @Override
    public void onPause() {
        super.onPause();
        cancelAllCroutons();
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {
        if (requestId == RequestId.FOURSQUARE_VENUES) {
            mVenues = (Venue[]) response.responseBundle
                            .getParcelableArray(HttpConstants.LOCATIONS);
            mSelectLocationAdapter.setVenues(mVenues);
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
        if (requestId == RequestId.FOURSQUARE_VENUES) {
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
            addRequestToQueue(request, true, 0, true);
            request.setRequestId(RequestId.SET_USER_PREFERRED_LOCATION);
        } catch (final JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                    long id) {

        if (parent.getId() == R.id.list_locations) {

            final Venue venue = (Venue) mSelectLocationAdapter
                            .getItem(position);
            setUserPreferredLocation(venue.name, venue.address, venue.latitude, venue.longitude);
        }
    }

}
