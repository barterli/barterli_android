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

package li.barter.fragments;

import android.support.v4.app.FragmentTransaction;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Vinay S Shenoy Annotation to specify Fragment transitions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface FragmentTransition {

    /**
     * @return The animation resource ID to be applied to a fragment that is
     *         just appearing
     */
    int enterAnimation() default FragmentTransaction.TRANSIT_FRAGMENT_OPEN;

    /**
     * @return The animation resource ID to be applied to a fragment that is
     *         exiting
     */
    int exitAnimation() default FragmentTransaction.TRANSIT_FRAGMENT_CLOSE;

    /**
     * @return The animation resource ID to be applied to a fragment that is
     *         being popped from the backstack
     */
    int popEnterAnimation() default FragmentTransaction.TRANSIT_FRAGMENT_OPEN;

    /**
     * @return The animation resource ID to be applied to a fragment that is
     *         being popped from the backstack
     */
    int popExitAnimation() default FragmentTransaction.TRANSIT_FRAGMENT_CLOSE;
}
