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

package li.barter;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

public class GPSTracker extends Service implements LocationListener {

    private final Context     mContext;

    // flag for GPS status
    boolean                   isGPSEnabled                    = false;

    // flag for network status
    boolean                   isNetworkEnabled                = false;

    // flag for GPS status
    boolean                   canGetLocation                  = false;

    Location                  location;                                       // location
    double                    latitude;                                       // latitude
    double                    longitude;                                      // longitude

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;           // 10
                                                                               // meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES             = 1000 * 60 * 1; // 1
                                                                               // minute

    // Declaring a Location Manager
    protected LocationManager locationManager;

    public GPSTracker(final Context context) {
        this.mContext = context;
        getLocation();
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) mContext
                            .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                            .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                            .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;
                if (isNetworkEnabled) {
                    locationManager.requestLocationUpdates(
                                    LocationManager.NETWORK_PROVIDER,
                                    MIN_TIME_BW_UPDATES,
                                    MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                    Log.d("Network", "Network");
                    if (locationManager != null) {
                        location = locationManager
                                        .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                    }
                }
                // if GPS Enabled get lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                        LocationManager.GPS_PROVIDER,
                                        MIN_TIME_BW_UPDATES,
                                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                            .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (final Exception e) {
            e.printStackTrace();
        }

        return location;
    }

    public Address getMyLocationAddress(final String locationName) {
        final Geocoder geocoder = new Geocoder(mContext);
        Address address = null;
        try {
            final ArrayList<Address> adresses = (ArrayList<Address>) geocoder
                            .getFromLocationName(locationName, 5);
            if (!adresses.isEmpty()) {
                address = adresses.get(0);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    /**
     * Stop using GPS listener Calling this function will stop using GPS in your
     * app
     */
    public void stopUsingGPS() {
        if (locationManager != null) {
            locationManager.removeUpdates(GPSTracker.this);
        }
    }

    /**
     * Function to get latitude
     */
    public double getLatitude() {
        if (location != null) {
            latitude = location.getLatitude();
        }

        // return latitude
        return latitude;
    }

    /**
     * Function to get longitude
     */
    public double getLongitude() {
        if (location != null) {
            longitude = location.getLongitude();
        }

        // return longitude
        return longitude;
    }

    /**
     * Function to check GPS/wifi enabled
     * 
     * @return boolean
     */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog On pressing Settings button will
     * lauch Settings Options
     */
    public void showSettingsAlert() {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
                        mContext);

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                            final int which) {
                                final Intent intent = new Intent(
                                                Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                mContext.startActivity(intent);
                            }
                        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                            final int which) {
                                dialog.cancel();
                            }
                        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(final Location location) {
        final String str = "Latitude: " + location.getLatitude()
                        + "Longitude: " + location.getLongitude();
        // Toast.makeText(mContext, str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(final String provider) {
        // Toast.makeText(getBaseContext(), "Gps turned off ",
        // Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(final String provider) {
        // Toast.makeText(getBaseContext(), "Gps turned on ",
        // Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(final String provider, final int status,
                    final Bundle extras) {
    }

    @Override
    public IBinder onBind(final Intent arg0) {
        return null;
    }

}
