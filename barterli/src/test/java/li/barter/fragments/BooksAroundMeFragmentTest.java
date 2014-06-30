package li.barter.fragments;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * Created by imran on 30/06/14.
 */

@RunWith(RobolectricTestRunner.class)
public class BooksAroundMeFragmentTest extends FragmentBase<BooksAroundMeFragment> {

    private BooksAroundMeFragment fragment;

    @Before
    public void setUp() {
        fragment = new BooksAroundMeFragment();
    }

    @Test
    public void createsAndDestroysFragment() {
        startFragment(fragment);
    }

    @Test
    public void pausesAndResumesFragment() {
        startFragment(fragment);
        pauseAndResumeFragment();
    }

    @Test
    public void recreatesFragment() {
        startFragment(fragment);
        fragment = recreateFragment();
    }
}
