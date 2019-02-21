package sg.gov.dsta.mobileC3.ventilo.activity.report;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;

import java.util.HashMap;

import sg.gov.dsta.mobileC3.ventilo.activity.report.incident.IncidentInnerDetailFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.report.incident.IncidentInnerFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.report.sitrep.SitRepInnerFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.report.task.TaskInnerFragment;
import sg.gov.dsta.mobileC3.ventilo.util.constant.ReportFragmentConstants;

public class ReportStatePagerAdapter extends FragmentStatePagerAdapter {

    private static HashMap<Integer, Fragment> mHashMapPageReference;

    public ReportStatePagerAdapter(FragmentManager fm) {
        super(fm);
        mHashMapPageReference = new HashMap<>();
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;

        switch (i) {
            case ReportFragmentConstants.REPORT_TAB_TITLE_INCIDENT_ID:
                fragment = new IncidentInnerFragment();
                mHashMapPageReference.put(ReportFragmentConstants.REPORT_TAB_TITLE_INCIDENT_ID,
                        fragment);
                break;
            case ReportFragmentConstants.REPORT_TAB_TITLE_TASK_ID:
                fragment = new TaskInnerFragment();
                mHashMapPageReference.put(ReportFragmentConstants.REPORT_TAB_TITLE_TASK_ID,
                        fragment);
                break;
            case ReportFragmentConstants.REPORT_TAB_TITLE_SITREP_ID:
                fragment = new SitRepInnerFragment();
                mHashMapPageReference.put(ReportFragmentConstants.REPORT_TAB_TITLE_SITREP_ID,
                        fragment);
                break;
            default:
                fragment = new IncidentInnerDetailFragment();
                mHashMapPageReference.put(ReportFragmentConstants.REPORT_TAB_TITLE_INCIDENT_ID,
                        fragment);
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }

    public static HashMap<Integer, Fragment> getPageReferenceMap() {
        return mHashMapPageReference;
    }

}
