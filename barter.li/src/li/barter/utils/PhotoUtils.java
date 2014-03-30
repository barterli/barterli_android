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

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

/**
 * @author Sharath Pandeshwar
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class PhotoUtils {

    /**
     * @param contentUri : URI as provided by Gallery Content Provider
     * @param context
     * @return Actual Path of the image
     */

    /*
     * public static String getRealPathFromURI(Context context, Uri contentUri)
     * { Cursor cursor = null; try { String[] proj = {
     * MediaStore.Images.Media.DATA }; cursor = context.getContentResolver()
     * .query(contentUri, proj, null, null, null); if (cursor == null) {
     * Log.v("getRealPathFromURI", "cursor is null"); } else {
     * Log.v("getRealPathFromURI", "cursor is not null" +
     * cursor.getColumnCount() + ":"); } int column_index = cursor
     * .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
     * Log.v("getRealPathFromURI", ":" + Integer.toString(column_index) + " : "
     * + cursor.getColumnName(column_index)); cursor.moveToFirst(); return
     * cursor.getString(column_index); } finally { if (cursor != null) {
     * cursor.close(); } } }
     */

    /**
     * @param bitmap Bitmap of image to be saved
     * @param title title to be given to the image to be saved
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
     * @param context
     * @param photoUri
     * @return orientation of the image
     */
    public static int getOrientation(Context context, Uri photoUri) {
        Cursor cursor = context.getContentResolver()
                        .query(photoUri, new String[] {
                            MediaStore.Images.ImageColumns.ORIENTATION
                        }, null, null, null);

        if (cursor.getCount() != 1) {
            return -1;
        }
        cursor.moveToFirst();
        return cursor.getInt(0);
    }

    /**
     * @param Context
     * @param uri
     * @param source : Options are "Gallery", "Camera"
     * @param shouldCompress : true or false
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

            tempPath = getPath(context, uri);
        } else {
            File f = new File(uri.getPath());
            tempPath = f.getAbsolutePath();
            //tempPath = getRealPathFromURI(uri, context);

        }
        BitmapFactory.Options btmapOptions = new BitmapFactory.Options();
        bm = BitmapFactory.decodeFile(tempPath, btmapOptions);

        if (shouldCompress) {
            bm = Bitmap.createScaledBitmap(bm, 200, 200, true);
        }

        //return bm;        

        // Orientation Change is currently working only for pictures taken from
        // Gallery

        if (source.equals("Gallery")) {
            if (PhotoUtils.getOrientation(context, uri) != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(PhotoUtils.getOrientation(context, uri));
                Bitmap rotatedBm = Bitmap
                                .createBitmap(bm, 0, 0, bm.getWidth(), bm
                                                .getHeight(), matrix, true);
                return rotatedBm;
            } else {
                return bm;
            }
        } else {
            return bm;
        }
    } // End of rotateBitmapIfNeededAndCompress 

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                        .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                        .getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                        .getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     * 
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri,
                    String selection, String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
            column
        };

        try {
            cursor = context.getContentResolver()
                            .query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     * 
     * @param context The context.
     * @param uri The Uri to query.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("NewApi")
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/"
                                    + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris
                                .withAppendedId(Uri
                                                .parse("content://downloads/public_downloads"), Long
                                                .valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                    split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

}
