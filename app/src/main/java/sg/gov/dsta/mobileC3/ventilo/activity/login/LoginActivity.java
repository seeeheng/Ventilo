package sg.gov.dsta.mobileC3.ventilo.activity.login;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.WaveRelayRadioViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ProgressBarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;
import sg.gov.dsta.mobileC3.ventilo.util.log.LoggerUtil;
import timber.log.Timber;

public class LoginActivity extends AppCompatActivity {
//        implements LoaderCallbacks<Cursor> {

    private static final String TAG = LoginActivity.class.getSimpleName();

    // View models
    private UserViewModel mUserViewModel;
    private WaveRelayRadioViewModel mWaveRelayRadioViewModel;

    // UI references
    private LinearLayout mMainLayout;
    private C2OpenSansSemiBoldEditTextView mEtvUserId;
    private C2OpenSansSemiBoldEditTextView mEtvPassword;

    private Spinner mSpinnerRadioNo;
    private ArrayAdapter mSpinnerRadioNoAdapter;
    private List<WaveRelayRadioModel> mWaveRelayRadioModelList;

    private ImageView mLoginBtn;

    // Snackbar
    private View mViewSnackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        resetSharedPref();
//        setUpDummyUser();
        observerSetup();
        initUI();
        initSnackbar();
    }

    private void initUI() {
        mMainLayout = findViewById(R.id.layout_login_activity);

        // Set up the login form.
        mEtvUserId = findViewById(R.id.etv_login_username);
        mEtvPassword = findViewById(R.id.etv_login_password);

        mSpinnerRadioNo = findViewById(R.id.spinner_login_radio_number);
        mSpinnerRadioNo.setOnItemSelectedListener(getRadioSpinnerItemSelectedListener);

        if (mWaveRelayRadioModelList != null) {
            mWaveRelayRadioModelList = new ArrayList<>();
        }

        mLoginBtn = findViewById(R.id.img_login_btn_login);
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mLoginBtn.setEnabled(false);
                attemptLogin();
            }
        });
    }

    private void initSnackbar() {
        mViewSnackbar = getLayoutInflater().inflate(R.layout.layout_custom_snackbar, null);
    }

    private AdapterView.OnItemSelectedListener getRadioSpinnerItemSelectedListener =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    parent.setSelected(true);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };

    private void resetSharedPref() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.apply();
    }

    private synchronized void populateAndSetRadioNoAdapterList() {
        ProgressBarUtil.createProgressDialog(this);

        SingleObserver<List<WaveRelayRadioModel>> singleObserverAllWaveRelayRadiosForUser =
                new SingleObserver<List<WaveRelayRadioModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<WaveRelayRadioModel> waveRelayRadioModelList) {



                        if (waveRelayRadioModelList != null) {

                            Timber.i("onSuccess singleObserverAllWaveRelayRadiosForUser, " +
                                    "populateRadioNoList. " +
                                    "waveRelayRadioModelList.size(): %d" , waveRelayRadioModelList.size());





                            mWaveRelayRadioModelList = waveRelayRadioModelList;

                            List<Integer> radioNoList = waveRelayRadioModelList.stream().map(
                                    WaveRelayRadioModel -> WaveRelayRadioModel.getRadioId()).
                                    collect(Collectors.toList());

                            ArrayList<String> radioNoStrList = new ArrayList<>();
                            for (int radioNo : radioNoList) {
                                radioNoStrList.add(String.valueOf(radioNo));
                            }

                            mSpinnerRadioNoAdapter = getSpinnerArrayAdapter(radioNoStrList);
                            mSpinnerRadioNo.setAdapter(mSpinnerRadioNoAdapter);

                            ProgressBarUtil.dismissProgressDialog();

                        } else {

                            Timber.i("onSuccess singleObserverAllWaveRelayRadiosForUser, " +
                                    "populateRadioNoList. " +
                                    "waveRelayRadioModelList is null");


                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        Timber.e("onError singleObserverAllWaveRelayRadiosForUser, " +
                                "populateRadioNoList. " +
                                "Error Msg: %s " , e.toString());

                    }
                };

        mWaveRelayRadioViewModel.getAllWaveRelayRadios(singleObserverAllWaveRelayRadiosForUser);
    }

    private ArrayAdapter<String> getSpinnerArrayAdapter(ArrayList<String> stringArrayList) {

        return new ArrayAdapter<String>(this,
                R.layout.spinner_row_item, R.id.tv_spinner_row_item_text, stringArrayList) {

            @Override
            public boolean isEnabled(int position) {
                return true;
            }

            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                LinearLayout layoutSpinner = view.findViewById(R.id.layout_spinner_text_item);
                layoutSpinner.setGravity(Gravity.END);
                layoutSpinner.setPadding(0, 0,
                        (int) getResources().getDimension(R.dimen.elements_large_margin_spacing), 0);

                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);

                // Set appropriate height for spinner items
                DimensionUtil.setDimensions(view,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        (int) getResources().getDimension(R.dimen.spinner_broad_height),
                        new LinearLayout(getContext()));

                LinearLayout layoutSpinner = view.findViewById(R.id.layout_spinner_text_item);
                layoutSpinner.setGravity(Gravity.CENTER);
                layoutSpinner.setPadding(0, 0,
                        (int) getResources().getDimension(R.dimen.elements_large_margin_spacing), 0);

                TextView tv = view.findViewById(R.id.tv_spinner_row_item_text);
                tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_white, null));

                return view;
            }
        };
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
     * Attempts to sign in. If there are form errors (invalid
     * username, missing fields, etc.), errors will be presented
     * and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mEtvUserId.setError(null);
        mEtvPassword.setError(null);

        // Store values at the time of the login attempt.
        String userId = mEtvUserId.getText().toString();
        String password = mEtvPassword.getText().toString();
        boolean isRadioSelected = mSpinnerRadioNo.isSelected();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid user Id.
        if (TextUtils.isEmpty(userId)) {
            mEtvUserId.setError(MainApplication.getAppContext().
                    getString(R.string.error_field_required));
            focusView = mEtvUserId;
            cancel = true;
        }

        // Check for a valid password
        if (TextUtils.isEmpty(password)) {
            mEtvPassword.setError(MainApplication.getAppContext().
                    getString(R.string.error_field_required));
            focusView = mEtvPassword;
            cancel = true;
        }

        // Check if radio number is selected
        if (!isRadioSelected) {
            focusView = mSpinnerRadioNo;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            mLoginBtn.setEnabled(true);
        } else {

            // TODO: Add after design review
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            checkIfValidUser();
        }
    }

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

    /**
     * Checks if radio number has already been used by any other devices
     *
     */
    private void checkIfValidRadioNo(UserModel userModel, int selectedRadioNo) {

        SingleObserver<WaveRelayRadioModel> singleObserverWaveRelayRadioByRadioNo =
                new SingleObserver<WaveRelayRadioModel>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(WaveRelayRadioModel waveRelayRadioModel) {
                        if (waveRelayRadioModel != null) {


                            Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo, checkIfValidRadioNo ,waveRelayRadioModel: %s" ,waveRelayRadioModel);


                            // No one is using selected radio number
                            if (waveRelayRadioModel.getUserId() == null ||
                                    StringUtil.EMPTY_STRING.equalsIgnoreCase(waveRelayRadioModel.getUserId())) {
                                saveLoginDetails(userModel);

                                Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);

                                // Transfer Radio Number to Main Activity
                                Bundle bundle = new Bundle();
                                bundle.putInt(MainNavigationConstants.WAVE_RELAY_RADIO_NO_KEY,
                                        waveRelayRadioModel.getRadioId());
                                activityIntent.putExtra(MainNavigationConstants.WAVE_RELAY_RADIO_NO_BUNDLE_KEY, bundle);

                                startActivity(activityIntent);
                            } else {
                                // Selected radio number is already being used by another device
                                Timber.i("Selected radio number is already being used by another device.");

                                SnackbarUtil.showCustomInfoSnackbar(mMainLayout, mViewSnackbar,
                                        getString(R.string.snackbar_login_selected_radio_no_is_used));
                                mLoginBtn.setEnabled(true);
                            }
                        } else {
                            Timber.i("onSuccess singleObserverWaveRelayRadioByRadioNo, " +
                                    "checkIfValidRadioNo. " +
                                    "waveRelayRadioModel is null");

                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        Timber.e("onError singleObserverWaveRelayRadioByRadioNo, " +
                                "checkIfValidRadioNo. " +
                                "Error Msg:  %s " , e.toString());


                    }
                };

        mWaveRelayRadioViewModel.queryRadioByRadioId(selectedRadioNo,
                singleObserverWaveRelayRadioByRadioNo);
    }

    private void checkIfValidUser() {
        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<UserModel> singleObserver = new SingleObserver<UserModel>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onSuccess(UserModel userModel) {

                String accessToken = userModel.getAccessToken();

                if (accessToken == null || StringUtil.EMPTY_STRING.equalsIgnoreCase(accessToken)) {

                    // Valid account to be logged in
                    if (mEtvPassword.getText().toString().trim().equalsIgnoreCase(userModel.getPassword())) {
                        int selectedRadioNo = Integer.valueOf(mSpinnerRadioNo.getSelectedItem().toString());
                        checkIfValidRadioNo(userModel, selectedRadioNo);
                    } else {
                        mEtvPassword.setError(MainApplication.getAppContext().
                                getString(R.string.error_incorrect_password));
                        mEtvPassword.requestFocus();
                        mLoginBtn.setEnabled(true);
                    }
                } else { // User account is already logged in from another device

                    Timber.i("User account is already logged in from another device.");

                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, mViewSnackbar,
                            getString(R.string.snackbar_login_user_already_logged_in));
                    mLoginBtn.setEnabled(true);
                }
            }

            @Override
            public void onError(Throwable e) {


                Timber.e("Error adding Access Token. Error Msg: %s" ,e.toString());
                SnackbarUtil.showCustomInfoSnackbar(mMainLayout, mViewSnackbar,
                        getString(R.string.snackbar_login_invalid_user));
                mLoginBtn.setEnabled(true);
            }
        };

        mUserViewModel.queryUserByUserId(mEtvUserId.getText().toString().trim(), singleObserver);
    }

