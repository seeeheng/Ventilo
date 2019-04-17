package sg.gov.dsta.mobileC3.ventilo.activity.task;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.helper.SwipeHelper;
import sg.gov.dsta.mobileC3.ventilo.model.eventbus.TaskEvent;
import sg.gov.dsta.mobileC3.ventilo.model.join.UserTaskJoin;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.TaskViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserTaskJoinViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DrawableUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class TaskFragment extends Fragment {

    private static final String TAG = TaskFragment.class.getSimpleName();

    // View Models
    private UserViewModel mUserViewModel;
    private TaskViewModel mTaskViewModel;
    private UserTaskJoinViewModel mUserTaskJoinViewModel;

    private RecyclerView mRecyclerView;
    private TaskRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    //    private TextView mToolbarTitleTextView;
//    private TextView mToolbarLeftActivityLinkTextView;
    private FloatingActionButton mFabAddTask;
//    private ImageButton mImgBtnMenu;

    private List<TaskModel> mTaskListItems;

    private boolean mIsVisibleToUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_task, container, false);
        observerSetup();
        initUI(inflater, container, rootView);

        return rootView;
    }

    private void initUI(LayoutInflater inflater, ViewGroup container, View rootView) {
        //        mToolbarTitleTextView = rootTasksView.findViewById(R.id.tv_fragment_toolbar_cancel_done_title);
//        mToolbarLeftActivityLinkTextView = rootTasksView.findViewById(R.id.tv_others_left_activity_link);
        mRecyclerView = rootView.findViewById(R.id.recycler_task);

//        mToolbarTitleTextView.setText(getString(R.string.toolbar_tasks_title));
//        mToolbarLeftActivityLinkTextView.setText(getString(R.string.toolbar_tasks_tv_filter));
        mRecyclerView.setHasFixedSize(false);

        mRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
//        mRecyclerView.addOnItemTouchListener(new TaskRecyclerItemTouchListener(getContext(), mRecyclerView, new TaskRecyclerItemTouchListener.OnItemClickListener() {
//            @Override
//            public void onItemClick(View view, int position) {
////                Toast.makeText(getContext(),"Task onItemClick" + "(" + position + ")", Toast.LENGTH_SHORT).show();
//
////                RelativeLayout relativeLayoutDeleteIcon = view.findViewById(R.id.layout_recycler_task_delete);
////                RelativeLayout relativeLayoutStartIcon = view.findViewById(R.id.layout_recycler_task_start);
////                RelativeLayout relativeLayoutDoneIcon = view.findViewById(R.id.layout_recycler_task_done);
////
////                if (relativeLayoutDeleteIcon.getVisibility() == View.VISIBLE) {
////                    removeItemInRecycler(position);
////                } else if (relativeLayoutStartIcon.getVisibility() == View.VISIBLE) {
////                    startItemInRecycler(position);
////                } else if (relativeLayoutDoneIcon.getVisibility() == View.VISIBLE) {
////                    completeItemInRecycler(position);
////                } else {
//                    Fragment taskDetailFragment = new TaskDetailFragment();
//                    Bundle bundle = new Bundle();
//                    bundle.putString(FragmentConstants.KEY_TASK, FragmentConstants.VALUE_TASK_VIEW);
//                    bundle.putString(FragmentConstants.KEY_TASK_TITLE, mTaskListItems.get(position).getTitle());
//                    bundle.putString(FragmentConstants.KEY_TASK_DESCRIPTION, mTaskListItems.get(position).getDescription());
//                    taskDetailFragment.setArguments(bundle);
//
//                    // Pass info to fragment
//                    FragmentManager fm = getActivity().getSupportFragmentManager();
//                    FragmentTransaction ft = fm.beginTransaction();
//                    ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
//                    ft.replace(R.id.layout_task_fragment, taskDetailFragment, taskDetailFragment.getClass().getSimpleName());
//                    ft.addToBackStack(taskDetailFragment.getClass().getSimpleName());
//                    ft.commit();
////                }
//            }
//
//            @Override
//            public void onLongItemClick(View view, int position) {
//                Toast.makeText(getContext(), "Task onItemLongClick" + "(" + position + ")", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onSwipeLeft(View view, int position) {
////                RelativeLayout relativeLayoutDeleteIcon = view.findViewById(R.id.layout_recycler_task_delete);
////                RelativeLayout relativeLayoutStartIcon = view.findViewById(R.id.layout_recycler_task_start);
////                RelativeLayout relativeLayoutDoneIcon = view.findViewById(R.id.layout_recycler_task_done);
////
////                if (relativeLayoutDeleteIcon.getVisibility() == View.VISIBLE) {
////                    relativeLayoutDeleteIcon.setVisibility(View.GONE);
////                } else {
////                    if (mTaskListItems.get(position).getStatus() == EStatus.NEW.toString()) {
////                        relativeLayoutStartIcon.setVisibility(View.VISIBLE);
////                    } else if (mTaskListItems.get(position).getStatus() == EStatus.IN_PROGRESS.toString()) {
////                        relativeLayoutDoneIcon.setVisibility(View.VISIBLE);
////                    }
////                }
////
////                relativeLayoutDeleteIcon.bringToFront();
////                relativeLayoutStartIcon.bringToFront();
////                relativeLayoutDoneIcon.bringToFront();
//            }
//
//            @Override
//            public void onSwipeRight(View view, int position) {
////                RelativeLayout relativeLayoutDeleteIcon = view.findViewById(R.id.layout_recycler_task_delete);
////                RelativeLayout relativeLayoutStartIcon = view.findViewById(R.id.layout_recycler_task_start);
////                RelativeLayout relativeLayoutDoneIcon = view.findViewById(R.id.layout_recycler_task_done);
////
////                if (relativeLayoutStartIcon.getVisibility() == View.VISIBLE ||
////                        relativeLayoutDoneIcon.getVisibility() == View.VISIBLE) {
////                    relativeLayoutStartIcon.setVisibility(View.GONE);
////                    relativeLayoutDoneIcon.setVisibility(View.GONE);
////                } else {
////                    relativeLayoutDeleteIcon.setVisibility(View.VISIBLE);
////                }
////
////                relativeLayoutDeleteIcon.bringToFront();
////                relativeLayoutStartIcon.bringToFront();
////                relativeLayoutDoneIcon.bringToFront();
//            }
//        }));

        // Set data for recycler view
        setUpRecyclerData();

        mRecyclerAdapter = new TaskRecyclerAdapter(getContext(), mTaskListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());


        /**
         * This identifies each task's status of the recycler view and creates the corresponding action
         * swipe buttons with ItemTouchHelper.SimpleCallback
         */
        SwipeHelper taskSwipeHelper = new SwipeHelper(getContext(), mRecyclerView) {
            @Override
            public void instantiateUnderlayButton(RecyclerView.ViewHolder viewHolder, List<UnderlayButton> underlayButtons) {
                if (viewHolder instanceof TaskViewHolder) {
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

                    Drawable deleteSwipeActionButtonImageDrawable =
                            ResourcesCompat.getDrawable(getResources(),
                                    R.drawable.btn_task_swipe_action_status_delete, null);

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
                    underlayButtons.add(secondUnderlayButton);
                    underlayButtons.add(firstUnderlayButton);
                }
            }
        };

        // Attach touch helper to recycler view
//        TaskItemTouchHelperCallback taskItemTouchHelperCallback = new
//                TaskItemTouchHelperCallback(this);
//        new ItemTouchHelper(taskItemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        taskSwipeHelper.attachSwipe();

        // Set up floating action button
        mFabAddTask = rootView.findViewById(R.id.fab_task_add);
        mFabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment taskDetailFragment = new TaskDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FragmentConstants.KEY_TASK, FragmentConstants.VALUE_TASK_ADD);
                bundle.putInt(FragmentConstants.KEY_TASK_TOTAL_NUMBER, mTaskListItems.size());
                taskDetailFragment.setArguments(bundle);

                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                ft.replace(R.id.layout_task_fragment, taskDetailFragment, taskDetailFragment.getClass().getSimpleName());
                ft.addToBackStack(taskDetailFragment.getClass().getSimpleName());
                ft.commit();
            }
        });
