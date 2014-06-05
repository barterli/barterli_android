package li.barter.fragments;

import li.barter.R;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class BarterLiDescriptionFragment extends AbstractBarterLiFragment{
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View view = inflater
                .inflate(R.layout.layout_barter_desc, null);
		return view;
	}
	

	@Override
    protected Object getVolleyTag() {
        return hashCode();
    }

	@Override
	public void onSuccess(int requestId, IBlRequestContract request,
			ResponseInfo response) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onBadRequestError(int requestId, IBlRequestContract request,
			int errorCode, String errorMessage, Bundle errorResponseBundle) {
		// TODO Auto-generated method stub
		
	}


	public static BarterLiDescriptionFragment newInstance() {
		BarterLiDescriptionFragment f = new BarterLiDescriptionFragment();
		return f;
	}
	
	
	

}
