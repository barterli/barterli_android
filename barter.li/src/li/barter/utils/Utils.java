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

package li.barter.utils;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import android.content.Context;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.view.View;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import li.barter.utils.AppConstants.DeviceInfo;

/**
 * @author Vinay S Shenoy Utility methods for barter.li
 */
public class Utils {

    private static final String TAG = "Utils";

    /**
     * Reads the network info from service and sets up the singleton
     */
    public static void setupNetworkInfo(final Context context) {

        final ConnectivityManager connManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        if (activeNetwork != null) {
            DeviceInfo.INSTANCE.setNetworkConnected(activeNetwork
                            .isConnectedOrConnecting());
            DeviceInfo.INSTANCE.setCurrentNetworkType(activeNetwork.getType());
        } else {
            DeviceInfo.INSTANCE.setNetworkConnected(false);
            DeviceInfo.INSTANCE
                            .setCurrentNetworkType(ConnectivityManager.TYPE_DUMMY);
        }

        Logger.d(TAG, "Network State Updated Connected: %b Type: %d", DeviceInfo.INSTANCE
                        .isNetworkConnected(), DeviceInfo.INSTANCE
                        .getCurrentNetworkType());
    }

    /**
     * Checks if the current thread is the main thread or not
     * 
     * @return <code>true</code> if the current thread is the main/UI thread,
     *         <code>false</code> otherwise
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    /**
     * Returns the center of a map
     * 
     * @param map The map to fetch the center location of
     * @return The center of the map
     */
    public static Location getCenterLocationOfMap(final GoogleMap map) {
        final LatLng latLng = map.getCameraPosition().target;
        final Location location = new Location(LocationManager.PASSIVE_PROVIDER);
        location.setLatitude(latLng.latitude);
        location.setLongitude(latLng.longitude);
        return location;
    }

    /**
     * From the center point of the map, caculate the shortest radius(in metres)
     * depending on the whether the {@link MapView} is in portrait/landscape
     * orientation
     * 
     * @param map The {@link MapView} to calculate the radius from
     * @return The shortest radius(in metres), or 0 of the map is not intialized
     */
    public static float getShortestRadiusFromCenter(final MapView mapView) {

        float radius = 0.0f;

        final GoogleMap map = mapView.getMap();

        if (map != null) {

            // To hold the coordinates of the center line of the map
            Point[] screenCenterEdgePoints = null;
            screenCenterEdgePoints = getShorterDimensionEdgePoints(mapView);
            final Location[] locations = getLocationsFromPoints(map, screenCenterEdgePoints);

            if (locations.length == 2) {
                radius = distanceBetween(locations[0], locations[1]);
                Logger.v(TAG, "Distance Calculated: %f", radius);
            }
        }
        return radius;
    }

    /**
     * Gets the distance between two Locations(in metres)
     * 
     * @param start The start location
     * @param end The end location
     * @return The distance between two locations(in metres)
     */
    public static float distanceBetween(final Location start, final Location end) {

        final float[] results = new float[1];
        Location.distanceBetween(start.getLatitude(), start.getLongitude(), end
                        .getLatitude(), end.getLongitude(), results);
        return results[0];
    }

    /**
     * Takes an array of points(in screen pixels) and converts them into
     * Location objects on the Map
     * 
     * @param map {@link Map} reference on which the points are calculated
     * @param points The {@link Point}s to convert to {@link Location}s
     * @return An array of {@link Location}s with a 1-to-1 mapping between the
     *         input points
     */
    public static Location[] getLocationsFromPoints(final GoogleMap map,
                    final Point[] points) {
        final Location[] locations = new Location[points.length];

        for (int i = 0; i < points.length; i++) {
            locations[i] = pointToLocation(map, points[i]);
        }
        return locations;
    }

    /**
     * Converts a {@link Point}(in screen pixels) into a {@link Location} using
     * the provided {@link GoogleMap}
     * 
     * @param map A {@link GoogleMap} instance
     * @param point The {@link Point} in the MapView, in screen pixels
     * @return A {@link Location} object
     */
    public static Location pointToLocation(final GoogleMap map,
                    final Point point) {

        final LatLng latLng = map.getProjection().fromScreenLocation(point);
        final Location location = new Location(LocationManager.PASSIVE_PROVIDER);

        if (latLng != null) {
            location.setLatitude(latLng.latitude);
            location.setLongitude(latLng.longitude);
        }

        Logger.v(TAG, "Converted Point %s to Location %s", point.toString(), location
                        .toString());
        return location;
    }

    /**
     * @param view the view to get the edges of
     * @return The edges(in screen coordinates) of the shortest dimension of the
     *         view. If in landscape, the points will be returned as top,
     *         bottom. Otherwise left, right
     */
    public static Point[] getShorterDimensionEdgePoints(final View view) {

        final Point[] edgePoints = new Point[2];

        final int viewTop = view.getTop();
        final int viewBottom = view.getBottom();
        final int viewLeft = view.getLeft();
        final int viewRight = view.getRight();

        Logger.v(TAG, "Left %d, Top %d, Right %d, Bottom %d", viewLeft, viewTop, viewRight, viewBottom);

        if (isViewInLandscape(view)) {

            final int centerX = (viewRight - viewLeft) / 2;
            edgePoints[0] = new Point(centerX, viewTop);
            edgePoints[1] = new Point(centerX, viewBottom);

            Logger.v(TAG, "Landscape Edge: %s, %s", edgePoints[0].toString(), edgePoints[1]
                            .toString());
        } else {

            final int centerY = (viewBottom - viewTop) / 2;

            edgePoints[0] = new Point(viewLeft, centerY);
            edgePoints[1] = new Point(viewRight, centerY);

            Logger.v(TAG, "Portrait Edge: %s, %s", edgePoints[0].toString(), edgePoints[1]
                            .toString());
        }
        return edgePoints;
    }

    /**
     * @return <code>true</code> if view is in landscape mode,
     *         <code>false</code> if it is in portrait mode
     */
    public static boolean isViewInLandscape(final View view) {
        return view.getWidth() >= view.getHeight();
    }

    /**
     * Makes an SHA1 Hash of the given string
     * 
     * @param string The string to shash
     * @return The hashed string
     * @throws NoSuchAlgorithmException
     */
    public static String sha1(final String string)
                    throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        final byte[] data = digest.digest(string.getBytes());
        return String.format("%0" + (data.length * 2) + "X", new BigInteger(1, data));
    }

}