//        initOtherLayouts(inflater, container);
    }

    private void deleteTask(int position) {
        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
        mTaskViewModel.deleteTask(taskModelAtPos.getId());
    }

    private void setTaskStatusComplete(int position) {
        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
        taskModelAtPos.setStatus(EStatus.COMPLETE.toString());
        mTaskViewModel.updateTask(taskModelAtPos);
    }

    private void setTaskStatusInProgress(int position) {
        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
        taskModelAtPos.setStatus(EStatus.IN_PROGRESS.toString());
        mTaskViewModel.updateTask(taskModelAtPos);
    }

    private void setTaskStatusNew(int position) {
        TaskModel taskModelAtPos = mRecyclerAdapter.getTaskModelAtPosition(position);
        taskModelAtPos.setStatus(EStatus.NEW.toString());
        mTaskViewModel.updateTask(taskModelAtPos);
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
            System.out.println("mTaskListItems.size() in recycler is " + mTaskListItems.size());
            mRecyclerAdapter.notifyItemInserted(mTaskListItems.size() - 1);
            mRecyclerAdapter.notifyItemRangeChanged(mTaskListItems.size() - 1, mTaskListItems.size());
//            mRecyclerAdapter.notifyDataSetChanged();
        }
    }

    public TaskRecyclerAdapter getAdapter() {
        return mRecyclerAdapter;
    }

    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mTaskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
        mUserTaskJoinViewModel = ViewModelProviders.of(this).get(UserTaskJoinViewModel.class);

        /*
         * Refreshes recyclerview UI whenever there is a change in task (insert, update or delete)
         */
        mTaskViewModel.getAllTasks().observe(this, new Observer<List<TaskModel>>() {
            @Override
            public void onChanged(@Nullable List<TaskModel> taskModelList) {
                mRecyclerAdapter.setTaskListItems(taskModelList);
            }
        });
    }

    private synchronized void setUpRecyclerData() {
        if (mTaskListItems == null) {
            mTaskListItems = new ArrayList<>();
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

                SingleObserver<List<TaskModel>> singleObserverOfTasksForUser = new SingleObserver<List<TaskModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onSuccess(List<TaskModel> taskModelList) {
                        Log.d(TAG, "onSuccess singleObserverOfTasksForUser, setUpRecyclerData: " +
                                "taskModelList.size() is " + taskModelList.size());
                        if (taskModelList.size() == 0) {
                            setUpDummyTasks();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverOfTasksForUser, setUpRecyclerData. " +
                                "Error Msg: " + e.toString());
                    }
                };

                mUserTaskJoinViewModel.queryTasksForUser(userModel.getUserId(), singleObserverOfTasksForUser);
            }

            @Override
            public void onError(Throwable e) {
                // show an error message
                Log.d(TAG, "onError singleObserverForUser, setUpRecyclerData. " +
                        "Error Msg: " + e.toString());
            }
        };

        mUserViewModel.queryUserByAccessToken(accessToken, singleObserverForUser);


