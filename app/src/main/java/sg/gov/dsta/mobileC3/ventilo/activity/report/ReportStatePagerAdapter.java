package sg.gov.dsta.mobileC3.ventilo.activity.report;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.HashMap;

import sg.gov.dsta.mobileC3.ventilo.activity.report.sitrep.SitRepInnerFragment;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;

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
//            case FragmentConstants.REPORT_TAB_TITLE_INCIDENT_ID:
//                fragment = new IncidentInnerFragment();
//                mHashMapPageReference.put(FragmentConstants.REPORT_TAB_TITLE_INCIDENT_ID,
//                        fragment);
//                break;
//            case FragmentConstants.REPORT_TAB_TITLE_TASK_ID:
//                fragment = new TaskFragment();
//                mHashMapPageReference.put(FragmentConstants.REPORT_TAB_TITLE_TASK_ID,
//                        fragment);
//                break;
            case FragmentConstants.REPORT_TAB_TITLE_SITREP_ID:
                fragment = new SitRepInnerFragment();
                mHashMapPageReference.put(FragmentConstants.REPORT_TAB_TITLE_SITREP_ID,
                        fragment);
                break;
            default:
                fragment = new SitRepInnerFragment();
                mHashMapPageReference.put(FragmentConstants.REPORT_TAB_TITLE_SITREP_ID,
                        fragment);
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }

    public static HashMap<Integer, Fragment> getPageReferenceMap() {
        return mHashMapPageReference;
    }

}
