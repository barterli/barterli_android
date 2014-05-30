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

package li.barter.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import li.barter.R;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.Keys;

/**
 * Fragment to display Any Web Content
 * 
 * @author Sharath Pandeshwar
 */
@FragmentTransition(enterAnimation = R.anim.slide_in_from_right, exitAnimation = R.anim.zoom_out, popEnterAnimation = R.anim.zoom_in, popExitAnimation = R.anim.slide_out_to_right)
public class ShowWebViewFragment extends AbstractBarterLiFragment {

    private static final String TAG = "ShowWebViewFragment";

    /**
     * Webview to display tribute page.
     */
    private WebView             mWebView;

    @Override
    public View onCreateView(final LayoutInflater inflater,
                    final ViewGroup container, final Bundle savedInstanceState) {
        init(container, savedInstanceState);
        //setHasOptionsMenu(true);
        setActionBarDrawerToggleEnabled(false);
        final View view = inflater
                        .inflate(R.layout.fragment_show_webview, null);
        mWebView = (WebView) view.findViewById(R.id.view_web);
        mWebView.setWebViewClient(new myWebViewClient());

        final Bundle myArgs = getArguments();
        final String mUrlToLoad = myArgs.getString(Keys.URL_TO_LOAD);
        mWebView.loadUrl(mUrlToLoad);
        return view;
    }

    @Override
    protected Object getVolleyTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(final int requestId,
                    final IBlRequestContract request,
                    final ResponseInfo response) {
    }

    @Override
    public void onBadRequestError(final int requestId,
                    final IBlRequestContract request, final int errorCode,
                    final String errorMessage, final Bundle errorResponseBundle) {
    }

    public class myWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view,
                        final String url) {
            view.loadUrl(url);
            return true;
        }
    }

}