//        if (getTotalNumberOfTasks() == 0) {
//            setUpDummyTasks();
////            addItemsToLocalDatabase();
//
//        }

//        else {
//            mTaskListItems.clear();
//
//            mTaskListItems = mDatabase.userTaskDao().queryTasksForUser(currentUser.getUserId());

//            for (int i = 0; i < getTotalNumberOfTasks(); i++) {
//                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                String taskInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(String.valueOf(i));
//
//                TaskModel taskItem = new TaskModel();
//
//                taskItem.setId(pref.getInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID), SharedPreferenceConstants.DEFAULT_INT));
//                taskItem.setAssignedBy(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER), SharedPreferenceConstants.DEFAULT_STRING));
//                taskItem.setAssignedTo(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE), SharedPreferenceConstants.DEFAULT_STRING));
////                    taskItem.setAssigneeAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
////                taskItem.setAssigneeAvatar(Objects.requireNonNull(getContext()).getDrawable(pref.getInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
////                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), SharedPreferenceConstants.DEFAULT_INT)));
//                taskItem.setTitle(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE), SharedPreferenceConstants.DEFAULT_STRING));
//                taskItem.setDescription(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION), SharedPreferenceConstants.DEFAULT_STRING));
////                taskItem.setStatus(EStatus.getEStatus(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
////                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS), SharedPreferenceConstants.DEFAULT_STRING)));
//                taskItem.setStatus(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS), SharedPreferenceConstants.DEFAULT_STRING));
////                taskItem.setCreatedDateTime(DateTimeUtil.stringToDate(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
////                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE), SharedPreferenceConstants.DEFAULT_STRING)));
//                taskItem.setCreatedDateTime(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE), SharedPreferenceConstants.DEFAULT_STRING));
//
//                mTaskListItems.add(taskItem);
//            }
//        }
    }

    private void setUpDummyTasks() {
        Log.d(TAG, "Setting up dummy tasks");
        TaskModel taskModel1 = new TaskModel();
//            taskModel1.setId(0);
        taskModel1.setAssignedBy("Aaron (A01)");
        taskModel1.setAssignedTo("");
//            taskModel1.setAssigneeAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
        taskModel1.setTitle("Deactivate IAD");
        taskModel1.setDescription("IAD is located in the Engine Room. Beware of enemy intruders in adjacent rooms.");
//            taskModel1.setStatus(EStatus.NEW);
        taskModel1.setStatus(EStatus.NEW.toString());
        Date date1 = DateTimeUtil.getSpecifiedDateByYear(2015);
        taskModel1.setCreatedDateTime(DateTimeUtil.dateToString(date1));

        TaskModel taskModel2 = new TaskModel();
//            taskModel2.setId(1);
        taskModel2.setAssignedBy("Bastian (B01)");
        taskModel2.setAssignedTo("");
        taskModel2.setTitle("Rescue Hostage");
        taskModel2.setDescription("Hostage is located in the Deck 1. Beware of enemy intruders in adjacent rooms.");
//            taskModel2.setStatus(EStatus.IN_PROGRESS);
        taskModel2.setStatus(EStatus.IN_PROGRESS.toString());
        Date date2 = DateTimeUtil.getSpecifiedDateByYear(2016);
        taskModel2.setCreatedDateTime(DateTimeUtil.dateToString(date2));

        TaskModel taskModel3 = new TaskModel();
//            taskModel3.setId(2);
        taskModel3.setAssignedBy("Chris (C01)");
        taskModel3.setAssignedTo("");
        taskModel3.setTitle("Secure Nuclear Weapon");
        taskModel3.setDescription("Nuclear Weapon is located in the Bridge. Beware of enemy intruders.");
//            taskModel3.setStatus(EStatus.COMPLETE);
        taskModel3.setStatus(EStatus.COMPLETE.toString());
        Date date3 = DateTimeUtil.getSpecifiedDate(2017, Calendar.DECEMBER, 22,
                Calendar.AM, 10, 30, 30);
        taskModel3.setCreatedDateTime(DateTimeUtil.dateToString(date3));

        mTaskListItems.add(taskModel1);
        mTaskListItems.add(taskModel2);
        mTaskListItems.add(taskModel3);

        addItemsToSqliteDatabase();
    }

    private void addItemsToSqliteDatabase() {
        for (int i = 0; i < mTaskListItems.size(); i++) {
//            final int j = i;

            SingleObserver<Long> singleObserverAddTask = new SingleObserver<Long>() {
                @Override
                public void onSubscribe(Disposable d) {
                    // add it to a CompositeDisposable
                }

                @Override
                public void onSuccess(Long taskId) {
                    Log.d(TAG, "onSuccess singleObserverAddTask, " +
                            "addItemsToSqliteDatabase. " +
                            "TaskId: " + taskId);
                    SharedPreferences pref = PreferenceManager.
                            getDefaultSharedPreferences(getActivity());
                    String accessToken = pref.getString(
                            SharedPreferenceConstants.ACCESS_TOKEN, "");

//                    mTaskListItems.get(j).setId(taskId);
                    addTasksToCompositeTableInDatabase(accessToken, taskId);
                }

                @Override
                public void onError(Throwable e) {
                    Log.d(TAG, "onError singleObserverAddTask, addItemsToSqliteDatabase. " +
                            "Error Msg: " + e.toString());
                }
            };

            mTaskViewModel.addTask(mTaskListItems.get(i), singleObserverAddTask);
        }

        // Add Assignees for each task
//        for(int i = 0; i < mTaskListItems.size(); i++) {
//            addAssigneesToTaskList(mTaskListItems.get(i));
//        }
//
//        mRecyclerAdapter.setTaskListItems(mTaskListItems);
    }

    private void addTasksToCompositeTableInDatabase(String accessToken, long taskId) {
        Log.d(TAG, "onSuccess singleObserverForGettingTaskId, addItemsToSqliteDatabase. " +
                "taskId: " + taskId);

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
                        "addTasksToCompositeTableInDatabase. " +
                        "UserId: " + userModel.getUserId());
                UserTaskJoin userTaskJoin = new UserTaskJoin(userModel.getUserId(), taskId);
                mUserTaskJoinViewModel.addUserTaskJoin(userTaskJoin);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverUser, " +
                        "addTasksToCompositeTableInDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        mUserViewModel.queryUserByAccessToken(accessToken, singleObserverUser);
        mUserViewModel.queryUserByUserId("456", singleObserverUser);
    }

