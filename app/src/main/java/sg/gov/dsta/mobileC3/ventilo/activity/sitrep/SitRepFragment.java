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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
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

        mLayoutAddNewItem = rootView.findViewById(R.id.layout_add_new_item);
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
                System.out.println("position is " + position);
                System.out.println("position location is " + mSitRepListItems.get(position).getLocation());
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

                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                ft.replace(R.id.layout_sitrep_fragment, sitRepAddUpdateFragment, sitRepAddUpdateFragment.getClass().getSimpleName());
                ft.addToBackStack(sitRepAddUpdateFragment.getClass().getSimpleName());
                ft.commit();

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

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        //Floating Action Button to add new task
//        mAddBtn = getView().findViewById(R.id.fab_task_add);
//
//        mAddBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(getContext(), TasksAddActivity.class);
//                startActivity(intent);
//            }
//        });
//    }

//    public SitRepRecyclerAdapter getAdapter() {
//        return mRecyclerAdapter;
//    }

//    private void deleteSitRep(int position) {
//        SitRepModel sitRepModelAtPos = mRecyclerAdapter.getSitRepModelAtPosition(position);
//        mSitRepViewModel.deleteSitRep(sitRepModelAtPos.getId());
//    }

//    public void refreshData() {
//        Log.d(TAG, "refreshData");
//        setUpRecyclerData();
//
////        if (mRecyclerAdapter != null) {
////            mRecyclerAdapter.notifyDataSetChanged();
////        }
//    }

    /**
     * Updates recycler view with new data
     */
    public void addItemInRecycler() {
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyItemInserted(mSitRepListItems.size() - 1);
            mRecyclerAdapter.notifyItemRangeChanged(mSitRepListItems.size() - 1, mSitRepListItems.size());
        }
    }

//    private void setUpRecyclerData() {
//        if (mSitRepListItems == null) {
//            mSitRepListItems = new ArrayList<>();
//        }
//
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String accessToken = pref.getString(SharedPreferenceConstants.ACCESS_TOKEN, "");
//
//        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
//        // asynchronously in the background thread and apply changes on the main UI thread
//        SingleObserver<UserModel> singleObserverForUser = new SingleObserver<UserModel>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//                // add it to a CompositeDisposable
//            }
//
//            @Override
//            public void onSuccess(UserModel userModel) {
//                Log.d(TAG, "onSuccess singleObserverForUser, setUpRecyclerData. " +
//                        "User Id: " + userModel.getUserId());
//
//                SingleObserver<List<SitRepModel>> singleObserverOfSitRepsForUser = new SingleObserver<List<SitRepModel>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                    }
//
//                    @Override
//                    public void onSuccess(List<SitRepModel> sitRepModelList) {
//                        Log.d(TAG, "onSuccess singleObserverOfTasksForUser, setUpRecyclerData: " +
//                                "taskModelList.size() is " + sitRepModelList.size());
//                        if (sitRepModelList.size() == 0) {
//                            setUpDummySitReps();
//                        } else {
//                            mSitRepListItems.clear();
//                            mSitRepListItems.addAll(sitRepModelList);
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "onError singleObserverOfTasksForUser, setUpRecyclerData. " +
//                                "Error Msg: " + e.toString());
//                    }
//                };
//
//                mUserSitRepJoinViewModel.querySitRepsForUser(userModel.getUserId(), singleObserverOfSitRepsForUser);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                // show an error message
//                Log.d(TAG, "onError singleObserverForUser, setUpRecyclerData. " +
//                        "Error Msg: " + e.toString());
//            }
//        };
//
//        mUserViewModel.queryUserByAccessToken(accessToken, singleObserverForUser);
//    }

//    private void setUpDummySitReps() {
//        SitRepModel sitRepModelOne = new SitRepModel();
//        sitRepModelOne.setReporter(SharedPreferenceUtil.getCurrentUserCallsignID(getActivity()));
//        byte[] imageByteArray = PhotoCaptureUtil.getByteArrayFromImage(
//                DrawableUtil.getBitmap(getResources().getDrawable(R.drawable.img_avatar_deck, null)),
//                100);
//        sitRepModelOne.setSnappedPhoto(imageByteArray);
//        sitRepModelOne.setLocation("BALESTIER");
//        sitRepModelOne.setActivity("Fire Fighting");
//        sitRepModelOne.setPersonnelT(6);
//        sitRepModelOne.setPersonnelS(5);
//        sitRepModelOne.setPersonnelD(4);
//        sitRepModelOne.setNextCoa("Inform HQ");
//        sitRepModelOne.setRequest("Additional MP");
//        Date date1 = DateTimeUtil.getSpecifiedDate(2016, Calendar.DECEMBER, 29,
//                Calendar.AM, 11, 30, 30);
//        String dateOneString = DateTimeUtil.dateToServerStringFormat(date1);
//        sitRepModelOne.setCreatedDateTime(dateOneString);
//
//        mSitRepListItems.add(sitRepModelOne);
//
//        addItemsToSqliteDatabase();
//    }

