package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserSitRepJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.SitRepViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserSitRepJoinViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DrawableUtil;
import sg.gov.dsta.mobileC3.ventilo.util.PhotoCaptureUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;

public class SitRepFragment extends Fragment {

    private static final String TAG = SitRepFragment.class.getSimpleName();

    // View Models
    private UserViewModel mUserViewModel;
    private SitRepViewModel mSitRepViewModel;
    private UserSitRepJoinViewModel mUserSitRepJoinViewModel;

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
        setUpRecyclerData();

        mRecyclerAdapter = new SitRepRecyclerAdapter(getContext(), mSitRepListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up floating action button
        mFabAddSitRep = rootView.findViewById(R.id.fab_sitrep_add);
        mFabAddSitRep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment sitRepInnerAddFragment = new SitRepAddFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FragmentConstants.KEY_SITREP, FragmentConstants.VALUE_SITREP_ADD);
                sitRepInnerAddFragment.setArguments(bundle);

                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                ft.replace(R.id.layout_sitrep_inner_fragment, sitRepInnerAddFragment, sitRepInnerAddFragment.getClass().getSimpleName());
                ft.addToBackStack(sitRepInnerAddFragment.getClass().getSimpleName());
                ft.commit();
            }
        });

//        initOtherLayouts(inflater, container);
    }

//    private void setImageListeners(View recyclerView) {
//        AppCompatImageView imgDelete = recyclerView.findViewById(R.id.img_task_delete);
//        AppCompatImageView imgStart = recyclerView.findViewById(R.id.img_task_start);
//        AppCompatImageView imgDone = recyclerView.findViewById(R.id.img_task_done);
//
//        imgDelete.setOnClickListener(onDeleteClickListener);
//        imgStart.setOnClickListener(onStartClickListener);
//        imgDone.setOnClickListener(onDoneClickListener);
//
//        imgDelete.bringToFront();
//        imgStart.bringToFront();
//        imgDone.bringToFront();
//    }
//
//    private View.OnClickListener onDeleteClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            TaskViewHolder holder = (TaskViewHolder) view.getTag();
//            int position = holder.getAdapterPosition();
//            removeItemInRecycler(position);
//        }
//    };
//
//    private View.OnClickListener onStartClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            TaskViewHolder holder = (TaskViewHolder) view.getTag();
//            int position = holder.getAdapterPosition();
//            startItemInRecycler(position);
//        }
//    };
//
//    private View.OnClickListener onDoneClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            TaskViewHolder holder = (TaskViewHolder) view.getTag();
//            int position = holder.getAdapterPosition();
//            completeItemInRecycler(position);
//        }
//    };
//
//    private void startItemInRecycler(int position) {
//        mTaskListItems.get(position).setStatus(EStatus.IN_PROGRESS);
//        mRecyclerAdapter.notifyDataSetChanged();
//    }
//
//    private void completeItemInRecycler(int position) {
//        mTaskListItems.get(position).setStatus(EStatus.COMPLETE);
//        mRecyclerAdapter.notifyDataSetChanged();
//    }
//
//    private void removeItemInRecycler(int position) {
//        mTaskListItems.remove(position);
//        mRecyclerView.removeViewAt(position);
//        mRecyclerAdapter.notifyItemRemoved(position);
//        mRecyclerAdapter.notifyItemRangeChanged(position, mTaskListItems.size());
//    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_main, menu);
//        return true;
//    }

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

    public SitRepRecyclerAdapter getAdapter() {
        return mRecyclerAdapter;
    }

