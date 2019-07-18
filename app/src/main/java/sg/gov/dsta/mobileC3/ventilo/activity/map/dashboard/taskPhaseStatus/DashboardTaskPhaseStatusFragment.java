package sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.taskPhaseStatus;

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

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.TaskViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EPhaseNo;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EStatus;

public class DashboardTaskPhaseStatusFragment extends Fragment {

    private static final String TAG = DashboardTaskPhaseStatusFragment.class.getSimpleName();

    // View Models
    private UserViewModel mUserViewModel;
    private TaskViewModel mTaskViewModel;

//    // UI components
//    private C2OpenSansLightTextView mTvOfflineTotal;
//    private C2OpenSansLightTextView mTvOnlineTotal;

    // Recycler View
    private RecyclerView mRecyclerView;
    private DashboardTaskPhaseStatusRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private List<String> mTeamListItems;
    private List<String> mPhaseStatusListItems;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard_task_phase_status, container, false);

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
        mRecyclerView = rootView.findViewById(R.id.recycler_dashboard_task_phase_status);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        if (mTeamListItems == null) {
            mTeamListItems = new ArrayList<>();
        }

        if (mPhaseStatusListItems == null) {
            mPhaseStatusListItems = new ArrayList<>();
        }

        mRecyclerAdapter = new DashboardTaskPhaseStatusRecyclerAdapter(getContext(),
                mTeamListItems, mPhaseStatusListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Removes 'Ad-Hoc' Tasks models, extracts and re-groups the remaining Task models into lists based on phases
     * and store these lists into a main list for the Recycler view
     *
     * @param taskModelList
     * @return
     */
    private List<TaskModel> refreshUI(List<TaskModel> taskModelList) {
        if (mTeamListItems == null) {
            mTeamListItems = new ArrayList<>();
        } else {
            mTeamListItems.clear();
        }

        if (mPhaseStatusListItems == null) {
            mPhaseStatusListItems = new ArrayList<>();
        } else {
            mPhaseStatusListItems.clear();
        }

        List<TaskModel> teamPhaseTaskModelListItems = new ArrayList<>();

        // Remove 'Ad Hoc' Tasks from list
        for (int i = 0; i < taskModelList.size(); i++) {
            if (!EPhaseNo.AD_HOC.toString().equalsIgnoreCase(taskModelList.get(i).getPhaseNo())) {
                teamPhaseTaskModelListItems.add(taskModelList.get(i));
            }
        }

        Log.d(TAG, "onSuccess singleObserverForAllUsers, refreshUI. " +
                "teamPhaseTaskModelListItems before sorting: " + teamPhaseTaskModelListItems);

        // Sort according to ascending order of phases (e.g. 1, 2, 3, 4...)
        teamPhaseTaskModelListItems.sort(TaskModel.getPhaseNoComparator());

        Log.d(TAG, "onSuccess singleObserverForAllUsers, refreshUI. " +
                "teamPhaseTaskModelListItems after sorting: " + teamPhaseTaskModelListItems);

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<List<UserModel>> singleObserverForAllUsers = new SingleObserver<List<UserModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<UserModel> userModelList) {
                Log.d(TAG, "onSuccess singleObserverForAllUsers, refreshUI. " +
                        "size of userModelList: " + userModelList.size());

                StringBuilder taskStatusPhaseCompletionStrBuilder = new StringBuilder();
                String taskStatusPhaseCompletion = StringUtil.EMPTY_STRING;

                // Iterates through all users (teams) in the database to get each phase status
                for (int i = 0; i < userModelList.size(); i++) {
                    UserModel currentUserModel = userModelList.get(i);
                    taskStatusPhaseCompletionStrBuilder.setLength(0);
                    for (int j = 1; j <= 4; j++) {

                        // Checks if current user (team) has a valid status for a specific phase
                        // If the team does not, set phase status to N.A.
                        // Else, set it to stored status
                        boolean isTeamPhaseApplicable = false;

                        if (teamPhaseTaskModelListItems != null) {
                            for (int k = 0; k < teamPhaseTaskModelListItems.size(); k++) {

                                if (String.valueOf(j).equalsIgnoreCase(
                                        teamPhaseTaskModelListItems.get(k).getPhaseNo())) {

                                    TaskModel currentTaskModel = teamPhaseTaskModelListItems.get(k);

                                    // 'Assigned to' may contain multiple users (teams) in a single entry
                                    // separated by commas.
                                    // Hence, extract each phase status of each team from the entry.
                                    String[] assignedToStrArray = StringUtil.
                                            removeCommasAndExtraSpaces(currentTaskModel.getAssignedTo());
                                    String[] statusStrArray = StringUtil.
                                            removeCommasAndExtraSpaces(currentTaskModel.getStatus());

                                    for (int l = 0; l < assignedToStrArray.length; l++) {
                                        if (assignedToStrArray[l].equalsIgnoreCase(currentUserModel.getUserId())) {
                                            taskStatusPhaseCompletionStrBuilder.append(statusStrArray[l]);
                                            taskStatusPhaseCompletionStrBuilder.append(StringUtil.COMMA);
                                            isTeamPhaseApplicable = true;
                                            break;
                                        }
                                    }
                                }
                            }
                        } else {
                            taskStatusPhaseCompletionStrBuilder.append(EStatus.NA.toString());
                            taskStatusPhaseCompletionStrBuilder.append(StringUtil.COMMA);
                        }

                        if (!isTeamPhaseApplicable) {
                            taskStatusPhaseCompletionStrBuilder.append(EStatus.NA.toString());
                            taskStatusPhaseCompletionStrBuilder.append(StringUtil.COMMA);
                        }

                        // Remove last comma for each user after appending all statuses
                        if (j == 4 && taskStatusPhaseCompletionStrBuilder.length() != 0) {
                            taskStatusPhaseCompletion = taskStatusPhaseCompletionStrBuilder.
                                    substring(0, taskStatusPhaseCompletionStrBuilder.length() - 1);
                        }
                    }

                    mTeamListItems.add(currentUserModel.getUserId());
                    mPhaseStatusListItems.add(taskStatusPhaseCompletion);
                }

                // Updates recycler adapter data for UI refresh
                if (mRecyclerAdapter != null) {
                    Log.d(TAG, "onSuccess singleObserverForAllUsers, refreshUI. " +
                            "mTeamListItems: " + mTeamListItems);
                    Log.d(TAG, "onSuccess singleObserverForAllUsers, refreshUI. " +
                            "mPhaseStatusListItems: " + mPhaseStatusListItems);
                    mRecyclerAdapter.setTeamPhaseStatusListItems(mTeamListItems, mPhaseStatusListItems);
                }
            }

            @Override
            public void onError(Throwable e) {
                // show an error message
                Log.d(TAG, "onError singleObserverForAllUsers, refreshUI. " +
                        "Error Msg: " + e.toString());
            }
        };

        mUserViewModel.getAllUsers(singleObserverForAllUsers);

        return teamPhaseTaskModelListItems;
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mTaskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);

        /*
         * Refreshes recyclerview UI whenever there is a change in task (insert, update or delete)
         */
        mTaskViewModel.getAllTasksLiveData().observe(this, new Observer<List<TaskModel>>() {
            @Override
            public void onChanged(@Nullable List<TaskModel> taskModelList) {
//                if (mTaskListItems == null) {
//                    mTaskListItems = new ArrayList<>();
//                } else {
//                    mTaskListItems.clear();
//                }

                Log.d(TAG, "taskModelList: " + taskModelList);

                if (taskModelList != null) {
                    refreshUI(taskModelList);
                }
            }
        });
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
}
