package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;

public class SitRepFragment extends Fragment {

    private static final String TAG = SitRepFragment.class.getSimpleName();

    // View Models
//    private UserViewModel mUserViewModel;
    private SitRepViewModel mSitRepViewModel;
//    private UserSitRepJoinViewModel mUserSitRepJoinViewModel;

    // Total count dashboard
    private View mLayoutTotalCountDashboard;
    private C2OpenSansSemiBoldTextView mTvTotalCountTitle;
    private C2OpenSansSemiBoldTextView mTvPersonnelTCountTitle;
    private C2OpenSansSemiBoldTextView mTvPersonnelSCountTitle;
    private C2OpenSansSemiBoldTextView mTvPersonnelDCountTitle;
    private C2OpenSansRegularTextView mTvTotalCountNumber;
    private C2OpenSansRegularTextView mTvPersonnelTCountNumber;
    private C2OpenSansRegularTextView mTvPersonnelSCountNumber;
    private C2OpenSansRegularTextView mTvPersonnelDCountNumber;

    // Add new item UI
    private View mLayoutAddNewItem;
    private C2OpenSansRegularTextView mTvAddNewItemCategory;

    private RecyclerView mRecyclerView;
    private SitRepRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private FloatingActionButton mFabAddSitRep;

    private List<SitRepModel> mSitRepListItems;

    private boolean mIsFragmentVisibleToUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sitrep, container, false);
        observerSetup();
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        mLayoutTotalCountDashboard = rootView.findViewById(R.id.layout_total_count_dashboard);
        initTotalCountDashboardUI(mLayoutTotalCountDashboard);

        mLayoutAddNewItem = rootView.findViewById(R.id.layout_sitrep_add_new_item);
//        mLayoutAddNewItem.setVisibility(View.GONE);
        mTvAddNewItemCategory = mLayoutAddNewItem.findViewById(R.id.tv_add_new_item_category);
        mTvAddNewItemCategory.setText(getString(R.string.add_new_item_sitrep));

        mRecyclerView = rootView.findViewById(R.id.recycler_sitrep);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        mRecyclerView.addOnItemTouchListener(new SitRepRecyclerItemTouchListener(getContext(), mRecyclerView, new SitRepRecyclerItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                navigateToSitRepDetailFragment(mSitRepListItems.get(position));
            }

            @Override
            public void onLongItemClick(View view, int position) {
                Toast.makeText(getContext(), "Sit Rep onItemLongClick" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeLeft(View view, int position) {
            }

            @Override
            public void onSwipeRight(View view, int position) {
            }
        }));

        // Set data for recycler view
        if (mSitRepListItems == null) {
            mSitRepListItems = new ArrayList<>();
        }
//        setUpRecyclerData();

        mRecyclerAdapter = new SitRepRecyclerAdapter(getContext(), mSitRepListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up floating action button
        mFabAddSitRep = rootView.findViewById(R.id.fab_sitrep_add);
        mFabAddSitRep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFabAddSitRep.setEnabled(false);

                Fragment sitRepAddUpdateFragment = new SitRepAddUpdateFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FragmentConstants.KEY_SITREP, FragmentConstants.VALUE_SITREP_ADD);
                sitRepAddUpdateFragment.setArguments(bundle);


                navigateToFragment(sitRepAddUpdateFragment);

//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                FragmentTransaction ft = fm.beginTransaction();
//                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
//                ft.replace(R.id.layout_sitrep_fragment, sitRepAddUpdateFragment, sitRepAddUpdateFragment.getClass().getSimpleName());
////                ft.addToBackStack(sitRepAddUpdateFragment.getClass().getSimpleName());
//                ft.addToBackStack(null);
//                ft.commit();

                mFabAddSitRep.setEnabled(true);
            }
        });

    }

    /**
     * Init dashboard UI displaying total count of Sit Rep Threat(T), Suspects(S) and Dead(D) personnel
     *
     * @param layoutTotalCountView
     */
    private void initTotalCountDashboardUI(View layoutTotalCountView) {
        mTvTotalCountTitle = layoutTotalCountView.findViewById(R.id.tv_total_count_title);
        mTvPersonnelTCountTitle = layoutTotalCountView.findViewById(R.id.tv_first_count_title);
        mTvPersonnelSCountTitle = layoutTotalCountView.findViewById(R.id.tv_second_count_title);
        mTvPersonnelDCountTitle = layoutTotalCountView.findViewById(R.id.tv_third_count_title);

        mTvTotalCountTitle.setText(getString(R.string.sitrep_total_count_title));
        mTvTotalCountTitle.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));

        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString personnelTitle = new SpannableString(getString(R.string.sitrep_personnel));
        personnelTitle.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(),
                R.color.primary_text_grey, null)), 0, personnelTitle.length(), 0);
        builder.append(personnelTitle);
        builder.append(StringUtil.SPACE);

        // Highlight T, S and D to cyan
        SpannableString threatTitle = new SpannableString(getString(R.string.sitrep_T));
        threatTitle.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null)), 0, threatTitle.length(), 0);
        builder.append(threatTitle);
        mTvPersonnelTCountTitle.setText(builder, C2OpenSansSemiBoldTextView.BufferType.SPANNABLE);
        builder.delete(personnelTitle.length() + StringUtil.SPACE.length(), builder.length());

        SpannableString suspectTitle = new SpannableString(getString(R.string.sitrep_S));
        suspectTitle.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null)), 0, suspectTitle.length(), 0);
        builder.append(suspectTitle);
        mTvPersonnelSCountTitle.setText(builder, C2OpenSansSemiBoldTextView.BufferType.SPANNABLE);
        builder.delete(personnelTitle.length() + StringUtil.SPACE.length(), builder.length());

        SpannableString deadTitle = new SpannableString(getString(R.string.sitrep_D));
        deadTitle.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null)), 0, deadTitle.length(), 0);
        builder.append(deadTitle);
        mTvPersonnelDCountTitle.setText(builder, C2OpenSansSemiBoldTextView.BufferType.SPANNABLE);

        mTvTotalCountNumber = layoutTotalCountView.findViewById(R.id.tv_total_count_number);
        mTvPersonnelTCountNumber = layoutTotalCountView.findViewById(R.id.tv_first_count_number);
        mTvPersonnelSCountNumber = layoutTotalCountView.findViewById(R.id.tv_second_count_number);
        mTvPersonnelDCountNumber = layoutTotalCountView.findViewById(R.id.tv_third_count_number);
    }

    /**
     * Updates recycler view with new data
     */
    public void addItemInRecycler() {
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyItemInserted(mSitRepListItems.size() - 1);
            mRecyclerAdapter.notifyItemRangeChanged(mSitRepListItems.size() - 1, mSitRepListItems.size());
        }
    }

    /**
     * Navigate to another fragment which displays details of selected Sit Rep
     * @param sitRepModel
     */
    private void navigateToSitRepDetailFragment(SitRepModel sitRepModel) {
        Fragment sitRepDetailFragment = new SitRepDetailFragment();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).postStickyModel(sitRepModel);
        }
