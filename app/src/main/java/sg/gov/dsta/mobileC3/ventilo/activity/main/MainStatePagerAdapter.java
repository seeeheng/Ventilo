package sg.gov.dsta.mobileC3.ventilo.activity.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;

import java.util.HashMap;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.map.MapFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.map.MapShipBlueprintFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.report.ReportFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.timeline.TimelineFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.videostream.VideoStreamFragment;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.ReportFragmentConstants;

public class MainStatePagerAdapter extends FragmentStatePagerAdapter {

    private static HashMap<Integer, Fragment> mHashMapPageReference;

    public MainStatePagerAdapter(FragmentManager fm) {
        super(fm);
        mHashMapPageReference = new HashMap<>();
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;

        switch (i) {
            case MainNavigationConstants.BTM_NAV_MENU_MAP_POSITION_ID:
                fragment = new MapFragment();
                mHashMapPageReference.put(MainNavigationConstants.BTM_NAV_MENU_MAP_POSITION_ID,
                        fragment);
                break;
            case MainNavigationConstants.BTM_NAV_MENU_TIMELINE_POSITION_ID:
                fragment = new TimelineFragment();
                mHashMapPageReference.put(MainNavigationConstants.BTM_NAV_MENU_TIMELINE_POSITION_ID,
                        fragment);
                break;
            case MainNavigationConstants.BTM_NAV_MENU_REPORT_POSITION_ID:
                fragment = new ReportFragment();
                mHashMapPageReference.put(MainNavigationConstants.BTM_NAV_MENU_REPORT_POSITION_ID,
                        fragment);
                break;
            case MainNavigationConstants.BTM_NAV_MENU_NOTIFICATION_POSITION_ID:
                fragment = new TimelineFragment();
                mHashMapPageReference.put(MainNavigationConstants.BTM_NAV_MENU_NOTIFICATION_POSITION_ID,
                        fragment);
                break;
            case MainNavigationConstants.BTM_NAV_MENU_STREAM_POSITION_ID:
                fragment = new VideoStreamFragment();
                mHashMapPageReference.put(MainNavigationConstants.BTM_NAV_MENU_STREAM_POSITION_ID,
                        fragment);
                break;
            default:
                fragment = new TimelineFragment();
                mHashMapPageReference.put(MainNavigationConstants.BTM_NAV_MENU_TIMELINE_POSITION_ID,
                        fragment);
                break;
        }

        return fragment;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "OBJECT " + (position + 1);
    }

    public static HashMap<Integer, Fragment> getPageReferenceMap() {
        return mHashMapPageReference;
    }
}
