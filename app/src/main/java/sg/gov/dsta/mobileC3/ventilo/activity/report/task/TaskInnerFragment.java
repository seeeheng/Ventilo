package sg.gov.dsta.mobileC3.ventilo.activity.report.task;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskItemModel;
import sg.gov.dsta.mobileC3.ventilo.util.constant.ReportFragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class TaskInnerFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    //    private TextView mToolbarTitleTextView;
//    private TextView mToolbarLeftActivityLinkTextView;
    private FloatingActionButton mFabAddTask;
//    private ImageButton mImgBtnMenu;

    private List<TaskItemModel> mTaskListItems;

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
        mRecyclerView.setHasFixedSize(true);

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
                    Fragment taskDetailFragment = new TaskInnerDetailFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(ReportFragmentConstants.KEY_TASK, ReportFragmentConstants.VALUE_TASK_VIEW);
                    bundle.putString(ReportFragmentConstants.KEY_TASK_TITLE, mTaskListItems.get(position).getTitle());
                    bundle.putString(ReportFragmentConstants.KEY_TASK_DESCRIPTION, mTaskListItems.get(position).getDescription());
                    taskDetailFragment.setArguments(bundle);

                    // Pass info to fragment
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                    ft.replace(R.id.layout_task_inner_fragment, taskDetailFragment, taskDetailFragment.getClass().getSimpleName());
                    ft.addToBackStack(taskDetailFragment.getClass().getSimpleName());
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
        setUpDummyData();

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
                bundle.putString(ReportFragmentConstants.KEY_TASK, ReportFragmentConstants.VALUE_TASK_ADD);
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

    private void setImageListeners(View recyclerView) {
        AppCompatImageView imgDelete = recyclerView.findViewById(R.id.img_task_delete);
        AppCompatImageView imgStart = recyclerView.findViewById(R.id.img_task_start);
        AppCompatImageView imgDone = recyclerView.findViewById(R.id.img_task_done);

        imgDelete.setOnClickListener(onDeleteClickListener);
        imgStart.setOnClickListener(onStartClickListener);
        imgDone.setOnClickListener(onDoneClickListener);

        imgDelete.bringToFront();
        imgStart.bringToFront();
        imgDone.bringToFront();
    }

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

    private void setUpDummyData() {
        mTaskListItems = new ArrayList<>();

        TaskItemModel taskItemModel1 = new TaskItemModel();
        taskItemModel1.setId("1");
        taskItemModel1.setAssigner("Aaron (A01)");
        taskItemModel1.setAssignee("Andy (A22)");
        taskItemModel1.setAssigneeAvatar(getContext().getDrawable(R.drawable.bft_1));
        taskItemModel1.setTitle("Deactivate IAD");
        taskItemModel1.setDescription("IAD is located in the Engine Room. Beware of enemy intruders in adjacent rooms.");
        taskItemModel1.setStatus(EStatus.NEW);

        TaskItemModel taskItemModel2 = new TaskItemModel();
        taskItemModel2.setId("2");
        taskItemModel2.setAssigner("Bastian (B01)");
        taskItemModel2.setAssignee("Brandon (B22)");
        taskItemModel2.setAssigneeAvatar(getContext().getDrawable(R.drawable.bft_2));
        taskItemModel2.setTitle("Rescue Hostage");
        taskItemModel2.setDescription("Hostage is located in the Deck 1. Beware of enemy intruders in adjacent rooms.");
        taskItemModel2.setStatus(EStatus.IN_PROGRESS);

        TaskItemModel taskItemModel3 = new TaskItemModel();
        taskItemModel3.setId("3");
        taskItemModel3.setAssigner("Chris (C01)");
        taskItemModel3.setAssignee("Cassie (C22)");
        taskItemModel3.setAssigneeAvatar(getContext().getDrawable(R.drawable.bft_5));
        taskItemModel3.setTitle("Secure Nuclear Weapon");
        taskItemModel3.setDescription("Nuclear Weapon is located in the Bridge. Beware of enemy intruders.");
        taskItemModel3.setStatus(EStatus.DONE);

        mTaskListItems.add(taskItemModel1);
        mTaskListItems.add(taskItemModel2);
        mTaskListItems.add(taskItemModel3);
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
