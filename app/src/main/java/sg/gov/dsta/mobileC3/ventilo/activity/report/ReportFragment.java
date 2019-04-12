package sg.gov.dsta.mobileC3.ventilo.activity.report;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TabLayout.OnTabSelectedListener;
import android.support.design.widget.TabLayout.Tab;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sg.gov.dsta.mobileC3.ventilo.NoSwipeViewPager;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;

public class ReportFragment extends Fragment {

    private String mPreviousTagName;
    private boolean mIsVisibleToUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_report, container, false);

        final NoSwipeViewPager noSwipeViewPager = rootView.findViewById(R.id.viewpager_report);
        noSwipeViewPager.setPagingEnabled(false);

        ReportStatePagerAdapter wallStatePagerAdapter = new ReportStatePagerAdapter(
                getChildFragmentManager());
        noSwipeViewPager.setAdapter(wallStatePagerAdapter);

        TabLayout tabLayout = rootView.findViewById(R.id.tab_layout_report_tabs);
        tabLayout.setupWithViewPager(noSwipeViewPager);
//        tabLayout.getTabAt(FragmentConstants.REPORT_TAB_TITLE_INCIDENT_ID).
//                setText(getString(R.string.tab_layout_tabs_incident_title));
//        tabLayout.getTabAt(FragmentConstants.REPORT_TAB_TITLE_TASK_ID).
//                setText(getString(R.string.tab_layout_tabs_task_title));
        tabLayout.getTabAt(FragmentConstants.REPORT_TAB_TITLE_SITREP_ID).
                setText(getString(R.string.tab_layout_tabs_sitrep_title));

//        tabLayout.getTabAt(FragmentConstants.REPORT_TAB_TITLE_INCIDENT_ID).
//                setTag(getString(R.string.tab_layout_tabs_incident_title));
//        tabLayout.getTabAt(FragmentConstants.REPORT_TAB_TITLE_TASK_ID).
//                setTag(getString(R.string.tab_layout_tabs_task_title));
        tabLayout.getTabAt(FragmentConstants.REPORT_TAB_TITLE_SITREP_ID).
                setTag(getString(R.string.tab_layout_tabs_sitrep_title));

//        tabLayout.addOnTabSelectedListener(onTabSelectedListener);
//        mPreviousTagName = getString(R.string.tab_layout_tabs_incident_title);

        return rootView;
    }

//    private OnTabSelectedListener onTabSelectedListener = new OnTabSelectedListener() {
//
//        @Override
//        public void onTabSelected(Tab tab) {
//            if (!mPreviousTagName.equalsIgnoreCase(tab.getTag().toString())) {
//                onInvisible();
//                mPreviousTagName = tab.getTag().toString();
//            }
//        }
//
//        @Override
//        public void onTabUnselected(Tab tab) {
//
//        }
//
//        @Override
//        public void onTabReselected(Tab tab) {
//
//        }
//    };

//    private void onVisible() {
//
//    }

    // Remove all fragments from back stack once this fragment is invisible (user navigates to other tabs)
//    private void onInvisible() {
//        int count = getActivity().getSupportFragmentManager().getBackStackEntryCount();
//        if (count > 0) {
//            getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
//        }
//    }

    @Override
    public void onPause() {
        super.onPause();

//        if (mIsVisibleToUser) {
//            onInvisible();
//        }
    }

    @Override
    public void onStart() {
        super.onStart();

//        if (mIsVisibleToUser) {
//            onVisible();
//        }
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        mIsVisibleToUser = isVisibleToUser;
//        if (isResumed()) { // fragment has been created at this point
//            if (mIsVisibleToUser) {
//                onVisible();
//            } else {
//                onInvisible();
//            }
//        }
//    }
}
