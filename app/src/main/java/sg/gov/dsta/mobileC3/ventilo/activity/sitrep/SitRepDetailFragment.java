package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.PhotoCaptureUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;

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

    private SitRepModel mSitRepModelInDisplay;

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

//        mSendButton = rootView.findViewById(R.id.btn_sitrep_detail_send);
//        mSendButton.setOnClickListener(onEditClickListener);
        SitRepModel sitRepModel = EventBus.getDefault().removeStickyEvent(SitRepModel.class);

        System.out.println("sitRepModel location is " + sitRepModel.getLocation());
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
        tvToolbarEdit.setText(getString(R.string.btn_edit));
    }

    private void updateUI(SitRepModel sitRepModel) {
        setSitRepInfo(sitRepModel);
    }

    private void setSitRepInfo(SitRepModel sitRepModel) {
        if (sitRepModel != null) {
            mSitRepModelInDisplay = sitRepModel;
            // Team Name
            StringBuilder teamNameHeaderStringBuilder = new StringBuilder();
            teamNameHeaderStringBuilder.append("Team");
            teamNameHeaderStringBuilder.append(StringUtil.SPACE);

            if (sitRepModel.getReporter() != null) {
                teamNameHeaderStringBuilder.append(sitRepModel.getReporter().trim());
                teamNameHeaderStringBuilder.append(StringUtil.SPACE);
            }

            teamNameHeaderStringBuilder.append(getString(R.string.sitrep_callsign_header));
            mTvTeamNameHeader.setText(teamNameHeaderStringBuilder.toString());

            // Assigned team
            StringBuilder reporterStringBuilder = new StringBuilder();
            reporterStringBuilder.append(getString(R.string.sitrep_team_header));
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

            if (sitRepModel.getCreatedDateTime() != null) {
                reportedDateTimeStringBuilder.append(reportedDateTimeInCustomStrFormat);
                reportedDateTimeStringBuilder.append(StringUtil.TAB);
                reportedDateTimeStringBuilder.append(StringUtil.TAB);
                reportedDateTimeStringBuilder.append(StringUtil.TAB);
                reportedDateTimeStringBuilder.append(StringUtil.OPEN_BRACKET);
                reportedDateTimeStringBuilder.append(DateTimeUtil.getTimeDifference(getContext(),
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
            if (sitRepModel.getPersonnelT() != -1) {
                mTvPersonnelT.setText(String.valueOf(sitRepModel.getPersonnelT()));
            }

            // Personnel S
            if (sitRepModel.getPersonnelS() != -1) {
                mTvPersonnelS.setText(String.valueOf(sitRepModel.getPersonnelS()));
            }

            // Personnel D
            if (sitRepModel.getPersonnelD() != -1) {
                mTvPersonnelD.setText(String.valueOf(sitRepModel.getPersonnelD()));
            }

            // Next course of action
            if (sitRepModel.getActivity() != null) {
                mTvNextCoa.setText(sitRepModel.getNextCoa().trim());
            }

            // Request
            if (sitRepModel.getActivity() != null) {
                mTvRequest.setText(sitRepModel.getRequest().trim());
            }
        }
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "Sit Rep back button clicked.");
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    };

    private View.OnClickListener onEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Log.i(TAG, "Sit Rep edit button clicked.");

            if (mSitRepModelInDisplay != null) {
                Fragment sitRepAddUpdateFragment = new SitRepAddUpdateFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FragmentConstants.KEY_SITREP, FragmentConstants.VALUE_SITREP_UPDATE);
                sitRepAddUpdateFragment.setArguments(bundle);

                EventBus.getDefault().postSticky(mSitRepModelInDisplay);

                FragmentManager fm = getActivity().getSupportFragmentManager();
//                fm.popBackStack();

                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                ft.replace(R.id.layout_sitrep_detail_fragment, sitRepAddUpdateFragment, sitRepAddUpdateFragment.getClass().getSimpleName());
                ft.addToBackStack(sitRepAddUpdateFragment.getClass().getSimpleName());
                ft.commit();
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
                    if (mSitRepModelInDisplay != null &&
                            mSitRepModelInDisplay.getId() == sitRepModelList.get(i).getId()) {
                        updateUI(sitRepModelList.get(i));
                        break;
                    }
                }
            }
        });
    }

//    private String getStringFromThirdWordOnwards(String title) {
//        if (!FragmentConstants.DEFAULT_STRING.equalsIgnoreCase(title)) {
//            title = title.substring(title.indexOf(" ", title.indexOf(" ") + 1));
//        }
//
//        return title.concat(":");
//    }

//    private void setSendButton() {
//        Bundle bundle = this.getArguments();
//
//        if (bundle.getString(FragmentConstants.KEY_SITREP) != null) {
//            if (FragmentConstants.VALUE_SITREP_VIEW.equalsIgnoreCase
//                    (bundle.getString(FragmentConstants.KEY_SITREP))) {
//                mSendButton.setVisibility(View.GONE);
//            } else {
//                mSendButton.setVisibility(View.VISIBLE);
//            }
//        }
//    }

//    private View.OnClickListener onEditClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            SitRepFragment sitRepFragment = (SitRepFragment) MainStatePagerAdapter.getPageReferenceMap().
//                    get(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID);
//
//            if (sitRepFragment != null) {
//                publishSitRepAdd();
//
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                fragmentManager.popBackStack();
//            }
//        }
//    };

//    private void publishSitRepAdd() {
//        Bundle bundle = this.getArguments();
//        int numberOfSitReps = bundle.getInt(
//                FragmentConstants.KEY_SITREP_TOTAL_NUMBER, FragmentConstants.DEFAULT_INT);
//
//        JSONObject newSitRepJSON = new JSONObject();
//
//        try {
//            newSitRepJSON.put("key", FragmentConstants.KEY_SITREP_ADD);
//            newSitRepJSON.put("id", String.valueOf(numberOfSitReps));
//            newSitRepJSON.put("reporter", SharedPreferenceUtil.getCurrentUserCallsignID(getActivity()));
//            newSitRepJSON.put("reporterAvatarId", "default");
//            newSitRepJSON.put("location", mLocation);
//            newSitRepJSON.put("activity", mActivity);
//            newSitRepJSON.put("personnel_t", mPersonnelT);
//            newSitRepJSON.put("personnel_s", mPersonnelS);
//            newSitRepJSON.put("personnel_d", mPersonnelD);
//            newSitRepJSON.put("next_coa", mNextCoa);
//            newSitRepJSON.put("request", mRequest);
//            newSitRepJSON.put("date", String.valueOf(Calendar.getInstance().getTime()));
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
////        MqttHelper.getInstance().publishMessage(newSitRepJSON.toString());
//        RabbitMQHelper.getInstance().sendMessage(newSitRepJSON.toString());
//    }
}
