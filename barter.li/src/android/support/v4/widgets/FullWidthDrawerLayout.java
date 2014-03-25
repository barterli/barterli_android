/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.v4.widgets;

import android.content.Context;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;

/**
 * DrawerLayout acts as a top-level container for window content that allows for
 * interactive "drawer" views to be pulled out from the edge of the window.
 * <p>
 * Drawer positioning and layout is controlled using the
 * <code>android:layout_gravity</code> attribute on child views corresponding to
 * which side of the view you want the drawer to emerge from: left or right. (Or
 * start/end on platform versions that support layout direction.)
 * </p>
 * <p>
 * To use a DrawerLayout, position your primary content view as the first child
 * with a width and height of <code>match_parent</code>. Add drawers as child
 * views after the main content view and set the <code>layout_gravity</code>
 * appropriately. Drawers commonly use <code>match_parent</code> for height with
 * a fixed width.
 * </p>
 * <p>
 * {@link DrawerListener} can be used to monitor the state and motion of drawer
 * views. Avoid performing expensive operations such as layout during animation
 * as it can cause stuttering; try to perform expensive operations during the
 * {@link #STATE_IDLE} state. {@link SimpleDrawerListener} offers default/no-op
 * implementations of each callback method.
 * </p>
 * <p>
 * As per the Android Design guide, any drawers positioned to the left/start
 * should always contain content for navigating around the application, whereas
 * any drawers positioned to the right/end should always contain actions to take
 * on the current content. This preserves the same navigation left, actions
 * right structure present in the Action Bar and elsewhere.
 * </p>
 * Note: Copied from Android Support Library source and modified to prevent
 * default margin being added
 */
public class FullWidthDrawerLayout extends DrawerLayout {

    public FullWidthDrawerLayout(final Context context) {
        super(context);
    }

    public FullWidthDrawerLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public FullWidthDrawerLayout(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec,
                    final int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        if ((widthMode != MeasureSpec.EXACTLY)
                        || (heightMode != MeasureSpec.EXACTLY)) {
            throw new IllegalArgumentException("DrawerLayout must be measured with MeasureSpec.EXACTLY.");
        }

        setMeasuredDimension(widthSize, heightSize);

        // Gravity value for each drawer we've seen. Only one of each permitted.
        final int foundDrawers = 0;
        final int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            final LayoutParams lp = (LayoutParams) child.getLayoutParams();

            if (isChildAContentView(child)) {
                // Content views get measured at exactly the layout's size.
                final int contentWidthSpec = MeasureSpec
                                .makeMeasureSpec(widthSize - lp.leftMargin
                                                - lp.rightMargin, MeasureSpec.EXACTLY);
                final int contentHeightSpec = MeasureSpec
                                .makeMeasureSpec(heightSize - lp.topMargin
                                                - lp.bottomMargin, MeasureSpec.EXACTLY);
                child.measure(contentWidthSpec, contentHeightSpec);
            } else if (isChildADrawerView(child)) {
                final int childGravity = getDrawerViewGravity(child)
                                & Gravity.HORIZONTAL_GRAVITY_MASK;
                if ((foundDrawers & childGravity) != 0) {
                    throw new IllegalStateException("Child drawer has absolute gravity "
                                    + gravityToString(childGravity)
                                    + " but this already has a "
                                    + "drawer view along that edge");
                }
                final int drawerWidthSpec = getChildMeasureSpec(widthMeasureSpec, lp.leftMargin
                                + lp.rightMargin, lp.width);
                final int drawerHeightSpec = getChildMeasureSpec(heightMeasureSpec, lp.topMargin
                                + lp.bottomMargin, lp.height);
                child.measure(drawerWidthSpec, drawerHeightSpec);
            } else {
                throw new IllegalStateException("Child "
                                + child
                                + " at index "
                                + i
                                + " does not have a valid layout_gravity - must be Gravity.LEFT, "
                                + "Gravity.RIGHT or Gravity.NO_GRAVITY");
            }
        }
    }

    boolean isChildAContentView(final View child) {
        return ((LayoutParams) child.getLayoutParams()).gravity == Gravity.NO_GRAVITY;
    }

    boolean isChildADrawerView(final View child) {
        final int gravity = ((LayoutParams) child.getLayoutParams()).gravity;
        final int absGravity = Gravity
                        .getAbsoluteGravity(gravity, GravityCompat.getAbsoluteGravity(gravity, ViewCompat
                                        .getLayoutDirection(child)));
        return (absGravity & (Gravity.LEFT | Gravity.RIGHT)) != 0;
    }

    int getDrawerViewGravity(final View drawerView) {
        final int gravity = ((LayoutParams) drawerView.getLayoutParams()).gravity;
        return Gravity.getAbsoluteGravity(gravity, GravityCompat
                        .getAbsoluteGravity(gravity, ViewCompat
                                        .getLayoutDirection(drawerView)));
    }

    static String gravityToString(final int gravity) {
        if ((gravity & Gravity.LEFT) == Gravity.LEFT) {
            return "LEFT";
        }
        if ((gravity & Gravity.RIGHT) == Gravity.RIGHT) {
            return "RIGHT";
        }
        return Integer.toHexString(gravity);
    }
}
