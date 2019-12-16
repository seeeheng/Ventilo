package sg.gov.dsta.mobileC3.ventilo.activity.task;

import android.annotation.SuppressLint;
import androidx.lifecycle.ViewModelProviders;
import android.graphics.PorterDuff;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.widget.AppCompatImageView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.listener.DebounceOnClickListener;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoinModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.TaskViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserTaskJoinViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ListenerUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ProgressBarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.DatabaseTableConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.EIsValid;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EAdHocTaskPriority;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EPhaseNo;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EStatus;
import timber.log.Timber;

public class TaskAddUpdateFragment extends Fragment implements SnackbarUtil.SnackbarActionClickListener {

    private static final String TAG = TaskAddUpdateFragment.class.getSimpleName();
    private static final int RB_TASK_TYPE_AD_HOC_ID = R.id.radio_btn_add_update_task_type_ad_hoc;
    private static final int RB_TASK_TYPE_PLANNED_ID = R.id.radio_btn_add_update_task_type_planned;

    // View Models
    private UserViewModel mUserViewModel;
    private TaskViewModel mTaskViewModel;
    private UserTaskJoinViewModel mUserTaskJoinViewModel;

    // Main
    private FrameLayout mMainLayout;

    // Toolbar section
    private LinearLayout mLinearLayoutBtnCreateOrUpdate;
    private C2OpenSansSemiBoldTextView mTvToolbarCreateOrUpdate;

    // Task Type
    private C2OpenSansSemiBoldTextView mTvTaskPriorityOrPhaseTitle;
    private RadioGroup mRgTaskType;

    // Task Priority / Phase section
    private View mLayoutInputTaskPriority;
    private View mLayoutInputTaskPhase;
    private ExpandableListView mTaskPriorityExpandableListView;
    private ExpandableListView mTaskPhaseExpandableListView;
    private TaskExpandableListAdapter mTaskPriorityExpandableListAdapter;
    private TaskExpandableListAdapter mTaskPhaseExpandableListAdapter;
//    private List<String> mTaskPriorityDistinctList;
//    private List<String> mTaskPhaseDistinctList;

    // Task Name section
    private ExpandableListView mTaskNameExpandableListView;
    private TaskExpandableListAdapter mTaskNameExpandableListAdapter;
    private RelativeLayout mLayoutTaskNameInputOthers;
    private AppCompatImageView mImgTaskNameInputOthers;
    private C2OpenSansRegularEditTextView mEtvTaskNameOthers;

    // Task Description section
    private C2OpenSansRegularEditTextView mEtvTaskDescription;

    // Assign To section
    private LinearLayout mLinearLayoutAssignTo;

    private TaskModel mTaskModelToUpdate;

    private static final String TASK_PHASE_EXPANDABLE_LIST_SELECTED_STRING = "Task Phase Expandable List Selected String";
    private static final String TASK_PRIORITY_EXPANDABLE_LIST_SELECTED_STRING = "Task Priority Expandable List Selected String";
    private static final String TASK_NAME_INPUT_OTHERS_SELECTION = "Task Name Input Others Selection";
    private static final String TASK_NAME_EXPANDABLE_LIST_SELECTED_STRING = "Task Name Expandable List Selected String";
    private static final String TASK_NAME_INPUT_OTHERS_TEXT = "Task Name Input Others Text";

    private boolean mIsTaskNameInputOthersSelected;

    private String mTaskNameOthersText;

