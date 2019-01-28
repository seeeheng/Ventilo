package sg.gov.dsta.mobileC3.ventilo.activity.report;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import sg.gov.dsta.mobileC3.ventilo.activity.report.incident.IncidentInnerDetailFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.report.incident.IncidentInnerFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.report.sitrep.SitRepInnerFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.report.task.TaskInnerFragment;
import sg.gov.dsta.mobileC3.ventilo.util.constant.ReportFragmentConstants;

public class ReportStatePagerAdapter extends FragmentStatePagerAdapter {

    public ReportStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;

        switch (i) {
            case ReportFragmentConstants.REPORT_TAB_TITLE_INCIDENTS_ID:
                fragment = new IncidentInnerFragment();
                break;
            case ReportFragmentConstants.REPORT_TAB_TITLE_TASKS_ID:
                fragment = new TaskInnerFragment();
                break;
            case ReportFragmentConstants.REPORT_TAB_TITLE_SITREP_ID:
                fragment = new SitRepInnerFragment();
                break;
            default:
                fragment = new IncidentInnerDetailFragment();
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
}
