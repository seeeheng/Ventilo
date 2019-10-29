package sg.gov.dsta.mobileC3.ventilo.activity.task;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.helper.SwipeHelper;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.TaskViewModel;
import sg.gov.dsta.mobileC3.ventilo.network.jeroMQ.JeroMQBroadcastOperation;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DrawableUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.EIsValid;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EAdHocTaskPriority;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EPhaseNo;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EStatus;
import timber.log.Timber;

public class TaskFragment extends Fragment {

    private static final String TAG = TaskFragment.class.getSimpleName();

    // View Models
//    private UserViewModel mUserViewModel;
    private TaskViewModel mTaskViewModel;
//    private UserTaskJoinViewModel mUserTaskJoinViewModel;

    // Main layout
    private View mRootView;

    // Total count dashboard
    private View mLayoutTotalCountDashboard;
    private C2OpenSansSemiBoldTextView mTvTotalCountTitle;
    private C2OpenSansSemiBoldTextView mTvTaskCompletedCountTitle;
    private C2OpenSansSemiBoldTextView mTvTaskInProgressCountTitle;
    private C2OpenSansSemiBoldTextView mTvTaskNewCountTitle;
    private C2OpenSansRegularTextView mTvTotalCountNumber;
    private C2OpenSansRegularTextView mTvTaskCompletedCountNumber;
    private C2OpenSansRegularTextView mTvTaskInProgressCountNumber;
    private C2OpenSansRegularTextView mTvTaskNewCountNumber;

    // Add new item UI
    private View mLayoutAddNewItem;
    private C2OpenSansRegularTextView mTvAddNewItemCategory;

    private RecyclerView mRecyclerView;
    private TaskRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    //    private TextView mToolbarTitleTextView;
//    private TextView mToolbarLeftActivityLinkTextView;
    private FloatingActionButton mFabAddTask;
//    private ImageButton mImgBtnMenu;

    private List<TaskModel> mTaskListItems;

    private boolean mIsFragmentVisibleToUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setRetainInstance(true);
        observerSetup();

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_task, container, false);
            initUI(mRootView);
        }

        return mRootView;
    }

    private void initUI(View rootView) {
        initAddNewItemUI(rootView);

        mLayoutTotalCountDashboard = rootView.findViewById(R.id.layout_total_count_dashboard);
        initTotalCountDashboardUI(mLayoutTotalCountDashboard);

        //        mToolbarTitleTextView = rootTasksView.findViewById(R.id.tv_fragment_toolbar_cancel_done_title);
//        mToolbarLeftActivityLinkTextView = rootTasksView.findViewById(R.id.tv_others_left_activity_link);
        mRecyclerView = rootView.findViewById(R.id.recycler_task);

//        mToolbarTitleTextView.setText(getString(R.string.toolbar_tasks_title));
//        mToolbarLeftActivityLinkTextView.setText(getString(R.string.toolbar_tasks_tv_filter));
        mRecyclerView.setHasFixedSize(false);

        mRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        mRecyclerView.addOnItemTouchListener(new TaskRecyclerItemTouchListener(getContext(), mRecyclerView, new TaskRecyclerItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                navigateToTaskDetailFragment(mTaskListItems.get(position));
            }

            @Override
            public void onLongItemClick(View view, int position) {
//                Toast.makeText(getContext(), "Task onItemLongClick" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeLeft(View view, int position) {
//                RelativeLayout relativeLayoutDeleteIcon = view.findViewById(R.id.layout_recycler_task_delete);
//                RelativeLayout relativeLayoutStartIcon = view.findViewById(R.id.layout_recycler_task_start);
//                RelativeLayout relativeLayoutDoneIcon = view.findViewById(R.id.layout_recycler_task_done);
//
//                if (relativeLayoutDeleteIcon.getVisibility() == View.VISIBLE) {
//                    relativeLayoutDeleteIcon.setVisibility(View.GONE);
//                } else {
//                    if (mTaskListItems.get(position).getStatus() == EStatus.NEW.toString()) {
//                        relativeLayoutStartIcon.setVisibility(View.VISIBLE);
//                    } else if (mTaskListItems.get(position).getStatus() == EStatus.IN_PROGRESS.toString()) {
//                        relativeLayoutDoneIcon.setVisibility(View.VISIBLE);
//                    }
//                }
//
//                relativeLayoutDeleteIcon.bringToFront();
//                relativeLayoutStartIcon.bringToFront();
//                relativeLayoutDoneIcon.bringToFront();
            }

            @Override
            public void onSwipeRight(View view, int position) {
//                RelativeLayout relativeLayoutDeleteIcon = view.findViewById(R.id.layout_recycler_task_delete);
//                RelativeLayout relativeLayoutStartIcon = view.findViewById(R.id.layout_recycler_task_start);
//                RelativeLayout relativeLayoutDoneIcon = view.findViewById(R.id.layout_recycler_task_done);
//
//                if (relativeLayoutStartIcon.getVisibility() == View.VISIBLE ||
//                        relativeLayoutDoneIcon.getVisibility() == View.VISIBLE) {
//                    relativeLayoutStartIcon.setVisibility(View.GONE);
//                    relativeLayoutDoneIcon.setVisibility(View.GONE);
//                } else {
//                    relativeLayoutDeleteIcon.setVisibility(View.VISIBLE);
//                }
//
//                relativeLayoutDeleteIcon.bringToFront();
//                relativeLayoutStartIcon.bringToFront();
//                relativeLayoutDoneIcon.bringToFront();
            }
        }));

        // Set data for recycler view
        if (mTaskListItems == null) {
            mTaskListItems = new ArrayList<>();
        }
