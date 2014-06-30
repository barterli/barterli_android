package li.barter.activities;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

/**
 * Created by imran on 30/06/14.
 */

@RunWith(RobolectricTestRunner.class)
public class HomeActivityTest {

    private HomeActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(HomeActivity.class).create().visible().get();

        //assertTrue(activity != null);
    }


}
