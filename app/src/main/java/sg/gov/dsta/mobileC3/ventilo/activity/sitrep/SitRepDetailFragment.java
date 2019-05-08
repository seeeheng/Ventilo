package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.greenrobot.eventbus.EventBus;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.PhotoCaptureUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.ImageClass;

public class SitRepDetailFragment extends Fragment {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    // Toolbar section
    private LinearLayout mLinearLayoutBtnEdit;
    private C2OpenSansSemiBoldTextView mTvToolbarEdit;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sitrep_detail, container, false);
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
        refreshUI();
    }

    private void initToolbarUI(View rootView) {
        View layoutToolbar = rootView.findViewById(R.id.layout_toolbar_sitrep_text_left_text_right);
        LinearLayout linearLayoutBtnBack = layoutToolbar.findViewById(R.id.layout_toolbar_top_left_btn);
        linearLayoutBtnBack.setOnClickListener(onBackClickListener);

        mLinearLayoutBtnEdit = layoutToolbar.findViewById(R.id.layout_toolbar_top_right_btn);
        mLinearLayoutBtnEdit.setEnabled(false);
        mLinearLayoutBtnEdit.setOnClickListener(onEditClickListener);

        mTvToolbarEdit = layoutToolbar.findViewById(R.id.toolbar_top_right_btn_text);
        mTvToolbarEdit.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));
        mTvToolbarEdit.setText(getString(R.string.btn_edit));
    }

    private void refreshUI() {
        setSitRepInfo();
//        setSendButton();
    }

    private void setSitRepInfo() {
        SitRepModel sitRepModel = EventBus.getDefault().removeStickyEvent(SitRepModel.class);

        if (sitRepModel != null) {
            System.out.println("setSitRepInfo sitRepModel not null");
            // Team Name
            StringBuilder teamNameHeaderStringBuilder = new StringBuilder();
            teamNameHeaderStringBuilder.append("Team");
            teamNameHeaderStringBuilder.append(StringUtil.SPACE);

            if (sitRepModel.getReporter() != null) {
                teamNameHeaderStringBuilder.append(sitRepModel.getReporter().trim());
            }

            teamNameHeaderStringBuilder.append("SITREP");
            mTvTeamNameHeader.setText(teamNameHeaderStringBuilder.toString());

            // Assigned team
            StringBuilder reporterStringBuilder = new StringBuilder();
            reporterStringBuilder.append("Team");
            reporterStringBuilder.append(StringUtil.SPACE);

            if (sitRepModel.getReporter() != null) {
                reporterStringBuilder.append(sitRepModel.getReporter().trim());
            }

            mTvReporter.setText(reporterStringBuilder.toString());

            // Reported date/time
            StringBuilder reportedDateTimeStringBuilder = new StringBuilder();
            String reportedDateTime = sitRepModel.getReportedDateTime();
            String reportedDateTimeInCustomStrFormat = DateTimeUtil.dateToCustomStringFormat(
                    DateTimeUtil.stringToDate(reportedDateTime));

            if (sitRepModel.getReportedDateTime() != null) {
                reportedDateTimeStringBuilder.append(reportedDateTimeInCustomStrFormat);
                reportedDateTimeStringBuilder.append("\t");
                reportedDateTimeStringBuilder.append("\t");
                reportedDateTimeStringBuilder.append("\t");
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

//        Bundle bundle = this.getArguments();
//
//        if (!bundle.isEmpty()) {
//
//            mLocation = bundle.getString(FragmentConstants.KEY_SITREP_LOCATION,
//                    FragmentConstants.DEFAULT_STRING);
//            String locationInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_LOCATION).
//                    concat(SPACE).concat(mLocation);
//
//            mActivity = bundle.getString(FragmentConstants.KEY_SITREP_ACTIVITY,
//                    FragmentConstants.DEFAULT_STRING);
//            String activityInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_ACTIVITY).
//                    concat(SPACE).concat(mActivity);
//
//            mPersonnelT = bundle.getInt(FragmentConstants.KEY_SITREP_PERSONNEL_T,
//                    FragmentConstants.DEFAULT_INT);
//            String personnelTInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_PERSONNEL_T).
//                    concat(SPACE).concat(String.valueOf(mPersonnelT));
//
//            mPersonnelS = bundle.getInt(FragmentConstants.KEY_SITREP_PERSONNEL_S,
//                    FragmentConstants.DEFAULT_INT);
//            String personnelSInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_PERSONNEL_S).
//                    concat(SPACE).concat(String.valueOf(mPersonnelS));
//
//            mPersonnelD = bundle.getInt(FragmentConstants.KEY_SITREP_PERSONNEL_D,
//                    FragmentConstants.DEFAULT_INT);
//            String personnelDInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_PERSONNEL_D).
//                    concat(SPACE).concat(String.valueOf(mPersonnelD));
//
//            mNextCoa = bundle.getString(FragmentConstants.KEY_SITREP_NEXT_COA,
//                    FragmentConstants.DEFAULT_STRING);
//            String nextCoaInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_NEXT_COA).
//                    concat(SPACE).concat(mNextCoa);
//
//            mRequest = bundle.getString(FragmentConstants.KEY_SITREP_REQUEST,
//                    FragmentConstants.DEFAULT_STRING);
//            String requestInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_REQUEST).
//                    concat(SPACE).concat(mRequest);
//
//            String reportInfo = locationInfo + LINE_SEPARATOR + activityInfo + LINE_SEPARATOR
//                    + personnelTInfo + LINE_SEPARATOR + personnelSInfo + LINE_SEPARATOR
//                    + personnelDInfo + LINE_SEPARATOR + nextCoaInfo + LINE_SEPARATOR + requestInfo;
//
//            mTvReport.setText(reportInfo);
//        } else {
//            mTvReport.setText("Please return to the previous page and enter Sit Rep information again.");
//        }
//        }
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    };

    private View.OnClickListener onEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

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