//        setUpRecyclerData();

        mRecyclerAdapter = new TaskRecyclerAdapter(getContext(), mTaskListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // CCT will be able to view but NOT change task's status
//        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
//                SharedPreferenceUtil.getCurrentUserAccessRight())) {
            setUpSwipeHelper();
//        }

        // Set up floating action button
        mFabAddTask = rootView.findViewById(R.id.fab_task_add);

        if (EAccessRight.CCT.toString().equalsIgnoreCase(
                SharedPreferenceUtil.getCurrentUserAccessRight())) {
            mFabAddTask.show();
            mFabAddTask.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFabAddTask.setEnabled(false);

                    Fragment taskAddUpdateFragment = new TaskAddUpdateFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(FragmentConstants.KEY_TASK, FragmentConstants.VALUE_TASK_ADD);
                    taskAddUpdateFragment.setArguments(bundle);

                    navigateToTaskAddUpdateFragment();
                }
            });
        } else {
            if (mFabAddTask.isShown()) {
                mFabAddTask.hide();
            }
        }
//        initOtherLayouts(inflater, container);
    }

    /**
     * Creates message when no Task is available
     * @param rootView
     */
    private void initAddNewItemUI(View rootView) {
        mLayoutAddNewItem = rootView.findViewById(R.id.layout_task_add_new_item);
        C2OpenSansRegularTextView tvAddNewItemCategory = mLayoutAddNewItem.findViewById(R.id.tv_add_new_item_category);

        // Team Leads can only view Task from CCT
        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                SharedPreferenceUtil.getCurrentUserAccessRight())) {
            AppCompatImageView imgAddNewItemCategory = mLayoutAddNewItem.findViewById(R.id.img_add_new_item_category);
            C2OpenSansRegularTextView tvAddNewItemCategoryBelow = mLayoutAddNewItem.findViewById(R.id.tv_add_new_item_category_below);
            imgAddNewItemCategory.setVisibility(View.GONE);
            tvAddNewItemCategoryBelow.setVisibility(View.GONE);
            tvAddNewItemCategory.setText(getString(R.string.no_task));

        } else { // CCT can create/view Task
            tvAddNewItemCategory.setText(MainApplication.getAppContext().getString(R.string.add_new_item_task));
        }
    }

    /**
     * Init dashboard UI displaying total count of Task Completed, In Progress and New
     *
     * @param layoutTotalCountView
     */
    private void initTotalCountDashboardUI(View layoutTotalCountView) {
        mTvTotalCountTitle = layoutTotalCountView.findViewById(R.id.tv_total_count_title);
        mTvTaskCompletedCountTitle = layoutTotalCountView.findViewById(R.id.tv_first_count_title);
        mTvTaskInProgressCountTitle = layoutTotalCountView.findViewById(R.id.tv_second_count_title);
        mTvTaskNewCountTitle = layoutTotalCountView.findViewById(R.id.tv_third_count_title);

        // Task 'Total' title
        mTvTotalCountTitle.setText(getString(R.string.task_total_count_title));
        mTvTotalCountTitle.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_highlight_cyan, null));

        // Task 'Completed' title
        mTvTaskCompletedCountTitle.setText(getString(R.string.task_status_completed));
        mTvTaskCompletedCountTitle.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.dull_green, null));

        // Task 'In Progress' title
        mTvTaskInProgressCountTitle.setText(getString(R.string.task_status_in_progress));
        mTvTaskInProgressCountTitle.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.task_status_yellow, null));

        // Task 'New' title
        mTvTaskNewCountTitle.setText(getString(R.string.task_status_new));
        mTvTaskNewCountTitle.setTextColor(ResourcesCompat.getColor(getResources(),
                R.color.primary_white, null));

        mTvTotalCountNumber = layoutTotalCountView.findViewById(R.id.tv_total_count_number);
        mTvTaskCompletedCountNumber = layoutTotalCountView.findViewById(R.id.tv_first_count_number);
        mTvTaskInProgressCountNumber = layoutTotalCountView.findViewById(R.id.tv_second_count_number);
        mTvTaskNewCountNumber = layoutTotalCountView.findViewById(R.id.tv_third_count_number);
    }

    /**
     * This identifies each task's status of the recycler view and creates the corresponding action
     * swipe buttons with ItemTouchHelper.SimpleCallback
     */
    private void setUpSwipeHelper() {
        SwipeHelper taskSwipeHelper = new SwipeHelper(getContext(), mRecyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                if (viewHolder instanceof TaskViewHolder) {

                    // Team Lead is able to set task's status but NOT delete them
                    if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                            SharedPreferenceUtil.getCurrentUserAccessRight())) {
                        TaskViewHolder taskViewHolder = (TaskViewHolder) viewHolder;
                        String taskStatus = taskViewHolder.getTvStatus().getText().toString().trim();

                        int firstSwipeActionButtonImageId = 0;
                        int secondSwipeActionButtonImageId = 0;

                        if (EStatus.NEW.toString().equalsIgnoreCase(taskStatus)) {
                            firstSwipeActionButtonImageId = R.drawable.btn_task_swipe_action_status_in_progress;
                            secondSwipeActionButtonImageId = R.drawable.btn_task_swipe_action_status_complete;

                        } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(taskStatus)) {
                            firstSwipeActionButtonImageId = R.drawable.btn_task_swipe_action_status_new;
                            secondSwipeActionButtonImageId = R.drawable.btn_task_swipe_action_status_complete;

                        } else {
                            firstSwipeActionButtonImageId = R.drawable.btn_task_swipe_action_status_new;
                            secondSwipeActionButtonImageId = R.drawable.btn_task_swipe_action_status_in_progress;
                        }

                        final Drawable firstSwipeActionButtonImageDrawable =
                                ResourcesCompat.getDrawable(getResources(),
                                        firstSwipeActionButtonImageId, null);

                        SwipeHelper.UnderlayButton firstUnderlayButton = new SwipeHelper.UnderlayButton(
                                "", firstSwipeActionButtonImageDrawable, null,
                                new SwipeHelper.UnderlayButtonClickListener() {
                                    @Override
                                    public void onClick(int pos) {
                                        if (DrawableUtil.areDrawablesIdentical(firstSwipeActionButtonImageDrawable,
                                                ResourcesCompat.getDrawable(getResources(),
                                                        R.drawable.btn_task_swipe_action_status_new, null))) {
                                            setTaskStatusNew(pos);
                                        } else if (DrawableUtil.areDrawablesIdentical(firstSwipeActionButtonImageDrawable,
                                                ResourcesCompat.getDrawable(getResources(),
                                                        R.drawable.btn_task_swipe_action_status_in_progress, null))) {
                                            setTaskStatusInProgress(pos);
                                        } else {
                                            setTaskStatusComplete(pos);
                                        }
                                    }
                                }
                        );

                        final Drawable secondSwipeActionButtonImageDrawable =
                                ResourcesCompat.getDrawable(getResources(),
                                        secondSwipeActionButtonImageId, null);

                        SwipeHelper.UnderlayButton secondUnderlayButton = new SwipeHelper.UnderlayButton(
                                "", secondSwipeActionButtonImageDrawable, null,
                                new SwipeHelper.UnderlayButtonClickListener() {
                                    @Override
                                    public void onClick(int pos) {
                                        if (DrawableUtil.areDrawablesIdentical(secondSwipeActionButtonImageDrawable,
                                                ResourcesCompat.getDrawable(getResources(),
                                                        R.drawable.btn_task_swipe_action_status_new, null))) {
                                            setTaskStatusNew(pos);
                                        } else if (DrawableUtil.areDrawablesIdentical(secondSwipeActionButtonImageDrawable,
                                                ResourcesCompat.getDrawable(getResources(),
                                                        R.drawable.btn_task_swipe_action_status_in_progress, null))) {
                                            setTaskStatusInProgress(pos);
                                        } else {
                                            setTaskStatusComplete(pos);
                                        }
                                    }
                                }
                        );

                        underlayButtons.add(secondUnderlayButton);
                        underlayButtons.add(firstUnderlayButton);

                    } else { // CCT will only be able to delete tasks
                        Drawable deleteSwipeActionButtonImageDrawable =
                                ResourcesCompat.getDrawable(getResources(),
                                        R.drawable.btn_swipe_action_status_delete, null);

                        SwipeHelper.UnderlayButton deleteUnderlayButton = new SwipeHelper.UnderlayButton(
                                "", deleteSwipeActionButtonImageDrawable, null,
                                new SwipeHelper.UnderlayButtonClickListener() {
                                    @Override
                                    public void onClick(int pos) {
                                        deleteTask(pos);
                                    }
                                }
                        );

                        underlayButtons.add(deleteUnderlayButton);
                    }
                }
            }
        };

        // Attach touch helper to recycler view
