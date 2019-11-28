package sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.taskPhaseStatus;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.TaskViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.EIsValid;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EPhaseNo;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EStatus;
import timber.log.Timber;

public class DashboardTaskPhaseStatusFragment extends Fragment {

    private static final String TAG = DashboardTaskPhaseStatusFragment.class.getSimpleName();

    // View Models
    private UserViewModel mUserViewModel;
    private TaskViewModel mTaskViewModel;

    // Main layout
    private View mRootView;

//    // UI components
//    private C2OpenSansLightTextView mTvOfflineTotal;
//    private C2OpenSansLightTextView mTvOnlineTotal;

    // Recycler View
    private RecyclerView mRecyclerView;
    private DashboardTaskPhaseStatusRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private List<String> mTeamListItems;
    private List<String> mPhaseStatusListItems;

    private boolean mIsFragmentVisibleToUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        observerSetup();

        Log.d(TAG, "onCreate in");

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_dashboard_task_phase_status, container, false);
            initUI(mRootView);
            Log.d(TAG, "onCreate out");
        }

        return mRootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG, "onViewCreated");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            Log.d(TAG, ((Object) this).getClass().getSimpleName() + " is NOT on screen");
        }
        else
        {
            Log.d(TAG, ((Object) this).getClass().getSimpleName() + " is on screen");
        }
    }


    /**
     * Initialise view with recycler data
     *
     * @param rootView
     */
    private void initUI(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.recycler_dashboard_task_phase_status);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerLayoutManager = new LinearLayoutManager(getParentFragment().getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        mRecyclerView.setNestedScrollingEnabled(true);

        if (mTeamListItems == null) {
            mTeamListItems = new ArrayList<>();
        }

        if (mPhaseStatusListItems == null) {
            mPhaseStatusListItems = new ArrayList<>();
        }

        mRecyclerAdapter = new DashboardTaskPhaseStatusRecyclerAdapter(getParentFragment().getContext(),
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
    private synchronized List<TaskModel> refreshUI(List<TaskModel> taskModelList) {
        List<TaskModel> teamPhaseTaskModelListItems = new ArrayList<>();

        // Remove 'Ad Hoc' Tasks from list
        for (int i = 0; i < taskModelList.size(); i++) {
            if (!EPhaseNo.AD_HOC.toString().equalsIgnoreCase(taskModelList.get(i).getPhaseNo())) {
                teamPhaseTaskModelListItems.add(taskModelList.get(i));
            }
        }

        Timber.i("onSuccess singleObserverForAllUsers, refreshUI. teamPhaseTaskModelListItems before sorting: %s ", teamPhaseTaskModelListItems);


        // Sort according to ascending order of phases (e.g. 1, 2, 3, 4...)
        teamPhaseTaskModelListItems.sort(TaskModel.getPhaseNoComparator());


        Timber.i("onSuccess singleObserverForAllUsers, refreshUI. teamPhaseTaskModelListItems after sorting: %s", teamPhaseTaskModelListItems);


        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<List<UserModel>> singleObserverForAllUsers = new SingleObserver<List<UserModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<UserModel> userModelList) {

                Timber.i("onSuccess singleObserverForAllUsers, refreshUI. size of userModelList: %s", userModelList.size());


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
                    Timber.i("onSuccess singleObserverForAllUsers, refreshUI. mTeamListItems:  %s", mTeamListItems);
                    Timber.i("onSuccess singleObserverForAllUsers, refreshUI. mPhaseStatusListItems: %s", mPhaseStatusListItems);

                    mRecyclerAdapter.setTeamPhaseStatusListItems(mTeamListItems, mPhaseStatusListItems);
                }
            }

            @Override
            public void onError(Throwable e) {
                // show an error message
                Timber.i("onError singleObserverForAllUsers, refreshUI. Error Msg: %s", e.toString());

            }
        };

        mUserViewModel.getAllUsers(singleObserverForAllUsers);

        return teamPhaseTaskModelListItems;
    }

    /**
     * Obtain all VALID (not deleted) Task Models from database
     * @param taskModelList
     * @return
     */
    private List<TaskModel> getAllValidTaskListFromDatabase(List<TaskModel> taskModelList) {

        List<TaskModel> validTaskModelList = taskModelList.stream().
                filter(taskModel -> EIsValid.YES.toString().
                        equalsIgnoreCase(taskModel.getIsValid())).
                collect(Collectors.toList());

        return validTaskModelList;
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        if (getParentFragment() != null) {

            mUserViewModel = ViewModelProviders.of(getParentFragment()).get(UserViewModel.class);
            mTaskViewModel = ViewModelProviders.of(getParentFragment()).get(TaskViewModel.class);

            /*
             * Refreshes recyclerview UI whenever there is a change in task (insert, update or delete)
             */
            mTaskViewModel.getAllTasksLiveData().observe(getParentFragment(), new Observer<List<TaskModel>>() {
                @Override
                public void onChanged(@Nullable List<TaskModel> taskModelList) {

                    Timber.d("taskModelList: " + taskModelList);

                    if (taskModelList != null) {
                        taskModelList = getAllValidTaskListFromDatabase(taskModelList);

                        refreshUI(taskModelList);
                    }
                }
            });
        }
    }

    public void onVisible() {
        Timber.i("onVisible");
        observerSetup();

        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyDataSetChanged();
        }
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
                Timber.i("setUserVisibleHint onVisible");
                onVisible();
            } else {
                Timber.i("setUserVisibleHint onInvisible");
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
