/**
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
 */

package li.barter.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable class to represent a Hangput
 * 
 * @author Vinay S Shenoy
 */
public class Venue implements Parcelable {

    public String foursquareId;
    public String name;
    public String address;
    public String city;
    public String state;
    public String country;
    public double latitude;
    public double longitude;
    public int distance;

    //Default constructor
    public Venue() {

    }

    //Constructor to read from parcel
    public Venue(final Parcel source) {
        foursquareId = source.readString();
        name = source.readString();
        address = source.readString();
        city = source.readString();
        state = source.readString();
        country = source.readString();
        latitude = source.readDouble();
        longitude = source.readDouble();
        distance = source.readInt();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(foursquareId);
        dest.writeString(name);
        dest.writeString(address);
        dest.writeString(city);
        dest.writeString(state);
        dest.writeString(country);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeInt(distance);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* REQUIRED FOR PARCELLABLE. DO NOT MODIFY IN ANY WAY */
    public static final Creator<Venue> CREATOR = new Creator<Venue>() {

                                                     @Override
                                                     public Venue createFromParcel(
                                                                     final Parcel source) {
                                                         return new Venue(source);
                                                     }

                                                     @Override
                                                     public Venue[] newArray(
                                                                     final int size) {
                                                         return new Venue[size];
                                                     }
                                                 };

}
