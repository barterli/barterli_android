/*******************************************************************************
 * Copyright 2014,  barter.li
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

package li.barter.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Team implements Parcelable {
    private String mName;
    private String mDescription;
    private String mEmail;
    private String mImageUrl;

    public Team() {

    }

    @Override
    public String toString() {
        return mName;
    }

    public String getName() {
        return mName;
    }

    public void setName(final String name) {
        mName = name;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(final String description) {
        mDescription = description;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(final String email) {
        mEmail = email;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(final String imageUrl) {
        mImageUrl = imageUrl;
    }

    //Constructor to read from parcel
    public Team(final Parcel source) {
        mName = source.readString();
        mEmail = source.readString();
        mDescription = source.readString();
        mImageUrl = source.readString();
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(mName);
        dest.writeString(mEmail);
        dest.writeString(mDescription);
        dest.writeString(mImageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* REQUIRED FOR PARCELLABLE. DO NOT MODIFY IN ANY WAY */
    public static final Creator<Team> CREATOR = new Creator<Team>() {

                                                  @Override
                                                  public Team createFromParcel(
                                                                  final Parcel source) {
                                                      return new Team(source);
                                                  }

                                                  @Override
                                                  public Team[] newArray(
                                                                  final int size) {
                                                      return new Team[size];
                                                  }
                                              };

}
