package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserSitRepJoinViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DrawableUtil;
import sg.gov.dsta.mobileC3.ventilo.util.PhotoCaptureUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SpinnerItemListDataBank;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ValidationUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.DatabaseTableConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.EIsValid;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.enums.sitRep.EReportType;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import timber.log.Timber;

public class SitRepAddUpdateFragment extends Fragment implements SnackbarUtil.SnackbarActionClickListener {

    private static final String TAG = SitRepAddUpdateFragment.class.getSimpleName();
    private static final int RB_SITREP_TYPE_MISSION_ID = R.id.radio_btn_add_update_sitrep_type_mission;
    private static final int RB_SITREP_TYPE_INSPECTION_ID = R.id.radio_btn_add_update_sitrep_type_inspection;

    // View Models
    private UserViewModel mUserViewModel;
    private SitRepViewModel mSitRepViewModel;
    private UserSitRepJoinViewModel mUserSitRepJoinViewModel;

    // Main
    private FrameLayout mMainLayout;

    // Toolbar section
    private LinearLayout mLinearLayoutBtnSendOrUpdate;
    private C2OpenSansSemiBoldTextView mTvToolbarSendOrUpdate;

    // Task Type
    private RadioGroup mRgSitRepType;

    // Header Title section
    private C2OpenSansSemiBoldTextView mTvCallsignTitle;

    // Linear Layout Groups (For different reports)
    private LinearLayout mLinearLayoutMission;
    private LinearLayout mLinearLayoutInspection;

    // Picture section
    private AppCompatImageView mImgPhotoGallery;
    private AppCompatImageView mImgOpenCamera;
    private FrameLayout mFrameLayoutPicture;
    private SubsamplingScaleImageView mImgPicture;
    private AppCompatImageView mImgClose;
    private String mImageFileAbsolutePath;
    private Bitmap mCompressedFileBitmap;

    /** -------------------- Mission Report UI -------------------- **/
    // Location section
    private RelativeLayout mLayoutLocationInputOthers;
    private AppCompatImageView mImgLocationInputOthers;
    private Spinner mSpinnerLocation;
    private C2OpenSansRegularEditTextView mEtvLocationOthers;

    // Activity section
    private RelativeLayout mLayoutActivityInputOthers;
    private AppCompatImageView mImgActivityInputOthers;
    private Spinner mSpinnerActivity;
    private C2OpenSansRegularEditTextView mEtvActivityOthers;

    // Personnel section;
    private C2OpenSansRegularTextView mTvPersonnelNumberT;
    private C2OpenSansRegularTextView mTvPersonnelNumberS;
    private C2OpenSansRegularTextView mTvPersonnelNumberD;

    // Next coa section
    private RelativeLayout mLayoutNextCoaInputOthers;
    private AppCompatImageView mImgNextCoaInputOthers;
    private Spinner mSpinnerNextCoa;
    private C2OpenSansRegularEditTextView mEtvNextCoaOthers;

    // Request section
    private RelativeLayout mLayoutRequestInputOthers;
    private AppCompatImageView mImgRequestInputOthers;
    private Spinner mSpinnerRequest;
    private C2OpenSansRegularEditTextView mEtvRequestOthers;

    // Others section
    private C2OpenSansRegularEditTextView mEtvOthers;

    /** -------------------- Inspection Report UI -------------------- **/

    // Vessel Type section
    private RelativeLayout mLayoutVesselTypeInputOthers;
    private AppCompatImageView mImgVesselTypeInputOthers;
    private Spinner mSpinnerVesselType;
    private C2OpenSansRegularEditTextView mEtvVesselTypeOthers;

    // Vessel Name section
    private RelativeLayout mLayoutVesselNameInputOthers;
    private AppCompatImageView mImgVesselNameInputOthers;
    private Spinner mSpinnerVesselName;
    private C2OpenSansRegularEditTextView mEtvVesselNameOthers;

    // LPOC section
    private RelativeLayout mLayoutLpocInputOthers;
    private AppCompatImageView mImgLpocInputOthers;
    private Spinner mSpinnerLpoc;
    private C2OpenSansRegularEditTextView mEtvLpocOthers;

    // NPOC section
    private RelativeLayout mLayoutNpocInputOthers;
    private AppCompatImageView mImgNpocInputOthers;
    private Spinner mSpinnerNpoc;
    private C2OpenSansRegularEditTextView mEtvNpocOthers;

    // Last Visit to SG section
    private C2OpenSansRegularEditTextView mEtvLastVisitToSg;

    // Vessel Last Boarded section
    private C2OpenSansRegularEditTextView mEtvVesselLastBoarded;

    // Cargo section
    private RelativeLayout mLayoutCargoInputOthers;
    private AppCompatImageView mImgCargoInputOthers;

    private Spinner mSpinnerCargo;
    private C2OpenSansRegularEditTextView mEtvCargoOthers;

    // Purpose of Call section
    private C2OpenSansRegularEditTextView mEtvPurposeOfCall;

    // Duration section
    private C2OpenSansRegularEditTextView mEtvDuration;

    // Current Crew section
    private C2OpenSansRegularEditTextView mEtvCurrentCrew;

    // Current Master section
    private C2OpenSansRegularEditTextView mEtvCurrentMaster;

    // Current CE section
    private C2OpenSansRegularEditTextView mEtvCurrentCe;

    // Queries section
    private C2OpenSansRegularEditTextView mEtvQueries;

    private SitRepModel mSitRepModelToUpdate;

    private boolean mIsFragmentVisibleToUser;
    private int mChosenRequestCode;

    // ----- Saved State -----
    // Mission Sit Rep
    private static final String LOCATION_INPUT_OTHERS_SELECTION = "Location Input Others Selection";
    private static final String LOCATION_SPINNER_SELECTED_POS = "Location Spinner Selected Pos";
    private static final String LOCATION_INPUT_OTHERS_TEXT = "Location Input Others Text";
    private static final String ACTIVITY_SPINNER_SELECTED_POS = "Activity Spinner Selected Pos";
    private static final String ACTIVITY_INPUT_OTHERS_SELECTION = "Activity Input Others Selection";
    private static final String ACTIVITY_INPUT_OTHERS_TEXT = "Activity Input Others Text";
    private static final String PERSONNEL_T_VALUE = "Personnel T Value";
    private static final String PERSONNEL_S_VALUE = "Personnel S Value";
    private static final String PERSONNEL_D_VALUE = "Personnel D Value";
    private static final String NEXT_COA_INPUT_OTHERS_SELECTION = "Next COA Input Others Selection";
    private static final String NEXT_COA_SPINNER_SELECTED_POS = "Next COA Spinner Selected Pos";
    private static final String NEXT_COA_INPUT_OTHERS_TEXT = "Next COA Input Others Text";
    private static final String REQUEST_INPUT_OTHERS_SELECTION = "Request Input Others Selection";
    private static final String REQUEST_SPINNER_SELECTED_POS = "Request Spinner Selected Pos";
    private static final String REQUEST_INPUT_OTHERS_TEXT = "Request Input Others Text";

    private boolean mIsLocationInputOthersSelected;
    private boolean mIsActivityInputOthersSelected;
    private int mPersonnelNumberTValue;
    private int mPersonnelNumberSValue;
    private int mPersonnelNumberDValue;
    private boolean mIsNextCoaInputOthersSelected;
    private boolean mIsRequestInputOthersSelected;

    private String mLocationOthersText;
    private String mActivityOthersText;
    private String mNextCoaOthersText;
    private String mRequestOthersText;

    private int mLocationSpinnerSelectedPos;
    private int mActivitySpinnerSelectedPos;
    private int mNextCoaSpinnerSelectedPos;
    private int mRequestSpinnerSelectedPos;

    // Inspection Sit Rep
    private static final String VESSEL_TYPE_INPUT_OTHERS_SELECTION = "Vessel Type Input Others Selection";
    private static final String VESSEL_TYPE_SPINNER_SELECTED_POS = "Vessel Type Spinner Selected Pos";
    private static final String VESSEL_TYPE_INPUT_OTHERS_TEXT = "Vessel Type Input Others Text";
    private static final String VESSEL_NAME_INPUT_OTHERS_SELECTION = "Vessel Name Input Others Selection";
    private static final String VESSEL_NAME_SPINNER_SELECTED_POS = "Vessel Name Spinner Selected Pos";
    private static final String VESSEL_NAME_INPUT_OTHERS_TEXT = "Vessel Name Input Others Text";
    private static final String LPOC_INPUT_OTHERS_SELECTION = "LPOC Input Others Selection";
    private static final String LPOC_SPINNER_SELECTED_POS = "LPOC Spinner Selected Pos";
    private static final String LPOC_INPUT_OTHERS_TEXT = "LPOC Input Others Text";
    private static final String NPOC_INPUT_OTHERS_SELECTION = "NPOC Input Others Selection";
    private static final String NPOC_SPINNER_SELECTED_POS = "NPOC Spinner Selected Pos";
    private static final String NPOC_INPUT_OTHERS_TEXT = "NPOC Input Others Text";
    private static final String CARGO_INPUT_OTHERS_SELECTION = "Cargo Input Others Selection";
    private static final String CARGO_SPINNER_SELECTED_POS = "Cargo Spinner Selected Pos";
    private static final String CARGO_INPUT_OTHERS_TEXT = "Cargo Input Others Text";

    private boolean mIsVesselTypeInputOthersSelected;
    private boolean mIsVesselNameInputOthersSelected;
    private boolean mIsLpocInputOthersSelected;
    private boolean mIsNpocInputOthersSelected;
    private boolean mIsCargoInputOthersSelected;

    private String mVesselTypeOthersText;
    private String mVesselNameOthersText;
    private String mLpocOthersText;
    private String mNpocOthersText;
    private String mCargoOthersText;

    private int mVesselTypeSpinnerSelectedPos;
    private int mVesselNameSpinnerSelectedPos;
    private int mLpocSpinnerSelectedPos;
    private int mNpocSpinnerSelectedPos;
    private int mCargoSpinnerSelectedPos;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_update_sitrep, container, false);
        observerSetup();
        initUI(rootView);
        checkBundle();