//    private void addItemsToSqliteDatabase() {
//        for (int i = 0; i < mSitRepListItems.size(); i++) {
////            final int j = i;
//
//            SingleObserver<Long> singleObserverAddSitRep = new SingleObserver<Long>() {
//                @Override
//                public void onSubscribe(Disposable d) {
//                    // add it to a CompositeDisposable
//                }
//
//                @Override
//                public void onSuccess(Long sitRepId) {
//                    Log.d(TAG, "onSuccess singleObserverAddSitRep, " +
//                            "addItemsToSqliteDatabase. " +
//                            "SitRepId: " + sitRepId);
//                    SharedPreferences pref = PreferenceManager.
//                            getDefaultSharedPreferences(getActivity());
//                    String accessToken = pref.getString(
//                            SharedPreferenceConstants.ACCESS_TOKEN, "");
//
////                    mTaskListItems.get(j).setId(taskId);
//                    addSitRepsToCompositeTableInDatabase(accessToken, sitRepId);
//                }
//
//                @Override
//                public void onError(Throwable e) {
//                    Log.d(TAG, "onError singleObserverAddTask, addItemsToSqliteDatabase. " +
//                            "Error Msg: " + e.toString());
//                }
//            };
//
//            mSitRepViewModel.insertSitRepWithObserver(mSitRepListItems.get(i), singleObserverAddSitRep);
//        }
//    }

//    private void addSitRepsToCompositeTableInDatabase(String accessToken, long sitRepId) {
//        Log.d(TAG, "onSuccess singleObserverForGettingSitRepId, addItemsToSqliteDatabase. " +
//                "sitRepId: " + sitRepId);
//
//        // TODO: Added this for second user. Remove this once users can be retrieved from excel file.
//        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
//        // asynchronously in the background thread and apply changes on the main UI thread
//        SingleObserver<UserModel> singleObserverUser = new SingleObserver<UserModel>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//                // add it to a CompositeDisposable
//            }
//
//            @Override
//            public void onSuccess(UserModel userModel) {
//                Log.d(TAG, "onSuccess singleObserverUser, " +
//                        "addSitRepsToCompositeTableInDatabase. " +
//                        "UserId: " + userModel.getUserId());
//                UserSitRepJoinModel userSitRepJoinModel = new UserSitRepJoinModel(userModel.getUserId(), sitRepId);
//                mUserSitRepJoinViewModel.addUserSitRepJoin(userSitRepJoinModel);
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.d(TAG, "onError singleObserverUser, " +
//                        "addSitRepsToCompositeTableInDatabase. " +
//                        "Error Msg: " + e.toString());
//            }
//        };
//
//        mUserViewModel.queryUserByAccessToken(accessToken, singleObserverUser);
//        mUserViewModel.queryUserByUserId("456", singleObserverUser);
//    }

    /**
     * Navigate to another fragment which displays details of selected Sit Rep
     * @param sitRepModel
     */
    private void navigateToSitRepDetailFragment(SitRepModel sitRepModel) {
        Fragment sitRepDetailFragment = new SitRepDetailFragment();

        EventBus.getDefault().postSticky(sitRepModel);

        // Pass info to fragment
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
        ft.replace(R.id.layout_sitrep_fragment, sitRepDetailFragment, sitRepDetailFragment.getClass().getSimpleName());
        ft.addToBackStack(sitRepDetailFragment.getClass().getSimpleName());
        ft.commit();
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
                mRecyclerAdapter.setSitRepListItems(sitRepModelList);

                if (mSitRepListItems == null) {
                    mSitRepListItems = new ArrayList<>();
                } else {
                    mSitRepListItems.clear();
                    mSitRepListItems.addAll(sitRepModelList);
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
