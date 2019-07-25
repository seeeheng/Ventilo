package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
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
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;
import timber.log.Timber;

public class SitRepFragment extends Fragment {

    private static final String TAG = SitRepFragment.class.getSimpleName();

    // View Models
    private SitRepViewModel mSitRepViewModel;

    // Main layout
    private View mRootView;

    // Total count dashboard
    private View mLayoutTotalCountDashboard;
    private C2OpenSansRegularTextView mTvTotalCountNumber;
    private C2OpenSansRegularTextView mTvPersonnelTCountNumber;
    private C2OpenSansRegularTextView mTvPersonnelSCountNumber;
    private C2OpenSansRegularTextView mTvPersonnelDCountNumber;

    // Add new item UI
    private View mLayoutAddNewItem;

    private SitRepRecyclerAdapter mRecyclerAdapter;
    private FloatingActionButton mFabAddSitRep;

    private List<SitRepModel> mSitRepListItems;

    private boolean mIsFragmentVisibleToUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_sitrep, container, false);
            observerSetup();
            initUI(mRootView);
        }

        return mRootView;
    }

    private void initUI(View rootView) {
        initAddNewItemUI(rootView);

        mLayoutTotalCountDashboard = rootView.findViewById(R.id.layout_total_count_dashboard);
        initTotalCountDashboardUI(mLayoutTotalCountDashboard);

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_sitrep);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager recyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerLayoutManager);
        recyclerView.addOnItemTouchListener(new SitRepRecyclerItemTouchListener(getContext(), recyclerView, new SitRepRecyclerItemTouchListener.OnItemClickListener() {
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
        recyclerView.setAdapter(mRecyclerAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up floating action button
        mFabAddSitRep = rootView.findViewById(R.id.fab_sitrep_add);

        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                SharedPreferenceUtil.getCurrentUserAccessRight())) {
            mFabAddSitRep.show();
            mFabAddSitRep.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFabAddSitRep.setEnabled(false);

                    Fragment sitRepAddUpdateFragment = new SitRepAddUpdateFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(FragmentConstants.KEY_SITREP, FragmentConstants.VALUE_SITREP_ADD);
                    sitRepAddUpdateFragment.setArguments(bundle);

                    navigateToFragment(sitRepAddUpdateFragment);
                }
            });
        } else {
            if (mFabAddSitRep.isShown()) {
                mFabAddSitRep.hide();
            }
        }

    }

    /**
     * Creates message when no Sit Rep is available
     * @param rootView
     */
    private void initAddNewItemUI(View rootView) {
        mLayoutAddNewItem = rootView.findViewById(R.id.layout_sitrep_add_new_item);
        C2OpenSansRegularTextView tvAddNewItemCategory = mLayoutAddNewItem.findViewById(R.id.tv_add_new_item_category);

        // Team Leads can create/view Sit Rep
        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                SharedPreferenceUtil.getCurrentUserAccessRight())) {
            tvAddNewItemCategory.setText(MainApplication.getAppContext().getString(R.string.add_new_item_sitrep));

        } else { // CCT can only view Sit Rep from Team Leads
            AppCompatImageView imgAddNewItemCategory = mLayoutAddNewItem.findViewById(R.id.img_add_new_item_category);
            C2OpenSansRegularTextView tvAddNewItemCategoryBelow = mLayoutAddNewItem.findViewById(R.id.tv_add_new_item_category_below);
            imgAddNewItemCategory.setVisibility(View.GONE);
            tvAddNewItemCategoryBelow.setVisibility(View.GONE);

            tvAddNewItemCategory.setText(getString(R.string.no_sitrep));
        }
    }

    /**
     * Init dashboard UI displaying total count of Sit Rep Threat(T), Suspects(S) and Dead(D) personnel
     *
     * @param layoutTotalCountView
     */
    private void initTotalCountDashboardUI(View layoutTotalCountView) {
        C2OpenSansSemiBoldTextView tvTotalCountTitle = layoutTotalCountView.findViewById(R.id.tv_total_count_title);
        C2OpenSansSemiBoldTextView tvPersonnelTCountTitle = layoutTotalCountView.findViewById(R.id.tv_first_count_title);
        C2OpenSansSemiBoldTextView tvPersonnelSCountTitle = layoutTotalCountView.findViewById(R.id.tv_second_count_title);
        C2OpenSansSemiBoldTextView tvPersonnelDCountTitle = layoutTotalCountView.findViewById(R.id.tv_third_count_title);

        tvTotalCountTitle.setText(MainApplication.getAppContext().
                getString(R.string.sitrep_total_count_title));
        tvTotalCountTitle.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));

        SpannableStringBuilder builder = new SpannableStringBuilder();

        SpannableString personnelTitle = new SpannableString(MainApplication.getAppContext().
                getString(R.string.sitrep_personnel));
        personnelTitle.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(),
                R.color.primary_text_grey, null)), 0, personnelTitle.length(), 0);
        builder.append(personnelTitle);
        builder.append(StringUtil.SPACE);

        // Highlight T, S and D to cyan
        SpannableString threatTitle = new SpannableString(MainApplication.getAppContext().
                getString(R.string.sitrep_T));
        threatTitle.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null)), 0, threatTitle.length(), 0);
        builder.append(threatTitle);
        tvPersonnelTCountTitle.setText(builder, C2OpenSansSemiBoldTextView.BufferType.SPANNABLE);
        builder.delete(personnelTitle.length() + StringUtil.SPACE.length(), builder.length());

        SpannableString suspectTitle = new SpannableString(MainApplication.getAppContext().
                getString(R.string.sitrep_S));
        suspectTitle.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null)), 0, suspectTitle.length(), 0);
        builder.append(suspectTitle);
        tvPersonnelSCountTitle.setText(builder, C2OpenSansSemiBoldTextView.BufferType.SPANNABLE);
        builder.delete(personnelTitle.length() + StringUtil.SPACE.length(), builder.length());

        SpannableString deadTitle = new SpannableString(MainApplication.getAppContext().
                getString(R.string.sitrep_D));
        deadTitle.setSpan(new ForegroundColorSpan(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null)), 0, deadTitle.length(), 0);
        builder.append(deadTitle);
        tvPersonnelDCountTitle.setText(builder, C2OpenSansSemiBoldTextView.BufferType.SPANNABLE);

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
     *
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
            enableFabAddSitRep();
        }
    }

    /**
     * Allows other fragments to re-enable Floating Action Button
     */
    public void enableFabAddSitRep() {
        if (mFabAddSitRep != null && !mFabAddSitRep.isEnabled()) {
            mFabAddSitRep.setEnabled(true);
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
        Timber.i("onVisible");

        enableFabAddSitRep();
    }

    private void onInvisible() {
        Timber.i("onInvisible");
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
