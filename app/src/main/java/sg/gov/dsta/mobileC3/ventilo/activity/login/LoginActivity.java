package sg.gov.dsta.mobileC3.ventilo.activity.login;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.database.VentiloDatabase;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.MainViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBlackEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.security.RandomString;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
//import sg.com.superc2.utils.GsonCreator;
//import sg.com.superc2.utils.constants.RestConstants;
//import sg.com.superc2.utils.rest.QueueSingleton;
//import sg.com.superc2.utils.rest.VolleyPostBuilder;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
//        implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;
    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final String USERNAME = "123";
    private static final String USERNAME_TWO = "456";
    private UserViewModel mUserViewModel;

    // UI references
    private C2OpenSansBlackEditTextView mEtvUserId;
    private C2OpenSansBlackEditTextView mEtvPassword;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: remove after demo
        resetSharedPref();
        setUpDummyUser();
        observerSetup();

//        Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
//        startActivity(activityIntent);

        setContentView(R.layout.activity_login);
        // Set up the login form.
        mEtvUserId = (C2OpenSansBlackEditTextView) findViewById(R.id.etv_login_username);
//        populateAutoComplete();

        mEtvPassword = (C2OpenSansBlackEditTextView) findViewById(R.id.etv_login_password);
        mEtvPassword.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
//                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        ImageView mLoginBtn = (ImageView) findViewById(R.id.img_btn_sign_in);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                attemptLogin();

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplication());
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(SharedPreferenceConstants.CALLSIGN_USER, mEtvUserId.getText().toString().trim());
                editor.apply();

                SharedPreferenceConstants.INITIALS = SharedPreferenceConstants.TEAM_NUMBER.
                        concat(SharedPreferenceConstants.SEPARATOR).concat(SharedPreferenceConstants.CALLSIGN_USER);

                // TODO: Remove after demo
                SharedPreferenceUtil.setContext(getApplication());

                checkIfValidUser();
            }
        });

//        mForgotPasswordTV = (C2LatoBlackTextView) findViewById(R.id.tv_login_footnote);
//        mForgotPasswordTV.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v){
//                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(intent);
//            }
//        });

        mLoginFormView = findViewById(R.id.inner_login_form);
        mProgressView = findViewById(R.id.login_progress);

        // TODO: Remove after testing
//        mPasswordET.setText("Bella@com.sg");
//        mPasswordET.setText("pass");
    }

    private void resetSharedPref() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        registerReceiver(receiver, new IntentFilter(BroadcastReceiverType.LOGIN_RECEIVER));
    }

    @Override
    protected void onPause() {
        super.onPause();
//        unregisterReceiver(receiver);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made. U
     */
    private void attemptLogin() {

        // Reset errors.
        mEtvUserId.setError(null);
        mEtvPassword.setError(null);

        // Store values at the time of the login attempt.
        String userId = mEtvUserId.getText().toString();
        String password = mEtvPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password)) {
            mEtvPassword.setError(getString(R.string.error_field_required));
            focusView = mEtvPassword;
            cancel = true;
        }

        // Check for a valid user Id.
        if (TextUtils.isEmpty(userId)) {
            mEtvUserId.setError(getString(R.string.error_field_required));
            focusView = mEtvUserId;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            // TODO: Add after design review
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            checkIfValidUser();
//            LoginTx loginTx = new LoginTx(mEtvUserId.getText().toString()
//                    , mPasswordET.getText().toString());
//            StringRequest stringRequest = VolleyPostBuilder.getRequest(RestConstants.BASE_URL + RestConstants.POST_LOGIN, loginTx, loginListener, errorListener, getApplicationContext());
//            QueueSingleton.getInstance(this).addToRequestQueue(stringRequest);
//            showProgress(true);
        }
    }

//    private void storeAccessToken(String userId) {
//        RandomString randomString = new RandomString();
//        String accessToken = randomString.nextString();
//        saveLoginDetails(userId, accessToken);
//    }

    // TODO: Add after design review
//    Response.Listener<String> loginListener = new Response.Listener<String>() {
//        @Override
//        public void onResponse(String response) {
//            Gson gson = GsonCreator.createGson();
//            AuthRx authRx = gson.fromJson(response, AuthRx.class);
//            saveLoginDetails(authRx.getAccessToken());
//            Log.d("SuperC2", "Response: " + response);
//            Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
//            startActivity(activityIntent);
//        }
//    };

//    Response.ErrorListener errorListener = new Response.ErrorListener() {
//        @Override
//        public void onErrorResponse(VolleyError error) {
//            showProgress(false);
//            Toast.makeText(getApplicationContext(), "Incorrect username or password. Please try again.",
//                    Toast.LENGTH_LONG).show();
//        }
//    };