//        setRetainInstance(true);

        Log.i(TAG, "Fragment view created.");

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // ----- Mission Sit Rep -----
        outState.putBoolean(LOCATION_INPUT_OTHERS_SELECTION, mIsLocationInputOthersSelected);
        outState.putBoolean(ACTIVITY_INPUT_OTHERS_SELECTION, mIsActivityInputOthersSelected);
        outState.putInt(PERSONNEL_T_VALUE, mPersonnelNumberTValue);
        outState.putInt(PERSONNEL_S_VALUE, mPersonnelNumberSValue);
        outState.putInt(PERSONNEL_D_VALUE, mPersonnelNumberDValue);
        outState.putBoolean(NEXT_COA_INPUT_OTHERS_SELECTION, mIsNextCoaInputOthersSelected);
        outState.putBoolean(REQUEST_INPUT_OTHERS_SELECTION, mIsRequestInputOthersSelected);

        outState.putString(LOCATION_INPUT_OTHERS_TEXT, mEtvLocationOthers.getText().toString());
        outState.putString(ACTIVITY_INPUT_OTHERS_TEXT, mEtvActivityOthers.getText().toString());
        outState.putString(NEXT_COA_INPUT_OTHERS_TEXT, mEtvNextCoaOthers.getText().toString());
        outState.putString(REQUEST_INPUT_OTHERS_TEXT, mEtvRequestOthers.getText().toString());

        outState.putInt(LOCATION_SPINNER_SELECTED_POS, mSpinnerLocation.getSelectedItemPosition());
        outState.putInt(ACTIVITY_SPINNER_SELECTED_POS, mSpinnerActivity.getSelectedItemPosition());
        outState.putInt(NEXT_COA_SPINNER_SELECTED_POS, mSpinnerNextCoa.getSelectedItemPosition());
        outState.putInt(REQUEST_SPINNER_SELECTED_POS, mSpinnerRequest.getSelectedItemPosition());

        // ----- Inspection Sit Rep -----
        outState.putBoolean(VESSEL_TYPE_INPUT_OTHERS_SELECTION, mIsVesselTypeInputOthersSelected);
        outState.putBoolean(VESSEL_NAME_INPUT_OTHERS_SELECTION, mIsVesselNameInputOthersSelected);
        outState.putBoolean(LPOC_INPUT_OTHERS_SELECTION, mIsLpocInputOthersSelected);
        outState.putBoolean(NPOC_INPUT_OTHERS_SELECTION, mIsNpocInputOthersSelected);
        outState.putBoolean(CARGO_INPUT_OTHERS_SELECTION, mIsCargoInputOthersSelected);

        outState.putString(VESSEL_TYPE_INPUT_OTHERS_TEXT, mEtvVesselTypeOthers.getText().toString());
        outState.putString(VESSEL_NAME_INPUT_OTHERS_TEXT, mEtvVesselNameOthers.getText().toString());
        outState.putString(LPOC_INPUT_OTHERS_TEXT, mEtvLpocOthers.getText().toString());
        outState.putString(NPOC_INPUT_OTHERS_TEXT, mEtvNpocOthers.getText().toString());
        outState.putString(CARGO_INPUT_OTHERS_TEXT, mEtvCargoOthers.getText().toString());

        outState.putInt(VESSEL_TYPE_SPINNER_SELECTED_POS, mSpinnerVesselType.getSelectedItemPosition());
        outState.putInt(VESSEL_NAME_SPINNER_SELECTED_POS, mSpinnerVesselName.getSelectedItemPosition());
        outState.putInt(LPOC_SPINNER_SELECTED_POS, mSpinnerLpoc.getSelectedItemPosition());
        outState.putInt(NPOC_SPINNER_SELECTED_POS, mSpinnerNpoc.getSelectedItemPosition());
        outState.putInt(CARGO_SPINNER_SELECTED_POS, mSpinnerCargo.getSelectedItemPosition());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            // ----- Mission Sit Rep -----
            // Location
            mIsLocationInputOthersSelected = savedInstanceState.getBoolean(LOCATION_INPUT_OTHERS_SELECTION, false);

            mLocationSpinnerSelectedPos = savedInstanceState.getInt(LOCATION_SPINNER_SELECTED_POS, 0);

            mLocationOthersText = savedInstanceState.getString(LOCATION_INPUT_OTHERS_TEXT, StringUtil.EMPTY_STRING);

            // Activity
            mIsActivityInputOthersSelected = savedInstanceState.getBoolean(ACTIVITY_INPUT_OTHERS_SELECTION, false);

            mActivitySpinnerSelectedPos = savedInstanceState.getInt(ACTIVITY_SPINNER_SELECTED_POS, 0);

            mActivityOthersText = savedInstanceState.getString(ACTIVITY_INPUT_OTHERS_TEXT, StringUtil.EMPTY_STRING);

            // Personnel Number T, S, D
            mPersonnelNumberTValue = savedInstanceState.getInt(PERSONNEL_T_VALUE, 0);
            mPersonnelNumberSValue = savedInstanceState.getInt(PERSONNEL_S_VALUE, 0);
            mPersonnelNumberDValue = savedInstanceState.getInt(PERSONNEL_D_VALUE, 0);

            if (mTvPersonnelNumberT != null) {
                mTvPersonnelNumberT.setText(String.valueOf(mPersonnelNumberTValue));
            }

            if (mTvPersonnelNumberS != null) {
                mTvPersonnelNumberS.setText(String.valueOf(mPersonnelNumberSValue));
            }

            if (mTvPersonnelNumberD != null) {
                mTvPersonnelNumberD.setText(String.valueOf(mPersonnelNumberDValue));
            }

            // Next COA
            mIsNextCoaInputOthersSelected = savedInstanceState.getBoolean(NEXT_COA_INPUT_OTHERS_SELECTION, false);

            mNextCoaSpinnerSelectedPos = savedInstanceState.getInt(NEXT_COA_SPINNER_SELECTED_POS, 0);

            mNextCoaOthersText = savedInstanceState.getString(NEXT_COA_INPUT_OTHERS_TEXT, StringUtil.EMPTY_STRING);

            // Request
            mIsRequestInputOthersSelected = savedInstanceState.getBoolean(REQUEST_INPUT_OTHERS_SELECTION, false);

            mRequestSpinnerSelectedPos = savedInstanceState.getInt(REQUEST_SPINNER_SELECTED_POS, 0);

            mRequestOthersText = savedInstanceState.getString(REQUEST_INPUT_OTHERS_TEXT, StringUtil.EMPTY_STRING);

            // ----- Inspection Sit Rep -----
            // Vessel Type
            mIsVesselTypeInputOthersSelected = savedInstanceState.getBoolean(VESSEL_TYPE_INPUT_OTHERS_SELECTION, false);

            mVesselTypeSpinnerSelectedPos = savedInstanceState.getInt(VESSEL_TYPE_SPINNER_SELECTED_POS, 0);

            mVesselTypeOthersText = savedInstanceState.getString(VESSEL_TYPE_INPUT_OTHERS_TEXT, StringUtil.EMPTY_STRING);

            // Vessel Name
            mIsVesselNameInputOthersSelected = savedInstanceState.getBoolean(VESSEL_NAME_INPUT_OTHERS_SELECTION, false);

            mVesselNameSpinnerSelectedPos = savedInstanceState.getInt(VESSEL_NAME_SPINNER_SELECTED_POS, 0);

            mVesselNameOthersText = savedInstanceState.getString(VESSEL_NAME_INPUT_OTHERS_TEXT, StringUtil.EMPTY_STRING);

            // Lpoc
            mIsLpocInputOthersSelected = savedInstanceState.getBoolean(LPOC_INPUT_OTHERS_SELECTION, false);

            mLpocSpinnerSelectedPos = savedInstanceState.getInt(LPOC_SPINNER_SELECTED_POS, 0);

            mLpocOthersText = savedInstanceState.getString(LPOC_INPUT_OTHERS_TEXT, StringUtil.EMPTY_STRING);

            // Npoc
            mIsNpocInputOthersSelected = savedInstanceState.getBoolean(NPOC_INPUT_OTHERS_SELECTION, false);

            mNpocSpinnerSelectedPos = savedInstanceState.getInt(NPOC_SPINNER_SELECTED_POS, 0);

            mNpocOthersText = savedInstanceState.getString(NPOC_INPUT_OTHERS_TEXT, StringUtil.EMPTY_STRING);

            // Cargo
            mIsCargoInputOthersSelected = savedInstanceState.getBoolean(CARGO_INPUT_OTHERS_SELECTION, false);

            mCargoSpinnerSelectedPos = savedInstanceState.getInt(CARGO_SPINNER_SELECTED_POS, 0);

            mCargoOthersText = savedInstanceState.getString(CARGO_INPUT_OTHERS_TEXT, StringUtil.EMPTY_STRING);
        }
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mSitRepViewModel = ViewModelProviders.of(this).get(SitRepViewModel.class);
        mUserSitRepJoinViewModel = ViewModelProviders.of(this).get(UserSitRepJoinViewModel.class);
    }

    /**
     * Initialise UIs of every section
     *
     * @param rootView
     */
    private void initUI(View rootView) {
        mMainLayout = rootView.findViewById(R.id.layout_add_update_sitrep_fragment);
        mLinearLayoutMission = rootView.findViewById(R.id.layout_sitrep_mission_report);
        mLinearLayoutInspection = rootView.findViewById(R.id.layout_sitrep_inspection_report);

        initToolbarUI(rootView);
        initCallsignTitleUI(rootView);
        initSitRepTypeUI(rootView);
        initPicUI(rootView);
        initMissionSitRepUI(rootView);
        initInspectionSitRepUI(rootView);

        // Choose default 'Mission' for Sit Rep type
        mLinearLayoutMission.setVisibility(View.VISIBLE);
        mLinearLayoutInspection.setVisibility(View.GONE);
        mRgSitRepType.check(RB_SITREP_TYPE_MISSION_ID);

//        initLayouts(rootView);
//        initSpinners(rootView);
    }

    /**
     * Initialise toolbar UI with back (left) and send/update (right) buttons
     *
     * @param rootView
     */
    private void initToolbarUI(View rootView) {
        View layoutToolbar = rootView.findViewById(R.id.layout_toolbar_add_sitrep_text_left_text_right);
        layoutToolbar.setClickable(true);

        LinearLayout linearLayoutBtnBack = layoutToolbar.findViewById(R.id.layout_toolbar_top_left_btn);
        linearLayoutBtnBack.setOnClickListener(onBackClickListener);
        mLinearLayoutBtnSendOrUpdate = layoutToolbar.findViewById(R.id.layout_toolbar_top_right_btn);

        mTvToolbarSendOrUpdate = layoutToolbar.findViewById(R.id.toolbar_top_right_btn_text);
        mTvToolbarSendOrUpdate.setText(MainApplication.getAppContext().getString(R.string.btn_send));
        mLinearLayoutBtnSendOrUpdate.setOnClickListener(onSendClickListener);
    }

    private void initCallsignTitleUI(View rootView) {
        mTvCallsignTitle = rootView.findViewById(R.id.tv_add_update_sitrep_callsign);

        StringBuilder callsignTitleBuilder = new StringBuilder();
        callsignTitleBuilder.append(SharedPreferenceUtil.getCurrentUserCallsignID());
        callsignTitleBuilder.append(StringUtil.SPACE);
        callsignTitleBuilder.append(MainApplication.getAppContext().
                getString(R.string.sitrep_callsign_header));

        mTvCallsignTitle.setText(callsignTitleBuilder.toString());
    }

    private void initSitRepTypeUI(View rootView) {
        mRgSitRepType = rootView.findViewById(R.id.radio_group_add_update_sitrep_type);
        mRgSitRepType.setOnCheckedChangeListener(onSitRepTypeRadioGroupCheckedChangeListener);
    }

    private void initPicUI(View rootView) {
        mImgPhotoGallery = rootView.findViewById(R.id.img_add_update_sitrep_photo_gallery);
        mImgOpenCamera = rootView.findViewById(R.id.img_add_update_sitrep_open_camera);
        mFrameLayoutPicture = rootView.findViewById(R.id.layout_add_update_sitrep_picture);
        mImgPicture = rootView.findViewById(R.id.img_add_update_sitrep_picture);
        mImgClose = rootView.findViewById(R.id.img_add_update_sitrep_picture_close);

        mImgPhotoGallery.setOnClickListener(onPhotoGalleryClickListener);
        mImgOpenCamera.setOnClickListener(onOpenCameraClickListener);
        mImgClose.setOnClickListener(onPictureCloseClickListener);
    }

    private void initMissionSitRepUI(View rootView) {
        initLocationUI(rootView);
        initActivityUI(rootView);
        initPersonnelUI(rootView);
        initNextCoaUI(rootView);
        initRequestUI(rootView);
        initOthers(rootView);
    }

    private void initInspectionSitRepUI(View rootView) {
        initVesselTypeUI(rootView);
        initVesselNameUI(rootView);
        initLpocUI(rootView);
        initNpocUI(rootView);
        initLastVisitToSgUI(rootView);
        initVesselLastBoardedUI(rootView);
        initCargoUI(rootView);
        initPurposeOfCallUI(rootView);
        initDurationUI(rootView);
        initCurrentCrewUI(rootView);
        initCurrentMasterUI(rootView);
        initCurrentCeUI(rootView);
        initQueriesUI(rootView);
    }

    /** -------------------- Sit Rep Mission init UI -------------------- **/

    private void initLocationUI(View rootView) {
        initInputLocationUI(rootView);
    }

    private void initActivityUI(View rootView) {
        initInputActivity(rootView);
    }

    private void initPersonnelUI(View rootView) {
        View layoutContainerPersonnelT = rootView.findViewById(R.id.layout_add_update_sitrep_personnel_T);
        View layoutContainerPersonnelS = rootView.findViewById(R.id.layout_add_update_sitrep_personnel_S);
        View layoutContainerPersonnelD = rootView.findViewById(R.id.layout_add_update_sitrep_personnel_D);

        initPersonnelTextViewsUI(layoutContainerPersonnelT, layoutContainerPersonnelS, layoutContainerPersonnelD);
        initPersonnelBtns(layoutContainerPersonnelT, layoutContainerPersonnelS, layoutContainerPersonnelD);
    }

    private void initPersonnelTextViewsUI(View layoutContainerPersonnelT, View layoutContainerPersonnelS,
                                          View layoutContainerPersonnelD) {
        C2OpenSansSemiBoldTextView tvPersonnelT = layoutContainerPersonnelT.findViewById(R.id.tv_sitrep_personnel_type);
        C2OpenSansSemiBoldTextView tvPersonnelS = layoutContainerPersonnelS.findViewById(R.id.tv_sitrep_personnel_type);
        C2OpenSansSemiBoldTextView tvPersonnelD = layoutContainerPersonnelD.findViewById(R.id.tv_sitrep_personnel_type);

        tvPersonnelT.setText(MainApplication.getAppContext().getString(R.string.sitrep_T));
        tvPersonnelS.setText(MainApplication.getAppContext().getString(R.string.sitrep_S));
        tvPersonnelD.setText(MainApplication.getAppContext().getString(R.string.sitrep_D));

        mTvPersonnelNumberT = layoutContainerPersonnelT.findViewById(R.id.tv_sitrep_personnel_number);
        mTvPersonnelNumberS = layoutContainerPersonnelS.findViewById(R.id.tv_sitrep_personnel_number);
        mTvPersonnelNumberD = layoutContainerPersonnelD.findViewById(R.id.tv_sitrep_personnel_number);

        mTvPersonnelNumberT.setText("0");
        mTvPersonnelNumberS.setText("0");
        mTvPersonnelNumberD.setText("0");
    }

    private void initPersonnelBtns(View layoutContainerPersonnelT,
                                   View layoutContainerPersonnelS, View layoutContainerPersonnelD) {
        AppCompatImageView imgPersonnelAddT = layoutContainerPersonnelT.findViewById(R.id.img_personnel_add);
        imgPersonnelAddT.setOnClickListener(onAddTClickListener);
        AppCompatImageView imgPersonnelReduceT = layoutContainerPersonnelT.findViewById(R.id.img_personnel_reduce);
        imgPersonnelReduceT.setOnClickListener(onReduceTClickListener);

        AppCompatImageView imgPersonnelAddS = layoutContainerPersonnelS.findViewById(R.id.img_personnel_add);
        imgPersonnelAddS.setOnClickListener(onAddSClickListener);
        AppCompatImageView imgPersonnelReduceS = layoutContainerPersonnelS.findViewById(R.id.img_personnel_reduce);
        imgPersonnelReduceS.setOnClickListener(onReduceSClickListener);

        AppCompatImageView imgPersonnelAddD = layoutContainerPersonnelD.findViewById(R.id.img_personnel_add);
        imgPersonnelAddD.setOnClickListener(onAddDClickListener);
        AppCompatImageView imgPersonnelReduceD = layoutContainerPersonnelD.findViewById(R.id.img_personnel_reduce);
        imgPersonnelReduceD.setOnClickListener(onReduceDClickListener);
    }

    private void initNextCoaUI(View rootView) {
        initInputNextCoa(rootView);
    }

    private void initRequestUI(View rootView) {
        initInputRequest(rootView);
    }

    /** -------------------- Sit Rep Inspection init UI -------------------- **/
    private void initVesselTypeUI(View rootView) {
        initInputVesselTypeUI(rootView);
    }

    private void initVesselNameUI(View rootView) {
        initInputVesselNameUI(rootView);
    }

    private void initLpocUI(View rootView) {
        initInputLpocUI(rootView);
    }

    private void initNpocUI(View rootView) {
        initInputNpocUI(rootView);
    }

    private void initCargoUI(View rootView) {
        initInputCargoUI(rootView);
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Timber.i("Back button pressed.");
            popChildBackStack();
        }
    };

    private View.OnClickListener onSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isFormCompleteForFurtherAction()) {
                if (getSnackbarView() != null) {
                    SnackbarUtil.showCustomAlertSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_sitrep_send_confirmation_message),
                            SitRepAddUpdateFragment.this);
                }
            }
        }
    };

    private View.OnClickListener onUpdateClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (isFormCompleteForFurtherAction()) {
                if (getSnackbarView() != null) {
                    SnackbarUtil.showCustomAlertSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_sitrep_update_confirmation_message),
                            SitRepAddUpdateFragment.this);
                }
            }
        }
    };

    private RadioGroup.OnCheckedChangeListener onSitRepTypeRadioGroupCheckedChangeListener =
            new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case RB_SITREP_TYPE_MISSION_ID:
                            mLinearLayoutMission.setVisibility(View.VISIBLE);
                            mLinearLayoutInspection.setVisibility(View.GONE);

                            break;
                        case RB_SITREP_TYPE_INSPECTION_ID:
                            mLinearLayoutMission.setVisibility(View.GONE);
                            mLinearLayoutInspection.setVisibility(View.VISIBLE);
                            break;

                        default:
                            break;
                    }
                }
            };

    private View.OnClickListener onPhotoGalleryClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent photoGalleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(photoGalleryIntent, PhotoCaptureUtil.PHOTO_GALLERY_REQUEST_CODE);
        }
    };

    private View.OnClickListener onOpenCameraClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            openCameraIntent();
        }
    };

    private View.OnClickListener onPictureCloseClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            closeSelectedPictureUI();
        }
    };

    private void openCameraIntent() {
//        ContentValues values = new ContentValues();

//        StringBuilder imageFullTitle = new StringBuilder();
//        imageFullTitle.append(getString(R.string.sitrep_picture_general_title));
//        imageFullTitle.append(UNDERSCORE);
//        imageFullTitle.append(new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()));
//        values.put(MediaStore.Images.Media.TITLE, imageFullTitle.toString());
//        values.put(MediaStore.Images.Media.DESCRIPTION, "Captured through ventilo app");
//
//        mPhotoURI = getActivity().getContentResolver().insert(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
//        Intent openCameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mPhotoURI);
//        startActivityForResult(openCameraIntent, PhotoCaptureUtil.OPEN_CAMERA_REQUEST_CODE);

        Intent openCameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        if (openCameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = PhotoCaptureUtil.createImageFile(getContext());
            } catch (IOException ex) {
                // Error occurred while creating the File
                Timber.e("onOpenCameraClickListener: Error creating file for captured camera shot");
            }

            if (photoFile != null) {
                try {
                    mImageFileAbsolutePath = photoFile.getAbsolutePath();
                    Uri photoURI = FileProvider.getUriForFile(getActivity().getApplicationContext(),
                            "sg.gov.dsta.mobileC3.ventilo.fileprovider", photoFile);
                    openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            photoURI);
                    startActivityForResult(openCameraIntent,
                            PhotoCaptureUtil.OPEN_CAMERA_REQUEST_CODE);
                } catch (NullPointerException ex) {
                    Timber.e("onOpenCameraClickListener: %s ", ex.toString());
                } catch (IllegalArgumentException ex) {
                    Timber.e("onOpenCameraClickListener: %s ", ex.toString());
                }
            }
        }
    }

    private View.OnClickListener onAddTClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberT.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberT.getText().toString())) {
                int newValue = Integer.valueOf(mTvPersonnelNumberT.getText().toString()) + 1;
                mPersonnelNumberTValue = newValue;
                mTvPersonnelNumberT.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onReduceTClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberT.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberT.getText().toString())
                    && Integer.valueOf(mTvPersonnelNumberT.getText().toString()) > 0) {
                int newValue = Integer.valueOf(mTvPersonnelNumberT.getText().toString()) - 1;
                mPersonnelNumberTValue = newValue;
                mTvPersonnelNumberT.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onAddSClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberS.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberS.getText().toString())) {
                int newValue = Integer.valueOf(mTvPersonnelNumberS.getText().toString()) + 1;
                mPersonnelNumberSValue = newValue;
                mTvPersonnelNumberS.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onReduceSClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberS.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberS.getText().toString())
                    && Integer.valueOf(mTvPersonnelNumberS.getText().toString()) > 0) {
                int newValue = Integer.valueOf(mTvPersonnelNumberS.getText().toString()) - 1;
                mPersonnelNumberSValue = newValue;
                mTvPersonnelNumberS.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onAddDClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberD.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberD.getText().toString())) {
                int newValue = Integer.valueOf(mTvPersonnelNumberD.getText().toString()) + 1;
                mPersonnelNumberDValue = newValue;
                mTvPersonnelNumberD.setText(String.valueOf(newValue));
            }
        }
    };

    private View.OnClickListener onReduceDClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (mTvPersonnelNumberD.getText() != null && ValidationUtil.isNumberField(mTvPersonnelNumberD.getText().toString())
                    && Integer.valueOf(mTvPersonnelNumberD.getText().toString()) > 0) {
                int newValue = Integer.valueOf(mTvPersonnelNumberD.getText().toString()) - 1;
                mPersonnelNumberDValue = newValue;
                mTvPersonnelNumberD.setText(String.valueOf(newValue));
            }
        }
    };

    private ArrayAdapter<String> getSpinnerArrayAdapter(String[] stringArray) {

        return new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_row_item, R.id.tv_spinner_row_item_text, stringArray) {

            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be used as hint
                    return false;
                } else {
                    return true;
                }
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
                layoutSpinner.setGravity(Gravity.END);
                layoutSpinner.setPadding(0, 0,
                        (int) getResources().getDimension(R.dimen.elements_large_margin_spacing), 0);

                TextView tv = view.findViewById(R.id.tv_spinner_row_item_text);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_white, null));
                }

                return view;
            }
        };
    }

    /** -------------------- Mission Report Init UI -------------------- **/

    /**
     * Initialise Sit Rep input location UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputLocationUI(View rootView) {
        View viewInputLocation = rootView.findViewById(R.id.layout_add_update_sitrep_input_location);
        mLayoutLocationInputOthers = viewInputLocation.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgLocationInputOthers = viewInputLocation.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerLocation = viewInputLocation.findViewById(R.id.spinner_broad);

        // Save Enabled set to false to prevent Android from restoring its state by default
        // (This means that text contained in the last of such component (in this case, the spinner) will
        // be saved and be used to populate all components on refresh-- For setSaveEnabled(true) )
        // This is required as there are some layouts that use the same id for Spinners and EditTextView
//        mSpinnerLocation.setSaveEnabled(false);
        mEtvLocationOthers = viewInputLocation.findViewById(R.id.etv_spinner_edittext_others_info);
//        mEtvLocationOthers.setSaveEnabled(false);
        mEtvLocationOthers.setOnTouchListener(onViewTouchListener);

        String[] locationStringArray = SpinnerItemListDataBank.getInstance().getLocationStrArray();

        mLayoutLocationInputOthers.setOnClickListener(onLocationInputOthersClickListener);

        mSpinnerLocation.setAdapter(getSpinnerArrayAdapter(locationStringArray));

        mEtvLocationOthers.setHint(MainApplication.getAppContext().
                getString(R.string.sitrep_location_hint));
    }

    private View.OnClickListener onLocationInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mIsLocationInputOthersSelected = !view.isSelected();
            view.setSelected(mIsLocationInputOthersSelected);

            if (view.isSelected()) {
                setInputOthersSelectedUI(view, mImgLocationInputOthers, mSpinnerLocation, mEtvLocationOthers);
            } else {
                setInputOthersUnselectedUI(view, mImgLocationInputOthers, mSpinnerLocation, mEtvLocationOthers);
            }
        }
    };

    private void setInputOthersSelectedUI(View view, AppCompatImageView inputOthersImageView,
                                          Spinner spinner, C2OpenSansRegularEditTextView editTextView) {

        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));
        inputOthersImageView.setColorFilter(ResourcesCompat.getColor(
                getResources(), R.color.background_main_black, null));
        spinner.setVisibility(View.GONE);
        editTextView.setVisibility(View.VISIBLE);
    }

    private void setInputOthersUnselectedUI(View view, AppCompatImageView inputOthersImageView,
                                            Spinner spinner, C2OpenSansRegularEditTextView editTextView) {

        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.background_dark_grey, null));
        inputOthersImageView.setColorFilter(null);
        spinner.setVisibility(View.VISIBLE);
        editTextView.setVisibility(View.GONE);
    }

    /**
     * Enable internal vertical scrolling for edit text views where content exceed maximum height
     */
    private View.OnTouchListener onViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view.hasFocus()) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_SCROLL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                }
            }
            return false;
        }
    };

    /**
     * Initialise Sit Rep input activity request UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputActivity(View rootView) {
        View viewInputActivity = rootView.findViewById(R.id.layout_add_update_sitrep_input_activity);
        mLayoutActivityInputOthers = viewInputActivity.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgActivityInputOthers = viewInputActivity.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerActivity = viewInputActivity.findViewById(R.id.spinner_broad);
//        mSpinnerActivity.setSaveEnabled(false);
        mEtvActivityOthers = viewInputActivity.findViewById(R.id.etv_spinner_edittext_others_info);
//        mEtvActivityOthers.setSaveEnabled(false);
        mEtvActivityOthers.setOnTouchListener(onViewTouchListener);

        String[] activityStringArray = SpinnerItemListDataBank.getInstance().getActivityStrArray();

        mLayoutActivityInputOthers.setOnClickListener(onActivityInputOthersClickListener);

        mSpinnerActivity.setAdapter(getSpinnerArrayAdapter(activityStringArray));

        mEtvActivityOthers.setHint(MainApplication.getAppContext().
                getString(R.string.sitrep_activity_hint));
    }

    private View.OnClickListener onActivityInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mIsActivityInputOthersSelected = !view.isSelected();
            view.setSelected(mIsActivityInputOthersSelected);

            if (view.isSelected()) {
                setInputOthersSelectedUI(view, mImgActivityInputOthers, mSpinnerActivity, mEtvActivityOthers);
            } else {
                setInputOthersUnselectedUI(view, mImgActivityInputOthers, mSpinnerActivity, mEtvActivityOthers);
            }
        }
    };

    /**
     * Initialise Sit Rep input next course of action UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputNextCoa(View rootView) {
        View viewInputNextCoa = rootView.findViewById(R.id.layout_add_update_sitrep_input_next_coa);
        mLayoutNextCoaInputOthers = viewInputNextCoa.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgNextCoaInputOthers = viewInputNextCoa.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerNextCoa = viewInputNextCoa.findViewById(R.id.spinner_broad);
//        mSpinnerNextCoa.setSaveEnabled(false);
        mEtvNextCoaOthers = viewInputNextCoa.findViewById(R.id.etv_spinner_edittext_others_info);
//        mEtvNextCoaOthers.setSaveEnabled(false);
        mEtvNextCoaOthers.setOnTouchListener(onViewTouchListener);

        String[] nextCoaStringArray = SpinnerItemListDataBank.getInstance().getNextCoaStrArray();

        mLayoutNextCoaInputOthers.setOnClickListener(onNextCoaInputOthersClickListener);

        mSpinnerNextCoa.setAdapter(getSpinnerArrayAdapter(nextCoaStringArray));

        mEtvNextCoaOthers.setHint(MainApplication.getAppContext().
                getString(R.string.sitrep_next_coa_hint));
    }

    private View.OnClickListener onNextCoaInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mIsNextCoaInputOthersSelected = !view.isSelected();
            view.setSelected(mIsNextCoaInputOthersSelected);

            if (view.isSelected()) {
                setInputOthersSelectedUI(view, mImgNextCoaInputOthers, mSpinnerNextCoa, mEtvNextCoaOthers);
            } else {
                setInputOthersUnselectedUI(view, mImgNextCoaInputOthers, mSpinnerNextCoa, mEtvNextCoaOthers);
            }
        }
    };

    /**
     * Initialise Sit Rep input request UI
     * <p>
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputRequest(View rootView) {
        View viewInputRequest = rootView.findViewById(R.id.layout_add_update_sitrep_input_request);
        mLayoutRequestInputOthers = viewInputRequest.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgRequestInputOthers = viewInputRequest.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerRequest = viewInputRequest.findViewById(R.id.spinner_broad);
//        mSpinnerRequest.setSaveEnabled(false);
        mEtvRequestOthers = viewInputRequest.findViewById(R.id.etv_spinner_edittext_others_info);
//        mEtvRequestOthers.setSaveEnabled(false);
        mEtvRequestOthers.setOnTouchListener(onViewTouchListener);

        mLayoutRequestInputOthers.setOnClickListener(onRequestInputOthersClickListener);

        String[] requestStringArray = SpinnerItemListDataBank.getInstance().getRequestStrArray();
        mSpinnerRequest.setAdapter(getSpinnerArrayAdapter(requestStringArray));
        mSpinnerRequest.setOnItemSelectedListener(getRequestSpinnerItemSelectedListener);

        mEtvRequestOthers.setHint(MainApplication.getAppContext().
                getString(R.string.sitrep_request_hint));
    }

    private View.OnClickListener onRequestInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mIsRequestInputOthersSelected = !view.isSelected();
            view.setSelected(mIsRequestInputOthersSelected);

            if (view.isSelected()) {
                setInputOthersSelectedUI(view, mImgRequestInputOthers, mSpinnerRequest, mEtvRequestOthers);
            } else {
                setInputOthersUnselectedUI(view, mImgRequestInputOthers, mSpinnerRequest, mEtvRequestOthers);
            }
        }
    };

    private AdapterView.OnItemSelectedListener getRequestSpinnerItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            String selectedItemText = (String) parent.getItemAtPosition(position);
            // If user change the default selection
            // First item is disable and it is used for hint
            if (position > 0) {
                // Notify the selected item text
                mLinearLayoutBtnSendOrUpdate.setEnabled(true);
                mTvToolbarSendOrUpdate.setTextColor(ResourcesCompat.getColor(getResources(),
                        R.color.primary_highlight_cyan, null));
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * Initialise Sit Rep others UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initOthers(View rootView) {
        mEtvOthers = rootView.findViewById(R.id.etv_add_update_sitrep_input_others);
//        mEtvOthers.setSaveEnabled(false);
        mEtvOthers.setOnTouchListener(onViewTouchListener);
    }

    /** -------------------- Inspection Report Init UI -------------------- **/

    /**
     * Initialise Sit Rep input vessel type UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputVesselTypeUI(View rootView) {
        View viewInputVesselType = rootView.findViewById(R.id.layout_add_update_sitrep_input_vessel_type);
        mLayoutVesselTypeInputOthers = viewInputVesselType.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgVesselTypeInputOthers = viewInputVesselType.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerVesselType = viewInputVesselType.findViewById(R.id.spinner_broad);
//        mSpinnerVesselType.setSaveEnabled(false);
        mEtvVesselTypeOthers = viewInputVesselType.findViewById(R.id.etv_spinner_edittext_others_info);
//        mEtvVesselTypeOthers.setSaveEnabled(false);
        mEtvVesselTypeOthers.setOnTouchListener(onViewTouchListener);

        String[] vesselTypeStringArray = SpinnerItemListDataBank.getInstance().getVesselTypeStrArray();

        mLayoutVesselTypeInputOthers.setOnClickListener(onVesselTypeInputOthersClickListener);

        mSpinnerVesselType.setAdapter(getSpinnerArrayAdapter(vesselTypeStringArray));

        mEtvVesselTypeOthers.setHint(MainApplication.getAppContext().
                getString(R.string.sitrep_vessel_type_hint));
    }

    private View.OnClickListener onVesselTypeInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mIsVesselTypeInputOthersSelected = !view.isSelected();
            view.setSelected(mIsVesselTypeInputOthersSelected);

            if (view.isSelected()) {
                setInputOthersSelectedUI(view, mImgVesselTypeInputOthers, mSpinnerVesselType, mEtvVesselTypeOthers);
            } else {
                setInputOthersUnselectedUI(view, mImgVesselTypeInputOthers, mSpinnerVesselType, mEtvVesselTypeOthers);
            }
        }
    };

    /**
     * Initialise Sit Rep input vessel name UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputVesselNameUI(View rootView) {
        View viewInputVesselName = rootView.findViewById(R.id.layout_add_update_sitrep_input_vessel_name);
        mLayoutVesselNameInputOthers = viewInputVesselName.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgVesselNameInputOthers = viewInputVesselName.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerVesselName = viewInputVesselName.findViewById(R.id.spinner_broad);
//        mSpinnerVesselName.setSaveEnabled(false);
        mEtvVesselNameOthers = viewInputVesselName.findViewById(R.id.etv_spinner_edittext_others_info);
//        mEtvVesselNameOthers.setSaveEnabled(false);
        mEtvVesselNameOthers.setOnTouchListener(onViewTouchListener);

        String[] vesselNameStringArray = SpinnerItemListDataBank.getInstance().getVesselNameStrArray();

        mLayoutVesselNameInputOthers.setOnClickListener(onVesselNameInputOthersClickListener);

        mSpinnerVesselName.setAdapter(getSpinnerArrayAdapter(vesselNameStringArray));

        mEtvVesselNameOthers.setHint(MainApplication.getAppContext().
                getString(R.string.sitrep_vessel_name_hint));
    }

    private View.OnClickListener onVesselNameInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mIsVesselNameInputOthersSelected = !view.isSelected();
            view.setSelected(mIsVesselNameInputOthersSelected);

            if (view.isSelected()) {
                setInputOthersSelectedUI(view, mImgVesselNameInputOthers, mSpinnerVesselName, mEtvVesselNameOthers);
            } else {
                setInputOthersUnselectedUI(view, mImgVesselNameInputOthers, mSpinnerVesselName, mEtvVesselNameOthers);
            }
        }
    };

    /**
     * Initialise Sit Rep input LPOC UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputLpocUI(View rootView) {
        View viewInputLpoc = rootView.findViewById(R.id.layout_add_update_sitrep_input_lpoc);
        mLayoutLpocInputOthers = viewInputLpoc.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgLpocInputOthers = viewInputLpoc.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerLpoc = viewInputLpoc.findViewById(R.id.spinner_broad);
//        mSpinnerLpoc.setSaveEnabled(false);
        mEtvLpocOthers = viewInputLpoc.findViewById(R.id.etv_spinner_edittext_others_info);
//        mEtvLpocOthers.setSaveEnabled(false);
        mEtvLpocOthers.setOnTouchListener(onViewTouchListener);

        String[] lpocStringArray = SpinnerItemListDataBank.getInstance().getLpocStrArray();

        mLayoutLpocInputOthers.setOnClickListener(onLpocInputOthersClickListener);

        mSpinnerLpoc.setAdapter(getSpinnerArrayAdapter(lpocStringArray));

        mEtvLpocOthers.setHint(MainApplication.getAppContext().
                getString(R.string.sitrep_lpoc_hint));
    }

    private View.OnClickListener onLpocInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mIsLpocInputOthersSelected = !view.isSelected();
            view.setSelected(mIsLpocInputOthersSelected);

            if (view.isSelected()) {
                setInputOthersSelectedUI(view, mImgLpocInputOthers, mSpinnerLpoc, mEtvLpocOthers);
            } else {
                setInputOthersUnselectedUI(view, mImgLpocInputOthers, mSpinnerLpoc, mEtvLpocOthers);
            }
        }
    };

    /**
     * Initialise Sit Rep input NPOC UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputNpocUI(View rootView) {
        View viewInputNpoc = rootView.findViewById(R.id.layout_add_update_sitrep_input_npoc);
        mLayoutNpocInputOthers = viewInputNpoc.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgNpocInputOthers = viewInputNpoc.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerNpoc = viewInputNpoc.findViewById(R.id.spinner_broad);
//        mSpinnerNpoc.setSaveEnabled(false);
        mEtvNpocOthers = viewInputNpoc.findViewById(R.id.etv_spinner_edittext_others_info);
//        mEtvNpocOthers.setSaveEnabled(false);
        mEtvNpocOthers.setOnTouchListener(onViewTouchListener);

        String[] npocStringArray = SpinnerItemListDataBank.getInstance().getNpocStrArray();

        mLayoutNpocInputOthers.setOnClickListener(onNpocInputOthersClickListener);

        mSpinnerNpoc.setAdapter(getSpinnerArrayAdapter(npocStringArray));

        mEtvNpocOthers.setHint(MainApplication.getAppContext().
                getString(R.string.sitrep_npoc_hint));
    }

    private View.OnClickListener onNpocInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mIsNpocInputOthersSelected = !view.isSelected();
            view.setSelected(mIsNpocInputOthersSelected);

            if (view.isSelected()) {
                setInputOthersSelectedUI(view, mImgNpocInputOthers, mSpinnerNpoc, mEtvNpocOthers);
            } else {
                setInputOthersUnselectedUI(view, mImgNpocInputOthers, mSpinnerNpoc, mEtvNpocOthers);
            }
        }
    };

    /**
     * Initialise Sit Rep last visit to SG UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initLastVisitToSgUI(View rootView) {
        mEtvLastVisitToSg = rootView.findViewById(R.id.etv_add_update_sitrep_input_last_visit_to_sg);
//        mEtvLastVisitToSg.setSaveEnabled(false);
        mEtvLastVisitToSg.setOnTouchListener(onViewTouchListener);
    }

    /**
     * Initialise Sit Rep vessel last boarded UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initVesselLastBoardedUI(View rootView) {
        mEtvVesselLastBoarded = rootView.findViewById(R.id.etv_add_update_sitrep_input_vessel_last_boarded);
//        mEtvVesselLastBoarded.setSaveEnabled(false);
        mEtvVesselLastBoarded.setOnTouchListener(onViewTouchListener);
    }

    /**
     * Initialise Sit Rep input cargo UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initInputCargoUI(View rootView) {
        View viewInputCargo = rootView.findViewById(R.id.layout_add_update_sitrep_input_cargo);
        mLayoutCargoInputOthers = viewInputCargo.findViewById(R.id.layout_spinner_edittext_input_others_icon);
        mImgCargoInputOthers = viewInputCargo.findViewById(R.id.img_spinner_edittext_input_others_icon);
        mSpinnerCargo = viewInputCargo.findViewById(R.id.spinner_broad);
//        mSpinnerCargo.setSaveEnabled(false);
        mEtvCargoOthers = viewInputCargo.findViewById(R.id.etv_spinner_edittext_others_info);
//        mEtvCargoOthers.setSaveEnabled(false);
        mEtvCargoOthers.setOnTouchListener(onViewTouchListener);

        String[] cargoStringArray = SpinnerItemListDataBank.getInstance().getCargoStrArray();

        mLayoutCargoInputOthers.setOnClickListener(onCargoInputOthersClickListener);

        mSpinnerCargo.setAdapter(getSpinnerArrayAdapter(cargoStringArray));

        mEtvCargoOthers.setHint(MainApplication.getAppContext().
                getString(R.string.sitrep_cargo_hint));
    }

    private View.OnClickListener onCargoInputOthersClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mIsCargoInputOthersSelected = !view.isSelected();
            view.setSelected(mIsCargoInputOthersSelected);

            if (view.isSelected()) {
                setInputOthersSelectedUI(view, mImgCargoInputOthers, mSpinnerCargo, mEtvCargoOthers);
            } else {
                setInputOthersUnselectedUI(view, mImgCargoInputOthers, mSpinnerCargo, mEtvCargoOthers);
            }
        }
    };

    /**
     * Initialise Sit Rep purpose of call UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initPurposeOfCallUI(View rootView) {
        mEtvPurposeOfCall = rootView.findViewById(R.id.etv_add_update_sitrep_input_purpose_of_call);
//        mEtvPurposeOfCall.setSaveEnabled(false);
        mEtvPurposeOfCall.setOnTouchListener(onViewTouchListener);
    }

    /**
     * Initialise Sit Rep duration UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initDurationUI(View rootView) {
        mEtvDuration = rootView.findViewById(R.id.etv_add_update_sitrep_input_duration);
//        mEtvDuration.setSaveEnabled(false);
        mEtvDuration.setOnTouchListener(onViewTouchListener);
    }

    /**
     * Initialise Sit Rep current crew UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initCurrentCrewUI(View rootView) {
        mEtvCurrentCrew = rootView.findViewById(R.id.etv_add_update_sitrep_input_current_crew);
//        mEtvCurrentCrew.setSaveEnabled(false);
        mEtvCurrentCrew.setOnTouchListener(onViewTouchListener);
    }

    /**
     * Initialise Sit Rep current master UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initCurrentMasterUI(View rootView) {
        mEtvCurrentMaster = rootView.findViewById(R.id.etv_add_update_sitrep_input_current_master);
//        mEtvCurrentMaster.setSaveEnabled(false);
        mEtvCurrentMaster.setOnTouchListener(onViewTouchListener);
    }

    /**
     * Initialise Sit Rep current ce UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initCurrentCeUI(View rootView) {
        mEtvCurrentCe = rootView.findViewById(R.id.etv_add_update_sitrep_input_current_ce);
//        mEtvCurrentCe.setSaveEnabled(false);
        mEtvCurrentCe.setOnTouchListener(onViewTouchListener);
    }

    /**
     * Initialise Sit Rep queries UI
     *
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initQueriesUI(View rootView) {
        mEtvQueries = rootView.findViewById(R.id.etv_add_update_sitrep_input_queries);
//        mEtvQueries.setSaveEnabled(false);
        mEtvQueries.setOnTouchListener(onViewTouchListener);
    }

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

    /**
     * Displays scaled bitmap image (of size 1024px which is not too large) to ensure smooth page loading
     *
     * @param bitMapThumbnail
     */
    private void displayScaledBitmap(Bitmap bitMapThumbnail) {
//        Bitmap bitMapThumbnail = BitmapFactory.decodeFile(compressedFilePathName);
        int maxScaledHeight = (int) (bitMapThumbnail.getHeight() *
                (PhotoCaptureUtil.MAX_SCALED_WIDTH_OF_DISPLAY_IN_PIXEL / bitMapThumbnail.getWidth()));
        Bitmap scaledBitMapThumbnail = Bitmap.createScaledBitmap(bitMapThumbnail,
                (int) PhotoCaptureUtil.MAX_SCALED_WIDTH_OF_DISPLAY_IN_PIXEL, maxScaledHeight, true);
        displaySelectedPictureUI(scaledBitMapThumbnail);
    }

    /**
     * Remove selected photo from UI
     */
    private void closeSelectedPictureUI() {
        if (mFrameLayoutPicture != null && mImgPicture != null) {
            mFrameLayoutPicture.setVisibility(View.GONE);
            mImgPicture.recycle();
            mCompressedFileBitmap = null;
        }
    }

    /**
     * Displays user selected photo in UI
     *
     * @param bitmap
     */
    private void displaySelectedPictureUI(Bitmap bitmap) {
        if (mFrameLayoutPicture != null && mImgPicture != null) {
            mFrameLayoutPicture.setVisibility(View.VISIBLE);
            mImgPicture.setImage(ImageSource.bitmap(bitmap));
            mCompressedFileBitmap = bitmap;
        }
    }

    /**
     * Create new Sit Rep Model from form
     *
     * @param userId
     * @return
     */
    private SitRepModel createNewOrUpdateSitRepModelFromForm(String userId, boolean toUpdate) {
        SitRepModel newSitRepModel;

        if (toUpdate) {
            newSitRepModel = mSitRepModelToUpdate;
            newSitRepModel.setLastUpdatedDateTime(DateTimeUtil.getCurrentDateTime());
        } else {
            newSitRepModel = new SitRepModel();
            newSitRepModel.setRefId(DatabaseTableConstants.LOCAL_REF_ID);
            newSitRepModel.setLastUpdatedDateTime(StringUtil.INVALID_STRING);
            newSitRepModel.setCreatedDateTime(DateTimeUtil.getCurrentDateTime());
        }

        newSitRepModel.setReporter(userId);

        byte[] imageByteArray = null;
        if (mCompressedFileBitmap != null) {
            imageByteArray = PhotoCaptureUtil.getByteArrayFromImage(mCompressedFileBitmap, 100);
        }
        newSitRepModel.setSnappedPhoto(imageByteArray);


        if (RB_SITREP_TYPE_MISSION_ID == mRgSitRepType.getCheckedRadioButtonId()) {
            newSitRepModel.setReportType(EReportType.MISSION.toString());

            String location = "";
            if (mLayoutLocationInputOthers.isSelected()) {
                location = mEtvLocationOthers.getText().toString().trim();
            } else {
                location = mSpinnerLocation.getSelectedItem().toString();
            }

            String activity = "";
            if (mLayoutActivityInputOthers.isSelected()) {
                activity = mEtvActivityOthers.getText().toString().trim();
            } else {
                activity = mSpinnerActivity.getSelectedItem().toString();
            }

            String nextCoa = "";
            if (mLayoutNextCoaInputOthers.isSelected()) {
                nextCoa = mEtvNextCoaOthers.getText().toString().trim();
            } else {
                nextCoa = mSpinnerNextCoa.getSelectedItem().toString();
            }

            String request = "";
            if (mLayoutRequestInputOthers.isSelected()) {
                request = mEtvRequestOthers.getText().toString().trim();
            } else {
                request = mSpinnerRequest.getSelectedItem().toString();
            }

            String others = "";
            others = mEtvOthers.getText().toString().trim();

            // Inspection fields
            newSitRepModel.setVesselType(StringUtil.EMPTY_STRING);
            newSitRepModel.setVesselName(StringUtil.EMPTY_STRING);
            newSitRepModel.setLpoc(StringUtil.EMPTY_STRING);
            newSitRepModel.setNpoc(StringUtil.EMPTY_STRING);
            newSitRepModel.setLastVisitToSg(StringUtil.EMPTY_STRING);
            newSitRepModel.setVesselLastBoarded(StringUtil.EMPTY_STRING);
            newSitRepModel.setCargo(StringUtil.EMPTY_STRING);
            newSitRepModel.setPurposeOfCall(StringUtil.EMPTY_STRING);
            newSitRepModel.setDuration(StringUtil.EMPTY_STRING);
            newSitRepModel.setCurrentCrew(StringUtil.EMPTY_STRING);
            newSitRepModel.setCurrentMaster(StringUtil.EMPTY_STRING);
            newSitRepModel.setCurrentCe(StringUtil.EMPTY_STRING);
            newSitRepModel.setQueries(StringUtil.EMPTY_STRING);

            // Mission fields
            newSitRepModel.setLocation(location);
            newSitRepModel.setActivity(activity);
            newSitRepModel.setPersonnelT(Integer.valueOf(mTvPersonnelNumberT.getText().toString().trim()));
            newSitRepModel.setPersonnelS(Integer.valueOf(mTvPersonnelNumberS.getText().toString().trim()));
            newSitRepModel.setPersonnelD(Integer.valueOf(mTvPersonnelNumberD.getText().toString().trim()));
            newSitRepModel.setNextCoa(nextCoa);
            newSitRepModel.setRequest(request);
            newSitRepModel.setOthers(others);

        } else if (RB_SITREP_TYPE_INSPECTION_ID == mRgSitRepType.getCheckedRadioButtonId()) {
            newSitRepModel.setReportType(EReportType.INSPECTION.toString());

            String vesselType = "";
            if (mLayoutVesselTypeInputOthers.isSelected()) {
                vesselType = mEtvVesselTypeOthers.getText().toString().trim();
            } else {
                vesselType = mSpinnerVesselType.getSelectedItem().toString();
            }

            String vesselName = "";
            if (mLayoutVesselNameInputOthers.isSelected()) {
                vesselName = mEtvVesselNameOthers.getText().toString().trim();
            } else {
                vesselName = mSpinnerVesselName.getSelectedItem().toString();
            }

            String lpoc = "";
            if (mLayoutLpocInputOthers.isSelected()) {
                lpoc = mEtvLpocOthers.getText().toString().trim();
            } else {
                lpoc = mSpinnerLpoc.getSelectedItem().toString();
            }

            String npoc = "";
            if (mLayoutNpocInputOthers.isSelected()) {
                npoc = mEtvNpocOthers.getText().toString().trim();
            } else {
                npoc = mSpinnerNpoc.getSelectedItem().toString();
            }

            String lastVisitToSg = "";
            lastVisitToSg = mEtvLastVisitToSg.getText().toString().trim();

            String vesselLastBoarded = "";
            vesselLastBoarded = mEtvVesselLastBoarded.getText().toString().trim();

            String cargo = "";
            if (mLayoutCargoInputOthers.isSelected()) {
                cargo = mEtvCargoOthers.getText().toString().trim();
            } else {
                cargo = mSpinnerCargo.getSelectedItem().toString();
            }

            String purposeOfCall = "";
            purposeOfCall = mEtvPurposeOfCall.getText().toString().trim();

            String duration = "";
            duration = mEtvDuration.getText().toString().trim();

            String currentCrew = "";
            currentCrew = mEtvCurrentCrew.getText().toString().trim();

            String currentMaster = "";
            currentMaster = mEtvCurrentMaster.getText().toString().trim();

            String currentCe = "";
            currentCe = mEtvCurrentCe.getText().toString().trim();

            String queries = "";
            queries = mEtvQueries.getText().toString().trim();

            // Inspection fields
            newSitRepModel.setVesselType(vesselType);
            newSitRepModel.setVesselName(vesselName);
            newSitRepModel.setLpoc(lpoc);
            newSitRepModel.setNpoc(npoc);
            newSitRepModel.setLastVisitToSg(lastVisitToSg);
            newSitRepModel.setVesselLastBoarded(vesselLastBoarded);
            newSitRepModel.setCargo(cargo);
            newSitRepModel.setPurposeOfCall(purposeOfCall);
            newSitRepModel.setDuration(duration);
            newSitRepModel.setCurrentCrew(currentCrew);
            newSitRepModel.setCurrentMaster(currentMaster);
            newSitRepModel.setCurrentCe(currentCe);
            newSitRepModel.setQueries(queries);

            // Mission fields
            newSitRepModel.setLocation(StringUtil.EMPTY_STRING);
            newSitRepModel.setActivity(StringUtil.EMPTY_STRING);
            newSitRepModel.setPersonnelT(0);
            newSitRepModel.setPersonnelS(0);
            newSitRepModel.setPersonnelD(0);
            newSitRepModel.setNextCoa(StringUtil.EMPTY_STRING);
            newSitRepModel.setRequest(StringUtil.EMPTY_STRING);
            newSitRepModel.setOthers(StringUtil.EMPTY_STRING);
        }

//        System.out.println("DateTimeFormatter.ISO_ZONED_DATE_TIME.toString() is " + DateTimeFormatter.ISO_ZONED_DATE_TIME.toString());
//        newSitRepModel.setCreatedDateTime(DateTimeUtil.getCurrentDateTime());

        // Is Valid (Sit Rep Model's validity - whether it has been deleted or not)
        newSitRepModel.setIsValid(EIsValid.YES.toString());

        return newSitRepModel;
    }

    /**
     * Stores Sit Rep data locally and broadcasts to other devices
     *
     * @param sitRepModel
     * @param userId
     */
    private void addItemToLocalDatabaseAndBroadcast(SitRepModel sitRepModel, String userId) {

        SingleObserver<Long> singleObserverAddSitRep = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(Long sitRepId) {
                Timber.i("onSuccess singleObserverAddSitRep,addItemToLocalDatabaseAndBroadcast. SitRepId: %d", sitRepId);

                sitRepModel.setRefId(sitRepId);
//                mSitRepViewModel.updateSitRep(sitRepModel);

                // Store UserSitRepJoin data locally
                UserSitRepJoinModel newUserSitRepJoinModel = new UserSitRepJoinModel(
                        userId, sitRepId);
                mUserSitRepJoinViewModel.addUserSitRepJoin(newUserSitRepJoinModel);

                // Send newly created Sit Rep model to all other devices
                JeroMQBroadcastOperation.broadcastDataInsertionOverSocket(sitRepModel);

                // Show snackbar message and return to main Sit Rep fragment page
                if (getSnackbarView() != null) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            MainApplication.getAppContext().
                                    getString(R.string.snackbar_sitrep_sent_message));
                }

                popChildBackStack();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverAddSitRep, addItemToLocalDatabaseAndBroadcast.Error Msg: %s ", e.toString());
            }
        };

        mSitRepViewModel.insertSitRepWithObserver(sitRepModel, singleObserverAddSitRep);
    }

    /**
     * Validates form before enabling Save/Update button
     *
     * @return
     */
    private boolean isFormCompleteForFurtherAction() {

        // Count used to check that all fields are complete
        // 4 means form is completed, otherwise it is incomplete
        int formCompletedCount = 0;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(MainApplication.getAppContext().
                getString(R.string.snackbar_form_incomplete_message));

        /** -------------------- Sit Rep Mission -------------------- **/
        if (RB_SITREP_TYPE_MISSION_ID == mRgSitRepType.getCheckedRadioButtonId()) {

            // Validate location field
            if (!mLayoutLocationInputOthers.isSelected()) {
                if (mSpinnerLocation.getSelectedItemPosition() != 0) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_location));
                }
            } else {
                if (!TextUtils.isEmpty(mEtvLocationOthers.getText().toString().trim())) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_location));
                }
            }

            // Validate activity field
            if (!mLayoutActivityInputOthers.isSelected()) {
                if (mSpinnerActivity.getSelectedItemPosition() != 0) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_activity));
                }
            } else {
                if (!TextUtils.isEmpty(mEtvActivityOthers.getText().toString().trim())) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_activity));
                }
            }

            // Validate next course of action field
            if (!mLayoutNextCoaInputOthers.isSelected()) {
                if (mSpinnerNextCoa.getSelectedItemPosition() != 0) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_next_coa));
                }
            } else {
                if (!TextUtils.isEmpty(mEtvNextCoaOthers.getText().toString().trim())) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_next_coa));
                }
            }

            // Validate request field
            if (!mLayoutRequestInputOthers.isSelected()) {
                if (mSpinnerRequest.getSelectedItemPosition() != 0) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_request));
                }
            } else {
                if (!TextUtils.isEmpty(mEtvRequestOthers.getText().toString().trim())) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_request));
                }
            }

        } else if (RB_SITREP_TYPE_INSPECTION_ID == mRgSitRepType.getCheckedRadioButtonId()) {

            // Validate vessel type field
            if (!mLayoutVesselTypeInputOthers.isSelected()) {
                if (mSpinnerVesselType.getSelectedItemPosition() != 0) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_vessel_type));
                }
            } else {
                if (!TextUtils.isEmpty(mEtvVesselTypeOthers.getText().toString().trim())) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_vessel_type));
                }
            }

            // Validate vessel name field
            if (!mLayoutVesselNameInputOthers.isSelected()) {
                if (mSpinnerVesselName.getSelectedItemPosition() != 0) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_vessel_name));
                }
            } else {
                if (!TextUtils.isEmpty(mEtvVesselNameOthers.getText().toString().trim())) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_vessel_name));
                }
            }

            // Validate LPOC field
            if (!mLayoutLpocInputOthers.isSelected()) {
                if (mSpinnerLpoc.getSelectedItemPosition() != 0) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_lpoc));
                }
            } else {
                if (!TextUtils.isEmpty(mEtvLpocOthers.getText().toString().trim())) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_lpoc));
                }
            }

            // Validate NPOC field
            if (!mLayoutNpocInputOthers.isSelected()) {
                if (mSpinnerNpoc.getSelectedItemPosition() != 0) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_npoc));
                }
            } else {
                if (!TextUtils.isEmpty(mEtvNpocOthers.getText().toString().trim())) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_npoc));
                }
            }

            // Validate last visit to Sg field
            if (!TextUtils.isEmpty(mEtvLastVisitToSg.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(MainApplication.getAppContext().
                        getString(R.string.sitrep_last_visit_to_sg));
            }

            // Validate vessel last boarded field
            if (!TextUtils.isEmpty(mEtvVesselLastBoarded.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(MainApplication.getAppContext().
                        getString(R.string.sitrep_vessel_last_boarded));
            }

            // Validate cargo field
            if (!mLayoutCargoInputOthers.isSelected()) {
                if (mSpinnerCargo.getSelectedItemPosition() != 0) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_cargo));
                }
            } else {
                if (!TextUtils.isEmpty(mEtvCargoOthers.getText().toString().trim())) {
                    formCompletedCount++;
                } else {
                    stringBuilder.append(System.lineSeparator());
                    stringBuilder.append(StringUtil.HYPHEN);
                    stringBuilder.append(StringUtil.SPACE);
                    stringBuilder.append(MainApplication.getAppContext().
                            getString(R.string.sitrep_cargo));
                }
            }

            // Validate purpose of call field
            if (!TextUtils.isEmpty(mEtvPurposeOfCall.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(MainApplication.getAppContext().
                        getString(R.string.sitrep_purpose_of_call));
            }

            // Validate duration field
            if (!TextUtils.isEmpty(mEtvDuration.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(MainApplication.getAppContext().
                        getString(R.string.sitrep_duration));
            }

            // Validate current crew field
            if (!TextUtils.isEmpty(mEtvCurrentCrew.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(MainApplication.getAppContext().
                        getString(R.string.sitrep_current_crew));
            }

            // Validate current master field
            if (!TextUtils.isEmpty(mEtvCurrentMaster.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(MainApplication.getAppContext().
                        getString(R.string.sitrep_current_master));
            }

            // Validate current ce field
            if (!TextUtils.isEmpty(mEtvCurrentCe.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(MainApplication.getAppContext().
                        getString(R.string.sitrep_current_ce));
            }

            // Validate current queries field
            if (!TextUtils.isEmpty(mEtvQueries.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(MainApplication.getAppContext().
                        getString(R.string.sitrep_queries));
            }
        }

        // Form is incomplete; show snackbar message to fill required fields
        if ((RB_SITREP_TYPE_MISSION_ID == mRgSitRepType.getCheckedRadioButtonId() && formCompletedCount != 4) ||
                (RB_SITREP_TYPE_INSPECTION_ID == mRgSitRepType.getCheckedRadioButtonId() && formCompletedCount != 13)) {
            String fieldsToCompleteMessage = stringBuilder.toString().trim();
            if (getSnackbarView() != null) {
                SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                        fieldsToCompleteMessage);
            }
        } else { // form is complete
            return true;
        }

        return false;
    }

    /**
     * Populates form with Sit Rep model data that is to be updated
     *
     * @param sitRepModel
     */
    private void updateFormData(SitRepModel sitRepModel) {
        if (sitRepModel != null) {
            mTvToolbarSendOrUpdate.setText(MainApplication.getAppContext().
                    getString(R.string.btn_update));
            mTvToolbarSendOrUpdate.setOnClickListener(onUpdateClickListener);

            // Display selected team information
            StringBuilder sitRepTitleBuilder = new StringBuilder();
            sitRepTitleBuilder.append(MainApplication.getAppContext().
                    getString(R.string.team_header));
            sitRepTitleBuilder.append(StringUtil.SPACE);
            sitRepTitleBuilder.append(sitRepModel.getReporter());
            sitRepTitleBuilder.append(StringUtil.SPACE);
            sitRepTitleBuilder.append(MainApplication.getAppContext().
                    getString(R.string.sitrep_callsign_header));
            mTvCallsignTitle.setText(sitRepTitleBuilder.toString().trim());

            // Display selected captured picture
            if (sitRepModel.getSnappedPhoto() != null &&
                    DrawableUtil.IsValidImage(sitRepModel.getSnappedPhoto())) {
                displaySelectedPictureUI(DrawableUtil.getBitmapFromBytes(
                        sitRepModel.getSnappedPhoto()));
            }

            if (EReportType.MISSION.toString().equalsIgnoreCase(sitRepModel.getReportType())) {

                mRgSitRepType.check(RB_SITREP_TYPE_MISSION_ID);

                // Display selected location information
                String[] locationStringArray = SpinnerItemListDataBank.getInstance().getLocationStrArray();
                String sitRepLocation = sitRepModel.getLocation();
                boolean isLocationFoundInSpinner = false;
                for (int i = 0; i < locationStringArray.length; i++) {
                    if (sitRepLocation.equalsIgnoreCase(locationStringArray[i])) {
                        mSpinnerLocation.setSelection(i);
                        isLocationFoundInSpinner = true;
                        break;
                    }
                }

                if (!isLocationFoundInSpinner) {
                    mLayoutLocationInputOthers.setSelected(true);
                    setInputOthersSelectedUI(mLayoutLocationInputOthers, mImgLocationInputOthers,
                            mSpinnerLocation, mEtvLocationOthers);
                    mEtvLocationOthers.setText(sitRepModel.getLocation());
                }

                // Display selected activity information
                String[] activityStringArray = SpinnerItemListDataBank.getInstance().getActivityStrArray();
                String sitRepActivity = sitRepModel.getActivity();
                boolean isActivityFoundInSpinner = false;
                for (int i = 0; i < activityStringArray.length; i++) {
                    if (sitRepActivity.equalsIgnoreCase(activityStringArray[i])) {
                        mSpinnerActivity.setSelection(i);
                        isActivityFoundInSpinner = true;
                        break;
                    }
                }

                if (!isActivityFoundInSpinner) {
                    mLayoutActivityInputOthers.setSelected(true);
                    setInputOthersSelectedUI(mLayoutActivityInputOthers, mImgActivityInputOthers,
                            mSpinnerActivity, mEtvActivityOthers);
                    mEtvActivityOthers.setText(sitRepModel.getActivity());
                }

                // Display selected personnel information
                mTvPersonnelNumberT.setText(String.valueOf(sitRepModel.getPersonnelT()));
                mTvPersonnelNumberS.setText(String.valueOf(sitRepModel.getPersonnelS()));
                mTvPersonnelNumberD.setText(String.valueOf(sitRepModel.getPersonnelD()));

                // Display selected next course of action information
                String[] nextCoaStringArray = SpinnerItemListDataBank.getInstance().getNextCoaStrArray();
                String sitRepNextCoa = sitRepModel.getNextCoa();
                boolean isNextCoaFoundInSpinner = false;
                for (int i = 0; i < nextCoaStringArray.length; i++) {
                    if (sitRepNextCoa.equalsIgnoreCase(nextCoaStringArray[i])) {
                        mSpinnerNextCoa.setSelection(i);
                        isNextCoaFoundInSpinner = true;
                        break;
                    }
                }

                if (!isNextCoaFoundInSpinner) {
                    mLayoutNextCoaInputOthers.setSelected(true);
                    setInputOthersSelectedUI(mLayoutNextCoaInputOthers, mImgNextCoaInputOthers,
                            mSpinnerNextCoa, mEtvNextCoaOthers);
                    mEtvNextCoaOthers.setText(sitRepModel.getNextCoa());
                }

                // Display selected request information
                String[] requestStringArray = SpinnerItemListDataBank.getInstance().getRequestStrArray();
                String sitRepRequest = sitRepModel.getRequest();
                boolean isRequestFoundInSpinner = false;
                for (int i = 0; i < requestStringArray.length; i++) {
                    if (sitRepRequest.equalsIgnoreCase(requestStringArray[i])) {
                        mSpinnerRequest.setSelection(i);
                        isRequestFoundInSpinner = true;
                        break;
                    }
                }

                if (!isRequestFoundInSpinner) {
                    mLayoutRequestInputOthers.setSelected(true);
                    setInputOthersSelectedUI(mLayoutRequestInputOthers, mImgRequestInputOthers,
                            mSpinnerRequest, mEtvRequestOthers);
                    mEtvRequestOthers.setText(sitRepModel.getRequest());
                }

                mEtvOthers.setText(sitRepModel.getOthers());

            } else if (EReportType.INSPECTION.toString().equalsIgnoreCase(sitRepModel.getReportType())) {

                mRgSitRepType.check(RB_SITREP_TYPE_INSPECTION_ID);

                // Display selected vessel type information
                String[] vesselTypeStringArray = SpinnerItemListDataBank.getInstance().getVesselTypeStrArray();
                String sitRepVesselType = sitRepModel.getVesselType();
                boolean isVesselTypeFoundInSpinner = false;
                for (int i = 0; i < vesselTypeStringArray.length; i++) {
                    if (sitRepVesselType.equalsIgnoreCase(vesselTypeStringArray[i])) {
                        mSpinnerVesselType.setSelection(i);
                        isVesselTypeFoundInSpinner = true;
                        break;
                    }
                }

                if (!isVesselTypeFoundInSpinner) {
                    mLayoutVesselTypeInputOthers.setSelected(true);
                    setInputOthersSelectedUI(mLayoutVesselTypeInputOthers, mImgVesselTypeInputOthers,
                            mSpinnerVesselType, mEtvVesselTypeOthers);
                    mEtvVesselTypeOthers.setText(sitRepModel.getVesselType());
                }

                // Display selected vessel name information
                String[] vesselNameStringArray = SpinnerItemListDataBank.getInstance().getVesselNameStrArray();
                String sitRepVesselName = sitRepModel.getVesselName();
                boolean isVesselNameFoundInSpinner = false;
                for (int i = 0; i < vesselNameStringArray.length; i++) {
                    if (sitRepVesselName.equalsIgnoreCase(vesselNameStringArray[i])) {
                        mSpinnerVesselName.setSelection(i);
                        isVesselNameFoundInSpinner = true;
                        break;
                    }
                }

                if (!isVesselNameFoundInSpinner) {
                    mLayoutVesselNameInputOthers.setSelected(true);
                    setInputOthersSelectedUI(mLayoutVesselNameInputOthers, mImgVesselNameInputOthers,
                            mSpinnerVesselName, mEtvVesselNameOthers);
                    mEtvVesselNameOthers.setText(sitRepModel.getVesselName());
                }

                // Display selected LPOC information
                String[] lpocStringArray = SpinnerItemListDataBank.getInstance().getLpocStrArray();
                String sitRepLpoc = sitRepModel.getLpoc();
                boolean isLpocFoundInSpinner = false;
                for (int i = 0; i < lpocStringArray.length; i++) {
                    if (sitRepLpoc.equalsIgnoreCase(lpocStringArray[i])) {
                        mSpinnerLpoc.setSelection(i);
                        isLpocFoundInSpinner = true;
                        break;
                    }
                }

                if (!isLpocFoundInSpinner) {
                    mLayoutLpocInputOthers.setSelected(true);
                    setInputOthersSelectedUI(mLayoutLpocInputOthers, mImgLpocInputOthers,
                            mSpinnerLpoc, mEtvLpocOthers);
                    mEtvLpocOthers.setText(sitRepModel.getLpoc());
                }

                // Display selected NPOC information
                String[] npocStringArray = SpinnerItemListDataBank.getInstance().getNpocStrArray();
                String sitRepNpoc = sitRepModel.getNpoc();
                boolean isNpocFoundInSpinner = false;
                for (int i = 0; i < npocStringArray.length; i++) {
                    if (sitRepNpoc.equalsIgnoreCase(npocStringArray[i])) {
                        mSpinnerNpoc.setSelection(i);
                        isNpocFoundInSpinner = true;
                        break;
                    }
                }

                if (!isNpocFoundInSpinner) {
                    mLayoutNpocInputOthers.setSelected(true);
                    setInputOthersSelectedUI(mLayoutNpocInputOthers, mImgNpocInputOthers,
                            mSpinnerNpoc, mEtvNpocOthers);
                    mEtvNpocOthers.setText(sitRepModel.getNpoc());
                }

                // Display selected last visit to Sg information
                mEtvLastVisitToSg.setText(sitRepModel.getLastVisitToSg());

                // Display selected vessel last boarded information
                mEtvVesselLastBoarded.setText(sitRepModel.getVesselLastBoarded());

                // Display selected cargo information
                String[] cargoStringArray = SpinnerItemListDataBank.getInstance().getCargoStrArray();
                String sitRepCargo = sitRepModel.getCargo();
                boolean isCargoFoundInSpinner = false;
                for (int i = 0; i < cargoStringArray.length; i++) {
                    if (sitRepCargo.equalsIgnoreCase(cargoStringArray[i])) {
                        mSpinnerCargo.setSelection(i);
                        isCargoFoundInSpinner = true;
                        break;
                    }
                }

                if (!isCargoFoundInSpinner) {
                    mLayoutCargoInputOthers.setSelected(true);
                    setInputOthersSelectedUI(mLayoutCargoInputOthers, mImgCargoInputOthers,
                            mSpinnerCargo, mEtvCargoOthers);
                    mEtvCargoOthers.setText(sitRepModel.getCargo());
                }

                // Display selected purpose of call information
                mEtvPurposeOfCall.setText(sitRepModel.getPurposeOfCall());

                // Display selected duration information
                mEtvDuration.setText(sitRepModel.getDuration());

                // Display selected current crew information
                mEtvCurrentCrew.setText(sitRepModel.getCurrentCrew());

                // Display selected current master information
                mEtvCurrentMaster.setText(sitRepModel.getCurrentMaster());

                // Display selected current CE information
                mEtvCurrentCe.setText(sitRepModel.getCurrentCe());

                // Display selected queries information
                mEtvQueries.setText(sitRepModel.getQueries());
            }
        }
    }

    /**
     * Checks the link status of CCT and current user to ensure that
     * Sit Rep can be sent successfully
     */
    private void checkNetworkLinkStatusOfRelevantParties() {
        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<List<UserModel>> singleObserverAllUsers = new SingleObserver<List<UserModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<UserModel> userModelList) {
                Timber.i(TAG, "onSuccess singleObserverAllUsers, " +
                        "checkNetworkLinkStatusOfRelevantParties. " +
                        "userModelList size: %d ", userModelList.size());

                // Obtain User model of CCTs who are ONLINE from database
                List<UserModel> cctUserOnlineModelList = userModelList.stream().
                        filter(userModel -> EAccessRight.CCT.toString().
                                equalsIgnoreCase(userModel.getRole()) &&
                                userModel.getRadioFullConnectionStatus().
                                        equalsIgnoreCase(ERadioConnectionStatus.ONLINE.toString())).
                        collect(Collectors.toList());

                // Obtain current User model who is ONLINE from database
                List<UserModel> currentUserOnlineModelList = userModelList.stream().
                        filter(userModel -> SharedPreferenceUtil.getCurrentUserCallsignID().
                                equalsIgnoreCase(userModel.getUserId()) &&
                                userModel.getRadioFullConnectionStatus().
                                        equalsIgnoreCase(ERadioConnectionStatus.ONLINE.toString())).
                        collect(Collectors.toList());

                if (cctUserOnlineModelList.size() == 0) {
                    if (getSnackbarView() != null) {
                        SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                MainApplication.getAppContext().
                                        getString(R.string.snackbar_send_error_cct_not_connected_message));
                    }
                } else if (currentUserOnlineModelList.size() != 1) {
                    if (getSnackbarView() != null) {
                        SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                MainApplication.getAppContext().
                                        getString(R.string.snackbar_send_error_current_user_not_connected_message));
                    }
                } else {
                    // Only perform send/update action if CCT and current user
                    // are CONNECTED to network
                    UserModel userModel = currentUserOnlineModelList.get(0);
                    performActionClick(userModel);
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(TAG, "onError singleObserverAllUsers, " +
                        "checkNetworkLinkStatusOfRelevantParties. " +
                        "Error Msg: %s ", e.toString());
            }
        };

        mUserViewModel.getAllUsers(singleObserverAllUsers);
    }

    /**
     * Performs Send or Update Sit Rep actions accordingly
     */
    private void performActionClick(UserModel userModel) {

        // Send button
        if (MainApplication.getAppContext().getString(R.string.btn_send).equalsIgnoreCase(
                mTvToolbarSendOrUpdate.getText().toString().trim())) {

            // Create new Sit Rep, store in local database and broadcast to other connected devices
            SitRepModel newSitRepModel = createNewOrUpdateSitRepModelFromForm(userModel.getUserId(),
                    false);
            addItemToLocalDatabaseAndBroadcast(newSitRepModel, userModel.getUserId());

        } else {    // Update button

            // Update existing Sit Rep model
            SitRepModel newSitRepModel = createNewOrUpdateSitRepModelFromForm(userModel.getUserId(),
                    true);

            // Update local Sit Rep data
            mSitRepViewModel.updateSitRep(newSitRepModel);

            // Send updated Sit Rep data to other connected devices
            JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(newSitRepModel);

            // Show snackbar message and return to Sit Rep edit fragment page
            if (getSnackbarView() != null) {
                SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                        MainApplication.getAppContext().
                                getString(R.string.snackbar_sitrep_updated_message));
            }

            popChildBackStack();
        }
    }

    /**
     * Accesses child base fragment of current selected view pager item and remove this fragment
     * from child base fragment's stack.
     * <p>
     * Possible Selected View Pager Item: Sit Rep / Video Stream
     * Child Base Fragment: SitRepFragment / VideoStreamFragment
     */
    private void popChildBackStack() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = ((MainActivity) getActivity());

            if (mainActivity.getViewPager().getCurrentItem() ==
                    MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID) {
                mainActivity.popChildFragmentBackStack(
                        MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID);
            } else if (mainActivity.getViewPager().getCurrentItem() ==
                    MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID) {
                mainActivity.popChildFragmentBackStack(
                        MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // 'Mission' Sit Rep
        if (mLayoutLocationInputOthers != null) {
            mLayoutLocationInputOthers.setSelected(mIsLocationInputOthersSelected);

            if (mLayoutLocationInputOthers.isSelected()) {
                setInputOthersSelectedUI(mLayoutLocationInputOthers, mImgLocationInputOthers, mSpinnerLocation, mEtvLocationOthers);
            } else {
                setInputOthersUnselectedUI(mLayoutLocationInputOthers, mImgLocationInputOthers, mSpinnerLocation, mEtvLocationOthers);
            }
        }

        if (mLayoutActivityInputOthers != null) {
            mLayoutActivityInputOthers.setSelected(mIsActivityInputOthersSelected);

            if (mLayoutActivityInputOthers.isSelected()) {
                setInputOthersSelectedUI(mLayoutActivityInputOthers, mImgActivityInputOthers, mSpinnerActivity, mEtvActivityOthers);
            } else {
                setInputOthersUnselectedUI(mLayoutActivityInputOthers, mImgActivityInputOthers, mSpinnerActivity, mEtvActivityOthers);
            }
        }

        if (mLayoutNextCoaInputOthers != null) {
            mLayoutNextCoaInputOthers.setSelected(mIsNextCoaInputOthersSelected);

            if (mLayoutNextCoaInputOthers.isSelected()) {
                setInputOthersSelectedUI(mLayoutNextCoaInputOthers, mImgNextCoaInputOthers, mSpinnerNextCoa, mEtvNextCoaOthers);
            } else {
                setInputOthersUnselectedUI(mLayoutNextCoaInputOthers, mImgNextCoaInputOthers, mSpinnerNextCoa, mEtvNextCoaOthers);
            }
        }

        if (mLayoutRequestInputOthers != null) {
            mLayoutRequestInputOthers.setSelected(mIsRequestInputOthersSelected);

            if (mLayoutRequestInputOthers.isSelected()) {
                setInputOthersSelectedUI(mLayoutRequestInputOthers, mImgRequestInputOthers, mSpinnerRequest, mEtvRequestOthers);
            } else {
                setInputOthersUnselectedUI(mLayoutRequestInputOthers, mImgRequestInputOthers, mSpinnerRequest, mEtvRequestOthers);
            }
        }

        mSpinnerLocation.setSelection(mLocationSpinnerSelectedPos);
        mSpinnerActivity.setSelection(mActivitySpinnerSelectedPos);
        mSpinnerNextCoa.setSelection(mNextCoaSpinnerSelectedPos);
        mSpinnerRequest.setSelection(mRequestSpinnerSelectedPos);

        if (mLocationOthersText != null) {
            mEtvLocationOthers.setText(mLocationOthersText);
        }

        if (mActivityOthersText != null) {
            mEtvActivityOthers.setText(mActivityOthersText);
        }

        if (mNextCoaOthersText != null) {
            mEtvNextCoaOthers.setText(mNextCoaOthersText);
        }

        if (mRequestOthersText != null) {
            mEtvRequestOthers.setText(mRequestOthersText);
        }

        // Inspection Sit Rep
        if (mLayoutVesselTypeInputOthers != null) {
            mLayoutVesselTypeInputOthers.setSelected(mIsVesselTypeInputOthersSelected);

            if (mLayoutVesselTypeInputOthers.isSelected()) {
                setInputOthersSelectedUI(mLayoutVesselTypeInputOthers, mImgVesselTypeInputOthers, mSpinnerVesselType, mEtvVesselTypeOthers);
            } else {
                setInputOthersUnselectedUI(mLayoutVesselTypeInputOthers, mImgVesselTypeInputOthers, mSpinnerVesselType, mEtvVesselTypeOthers);
            }
        }

        if (mLayoutVesselNameInputOthers != null) {
            mLayoutVesselNameInputOthers.setSelected(mIsVesselNameInputOthersSelected);

            if (mLayoutVesselNameInputOthers.isSelected()) {
                setInputOthersSelectedUI(mLayoutVesselNameInputOthers, mImgVesselNameInputOthers, mSpinnerVesselName, mEtvVesselNameOthers);
            } else {
                setInputOthersUnselectedUI(mLayoutVesselNameInputOthers, mImgVesselNameInputOthers, mSpinnerVesselName, mEtvVesselNameOthers);
            }
        }

        if (mLayoutLpocInputOthers != null) {
            mLayoutLpocInputOthers.setSelected(mIsLpocInputOthersSelected);

            if (mLayoutLpocInputOthers.isSelected()) {
                setInputOthersSelectedUI(mLayoutLpocInputOthers, mImgLpocInputOthers, mSpinnerLpoc, mEtvLpocOthers);
            } else {
                setInputOthersUnselectedUI(mLayoutLpocInputOthers, mImgLpocInputOthers, mSpinnerLpoc, mEtvLpocOthers);
            }
        }

        if (mLayoutNpocInputOthers != null) {
            mLayoutNpocInputOthers.setSelected(mIsNpocInputOthersSelected);

            if (mLayoutNpocInputOthers.isSelected()) {
                setInputOthersSelectedUI(mLayoutNpocInputOthers, mImgNpocInputOthers, mSpinnerNpoc, mEtvNpocOthers);
            } else {
                setInputOthersUnselectedUI(mLayoutNpocInputOthers, mImgNpocInputOthers, mSpinnerNpoc, mEtvNpocOthers);
            }
        }

        if (mLayoutCargoInputOthers != null) {
            mLayoutCargoInputOthers.setSelected(mIsCargoInputOthersSelected);

            if (mLayoutCargoInputOthers.isSelected()) {
                setInputOthersSelectedUI(mLayoutCargoInputOthers, mImgCargoInputOthers, mSpinnerCargo, mEtvCargoOthers);
            } else {
                setInputOthersUnselectedUI(mLayoutCargoInputOthers, mImgCargoInputOthers, mSpinnerCargo, mEtvCargoOthers);
            }
        }

        mSpinnerVesselType.setSelection(mVesselTypeSpinnerSelectedPos);
        mSpinnerVesselName.setSelection(mVesselNameSpinnerSelectedPos);
        mSpinnerLpoc.setSelection(mLpocSpinnerSelectedPos);
        mSpinnerNpoc.setSelection(mNpocSpinnerSelectedPos);
        mSpinnerCargo.setSelection(mCargoSpinnerSelectedPos);

        if (mVesselTypeOthersText != null) {
            mEtvVesselTypeOthers.setText(mVesselTypeOthersText);
        }

        if (mVesselNameOthersText != null) {
            mEtvVesselNameOthers.setText(mVesselNameOthersText);
        }

        if (mLpocOthersText != null) {
            mEtvLpocOthers.setText(mLpocOthersText);
        }

        if (mNpocOthersText != null) {
            mEtvNpocOthers.setText(mNpocOthersText);
        }

        if (mCargoOthersText != null) {
            mEtvCargoOthers.setText(mCargoOthersText);
        }
    }

    private void onVisible() {
        Timber.i("onVisible");

//        FragmentManager fm = getActivity().getSupportFragmentManager();
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
//        // If not found, add to current fragment to Back stack
//        if (!isFragmentFound) {
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.addToBackStack(this.getClass().getSimpleName());
//            ft.commit();
//        }
    }

    private void onInvisible() {
        Timber.i("onInvisible");
    }

    /**
     * Checks for existing data transferred over by Bundle
     */
    private void checkBundle() {
        // Checks if this current fragment is for creation of new Sit Rep or update of existing Sit Rep
        // Else it is returning a selected
        String fragmentType;
        String defaultValue = FragmentConstants.VALUE_SITREP_ADD;
//        String defaultValue = StringUtil.EMPTY_STRING;
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            fragmentType = bundle.getString(FragmentConstants.KEY_SITREP, defaultValue);
        } else {
            fragmentType = defaultValue;
        }

        if (fragmentType.equalsIgnoreCase(FragmentConstants.VALUE_SITREP_UPDATE)) {

//            System.out.println("mSitRepModelToUpdate is " + mSitRepModelToUpdate);
//            mSitRepModelToUpdate = EventBus.getDefault().getStickyEvent(SitRepModel.class);
//            SitRepModel sitRepModelToUpdate = null;
            if (getActivity() instanceof MainActivity) {
                Object objectToUpdate = ((MainActivity) getActivity()).
                        getStickyModel(SitRepModel.class.getSimpleName());
                if (objectToUpdate instanceof SitRepModel) {
                    mSitRepModelToUpdate = (SitRepModel) objectToUpdate;
                }
            }

            updateFormData(mSitRepModelToUpdate);

        } else if (fragmentType.equalsIgnoreCase(FragmentConstants.VALUE_SITREP_ADD_FROM_VIDEO)) {
//            byte[] selectedImageByteArray = bundle.getByteArray(FragmentConstants.KEY_SITREP_PICTURE);
            String selectedImageAbsolutePath = bundle.getString(FragmentConstants.KEY_SITREP_PICTURE, StringUtil.EMPTY_STRING);

//            if (selectedImageByteArray != null) {
//                Bitmap selectedImageBitmap = BitmapFactory.decodeByteArray(selectedImageByteArray, 0,
//                        selectedImageByteArray.length);

            if (selectedImageAbsolutePath != null) {

                Timber.i("selectedImageAbsolutePath: %s", selectedImageAbsolutePath);

                PhotoCaptureUtil.compressBitmapToFile(selectedImageAbsolutePath);
                mCompressedFileBitmap = PhotoCaptureUtil.resizeImage(getContext(), selectedImageAbsolutePath);

                if (mCompressedFileBitmap != null) {
                    displayScaledBitmap(mCompressedFileBitmap);
                }
            }
        }
    }

    @Override
    public void onSnackbarActionClick() {
        // Store compressed bitmap file into phone memory upon sending/update confirmation
        Timber.i("mChosenRequestCode: %s", mChosenRequestCode);

        if (mCompressedFileBitmap != null && mChosenRequestCode == PhotoCaptureUtil.OPEN_CAMERA_REQUEST_CODE) {
            String compressedFilePathName = PhotoCaptureUtil.storeFileIntoPhoneMemory(mCompressedFileBitmap, 1);
            File compressedBitmapFile = new File(compressedFilePathName);

            // This refreshes the photo Gallery app in Android to display the updated list
            getContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(compressedBitmapFile)));
        }

