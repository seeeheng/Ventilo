package sg.gov.dsta.mobileC3.ventilo.activity.user;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
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
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQClientPairRunnable;
import sg.gov.dsta.mobileC3.ventilo.repository.ExcelSpreadsheetRepository;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;
import timber.log.Timber;

public class UserSettingsFragment extends Fragment implements SnackbarUtil.SnackbarActionClickListener {

    private static final String TAG = UserSettingsFragment.class.getSimpleName();
    public static final int SNACKBAR_PULL_FROM_EXCEL_ID = 0;
    public static final int SNACKBAR_PUSH_TO_EXCEL_ID = 1;
    public static final int SNACKBAR_LOGOUT_ID = 2;

    // Intent Filters
    public static final String EXCEL_DATA_PULLED_INTENT_ACTION = "Excel Data Pulled Successfully";
    public static final String EXCEL_DATA_PULL_FAILED_INTENT_ACTION = "Excel Data Pulled Failed";
    public static final String EXCEL_DATA_SHIP_CONFIG_PULLED_INTENT_ACTION = "Ship Config Excel Data Pulled Successfully";
    public static final String EXCEL_DATA_SHIP_CONFIG_PULL_FAILED_INTENT_ACTION = "Ship Config Excel Data Pulled Failed";
    public static final String EXCEL_DATA_PUSHED_INTENT_ACTION = "Excel Data Pushed Successfully";
    public static final String EXCEL_DATA_PUSH_FAILED_INTENT_ACTION = "Excel Data Pushed Failed";
    public static final String DATA_SYNCED_INTENT_ACTION = "Data Synced Successfully.";
    public static final String LOGGED_OUT_INTENT_ACTION = "Logged Out Successfully";

    // View Models
    private UserViewModel mUserViewModel;
    private WaveRelayRadioViewModel mWaveRelayRadioViewModel;

    // Main
    private View mRootView;
    private View mMainLayout;

    // Current user callsign / team profile section
    private LinearLayout mLinearLayoutTeamProfileSelection;
    private UserSettingsCallsignTeamProfileRecyclerAdapter mRecyclerAdapter;
    private AppCompatImageView mImgEditSaveIcon;

    // Image buttons
    private View mImgLayoutPullFromExcel;
    private View mImgLayoutPushToExcel;

    // Sync with Callsign overlay
    private FrameLayout mFrameLayoutTransparentBg;
    private View mViewSyncWithCallsign;
    private C2OpenSansSemiBoldTextView mTvSyncWithCallsignNoOne;
    private UserSettingsSyncWithCallsignRecyclerAdapter mRecyclerAdapterSyncWithCallsign;
    private List<UserModel> mSyncWithCallsignUserModelList;
    private String mSelectedUserIdForSync;

    /**
     * Snackbar Options:
     * 0 - Save Settings,
     * 1 - Pull from Excel,
     * 2 - Push to Excel,
     * 3 - Logout
     */
    private int mSnackbarOption;
    private UserModel mCurrentUserModel;