//    private void addAssigneesToTaskList() {
//        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
//        // asynchronously in the background thread and apply changes on the main UI thread
//        SingleObserver<List<TaskModel>> singleObserverTasksForUser =
//                new SingleObserver<List<TaskModel>>() {
//                    @Override
//                    public void onSubscribe(Disposable d) {}
//
//                    @Override
//                    public void onSuccess(List<TaskModel> taskModelList) {
//                        Log.d(TAG, "onSuccess singleObserverForUsersOfTask, addItemsToSqliteDatabase. " +
//                                "taskModelList size: " + taskModelList.size());
//                        for (int i = 0; i < taskModelList.size(); i++) {
//                            addAssigneesToTaskList(taskModelList.get(i));
//                        }
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.d(TAG, "onError singleObserverTasksForUser, addAssigneesToTaskList. " +
//                                "Error Msg: " + e.toString());
//                    }
//                };
//
//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String userId = sharedPrefs.getString(SharedPreferenceConstants.USER_ID,
//                SharedPreferenceConstants.DEFAULT_STRING);
//
//        System.out.println("SharedPreferences userId is " + userId);
//        mUserTaskJoinViewModel.queryTasksForUser(userId, singleObserverTasksForUser);
//    }

//    private void addAssigneesToTaskList(TaskModel taskModel) {
//        // Creates an observer (serving as a callback) to retrieve data from SqLite Room database
//        // asynchronously in the background thread and apply changes on the main UI thread
//        SingleObserver<List<UserModel>> singleObserverUsersForTask = new SingleObserver<List<UserModel>>() {
//            @Override
//            public void onSubscribe(Disposable d) {
//            }
//
//            @Override
//            public void onSuccess(List<UserModel> userModelList) {
//                Log.d(TAG, "onSuccess singleObserverUsersForTask, addAssigneesToTaskList. " +
//                        "userModelList size: " + userModelList.size());
//
//                StringBuilder assignedToList = new StringBuilder();
//                for (int j = 0; j < userModelList.size(); j++) {
//                    assignedToList.append(userModelList.get(j).getUserId());
//
//                    if (j == userModelList.size() - 1)
//                        break;
//
//                    assignedToList.append(", ");
//                }
//
////                System.out.println("assignedToList.toString() is "+ assignedToList.toString());
////                TaskModel tempTaskModel = mTaskListItems.get(taskItemIndex);
////                tempTaskModel.setId(taskId);
////                tempTaskModel.setAssignedTo(assignedToList.toString());
////                mTaskListItems.set(taskItemIndex, tempTaskModel);
//
//                taskModel.setAssignedTo(assignedToList.toString());
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                Log.d(TAG, "onError singleObserverUsersForTask, addAssigneesToTaskList. " +
//                        "Error Msg: " + e.toString());
//            }
//        };
//
//        mUserTaskJoinViewModel.queryUsersForTask(taskModel.getId(), singleObserverUsersForTask);
//    }

    /*
     * Obtain total number of tasks of user
     */
//    private int getTotalNumberOfTasks() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        int totalNumberOfTasks = pref.getInt(SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.TASK_TOTAL_NUMBER), 0);
//
//        return totalNumberOfTasks;
//    }

    /*
     * Add task items to local database
     */
//    private void addItemsToLocalDatabase() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        SharedPreferences.Editor editor = pref.edit();
//
//        // Increment total number of tasks by one, and store it
//        String totalNumberOfTasksKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.TASK_TOTAL_NUMBER);
//
//        editor.putInt(totalNumberOfTasksKey, mTaskListItems.size());
//
//        for (int i = 0; i < mTaskListItems.size(); i++) {
//            TaskModel taskItem = mTaskListItems.get(i);
//            addSingleItemToLocalDatabase(editor, taskItem.getId(), taskItem.getAssignedBy(),
////                    taskItem.getAssignedTo(),
//                    R.drawable.default_soldier_icon, taskItem.getTitle(), taskItem.getDescription(),
//                    taskItem.getStatus(), taskItem.getCreatedDateTime());
//        }
//    }

    /*
     * Add single task with respective fields
     */
//    private void addSingleItemToLocalDatabase(SharedPreferences.Editor editor, long id, String assigner,
////            , String assignee,
//                                              int assigneeAvatarId, String title, String description, String status, String dateString) {
//
//        String taskInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
//                concat(String.valueOf(id));
//
//        editor.putLong(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID), id);
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER), assigner);
////        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
////                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE), assignee);
//        editor.putInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), assigneeAvatarId);
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE), title);
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION), description);
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS), status);
//        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE), dateString);
//
//        editor.apply();
//    }

