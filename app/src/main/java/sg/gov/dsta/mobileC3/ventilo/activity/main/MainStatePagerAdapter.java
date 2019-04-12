package sg.gov.dsta.mobileC3.ventilo.activity.main;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;

import java.util.HashMap;
import java.util.Map;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.map.MapFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.radiolinkstatus.RadioLinkStatusFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.report.ReportFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.task.TaskFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.timeline.TimelineFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.videostream.VideoStreamFragment;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;

public class MainStatePagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private FragmentManager mFm;
    private static HashMap<Integer, Fragment> mPageFragmentReferenceHashMap;
    private static HashMap<Integer, String> mPageNameReferenceHashMap;

    public MainStatePagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mFm = fm;
        mContext = context;
        mPageFragmentReferenceHashMap = new HashMap<>();
        mPageNameReferenceHashMap = new HashMap<>();
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;
        FragmentTransaction ft = mFm.beginTransaction();

        switch (i) {
            case MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID:
                fragment = new MapFragment();
                mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID,
                        fragment);
                mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID,
                        mContext.getResources().getString(R.string.map_page_title));
                break;
            case MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID:
                fragment = new VideoStreamFragment();
                mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID,
                        fragment);
                mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID,
                        mContext.getResources().getString(R.string.video_stream_page_title));
                break;
            case MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID:
                fragment = new ReportFragment();
                mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID,
                        fragment);
                mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID,
                        mContext.getResources().getString(R.string.sitrep_page_title));
                break;
            case MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID:
                fragment = new TimelineFragment();
                mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID,
                        fragment);
                mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID,
                        mContext.getResources().getString(R.string.timeline_page_title));
                break;
            case MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID:
                fragment = new TaskFragment();
                mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID,
                        fragment);
                mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID,
                        mContext.getResources().getString(R.string.task_page_title));
                break;
            case MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID:
                fragment = new RadioLinkStatusFragment();
                mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
                        fragment);
                mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
                        mContext.getResources().getString(R.string.radio_link_status_page_title));
                break;
            default:
                fragment = new RadioLinkStatusFragment();
                mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
                        fragment);
                mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
                        mContext.getResources().getString(R.string.radio_link_status_page_title));
                break;
        }


        ft.addToBackStack(fragment.getClass().getSimpleName());
        ft.commit();

        return fragment;
    }

    @Override
    public int getCount() {
        return MainNavigationConstants.SIDE_MENU_TAB_TOTAL_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mPageNameReferenceHashMap.get(position);
    }

    public static HashMap<Integer, Fragment> getPageReferenceMap() {
        return mPageFragmentReferenceHashMap;
    }
}
