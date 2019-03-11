package sg.gov.dsta.mobileC3.ventilo.activity.report;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import sg.gov.dsta.mobileC3.ventilo.NoSwipeViewPager;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;

public class ReportFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_report, container, false);

        final NoSwipeViewPager noSwipeViewPager = rootView.findViewById(R.id.viewpager_report);
        noSwipeViewPager.setPagingEnabled(false);

//        mToolbarTitleTextView.setText(getString(R.string.toolbar_wall_title));
//        mToolbarLeftActivityLinkTextView.setText(getString(R.string.toolbar_wall_tv_share));

        ReportStatePagerAdapter wallStatePagerAdapter = new ReportStatePagerAdapter(
                getChildFragmentManager());
        noSwipeViewPager.setAdapter(wallStatePagerAdapter);

        TabLayout tabLayout = rootView.findViewById(R.id.tab_layout_report_tabs);
        tabLayout.setupWithViewPager(noSwipeViewPager);
        tabLayout.getTabAt(FragmentConstants.REPORT_TAB_TITLE_INCIDENT_ID).
                setText(getString(R.string.tab_layout_tabs_incident_title));
        tabLayout.getTabAt(FragmentConstants.REPORT_TAB_TITLE_TASK_ID).
                setText(getString(R.string.tab_layout_tabs_task_title));
        tabLayout.getTabAt(FragmentConstants.REPORT_TAB_TITLE_SITREP_ID).
                setText(getString(R.string.tab_layout_tabs_sitrep_title));

        return rootView;
    }
}
