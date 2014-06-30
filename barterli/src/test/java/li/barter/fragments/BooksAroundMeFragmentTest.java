package li.barter.fragments;

import android.widget.GridView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import li.barter.activities.HomeActivity;
import li.barter.utils.AppConstants;

import static org.fest.assertions.api.ANDROID.assertThat;

/**
 * Created by imran on 30/06/14.
 */

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class BooksAroundMeFragmentTest {

    private BooksAroundMeFragment fragment;
    private GridView gridView;

    @Before
    public void setUp() {
        fragment = new BooksAroundMeFragment();
    }

    @Test
    public void shouldHaveFragment() throws Exception {
        HomeActivity activity = Robolectric.buildActivity(HomeActivity.class).create().visible().get();
        assertThat(activity.getSupportFragmentManager().findFragmentByTag(AppConstants.FragmentTags.BOOKS_AROUND_ME)).isNotNull();
    }

}
