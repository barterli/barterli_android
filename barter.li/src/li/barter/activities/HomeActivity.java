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

package li.barter.activities;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;

import li.barter.R;
import li.barter.adapters.HomeNavDrawerAdapter;
import li.barter.fragments.BooksAroundMeFragment;

/**
 * @author Vinay S Shenoy Main Activity for holding the Navigation Drawer and
 *         manages loading different fragments/options menus on Navigation items
 *         clicked
 */
public class HomeActivity extends AbstractBarterLiActivity {

    private static final String   TAG = "HomeActivity";

    /**
     * Drawer Layout that contains the Navigation Drawer
     */
    private DrawerLayout          mDrawerLayout;

    /**
     * Drawer toggle for Action Bar
     */
    private ActionBarDrawerToggle mDrawerToggle;

    /**
     * {@link ListView} that provides the navigation items
     */
    private ListView              mNavListView;

    /**
     * {@link BaseAdapter} implementation for Navigation drawer item
     */
    private HomeNavDrawerAdapter  mNavDrawerAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavListView = (ListView) findViewById(R.id.list_nav_drawer);

        setActionBarDisplayOptions(ActionBar.DISPLAY_SHOW_TITLE);
        initDrawer();
        if (savedInstanceState == null) {
            loadBooksAroundMeFragment();
        } else {
            // Do we need to remember which fragment was visible and load that
            // one instead? Or does the Android system take care of it?
        }

    }

    /**
     * Loads the {@link BooksAroundMeFragment} into the fragment container
     */
    private void loadBooksAroundMeFragment() {

        final FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                        .add(R.id.frame_content,
                                        Fragment.instantiate(
                                                        this,
                                                        BooksAroundMeFragment.class
                                                                        .getName()),
                                        null).commit();
    }

    /**
     * Initialize the Action Bar Drawer toggle
     */
    private void initDrawer() {

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                        R.drawable.ic_navigation_drawer, R.string.drawer_open,
                        R.string.drawer_closed) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }

        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        
        mNavDrawerAdapter = new HomeNavDrawerAdapter(this, R.array.nav_drawer_titles, R.array.nav_drawer_descriptions);
        mNavListView.setAdapter(mNavDrawerAdapter);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        setOptionsGroupHidden(menu, mDrawerLayout.isDrawerOpen(mNavListView));
        return super.onPrepareOptionsMenu(menu);
    }

    private void setOptionsGroupHidden(final Menu menu, final boolean drawerOpen) {

        menu.setGroupEnabled(R.id.group_hide_on_drawer_open, !drawerOpen);
        menu.setGroupVisible(R.id.group_hide_on_drawer_open, !drawerOpen);

    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {

        getMenuInflater().inflate(R.menu.menu_books_around_me, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {

            case R.id.action_scan_book: {
                startActivity(new Intent(this, ScanIsbnActivity.class));
                return true;
            }

            case R.id.action_my_books: {
                // TODO do we have a screen to show here
                return true;
            }

            case R.id.action_profile: {
                // TODO Show profile screen
                return true;
            }

            case R.id.action_add_book: {
                startActivity(new Intent(this, AddOrEditBookActivity.class));
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

}
