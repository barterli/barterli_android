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

package li.barter.widgets.autocomplete;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class representing a suggestion
 * 
 * @author Vinay S Shenoy
 */
public class Suggestion implements Parcelable {

    /**
     * The suggestion id. Used for selecting the suggestion when an item from
     * the drop down is tapped
     */
    public String id;

    /**
     * The name of the suggestion. Used for displaying the title label
     */
    public String name;

    /**
     * Any image URLs, if present, to display as a suggestion image
     */
    public String imageUrl;

    public Suggestion() {
    }

    public Suggestion(Parcel source) {
        id = source.readString();
        name = source.readString();
        imageUrl = source.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(imageUrl);
    }

    @Override
    public String toString() {
        return String.format("Suggestion %s, %s, %s", id, name, imageUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /* REQUIRED FOR PARCELLABLE. DO NOT MODIFY IN ANY WAY */
    public static final Creator<Suggestion> CREATOR = new Creator<Suggestion>() {

                                                        @Override
                                                        public Suggestion createFromParcel(
                                                                        Parcel source) {
                                                            return new Suggestion(source);
                                                        }

                                                        @Override
                                                        public Suggestion[] newArray(
                                                                        int size) {
                                                            return new Suggestion[size];
                                                        }
                                                    };

}