//        EventBus.getDefault().postSticky(sitRepModel);

        // Pass info to fragment
        navigateToFragment(sitRepDetailFragment);
//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        FragmentTransaction ft = fm.beginTransaction();
//        ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
//        ft.replace(R.id.layout_sitrep_fragment, sitRepDetailFragment, sitRepDetailFragment.getClass().getSimpleName());
////        ft.addToBackStack(sitRepDetailFragment.getClass().getSimpleName());
//        ft.addToBackStack(null);
//        ft.commit();
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
//        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mSitRepViewModel = ViewModelProviders.of(this).get(SitRepViewModel.class);
//        mUserSitRepJoinViewModel = ViewModelProviders.of(this).get(UserSitRepJoinViewModel.class);

        /*
         * Refreshes recyclerview UI whenever there is a change in Sit Rep (insert, update or delete)
         */
        mSitRepViewModel.getAllSitRepsLiveData().observe(this, new Observer<List<SitRepModel>>() {
            @Override
            public void onChanged(@Nullable List<SitRepModel> sitRepModelList) {
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

                if (mSitRepListItems.size() == 0) {
                    mLayoutAddNewItem.setVisibility(View.VISIBLE);
                    mLayoutTotalCountDashboard.setVisibility(View.GONE);
                } else {
                    mLayoutAddNewItem.setVisibility(View.GONE);
                    mLayoutTotalCountDashboard.setVisibility(View.VISIBLE);

                    int totalPersonnelT = 0;
                    int totalPersonnelS = 0;
                    int totalPersonnelD = 0;
                    for (int i = 0; i < sitRepModelList.size(); i++) {
                        totalPersonnelT += sitRepModelList.get(i).getPersonnelT();
                        totalPersonnelS += sitRepModelList.get(i).getPersonnelS();
                        totalPersonnelD += sitRepModelList.get(i).getPersonnelD();
                    }

                    int totalCount = totalPersonnelT + totalPersonnelS + totalPersonnelD;
                    mTvTotalCountNumber.setText(String.valueOf(totalCount));
                    mTvPersonnelTCountNumber.setText(String.valueOf(totalPersonnelT));
                    mTvPersonnelSCountNumber.setText(String.valueOf(totalPersonnelS));
                    mTvPersonnelDCountNumber.setText(String.valueOf(totalPersonnelD));
                }
            }
        });
    }

    /**
     * Adds designated fragment to Back Stack of Base Child Fragment
     * before navigating to it
     *
     * @param toFragment
     */
    private void navigateToFragment(Fragment toFragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateWithAnimatedTransitionToFragment(
                    R.id.layout_sitrep_fragment, this, toFragment);
        }
    }

    /**
     * Pops back stack of ONLY current tab
     *
     * @return
     */
    public boolean popBackStack() {
        if (!isAdded())
            return false;

        if(getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
            return true;
        } else
            return false;
    }

    private void onVisible() {
        Log.d(TAG, "onVisible");

        FragmentManager fm = getChildFragmentManager();
        boolean isFragmentFound = false;

        int count = fm.getBackStackEntryCount();

        // Checks if current fragment exists in Back stack
        for (int i = 0; i < count; i++) {
            if (this.getClass().getSimpleName().equalsIgnoreCase(fm.getBackStackEntryAt(i).getName())) {
                isFragmentFound = true;
            }
        }

        // If not found, add to current fragment to Back stack
        if (!isFragmentFound) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.addToBackStack(this.getClass().getSimpleName());
            ft.commit();
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
                onVisible();
            } else {
                onInvisible();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIsFragmentVisibleToUser) {
            onInvisible();
        }
    }

//    private BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String messageType = intent.getStringExtra(RestConstants.REST_REQUEST_TYPE);
//            String response = intent.getStringExtra(RestConstants.REST_REQUEST_RESULT);
//            int responseCode = intent.getIntExtra(RestConstants.REST_HTTP_STATUS, -1);
//
//            if ((responseCode < 200) || (responseCode >= 300)) {
//                //Error
//                Log.e("SuperC2", "HTTP Error: " + responseCode + " " + response);
//                return;
//            }
//
//            Gson gson = GsonCreator.createGson();
//            switch (messageType) {
//                case MessageType.GET_TASKS:
//
//
//                    break;
//
//                default:
//                    Log.e("SuperC2", "TaskFragment Receiver: Unknown MessageType: " + messageType + ": " + response);
//                    break;
//            }
//
//        }
//    };
}