    private BroadcastReceiver mDataSyncBroadcastReceiver;
    private boolean mIsFragmentVisibleToUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        observerSetup();

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_user_settings, container, false);
            initUI(inflater, mRootView);
        }

        return mRootView;
    }

    private void initUI(LayoutInflater inflater, View rootView) {
        mMainLayout = rootView.findViewById(R.id.layout_user_settings);

        initAllCallsignTeamProfileUI(rootView);
        initCurrentUserCallsignTeamProfileUI(inflater, rootView);
        initImageButtons(rootView);
    }

    private void initAllCallsignTeamProfileUI(View rootView) {
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_user_settings_callsign_team_profile);

        recyclerView.setHasFixedSize(false);

        RecyclerView.LayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerLayoutManager);

        List<UserModel> userListItems = new ArrayList<>();

        mRecyclerAdapter = new UserSettingsCallsignTeamProfileRecyclerAdapter(
                getContext(), userListItems);
        recyclerView.setAdapter(mRecyclerAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Initialise available Teams for current user to select from
     *
     * @param inflater
     * @param rootView
     */
    private void initCurrentUserCallsignTeamProfileUI(LayoutInflater inflater, View rootView) {
        C2OpenSansRegularTextView tvCurrentUserCallsign = rootView.findViewById(R.id.tv_user_settings_current_user_callsign);
        tvCurrentUserCallsign.setText(SharedPreferenceUtil.getCurrentUserCallsignID());

        mLinearLayoutTeamProfileSelection = rootView.findViewById(R.id.layout_user_settings_team_profile_selection);

        LinearLayout linearLayoutEditSaveIcon = rootView.findViewById(R.id.layout_user_settings_edit_save);
        linearLayoutEditSaveIcon.setOnClickListener(onEditOrSaveTeamProfileClickListener);

        mImgEditSaveIcon = rootView.findViewById(R.id.img_user_settings_team_profile_edit_save);

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<List<UserModel>> singleObserverForAllUsers = new SingleObserver<List<UserModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<UserModel> userModelList) {

                Timber.i("onSuccess singleObserverForAllUsers, initCurrentUserCallsignTeamProfileUI. size of userModelList:  %d", userModelList.size());


                // Get a list of a list of team profile names (by removing commas and spaces from
                // each UserModel.getTeam())
                // For e.g., original list from UserModel.getTeam()
                // String item 1 -> Alpha
                // String item 2 -> Alpha, Bravo
                // will be streamed to:
                // List<String> item 1 -> (Sub-item 1) Alpha
                // List<String> item 2 -> (Sub-item 1) Alpha | (Sub-item 2) Bravo
                // and finally streamed into List<List<String>>
                List<List<String>> listOfListOfTeamsProfileNames = userModelList.stream().map(
                        UserModel -> Arrays.asList(StringUtil.removeCommasAndExtraSpaces(UserModel.getTeam()))).
                        collect(Collectors.toList());

                Timber.i("List of list of team profile names: %s", listOfListOfTeamsProfileNames);


                // Converts the above list of list into a flat/single hierachy list
                // and extract ONLY distinct values
                // Above example will stream to flat hierachical list of:
                // List<String> item 1 -> Alpha
                // List<String> item 2 -> Alpha
                // List<String> item 3 -> Bravo
                // And then to distinct value of:
                // List<String> item 1 -> Alpha
                // List<String> item 2 -> Bravo
                List<Object> flatListOfDistinctTeamsProfileNames =
                        listOfListOfTeamsProfileNames.stream()
                                .flatMap(list -> list.stream()).distinct()
                                .collect(Collectors.toList());

                Timber.i("List of distinct team profile names: %s",
                        flatListOfDistinctTeamsProfileNames);


                // Get user's list of teams which he belongs to
                List<UserModel> currentUserModelList = userModelList.stream().filter(
                        UserModel -> UserModel.getUserId().equalsIgnoreCase(
                                SharedPreferenceUtil.getCurrentUserCallsignID())).
                        collect(Collectors.toList());

                List<String> currentUserTeamProfileNameList = new ArrayList<>();

                for (int j = 0; j < currentUserModelList.size(); j++) {
                    mCurrentUserModel = currentUserModelList.get(j);
                    currentUserTeamProfileNameList.addAll(Arrays.asList(StringUtil.
                            removeCommasAndExtraSpaces(mCurrentUserModel.getTeam())));
                }

                Timber.i("List of team profile names which current user belong to: %s",
                        currentUserTeamProfileNameList);


                // Create button layout for each available Team user
                for (int i = 0; i < flatListOfDistinctTeamsProfileNames.size(); i++) {
                    View viewBtnTeamProfileName = inflater.inflate(
                            R.layout.layout_img_text_fixed_dimension_img_btn, null);

                    C2OpenSansSemiBoldTextView tvTeamProfileName = viewBtnTeamProfileName.
                            findViewById(R.id.tv_img_text_fixed_dimension_text_img_btn);
                    AppCompatImageView imgTeamProfileName = viewBtnTeamProfileName.
                            findViewById(R.id.img_img_text_fixed_dimension_pic_img_btn);

                    // Set Team Name
                    StringBuilder teamProfileName = new StringBuilder();
                    teamProfileName.append(MainApplication.getAppContext().getString(R.string.team_header));
                    teamProfileName.append(StringUtil.SPACE);
                    teamProfileName.append(flatListOfDistinctTeamsProfileNames.get(i).toString().trim());
                    tvTeamProfileName.setText(teamProfileName.toString());

                    if (getContext() != null) {
                        // Set fixed height for newly created button
                        DimensionUtil.setDimensions(viewBtnTeamProfileName,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                (int) getResources().getDimension(R.dimen.img_btn_fixed_height),
                                new LinearLayout(getContext()));

                        // Set margin at the end of each button
                        DimensionUtil.setMargins(viewBtnTeamProfileName, 0, 0,
                                (int) getResources().getDimension(R.dimen.elements_margin_spacing), 0);
                    }

                    String currentTeamNameToCompare = flatListOfDistinctTeamsProfileNames.get(i).toString();

                    // Compare user's list of team (names) to each button's team name
                    boolean isUserTeamMatched = currentUserTeamProfileNameList.stream().anyMatch(
                            teamName -> teamName.equalsIgnoreCase(currentTeamNameToCompare));

                    // Set default uneditable UI state; Additionally, set selected state if
                    // user belongs to corresponding team profile name
                    if (isUserTeamMatched) {
                        viewBtnTeamProfileName.setSelected(true);
                        setTeamProfileNameSelectedUneditableUI(viewBtnTeamProfileName,
                                tvTeamProfileName, imgTeamProfileName);
                    } else {
                        viewBtnTeamProfileName.setSelected(false);
                        setTeamProfileNameUnselectedUneditableUI(viewBtnTeamProfileName,
                                tvTeamProfileName, imgTeamProfileName);
                    }

                    // Set default view disabled
                    viewBtnTeamProfileName.setEnabled(false);

                    // Set highlight on button upon click
                    viewBtnTeamProfileName.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            view.setSelected(!view.isSelected());
                            if (view.isSelected()) {
                                setTeamProfileNameSelectedEditableUI(viewBtnTeamProfileName, tvTeamProfileName, imgTeamProfileName);
                            } else {
                                setTeamProfileNameUnselectedEditableUI(viewBtnTeamProfileName, tvTeamProfileName, imgTeamProfileName);
                            }
                        }
                    });

                    mLinearLayoutTeamProfileSelection.addView(viewBtnTeamProfileName);
                }