//        performActionClick();
        checkNetworkLinkStatusOfRelevantParties();
    }

    /**
     * Executes screenshot capture of picture or retrieval of picture from Photo Gallery
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Gets file from gallery and displays UI
        // Stores reference of bitmap for storage at a later stage, if confirmed
        if (requestCode == PhotoCaptureUtil.PHOTO_GALLERY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mChosenRequestCode = requestCode;

                Uri targetUri = data.getData();
                String filePathName = PhotoCaptureUtil.getRealPathFromURI(getContext(),
                        targetUri, null, null);
                PhotoCaptureUtil.compressBitmapToFile(filePathName);
                mCompressedFileBitmap = PhotoCaptureUtil.resizeImage(getContext(), filePathName);
                displayScaledBitmap(mCompressedFileBitmap);


            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "Photo gallery picture selection operation cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Opens default camera function.
        // Snapped photo will be compressed and stored in gallery before display of UI.
        // Compressed image is then deleted while bitmap reference is kept for storage at a later stage, if confirmed.
        if (requestCode == PhotoCaptureUtil.OPEN_CAMERA_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                mChosenRequestCode = requestCode;

                Timber.i("mImageFileAbsolutePath: %s", mImageFileAbsolutePath);
                PhotoCaptureUtil.compressBitmapToFile(mImageFileAbsolutePath);
                mCompressedFileBitmap = PhotoCaptureUtil.resizeImage(getContext(), mImageFileAbsolutePath);
                displayScaledBitmap(mCompressedFileBitmap);

            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(getContext(), "Photo taking operation cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;
        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
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
}
