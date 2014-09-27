/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.Locale;

import li.barter.fragments.AboutMeFragment;
import li.barter.fragments.AbstractBarterLiFragment;
import li.barter.fragments.MyBooksFragment;

/**
 * Pager Adapter for tabs on Profle screen
 * 
 * @author Vinay S Shenoy
 */
public class ProfileFragmentsAdapter extends FragmentStatePagerAdapter {

    private static final String        TAG   = "ProfileFragmentsAdapter";
    private static final int           COUNT = 2;

    private AbstractBarterLiFragment[] mFragments;

    /**
     * @param fm
     */
    public ProfileFragmentsAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new AbstractBarterLiFragment[COUNT];
    }

    @Override
    public Fragment getItem(int position) {

        AbstractBarterLiFragment fragment = null;
        if (position == 0) {
            fragment = new AboutMeFragment();
        } else if (position == 1) {
            fragment = new MyBooksFragment();
        }

        if (fragment != null) {
            mFragments[position] = fragment;
        }
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mFragments[position] = null;
        super.destroyItem(container, position, object);
    }

    public AbstractBarterLiFragment getFragmentAtPosition(int position) {
        if (position >= COUNT) {
            throw new IllegalArgumentException(String.format(Locale.US, "Asking for position %d with count %d", position, COUNT));
        }
        
        return mFragments[position];
    }

    @Override
    public int getCount() {
        return COUNT;
    }

}
