/*
 * Copyright (C) 2014 barter.li
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package li.barter.fragments.dialogs;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import li.barter.R;
import li.barter.utils.Logger;

/**
 * Alert Dialog to display a custom view to Enable Location if not enabled
 * 
 * @author Anshul Kamboj
 */
public class EnableLocationDialogFragment extends DialogFragment {

    private static final String TAG = "EnableLocationDialogFragment";

    /** Res Id for the Dialog Title. */
    private int                 mTitleId;

    /** Res Id for the Positive button label. */
    private int                 mPositiveLabelId;
    
    /** Res Id for the Dialog message. */
    private int                 mMessageId;

    /** Res Id for the negative Button label. */
    private int                 mNegativeLabelId;

    /** Res Id for the neutral button label. */
    private int                 mNeutralLabelId;

    /** Click Listener for the Dialog buttons. */
    private OnClickListener     mClickListener;

    /** On Dismiss Listener for the Dialog */
    private OnDismissListener   mOnDismissListener;

    /** Resource Id for the icon to be be used in the alert dialog. */
    private int                 mIconId;

    /** Boolean flag to identify if the dialog is cancelable. */
    private boolean             isCancellable;

    /** Resource Id for the theme to be used for the alert dialog. */
    private int                 mTheme;

    /** Message parameters(if any) for formatting the Dialog message */
    private String[]            mMessageParams;

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mTitleId = savedInstanceState.getInt(DialogKeys.TITLE_ID);
            
            mMessageId = savedInstanceState.getInt(DialogKeys.MESSAGE_ID);
            mNegativeLabelId = savedInstanceState
                            .getInt(DialogKeys.NEGATIVE_LABEL_ID);
            mNeutralLabelId = savedInstanceState
                            .getInt(DialogKeys.NEUTRAL_LABEL_ID);
            mPositiveLabelId = savedInstanceState
                            .getInt(DialogKeys.POSITIVE_LABEL_ID);
            isCancellable = savedInstanceState
                            .getBoolean(DialogKeys.CANCELLABLE);
            mIconId = savedInstanceState.getInt(DialogKeys.ICON_ID);
            mTheme = savedInstanceState.getInt(DialogKeys.THEME);
            mMessageParams = savedInstanceState
                    .getStringArray(DialogKeys.MESSAGE_PARAMS);
        }

        final Builder builder = new Builder(getActivity(), mTheme);

        if ((mTitleId == 0) && (mMessageId == 0)) {
            throw new IllegalArgumentException("No Title and no message");
        }
        
        if (mMessageId != 0) {

            if ((mMessageParams != null) && (mMessageParams.length > 0)) {
                builder.setMessage(getActivity()
                                .getString(mMessageId, (Object[]) mMessageParams));
            } else {
                builder.setMessage(mMessageId);
            }
        }

      

        if (mIconId != 0) {
            builder.setIcon(mIconId);
        }
        if (mTitleId != 0) {
            builder.setTitle(mTitleId);
        }

        if (mPositiveLabelId != 0) {
            builder.setPositiveButton(mPositiveLabelId, mClickListener);
        }

        if (mNegativeLabelId != 0) {
            builder.setNegativeButton(mNegativeLabelId, mClickListener);
        }

        if (mNeutralLabelId != 0) {
            builder.setNeutralButton(mNeutralLabelId, mClickListener);
        }

        builder.setCancelable(isCancellable);
        setCancelable(isCancellable);
        return builder.create();
    }

    @Override
    public void onAttach(final Activity activity) {

        super.onAttach(activity);

        if (activity instanceof OnClickListener) {
            mClickListener = (OnClickListener) activity;
        }

        else {
            throw new IllegalStateException("Activity must implement DialogInterface.OnClickListener");
        }

        if (activity instanceof OnDismissListener) {
            mOnDismissListener = (OnDismissListener) activity;
        }
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mOnDismissListener != null) {
            mOnDismissListener.onDismiss(dialog);
        }
    }

    @Override
    public void onSaveInstanceState(final Bundle outState) {

        outState.putInt(DialogKeys.TITLE_ID, mTitleId);
        outState.putInt(DialogKeys.NEGATIVE_LABEL_ID, mNegativeLabelId);
        outState.putInt(DialogKeys.POSITIVE_LABEL_ID, mPositiveLabelId);
        outState.putInt(DialogKeys.NEUTRAL_LABEL_ID, mNeutralLabelId);
        outState.putBoolean(DialogKeys.CANCELLABLE, isCancellable);
        outState.putInt(DialogKeys.ICON_ID, mIconId);
        outState.putInt(DialogKeys.THEME, mTheme);
        outState.putStringArray(DialogKeys.MESSAGE_PARAMS, mMessageParams);
        outState.putInt(DialogKeys.MESSAGE_ID, mMessageId);
        super.onSaveInstanceState(outState);
    }

    public void show(final int theme, final int iconId, final int titleId,
            final int messageId, final int positiveLabelId,
            final int negativeLabelId, final int neutralLabelId,
            final FragmentManager manager, final boolean cancellable,
            final String fragmentTag, final String... messageParams) {

        mTheme = theme;
        mIconId = iconId;
        mTitleId = titleId;
        mPositiveLabelId = positiveLabelId;
        mNegativeLabelId = negativeLabelId;
        mNeutralLabelId = neutralLabelId;
        isCancellable = cancellable;
        mMessageParams = messageParams;
        mMessageId = messageId;

        try {
            super.show(manager, fragmentTag);
        } catch (final IllegalStateException e) {
            Logger.e(TAG, e, "Exception");
        }

    }


  

}
