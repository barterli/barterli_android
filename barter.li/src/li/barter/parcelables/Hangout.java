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

package li.barter.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Parcelable class to represent a Hangput
 * 
 * @author Vinay S Shenoy
 */
public class Hangout implements Parcelable {

    public String name;
    public String address;
    public double latitude;
    public double longitude;

    //Default constructor
    public Hangout() {

    }

    //Constructor to read from parcel
    public Hangout(Parcel source) {
        name = source.readString();
        address = source.readString();
        latitude = source.readDouble();
        longitude = source.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(address);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* REQUIRED FOR PARCELLABLE. DO NOT MODIFY IN ANY WAY */
    public static final Creator<Hangout> CREATOR = new Creator<Hangout>() {

                                                     @Override
                                                     public Hangout createFromParcel(
                                                                     Parcel source) {
                                                         return new Hangout(source);
                                                     }

                                                     @Override
                                                     public Hangout[] newArray(
                                                                     int size) {
                                                         return new Hangout[size];
                                                     }
                                                 };

}