//        TaskItemTouchHelperCallback taskItemTouchHelperCallback = new
//                TaskItemTouchHelperCallback(this);
//        new ItemTouchHelper(taskItemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        taskSwipeHelper.attachSwipe();
    }

    private void deleteTask(int position) {
        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
        mTaskViewModel.deleteTask(taskModelAtPos.getId());
        JeroMQBroadcastOperation.broadcastDataDeletionOverSocket(taskModelAtPos);
    }

    private void setTaskStatusComplete(int position) {
        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
        taskModelAtPos = updateTaskStatusAndCompletedDateTimeOfCurrentUser(taskModelAtPos,
                EStatus.COMPLETE.toString());
        mTaskViewModel.updateTask(taskModelAtPos);
        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(taskModelAtPos);
    }

    private void setTaskStatusInProgress(int position) {
        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
        taskModelAtPos = updateTaskStatusAndCompletedDateTimeOfCurrentUser(taskModelAtPos,
                EStatus.IN_PROGRESS.toString());
        mTaskViewModel.updateTask(taskModelAtPos);
        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(taskModelAtPos);
    }

    private void setTaskStatusNew(int position) {
        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
        taskModelAtPos = updateTaskStatusAndCompletedDateTimeOfCurrentUser(taskModelAtPos,
                EStatus.NEW.toString());
        mTaskViewModel.updateTask(taskModelAtPos);
        JeroMQBroadcastOperation.broadcastDataUpdateOverSocket(taskModelAtPos);
    }

    /**
     * Update Task status and completed date time for current user
     * e.g. if current user is 456 and changes status to 'Complete'
     * with the following field data for particular task model:
     * 1) assignedPersonnel - 123, 456
     * 2) status            - New, In Progress
     * will be changed to:
     * 1) assignedPersonnel - 123, 456
     * 2) status            - New, Complete
     *
     * @param taskModelAtPos
     * @param newStatus
     * @return
     */
    private TaskModel updateTaskStatusAndCompletedDateTimeOfCurrentUser(TaskModel taskModelAtPos, String newStatus) {
        String[] assignedToPersonnel = StringUtil.removeCommasAndExtraSpaces(taskModelAtPos.getAssignedTo());
        String[] statusGroup = StringUtil.removeCommasAndExtraSpaces(taskModelAtPos.getStatus());
        String[] completedDateTimeGroup = StringUtil.removeCommasAndExtraSpaces(taskModelAtPos.getCompletedDateTime());
        String[] lastUpdatedDateTimeGroup = StringUtil.removeCommasAndExtraSpaces(taskModelAtPos.getLastUpdatedStatusDateTime());

        for (int i = 0; i < assignedToPersonnel.length; i++) {
            if (assignedToPersonnel[i].equalsIgnoreCase(
                    SharedPreferenceUtil.getCurrentUserCallsignID())) {
                statusGroup[i] = newStatus;

                if (newStatus.equalsIgnoreCase(EStatus.COMPLETE.toString())) {
                    completedDateTimeGroup[i] = DateTimeUtil.getCurrentDateTime();
                } else {
                    completedDateTimeGroup[i] = StringUtil.INVALID_STRING;
                }

                lastUpdatedDateTimeGroup[i] = DateTimeUtil.getCurrentDateTime();
            }
        }

        String status = String.join(StringUtil.COMMA, statusGroup);
        String completedDateTime = String.join(StringUtil.COMMA, completedDateTimeGroup);
        String lastUpdatedDateTime = String.join(StringUtil.COMMA, lastUpdatedDateTimeGroup);
        taskModelAtPos.setStatus(status);
        taskModelAtPos.setCompletedDateTime(completedDateTime);
        taskModelAtPos.setLastUpdatedStatusDateTime(lastUpdatedDateTime);

        return taskModelAtPos;
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

//    private void startItemInRecycler(int position) {
//        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
//        taskModelAtPos.setStatus(EStatus.IN_PROGRESS.toString());
//        mTaskViewModel.updateTask(taskModelAtPos);
//    }
//
//    private void completeItemInRecycler(int position) {
//        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
//        taskModelAtPos.setStatus(EStatus.COMPLETE.toString());
//        mTaskViewModel.updateTask(taskModelAtPos);
//    }
//
//    private void removeItemInRecycler(int position) {
//        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
//        mTaskViewModel.deleteTask(taskModelAtPos.getId());
//
////        mTaskListItems.remove(position);
////        mRecyclerView.removeViewAt(position);
////        mRecyclerAdapter.notifyItemRemoved(position);
////        mRecyclerAdapter.notifyItemRangeChanged(position, mTaskListItems.size());
////
////        removeSingleItemFromLocalDatabase(position);
//
//    }

    public void addItemInRecycler() {
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyItemInserted(mTaskListItems.size() - 1);
            mRecyclerAdapter.notifyItemRangeChanged(mTaskListItems.size() - 1, mTaskListItems.size());
//            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private void refreshDashboardUI() {
        if (mTaskListItems.size() == 0) {
            mLayoutAddNewItem.setVisibility(View.VISIBLE);
            mLayoutTotalCountDashboard.setVisibility(View.GONE);
        } else {
            mLayoutAddNewItem.setVisibility(View.GONE);
            mLayoutTotalCountDashboard.setVisibility(View.VISIBLE);

            int totalTaskCompleted = 0;
            int totalTaskInProgress = 0;
            int totalTaskNew = 0;

            for (int i = 0; i < mTaskListItems.size(); i++) {
                String taskStatus = mTaskListItems.get(i).getStatus();
                if (EStatus.COMPLETE.toString().equalsIgnoreCase(taskStatus)) {
                    totalTaskCompleted++;
                } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(taskStatus)) {
                    totalTaskInProgress++;
                } else {
                    totalTaskNew++;
                }
            }

            int totalCount = totalTaskCompleted + totalTaskInProgress + totalTaskNew;
            mTvTotalCountNumber.setText(String.valueOf(totalCount));
            mTvTaskCompletedCountNumber.setText(String.valueOf(totalTaskCompleted));
            mTvTaskInProgressCountNumber.setText(String.valueOf(totalTaskInProgress));
            mTvTaskNewCountNumber.setText(String.valueOf(totalTaskNew));
        }
    }

    /**
     * Sort by urgency (Ad Hoc (High or Low), Phase), then by status (e.g. Completed, In Progress and New)
     *
     * @param taskModelList
     * @return
     */
    private List<TaskModel> sortTaskListItems(List<TaskModel> taskModelList) {
        List<TaskModel> sortedTaskListItemsByUrgency = sortTaskListByUrgency(taskModelList);
        List<TaskModel> sortedTaskListItems = sortTaskListByStatus(sortedTaskListItemsByUrgency);

        return sortedTaskListItems;
    }

    /**
     * Sort task items in the following order:
     * 1) Ad Hoc High Priority Task
     * 2) Phase Task
     * 3) Ad Hoc Low Priority Task
     * <p>
     * For each group of list items, sort according to time from earliest to most recent.
     *
     * @param taskModelList
     * @return
     */
    private List<TaskModel> sortTaskListByUrgency(List<TaskModel> taskModelList) {
        List<TaskModel> sortTaskListByUrgency = new ArrayList<>();

        List<TaskModel> adHocHighTaskPriorityListItems = new ArrayList<>();
        List<TaskModel> phaseListItems = new ArrayList<>();
        List<TaskModel> adHocLowTaskPriorityListItems = new ArrayList<>();

        for (int i = 0; i < taskModelList.size(); i++) {
            if (taskModelList.get(i).getPhaseNo().equalsIgnoreCase(EPhaseNo.AD_HOC.toString())) {
                if (taskModelList.get(i).getAdHocTaskPriority().
                        equalsIgnoreCase(EAdHocTaskPriority.HIGH.toString())) {
                    adHocHighTaskPriorityListItems.add(taskModelList.get(i));
                } else {
                    adHocLowTaskPriorityListItems.add(taskModelList.get(i));
                }
            } else {
                phaseListItems.add(taskModelList.get(i));
            }
        }

        Collections.sort(adHocHighTaskPriorityListItems, new Comparator<TaskModel>() {
            public int compare(TaskModel taskModelOne, TaskModel taskModelTwo) {
                if (taskModelOne.getCreatedDateTime() == null ||
                        taskModelTwo.getCreatedDateTime() == null)
                    return 0;
                return taskModelOne.getCreatedDateTime().compareTo(taskModelTwo.getCreatedDateTime());
            }
        });

        Collections.sort(phaseListItems, new Comparator<TaskModel>() {
            public int compare(TaskModel taskModelOne, TaskModel taskModelTwo) {
                if (taskModelOne.getCreatedDateTime() == null ||
                        taskModelTwo.getCreatedDateTime() == null)
                    return 0;
                return taskModelOne.getCreatedDateTime().compareTo(taskModelTwo.getCreatedDateTime());
            }
        });

        Collections.sort(adHocLowTaskPriorityListItems, new Comparator<TaskModel>() {
            public int compare(TaskModel taskModelOne, TaskModel taskModelTwo) {
                if (taskModelOne.getCreatedDateTime() == null ||
                        taskModelTwo.getCreatedDateTime() == null)
                    return 0;
                return taskModelOne.getCreatedDateTime().compareTo(taskModelTwo.getCreatedDateTime());
            }
        });

        sortTaskListByUrgency.addAll(adHocHighTaskPriorityListItems);
        sortTaskListByUrgency.addAll(phaseListItems);
        sortTaskListByUrgency.addAll(adHocLowTaskPriorityListItems);

        return sortTaskListByUrgency;
    }

    /**
     * Sort task items in the following order:
     * 1) Completed
     * 2) In Progress
     * 3) New
     *
     * @param taskModelList
     * @return
     */
    private List<TaskModel> sortTaskListByStatus(List<TaskModel> taskModelList) {
        List<TaskModel> sortedTaskListItemsByStatus = new ArrayList<>();

        List<TaskModel> completedTaskListItems = new ArrayList<>();
        List<TaskModel> inProgressTaskListItems = new ArrayList<>();
        List<TaskModel> newTaskListItems = new ArrayList<>();

        String taskStatus;

        for (int i = 0; i < taskModelList.size(); i++) {

            // Show current user's task status if access right is NOT 'CCT'
            // Else, show overall task status from all teams
            if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                    SharedPreferenceUtil.getCurrentUserAccessRight())) {
                taskStatus = getCurrentUserTaskStatus(taskModelList.get(i));
            } else {
                taskStatus = getFinalTaskStatus(taskModelList.get(i));
            }

            if (taskStatus.equalsIgnoreCase(EStatus.COMPLETE.toString())) {
                completedTaskListItems.add(taskModelList.get(i));
            } else if (taskStatus.equalsIgnoreCase(EStatus.IN_PROGRESS.toString())) {
                inProgressTaskListItems.add(taskModelList.get(i));
            } else {
                newTaskListItems.add(taskModelList.get(i));
            }
        }

        sortedTaskListItemsByStatus.addAll(inProgressTaskListItems);
        sortedTaskListItemsByStatus.addAll(newTaskListItems);
        sortedTaskListItemsByStatus.addAll(completedTaskListItems);

        return sortedTaskListItemsByStatus;
    }

    /**
     * Get task status based on the following:
     * Status: Complete     - When ALL teams have completed their tasks
     * Status: New          - When ALL teams have not started their tasks
     * Status: In Progress  - All other scenarios not mentioned above
     *
     * @param taskModelAtPos
     * @return
     */
    private String getFinalTaskStatus(TaskModel taskModelAtPos) {
        String[] statusGroup = StringUtil.removeCommasAndExtraSpaces(taskModelAtPos.getStatus());

        String finalStatus;
        boolean isAllCompleted = true;
        boolean isAllNew = true;

        for (int i = 0; i < statusGroup.length; i++) {
            if (EStatus.NEW.toString().equalsIgnoreCase(statusGroup[i])) {
                isAllCompleted = false;
            } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(statusGroup[i])) {
                isAllCompleted = false;
                isAllNew = false;
            } else {
                isAllNew = false;
            }
        }

        if (isAllCompleted) {
            finalStatus = EStatus.COMPLETE.toString();
        } else if (isAllNew) {
            finalStatus = EStatus.IN_PROGRESS.toString();
        } else {
            finalStatus = EStatus.NEW.toString();
        }

        return finalStatus;
    }

    private String getCurrentUserTaskStatus(TaskModel taskModelAtPos) {
        String[] assignedToPersonnel = StringUtil.removeCommasAndExtraSpaces(taskModelAtPos.getAssignedTo());
        String[] statusGroup = StringUtil.removeCommasAndExtraSpaces(taskModelAtPos.getStatus());

        for (int i = 0; i < assignedToPersonnel.length; i++) {
            if (assignedToPersonnel[i].equalsIgnoreCase(
                    SharedPreferenceUtil.getCurrentUserCallsignID())) {
                return statusGroup[i];
            }
        }

        return StringUtil.EMPTY_STRING;
    }

    /**
     * Extracts a list of all Tasks of current user
     *
     * @param taskModelList
     * @return
     */
    private List<TaskModel> getCurrentUserTasks(List<TaskModel> taskModelList) {

        List<TaskModel> currentUserTaskList = taskModelList.stream().
                filter(TaskModel -> TaskModel.getAssignedTo().
                        contains(SharedPreferenceUtil.getCurrentUserCallsignID())).
                collect(Collectors.toList());

        return currentUserTaskList;
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
//        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mTaskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
//        mUserTaskJoinViewModel = ViewModelProviders.of(this).get(UserTaskJoinViewModel.class);

        /*
         * Refreshes recyclerview UI whenever there is a change in task (insert, update or delete)
         */
        mTaskViewModel.getAllTasksLiveData().observe(this, new Observer<List<TaskModel>>() {
            @Override
            public void onChanged(@Nullable List<TaskModel> taskModelList) {

                synchronized (mTaskListItems) {

                    List<TaskModel> validTaskModelList = null;

                    if (taskModelList != null) {
                        validTaskModelList = getAllValidTaskListFromDatabase(taskModelList);
                    }

                    if (mTaskListItems == null) {
                        mTaskListItems = new ArrayList<>();
                    } else {
                        mTaskListItems.clear();
                    }

                    if (validTaskModelList != null) {
                        List<TaskModel> taskModelListForDisplay;

                        // Show only current user's task list if access right is NOT 'CCT'
                        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                                SharedPreferenceUtil.getCurrentUserAccessRight())) {
                            taskModelListForDisplay = getCurrentUserTasks(validTaskModelList);

                        } else {
                            taskModelListForDisplay = validTaskModelList;
                        }

                        // Sort task list accordingly based on importance (more details in method)
                        List<TaskModel> sortedTaskModelList = sortTaskListItems(taskModelListForDisplay);
                        mTaskListItems.addAll(sortedTaskModelList);
                    }

                    if (mRecyclerAdapter != null) {
                        mRecyclerAdapter.setTaskListItems(mTaskListItems);
                    }

                    refreshDashboardUI();
                }
            }
        });
    }

    /**
     * Navigate to another fragment which displays details of selected Task
     *
     * @param taskModel
     */
    private void navigateToTaskDetailFragment(TaskModel taskModel) {
        Fragment taskDetailFragment = new TaskDetailFragment();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).postStickyModel(taskModel);
        }
//        EventBus.getDefault().postSticky(taskModel);

        // Pass info to fragment
        navigateToFragment(taskDetailFragment);
    }

    /**
     * Navigate to another fragment to add a new Task
     *
     */
    private void navigateToTaskAddUpdateFragment() {
        Fragment taskAddUpdateFragment = new TaskAddUpdateFragment();
        navigateToFragment(taskAddUpdateFragment);
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
                    R.id.layout_task_fragment, this, toFragment);
            enableFabAddTask();
        }
    }

    /**
     * Allows other fragments to re-enable Floating Action Button
     */
    public void enableFabAddTask() {
        if (mFabAddTask != null && !mFabAddTask.isEnabled()) {
            mFabAddTask.setEnabled(true);
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
        enableFabAddTask();
        mRecyclerAdapter.notifyDataSetChanged();
    }

    private void onInvisible() {
        Timber.i("onInvisible");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;

        Timber.i("setUserVisibleHint");

        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                Timber.i("setUserVisibleHint onVisible");

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
}