    private String mTaskPriorityExpandableListSelectedString;
    private String mTaskPhaseExpandableListSelectedString;
    private String mTaskNameExpandableListSelectedString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_add_update_task, container, false);
        observerSetup();
        initUI(inflater, rootView);
        Timber.i("Fragment view created.");

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(TASK_NAME_INPUT_OTHERS_SELECTION, mIsTaskNameInputOthersSelected);

        outState.putString(TASK_PRIORITY_EXPANDABLE_LIST_SELECTED_STRING, mTaskPriorityExpandableListAdapter.getSelectedChildTaskName());
        outState.putString(TASK_PHASE_EXPANDABLE_LIST_SELECTED_STRING, mTaskPhaseExpandableListAdapter.getSelectedChildTaskName());
        outState.putString(TASK_NAME_EXPANDABLE_LIST_SELECTED_STRING, mTaskNameExpandableListAdapter.getSelectedChildTaskName());
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {
            // Task Priority
            mTaskPriorityExpandableListSelectedString = savedInstanceState.getString(TASK_PRIORITY_EXPANDABLE_LIST_SELECTED_STRING, StringUtil.EMPTY_STRING);

            // Task Phase
            mTaskPhaseExpandableListSelectedString = savedInstanceState.getString(TASK_PHASE_EXPANDABLE_LIST_SELECTED_STRING, StringUtil.EMPTY_STRING);

            // Task Name
            mIsTaskNameInputOthersSelected = savedInstanceState.getBoolean(TASK_NAME_INPUT_OTHERS_SELECTION, false);

            mTaskNameExpandableListSelectedString = savedInstanceState.getString(TASK_NAME_EXPANDABLE_LIST_SELECTED_STRING, StringUtil.EMPTY_STRING);

            mTaskNameOthersText = savedInstanceState.getString(TASK_NAME_INPUT_OTHERS_TEXT, StringUtil.EMPTY_STRING);
        }
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mTaskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
        mUserTaskJoinViewModel = ViewModelProviders.of(this).get(UserTaskJoinViewModel.class);
    }

    /**
     * Initialise UIs of every section
     *
     * @param rootView
     */
    private void initUI(LayoutInflater inflater, View rootView) {
        mMainLayout = rootView.findViewById(R.id.layout_add_update_task_fragment);

        // Show loading progress dialog upon start of page data setup
        ProgressBarUtil.createProgressDialog(getContext());

        initToolbarUI(rootView);
        initTaskTypeUI(rootView);
        initTaskFieldsUI(rootView);
        initTaskDescriptionUI(rootView);
        initAssignToUI(inflater, rootView);

//        initCallsignTitleUI(rootView);
//        initPicUI(rootView);
//        initLocationUI(rootView);
//        initActivityUI(rootView);
//        initPersonnelUI(rootView);
//        initNextCoaUI(rootView);
//        initRequestUI(rootView);

//        initLayouts(rootView);
//        initSpinners(rootView);
    }

    /**
     * Initialise toolbar UI with back (left) and send/update (right) buttons
     *
     * @param rootView
     */
    private void initToolbarUI(View rootView) {
        View layoutToolbar = rootView.findViewById(R.id.layout_toolbar_add_update_task_text_left_text_right);
        layoutToolbar.setClickable(true);

        LinearLayout linearLayoutBtnBack = layoutToolbar.findViewById(R.id.layout_toolbar_top_left_btn);
        linearLayoutBtnBack.setOnClickListener(onBackClickListener);
        mLinearLayoutBtnCreateOrUpdate = layoutToolbar.findViewById(R.id.layout_toolbar_top_right_btn);

        mTvToolbarCreateOrUpdate = layoutToolbar.findViewById(R.id.toolbar_top_right_btn_text);
        mTvToolbarCreateOrUpdate.setText(getString(R.string.btn_create));
        mLinearLayoutBtnCreateOrUpdate.setOnClickListener(onCreateClickListener);
    }

    private void initTaskTypeUI(View rootView) {
        mTvTaskPriorityOrPhaseTitle = rootView.findViewById(R.id.tv_add_update_task_task_priority_phase);
        mRgTaskType = rootView.findViewById(R.id.radio_group_add_update_task_type);
        mRgTaskType.setOnCheckedChangeListener(onTaskTypeRadioGroupCheckedChangeListener);
    }

    private RadioGroup.OnCheckedChangeListener onTaskTypeRadioGroupCheckedChangeListener =
            new RadioGroup.OnCheckedChangeListener() {
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    switch (checkedId) {
                        case RB_TASK_TYPE_AD_HOC_ID:
                            mTvTaskPriorityOrPhaseTitle.setText(getString(R.string.task_ad_hoc_task_priority));
                            mLayoutInputTaskPhase.setVisibility(View.GONE);
                            mLayoutInputTaskPriority.setVisibility(View.VISIBLE);
//                            mTaskPhaseExpandableListAdapter.setHeaderTitle(
//                                    getString(R.string.task_select_task_ad_hoc_priority));

//                            Log.i(TAG, "RadioGroup.OnCheckedChangeListener, Task Priority List: " +
//                                    mTaskPriorityDistinctList);

//                            mTaskPhaseExpandableListAdapter.setChildList(mTaskPriorityDistinctList);

                            break;
                        case RB_TASK_TYPE_PLANNED_ID:
                            mTvTaskPriorityOrPhaseTitle.setText(getString(R.string.task_task_phase));
                            mLayoutInputTaskPhase.setVisibility(View.VISIBLE);
                            mLayoutInputTaskPriority.setVisibility(View.GONE);
//                            mTaskPhaseExpandableListAdapter.setHeaderTitle(
//                                    getString(R.string.task_select_task_phase));

//                            Log.i(TAG, "RadioGroup.OnCheckedChangeListener, Task Phase List: " +
//                                    mTaskPhaseDistinctList);

//                            mTaskPhaseExpandableListAdapter.setChildList(mTaskPhaseDistinctList);
                            break;
                        default:
                            break;
                    }
                }
            };

    /**
     * Initialise Task Priority, Phase and Name UI
     *
     * @param rootView
     */
    private void initTaskFieldsUI(View rootView) {
        initTaskPriorityUI(rootView);
        initTaskPhaseUI(rootView);
        initTaskNameUI(rootView);
        initTaskPhaseAdapter();
        initTaskNameAdapter();

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<List<TaskModel>> singleObserverForAllTasks = new SingleObserver<List<TaskModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<TaskModel> taskModelList) {
                Timber.i("onSuccess singleObserverForAllTasks, initTaskFieldsUI. " +
                        "size of taskModelList: %d", taskModelList.size());

//                initTaskPhaseAdapter(taskModelList);
//                initTaskNameAdapter(taskModelList);

                List<String> tempTaskPhaseList = sortUpdatedPhaseList(taskModelList);
                List<String> taskNameList = sortUpdatedTaskNameList(taskModelList);

                List<String> taskPhaseList = new ArrayList<>();
                taskPhaseList.add(getString(R.string.dashboard_task_phase_one));
                taskPhaseList.add(getString(R.string.dashboard_task_phase_two));
                taskPhaseList.add(getString(R.string.dashboard_task_phase_three));
                taskPhaseList.add(getString(R.string.dashboard_task_phase_four));

                for (int i = 0; i < tempTaskPhaseList.size(); i++) {
                    if (taskPhaseList.contains(tempTaskPhaseList.get(i).trim())) {
                        taskPhaseList.remove(tempTaskPhaseList.get(i));
                    }
                }

                if (mTaskPhaseExpandableListSelectedString != null) {
                    mTaskPhaseExpandableListAdapter.setSelectedChildItemValue(mTaskPhaseExpandableListSelectedString);
                }

//                if (taskPhaseList.size() == 0) {
////                    mTaskPhaseExpandableListAdapter.setGroupTitle(
////                            getString(R.string.task_select_task_phase_no_option));
//
////                    if (getSnackbarView() != null) {
////                        SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
////                                getString(R.string.snackbar_task_select_task_phase_no_option));
////                    }
//
//                    mTaskPhaseExpandableListView.setEnabled(false);
//                } else {
//                    mTaskPhaseExpandableListView.setEnabled(true);
//                }

                mTaskPhaseExpandableListAdapter.setChildList(taskPhaseList);
                mTaskPhaseExpandableListAdapter.notifyDataSetChanged();

//                if (taskNameList.size() == 0) {
////                    mTaskNameExpandableListAdapter.setGroupTitle(
////                            getString(R.string.task_task_name_no_option));
//
////                    if (getSnackbarView() != null) {
////                        SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
////                                getString(R.string.snackbar_task_task_name_no_option));
////                    }
//
//                    mTaskNameExpandableListView.setEnabled(false);
//                } else {
//                    mTaskNameExpandableListView.setEnabled(true);
//                }

                mTaskNameExpandableListAdapter.setChildList(taskNameList);
                mTaskNameExpandableListAdapter.notifyDataSetChanged();

                // Checks Bundle here ONLY after getting required
                // taskModelist (without 'Assign To') data for checks
                checkBundle(false);
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverForAllTasks, initTaskFieldsUI. " +
                        "Error Msg: %s", e.toString());
            }
        };

        mTaskViewModel.getAllTasks(singleObserverForAllTasks);
    }

    /**
     * Initialise Task Priority UI and adapter
     *
     * @param rootView
     */
    private void initTaskPriorityUI(View rootView) {
        mLayoutInputTaskPriority = rootView.findViewById(R.id.input_task_priority);
        initTaskPriorityInputOthersUI(mLayoutInputTaskPriority);
        mTaskPriorityExpandableListView = mLayoutInputTaskPriority.
                findViewById(R.id.listview_expandable_listview_edittext);

        List<String> taskPriorityList = getPriorityList();
        String TaskPriorityListHeader = getString(R.string.task_select_task_ad_hoc_priority);
        mTaskPriorityExpandableListAdapter = new TaskExpandableListAdapter(getContext(),
                TaskPriorityListHeader, taskPriorityList);

        mTaskPriorityExpandableListView.setOnChildClickListener(onTaskPriorityListChildClickListener);
        mTaskPriorityExpandableListView.setOnGroupExpandListener(onTaskPriorityListGroupExpandListener);
        mTaskPriorityExpandableListView.setOnGroupCollapseListener(onTaskPriorityListGroupCollapseListener);

        // Set list adapter
        mTaskPriorityExpandableListView.setAdapter(mTaskPriorityExpandableListAdapter);
    }

    /**
     * Initialise Task Phase UI
     *
     * @param rootView
     */
    private void initTaskPhaseUI(View rootView) {
        mLayoutInputTaskPhase = rootView.findViewById(R.id.input_task_phase);
        initTaskPhaseInputOthersUI(mLayoutInputTaskPhase);
        mTaskPhaseExpandableListView = mLayoutInputTaskPhase.
                findViewById(R.id.listview_expandable_listview_edittext);

        mTaskPhaseExpandableListView.setOnChildClickListener(onTaskPhaseListChildClickListener);
        mTaskPhaseExpandableListView.setOnGroupExpandListener(onTaskPhaseListGroupExpandListener);
        mTaskPhaseExpandableListView.setOnGroupCollapseListener(onTaskPhaseListGroupCollapseListener);

        // Choose default 'Planned' for Task type
        mLayoutInputTaskPriority.setVisibility(View.GONE);
        mLayoutInputTaskPhase.setVisibility(View.VISIBLE);
        mRgTaskType.check(RB_TASK_TYPE_PLANNED_ID);
    }

    /**
     * Initialise Task Name UI
     *
     * @param rootView
     */
    private void initTaskNameUI(View rootView) {
        View layoutInputTaskName = rootView.findViewById(R.id.input_task_name);
        initTaskNameInputOthersUI(layoutInputTaskName);

        mTaskNameExpandableListView = layoutInputTaskName.
                findViewById(R.id.listview_expandable_listview_edittext);
//        mTaskNameExpandableListView.setSaveEnabled(false);

        mTaskNameExpandableListView.setOnChildClickListener(onTaskNameListChildClickListener);
        mTaskNameExpandableListView.setOnGroupExpandListener(onTaskNameListGroupExpandListener);
        mTaskNameExpandableListView.setOnGroupCollapseListener(onTaskNameListGroupCollapseListener);
    }

    /**
     * Initialise Task Priority input others UI
     *
     * @param layoutInputTaskPriority
     */
    private void initTaskPriorityInputOthersUI(View layoutInputTaskPriority) {
        RelativeLayout layoutTaskPriorityInputOthers = layoutInputTaskPriority.findViewById(
                R.id.layout_expandable_listview_edittext_input_others_icon);

        layoutTaskPriorityInputOthers.setEnabled(false);
    }

    /**
     * Initialise Task Phase input others UI
     *
     * @param layoutInputTaskPhase
     */
    private void initTaskPhaseInputOthersUI(View layoutInputTaskPhase) {
        RelativeLayout layoutTaskPhaseInputOthers = layoutInputTaskPhase.findViewById(
                R.id.layout_expandable_listview_edittext_input_others_icon);

        layoutTaskPhaseInputOthers.setEnabled(false);
    }

    /**
     * Initialise Task Name input others UI
     *
     * @param layoutInputTaskName
     */
    private void initTaskNameInputOthersUI(View layoutInputTaskName) {
        mLayoutTaskNameInputOthers = layoutInputTaskName.findViewById(
                R.id.layout_expandable_listview_edittext_input_others_icon);
        mImgTaskNameInputOthers = layoutInputTaskName.findViewById(
                R.id.img_expandable_listview_edittext_input_others_icon);
        mEtvTaskNameOthers = layoutInputTaskName.findViewById(
                R.id.etv_expandable_listview_edittext_others_info);
//        mEtvTaskNameOthers.setSaveEnabled(false);

        mLayoutTaskNameInputOthers.setOnClickListener(onTaskNameInputOthersClickListener);
        mEtvTaskNameOthers.setHint(getString(R.string.task_task_name_hint));
    }

    /**
     * Sets selected UI for Task Name input others UI
     *
     * @param view
     */
    private void setTaskNameInputOthersSelectedUI(View view) {
        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));
        mImgTaskNameInputOthers.setColorFilter(ResourcesCompat.getColor(
                getResources(), R.color.background_main_black, null));
        mTaskNameExpandableListView.setVisibility(View.GONE);
        mEtvTaskNameOthers.setVisibility(View.VISIBLE);
    }

    /**
     * Sets unselected UI for Task Name input others UI
     *
     * @param view
     */
    private void setTaskNameInputOthersUnselectedUI(View view) {
        view.setBackgroundColor(ResourcesCompat.getColor(getResources(),
                R.color.background_dark_grey, null));
        mImgTaskNameInputOthers.setColorFilter(null);
        mEtvTaskNameOthers.setVisibility(View.GONE);
        mTaskNameExpandableListView.setVisibility(View.VISIBLE);
    }

    // ---------------------------------------- Adapters ---------------------------------------- //

    /**
     * Get adapter list containing Ad Hoc Priority
     *
     * @return
     */
    private List<String> getPriorityList() {
        List<String> taskPriorityDistinctList = new ArrayList<>();
        taskPriorityDistinctList.add(EAdHocTaskPriority.HIGH.toString());
        taskPriorityDistinctList.add(EAdHocTaskPriority.LOW.toString());
        return taskPriorityDistinctList;
    }

    /**
     * Sorts updated list of task phases by distinct values for adapter
     *
     * @param taskModelList
     * @return
     */
    private List<String> sortUpdatedPhaseList(List<TaskModel> taskModelList) {
        // Extracts a list of all Task phase from the list of all TaskModel objects
        List<String> taskPhaseList = taskModelList.stream().
                map(TaskModel -> TaskModel.getPhaseNo()).
                filter(TaskModel -> !TaskModel.equalsIgnoreCase(EPhaseNo.AD_HOC.toString())).
                collect(Collectors.toList());

        // Extracts a list of distinct values (e.g. 3, 1, 4, 2...)
        List<String> taskPhaseDistinctList = taskPhaseList.stream().distinct().collect(Collectors.toList());

        for (int i = 0; i < taskPhaseDistinctList.size(); i++) {
            String phaseNo = getString(R.string.task_phase).
                    concat(StringUtil.SPACE).concat(taskPhaseDistinctList.get(i));
            taskPhaseDistinctList.set(i, phaseNo);
        }

        return taskPhaseDistinctList;
    }

    /**
     * Initialise Task Phase adapter
     */
    private void initTaskPhaseAdapter() {

        // -------------------- Task Phase UI --------------------
//        mTaskPhaseDistinctList = null;
//        mTaskPhaseDistinctList = getUpdatedPhaseList(taskModelList);
        List<String> taskPhaseList = new ArrayList<>();
        String TaskPhaseListHeader = getString(R.string.task_select_task_phase);
        mTaskPhaseExpandableListAdapter = new TaskExpandableListAdapter(getContext(),
                TaskPhaseListHeader, taskPhaseList);

        // Set list adapter
        mTaskPhaseExpandableListView.setAdapter(mTaskPhaseExpandableListAdapter);
    }

    /**
     * Sorts updated list of task names by distinct values for adapter
     *
     * @param taskModelList
     * @return
     */
    private List<String> sortUpdatedTaskNameList(List<TaskModel> taskModelList) {
        // Extracts a list of all Task titles from the list of all TaskModel objects
        List<String> taskNameList = taskModelList.stream().map(
                TaskModel -> TaskModel.getTitle()).collect(Collectors.toList());

        // Extracts a list of distinct values (e.g. 3, 1, 4, 2...)
        List<String> taskNameDistinctList = taskNameList.stream().distinct().collect(Collectors.toList());

        return taskNameDistinctList;
    }

    /**
     * Initialise Task Name adapter
     */
    private void initTaskNameAdapter() {
        String listHeader = getString(R.string.task_select_task_name);
        List<String> taskNameList = new ArrayList<>();

        mTaskNameExpandableListAdapter = new TaskExpandableListAdapter(getContext(), listHeader, taskNameList);

        // Set list adapter
        mTaskNameExpandableListView.setAdapter(mTaskNameExpandableListAdapter);
    }

    private DebounceOnClickListener onTaskNameInputOthersClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            mIsTaskNameInputOthersSelected = !view.isSelected();
            view.setSelected(mIsTaskNameInputOthersSelected);

            if (view.isSelected()) {
                setTaskNameInputOthersSelectedUI(view);
            } else {
                setTaskNameInputOthersUnselectedUI(view);
            }
        }
    };

    /**
     * ---------------------------------------- Task Priority onClickListeners ----------------------------------------
     **/
    private ExpandableListView.OnChildClickListener onTaskPriorityListChildClickListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view,
                                            int groupPosition, int childPosition, long id) {
                    // --------------------------------------------------------------------------------
                    // For getChildAt(index), the following applies:
                    // Group 1 [index = 0]
                    // Child 1 [index = 1]
                    // Child 2 [index = 2]
                    // Group 2 (if any) [index = 3]
                    // ...
                    // groupPosition starts from 0; childPosition starts from 0;
                    // Hence, first child = childPosition + 1;
                    // --------------------------------------------------------------------------------
                    childPosition++;

                    // Get text from selected child view and save it in adapter for retrieval later
                    C2OpenSansRegularTextView tvChildListItem = expandableListView.getChildAt(childPosition).
                            findViewById(R.id.tv_expandable_list_row_item_text);
                    String selectedChildItemValue = tvChildListItem.getText().toString().trim();

                    if (mTaskPriorityExpandableListAdapter != null) {
                        mTaskPriorityExpandableListAdapter.setSelectedChildItemValue(selectedChildItemValue);
                    }

                    // By collapsing group, it calls notifyDataSetChanged on adapter to refresh data
                    // and refreshes ALL child views to be unselected
                    expandableListView.collapseGroup(groupPosition);

                    return false;
                }
            };

    private ExpandableListView.OnGroupExpandListener onTaskPriorityListGroupExpandListener =
            new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {
                    int noOfChildren = mTaskPriorityExpandableListAdapter.getChildrenCount(groupPosition);
                    mTaskPriorityExpandableListView.getLayoutParams().height +=
                            getResources().getDimension(R.dimen.input_others_layout_icon_height) * noOfChildren;
                }
            };

    private ExpandableListView.OnGroupCollapseListener onTaskPriorityListGroupCollapseListener =
            new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {
                    int noOfChildren = mTaskPriorityExpandableListAdapter.getChildrenCount(groupPosition);
                    mTaskPriorityExpandableListView.getLayoutParams().height -=
                            getResources().getDimension(R.dimen.input_others_layout_icon_height) * noOfChildren;
                }
            };

    /**
     * ---------------------------------------- Task Phase onClickListeners ----------------------------------------
     **/
    private ExpandableListView.OnChildClickListener onTaskPhaseListChildClickListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view,
                                            int groupPosition, int childPosition, long id) {
                    // --------------------------------------------------------------------------------
                    // For getChildAt(index), the following applies:
                    // Group 1 [index = 0]
                    // Child 1 [index = 1]
                    // Child 2 [index = 2]
                    // Group 2 (if any) [index = 3]
                    // ...
                    // groupPosition starts from 0; childPosition starts from 0;
                    // Hence, first child = childPosition + 1;
                    // --------------------------------------------------------------------------------
                    childPosition++;

                    // Get text from selected child view and save it in adapter for retrieval later
                    C2OpenSansRegularTextView tvChildListItem = expandableListView.getChildAt(childPosition).
                            findViewById(R.id.tv_expandable_list_row_item_text);
                    String selectedChildItemValue = tvChildListItem.getText().toString().trim();

                    if (mTaskPhaseExpandableListAdapter != null) {
                        mTaskPhaseExpandableListAdapter.setSelectedChildItemValue(selectedChildItemValue);
                    }

                    // By collapsing group, it calls notifyDataSetChanged on adapter to refresh data
                    // and refreshes ALL child views to be unselected
                    expandableListView.collapseGroup(groupPosition);

                    return false;
                }
            };

    private ExpandableListView.OnGroupExpandListener onTaskPhaseListGroupExpandListener =
            new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {
                    int noOfChildren = mTaskPhaseExpandableListAdapter.getChildrenCount(groupPosition);
                    mTaskPhaseExpandableListView.getLayoutParams().height +=
                            getResources().getDimension(R.dimen.input_others_layout_icon_height) * noOfChildren;

                    if ((mTaskPhaseExpandableListAdapter.getChildList().size() == 0)) {
                        if (getSnackbarView() != null) {
                            SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                    getString(R.string.snackbar_task_select_task_phase_no_option));
                        }
                    }
                }
            };

    private ExpandableListView.OnGroupCollapseListener onTaskPhaseListGroupCollapseListener =
            new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {
                    int noOfChildren = mTaskPhaseExpandableListAdapter.getChildrenCount(groupPosition);
                    mTaskPhaseExpandableListView.getLayoutParams().height -=
                            getResources().getDimension(R.dimen.input_others_layout_icon_height) * noOfChildren;
                }
            };

    /**
     * ---------------------------------------- Task Name onClickListeners ----------------------------------------
     **/
    private ExpandableListView.OnChildClickListener onTaskNameListChildClickListener =
            new ExpandableListView.OnChildClickListener() {
                @Override
                public boolean onChildClick(ExpandableListView expandableListView, View view,
                                            int groupPosition, int childPosition, long id) {
                    // --------------------------------------------------------------------------------
                    // For getChildAt(index), the following applies:
                    // Group 1 [index = 0]
                    // Child 1 [index = 1]
                    // Child 2 [index = 2]
                    // Group 2 (if any) [index = 3]
                    // ...
                    // groupPosition starts from 0; childPosition starts from 0;
                    // Hence, first child = childPosition + 1;
                    // --------------------------------------------------------------------------------
                    childPosition++;

                    // Get text from selected child view and save it in adapter for retrieval later
                    C2OpenSansRegularTextView tvChildListItem = expandableListView.getChildAt(childPosition).
                            findViewById(R.id.tv_expandable_list_row_item_text);
                    String selectedChildItemValue = tvChildListItem.getText().toString().trim();

                    if (mTaskNameExpandableListAdapter != null) {
                        mTaskNameExpandableListAdapter.setSelectedChildItemValue(selectedChildItemValue);
                    }

                    // By collapsing group, it calls notifyDataSetChanged on adapter to refresh data
                    // and refreshes ALL child views to be unselected
                    expandableListView.collapseGroup(groupPosition);

                    return false;
                }
            };