//    private void removeSingleItemFromLocalDatabase(int position) {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        SharedPreferences.Editor editor = pref.edit();
//
//        // Overwrite all fields of specified task with next stored task fields
//        // E.g. Task id(2) is replaced with id(3)
//        System.out.println("position is " + position);
//        System.out.println("removeSingleItemFromLocalDatabase is " + getTotalNumberOfTasks());
//        boolean isReplaced = false;
//
//        for (int i = position; i < getTotalNumberOfTasks() - 1; i++) {
//            isReplaced = true;
//            String taskInitialsOfCurrentPos = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(String.valueOf(i));
//
//            String taskInitialsOfNextPos = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(String.valueOf(i + 1));
//
//            editor.putInt(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID),
//                    pref.getInt(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID), SharedPreferenceConstants.DEFAULT_INT));
//            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER),
//                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER), SharedPreferenceConstants.DEFAULT_STRING));
//            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE),
//                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE), SharedPreferenceConstants.DEFAULT_STRING));
//            editor.putInt(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID),
//                    pref.getInt(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), SharedPreferenceConstants.DEFAULT_INT));
//            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE),
//                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE), SharedPreferenceConstants.DEFAULT_STRING));
//            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION),
//                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION), SharedPreferenceConstants.DEFAULT_STRING));
//            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS),
//                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS), SharedPreferenceConstants.DEFAULT_STRING));
//            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE),
//                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE), SharedPreferenceConstants.DEFAULT_STRING));
//        }
//
//        // Safely remove all fields of task at the back of the queue
//        int newPosToDelete;
//
//        if (isReplaced) {
//            newPosToDelete = getTotalNumberOfTasks() - 1;
//        } else {
//            newPosToDelete = position;
//        }
//
//        String taskInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
//                concat(String.valueOf(newPosToDelete));
//
//        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID));
//        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER));
//        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE));
//        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID));
//        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE));
//        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION));
//        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS));
//        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE));
//
//        // Decrement total number of tasks by one, and store it
//        String totalNumberOfTasksKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.TASK_TOTAL_NUMBER);
//        editor.putInt(totalNumberOfTasksKey, getTotalNumberOfTasks() - 1);
//
//        editor.apply();
//    }

