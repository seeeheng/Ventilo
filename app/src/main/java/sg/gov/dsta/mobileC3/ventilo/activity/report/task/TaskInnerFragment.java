package sg.gov.dsta.mobileC3.ventilo.activity.report.task;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskItemModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class TaskInnerFragment extends Fragment {

    private static final String TAG = "TaskInnerFragment";

    private RecyclerView mRecyclerView;
    private TaskRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    //    private TextView mToolbarTitleTextView;
//    private TextView mToolbarLeftActivityLinkTextView;
    private FloatingActionButton mFabAddTask;
//    private ImageButton mImgBtnMenu;

    private List<TaskItemModel> mTaskListItems;

    private boolean mIsVisibleToUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_inner_task, container, false);
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
        mRecyclerView.addOnItemTouchListener(new TaskRecyclerItemTouchListener(getContext(), mRecyclerView, new TaskRecyclerItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
//                Toast.makeText(getContext(),"Task onItemClick" + "(" + position + ")", Toast.LENGTH_SHORT).show();

                RelativeLayout relativeLayoutDeleteIcon = view.findViewById(R.id.layout_recycler_task_delete);
                RelativeLayout relativeLayoutStartIcon = view.findViewById(R.id.layout_recycler_task_start);
                RelativeLayout relativeLayoutDoneIcon = view.findViewById(R.id.layout_recycler_task_done);

                if (relativeLayoutDeleteIcon.getVisibility() == View.VISIBLE) {
                    removeItemInRecycler(position);
                } else if (relativeLayoutStartIcon.getVisibility() == View.VISIBLE) {
                    startItemInRecycler(position);
                } else if (relativeLayoutDoneIcon.getVisibility() == View.VISIBLE) {
                    completeItemInRecycler(position);
                } else {
                    Fragment taskInnerDetailFragment = new TaskInnerDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(FragmentConstants.KEY_TASK, FragmentConstants.VALUE_TASK_VIEW);
                    bundle.putString(FragmentConstants.KEY_TASK_TITLE, mTaskListItems.get(position).getTitle());
                    bundle.putString(FragmentConstants.KEY_TASK_DESCRIPTION, mTaskListItems.get(position).getDescription());
                    taskInnerDetailFragment.setArguments(bundle);

                    // Pass info to fragment
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                    ft.replace(R.id.layout_task_inner_fragment, taskInnerDetailFragment, taskInnerDetailFragment.getClass().getSimpleName());
                    ft.addToBackStack(taskInnerDetailFragment.getClass().getSimpleName());
                    ft.commit();
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {
                Toast.makeText(getContext(), "Task onItemLongClick" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeLeft(View view, int position) {
                RelativeLayout relativeLayoutDeleteIcon = view.findViewById(R.id.layout_recycler_task_delete);
                RelativeLayout relativeLayoutStartIcon = view.findViewById(R.id.layout_recycler_task_start);
                RelativeLayout relativeLayoutDoneIcon = view.findViewById(R.id.layout_recycler_task_done);

                if (relativeLayoutDeleteIcon.getVisibility() == View.VISIBLE) {
                    relativeLayoutDeleteIcon.setVisibility(View.GONE);
                } else {
                    if (mTaskListItems.get(position).getStatus() == EStatus.NEW) {
                        relativeLayoutStartIcon.setVisibility(View.VISIBLE);
                    } else if (mTaskListItems.get(position).getStatus() == EStatus.IN_PROGRESS) {
                        relativeLayoutDoneIcon.setVisibility(View.VISIBLE);
                    }
                }

                relativeLayoutDeleteIcon.bringToFront();
                relativeLayoutStartIcon.bringToFront();
                relativeLayoutDoneIcon.bringToFront();
//                setImageListeners(view);
//                Toast.makeText(getContext(), "Task onSwipeLeft" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeRight(View view, int position) {
                RelativeLayout relativeLayoutDeleteIcon = view.findViewById(R.id.layout_recycler_task_delete);
                RelativeLayout relativeLayoutStartIcon = view.findViewById(R.id.layout_recycler_task_start);
                RelativeLayout relativeLayoutDoneIcon = view.findViewById(R.id.layout_recycler_task_done);

                if (relativeLayoutStartIcon.getVisibility() == View.VISIBLE ||
                        relativeLayoutDoneIcon.getVisibility() == View.VISIBLE) {
                    relativeLayoutStartIcon.setVisibility(View.GONE);
                    relativeLayoutDoneIcon.setVisibility(View.GONE);
                } else {
                    relativeLayoutDeleteIcon.setVisibility(View.VISIBLE);
                }

                relativeLayoutDeleteIcon.bringToFront();
                relativeLayoutStartIcon.bringToFront();
                relativeLayoutDoneIcon.bringToFront();
//                setImageListeners(view);
//                Toast.makeText(getContext(), "Task onSwipeRight" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }
        }));

        // Set data for recycler view
        setUpRecyclerData();

        mRecyclerAdapter = new TaskRecyclerAdapter(getContext(), mTaskListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up floating action button
        mFabAddTask = rootView.findViewById(R.id.fab_task_add);
        mFabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment taskDetailFragment = new TaskInnerDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FragmentConstants.KEY_TASK, FragmentConstants.VALUE_TASK_ADD);
                bundle.putInt(FragmentConstants.KEY_TASK_TOTAL_NUMBER, mTaskListItems.size());
                taskDetailFragment.setArguments(bundle);

                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                ft.replace(R.id.layout_task_inner_fragment, taskDetailFragment, taskDetailFragment.getClass().getSimpleName());
                ft.addToBackStack(taskDetailFragment.getClass().getSimpleName());
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

    private View.OnClickListener onDeleteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TaskViewHolder holder = (TaskViewHolder) view.getTag();
            int position = holder.getAdapterPosition();
            removeItemInRecycler(position);
        }
    };

    private View.OnClickListener onStartClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TaskViewHolder holder = (TaskViewHolder) view.getTag();
            int position = holder.getAdapterPosition();
            startItemInRecycler(position);
        }
    };

    private View.OnClickListener onDoneClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            TaskViewHolder holder = (TaskViewHolder) view.getTag();
            int position = holder.getAdapterPosition();
            completeItemInRecycler(position);
        }
    };

    private void startItemInRecycler(int position) {
        mTaskListItems.get(position).setStatus(EStatus.IN_PROGRESS);
        mRecyclerAdapter.notifyDataSetChanged();
    }

    private void completeItemInRecycler(int position) {
        mTaskListItems.get(position).setStatus(EStatus.DONE);
        mRecyclerAdapter.notifyDataSetChanged();
    }

    private void removeItemInRecycler(int position) {
        mTaskListItems.remove(position);
        mRecyclerView.removeViewAt(position);
        mRecyclerAdapter.notifyItemRemoved(position);
        mRecyclerAdapter.notifyItemRangeChanged(position, mTaskListItems.size());

        removeSingleItemFromLocalDatabase(position);
    }

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

    private synchronized void setUpRecyclerData() {

        if (mTaskListItems == null) {
            mTaskListItems = new ArrayList<>();
        }

        System.out.println("getTotalNumberOfTasks() is " + getTotalNumberOfTasks());

        if (getTotalNumberOfTasks() == 0) {
            TaskItemModel taskItemModel1 = new TaskItemModel();
            taskItemModel1.setId(0);
            taskItemModel1.setAssigner("Aaron (A01)");
            taskItemModel1.setAssignee("Andy (A22)");
            taskItemModel1.setAssigneeAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
            taskItemModel1.setTitle("Deactivate IAD");
            taskItemModel1.setDescription("IAD is located in the Engine Room. Beware of enemy intruders in adjacent rooms.");
            taskItemModel1.setStatus(EStatus.NEW);
            Date date1 = DateTimeUtil.getSpecifiedDateByYear(2015);
            taskItemModel1.setCreatedDateTime(date1);

            TaskItemModel taskItemModel2 = new TaskItemModel();
            taskItemModel2.setId(1);
            taskItemModel2.setAssigner("Bastian (B01)");
            taskItemModel2.setAssignee("Brandon (B22)");
            taskItemModel2.setAssigneeAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
            taskItemModel2.setTitle("Rescue Hostage");
            taskItemModel2.setDescription("Hostage is located in the Deck 1. Beware of enemy intruders in adjacent rooms.");
            taskItemModel2.setStatus(EStatus.IN_PROGRESS);
            Date date2 = DateTimeUtil.getSpecifiedDateByYear(2016);
            taskItemModel2.setCreatedDateTime(date2);

            TaskItemModel taskItemModel3 = new TaskItemModel();
            taskItemModel3.setId(2);
            taskItemModel3.setAssigner("Chris (C01)");
            taskItemModel3.setAssignee("Cassie (C22)");
            taskItemModel3.setAssigneeAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
            taskItemModel3.setTitle("Secure Nuclear Weapon");
            taskItemModel3.setDescription("Nuclear Weapon is located in the Bridge. Beware of enemy intruders.");
            taskItemModel3.setStatus(EStatus.DONE);
            Date date3 = DateTimeUtil.getSpecifiedDate(2017, Calendar.DECEMBER, 22,
                    Calendar.AM, 10, 30, 30);
            taskItemModel3.setCreatedDateTime(date3);

            mTaskListItems.add(taskItemModel1);
            mTaskListItems.add(taskItemModel2);
            mTaskListItems.add(taskItemModel3);

            addItemsToLocalDatabase();

        } else {
            mTaskListItems.clear();

            for (int i = 0; i < getTotalNumberOfTasks(); i++) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String taskInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
                        concat(String.valueOf(i));

                TaskItemModel taskItem = new TaskItemModel();
                taskItem.setId(pref.getInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID), SharedPreferenceConstants.DEFAULT_INT));
                taskItem.setAssigner(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER), SharedPreferenceConstants.DEFAULT_STRING));
                taskItem.setAssignee(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE), SharedPreferenceConstants.DEFAULT_STRING));
                    taskItem.setAssigneeAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
