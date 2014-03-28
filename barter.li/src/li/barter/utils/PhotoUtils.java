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

import java.io.File;
import java.io.FileOutputStream;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

/**
 * @author Sharath Pandeshwar
 * 
 */
public class PhotoUtils {

	/**
	 * 
	 * 
	 * @param contentUri
	 *            : URI as provided by Gallery Content Provider
	 * @param context
	 * @return Actual Path of the image
	 */

	public static String getRealPathFromURI(Uri contentUri, Context context) {
		Cursor cursor = null;
		try {
			String[] proj = { MediaStore.Images.Media.DATA };
			cursor = context.getContentResolver().query(contentUri, proj, null,
					null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	} // End of getRealPathFromURI

	/**
	 * 
	 * 
	 * @param bitmap
	 *            Bitmap of image to be saved
	 * @param title
	 *            title to be given to the image to be saved
	 */

	public static void saveImage(Bitmap bitmap, String title) {
		File file = new File(Environment.getExternalStorageDirectory(), title);
		if (file.exists())
			file.delete();
		try {
			FileOutputStream out = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * 
	 * @param context
	 * @param photoUri
	 * @return orientation of the image
	 */
	public static int getOrientation(Context context, Uri photoUri) {
		Cursor cursor = context.getContentResolver().query(photoUri,
				new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
				null, null, null);

		if (cursor.getCount() != 1) {
			return -1;
		}
		cursor.moveToFirst();
		return cursor.getInt(0);
	}

	/**
	 * 
	 * @param Context
	 * @param uri
	 * @param source
	 *            : Options are "Gallery", "Camera"
	 * @param shouldCompress
	 *            : true or false
	 * @return
	 */

	public static Bitmap rotateBitmapIfNeededAndCompressIfTold(Context context,
			Uri uri, String source, boolean shouldCompress) {

		if (!(source.equals("Camera") || (source.equals("Gallery")))) {
			return null;
		}

		Bitmap bm = null;
		String tempPath;
		if (source.equals("Gallery")) {
			tempPath = getRealPathFromURI(uri, context);
		} else {
			File f = new File(uri.getPath());
			tempPath = f.getAbsolutePath();
		}
		BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
		bm = BitmapFactory.decodeFile(tempPath, btmapOptions);
		if (shouldCompress) {
			bm = Bitmap.createScaledBitmap(bm, 200, 200, true);
		}

		// Orientation Change is currently working only for pictures taken from
		// Gallery

		if (source.equals("Gallery")) {
			if (PhotoUtils.getOrientation(context, uri) != 0) {
				Matrix matrix = new Matrix();
				matrix.postRotate(PhotoUtils.getOrientation(context, uri));
				Bitmap rotatedBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
						bm.getHeight(), matrix, true);
				return rotatedBm;
			} else {
				return bm;
			}
		} else {
			return bm;
		}
	} // End of rotateBitmapIfNeededAndCompress 

}