//                // Checks Bundle here ONLY after inflating required 'Assign To' data for checks
//                checkBundle(true);
            }

            @Override
            public void onError(Throwable e) {
                // show an error message
                Timber.e("onError singleObserverForAllUsers, initCurrentUserCallsignTeamProfileUI. Error Msg: %s", e.toString());
            }
        };

        mUserViewModel.getAllUsers(singleObserverForAllUsers);
    }

    /**
     * Initialise image buttons UI
     */
    private void initImageButtons(View rootView) {

        // Only allow CCT to push data to Excel
        // Both CCT and Team Leads are permitted to pull data from Excel
        mImgLayoutPullFromExcel = rootView.findViewById(R.id.layout_pull_from_excel_text_img_btn);
        mImgLayoutPushToExcel = rootView.findViewById(R.id.layout_push_to_excel_img_text_img_btn);
        View imgLayoutSyncUp = rootView.findViewById(R.id.layout_sync_img_text_img_btn);
        imgLayoutSyncUp.setVisibility(View.GONE);
        mImgLayoutPullFromExcel.setVisibility(View.GONE);

        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                SharedPreferenceUtil.getCurrentUserAccessRight())) {

            mImgLayoutPushToExcel.setVisibility(View.GONE);

        } else {
            initPushToExcelUI();

        }

        initPullFromExcelUI();
        initLogoutUI(rootView);
    }

    /**
     * Initialise Pull from Excel button UI
     */
    private void initPullFromExcelUI() {
        mImgLayoutPullFromExcel.setVisibility(View.VISIBLE);
        LinearLayout layoutPullFromExcel = mImgLayoutPullFromExcel.findViewById(R.id.layout_main_img_text_img_btn);
        C2OpenSansSemiBoldTextView tvPullFromExcel = mImgLayoutPullFromExcel.findViewById(R.id.tv_text_img_btn);
        AppCompatImageView imgPullFromExcel = mImgLayoutPullFromExcel.findViewById(R.id.img_pic_img_btn);
        layoutPullFromExcel.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.img_btn_background_green_border, null));
        tvPullFromExcel.setText(getString(R.string.btn_pull_from_excel));
        tvPullFromExcel.setTextColor(ResourcesCompat.getColor(getResources(), R.color.dull_green, null));
        imgPullFromExcel.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.side_nav_menu_sitrep_btn, null));
        imgPullFromExcel.setColorFilter(ContextCompat.getColor(getContext(), R.color.dull_green),
                PorterDuff.Mode.SRC_ATOP);

        mImgLayoutPullFromExcel.setOnClickListener(onPullFromExcelClickListener);
    }

    /**
     * Initialise Push to Excel button UI
     */
    private void initPushToExcelUI() {
        mImgLayoutPushToExcel.setVisibility(View.VISIBLE);
        LinearLayout layoutPushToExcel = mImgLayoutPushToExcel.findViewById(R.id.layout_main_img_text_img_btn);
        C2OpenSansSemiBoldTextView tvPushToExcel = mImgLayoutPushToExcel.findViewById(R.id.tv_text_img_btn);
        AppCompatImageView imgPushToExcel = mImgLayoutPushToExcel.findViewById(R.id.img_pic_img_btn);
        layoutPushToExcel.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.img_btn_background_green_border, null));
        tvPushToExcel.setText(getString(R.string.btn_push_to_excel));
        tvPushToExcel.setTextColor(ResourcesCompat.getColor(getResources(), R.color.dull_green, null));
        imgPushToExcel.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.side_nav_menu_sitrep_btn, null));
        imgPushToExcel.setColorFilter(ContextCompat.getColor(getContext(), R.color.dull_green),
                PorterDuff.Mode.SRC_ATOP);

        mImgLayoutPushToExcel.setOnClickListener(onPushToExcelClickListener);
    }

    /**
     * Initialise Logout button UI
     *
     * @param rootView
     */
    private void initLogoutUI(View rootView) {
        View imgLayoutLogout = rootView.findViewById(R.id.layout_logout_img_text_img_btn);
        LinearLayout layoutLogout = imgLayoutLogout.findViewById(R.id.layout_main_img_text_img_btn);
        C2OpenSansSemiBoldTextView tvLogout = imgLayoutLogout.findViewById(R.id.tv_text_img_btn);
        AppCompatImageView imgLogout = imgLayoutLogout.findViewById(R.id.img_pic_img_btn);
        layoutLogout.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.img_btn_background_red_border, null));
        tvLogout.setText(getString(R.string.btn_logout));
        tvLogout.setTextColor(ResourcesCompat.getColor(getResources(), R.color.dull_red, null));
        imgLogout.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                R.drawable.icon_logout, null));
        imgLogout.setColorFilter(ContextCompat.getColor(getContext(), R.color.dull_red),
                PorterDuff.Mode.SRC_ATOP);

        imgLayoutLogout.setOnClickListener(onLogoutClickListener);
    }

    private View.OnClickListener onEditOrSaveTeamProfileClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            view.setSelected(!view.isSelected());

            // Set UI for each of the following states:
            // View selected icon state: Save
            //      i)  Selected Team profile           (editable)
            //      ii) Unselected team profile name    (editable)
            // View unselected icon state: Edit
            //      i)  Selected Team profile           (uneditable)
            //      ii) Unselected team profile name    (uneditable)
            if (view.isSelected()) {
                mImgEditSaveIcon.setImageDrawable(ResourcesCompat.getDrawable(
                        getResources(), R.drawable.btn_save, null));

                for (int i = 0; i < mLinearLayoutTeamProfileSelection.getChildCount(); i++) {
                    View viewBtnTeamProfileName = mLinearLayoutTeamProfileSelection.getChildAt(i);

                    C2OpenSansSemiBoldTextView tvTeamProfileName = viewBtnTeamProfileName.
                            findViewById(R.id.tv_img_text_fixed_dimension_text_img_btn);
                    AppCompatImageView imgTeamProfileName = viewBtnTeamProfileName.
                            findViewById(R.id.img_img_text_fixed_dimension_pic_img_btn);

                    if (viewBtnTeamProfileName.isSelected()) {
                        setTeamProfileNameSelectedEditableUI(viewBtnTeamProfileName,
                                tvTeamProfileName, imgTeamProfileName);
                    } else {
                        setTeamProfileNameUnselectedEditableUI(viewBtnTeamProfileName,
                                tvTeamProfileName, imgTeamProfileName);
                    }

                    // Enable all buttons
                    viewBtnTeamProfileName.setEnabled(true);
                }
            } else {
                mImgEditSaveIcon.setImageDrawable(ResourcesCompat.getDrawable(
                        getResources(), R.drawable.btn_edit, null));

                StringBuilder teamProfileNameGroup = new StringBuilder();

                for (int i = 0; i < mLinearLayoutTeamProfileSelection.getChildCount(); i++) {
                    View viewBtnTeamProfileName = mLinearLayoutTeamProfileSelection.getChildAt(i);

                    C2OpenSansSemiBoldTextView tvTeamProfileName = viewBtnTeamProfileName.
                            findViewById(R.id.tv_img_text_fixed_dimension_text_img_btn);
                    AppCompatImageView imgTeamProfileName = viewBtnTeamProfileName.
                            findViewById(R.id.img_img_text_fixed_dimension_pic_img_btn);

                    if (viewBtnTeamProfileName.isSelected()) {
                        String selectedTeamProfileName = StringUtil.removeFirstWord(
                                tvTeamProfileName.getText().toString().trim()).trim();
                        teamProfileNameGroup.append(selectedTeamProfileName);
                        teamProfileNameGroup.append(StringUtil.COMMA);

                        setTeamProfileNameSelectedUneditableUI(viewBtnTeamProfileName,
                                tvTeamProfileName, imgTeamProfileName);
                    } else {
                        setTeamProfileNameUnselectedUneditableUI(viewBtnTeamProfileName,
                                tvTeamProfileName, imgTeamProfileName);
                    }

                    // Disable all buttons
                    viewBtnTeamProfileName.setEnabled(false);
                }

                // Save button pressed; save new Team Profile Names which current user now belongs to
                mCurrentUserModel.setTeam(teamProfileNameGroup.substring(0,
                        teamProfileNameGroup.length() - 1).trim());
                mUserViewModel.updateUser(mCurrentUserModel);
                JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(mCurrentUserModel);
            }
        }
    };

    private View.OnClickListener onPullFromExcelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getSnackbarView() != null) {
                mSnackbarOption = SNACKBAR_PULL_FROM_EXCEL_ID;
                SnackbarUtil.showCustomAlertSnackbar(mMainLayout, getSnackbarView(),
                        getString(R.string.snackbar_settings_pull_from_excel_message),
                        UserSettingsFragment.this);
            }
        }
    };

    private View.OnClickListener onPushToExcelClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getSnackbarView() != null) {
                mSnackbarOption = SNACKBAR_PUSH_TO_EXCEL_ID;
                SnackbarUtil.showCustomAlertSnackbar(mMainLayout, getSnackbarView(),
                        getString(R.string.snackbar_settings_push_to_excel_message),
                        UserSettingsFragment.this);
            }
        }
    };

    private View.OnClickListener onLogoutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (getSnackbarView() != null) {
                mSnackbarOption = SNACKBAR_LOGOUT_ID;
                SnackbarUtil.showCustomAlertSnackbar(mMainLayout, getSnackbarView(),
                        getString(R.string.snackbar_settings_logout_message),
                        UserSettingsFragment.this);
            }
        }
    };

    /**
     * Get Snackbar view from main activity
     *
     * @return
     */
    private View getSnackbarView() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getSnackbarView();
        } else {
            return null;
        }
    }

    private void pullDataFromExcelToDatabase() {
        Timber.i("Pulling data from Excel to Database...");

        ExcelSpreadsheetRepository excelSpreadsheetRepository =
                new ExcelSpreadsheetRepository();
        excelSpreadsheetRepository.pullDataFromExcelToDatabase();
    }

    private void pushToExcelFromDatabase() {
        Timber.i("Pushing data to Excel from Database...");

        ExcelSpreadsheetRepository excelSpreadsheetRepository =
                new ExcelSpreadsheetRepository();
        excelSpreadsheetRepository.pushDataToExcelFromDatabase();
    }

    /**
     * Sets each Selected team profile name button UI to Editable layout
     *
     * @param viewBtnTeamProfileName
     * @param tvTeamProfileName
     * @param imgTeamProfileNameRadioBtn
     */
    private void setTeamProfileNameSelectedEditableUI(View viewBtnTeamProfileName,
                                                      C2OpenSansSemiBoldTextView tvTeamProfileName,
                                                      AppCompatImageView imgTeamProfileNameRadioBtn) {
        viewBtnTeamProfileName.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.img_btn_background_cyan_border, null));
        tvTeamProfileName.setTextColor(ContextCompat.getColor(getContext(),
                R.color.primary_highlight_cyan));
        imgTeamProfileNameRadioBtn.setImageDrawable(ResourcesCompat.getDrawable(
                getResources(), R.drawable.icon_checkbox_selected, null));
        imgTeamProfileNameRadioBtn.setColorFilter(ContextCompat.getColor(
                getContext(), R.color.translucent), PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Sets each Selected team profile name button UI to Uneditable layout
     *
     * @param viewBtnTeamProfileName
     * @param tvTeamProfileName
     * @param imgTeamProfileNameRadioBtn
     */
    private void setTeamProfileNameSelectedUneditableUI(View viewBtnTeamProfileName,
                                                        C2OpenSansSemiBoldTextView tvTeamProfileName,
                                                        AppCompatImageView imgTeamProfileNameRadioBtn) {
        viewBtnTeamProfileName.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.img_btn_background_divider_grey_border, null));
        tvTeamProfileName.setTextColor(ContextCompat.getColor(getContext(),
                R.color.divider_line_secondary_grey));
        imgTeamProfileNameRadioBtn.setImageDrawable(ResourcesCompat.getDrawable(
                getResources(), R.drawable.icon_checkbox_selected_uneditable, null));
        imgTeamProfileNameRadioBtn.setColorFilter(ContextCompat.getColor(
                getContext(), R.color.translucent), PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Sets each Unselected team profile name button UI to Editable layout
     *
     * @param viewBtnTeamProfileName
     * @param tvTeamProfileName
     * @param imgTeamProfileNameRadioBtn
     */
    private void setTeamProfileNameUnselectedEditableUI(View viewBtnTeamProfileName,
                                                        C2OpenSansSemiBoldTextView tvTeamProfileName,
                                                        AppCompatImageView imgTeamProfileNameRadioBtn) {
        viewBtnTeamProfileName.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.img_btn_background_without_border, null));
        tvTeamProfileName.setTextColor(ContextCompat.getColor(getContext(),
                R.color.primary_text_hint_dark_grey));
        imgTeamProfileNameRadioBtn.setImageDrawable(ResourcesCompat.getDrawable(
                getResources(), R.drawable.icon_new_unselected, null));
        imgTeamProfileNameRadioBtn.setColorFilter(ContextCompat.getColor(
                getContext(), R.color.translucent), PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Sets each Unselected team profile name button UI to Uneditable layout
     *
     * @param viewBtnTeamProfileName
     * @param tvTeamProfileName
     * @param imgTeamProfileNameRadioBtn
     */
    private void setTeamProfileNameUnselectedUneditableUI(View viewBtnTeamProfileName,
                                                          C2OpenSansSemiBoldTextView tvTeamProfileName,
                                                          AppCompatImageView imgTeamProfileNameRadioBtn) {
        viewBtnTeamProfileName.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.img_btn_background_divider_grey_border, null));
        tvTeamProfileName.setTextColor(ContextCompat.getColor(getContext(),
                R.color.divider_line_secondary_grey));
        imgTeamProfileNameRadioBtn.setImageDrawable(ResourcesCompat.getDrawable(
                getResources(), R.drawable.icon_new_unselected, null));
        imgTeamProfileNameRadioBtn.setColorFilter(ContextCompat.getColor(
                getContext(), R.color.divider_line_secondary_grey), PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Registers broadcast receiver notification of successful connection of wave relay client
     */
    private void registerDataSyncBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(JeroMQClientPairRunnable.DATA_SYNC_SUCCESSFUL_INTENT_ACTION);
        filter.addAction(JeroMQClientPairRunnable.DATA_SYNC_FAILED_INTENT_ACTION);

        mDataSyncBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (JeroMQClientPairRunnable.DATA_SYNC_SUCCESSFUL_INTENT_ACTION.
                        equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_sync_up_successful_message));

                } else if (JeroMQClientPairRunnable.DATA_SYNC_FAILED_INTENT_ACTION.
                        equalsIgnoreCase(intent.getAction())) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_settings_sync_up_failed_message));
                }
            }
        };

        LocalBroadcastManager.getInstance(MainApplication.getAppContext()).registerReceiver(mDataSyncBroadcastReceiver, filter);
    }

