package sg.gov.dsta.mobileC3.ventilo.activity.report.sitrep;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.report.ReportStatePagerAdapter;
import sg.gov.dsta.mobileC3.ventilo.helper.RabbitMQHelper;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBlackButton;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;

public class SitRepInnerDetailFragment extends Fragment {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final String SPACE = " ";

    private C2OpenSansRegularTextView mTvReport;
    private C2OpenSansBlackButton mSendButton;

    private String mLocation;
    private String mActivity;
    private int mPersonnelT;
    private int mPersonnelS;
    private int mPersonnelD;
    private String mNextCoa;
    private String mRequest;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inner_sitrep_detail, container, false);
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        mTvReport = rootView.findViewById(R.id.tv_sitrep_detail_report);
        mSendButton = rootView.findViewById(R.id.btn_sitrep_detail_send);
        mSendButton.setOnClickListener(onSendClickListener);
        refreshUI();
    }

    private void refreshUI() {
        setReportInfo();
        setSendButton();
    }

    private void setReportInfo() {
        Bundle bundle = this.getArguments();

        if (!bundle.isEmpty()) {
            mLocation = bundle.getString(FragmentConstants.KEY_SITREP_LOCATION,
                    FragmentConstants.DEFAULT_STRING);
            String locationInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_LOCATION).
                    concat(SPACE).concat(mLocation);

            mActivity = bundle.getString(FragmentConstants.KEY_SITREP_ACTIVITY,
                    FragmentConstants.DEFAULT_STRING);
            String activityInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_ACTIVITY).
                    concat(SPACE).concat(mActivity);

            mPersonnelT = bundle.getInt(FragmentConstants.KEY_SITREP_PERSONNEL_T,
                    FragmentConstants.DEFAULT_INT);
            String personnelTInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_PERSONNEL_T).
                    concat(SPACE).concat(String.valueOf(mPersonnelT));

            mPersonnelS = bundle.getInt(FragmentConstants.KEY_SITREP_PERSONNEL_S,
                    FragmentConstants.DEFAULT_INT);
            String personnelSInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_PERSONNEL_S).
                    concat(SPACE).concat(String.valueOf(mPersonnelS));

            mPersonnelD = bundle.getInt(FragmentConstants.KEY_SITREP_PERSONNEL_D,
                    FragmentConstants.DEFAULT_INT);
            String personnelDInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_PERSONNEL_D).
                    concat(SPACE).concat(String.valueOf(mPersonnelD));

            mNextCoa = bundle.getString(FragmentConstants.KEY_SITREP_NEXT_COA,
                    FragmentConstants.DEFAULT_STRING);
            String nextCoaInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_NEXT_COA).
                    concat(SPACE).concat(mNextCoa);

            mRequest = bundle.getString(FragmentConstants.KEY_SITREP_REQUEST,
                    FragmentConstants.DEFAULT_STRING);
            String requestInfo = getStringFromThirdWordOnwards(FragmentConstants.KEY_SITREP_REQUEST).
                    concat(SPACE).concat(mRequest);

            String reportInfo = locationInfo + LINE_SEPARATOR + activityInfo + LINE_SEPARATOR
                    + personnelTInfo + LINE_SEPARATOR + personnelSInfo + LINE_SEPARATOR
                    + personnelDInfo + LINE_SEPARATOR + nextCoaInfo + LINE_SEPARATOR + requestInfo;

            mTvReport.setText(reportInfo);
        } else {
            mTvReport.setText("Please return to the previous page and enter Sit Rep information again.");
        }
    }

    private String getStringFromThirdWordOnwards(String title) {
        if (!FragmentConstants.DEFAULT_STRING.equalsIgnoreCase(title)) {
            title = title.substring(title.indexOf(" ", title.indexOf(" ") + 1));
        }

        return title.concat(":");
    }

    private void setSendButton() {
        Bundle bundle = this.getArguments();

        if (bundle.getString(FragmentConstants.KEY_SITREP) != null) {
            if (FragmentConstants.VALUE_SITREP_VIEW.equalsIgnoreCase
                    (bundle.getString(FragmentConstants.KEY_SITREP))) {
                mSendButton.setVisibility(View.GONE);
            } else {
                mSendButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private View.OnClickListener onSendClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            SitRepInnerFragment sitRepInnerFragment = (SitRepInnerFragment) ReportStatePagerAdapter.getPageReferenceMap().
                    get(FragmentConstants.REPORT_TAB_TITLE_SITREP_ID);

            if (sitRepInnerFragment != null) {
                publishSitRepAdd();

                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.popBackStack();
            }
        }
    };

    private void publishSitRepAdd() {
        Bundle bundle = this.getArguments();
        int numberOfSitReps = bundle.getInt(
                FragmentConstants.KEY_SITREP_TOTAL_NUMBER, FragmentConstants.DEFAULT_INT);

        JSONObject newSitRepJSON = new JSONObject();

        try {
            newSitRepJSON.put("key", FragmentConstants.KEY_SITREP_ADD);
            newSitRepJSON.put("id", String.valueOf(numberOfSitReps));
            newSitRepJSON.put("reporter", SharedPreferenceUtil.getCurrentUser(getActivity()));
            newSitRepJSON.put("reporterAvatarId", "default");
            newSitRepJSON.put("location", mLocation);
            newSitRepJSON.put("activity", mActivity);
            newSitRepJSON.put("personnel_t", mPersonnelT);
            newSitRepJSON.put("personnel_s", mPersonnelS);
            newSitRepJSON.put("personnel_d", mPersonnelD);
            newSitRepJSON.put("next_coa", mNextCoa);
            newSitRepJSON.put("request", mRequest);
            newSitRepJSON.put("date", String.valueOf(Calendar.getInstance().getTime()));

        } catch (JSONException e) {
            e.printStackTrace();
        }

//        MqttHelper.getInstance().publishMessage(newSitRepJSON.toString());
        RabbitMQHelper.getInstance().sendMessage(newSitRepJSON.toString());
    }

}
