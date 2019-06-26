package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
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
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.PhotoCaptureUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;

public class SitRepDetailFragment extends Fragment {

    private static final String TAG = SitRepDetailFragment.class.getSimpleName();
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    // View models
    private SitRepViewModel mSitRepViewModel;

    private C2OpenSansBoldTextView mTvTeamNameHeader;
    private C2OpenSansRegularTextView mTvReporter;
    private C2OpenSansRegularTextView mTvReportedDateTime;

    private SubsamplingScaleImageView mImgCapturedPic;

    private C2OpenSansRegularTextView mTvLocation;
    private C2OpenSansRegularTextView mTvActivity;
    private C2OpenSansRegularTextView mTvPersonnelT;
    private C2OpenSansRegularTextView mTvPersonnelS;
    private C2OpenSansRegularTextView mTvPersonnelD;
    private C2OpenSansRegularTextView mTvNextCoa;
    private C2OpenSansRegularTextView mTvRequest;
    private C2OpenSansRegularTextView mTvOthers;

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

        mTvLocation = rootView.findViewById(R.id.tv_sitrep_detail_location);
        mTvActivity = rootView.findViewById(R.id.tv_sitrep_detail_activity);
        mTvPersonnelT = rootView.findViewById(R.id.tv_sitrep_detail_personnel_t);
        mTvPersonnelS = rootView.findViewById(R.id.tv_sitrep_detail_personnel_s);
        mTvPersonnelD = rootView.findViewById(R.id.tv_sitrep_detail_personnel_d);
        mTvNextCoa = rootView.findViewById(R.id.tv_sitrep_detail_next_coa);
        mTvRequest = rootView.findViewById(R.id.tv_sitrep_detail_request);
        mTvOthers = rootView.findViewById(R.id.tv_sitrep_detail_others);

//        SitRepModel sitRepModel = EventBus.getDefault().removeStickyEvent(SitRepModel.class);

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
            String reportedDateTimeInCustomStrFormat = DateTimeUtil.dateToCustomStringFormat(
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
                Bitmap snappedPhotoBitmap = PhotoCaptureUtil.getImageFromByteArray(snappedPhoto);
                mImgCapturedPic.setImage(ImageSource.bitmap(snappedPhotoBitmap));
                mImgCapturedPic.setVisibility(View.VISIBLE);
            }

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
        }
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "Back button pressed.");

            // Remove sticky Sit Rep model as it is no longer valid
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).removeStickyModel(mSitRepModelOnDisplay);
            }

            popChildBackStack();
        }
    };

    private View.OnClickListener onEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "Edit button pressed.");

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
        Log.d(TAG, "onVisible");
    }

    private void onInvisible() {
        Log.d(TAG, "onInvisible");
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
