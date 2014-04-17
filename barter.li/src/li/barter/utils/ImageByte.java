
package li.barter.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ImageByte {

    public static final String TAG = "ImageByte";

    public byte[] getUrlBytes(final String urlSpec) throws IOException {
        final URL url = new URL(urlSpec);
        final HttpURLConnection connection = (HttpURLConnection) url
                        .openConnection();

        try {
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return null;
            }

            int bytesRead = 0;
            final byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();
        } finally {
            connection.disconnect();
        }
    }
}
