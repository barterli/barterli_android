package li.barter.activities;

import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import li.barter.R;

import static org.fest.assertions.api.ANDROID.assertThat;

/**
 * Created by imran on 30/06/14.
 */

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class HomeActivityTest {

    private HomeActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.buildActivity(HomeActivity.class).create().visible().get();

        //assertTrue(activity != null);
    }

    @Test
    public void sampleTest() {
        TextView textView = (TextView) activity.findViewById(R.id.hello_world);

        assertThat(textView).containsText("Hello");
    }


}
