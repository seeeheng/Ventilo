package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.listener.DebounceOnClickListener;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DrawableUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ListenerUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.sitRep.EReportType;
import timber.log.Timber;

public class SitRepDetailFragment extends Fragment {

    private static final String TAG = SitRepDetailFragment.class.getSimpleName();
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    // View models
    private SitRepViewModel mSitRepViewModel;

    private C2OpenSansBoldTextView mTvTeamNameHeader;
    private C2OpenSansRegularTextView mTvReporter;
    private C2OpenSansRegularTextView mTvReportedDateTime;

    private SubsamplingScaleImageView mImgCapturedPic;

    // Layout
    private LinearLayout mLinearLayoutMission;
    private LinearLayout mLinearLayoutInspection;

    // 'Mission' type Sit Rep
    private C2OpenSansRegularTextView mTvLocation;
    private C2OpenSansRegularTextView mTvActivity;
    private C2OpenSansRegularTextView mTvPersonnelT;
    private C2OpenSansRegularTextView mTvPersonnelS;
    private C2OpenSansRegularTextView mTvPersonnelD;
    private C2OpenSansRegularTextView mTvNextCoa;
    private C2OpenSansRegularTextView mTvRequest;
    private C2OpenSansRegularTextView mTvOthers;

    // 'Inspection' type Sit Rep
    private C2OpenSansRegularTextView mTvVesselType;
    private C2OpenSansRegularTextView mTvVesselName;
    private C2OpenSansRegularTextView mTvLpoc;
    private C2OpenSansRegularTextView mTvNpoc;
    private C2OpenSansRegularTextView mTvLastVisitToSg;
    private C2OpenSansRegularTextView mTvVesselLastBoarded;
    private C2OpenSansRegularTextView mTvCargo;
    private C2OpenSansRegularTextView mTvPurposeOfCall;
    private C2OpenSansRegularTextView mTvDuration;
    private C2OpenSansRegularTextView mTvCurrentCrew;
    private C2OpenSansRegularTextView mTvCurrentMaster;
    private C2OpenSansRegularTextView mTvCurrentCe;
    private C2OpenSansRegularTextView mTvQueries;

    private SitRepModel mSitRepModelOnDisplay;

    private boolean mIsFragmentVisibleToUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sitrep_detail, container, false);
        observerSetup();
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        initToolbarUI(rootView);

        mTvTeamNameHeader = rootView.findViewById(R.id.tv_sitrep_details_team_name_header);
        mTvReporter = rootView.findViewById(R.id.tv_sitrep_detail_reporter);
        mTvReportedDateTime = rootView.findViewById(R.id.tv_sitrep_detail_reporter_date_time);

        mImgCapturedPic = rootView.findViewById(R.id.img_sitrep_detail_picture);

        // Layouts
        mLinearLayoutMission = rootView.findViewById(R.id.layout_sitrep_details_mission);
        mLinearLayoutInspection = rootView.findViewById(R.id.layout_sitrep_details_inspection);

        // 'Mission' type Sit Rep
        mTvLocation = rootView.findViewById(R.id.tv_sitrep_detail_location);
        mTvActivity = rootView.findViewById(R.id.tv_sitrep_detail_activity);
        mTvPersonnelT = rootView.findViewById(R.id.tv_sitrep_detail_personnel_t);
        mTvPersonnelS = rootView.findViewById(R.id.tv_sitrep_detail_personnel_s);
        mTvPersonnelD = rootView.findViewById(R.id.tv_sitrep_detail_personnel_d);
        mTvNextCoa = rootView.findViewById(R.id.tv_sitrep_detail_next_coa);
        mTvRequest = rootView.findViewById(R.id.tv_sitrep_detail_request);
        mTvOthers = rootView.findViewById(R.id.tv_sitrep_detail_others);

        // 'Inspection' type Sit Rep
        mTvVesselType = rootView.findViewById(R.id.tv_sitrep_detail_vessel_type);
        mTvVesselName = rootView.findViewById(R.id.tv_sitrep_detail_vessel_name);
        mTvLpoc = rootView.findViewById(R.id.tv_sitrep_detail_lpoc);
        mTvNpoc = rootView.findViewById(R.id.tv_sitrep_detail_npoc);
        mTvLastVisitToSg = rootView.findViewById(R.id.tv_sitrep_detail_last_visit_to_sg);
        mTvVesselLastBoarded = rootView.findViewById(R.id.tv_sitrep_detail_vessel_last_boarded);
        mTvCargo = rootView.findViewById(R.id.tv_sitrep_detail_cargo);
        mTvPurposeOfCall = rootView.findViewById(R.id.tv_sitrep_detail_purpose_of_call);
        mTvDuration = rootView.findViewById(R.id.tv_sitrep_detail_duration);
        mTvCurrentCrew = rootView.findViewById(R.id.tv_sitrep_detail_current_crew);
        mTvCurrentMaster = rootView.findViewById(R.id.tv_sitrep_detail_current_master);
        mTvCurrentCe = rootView.findViewById(R.id.tv_sitrep_detail_current_ce);
        mTvQueries = rootView.findViewById(R.id.tv_sitrep_detail_queries);

        SitRepModel sitRepModel = null;

        if (getActivity() instanceof MainActivity) {
            Object objectToUpdate = ((MainActivity) getActivity()).
                    getStickyModel(SitRepModel.class.getSimpleName());
            if (objectToUpdate instanceof SitRepModel) {
                sitRepModel = (SitRepModel) objectToUpdate;
            }
        }

        updateUI(sitRepModel);
    }

    private void initToolbarUI(View rootView) {
        View layoutToolbar = rootView.findViewById(R.id.layout_toolbar_sitrep_detail_text_left_text_right);
        LinearLayout linearLayoutBtnBack = layoutToolbar.findViewById(R.id.layout_toolbar_top_left_btn);
        linearLayoutBtnBack.setOnClickListener(onBackClickListener);

        LinearLayout linearLayoutBtnEdit = layoutToolbar.findViewById(R.id.layout_toolbar_top_right_btn);
        linearLayoutBtnEdit.setOnClickListener(onEditClickListener);

        C2OpenSansSemiBoldTextView tvToolbarEdit = layoutToolbar.findViewById(R.id.toolbar_top_right_btn_text);
        tvToolbarEdit.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));
        tvToolbarEdit.setText(MainApplication.getAppContext().getString(R.string.btn_edit));
    }

    private void updateUI(SitRepModel sitRepModel) {
        setSitRepInfo(sitRepModel);
    }

    private void setSitRepInfo(SitRepModel sitRepModel) {
        if (sitRepModel != null) {
            mSitRepModelOnDisplay = sitRepModel;
            // Team Name
            StringBuilder teamNameHeaderStringBuilder = new StringBuilder();
            teamNameHeaderStringBuilder.append("Team");
            teamNameHeaderStringBuilder.append(StringUtil.SPACE);

            if (sitRepModel.getReporter() != null) {
                teamNameHeaderStringBuilder.append(sitRepModel.getReporter().trim());
                teamNameHeaderStringBuilder.append(StringUtil.SPACE);
            }

            teamNameHeaderStringBuilder.append(MainApplication.getAppContext().
                    getString(R.string.sitrep_callsign_header));
            mTvTeamNameHeader.setText(teamNameHeaderStringBuilder.toString());

            // Assigned team
            StringBuilder reporterStringBuilder = new StringBuilder();
            reporterStringBuilder.append(MainApplication.getAppContext().
                    getString(R.string.team_header));
            reporterStringBuilder.append(StringUtil.SPACE);

            if (sitRepModel.getReporter() != null) {
                reporterStringBuilder.append(sitRepModel.getReporter().trim());
            }

            mTvReporter.setText(reporterStringBuilder.toString());

            // Reported date/time
            StringBuilder reportedDateTimeStringBuilder = new StringBuilder();
            String reportedDateTime = sitRepModel.getCreatedDateTime();
            String reportedDateTimeInCustomStrFormat = DateTimeUtil.dateToCustomDateTimeStringFormat(
                    DateTimeUtil.stringToDate(reportedDateTime));

            if (reportedDateTime != null) {
                reportedDateTimeStringBuilder.append(reportedDateTimeInCustomStrFormat);
                reportedDateTimeStringBuilder.append(StringUtil.TAB);
                reportedDateTimeStringBuilder.append(StringUtil.TAB);
                reportedDateTimeStringBuilder.append(StringUtil.TAB);
                reportedDateTimeStringBuilder.append(StringUtil.OPEN_BRACKET);
                reportedDateTimeStringBuilder.append(DateTimeUtil.getTimeDifference(
                        DateTimeUtil.stringToDate(reportedDateTime)));
                reportedDateTimeStringBuilder.append(StringUtil.CLOSE_BRACKET);
            }

            mTvReportedDateTime.setText(reportedDateTimeStringBuilder.toString());

            // Snapped Photo
            byte[] snappedPhoto = sitRepModel.getSnappedPhoto();

            if (snappedPhoto != null) {
                Bitmap snappedPhotoBitmap = DrawableUtil.getBitmapFromBytes(snappedPhoto);
                mImgCapturedPic.setImage(ImageSource.bitmap(snappedPhotoBitmap));
                mImgCapturedPic.setVisibility(View.VISIBLE);
            } else {
                mImgCapturedPic.recycle();
                mImgCapturedPic.setVisibility(View.GONE);
            }

            if (EReportType.MISSION.toString().equalsIgnoreCase(sitRepModel.getReportType())) {

                mLinearLayoutMission.setVisibility(View.VISIBLE);
                mLinearLayoutInspection.setVisibility(View.GONE);

                // Location
                if (sitRepModel.getLocation() != null) {
                    mTvLocation.setText(sitRepModel.getLocation().trim());
                }

                // Activity
                if (sitRepModel.getActivity() != null) {
                    mTvActivity.setText(sitRepModel.getActivity().trim());
                }

                // Personnel T
                if (sitRepModel.getPersonnelT() != Integer.valueOf(StringUtil.INVALID_STRING)) {
                    mTvPersonnelT.setText(String.valueOf(sitRepModel.getPersonnelT()));
                }

                // Personnel S
                if (sitRepModel.getPersonnelS() != Integer.valueOf(StringUtil.INVALID_STRING)) {
                    mTvPersonnelS.setText(String.valueOf(sitRepModel.getPersonnelS()));
                }

                // Personnel D
                if (sitRepModel.getPersonnelD() != Integer.valueOf(StringUtil.INVALID_STRING)) {
                    mTvPersonnelD.setText(String.valueOf(sitRepModel.getPersonnelD()));
                }

                // Next course of action
                if (sitRepModel.getNextCoa() != null) {
                    mTvNextCoa.setText(sitRepModel.getNextCoa().trim());
                }

                // Request
                if (sitRepModel.getRequest() != null) {
                    mTvRequest.setText(sitRepModel.getRequest().trim());
                }

                // Others
                if (sitRepModel.getActivity() != null) {
                    mTvOthers.setText(sitRepModel.getOthers().trim());
                }

            } else if (EReportType.INSPECTION.toString().equalsIgnoreCase(sitRepModel.getReportType())) {

                mLinearLayoutMission.setVisibility(View.GONE);
                mLinearLayoutInspection.setVisibility(View.VISIBLE);

                // Vessel Type
                if (sitRepModel.getVesselType() != null) {
                    mTvVesselType.setText(sitRepModel.getVesselType().trim());
                }

                // Vessel Name
                if (sitRepModel.getVesselName() != null) {
                    mTvVesselName.setText(sitRepModel.getVesselName().trim());
                }

                // LPOC
                if (sitRepModel.getLpoc() != null) {
                    mTvLpoc.setText(sitRepModel.getLpoc().trim());
                }

                // NPOC
                if (sitRepModel.getNpoc() != null) {
                    mTvNpoc.setText(sitRepModel.getNpoc().trim());
                }

                // Last Visit to SG
                if (sitRepModel.getLastVisitToSg() != null) {
                    mTvLastVisitToSg.setText(sitRepModel.getLastVisitToSg().trim());
                }

                // Vessel Last Boarded
                if (sitRepModel.getVesselLastBoarded() != null) {
                    mTvVesselLastBoarded.setText(sitRepModel.getVesselLastBoarded().trim());
                }

                // Cargo
                if (sitRepModel.getCargo() != null) {
                    mTvCargo.setText(sitRepModel.getCargo().trim());
                }

                // Purpose of Call
                if (sitRepModel.getPurposeOfCall() != null) {
                    mTvPurposeOfCall.setText(sitRepModel.getPurposeOfCall().trim());
                }

                // Duration
                if (sitRepModel.getDuration() != null) {
                    mTvDuration.setText(sitRepModel.getDuration().trim());
                }

                // Current Crew
                if (sitRepModel.getCurrentCrew() != null) {
                    mTvCurrentCrew.setText(sitRepModel.getCurrentCrew().trim());
                }

                // Current Master
                if (sitRepModel.getCurrentMaster() != null) {
                    mTvCurrentMaster.setText(sitRepModel.getCurrentMaster().trim());
                }

                // Current CE
                if (sitRepModel.getCurrentCe() != null) {
                    mTvCurrentCe.setText(sitRepModel.getCurrentCe().trim());
                }

                // Queries
                if (sitRepModel.getQueries() != null) {
                    mTvQueries.setText(sitRepModel.getQueries().trim());
                }
            }
        }
    }

    private DebounceOnClickListener onBackClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            Timber.i("Back button pressed.");


            // Remove sticky Sit Rep model as it is no longer valid
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).removeStickyModel(mSitRepModelOnDisplay);
            }

            popChildBackStack();
        }
    };

    private DebounceOnClickListener onEditClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            Timber.i("Edit button pressed.");


            if (mSitRepModelOnDisplay != null) {
                Fragment sitRepAddUpdateFragment = new SitRepAddUpdateFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FragmentConstants.KEY_SITREP, FragmentConstants.VALUE_SITREP_UPDATE);
                sitRepAddUpdateFragment.setArguments(bundle);

