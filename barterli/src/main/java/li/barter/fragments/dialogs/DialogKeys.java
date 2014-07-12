/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.fragments.dialogs;

/**
 * Constant interface for dialog constants. DO NOT implement
 * 
 * @author Vinay S Shenoy
 */
public interface DialogKeys {

    /** Key used to identify the dialog title. */
    public static final String TITLE_ID          = "title_id";

    /** Key used to identify the message to be displayed in the dialog. */
    public static final String MESSAGE_ID        = "message_id";

    /** Key used to identify the positive button text. */
    public static final String POSITIVE_LABEL_ID = "positive_label_id";

    /** Key used to identify the negative button text. */
    public static final String NEGATIVE_LABEL_ID = "negative_label_id";

    /** Key used to identify the neutral button text. */
    public static final String NEUTRAL_LABEL_ID  = "neutral_label_id";
    
    /** Key used to identify the neutral button text. */
    public static final String HINT_LABEL_ID 	 = "hint_label_id";
    

    /** Key used to identify if the dialog is cancellable. */
    public static final String CANCELLABLE       = "cancellable";

    /** Key used to identify the icon for the alert dialog. */
    public static final String ICON_ID           = "icon_id";

    /** Key used to identify the theme to be used for the alert dialog. */
    public static final String THEME             = "theme";

    /** Key identifying the message params for formatting the message string */
    public static final String MESSAGE_PARAMS    = "message_params";

    /**
     * Keys for the items resource IDs for choices
     */
    public static final String ITEMS_ID          = "items_id";

}
