package li.barter.fragments;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import org.junit.After;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

/**
 * Created by imran on 30/06/14.
 */
public class FragmentBase<T extends Fragment> {

    private static final String FRAGMENT_TAG = "fragment";

    private ActivityController<FragmentActivity> controller;
    private FragmentActivity activity;
    private T fragment;

    /**
     * Adds the fragment to a new blank activity, thereby fully
     * initializing its view.
     */
    public void startFragment(T fragment) {
        this.fragment = fragment;
        controller = Robolectric.buildActivity(FragmentActivity.class);
        activity = controller.create().start().visible().get();
        FragmentManager manager = activity.getSupportFragmentManager();
        manager.beginTransaction()
                .add(fragment, FRAGMENT_TAG).commit();
    }

    @After
    public void destroyFragment() {
        if (fragment != null) {
            FragmentManager manager = activity.getSupportFragmentManager();
            manager.beginTransaction().remove(fragment).commit();
            fragment = null;
            activity = null;
        }
    }

    public void pauseAndResumeFragment() {
        controller.pause().resume();
    }

    public T recreateFragment() {
        activity.recreate();
        // Recreating the activity creates a new instance of the
        // fragment which we need to retrieve by tag.
        fragment = (T) activity.getSupportFragmentManager()
                .findFragmentByTag(FRAGMENT_TAG);
        return fragment;
    }
}
