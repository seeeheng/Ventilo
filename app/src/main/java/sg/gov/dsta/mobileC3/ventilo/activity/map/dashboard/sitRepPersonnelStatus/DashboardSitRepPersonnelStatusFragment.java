package sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.sitRepPersonnelStatus;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;

public class DashboardSitRepPersonnelStatusFragment extends Fragment {

    private static final String TAG = DashboardSitRepPersonnelStatusFragment.class.getSimpleName();

    // View Models
//    private UserViewModel mUserViewModel;
    private SitRepViewModel mSitRepViewModel;

    // UI components
    private C2OpenSansRegularTextView mTvPersonnelStatusTotalT;
    private C2OpenSansRegularTextView mTvPersonnelStatusTotalS;
    private C2OpenSansRegularTextView mTvPersonnelStatusTotalD;

    // Recycler View
    private RecyclerView mRecyclerView;
    private DashboardSitRepPersonnelStatusRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private List<SitRepModel> mSitRepListItems;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard_sitrep_personnel_status, container, false);

        observerSetup();
        initUI(rootView);

        return rootView;
    }

    /**
     * Initialise view with recycler data
     *
     * @param rootView
     */
    private void initUI(View rootView) {
        mTvPersonnelStatusTotalT = rootView.findViewById(R.id.tv_dashboard_sitrep_personnel_status_total_T);
        mTvPersonnelStatusTotalS = rootView.findViewById(R.id.tv_dashboard_sitrep_personnel_status_total_S);
        mTvPersonnelStatusTotalD = rootView.findViewById(R.id.tv_dashboard_sitrep_personnel_status_total_D);

        mRecyclerView = rootView.findViewById(R.id.recycler_dashboard_sitrep_personnel_status);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(true);

        mRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        if (mSitRepListItems == null) {
            mSitRepListItems = new ArrayList<>();
        }

        mRecyclerAdapter = new
                DashboardSitRepPersonnelStatusRecyclerAdapter(getContext(), mSitRepListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Refresh UI with updated data
     */
    private void refreshUI() {
        if (mSitRepListItems != null) {

            int totalPersonnelT = 0;
            int totalPersonnelS = 0;
            int totalPersonnelD = 0;
            for (int i = 0; i < mSitRepListItems.size(); i++) {
                totalPersonnelT += mSitRepListItems.get(i).getPersonnelT();
                totalPersonnelS += mSitRepListItems.get(i).getPersonnelS();
                totalPersonnelD += mSitRepListItems.get(i).getPersonnelD();
            }

            mTvPersonnelStatusTotalT.setText(String.valueOf(totalPersonnelT));
            mTvPersonnelStatusTotalS.setText(String.valueOf(totalPersonnelS));
            mTvPersonnelStatusTotalD.setText(String.valueOf(totalPersonnelD));
        }
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mSitRepViewModel = ViewModelProviders.of(this).get(SitRepViewModel.class);

        /*
         * Refreshes recyclerview UI whenever there is a change in Sit Rep (insert, update or delete)
         */
        mSitRepViewModel.getAllSitRepsLiveData().observe(this, new Observer<List<SitRepModel>>() {
            @Override
            public void onChanged(@Nullable List<SitRepModel> sitRepModelList) {
                Log.i(TAG, "New Live Data, sitRepModelList: " + sitRepModelList);

                if (mSitRepListItems == null) {
                    mSitRepListItems = new ArrayList<>();
                } else {
                    mSitRepListItems.clear();
                }

                if (sitRepModelList != null) {
                    mSitRepListItems.addAll(sitRepModelList);
                }

                if (mRecyclerAdapter != null) {
                    mRecyclerAdapter.setSitRepListItems(mSitRepListItems);
                }

                refreshUI();
            }
        });
    }

}

//    /**
//     * Pops back stack of ONLY current tab
//     *
//     * @return
//     */
//    public boolean popBackStack() {
//        if (!isAdded())
//            return false;
//
//        if(getChildFragmentManager().getBackStackEntryCount() > 0) {
//            getChildFragmentManager().popBackStackImmediate();
//            return true;
//        } else
//            return false;
//    }
//
//    private void onVisible() {
//        Log.d(TAG, "onVisible");
//
//        FragmentManager fm = getChildFragmentManager();
//        boolean isFragmentFound = false;
//
//        int count = fm.getBackStackEntryCount();
//
//        // Checks if current fragment exists in Back stack
//        for (int i = 0; i < count; i++) {
//            if (this.getClass().getSimpleName().equalsIgnoreCase(fm.getBackStackEntryAt(i).getName())) {
//                isFragmentFound = true;
//            }
//        }
//
//        // If not found, add to current fragment to Back stack
//        if (!isFragmentFound) {
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.addToBackStack(this.getClass().getSimpleName());
//            ft.commit();
//        }
//    }
//
//    private void onInvisible() {
//        Log.d(TAG, "onInvisible");
//    }
//
//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        mIsFragmentVisibleToUser = isVisibleToUser;
//
//        if (isResumed()) { // fragment has been created at this point
//            if (mIsFragmentVisibleToUser) {
//                Log.d(TAG, "setUserVisibleHint onVisible");
//                onVisible();
//            } else {
//                Log.d(TAG, "setUserVisibleHint onInvisible");
//                onInvisible();
//            }
//        }
//    }
//
//    @Override
//    public void onStop() {
//        super.onStop();
//
//        if (mIsFragmentVisibleToUser) {
//            onVisible();
//        }
//    }
//
//    @Override
//    public void onStart() {
//        super.onStart();
//
//        if (mIsFragmentVisibleToUser) {
//            onVisible();
//        }
//    }
//}
