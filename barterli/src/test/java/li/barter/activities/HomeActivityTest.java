package li.barter.activities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.fest.assertions.api.ANDROID.assertThat;

/**
 * Created by imran on 30/06/14.
 */

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class HomeActivityTest {

    private HomeActivity activity;

    @Test
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(HomeActivity.class).create().visible().get();
        assertThat(activity).isNotNull();
    }


}