//                if (getActivity() instanceof MainActivity) {
//                    ((MainActivity) getActivity()).postStickyModel(mSitRepModelOnDisplay);
//                }
//                EventBus.getDefault().postSticky(mSitRepModelOnDisplay);

                navigateToFragment(sitRepAddUpdateFragment);
            }
        }
    };

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mSitRepViewModel = ViewModelProviders.of(this).get(SitRepViewModel.class);

        /*
         * Refreshes UI whenever there is a change in Sit Rep (insert, update or delete)
         */
        mSitRepViewModel.getAllSitRepsLiveData().observe(this, new Observer<List<SitRepModel>>() {
            @Override
            public void onChanged(@Nullable List<SitRepModel> sitRepModelList) {
                for (int i = 0; i < sitRepModelList.size(); i++) {
                    if (mSitRepModelOnDisplay != null &&
                            mSitRepModelOnDisplay.getId() == sitRepModelList.get(i).getId()) {
                        updateUI(sitRepModelList.get(i));
                        break;
                    }
                }
            }
        });
    }

    /**
     * Adds designated fragment to Back Stack of Base Child Fragment
     * before navigating to it
     *
     * @param toFragment
     */
    private void navigateToFragment(Fragment toFragment) {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = ((MainActivity) getActivity());
            mainActivity.navigateWithAnimatedTransitionToFragment(
                    R.id.layout_sitrep_detail_fragment,
                    mainActivity.getBaseChildFragmentOfCurrentFragment(
                            MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID), toFragment);
        }
    }

    /**
     * Accesses child base fragment of current selected view pager item and remove this fragment
     * from child base fragment's stack.
     * <p>
     * Selected View Pager Item: Sit Rep
     * Child Base Fragment: SitRepFragment
     */
    private void popChildBackStack() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = ((MainActivity) getActivity());
            mainActivity.popChildFragmentBackStack(
                    MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID);
        }
    }

//    private Fragment getBaseChildFragmentOfCurrentFragment() {
//        Fragment fragment = null;
//
//        if (getActivity() instanceof MainActivity) {
//            MainActivity mainActivity = ((MainActivity) getActivity());
//            if (mainActivity.getViewPagerAdapter() instanceof MainStatePagerAdapter) {
//                fragment = ((MainStatePagerAdapter) mainActivity.getViewPagerAdapter()).
//                        getFragment(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID);
//            }
//        }
//
//        return fragment;
//    }

    private void onVisible() {

        Timber.i("onVisible");

    }

    private void onInvisible() {

        Timber.i("onInvisible");
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