//    private boolean executePingCommand(String ipAddr) {
//        System.out.println("executeCommand");
//        Runtime runtime = Runtime.getRuntime();
//        try {
//            Process mIpAddrProcess = runtime.exec("/system/bin/ping -c 1 " + ipAddr);
//            int mExitValue = mIpAddrProcess.waitFor();
//            System.out.println(" mExitValue " + mExitValue);
//            if (mExitValue == 0) {
//                return true;
//            } else {
//                return false;
//            }
//        } catch (InterruptedException ignore) {
//            ignore.printStackTrace();
//            System.out.println(" Exception:" + ignore);
//        } catch (IOException e) {
//            e.printStackTrace();
//            System.out.println(" Exception:" + e);
//        }
//        return false;
//    }

    /**
     * Store user information with generated login access token upon successful login
     *
     * @param userModel
     */
    private void saveLoginDetails(UserModel userModel) {
        String accessToken = StringUtil.generateRandomString();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor prefsEditor = sharedPrefs.edit();
        prefsEditor.putString(SharedPreferenceConstants.USER_ID, userModel.getUserId());
        prefsEditor.putString(SharedPreferenceConstants.USER_TEAM, userModel.getTeam());
        prefsEditor.putString(SharedPreferenceConstants.ACCESS_TOKEN, accessToken);

        String accessRight;
        if (EAccessRight.CCT.toString().equalsIgnoreCase(userModel.getRole())) {
            accessRight = EAccessRight.CCT.toString();
        } else {
            accessRight = EAccessRight.TEAM_LEAD.toString();
        }

        prefsEditor.putString(SharedPreferenceConstants.ACCESS_RIGHT, accessRight);
        prefsEditor.apply();


        Timber.i("Added Access Token to User: %s", userModel.getUserId());

        Timber.i("accessToken is  %s",  accessToken);




        userModel.setAccessToken(accessToken);
        mUserViewModel.updateUser(userModel);
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mWaveRelayRadioViewModel = ViewModelProviders.of(this).get(WaveRelayRadioViewModel.class);
    }

    @Override
    public void onStart() {
        super.onStart();

        Timber.i("onStart.");

        populateAndSetRadioNoAdapterList();

        if (mLoginBtn != null) {
            mLoginBtn.setEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        Timber.i("Destroying Login Activity...");
        resetSharedPref();

//        /* Close service properly. Currently, the service is not destroyed, only the mqtt connection and
//         * connection status are closed.
//         */
//        synchronized (MainActivity.class) {
//            if (RabbitMQAsyncTask.mIsServiceRegistered) {
//                Log.i(TAG, "Destroying RabbitMQ...");
//
//                RabbitMQAsyncTask.stopRabbitMQ();
//                Log.i(TAG, "Stopped RabbitMQ Async Task.");
//
//                stopService(MainApplication.mRabbitMQIntent);
//                Log.i(TAG, "Stopped RabbitMQ Intent Service.");
//
//                MainApplication.networkService.stopSelf();
//                Log.i(TAG, "Stop RabbitMQ Network Self.");
//
//                getApplicationContext().unbindService(MainApplication.rabbitMQServiceConnection);
//                Log.i(TAG, "Unbinded RabbitMQ Service.");
//
//                JeroMQPublisher.getInstance().stop();
//                JeroMQSubscriber.getInstance().stop();
//
//                Log.i(TAG, "Stopped all JeroMQ connections.");
//                RabbitMQAsyncTask.mIsServiceRegistered = false;
//
//                JeroMQPubSubBrokerProxy.getInstance().stop();
//            }
//        }
    } // Stopping service sg.gov.dsta.mobileC3.ventilo/.network.NetworkService: remove task
}