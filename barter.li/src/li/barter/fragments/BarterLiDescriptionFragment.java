
package li.barter.fragments;

import li.barter.R;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BarterLiDescriptionFragment extends AbstractBarterLiFragment {

    /**
     * Whether this fragment is loaded individually or as part of a
     * pager/fragment setup
     */
    private boolean mLoadedIndividually;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                    Bundle savedInstanceState) {
        mLoadedIndividually = false;
        final View view = inflater.inflate(R.layout.layout_barter_desc, null);
        return view;
    }

    @Override
    protected Object getVolleyTag() {
        return hashCode();
    }

    @Override
    public void onSuccess(int requestId, IBlRequestContract request,
                    ResponseInfo response) {

    }

    @Override
    public void onBadRequestError(int requestId, IBlRequestContract request,
                    int errorCode, String errorMessage,
                    Bundle errorResponseBundle) {

    }

    public static BarterLiDescriptionFragment newInstance() {
        BarterLiDescriptionFragment f = new BarterLiDescriptionFragment();
        return f;
    }

    @Override
    protected String getAnalyticsScreenName() {

        if (mLoadedIndividually) {
            return "barter.li Description";
        } else {
            /*
             * We don't need to track this screen since it is loaded within a
             * viewpager. We will just track gthe parent fragment
             */
            return "";
        }
    }

}
