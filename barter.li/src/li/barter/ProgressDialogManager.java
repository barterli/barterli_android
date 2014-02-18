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
