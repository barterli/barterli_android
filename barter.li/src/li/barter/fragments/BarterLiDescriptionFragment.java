
package li.barter.fragments;

import com.google.android.gms.plus.PlusOneButton;

import li.barter.R;
import li.barter.analytics.AnalyticsConstants.Screens;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants;
import li.barter.utils.AppConstants.RequestCodes;
import android.content.Intent;
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
    
    private PlusOneButton		mPlusOneButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                    Bundle savedInstanceState) {
        mLoadedIndividually = false;
        final View view = inflater.inflate(R.layout.layout_barter_desc, null);
        mPlusOneButton=(PlusOneButton)view.findViewById(R.id.plus_one_button);
 
        return view;
    }
    @Override
    public void onResume() {
    	super.onResume();
    	mPlusOneButton.initialize(AppConstants.PLAY_STORE_LINK, RequestCodes.PLUS_LIKE);
    	
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if(requestCode==RequestCodes.PLUS_LIKE)
    	{
        	mPlusOneButton.initialize(AppConstants.PLAY_STORE_LINK, RequestCodes.PLUS_LIKE);
    	}
    }
    @Override
    protected Object getTaskTag() {
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
            return Screens.BARTERLI_DESCRIPTION;
        } else {
            /*
             * We don't need to track this screen since it is loaded within a
             * viewpager. We will just track gthe parent fragment
             */
            return "";
        }
    }

}
