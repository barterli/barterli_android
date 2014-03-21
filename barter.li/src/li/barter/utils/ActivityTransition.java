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
package li.barter.utils;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author vinaysshenoy
 *         Annotation to specify Activity transitions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface ActivityTransition {

    /**
     * @return The animation resource ID to be applied to a newly created
     *         Activity that is just appearing
     */
    int createEnterAnimation() default 0;

    /**
     * @return The animation resource ID to be applied to the current Activity
     *         that is going to the background after a new Activity has been
     *         created
     */
    int createExitAnimation() default 0;

    /**
     * @return The animation resource ID to be applied to an Activity that is
     *         coming into the foreground after the current Activity has been
     *         destroyed
     */
    int destroyEnterAnimation() default 0;

    /**
     * @return The animation resource ID to be applied to the curent Activity
     *         that is getting destroyed
     */
    int destroyExitAnimation() default 0;
}
