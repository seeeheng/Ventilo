package sg.gov.dsta.mobileC3.ventilo.activity.task;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.TaskViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBlackButton;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EPhaseNo;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EStatus;
import timber.log.Timber;

public class TaskDetailFragment extends Fragment {

    private static final String TAG = TaskDetailFragment.class.getSimpleName();

    // View models
    private TaskViewModel mTaskViewModel;

    private C2OpenSansBoldTextView mTvTitleHeader;
    private C2OpenSansSemiBoldTextView mTvTaskStatusHeader;
    private C2OpenSansRegularTextView mTvReporter;

    private C2OpenSansRegularTextView mTvCreatedDateTime;
    private C2OpenSansRegularTextView mTvTaskType;
    private C2OpenSansRegularTextView mTvTaskPriorityOrPhase;
    private C2OpenSansRegularTextView mTvTaskAssignTo;
    private C2OpenSansRegularTextView mTvTaskDescription;

    private Spinner mSpinnerDropdownTitle;

    private C2OpenSansBlackButton mBtnReport;

    private TaskModel mTaskModelOnDisplay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_task_detail, container, false);
        observerSetup();
        initUI(rootView);

        return rootView;
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mTaskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);

        /*
         * Refreshes UI whenever there is a change in Task (insert, update or delete)
         */
        mTaskViewModel.getAllTasksLiveData().observe(this, new Observer<List<TaskModel>>() {
            @Override
            public void onChanged(@Nullable List<TaskModel> taskModelList) {
                for (int i = 0; i < taskModelList.size(); i++) {
                    if (mTaskModelOnDisplay != null &&
                            mTaskModelOnDisplay.getId() == taskModelList.get(i).getId()) {
                        updateUI(taskModelList.get(i));
                        break;
                    }
                }
            }
        });
    }

    private void initUI(View rootView) {
        initToolbarUI(rootView);

        mTvTitleHeader = rootView.findViewById(R.id.tv_task_detail_task_title_header);
        mTvTaskStatusHeader = rootView.findViewById(R.id.tv_task_detail_task_status_header);

        mTvReporter = rootView.findViewById(R.id.tv_task_detail_reporter);
        mTvCreatedDateTime = rootView.findViewById(R.id.tv_task_detail_created_date_time);

        mTvTaskType = rootView.findViewById(R.id.tv_task_detail_task_type);
        mTvTaskPriorityOrPhase = rootView.findViewById(R.id.tv_task_detail_task_priority_phase);
        mTvTaskAssignTo = rootView.findViewById(R.id.tv_task_detail_assign_to);
        mTvTaskDescription = rootView.findViewById(R.id.tv_task_detail_task_description);

//        TaskModel taskModel = EventBus.getDefault().removeStickyEvent(TaskModel.class);

        TaskModel taskModel = null;

        if (getActivity() instanceof MainActivity) {
            Object objectToUpdate = ((MainActivity) getActivity()).
                    getStickyModel(TaskModel.class.getSimpleName());
            if (objectToUpdate instanceof TaskModel) {
                taskModel = (TaskModel) objectToUpdate;
            }
        }

        updateUI(taskModel);
    }

    private void initToolbarUI(View rootView) {
        View layoutToolbar = rootView.findViewById(R.id.layout_toolbar_task_detail_text_left_text_right);
        LinearLayout linearLayoutBtnBack = layoutToolbar.findViewById(R.id.layout_toolbar_top_left_btn);
        linearLayoutBtnBack.setOnClickListener(onBackClickListener);

        LinearLayout linearLayoutBtnEdit = layoutToolbar.findViewById(R.id.layout_toolbar_top_right_btn);
        linearLayoutBtnEdit.setOnClickListener(onEditClickListener);

        C2OpenSansSemiBoldTextView tvToolbarEdit = layoutToolbar.findViewById(R.id.toolbar_top_right_btn_text);

        // Remove edit button if user is CCT
        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                SharedPreferenceUtil.getCurrentUserAccessRight())) {
            tvToolbarEdit.setVisibility(View.GONE);
        } else {
            tvToolbarEdit.setTextColor(ResourcesCompat.getColor(getResources(),
                    R.color.primary_highlight_cyan, null));
            tvToolbarEdit.setText(getString(R.string.btn_edit));
            tvToolbarEdit.setVisibility(View.VISIBLE);
        }
    }

    private void updateUI(TaskModel taskModel) {
        setTaskInfo(taskModel);
    }

    private void setTaskInfo(TaskModel taskModel) {
        if (taskModel != null) {
            mTaskModelOnDisplay = taskModel;

            // Title
            if (taskModel.getTitle() != null) {
                mTvTitleHeader.setText(taskModel.getTitle().trim());
            }

            // Task Status
            if (taskModel.getStatus() != null) {
                String[] assignedToStrArray = StringUtil.removeCommasAndExtraSpaces(taskModel.getAssignedTo());
                String[] statusStrArray = StringUtil.removeCommasAndExtraSpaces(taskModel.getStatus());
                String status = EStatus.NEW.toString();

                for (int i = 0; i < assignedToStrArray.length; i++) {
                    if (SharedPreferenceUtil.getCurrentUserCallsignID().
                            equalsIgnoreCase(assignedToStrArray[i])) {
                        status = statusStrArray[i];
                    }
                }

                if (EStatus.NEW.toString().equalsIgnoreCase(status)) {
                    mTvTaskStatusHeader.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.primary_white));
                } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(status)) {
                    mTvTaskStatusHeader.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.task_status_yellow));
                } else {
                    mTvTaskStatusHeader.setTextColor(ContextCompat.getColor(getContext(),
                            R.color.dull_green));
                }

                mTvTaskStatusHeader.setText(status);
            }

            // Reporter
            if (taskModel.getAssignedBy() != null) {
                mTvReporter.setText(taskModel.getAssignedBy().trim());
            }

            // Created date/time
            StringBuilder createdDateTimeStringBuilder = new StringBuilder();
            String createdDateTime = taskModel.getCreatedDateTime();
            String createdDateTimeInCustomStrFormat = DateTimeUtil.dateToCustomDateTimeStringFormat(
                    DateTimeUtil.stringToDate(createdDateTime));

            if (createdDateTime != null) {
                createdDateTimeStringBuilder.append(createdDateTimeInCustomStrFormat);
                createdDateTimeStringBuilder.append(StringUtil.TAB);
                createdDateTimeStringBuilder.append(StringUtil.TAB);
                createdDateTimeStringBuilder.append(StringUtil.TAB);
                createdDateTimeStringBuilder.append(StringUtil.OPEN_BRACKET);
                createdDateTimeStringBuilder.append(DateTimeUtil.getTimeDifference(
                        DateTimeUtil.stringToDate(createdDateTime)));
                createdDateTimeStringBuilder.append(StringUtil.CLOSE_BRACKET);
            }

            mTvCreatedDateTime.setText(createdDateTimeStringBuilder.toString());

            // Task Type & Task Priority / Phase
            if (taskModel.getPhaseNo() != null) {
                if (EPhaseNo.AD_HOC.toString().equalsIgnoreCase(taskModel.getPhaseNo())) {
                    mTvTaskType.setText(R.string.task_type_ad_hoc);
                    mTvTaskPriorityOrPhase.setText(taskModel.getAdHocTaskPriority());
                } else {
                    mTvTaskType.setText(getString(R.string.task_type_planned));
                    mTvTaskPriorityOrPhase.setText(taskModel.getPhaseNo());
                }
            }

            // Task Assign To
            if (taskModel.getAssignedTo() != null) {
                mTvTaskAssignTo.setText(taskModel.getAssignedTo().trim());
            }

            // Task Description
            if (taskModel.getDescription() != null) {
                mTvTaskDescription.setText(taskModel.getDescription().trim());
            }
        }
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Timber.i("Back button pressed.");


            // Remove sticky Sit Rep model as it is no longer valid
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).removeStickyModel(mTaskModelOnDisplay);
            }

            popChildBackStack();
        }
    };

    private View.OnClickListener onEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Timber.i("Edit button pressed.");


            if (mTaskModelOnDisplay != null) {
                Fragment taskAddUpdateFragment = new TaskAddUpdateFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FragmentConstants.KEY_TASK, FragmentConstants.VALUE_TASK_UPDATE);
                taskAddUpdateFragment.setArguments(bundle);

