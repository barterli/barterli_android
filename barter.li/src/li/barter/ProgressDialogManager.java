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

import android.app.ProgressDialog;
import android.content.Context;

public class ProgressDialogManager {
	ProgressDialog pDialog;

	public void showProgresDialog(Context context, String message) {
		pDialog = new ProgressDialog(context);
		pDialog.setMessage(message);
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
	}

	public void dismissProgresDialog() {
		pDialog.dismiss();
	}
}