//    private void deleteSitRep(int position) {
//        SitRepModel sitRepModelAtPos = mRecyclerAdapter.getSitRepModelAtPosition(position);
//        mSitRepViewModel.deleteSitRep(sitRepModelAtPos.getId());
//    }

    public void refreshData() {
        Log.d(TAG, "refreshData");
        setUpRecyclerData();

//        if (mRecyclerAdapter != null) {
//            mRecyclerAdapter.notifyDataSetChanged();
//        }
    }

    public void addItemInRecycler() {
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyItemInserted(mSitRepListItems.size() - 1);
            mRecyclerAdapter.notifyItemRangeChanged(mSitRepListItems.size() - 1, mSitRepListItems.size());
        }
    }

    private void setUpRecyclerData() {
        if (mSitRepListItems == null) {
            mSitRepListItems = new ArrayList<>();
        }

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String accessToken = pref.getString(SharedPreferenceConstants.ACCESS_TOKEN, "");

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<UserModel> singleObserverForUser = new SingleObserver<UserModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(UserModel userModel) {
                Log.d(TAG, "onSuccess singleObserverForUser, setUpRecyclerData. " +
                        "User Id: " + userModel.getUserId());

                SingleObserver<List<SitRepModel>> singleObserverOfSitRepsForUser = new SingleObserver<List<SitRepModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(List<SitRepModel> sitRepModelList) {
                        Log.d(TAG, "onSuccess singleObserverOfTasksForUser, setUpRecyclerData: " +
                                "taskModelList.size() is " + sitRepModelList.size());
                        if (sitRepModelList.size() == 0) {
                            setUpDummySitReps();
                        } else {
                            mSitRepListItems.clear();
                            mSitRepListItems.addAll(sitRepModelList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverOfTasksForUser, setUpRecyclerData. " +
                                "Error Msg: " + e.toString());
                    }
                };

                mUserSitRepJoinViewModel.querySitRepsForUser(userModel.getUserId(), singleObserverOfSitRepsForUser);
            }

            @Override
            public void onError(Throwable e) {
                // show an error message
                Log.d(TAG, "onError singleObserverForUser, setUpRecyclerData. " +
                        "Error Msg: " + e.toString());
            }
        };

        mUserViewModel.queryUserByAccessToken(accessToken, singleObserverForUser);