//    private void showFirstAndSecondSwipeActionButtons(TaskViewHolder viewHolder, int firstActionSwipeBackgroundColor,
//                                                      Drawable firstActionSwipeDrawable, Drawable secondActionSwipeDrawable) {
//        viewHolder.getRelativeLayoutFirstSwipeActionButton().setBackgroundColor(firstActionSwipeBackgroundColor);
//        viewHolder.getImgFirstSwipeAction().setImageDrawable(firstActionSwipeDrawable);
//        viewHolder.getImgSecondSwipeAction().setImageDrawable(secondActionSwipeDrawable);
//    }

//    @Override
//    public void onChildDraw(TaskViewHolder viewHolder) {
//        String taskStatus = viewHolder.getTvStatus().getText().toString().trim();
//
//        if (EStatus.NEW.toString().equalsIgnoreCase(taskStatus)) {
//            showFirstAndSecondSwipeActionButtons(viewHolder,
//                    ResourcesCompat.getColor(getResources(), R.color.task_status_yellow, null),
//                    getContext().getDrawable(R.drawable.btn_task_swipe_action_status_in_progress),
//                    getContext().getDrawable(R.drawable.btn_task_swipe_action_status_complete));
//        } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(taskStatus)) {
//            showFirstAndSecondSwipeActionButtons(viewHolder,
//                    ResourcesCompat.getColor(getResources(), R.color.task_status_cyan, null),
//                    getContext().getDrawable(R.drawable.btn_task_swipe_action_status_new),
//                    getContext().getDrawable(R.drawable.btn_task_swipe_action_status_complete));
//        } else {
//            showFirstAndSecondSwipeActionButtons(viewHolder,
//                    ResourcesCompat.getColor(getResources(), R.color.task_status_cyan, null),
//                    getContext().getDrawable(R.drawable.btn_task_swipe_action_status_new),
//                    getContext().getDrawable(R.drawable.btn_task_swipe_action_status_in_progress));
//        }
//
////        viewHolder.getImgFirstSwipeAction().requestFocus();
////        viewHolder.getImgSecondSwipeAction().requestFocus();
////        viewHolder.getImgDelete().requestFocus();
//    }

    @Subscribe
    public void onEvent(TaskEvent taskEvent)
    {
        if (taskEvent.isNavigateToViewTask()) {
            Fragment taskDetailFragment = new TaskDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString(FragmentConstants.KEY_TASK, FragmentConstants.VALUE_TASK_VIEW);
            bundle.putString(FragmentConstants.KEY_TASK_TITLE, mTaskListItems.get(taskEvent.getItemPos()).getTitle());
            bundle.putString(FragmentConstants.KEY_TASK_DESCRIPTION, mTaskListItems.get(taskEvent.getItemPos()).getDescription());
            taskDetailFragment.setArguments(bundle);

            // Pass info to fragment
            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
            ft.replace(R.id.layout_task_fragment, taskDetailFragment, taskDetailFragment.getClass().getSimpleName());
            ft.addToBackStack(taskDetailFragment.getClass().getSimpleName());
            ft.commit();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        Log.d(TAG, "setUserVisibleHint");
        if (isResumed()) { // fragment has been created at this point
            if (mIsVisibleToUser) {
                Log.d(TAG, "setUserVisibleHint onVisible");
                onVisible();
            }
//            else {
//                onInvisible();
//            }
        }
    }

    public void refreshData() {
        Log.d(TAG, "refreshData");
        setUpRecyclerData();

//        if (mRecyclerAdapter != null) {
//            mRecyclerAdapter.notifyDataSetChanged();
//        }
    }

    private void onVisible() {
        refreshData();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        if (mIsVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
//        if (mIsVisibleToUser) {
//            onInvisible();
//        }
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
