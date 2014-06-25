/*
 * Copyright (C) 2011 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Modified by Vinay S Shenoy on 19/5/13
 */

package com.android.volley.toolbox;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.protocol.HTTP;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.MultiPartRequest.MultiPartParam;

/**
 * An {@link HttpStack} based on {@link HttpURLConnection}.
 */
public class HurlStack implements HttpStack {

    private static final String    HEADER_CONTENT_TYPE              = "Content-Type";
    private static final String    HEADER_USER_AGENT                = "User-Agent";
    private static final String    HEADER_CONTENT_DISPOSITION       = "Content-Disposition";
    private static final String    HEADER_CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding";
    private static final String    CONTENT_TYPE_MULTIPART           = "multipart/form-data; charset=%s; boundary=%s";
    private static final String    BINARY                           = "binary";
    private static final String    CRLF                             = "\r\n";
    private static final String    FORM_DATA                        = "form-data; name=\"%s\"";
    private static final String    BOUNDARY_PREFIX                  = "--";
    private static final String    CONTENT_TYPE_OCTET_STREAM        = "application/octet-stream";
    private static final String    FILENAME                         = "filename=%s";
    private static final String    COLON_SPACE                      = ": ";
    private static final String    SEMICOLON_SPACE                  = "; ";

    private final SSLSocketFactory mSslSocketFactory;
    private String                 mUserAgent;

    public HurlStack(String userAgent) {
        this(null, userAgent);
    }

    /**
     * @param sslSocketFactory SSL factory to use for HTTPS connections
     */
    public HurlStack(SSLSocketFactory sslSocketFactory, String userAgent) {

        mSslSocketFactory = sslSocketFactory;
        mUserAgent = userAgent;
    }

    /**
     * Add headers and user agent to an {@code }
     * 
     * @param connection The {@linkplain HttpURLConnection} to add request
     *            headers to
     * @param userAgent The User Agent to identify on server
     * @param additionalHeaders The headers to add to the request
     */
    private static void addHeadersToConnection(HttpURLConnection connection,
                    String userAgent, Map<String, String> additionalHeaders) {

        VolleyLog.v("Adding headers: %s", additionalHeaders);
        connection.setRequestProperty(HEADER_USER_AGENT, userAgent);
        for (String headerName : additionalHeaders.keySet()) {
            connection.addRequestProperty(headerName, additionalHeaders
                            .get(headerName));
        }
    }

    @Override
    public HttpResponse performRequest(Request<?> request,
                    Map<String, String> additionalHeaders) throws IOException,
                    AuthFailureError {

        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(request.getHeaders());
        map.putAll(additionalHeaders);
        URL parsedUrl = new URL(request.getUrl());

        request.addMarker(String.format("Calling url %s", parsedUrl));
        HttpURLConnection connection = openConnection(parsedUrl, request);

        if (request instanceof MultiPartRequest) {
            setConnectionParametersForMultipartRequest(connection, request, map, mUserAgent);
        } else {

            setConnectionParametersForRequest(connection, request, map, mUserAgent);
        }

        // Initialize HttpResponse with data from the HttpURLConnection.
        ProtocolVersion protocolVersion = new ProtocolVersion("HTTP", 1, 1);
        int responseCode = connection.getResponseCode();
        if (responseCode == -1) {
            // -1 is returned by getResponseCode() if the response code could
            // not be retrieved.
            // Signal to the caller that something was wrong with the
            // connection.
            throw new IOException("Could not retrieve response code from HttpUrlConnection.");
        }
        StatusLine responseStatus = new BasicStatusLine(protocolVersion, connection
                        .getResponseCode(), connection.getResponseMessage());
        BasicHttpResponse response = new BasicHttpResponse(responseStatus);
        response.setEntity(entityFromConnection(connection));
        for (Entry<String, List<String>> header : connection.getHeaderFields()
                        .entrySet()) {
            if (header.getKey() != null) {
                Header h = new BasicHeader(header.getKey(), header.getValue()
                                .get(0));
                response.addHeader(h);
            }
        }
        return response;
    }