//    /**
//     * Refresh Sync with Callsign UI with updated live data
//     * @param userModelList
//     */
//    private void refreshSyncWithCallsignUI(List<UserModel> userModelList) {
//
//        if (mSyncWithCallsignUserModelList == null) {
//            mSyncWithCallsignUserModelList = new ArrayList<>();
//        } else {
//            mSyncWithCallsignUserModelList.clear();
//        }
//
//        // Obtain User model of CCTs who are ONLINE from database
//        List<UserModel> cctUserOnlineModelList = userModelList.stream().
//                filter(userModel -> EAccessRight.CCT.toString().
//                        equalsIgnoreCase(userModel.getRole()) &&
//                        !SharedPreferenceUtil.getCurrentUserCallsignID().
//                                equalsIgnoreCase(userModel.getUserId()) &&
//                        userModel.getRadioFullConnectionStatus().
//                                equalsIgnoreCase(ERadioConnectionStatus.ONLINE.toString())).
//                collect(Collectors.toList());
//
//        // Obtain User model of Team Leads who are ONLINE from database
//        List<UserModel> teamLeadUserOnlineModelList = userModelList.stream().
//                filter(userModel -> EAccessRight.TEAM_LEAD.toString().
//                        equalsIgnoreCase(userModel.getRole()) &&
//                                !SharedPreferenceUtil.getCurrentUserCallsignID().
//                                        equalsIgnoreCase(userModel.getUserId()) &&
//                        userModel.getRadioFullConnectionStatus().
//                                equalsIgnoreCase(ERadioConnectionStatus.ONLINE.toString())).
//                collect(Collectors.toList());
//
//        mSyncWithCallsignUserModelList.addAll(cctUserOnlineModelList);
//        mSyncWithCallsignUserModelList.addAll(teamLeadUserOnlineModelList);
//
//        mRecyclerAdapterSyncWithCallsign.setUserListItems(mSyncWithCallsignUserModelList);
//
//        if (mSyncWithCallsignUserModelList.size() == 0) {
//            mTvSyncWithCallsignNoOne.setVisibility(View.VISIBLE);
//        } else {
//            mTvSyncWithCallsignNoOne.setVisibility(View.GONE);
//        }
//    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mWaveRelayRadioViewModel = ViewModelProviders.of(this).get(WaveRelayRadioViewModel.class);

        /*
         * Refreshes UI whenever there is a change in User (insert, update or delete)
         */
        mUserViewModel.getAllUsersLiveData().observe(this, new Observer<List<UserModel>>() {
            @Override
            public void onChanged(@Nullable List<UserModel> userModelList) {
                Timber.i("SyncWithCallsign userModelList: " + userModelList);

                mRecyclerAdapter.setUserListItems(userModelList);

//                refreshSyncWithCallsignUI(userModelList);
            }
        });
    }

    /**
     * Pops back stack of ONLY current tab
     *
     * @return
     */
    public boolean popBackStack() {
        if (!isAdded())
            return false;

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
            return true;
        } else
            return false;
    }

    private void onVisible() {
        Timber.i("onVisible");

        registerDataSyncBroadcastReceiver();

//        FragmentManager fm = getChildFragmentManager();
//        boolean isFragmentFound = false;
//
//        int count = fm.getBackStackEntryCount();
//
//        // Checks if current fragment exists in Back stack
//        for (int i = 0; i < count; i++) {
//            if (this.getClass().getSimpleName().equalsIgnoreCase(fm.getBackStackEntryAt(i).getName())) {
//                isFragmentFound = true;
//            }
//        }
//
//        // If not found, add current fragment to Back stack
//        if (!isFragmentFound) {
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.addToBackStack(this.getClass().getSimpleName());
//            ft.commit();
//        }
    }

    private void onInvisible() {
        Timber.i("onInvisible");

        if (mDataSyncBroadcastReceiver != null) {
            LocalBroadcastManager.getInstance(MainApplication.getAppContext()).unregisterReceiver(mDataSyncBroadcastReceiver);
            mDataSyncBroadcastReceiver = null;
        }
    }

    @Override
    public void onSnackbarActionClick() {
        switch (mSnackbarOption) {
            case SNACKBAR_PULL_FROM_EXCEL_ID: // Pull from Excel
                pullDataFromExcelToDatabase();
                break;

            case SNACKBAR_PUSH_TO_EXCEL_ID: // Push to Excel
                pushToExcelFromDatabase();
                break;

//            case SNACKBAR_SYNC_UP_ID: // Sync Up with selected user
//                syncWithCallsign();
//                break;

            case SNACKBAR_LOGOUT_ID: // Logout
                if (getActivity() instanceof MainActivity) {
                    getActivity().finish();
                }
                break;

            default:
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;

        Timber.i("setUserVisibleHint");

        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                Timber.i("setUserVisibleHint onVisible");

                onVisible();
            } else {
                onInvisible();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mIsFragmentVisibleToUser) {
            onInvisible();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        if (mBroadcastReceiver != null) {
//            mBroadcastReceiver = null;
//        }
    }
}