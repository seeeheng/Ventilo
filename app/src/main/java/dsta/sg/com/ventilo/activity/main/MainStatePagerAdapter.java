package dsta.sg.com.ventilo.activity.main;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import dsta.sg.com.ventilo.activity.map.MapFragment;
import dsta.sg.com.ventilo.activity.timeline.TimelineFragment;
import dsta.sg.com.ventilo.activity.videostream.VideoStreamFragment;
import dsta.sg.com.ventilo.util.constant.MainNavigationConstants;

public class MainStatePagerAdapter extends FragmentStatePagerAdapter {

    public MainStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;

        switch (i) {
            case MainNavigationConstants.BTM_NAV_MENU_MAP_POSITION_ID:
                fragment = new MapFragment();
                break;
            case MainNavigationConstants.BTM_NAV_MENU_TIMELINE_POSITION_ID:
//                fragment = new MapFragment();
                fragment = new TimelineFragment();
                break;
            case MainNavigationConstants.BTM_NAV_MENU_REPORT_POSITION_ID:
//                fragment = new VideoStreamFragment();
                fragment = new TimelineFragment();
                break;
            case MainNavigationConstants.BTM_NAV_MENU_NOTIFICATION_POSITION_ID:
                fragment = new TimelineFragment();
                break;
            case MainNavigationConstants.BTM_NAV_MENU_STREAM_POSITION_ID:
                fragment = new VideoStreamFragment();
                break;
            default:
                fragment = new TimelineFragment();
                break;
        }

//        if (i != 1) {
//            fragment = new DashboardFragment();
//        } else {
//            fragment = new HomeFragment();
//        }
//        Bundle args = new Bundle();
        // Our object is just an integer :-P
//        args.putInt(DemoObjectFragment.ARG_OBJECT, i + 1);
//        fragment.setArguments(args);
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
}
