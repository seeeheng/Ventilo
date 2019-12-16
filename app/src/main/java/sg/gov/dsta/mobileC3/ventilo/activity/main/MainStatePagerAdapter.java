package sg.gov.dsta.mobileC3.ventilo.activity.main;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import android.util.SparseArray;
import android.view.ViewGroup;

import sg.gov.dsta.mobileC3.ventilo.R;
//import sg.gov.dsta.mobileC3.ventilo.activity.map.MapShipBlueprintFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.map.MapShipBlueprintFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.radiolinkstatus.RadioLinkStatusFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.sitrep.SitRepFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.task.TaskFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.timeline.TimelineFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.user.UserSettingsFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.videostream.VideoStreamFragment;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;

public class MainStatePagerAdapter extends FragmentStatePagerAdapter {

    private Context mContext;
    private SparseArray<Fragment> mPageFragmentReferenceHashMap;
    private SparseArray<String> mPageNameReferenceHashMap;

    public MainStatePagerAdapter(FragmentManager fm, Context context) {
        super(fm, FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
        mPageFragmentReferenceHashMap = new SparseArray<>();
        mPageNameReferenceHashMap = new SparseArray<>();

        populatePageNameReferenceData();
    }

    private void populatePageNameReferenceData() {
        mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID,
                mContext.getResources().getString(R.string.map_page_title));
        mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID,
                mContext.getResources().getString(R.string.video_stream_page_title));
        mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID,
                mContext.getResources().getString(R.string.sitrep_page_title));
        mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID,
                mContext.getResources().getString(R.string.timeline_page_title));
        mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID,
                mContext.getResources().getString(R.string.task_page_title));
        mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
                mContext.getResources().getString(R.string.radio_link_status_page_title));
        mPageNameReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID,
                mContext.getResources().getString(R.string.settings_label));
    }

    @Override
    public Fragment getItem(int i) {
        Fragment fragment;

        switch (i) {
            case MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID:
                if (mPageFragmentReferenceHashMap.get(
                        MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID) != null) {
                    fragment = mPageFragmentReferenceHashMap.get(
                            MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID);
                } else {
                    fragment = new MapShipBlueprintFragment();

                    mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_MAP_POSITION_ID,
                            fragment);
                }

                break;

            case MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID:

                if (mPageFragmentReferenceHashMap.get(
                        MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID) != null) {
                    fragment = mPageFragmentReferenceHashMap.get(
                            MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID);
                } else {
                    fragment = new VideoStreamFragment();

                    mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID,
                            fragment);
                }

                break;

            case MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID:

                if (mPageFragmentReferenceHashMap.get(
                        MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID) != null) {
                    fragment = mPageFragmentReferenceHashMap.get(
                            MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID);

                } else {
                    fragment = new SitRepFragment();

                    mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_SITREP_POSITION_ID,
                            fragment);

                }

                break;

            case MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID:

                if (mPageFragmentReferenceHashMap.get(
                        MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID) != null) {
                    fragment = mPageFragmentReferenceHashMap.get(
                            MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID);
                } else {
                    fragment = new TimelineFragment();

                    mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_TIMELINE_POSITION_ID,
                            fragment);
                }

                break;

            case MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID:

                if (mPageFragmentReferenceHashMap.get(
                        MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID) != null) {
                    fragment = mPageFragmentReferenceHashMap.get(
                            MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID);
                } else {
                    fragment = new TaskFragment();

                    mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID,
                            fragment);
                }

                break;

            case MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID:

                if (mPageFragmentReferenceHashMap.get(
                        MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID) != null) {
                    fragment = mPageFragmentReferenceHashMap.get(
                            MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID);
                } else {
                    fragment = new RadioLinkStatusFragment();

                    mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
                            fragment);
                }

                break;

            case MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID:

                if (mPageFragmentReferenceHashMap.get(
                        MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID) != null) {
                    fragment = mPageFragmentReferenceHashMap.get(
                            MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID);
                } else {
                    fragment = new UserSettingsFragment();

                    mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_USER_SETTINGS_POSITION_ID,
                            fragment);
                }

                break;

            default:
                fragment = new RadioLinkStatusFragment();
                mPageFragmentReferenceHashMap.put(MainNavigationConstants.SIDE_MENU_TAB_RADIO_LINK_STATUS_POSITION_ID,
                        fragment);
                break;
        }

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

//    @Override
//    public void destroyItem(ViewGroup container, int position, Object object) {
//    }

    public SparseArray<Fragment> getPageReferenceMap() {
        return mPageFragmentReferenceHashMap;
    }

    public Fragment getFragment(int fragmentID) {
        return mPageFragmentReferenceHashMap.get(fragmentID);
    }
}
