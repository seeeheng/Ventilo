package sg.gov.dsta.mobileC3.ventilo.activity.report.sitrep;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.ReportFragmentConstants;

public class SitRepInnerDetailFragment extends Fragment {

    private static final String LINE_SEPARATOR = System.getProperty ("line.separator");
    private static final String SPACE = " ";
    private C2LatoRegularTextView mTvReport;

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
        setReportInfo();
    }

    private void setReportInfo() {
        Bundle bundle = this.getArguments();

        String locationInfo = getStringFromThirdWordOnwards(ReportFragmentConstants.KEY_SITREP_LOCATION).
                concat(SPACE).concat(bundle.getString(ReportFragmentConstants.KEY_SITREP_LOCATION,
                ReportFragmentConstants.DEFAULT_STRING));
        String activityInfo = getStringFromThirdWordOnwards(ReportFragmentConstants.KEY_SITREP_ACTIVITY).
                concat(SPACE).concat(bundle.getString(ReportFragmentConstants.KEY_SITREP_ACTIVITY,
                ReportFragmentConstants.DEFAULT_STRING));
        String personnelTInfo = getStringFromThirdWordOnwards(ReportFragmentConstants.KEY_SITREP_PERSONNEL_T).
                concat(SPACE).concat(bundle.getString(ReportFragmentConstants.KEY_SITREP_PERSONNEL_T,
                ReportFragmentConstants.DEFAULT_STRING));
        String personnelSInfo = getStringFromThirdWordOnwards(ReportFragmentConstants.KEY_SITREP_PERSONNEL_S).
                concat(SPACE).concat(bundle.getString(ReportFragmentConstants.KEY_SITREP_PERSONNEL_S,
                ReportFragmentConstants.DEFAULT_STRING));
        String personnelDInfo = getStringFromThirdWordOnwards(ReportFragmentConstants.KEY_SITREP_PERSONNEL_D).
                concat(SPACE).concat(bundle.getString(ReportFragmentConstants.KEY_SITREP_PERSONNEL_D,
                ReportFragmentConstants.DEFAULT_STRING));
        String nextCoaInfo = getStringFromThirdWordOnwards(ReportFragmentConstants.KEY_SITREP_NEXT_COA).
                concat(SPACE).concat(bundle.getString(ReportFragmentConstants.KEY_SITREP_NEXT_COA,
                ReportFragmentConstants.DEFAULT_STRING));
        String requestInfo = getStringFromThirdWordOnwards(ReportFragmentConstants.KEY_SITREP_REQUEST).
                concat(SPACE).concat(bundle.getString(ReportFragmentConstants.KEY_SITREP_REQUEST,
                ReportFragmentConstants.DEFAULT_STRING));

        String reportInfo = locationInfo + LINE_SEPARATOR + activityInfo + LINE_SEPARATOR
                + personnelTInfo + LINE_SEPARATOR + personnelSInfo + LINE_SEPARATOR
                + personnelDInfo + LINE_SEPARATOR + nextCoaInfo + LINE_SEPARATOR + requestInfo;

        mTvReport.setText(reportInfo);
    }

    private String getStringFromThirdWordOnwards(String title) {
        if (!ReportFragmentConstants.DEFAULT_STRING.equalsIgnoreCase(title)) {
            title = title.substring(title.indexOf(" ", title.indexOf(" ") + 1));
        }

        return title.concat(":");
    }
}
