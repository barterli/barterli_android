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

/**
 * Class representing a suggestion
 * 
 * @author Vinay S Shenoy
 */
public class Suggestion {

    /**
     * The suggestion id. Used for selecting the suggestion when an item from
     * the drop down is tapped
     */
    public final String id;

    /**
     * The name of the suggestion. Used for displaying the title label
     */
    public final String name;

    /**
     * @param id The suggestion id
     * @param name The suggestion name
     */
    public Suggestion(final String id, final String name) {
        this.id = id;
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }

}
