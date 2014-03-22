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

package li.barter.activities;

import com.facebook.Session;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import li.barter.R;
import li.barter.R.anim;
import li.barter.R.id;
import li.barter.R.layout;

@ActivityTransition(createEnterAnimation = R.anim.activity_slide_in_right, createExitAnimation = R.anim.activity_scale_out, destroyEnterAnimation = R.anim.activity_scale_in, destroyExitAnimation = R.anim.activity_slide_out_right)
public class LoginActivity extends AbstractBarterLiActivity implements
                OnClickListener {
    
    private static final String TAG = "LoginActivity";

    private Button   mFacebookLoginButton;
    private Button   mGoogleLoginButton;
    private Button   mSubmitButton;
    private EditText mEmailEditText;
    private EditText mPasswordEditText;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mFacebookLoginButton = (Button) findViewById(R.id.button_facebook_login);
        mGoogleLoginButton = (Button) findViewById(R.id.button_google_login);
        mSubmitButton = (Button) findViewById(R.id.button_submit);
        mEmailEditText = (EditText) findViewById(R.id.edit_text_email);
        mPasswordEditText = (EditText) findViewById(R.id.edit_text_password);

        mFacebookLoginButton.setOnClickListener(this);
        mGoogleLoginButton.setOnClickListener(this);
        mSubmitButton.setOnClickListener(this);

    }

    @Override
    protected Object getVolleyTag() {
        return TAG;
    }
    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
                    final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode,
                        resultCode, data);
    }

    @Override
    public void onClick(final View v) {

        switch (v.getId()) {

            case R.id.button_facebook_login: {
                // TODO Facebook Login
                break;
            }

            case R.id.button_google_login: {
                // TODO Google Login
                break;
            }

            case R.id.button_submit: {
                // TODO User Login/Create
                break;
            }
        }
    }

}