//    private boolean isUsernameValid(String username) {
////        return email.contains("@");
//        return username.length() >= 0;
//    }
//
//    private boolean isPasswordValid(String password) {
////        return password.length() >= 4;
//        return password.length() >= 0;
//    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

//    @Override
//    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        return new CursorLoader(this,
//                // Retrieve data rows for the device user's 'profile' contact.
//                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
//                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,
//
//                // Select only email addresses.
//                ContactsContract.Contacts.Data.MIMETYPE +
//                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
//                .CONTENT_ITEM_TYPE},
//
//                // Show primary email addresses first. Note that there won't be
//                // a primary email address if the user hasn't specified one.
//                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
//    }

//    @Override
//    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
//        List<String> emails = new ArrayList<>();
//        cursor.moveToFirst();
//        while (!cursor.isAfterLast()) {
//            emails.add(cursor.getString(ProfileQuery.ADDRESS));
//            cursor.moveToNext();
//        }
//
//        addEmailsToAutoComplete(emails);
//    }

//    @Override
//    public void onLoaderReset(Loader<Cursor> cursorLoader) {
//
//    }

//    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
//        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
//        ArrayAdapter<String> adapter =
//                new ArrayAdapter<>(LoginActivity.this,
//                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);
//
//        mEmailView.setAdapter(adapter);
//    }

//    private interface ProfileQuery {
//        String[] PROJECTION = {
//                ContactsContract.CommonDataKinds.Email.ADDRESS,
//                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
//        };
//
//        int ADDRESS = 0;
//        int IS_PRIMARY = 1;
//    }

    private void setUpDummyUser() {
        // TODO: Remove for actual deployment
        // Added for testing; Clears all data from database
        MainViewModel mainViewModel = new MainViewModel(getApplication());
        mainViewModel.clearAllData();

        UserViewModel userViewModel = new UserViewModel(getApplication());
        String password = "333";
        UserModel newUser = new UserModel(USERNAME, password, "", "Alpha", "Member");

        String passwordTwo = "444";
        UserModel newUserTwo = new UserModel(USERNAME_TWO, passwordTwo, "", "Alpha", "Member");

        userViewModel.addUser(newUser);
        userViewModel.addUser(newUserTwo);
    }

    private void checkIfValidUser() {
        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<UserModel> singleObserver = new SingleObserver<UserModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(UserModel userModel) {
                saveLoginDetails(userModel);

                //TODO figure out a way to toggle when server is not available
                Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(activityIntent);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "Error adding Access Token. Error Msg: " + e.toString());
            }
        };

//        mUserViewModel.queryUserByUserId(mEtvUserId.getText().toString().trim(), singleObserver);
        mUserViewModel.queryUserByUserId(USERNAME, singleObserver);
    }

    private void saveLoginDetails(UserModel userModel) {
        String accessToken = generateAccessToken();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putString(SharedPreferenceConstants.USER_ID, userModel.getUserId());
//        prefsEditor.putString(General.USER_HANDLE, userHandle);
//        prefsEditor.putString(General.USER_RANK, userRank);
//        prefsEditor.putString(General.REFRESH_TOKEN, refreshToken);
        prefsEditor.putString(SharedPreferenceConstants.ACCESS_TOKEN, accessToken);
        prefsEditor.apply();

        Log.d(TAG, "Added Access Token to User (" + userModel.getUserId() + ")");
        userModel.setAccessToken(accessToken);
        mUserViewModel.updateUser(userModel);
    }

    private String generateAccessToken() {
        RandomString randomString = new RandomString();
        return randomString.nextString();
    }

    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);

//        mUserViewModel.getUserByAccessTokenOrUserId().observe(this, new Observer<UserModel>() {
//            @Override
//            public void onChanged(@Nullable UserModel userModel) {
//
//            }
//        });
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
//    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
//
//        private final String mEmail;
//        private final String mPassword;
//
//        UserLoginTask(String email, String password) {
//            mEmail = email;
//            mPassword = password;
//        }
//
//        @Override
//        protected Boolean doInBackground(Void... params) {
//            // TODO: attempt authentication against a network service.
//
//            try {
//                // Simulate network access.
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                return false;
//            }
//
//            for (String credential : DUMMY_CREDENTIALS) {
//                String[] pieces = credential.split(":");
//                if (pieces[0].equals(mEmail)) {
//                    // Account exists, return true if the password matches.
//                    return pieces[1].equals(mPassword);
//                }
//            }
//
//            // TODO: register the new account here.
//            return true;
//        }
//
//        @Override
//        protected void onPostExecute(final Boolean success) {
//            mAuthTask = null;
//            showProgress(false);
//
//            if (success) {
//                finish();
//            } else {
//                mPasswordView.setError(getString(R.string.error_incorrect_password));
//                mPasswordView.requestFocus();
//            }
//        }
//
//        @Override
//        protected void onCancelled() {
//            mAuthTask = null;
//            showProgress(false);
//        }
//    }
}