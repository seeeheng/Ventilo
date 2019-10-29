package sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.sitRepPersonnelStatus;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import java.util.stream.Collectors;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.enums.EIsValid;
import timber.log.Timber;

public class DashboardSitRepPersonnelStatusFragment extends Fragment {

    private static final String TAG = DashboardSitRepPersonnelStatusFragment.class.getSimpleName();

    // View Models
//    private UserViewModel mUserViewModel;
    private SitRepViewModel mSitRepViewModel;

    // Main layout
    private View mRootView;

    // UI components
    private C2OpenSansRegularTextView mTvPersonnelStatusTotalT;
    private C2OpenSansRegularTextView mTvPersonnelStatusTotalS;
    private C2OpenSansRegularTextView mTvPersonnelStatusTotalD;

    // Recycler View
    private RecyclerView mRecyclerView;
    private DashboardSitRepPersonnelStatusRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private List<SitRepModel> mSitRepListItems;

    private boolean mIsFragmentVisibleToUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        setRetainInstance(true);
        observerSetup();

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_dashboard_sitrep_personnel_status, container, false);
            initUI(mRootView);
        }

        return mRootView;
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

        mRecyclerLayoutManager = new LinearLayoutManager(getParentFragment().getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        if (mSitRepListItems == null) {
            mSitRepListItems = new ArrayList<>();
        }

        mRecyclerAdapter = new
                DashboardSitRepPersonnelStatusRecyclerAdapter(getParentFragment().getContext(), mSitRepListItems);
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
     * Obtain all VALID (not deleted) Sit Rep Models from database
     * @param sitRepModelList
     * @return
     */
    private List<SitRepModel> getAllValidSitRepListFromDatabase(List<SitRepModel> sitRepModelList) {

        List<SitRepModel> validSitRepModelList = sitRepModelList.stream().
                filter(sitRepModel -> EIsValid.YES.toString().
                        equalsIgnoreCase(sitRepModel.getIsValid())).
                collect(Collectors.toList());

        return validSitRepModelList;
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        if (getParentFragment() != null) {
            mSitRepViewModel = ViewModelProviders.of(getParentFragment()).get(SitRepViewModel.class);

            /*
             * Refreshes recyclerview UI whenever there is a change in Sit Rep (insert, update or delete)
             */
            mSitRepViewModel.getAllSitRepsLiveData().observe(getParentFragment(), new Observer<List<SitRepModel>>() {
                @Override
                public void onChanged(@Nullable List<SitRepModel> sitRepModelList) {

                    synchronized (mSitRepListItems) {

                        if (sitRepModelList != null) {
                            sitRepModelList = getAllValidSitRepListFromDatabase(sitRepModelList);

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
                    }
                }
            });
        }
    }

    public void onVisible() {
        Log.d(TAG, "onVisible");
        observerSetup();

        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void onInvisible() {
        Log.d(TAG, "onInvisible");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;

        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                Log.d(TAG, "setUserVisibleHint onVisible");
                onVisible();
            } else {
                Log.d(TAG, "setUserVisibleHint onInvisible");
                onInvisible();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }
}