//        if (getTotalNumberOfSitReps() == 0) {
//            SitRepModel sitRepModelOne = new SitRepModel();
//            sitRepModelOne.setId(0);
//            sitRepModelOne.setReporter(SharedPreferenceUtil.getCurrentUserCallsignID(getActivity()));
//            sitRepModelOne.setLocation("BALESTIER");
//            sitRepModelOne.setActivity("Fire Fighting");
//            sitRepModelOne.setPersonnelT(6);
//            sitRepModelOne.setPersonnelS(5);
//            sitRepModelOne.setPersonnelD(4);
//            sitRepModelOne.setNextCoa("Inform HQ");
//            sitRepModelOne.setRequest("Additional MP");
//            Date date1 = DateTimeUtil.getSpecifiedDate(2016, Calendar.DECEMBER, 29,
//                    Calendar.AM, 11, 30, 30);
//            String dateOneString = DateTimeUtil.dateToServerStringFormat(date1);
//            sitRepModelOne.setReportedDateTime(dateOneString);
//
//            mSitRepListItems.add(sitRepModelOne);
//
//            addItemsToLocalDatabase();
//
//        } else {
//            mSitRepListItems.clear();
//
//            for (int i = 0; i < getTotalNumberOfSitReps(); i++) {
//                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                String sitRepInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.HEADER_SITREP).concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(String.valueOf(i));
//
//                SitRepModel sitRepModel = new SitRepModel();
//                sitRepModel.setId(pref.getInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ID), SharedPreferenceConstants.DEFAULT_INT));
//                sitRepModel.setReporter(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REPORTER), SharedPreferenceConstants.DEFAULT_STRING));
////                taskItem.setAssigneeAvatar(Objects.requireNonNull(getContext()).getDrawable(pref.getInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
////                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), SharedPreferenceConstants.DEFAULT_INT)));
//                sitRepModel.setLocation(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_LOCATION), SharedPreferenceConstants.DEFAULT_STRING));
//                sitRepModel.setActivity(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ACTIVITY), SharedPreferenceConstants.DEFAULT_STRING));
//                sitRepModel.setPersonnelT(pref.getInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_T), SharedPreferenceConstants.DEFAULT_INT));
//                sitRepModel.setPersonnelS(pref.getInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_S), SharedPreferenceConstants.DEFAULT_INT));
//                sitRepModel.setPersonnelD(pref.getInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_D), SharedPreferenceConstants.DEFAULT_INT));
//                sitRepModel.setNextCoa(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_NEXT_COA), SharedPreferenceConstants.DEFAULT_STRING));
//                sitRepModel.setRequest(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REQUEST), SharedPreferenceConstants.DEFAULT_STRING));
//                sitRepModel.setReportedDateTime(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_DATE), SharedPreferenceConstants.DEFAULT_STRING));
//
//                mSitRepListItems.add(sitRepModel);
//            }
//        }
    }

    private void setUpDummySitReps() {
        SitRepModel sitRepModelOne = new SitRepModel();
        sitRepModelOne.setReporter(SharedPreferenceUtil.getCurrentUserCallsignID(getActivity()));
        byte[] imageByteArray = PhotoCaptureUtil.getByteArrayFromImage(
                DrawableUtil.getBitmap(getResources().getDrawable(R.drawable.img_avatar_deck, null)),
                100);
        sitRepModelOne.setSnappedPhoto(imageByteArray);
        sitRepModelOne.setLocation("BALESTIER");
        sitRepModelOne.setActivity("Fire Fighting");
        sitRepModelOne.setPersonnelT(6);
        sitRepModelOne.setPersonnelS(5);
        sitRepModelOne.setPersonnelD(4);
        sitRepModelOne.setNextCoa("Inform HQ");
        sitRepModelOne.setRequest("Additional MP");
        Date date1 = DateTimeUtil.getSpecifiedDate(2016, Calendar.DECEMBER, 29,
                Calendar.AM, 11, 30, 30);
        String dateOneString = DateTimeUtil.dateToServerStringFormat(date1);
        sitRepModelOne.setReportedDateTime(dateOneString);

        mSitRepListItems.add(sitRepModelOne);

        addItemsToSqliteDatabase();
    }

    private void addItemsToSqliteDatabase() {
        for (int i = 0; i < mSitRepListItems.size(); i++) {
//            final int j = i;

            SingleObserver<Long> singleObserverAddSitRep = new SingleObserver<Long>() {
                @Override
                public void onSubscribe(Disposable d) {
                    // add it to a CompositeDisposable
                }

                @Override
                public void onSuccess(Long sitRepId) {
                    Log.d(TAG, "onSuccess singleObserverAddSitRep, " +
                            "addItemsToSqliteDatabase. " +
                            "SitRepId: " + sitRepId);
                    SharedPreferences pref = PreferenceManager.
                            getDefaultSharedPreferences(getActivity());
                    String accessToken = pref.getString(
                            SharedPreferenceConstants.ACCESS_TOKEN, "");

//                    mTaskListItems.get(j).setId(taskId);
                    addSitRepsToCompositeTableInDatabase(accessToken, sitRepId);
                }

                @Override
                public void onError(Throwable e) {
                    Log.d(TAG, "onError singleObserverAddTask, addItemsToSqliteDatabase. " +
                            "Error Msg: " + e.toString());
                }
            };

            mSitRepViewModel.addSitRep(mSitRepListItems.get(i), singleObserverAddSitRep);
        }
    }

    private void addSitRepsToCompositeTableInDatabase(String accessToken, long sitRepId) {
        Log.d(TAG, "onSuccess singleObserverForGettingSitRepId, addItemsToSqliteDatabase. " +
                "sitRepId: " + sitRepId);

        // TODO: Added this for second user. Remove this once users can be retrieved from excel file.
        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<UserModel> singleObserverUser = new SingleObserver<UserModel>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(UserModel userModel) {
                Log.d(TAG, "onSuccess singleObserverUser, " +
                        "addSitRepsToCompositeTableInDatabase. " +
                        "UserId: " + userModel.getUserId());
                UserSitRepJoinModel userSitRepJoinModel = new UserSitRepJoinModel(userModel.getUserId(), sitRepId);
                mUserSitRepJoinViewModel.addUserSitRepJoin(userSitRepJoinModel);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverUser, " +
                        "addSitRepsToCompositeTableInDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        mUserViewModel.queryUserByAccessToken(accessToken, singleObserverUser);
        mUserViewModel.queryUserByUserId("456", singleObserverUser);
    }



    private void navigateToSitRepDetailFragment(SitRepModel sitRepModel) {
        Fragment sitRepInnerDetailFragment = new SitRepDetailFragment();

        EventBus.getDefault().postSticky(sitRepModel);

//        Bundle bundle = new Bundle();
//        bundle.putString(FragmentConstants.KEY_SITREP, FragmentConstants.VALUE_SITREP_VIEW);
//        sitRepInnerDetailFragment.setArguments(bundle);

//        Bundle bundle = new Bundle();
//        bundle.putString(FragmentConstants.KEY_SITREP, FragmentConstants.VALUE_SITREP_VIEW);
//        bundle.putLong(FragmentConstants.KEY_SITREP_ID, sitRepModelId);
//        bundle.putString(FragmentConstants.KEY_SITREP_LOCATION, mSitRepListItems.get(position).getLocation());
//        bundle.putString(FragmentConstants.KEY_SITREP_ACTIVITY, mSitRepListItems.get(position).getActivity());
//        bundle.putInt(FragmentConstants.KEY_SITREP_PERSONNEL_T, mSitRepListItems.get(position).getPersonnelT());
//        bundle.putInt(FragmentConstants.KEY_SITREP_PERSONNEL_S, mSitRepListItems.get(position).getPersonnelS());
//        bundle.putInt(FragmentConstants.KEY_SITREP_PERSONNEL_D, mSitRepListItems.get(position).getPersonnelD());
//        bundle.putString(FragmentConstants.KEY_SITREP_NEXT_COA, mSitRepListItems.get(position).getNextCoa());
//        bundle.putString(FragmentConstants.KEY_SITREP_REQUEST, mSitRepListItems.get(position).getRequest());
//        sitRepInnerDetailFragment.setArguments(bundle);

        // Pass info to fragment
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
        ft.replace(R.id.layout_sitrep_inner_fragment, sitRepInnerDetailFragment, sitRepInnerDetailFragment.getClass().getSimpleName());
        ft.addToBackStack(sitRepInnerDetailFragment.getClass().getSimpleName());
        ft.commit();
    }
//    /*
//     * Obtain total number of sit reps of user
//     */
//    private int getTotalNumberOfSitReps() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        int totalNumberOfSitReps = pref.getInt(SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SITREP_TOTAL_NUMBER), 0);
//
//        return totalNumberOfSitReps;
//    }
//
//    /*
//     * Add sit rep items to local database
//     */
//    private void addItemsToLocalDatabase() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        SharedPreferences.Editor editor = pref.edit();
//
//        // Increment total number of sit reps by one, and store it
//        String totalNumberOfSitRepsKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SITREP_TOTAL_NUMBER);
//
//        editor.putInt(totalNumberOfSitRepsKey, mSitRepListItems.size());
//
//        for (int i = 0; i < mSitRepListItems.size(); i++) {
//            SitRepModel sitRepModel = mSitRepListItems.get(i);
//            addSingleItemToLocalDatabase(editor, sitRepModel.getId(), sitRepModel.getReporter(),
//                    R.drawable.default_soldier_icon, sitRepModel.getLocation(), sitRepModel.getActivity(),
//                    sitRepModel.getPersonnelT(), sitRepModel.getPersonnelS(), sitRepModel.getPersonnelD(),
//                    sitRepModel.getNextCoa(), sitRepModel.getRequest(),
//                    sitRepModel.getReportedDateTime());
//        }
//    }
//
//    /*
//     * Add single sit rep with respective fields
//     */
//    private void addSingleItemToLocalDatabase(SharedPreferences.Editor editor, long id, String reporter, int reporterAvatarId,
//                                              String location, String activity, int personnelT, int personnelS, int personnelD,
//                                              String nextCoa, String request, String date) {
//
//        String sitRepInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.HEADER_SITREP).concat(SharedPreferenceConstants.SEPARATOR).
//                concat(String.valueOf(id));
//
//        editor.putLong(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ID), id);
//        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REPORTER), reporter);
//        editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REPORTER_AVATAR_ID), reporterAvatarId);
//        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_LOCATION), location);
//        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ACTIVITY), activity);
//        editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_T), personnelT);
//        editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_S), personnelS);
//        editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_D), personnelD);
//        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_NEXT_COA), nextCoa);
//        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REQUEST), request);
//        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_DATE), date);
//
//        editor.apply();
//    }

    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mSitRepViewModel = ViewModelProviders.of(this).get(SitRepViewModel.class);
        mUserSitRepJoinViewModel = ViewModelProviders.of(this).get(UserSitRepJoinViewModel.class);

        /*
         * Refreshes recyclerview UI whenever there is a change in task (insert, update or delete)
         */
        mSitRepViewModel.getAllSitReps().observe(this, new Observer<List<SitRepModel>>() {
            @Override
            public void onChanged(@Nullable List<SitRepModel> sitRepModelList) {
                mRecyclerAdapter.setSitRepListItems(sitRepModelList);
                mSitRepListItems.clear();
                mSitRepListItems.addAll(sitRepModelList);
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
