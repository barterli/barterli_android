package li.barter.fragments;

import android.widget.GridView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.FragmentTestUtil;

import li.barter.R;

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
    public void createsAndDestroysFragment() throws Exception {
        FragmentTestUtil.startFragment(fragment);
        assertThat(fragment).isNotNull();
        gridView = (GridView) fragment.getActivity().findViewById(R.id.grid_books_around_me);
        assertThat(gridView).isNotNull();
    }

}