//                taskItem.setAssigneeAvatar(Objects.requireNonNull(getContext()).getDrawable(pref.getInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), SharedPreferenceConstants.DEFAULT_INT)));
                taskItem.setTitle(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE), SharedPreferenceConstants.DEFAULT_STRING));
                taskItem.setDescription(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION), SharedPreferenceConstants.DEFAULT_STRING));
                taskItem.setStatus(EStatus.getEStatus(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS), SharedPreferenceConstants.DEFAULT_STRING)));
                taskItem.setCreatedDateTime(DateTimeUtil.stringToDate(pref.getString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE), SharedPreferenceConstants.DEFAULT_STRING)));

                mTaskListItems.add(taskItem);
            }
        }
    }

    /*
     * Obtain total number of tasks of user
     */
    private int getTotalNumberOfTasks() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int totalNumberOfTasks = pref.getInt(SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.TASK_TOTAL_NUMBER), 0);

        return totalNumberOfTasks;
    }

    /*
     * Add task items to local database
     */
    private void addItemsToLocalDatabase() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();

        // Increment total number of tasks by one, and store it
        String totalNumberOfTasksKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.TASK_TOTAL_NUMBER);

        editor.putInt(totalNumberOfTasksKey, mTaskListItems.size());

        for (int i = 0; i < mTaskListItems.size(); i++) {
            TaskItemModel taskItem = mTaskListItems.get(i);
            addSingleItemToLocalDatabase(editor, taskItem.getId(), taskItem.getAssigner(), taskItem.getAssignee(),
                    R.drawable.default_soldier_icon, taskItem.getTitle(), taskItem.getDescription(),
                    taskItem.getStatus().toString(), DateTimeUtil.dateToString(taskItem.getCreatedDateTime()));
        }
    }

    /*
     * Add single task with respective fields
     */
    private void addSingleItemToLocalDatabase(SharedPreferences.Editor editor, int id, String assigner, String assignee,
                                              int assigneeAvatarId, String title, String description, String status, String dateString) {

        String taskInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
                concat(String.valueOf(id));

        editor.putInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID), id);
        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER), assigner);
        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE), assignee);
        editor.putInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), assigneeAvatarId);
        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE), title);
        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION), description);
        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS), status);
        editor.putString(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE), dateString);

        editor.apply();
    }

    private void removeSingleItemFromLocalDatabase(int position) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();

        // Overwrite all fields of specified task with next stored task fields
        // E.g. Task id(2) is replaced with id(3)
        System.out.println("position is " + position);
        System.out.println("removeSingleItemFromLocalDatabase is " + getTotalNumberOfTasks());
        for (int i = position; i < getTotalNumberOfTasks() - 1; i++) {
            String taskInitialsOfCurrentPos = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
                    concat(String.valueOf(i));

            String taskInitialsOfNextPos = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
                    concat(String.valueOf(i + 1));

            editor.putInt(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID),
                    pref.getInt(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID), SharedPreferenceConstants.DEFAULT_INT));
            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER),
                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER), SharedPreferenceConstants.DEFAULT_STRING));
            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE),
                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE), SharedPreferenceConstants.DEFAULT_STRING));
            editor.putInt(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID),
                    pref.getInt(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), SharedPreferenceConstants.DEFAULT_INT));
            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE),
                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE), SharedPreferenceConstants.DEFAULT_STRING));
            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION),
                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION), SharedPreferenceConstants.DEFAULT_STRING));
            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS),
                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS), SharedPreferenceConstants.DEFAULT_STRING));
            editor.putString(taskInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE),
                    pref.getString(taskInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE), SharedPreferenceConstants.DEFAULT_STRING));
        }

        // Safely remove all fields of task at the back of the queue
        String taskInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.HEADER_TASK).concat(SharedPreferenceConstants.SEPARATOR).
                concat(String.valueOf(getTotalNumberOfTasks()));

        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ID));
        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNER));
        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE));
        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID));
        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_TITLE));
        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_DESCRIPTION));
        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_STATUS));
        editor.remove(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_TASK_DATE));

        // Decrement total number of tasks by one, and store it
        String totalNumberOfTasksKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.TASK_TOTAL_NUMBER);
        editor.putInt(totalNumberOfTasksKey, getTotalNumberOfTasks() - 1);

        editor.apply();
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
        if (mIsVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
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