    /**
     * Perform a multipart request on a connection
     * 
     * @param connection The Connection to perform the multi part request
     * @param request
     * @param additionalHeaders
     * @param multipartParams The params to add to the Multi Part request
     * @param filesToUpload The files to upload
     * @throws ProtocolException
     */
    private static void setConnectionParametersForMultipartRequest(
                    HttpURLConnection connection, Request<?> request,
                    HashMap<String, String> additionalHeaders, String userAgent)
                    throws IOException, ProtocolException {

        final String charset = ((MultiPartRequest<?>) request)
                        .getProtocolCharset();
        final int curTime = (int) (System.currentTimeMillis() / 1000);
        final String boundary = BOUNDARY_PREFIX + curTime;
        
        if(request.getMethod() == Method.POST) {
            connection.setRequestMethod("POST");
        } else if(request.getMethod() == Method.PUT) {
            connection.setRequestMethod("PUT");
        } else {
            //Any other case
            connection.setRequestMethod("POST");
        }
        connection.setDoOutput(true);
        connection.setRequestProperty(HEADER_CONTENT_TYPE, String
                        .format(CONTENT_TYPE_MULTIPART, charset, curTime));
        connection.setChunkedStreamingMode(0);

        Map<String, MultiPartParam> multipartParams = ((MultiPartRequest<?>) request)
                        .getMultipartParams();
        Map<String, String> filesToUpload = ((MultiPartRequest<?>) request)
                        .getFilesToUpload();
        PrintWriter writer = null;
        try {
            addHeadersToConnection(connection, userAgent, additionalHeaders);
            OutputStream out = connection.getOutputStream();
            writer = new PrintWriter(new OutputStreamWriter(out, charset), true);

            for (String key : multipartParams.keySet()) {
                MultiPartParam param = multipartParams.get(key);

                final String part = new StringBuilder(boundary).append(CRLF)
                                .append(String.format(HEADER_CONTENT_DISPOSITION
                                                + COLON_SPACE + FORM_DATA, key))
                                .append(CRLF)
                                .append(HEADER_CONTENT_TYPE + COLON_SPACE
                                                + param.contentType)
                                .append(CRLF).append(CRLF).append(param.value)
                                .append(CRLF).toString();
                if(VolleyLog.sDebug) {
                    VolleyLog.d("%s", part);
                }
                writer.append(part).flush();
            }

            for (String key : filesToUpload.keySet()) {

                File file = new File(filesToUpload.get(key));

                if (!file.exists()) {
                    throw new IOException(String.format("File not found: %s", file
                                    .getAbsolutePath()));
                }

                if (file.isDirectory()) {
                    throw new IOException(String.format("File is a directory: %s", file
                                    .getAbsolutePath()));
                }

                final String part = new StringBuilder(boundary).append(CRLF)
                                .append(String.format(HEADER_CONTENT_DISPOSITION
                                                + COLON_SPACE
                                                + FORM_DATA
                                                + SEMICOLON_SPACE + FILENAME, key, file
                                                .getName()))
                                .append(CRLF)
                                .append(HEADER_CONTENT_TYPE + COLON_SPACE
                                                + CONTENT_TYPE_OCTET_STREAM)
                                .append(CRLF)
                                .append(HEADER_CONTENT_TRANSFER_ENCODING
                                                + COLON_SPACE + BINARY)
                                .append(CRLF).append(CRLF).toString();
                if(VolleyLog.sDebug) {
                    VolleyLog.d("%s", part);
                }
                writer.append(part)
                                .flush();

                BufferedInputStream input = null;
                try {
                    FileInputStream fis = new FileInputStream(file);
                    input = new BufferedInputStream(fis);
                    int bufferLength = 0;

                    byte[] buffer = new byte[1024];
                    while ((bufferLength = input.read(buffer)) > 0) {
                        out.write(buffer, 0, bufferLength);
                    }
                    out.flush(); // Important! Output cannot be closed. Close of
                                 // writer will close
                                 // output as well.
                } finally {
                    if (input != null)
                        try {
                            input.close();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                }
                writer.append(CRLF).flush(); // CRLF is important! It indicates
                                             // end of binary
                                             // boundary.
            }

            // End of multipart/form-data.
            writer.append(boundary + BOUNDARY_PREFIX).append(CRLF).flush();

        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Initializes an {@link HttpEntity} from the given
     * {@link HttpURLConnection}.
     * 
     * @param connection
     * @return an HttpEntity populated with data from <code>connection</code>.
     */
    private static HttpEntity entityFromConnection(HttpURLConnection connection) {

        BasicHttpEntity entity = new BasicHttpEntity();
        InputStream inputStream;
        try {
            inputStream = connection.getInputStream();
        } catch (IOException ioe) {
            inputStream = connection.getErrorStream();
        }
        entity.setContent(inputStream);
        entity.setContentLength(connection.getContentLength());
        entity.setContentEncoding(connection.getContentEncoding());
        entity.setContentType(connection.getContentType());
        return entity;
    }

    /**
     * Create an {@link HttpURLConnection} for the specified {@code url}.
     */
    protected HttpURLConnection createConnection(URL url) throws IOException {

        return (HttpURLConnection) url.openConnection();
    }

    /**
     * Opens an {@link HttpURLConnection} with parameters.
     * 
     * @param url
     * @return an open connection
     * @throws IOException
     */
    private HttpURLConnection openConnection(URL url, Request<?> request)
                    throws IOException {

        HttpURLConnection connection = createConnection(url);

        int timeoutMs = request.getTimeoutMs();
        connection.setConnectTimeout(timeoutMs);
        connection.setReadTimeout(timeoutMs);
        connection.setUseCaches(false);
        connection.setDoInput(true);

        // use caller-provided custom SslSocketFactory, if any, for HTTPS
        if ("https".equals(url.getProtocol()) && mSslSocketFactory != null) {
            ((HttpsURLConnection) connection)
                            .setSSLSocketFactory(mSslSocketFactory);
        }

        return connection;
    }

    private static void setConnectionParametersForRequest(
                    HttpURLConnection connection, Request<?> request,
                    HashMap<String, String> additionalHeaders, String userAgent)
                    throws IOException, AuthFailureError {

        addHeadersToConnection(connection, userAgent, additionalHeaders);
        switch (request.getMethod()) {
            case Method.GET:
                // Not necessary to set the request method because connection
                // defaults to GET but
                // being explicit here.
                connection.setRequestMethod("GET");
                break;
            case Method.DELETE:
                connection.setRequestMethod("DELETE");
                break;
            case Method.POST:
                connection.setRequestMethod("POST");
                addBodyIfExists(connection, request);
                break;
            case Method.PUT:
                connection.setRequestMethod("PUT");
                addBodyIfExists(connection, request);
                break;
            default:
                throw new IllegalStateException("Unknown method type.");
        }
    }

    private static void addBodyIfExists(HttpURLConnection connection,
                    Request<?> request) throws IOException, AuthFailureError {

        byte[] body = request.getBody();
        if (body != null) {
            if (VolleyLog.sDebug) {
                VolleyLog.v("Request url: %s\nBody: %s", request.getUrl(), new String(body, HTTP.UTF_8));
            }
            connection.setDoOutput(true);
            connection.addRequestProperty(HEADER_CONTENT_TYPE, request
                            .getBodyContentType());
            DataOutputStream out = new DataOutputStream(connection.getOutputStream());
            out.write(body);
            out.close();
        }
    }
}
