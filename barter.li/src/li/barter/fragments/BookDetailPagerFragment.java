package li.barter.fragments;

import java.util.ArrayList;

import li.barter.R;
import li.barter.data.DatabaseColumns;
import li.barter.data.SQLiteLoader;
import li.barter.data.TableSearchBooks;
import li.barter.http.IBlRequestContract;
import li.barter.http.ResponseInfo;
import li.barter.utils.AppConstants.Keys;
import li.barter.utils.AppConstants.Loaders;
import li.barter.utils.Logger;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * @author Anshul Kamboj Fragment for Paging Books Around Me. Also contains
 *         a Profile that the user can chat directly with the owner
 */

public class BookDetailPagerFragment extends AbstractBarterLiFragment implements LoaderCallbacks<Cursor>{

	
	private static final String           TAG                     = "BookDetailPagerFragment";

	
	/**
	 *  {@link BookPageAdapter} holds the {@link BookDetailFragment} as viewpager
	 */
	private BookPageAdapter				 mAdapter;
	
	
	/**
	 *  ViewPager which holds the fragment
	 */
    private ViewPager 					 mBookDetailPager;
    
    /**
	 *  Counter to load the current number of pages for {@link BookDetailFragment}
	 */
    private int 						 mBookCounter;
    
    /**
	 *  It holds the Book which is clicked
	 */
    private int							 mBookPosition;
    
    /**
   	 *  These arrays holds bookids and userids for all the books in the pager to map them
   	 */
    private ArrayList<String>			 mBookIdArray=new ArrayList<String>();
    private ArrayList<String> 			 mUserIdArray=new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
    		Bundle savedInstanceState) {
    	// TODO Auto-generated method stub
    	init(container, savedInstanceState);
    	
    	final View view = inflater
				.inflate(R.layout.fragment_book_detail_pager, container, false);
    	final Bundle extras = getArguments();

		if (extras != null) {
			mBookCounter = extras.getInt(Keys.BOOK_COUNT);
			mBookPosition = extras.getInt(Keys.BOOK_POSITION);
			
		}
    	
		 mBookDetailPager = (ViewPager)view.findViewById(R.id.bookpager);
		 
    	
        loadBookSearchResults();
    	return view;
    }
	
    /**
     * Starts the loader for book search results
     */
    private void loadBookSearchResults() {
        //TODO Add filters for search results
        getLoaderManager().restartLoader(Loaders.SEARCH_BOOKS, null, this);
    }
    
    
    
    
    public  class BookPageAdapter extends FragmentStatePagerAdapter {
        public BookPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mBookCounter;
        }

        @Override
        public Fragment getItem(int position) {
        	Logger.d(TAG, mUserIdArray.get(position)+"  "+mBookIdArray.get(position));
            return BookDetailFragment.newInstance(mUserIdArray.get(position),mBookIdArray.get(position));
            
        }
    }
    
	@Override
	protected Object getVolleyTag() {
		// TODO Auto-generated method stub
		
		return TAG;
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

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
		   if (loaderId == Loaders.SEARCH_BOOKS) {
	            return new SQLiteLoader(getActivity(), false, TableSearchBooks.NAME, null, null, null, null, null, null, null);
	        } else {
	            return null;
	        }
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		 if (loader.getId() == Loaders.SEARCH_BOOKS) {

	            Logger.d(TAG, "Cursor Loaded with count: %d", cursor.getCount());
	            mBookCounter=cursor.getCount();
	            cursor.moveToFirst();
	            for(int i=0;i<mBookCounter;i++)
	            {
	            	mBookIdArray.add(cursor.getString(cursor.getColumnIndex(DatabaseColumns.BOOK_ID)));
	            	mUserIdArray.add(cursor.getString(cursor.getColumnIndex(DatabaseColumns.USER_ID)));
	            	cursor.moveToNext();
	               
	            }
	            mAdapter = new BookPageAdapter(getChildFragmentManager());
	            
	            mBookDetailPager.setAdapter(mAdapter);
	            mBookDetailPager.setCurrentItem(mBookPosition);
	           
	        }
		
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		// TODO Auto-generated method stub
		
	}

}