//                if (getActivity() instanceof MainActivity) {
//                    ((MainActivity) getActivity()).postStickyModel(mTaskModelOnDisplay);
//                }
//                EventBus.getDefault().postSticky(mTaskModelOnDisplay);

                navigateToFragment(taskAddUpdateFragment);
            }
        }
    };

    /**
     * Adds designated fragment to Back Stack of Base Child Fragment
     * before navigating to it
     *
     * @param toFragment
     */
    private void navigateToFragment(Fragment toFragment) {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = ((MainActivity) getActivity());
            mainActivity.navigateWithAnimatedTransitionToFragment(
                    R.id.layout_task_detail_fragment,
                    mainActivity.getBaseChildFragmentOfCurrentFragment(
                            MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID), toFragment);
        }
    }

    /**
     * Accesses child base fragment of current selected view pager item and remove this fragment
     * from child base fragment's stack.
     *
     * Selected View Pager Item: Task
     * Child Base Fragment: TaskFragment
     */
    private void popChildBackStack() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = ((MainActivity) getActivity());
            mainActivity.popChildFragmentBackStack(
                    MainNavigationConstants.SIDE_MENU_TAB_TASK_POSITION_ID);
        }
    }

//    private void onVisible() {
//
//    }
//
//    // Remove all fragments from back stack once this fragment is invisible (user navigates to other tabs)
//    private void onInvisible() {
//        System.out.println("task Invisible");
//        int count = getFragmentManager().getBackStackEntryCount();
//        while (count > 0) {
//            getFragmentManager().popBackStack();
//            count = getFragmentManager().getBackStackEntryCount();
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();

//        refreshUI();

//        if (mIsVisibleToUser) {
//            onVisible();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();

//        if (mIsVisibleToUser) {
//            onInvisible();
//        }
    }

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        mIsVisibleToUser = isVisibleToUser;
//        System.out.println("task Visible");
//        if (isResumed()) { // fragment has been created at this point
//            if (mIsVisibleToUser) {
//                onVisible();
//            } else {
//                onInvisible();
//            }
//        }
//    }
}