//    private ExpandableListView.OnGroupClickListener onTaskNameListGroupClickListener =
//            new ExpandableListView.OnGroupClickListener() {
//                @Override
//                public boolean onGroupClick(ExpandableListView expandableListView,
//                                            View view, int groupPosition, long id) {
//                    // --------------------------------------------------------------------------------
//                    // In isItemChecked(index), the following applies:
//                    // Group 1 [index = 0]
//                    // Child 1 [index = 1]
//                    // Child 2 [index = 2]
//                    // Group 2 (if any) [index = 3]
//                    // ...
//                    // groupPosition starts from 0; Hence, first group = groupPosition
//                    // --------------------------------------------------------------------------------
//                    if (expandableListView.isGroupExpanded(groupPosition) &&
//                            expandableListView.getChildAt(mPreviousSelectedTaskNameChildId) != null) {
//                        expandableListView.getChildAt(mPreviousSelectedTaskNameChildId).setSelected(true);
//                    }
//
//                    return false;
//                }
//            };


    private ExpandableListView.OnGroupExpandListener onTaskNameListGroupExpandListener =
            new ExpandableListView.OnGroupExpandListener() {
                @Override
                public void onGroupExpand(int groupPosition) {
                    int noOfChildren = mTaskNameExpandableListAdapter.getChildrenCount(groupPosition);
                    mTaskNameExpandableListView.getLayoutParams().height +=
                            getResources().getDimension(R.dimen.input_others_layout_icon_height) * noOfChildren;

                    if ((mTaskNameExpandableListAdapter.getChildList().size() == 0)) {
                        if (getSnackbarView() != null) {
                            SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                    getString(R.string.snackbar_task_task_name_no_option));
                        }
                    }
                }
            };

    private ExpandableListView.OnGroupCollapseListener onTaskNameListGroupCollapseListener =
            new ExpandableListView.OnGroupCollapseListener() {
                @Override
                public void onGroupCollapse(int groupPosition) {
                    int noOfChildren = mTaskNameExpandableListAdapter.getChildrenCount(groupPosition);
                    mTaskNameExpandableListView.getLayoutParams().height -=
                            getResources().getDimension(R.dimen.input_others_layout_icon_height) * noOfChildren;
                }
            };

    /**
     * Initialise available Teams to assign task to
     *
     * @param inflater
     * @param rootView
     */
    private void initAssignToUI(LayoutInflater inflater, View rootView) {
        mLinearLayoutAssignTo = rootView.findViewById(R.id.layout_add_update_task_assign_to);

        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<List<UserModel>> singleObserverForAllUsers = new SingleObserver<List<UserModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<UserModel> userModelList) {
                Timber.i("onSuccess singleObserverForAllUsers, initAssignToUI. " +
                        "size of userModelList: %d", userModelList.size());

                // Get a list of a list of team profile names (by removing commas and spaces from
                // each UserModel.getTeam())
                // For e.g., original list from UserModel.getTeam()
                // String item 1 -> 456
                // String item 2 -> 456, 853
                // will be streamed to:
                // List<String> item 1 -> (Sub-item 1) 456
                // List<String> item 2 -> (Sub-item 1) 456 | (Sub-item 2) 853
                // and finally streamed into List<List<String>>
                List<List<String>> listOfListOfProfileUserIds = userModelList.stream().map(
                        UserModel -> Arrays.asList(StringUtil.removeCommasAndExtraSpaces(UserModel.getUserId()))).
                        collect(Collectors.toList());

                Timber.i("listOfListOfProfileUserIds: %s", listOfListOfProfileUserIds);

                // Converts the above list of list into a flat/single hierachy list
                // and extract ONLY distinct values
                // Above example will stream to flat hierachical list of:
                // List<String> item 1 -> 456
                // List<String> item 2 -> 456
                // List<String> item 3 -> 853
                // And then to distinct value of:
                // List<String> item 1 -> 456
                // List<String> item 2 -> 853
                List<Object> flatListOfDistinctProfileUserIds =
                        listOfListOfProfileUserIds.stream()
                                .flatMap(list -> list.stream()).distinct()
                                .collect(Collectors.toList());

                // Create button layout for each userId
                for (int i = 0; i < flatListOfDistinctProfileUserIds.size(); i++) {
                    View viewBtnAssignToTeam = inflater.inflate(R.layout.layout_img_text_fixed_dimension_img_btn, null);
                    C2OpenSansSemiBoldTextView tvAssignToTeam = viewBtnAssignToTeam.
                            findViewById(R.id.tv_img_text_fixed_dimension_text_img_btn);
                    AppCompatImageView imgAssignToTeam = viewBtnAssignToTeam.
                            findViewById(R.id.img_img_text_fixed_dimension_pic_img_btn);

                    // Set UserIds
                    StringBuilder teamName = new StringBuilder();
                    teamName.append(getString(R.string.team_header));
                    teamName.append(StringUtil.SPACE);
                    teamName.append(flatListOfDistinctProfileUserIds.get(i).toString().trim());
                    tvAssignToTeam.setText(teamName.toString());

                    // Set default unselected image
                    imgAssignToTeam.setImageDrawable(ResourcesCompat.getDrawable(getResources(),
                            R.drawable.icon_new_unselected, null));

                    // Set fixed height for newly created button
                    DimensionUtil.setDimensions(viewBtnAssignToTeam,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            (int) getResources().getDimension(R.dimen.img_btn_fixed_height),
                            new LinearLayout(getContext()));

                    // Set margin at the end of each button
                    DimensionUtil.setMargins(viewBtnAssignToTeam, 0, 0,
                            (int) getResources().getDimension(R.dimen.elements_margin_spacing), 0);

                    // Set highlight on button upon click
                    viewBtnAssignToTeam.setOnClickListener(new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

                        @Override
                        public void onDebouncedClick(View view) {
                            view.setSelected(!view.isSelected());
                            if (view.isSelected()) {
                                setAssignToSelectedUI(viewBtnAssignToTeam, tvAssignToTeam, imgAssignToTeam);
                            } else {
                                setAssignToUnselectedUI(viewBtnAssignToTeam, tvAssignToTeam, imgAssignToTeam);
                            }
                        }
                    });

                    mLinearLayoutAssignTo.addView(viewBtnAssignToTeam);
                }

                // Checks Bundle here ONLY after inflating required 'Assign To' data for checks
                checkBundle(true);

                // Dismiss progress dialog after page settings have been loaded
                ProgressBarUtil.dismissProgressDialog();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverForAllUsers, initAssignToUI. " +
                        "Error Msg: %s", e.toString());
            }
        };

        mUserViewModel.getAllUsers(singleObserverForAllUsers);
    }

    private void setAssignToSelectedUI(View viewBtnAssignToTeam,
                                       C2OpenSansSemiBoldTextView tvAssignToTeam,
                                       AppCompatImageView imgAssignToTeamRadioBtn) {
        viewBtnAssignToTeam.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.img_btn_background_cyan_border, null));
        tvAssignToTeam.setTextColor(ContextCompat.getColor(getContext(),
                R.color.primary_highlight_cyan));
        imgAssignToTeamRadioBtn.setImageDrawable(ResourcesCompat.getDrawable(
                getResources(), R.drawable.icon_checkbox_selected, null));
    }

    private void setAssignToUnselectedUI(View viewBtnAssignToTeam,
                                         C2OpenSansSemiBoldTextView tvAssignToTeam,
                                         AppCompatImageView imgAssignToTeam) {
        viewBtnAssignToTeam.setBackground(ResourcesCompat.getDrawable(
                getResources(), R.drawable.img_btn_background_without_border, null));
        tvAssignToTeam.setTextColor(ContextCompat.getColor(getContext(),
                R.color.primary_text_hint_dark_grey));
        imgAssignToTeam.setImageDrawable(ResourcesCompat.getDrawable(
                getResources(), R.drawable.icon_new_unselected, null));
        imgAssignToTeam.setColorFilter(ContextCompat.getColor(
                getContext(), R.color.translucent), PorterDuff.Mode.SRC_ATOP);
    }

    /**
     * Initialise Task description UI
     * <p>
     * Suppression is to remove warning for overriding OnTouchListener which Android requires proper
     * handling of the performClick() method thereafter, in which the standard UI views all set up to provide
     * blind users with appropriate feedback through Accessibility services. However, in this use case,
     * it is not crucial and does not affect targeted user experience.
     *
     * @param rootView
     */
    @SuppressLint("ClickableViewAccessibility")
    private void initTaskDescriptionUI(View rootView) {
        mEtvTaskDescription = rootView.findViewById(R.id.etv_add_update_task_description_info);
//        mEtvTaskDescription.setSaveEnabled(false);
        mEtvTaskDescription.setOnTouchListener(onViewTouchListener);
    }

    /**
     * Enable internal vertical scrolling for edit text views where content exceed maximum height
     */
    private View.OnTouchListener onViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (view.hasFocus()) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_SCROLL:
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        return true;
                }
            }
            return false;
        }
    };

    // ---------------------------------------- OnClickListeners ---------------------------------------- //
    private DebounceOnClickListener onBackClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            Timber.i("Back button pressed.");
            popChildBackStack();
        }
    };

    private DebounceOnClickListener onCreateClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            Timber.i("Create button pressed.");

            if (isFormCompleteForFurtherAction()) {
                if (getSnackbarView() != null) {
                    SnackbarUtil.showCustomAlertSnackbar(mMainLayout, getSnackbarView(),
                            getString(R.string.snackbar_task_create_confirmation_message),
                            TaskAddUpdateFragment.this);
                }
            }
        }
    };

    private DebounceOnClickListener onUpdateClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {

            if (isFormCompleteForFurtherAction()) {
                if (getSnackbarView() != null) {
                    SnackbarUtil.showCustomAlertSnackbar(mMainLayout, getSnackbarView(),
                            getString(R.string.snackbar_task_update_confirmation_message),
                            TaskAddUpdateFragment.this);
                }
            }
        }
    };

    /**
     * Create new or update existing Task model from form
     *
     * @param toUpdate
     * @return
     */
    private TaskModel createNewOrUpdateTaskModelFromForm(boolean toUpdate, String assignedBy) {
        TaskModel newTaskModel;

        if (toUpdate) {
            newTaskModel = mTaskModelToUpdate;

            // Last Updated Date Time of main information
            newTaskModel.setLastUpdatedMainDateTime(DateTimeUtil.getCurrentDateTime());

        } else {
            newTaskModel = new TaskModel();
            newTaskModel.setRefId(DatabaseTableConstants.LOCAL_REF_ID);
            newTaskModel.setLastUpdatedMainDateTime(StringUtil.INVALID_STRING);

            // Created DateTime
            newTaskModel.setCreatedDateTime(DateTimeUtil.getCurrentDateTime());
        }

        // Phase No, Ad Hoc Task Priority
        if (RB_TASK_TYPE_AD_HOC_ID == mRgTaskType.getCheckedRadioButtonId()) {
            newTaskModel.setPhaseNo(EPhaseNo.AD_HOC.toString());
            newTaskModel.setAdHocTaskPriority(mTaskPriorityExpandableListAdapter.
                    getSelectedChildTaskName().trim());
        } else {
            String phaseNoWithoutLabel = StringUtil.removeFirstWord(
                    mTaskPhaseExpandableListAdapter.getSelectedChildTaskName()).trim();
            newTaskModel.setPhaseNo(phaseNoWithoutLabel);
            newTaskModel.setAdHocTaskPriority(StringUtil.INVALID_STRING);
        }

        // Assign To, Status, Completed DateTime and Last Updated DateTime
        StringBuilder assignToStringBuilder = new StringBuilder();
        StringBuilder statusStringBuilder = new StringBuilder();
        StringBuilder completedDateTimeStringBuilder = new StringBuilder();
        StringBuilder lastUpdatedDateTimeStringBuilder = new StringBuilder();
        String assignToGroup = StringUtil.INVALID_STRING;
        String statusGroup = StringUtil.INVALID_STRING;
        String completedDateTimeGroup = StringUtil.INVALID_STRING;
        String lastUpdatedDateTimeGroup = StringUtil.INVALID_STRING;

        // For update; extract current list of existing 'Assign To', 'Status',
        // 'CompletedDateTime' and 'LastUpdatedDateTime' data for comparison later
        List<String> toUpdateAssignedToList = null;
        List<String> toUpdateStatusList = null;
        List<String> toUpdateCompletedDateTimeList = null;

        if (toUpdate) {
            toUpdateAssignedToList = Arrays.asList(StringUtil.removeCommasAndExtraSpaces(
                    newTaskModel.getAssignedTo()));
            toUpdateStatusList = Arrays.asList(StringUtil.removeCommasAndExtraSpaces(
                    newTaskModel.getStatus()));
            toUpdateCompletedDateTimeList = Arrays.asList(StringUtil.removeCommasAndExtraSpaces(
                    newTaskModel.getCompletedDateTime()));
        }

        for (int i = 0; i < mLinearLayoutAssignTo.getChildCount(); i++) {
            View viewBtnAssignToTeam = mLinearLayoutAssignTo.getChildAt(i);

            C2OpenSansSemiBoldTextView tvAssignToTeam = viewBtnAssignToTeam.
                    findViewById(R.id.tv_img_text_fixed_dimension_text_img_btn);

            if (viewBtnAssignToTeam.isSelected()) {
                String currentlySelectedAssignToTeam = StringUtil.removeFirstWord(
                        tvAssignToTeam.getText().toString().trim());

                assignToStringBuilder.append(currentlySelectedAssignToTeam);
                assignToStringBuilder.append(StringUtil.COMMA);

                // For existing assigned teams, compare with selected 'Assign To' teams
                // If match, do NOT update status and completedDateTime as these teams may
                // already be executing the tasks
                if (toUpdateAssignedToList != null &&
                        toUpdateAssignedToList.stream().anyMatch(
                                toUpdateAssignedTo -> toUpdateAssignedTo.
                                        equalsIgnoreCase(currentlySelectedAssignToTeam))) {

                    int index = toUpdateAssignedToList.indexOf(currentlySelectedAssignToTeam);
                    statusStringBuilder.append(toUpdateStatusList.get(index));
                    completedDateTimeStringBuilder.append(toUpdateCompletedDateTimeList.get(index));

                } else {
                    statusStringBuilder.append(EStatus.NEW.toString());
                    completedDateTimeStringBuilder.append(StringUtil.INVALID_STRING);

                }

                // Always update lastUpdatedDateTime for any changes regardless
                lastUpdatedDateTimeStringBuilder.append(DateTimeUtil.getCurrentDateTime());

                statusStringBuilder.append(StringUtil.COMMA);
                completedDateTimeStringBuilder.append(StringUtil.COMMA);
                lastUpdatedDateTimeStringBuilder.append(StringUtil.COMMA);
            }

            if (i == mLinearLayoutAssignTo.getChildCount() - 1) {
                assignToGroup = assignToStringBuilder.substring(0,
                        assignToStringBuilder.length() - 1);
                statusGroup = statusStringBuilder.substring(0,
                        statusStringBuilder.length() - 1);
                completedDateTimeGroup = completedDateTimeStringBuilder.substring(0,
                        completedDateTimeStringBuilder.length() - 1);
                lastUpdatedDateTimeGroup = lastUpdatedDateTimeStringBuilder.substring(0,
                        lastUpdatedDateTimeStringBuilder.length() - 1);
            }
        }

        newTaskModel.setAssignedTo(assignToGroup);
        newTaskModel.setStatus(statusGroup);
        newTaskModel.setCompletedDateTime(completedDateTimeGroup);
        newTaskModel.setLastUpdatedStatusDateTime(lastUpdatedDateTimeGroup);

        // Assign By (Default CCT - 999)
//        newTaskModel.setAssignedBy(DatabaseTableConstants.DEFAULT_CCT_ID);
        newTaskModel.setAssignedBy(assignedBy);

        // Title
        String title;

        if (!mLayoutTaskNameInputOthers.isSelected()) {
            title = mTaskNameExpandableListAdapter.getSelectedChildTaskName();
        } else {
            title = mEtvTaskNameOthers.getText().toString().trim();
        }

        // Title
        newTaskModel.setTitle(title);

        // Description
        newTaskModel.setDescription(mEtvTaskDescription.getText().toString().trim());

        // Is Valid (Task Model's validity - whether it has been deleted or not)
        newTaskModel.setIsValid(EIsValid.YES.toString());

        return newTaskModel;
    }

    /**
     * Stores Task data locally and broadcasts to other devices
     *
     * @param taskModel
     * @param userId
     */
    private void addItemToLocalDatabaseAndBroadcast(TaskModel taskModel, String userId) {
        SingleObserver<Long> singleObserverAddTask = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(Long taskId) {
                Timber.i("onSuccess singleObserverAddTask," +
                        "addItemToLocalDatabaseAndBroadcast." +
                        "TaskId: %d", taskId);

                taskModel.setRefId(taskId);
//                mTaskViewModel.updateTask(taskModel);

                // AssignedTo is used as userId(s) for UserTaskJoin composite table in local database
                // Create row for UserTaskJoin with userId and taskId
                String assignedToGroup = taskModel.getAssignedTo();
                String[] assignedToGroupsArray = StringUtil.removeCommasAndExtraSpaces(assignedToGroup);
                for (int i = 0; i < assignedToGroupsArray.length; i++) {
                    UserTaskJoinModel userTaskJoinModel = new
                            UserTaskJoinModel(assignedToGroupsArray[i].trim(), taskId);
                    mUserTaskJoinViewModel.addUserTaskJoin(userTaskJoinModel);
                }

                // Send newly created Task model to all other devices
                JeroMQBroadcastOperation.broadcastDataInsertionOverSocket(taskModel);

                // Show snackbar message and return to main Task fragment page
                if (getSnackbarView() != null) {
                    SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                            getString(R.string.snackbar_task_sent_message));
                }

                popChildBackStack();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverAddTask, addItemToLocalDatabaseAndBroadcast. " +
                        "Error Msg: %s ", e.toString());
            }
        };

        mTaskViewModel.insertTaskWithObserver(taskModel, singleObserverAddTask);
    }

    /**
     * Validates form before enabling Create/Update button
     *
     * @return
     */
    private boolean isFormCompleteForFurtherAction() {

        // Count used to check that all fields are complete
        // 4 means form is completed, otherwise it is incomplete
        int formCompletedCount = 0;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getString(R.string.snackbar_form_incomplete_message));

        // Validate Task Priority and Phase
        if (RB_TASK_TYPE_AD_HOC_ID == mRgTaskType.getCheckedRadioButtonId()) {
            if (StringUtil.EMPTY_STRING.equalsIgnoreCase(
                    mTaskPriorityExpandableListAdapter.getSelectedChildTaskName())) {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.task_ad_hoc_task_priority));
            } else {
                formCompletedCount++;
            }
        } else {
            if (StringUtil.EMPTY_STRING.equalsIgnoreCase(
                    mTaskPhaseExpandableListAdapter.getSelectedChildTaskName())) {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.task_task_phase));
            } else {
                formCompletedCount++;
            }
        }

        // Validate Task Name
        if (!mLayoutTaskNameInputOthers.isSelected()) {
            if (StringUtil.EMPTY_STRING.equalsIgnoreCase(
                    mTaskNameExpandableListAdapter.getSelectedChildTaskName())) {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.task_task_name));
            } else {
                formCompletedCount++;
            }
        } else {
            if (!TextUtils.isEmpty(mEtvTaskNameOthers.getText().toString().trim())) {
                formCompletedCount++;
            } else {
                stringBuilder.append(System.lineSeparator());
                stringBuilder.append(StringUtil.HYPHEN);
                stringBuilder.append(StringUtil.SPACE);
                stringBuilder.append(getString(R.string.task_task_name));
            }
        }

        // Validate Task Description
        if (!TextUtils.isEmpty(mEtvTaskDescription.getText().toString().trim())) {
            formCompletedCount++;
        } else {
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append(StringUtil.HYPHEN);
            stringBuilder.append(StringUtil.SPACE);
            stringBuilder.append(getString(R.string.task_task_description));
        }

        // Validate Task Assign To
        // Get selected assigned Team Name
        boolean isAssignToSelected = false;
        for (int i = 0; i < mLinearLayoutAssignTo.getChildCount(); i++) {
            View viewBtnAssignToTeam = mLinearLayoutAssignTo.getChildAt(i);

            if (viewBtnAssignToTeam.isSelected()) {
                isAssignToSelected = true;
            }
        }

        if (isAssignToSelected) {
            formCompletedCount++;
        } else {
            stringBuilder.append(System.lineSeparator());
            stringBuilder.append(StringUtil.HYPHEN);
            stringBuilder.append(StringUtil.SPACE);
            stringBuilder.append(getString(R.string.task_assign_to));
        }

        // Form is incomplete; show snackbar message to fill required fields
        if (formCompletedCount != 4) {
            String fieldsToCompleteMessage = stringBuilder.toString().trim();
            if (getSnackbarView() != null) {
                SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                        fieldsToCompleteMessage);
            }
        } else { // form is complete
            return true;
        }

        return false;
    }

    /**
     * Get Snackbar view from main activity
     *
     * @return
     */
    private View getSnackbarView() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getSnackbarView();
        } else {
            return null;
        }
    }

    /**
     * Populates form with Task model (without 'Assign To') data that is to be updated
     *
     * @param taskModel
     */
    private void updateFormWithoutAssignToData(TaskModel taskModel) {
        if (taskModel != null) {
            Timber.i("Updating form without AssignTo info...");

            // Type and Priority / Phase
            if (taskModel.getPhaseNo().equalsIgnoreCase(EPhaseNo.AD_HOC.toString())) {
                mRgTaskType.check(RB_TASK_TYPE_AD_HOC_ID);
                mTaskPriorityExpandableListAdapter.setSelectedChildItemValue(
                        taskModel.getAdHocTaskPriority());

                // Notify adapter here as child UI is ready for refresh
                mTaskPriorityExpandableListAdapter.notifyDataSetChanged();
            } else {
                mRgTaskType.check(RB_TASK_TYPE_PLANNED_ID);
                String phaseNo = getString(R.string.task_phase).
                        concat(StringUtil.SPACE).concat(taskModel.getPhaseNo());
                mTaskPhaseExpandableListAdapter.setSelectedChildItemValue(
                        phaseNo);
            }

            // Name
            List<String> taskNameExpandableChildList = mTaskNameExpandableListAdapter.getChildList();
            boolean isTaskNameSelected = false;
            for (int i = 0; i < taskNameExpandableChildList.size(); i++) {
                if (taskModel.getTitle().equalsIgnoreCase(taskNameExpandableChildList.get(i))) {
                    mTaskNameExpandableListAdapter.setSelectedChildItemValue(taskModel.getTitle());
                    isTaskNameSelected = true;
                }
            }

            if (isTaskNameSelected) {
                mEtvTaskNameOthers.setText(taskModel.getTitle());
            }

            // Description
            mEtvTaskDescription.setText(taskModel.getDescription());
        }
    }

    /**
     * Populates form with 'Assign To' data that is to be updated
     * Separated from the other fields; Requires async-ed task to be completed in order to retrieve data
     *
     * @param taskModel
     */
    private void updateAssignToData(TaskModel taskModel) {
        if (taskModel != null) {
            String assignedToGroup = taskModel.getAssignedTo();

            Timber.i("Updating AssignTo info: Team(s) %s", assignedToGroup);

            String[] assignedToGroupsArray = StringUtil.removeCommasAndExtraSpaces(assignedToGroup);
            for (int i = 0; i < assignedToGroupsArray.length; i++) {
                String currentAssignedToTeam = assignedToGroupsArray[i];

                for (int j = 0; j < mLinearLayoutAssignTo.getChildCount(); j++) {
                    View viewBtnAssignToTeam = mLinearLayoutAssignTo.getChildAt(j);

                    C2OpenSansSemiBoldTextView tvAssignToTeam = viewBtnAssignToTeam.
                            findViewById(R.id.tv_img_text_fixed_dimension_text_img_btn);
                    AppCompatImageView imgAssignToTeamRadioBtn = viewBtnAssignToTeam.
                            findViewById(R.id.img_img_text_fixed_dimension_pic_img_btn);

                    String tvAssignToTeamWithoutLabel = StringUtil.removeFirstWord(
                            tvAssignToTeam.getText().toString().trim()).trim();

                    if (currentAssignedToTeam.equalsIgnoreCase(tvAssignToTeamWithoutLabel)) {
                        viewBtnAssignToTeam.setSelected(true);
                        setAssignToSelectedUI(viewBtnAssignToTeam, tvAssignToTeam, imgAssignToTeamRadioBtn);
                    }
                }
            }
        }
    }

    /**
     * Checks the link status of targeted users and current user to ensure that
     * Task can be sent successfully
     */
    private void checkNetworkLinkStatusOfRelevantParties() {
        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
        // asynchronously in the background thread and apply changes on the main UI thread
        SingleObserver<List<UserModel>> singleObserverAllUsers = new SingleObserver<List<UserModel>>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(List<UserModel> userModelList) {
                Timber.i(TAG, "onSuccess singleObserverAllUsers, " +
                        "checkNetworkLinkStatusOfRelevantParties. " +
                        "userModelList size: %d", userModelList.size());

                // Obtain current User model who is ONLINE from database
                List<UserModel> currentUserOnlineModelList = userModelList.stream().
                        filter(userModel -> SharedPreferenceUtil.getCurrentUserCallsignID().
                                equalsIgnoreCase(userModel.getUserId()) &&
                                userModel.getRadioFullConnectionStatus().
                                        equalsIgnoreCase(ERadioConnectionStatus.ONLINE.toString())).
                        collect(Collectors.toList());

                if (currentUserOnlineModelList.size() != 1) {
                    if (getSnackbarView() != null) {
                        SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                MainApplication.getAppContext().
                                        getString(R.string.snackbar_send_error_current_user_not_connected_message));
                    }
                } else {
                    // Check if targeted users are connected to network
                    List<String> tempSelectedUserIdList = new ArrayList<>();

                    for (int i = 0; i < mLinearLayoutAssignTo.getChildCount(); i++) {
                        View viewBtnAssignToTeam = mLinearLayoutAssignTo.getChildAt(i);

                        C2OpenSansSemiBoldTextView tvAssignToTeam = viewBtnAssignToTeam.
                                findViewById(R.id.tv_img_text_fixed_dimension_text_img_btn);

                        if (viewBtnAssignToTeam.isSelected()) {
                            String currentlySelectedAssignToTeam = StringUtil.removeFirstWord(
                                    tvAssignToTeam.getText().toString().trim());

                            tempSelectedUserIdList.add(currentlySelectedAssignToTeam);
                        }
                    }

                    // Obtain ALL 'Assign To' User models who are ONLINE from database
                    List<UserModel> assignToUserOnlineModelList = userModelList.stream().
                            filter(userModel -> tempSelectedUserIdList.contains(userModel.getUserId()) &&
                                    userModel.getRadioFullConnectionStatus().
                                            equalsIgnoreCase(ERadioConnectionStatus.ONLINE.toString())).
                            collect(Collectors.toList());

                    // Obtain ALL 'Assign To' User Ids who are ONLINE from database
                    List<String> assignToUserOnlineIdList = assignToUserOnlineModelList.stream().map(
                            UserModel::getUserId).collect(Collectors.toList());

                    for (int i = 0; i < assignToUserOnlineIdList.size(); i++) {
                        Timber.i("assignToUserOnlineIdList[%d] userid: %s",
                                i, assignToUserOnlineIdList.get(i));
                    }

                    boolean isAllSelectedUsersConnected = true;
                    String errorMsg = "";

                    for (int i = 0; i < tempSelectedUserIdList.size(); i++) {
                        if (!assignToUserOnlineIdList.contains(tempSelectedUserIdList.get(i))) {
                            isAllSelectedUsersConnected = false;
                            errorMsg = errorMsg.concat(tempSelectedUserIdList.get(i)).
                                    concat(StringUtil.COMMA).
                                    concat(StringUtil.SPACE);
                        }

                        if (errorMsg.length() > 2 && i == tempSelectedUserIdList.size() - 1) {
                            errorMsg = errorMsg.substring(0,
                                    errorMsg.length() - 2);
                            errorMsg = errorMsg.concat(StringUtil.SPACE);
                        }
                    }

                    if (!isAllSelectedUsersConnected) {
                        if (getSnackbarView() != null) {
                            if (!errorMsg.contains(StringUtil.COMMA)) {
                                errorMsg = errorMsg.concat(MainApplication.getAppContext().
                                        getString(R.string.snackbar_send_error_is_not_connected_message));
                            } else {
                                errorMsg = errorMsg.concat(MainApplication.getAppContext().
                                        getString(R.string.snackbar_send_error_are_not_connected_message));
                            }

                            SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                                    errorMsg);
                        }
                    } else {
                        // Only perform send/update action if 'Assign To' users and current user
                        // are CONNECTED to network
                        UserModel userModel = currentUserOnlineModelList.get(0);
                        performActionClick(userModel);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Timber.e("onError singleObserverAllUsers, " +
                        "checkNetworkLinkStatusOfRelevantParties. " +
                        "Error Msg: %s", e.toString());
            }
        };

        mUserViewModel.getAllUsers(singleObserverAllUsers);
    }

    private void performActionClick(UserModel userModel) {
        // Send button
        if (getString(R.string.btn_create).equalsIgnoreCase(
                mTvToolbarCreateOrUpdate.getText().toString().trim())) {

            // Create new Task, store in local database and broadcast to other connected devices
            TaskModel newTaskModel = createNewOrUpdateTaskModelFromForm(
                    false, userModel.getUserId());

            addItemToLocalDatabaseAndBroadcast(newTaskModel, userModel.getUserId());
//                    TaskRepository taskRepo = new TaskRepository((Application) MainApplication.getAppContext());
//                    DatabaseOperation databaseOperation = new DatabaseOperation();
//                    databaseOperation.insertTaskIntoDatabaseAndBroadcast(taskRepo, newTaskModel);

        } else {    // Update button

            // Update existing Task model
            TaskModel newTaskModel = createNewOrUpdateTaskModelFromForm(
                    true, userModel.getUserId());

            // Update local Task data
            mTaskViewModel.updateTask(newTaskModel);

            // Send updated Task data to other connected devices
            JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(newTaskModel);

            // Show snackbar message and return to Task edit fragment page
            if (getSnackbarView() != null) {
                SnackbarUtil.showCustomInfoSnackbar(mMainLayout, getSnackbarView(),
                        getString(R.string.snackbar_task_updated_message));
            }

            // Remove sticky TaskModel as update is complete and it is no longer needed
//                    if (getActivity() instanceof MainActivity) {
//                        Object objectToUpdate = ((MainActivity) getActivity()).
//                                getStickyModel(TaskModel.class.getSimpleName());
//                        if (objectToUpdate instanceof TaskModel) {
//                            ((MainActivity) getActivity()).
//                                    removeStickyModel(objectToUpdate);
//                        }
//                    }

            popChildBackStack();
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

    /**
     * Checks for existing data transferred over by Bundle
     */
    private void checkBundle(boolean isUpdateAssignTo) {
        // Checks if this current fragment is for creation of new Task or update of existing Task
        String fragmentType;
        String defaultValue = FragmentConstants.VALUE_TASK_ADD;
        Bundle bundle = this.getArguments();

        if (bundle != null) {
            fragmentType = bundle.getString(FragmentConstants.KEY_TASK, defaultValue);
        } else {
            fragmentType = defaultValue;
        }

        if (fragmentType.equalsIgnoreCase(FragmentConstants.VALUE_TASK_UPDATE)) {
            mTvToolbarCreateOrUpdate.setText(getString(R.string.btn_update));
            mLinearLayoutBtnCreateOrUpdate.setOnClickListener(onUpdateClickListener);

//            TaskModel taskModelToUpdate = EventBus.getDefault().getStickyEvent(TaskModel.class);
            TaskModel taskModelToUpdate = null;
            if (getActivity() instanceof MainActivity) {
                Object objectToUpdate = ((MainActivity) getActivity()).
                        getStickyModel(TaskModel.class.getSimpleName());
                if (objectToUpdate instanceof TaskModel) {
                    taskModelToUpdate = (TaskModel) objectToUpdate;
                }
            }

            if (taskModelToUpdate != null) {
                mTaskModelToUpdate = taskModelToUpdate;
            }

            if (isUpdateAssignTo) {
                updateAssignToData(mTaskModelToUpdate);
            } else {
                updateFormWithoutAssignToData(mTaskModelToUpdate);
            }
        }
    }

    @Override
    public void onSnackbarActionClick() {
//        performActionClick();
        checkNetworkLinkStatusOfRelevantParties();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mLayoutTaskNameInputOthers != null) {
            mLayoutTaskNameInputOthers.setSelected(mIsTaskNameInputOthersSelected);

            if (mLayoutTaskNameInputOthers.isSelected()) {
                setTaskNameInputOthersSelectedUI(mLayoutTaskNameInputOthers);
            } else {
                setTaskNameInputOthersUnselectedUI(mLayoutTaskNameInputOthers);
            }
        }

        if (mTaskNameExpandableListSelectedString != null) {
            mTaskNameExpandableListAdapter.setSelectedChildItemValue(mTaskNameExpandableListSelectedString);
        }

        if (mTaskPriorityExpandableListSelectedString != null) {
            mTaskPriorityExpandableListAdapter.setSelectedChildItemValue(mTaskPriorityExpandableListSelectedString);
        }

    }

//    @Override
//    public void onStart() {
//        super.onStart();
//
//        Log.i(TAG, "onStart.");
//    }
}
