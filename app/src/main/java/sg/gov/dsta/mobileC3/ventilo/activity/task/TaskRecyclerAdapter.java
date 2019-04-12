package sg.gov.dsta.mobileC3.ventilo.activity.task;

import android.arch.lifecycle.ViewModelProviders;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.TaskViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.DrawableUtil;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class TaskRecyclerAdapter extends RecyclerView.Adapter<TaskViewHolder> {

    private TaskViewModel mTaskViewModel;
    private List<TaskModel> mTaskListItems;
    private Fragment mFragment;

    public TaskRecyclerAdapter(Fragment fragment, List<TaskModel> taskListItems) {
        this.mFragment = fragment;
        mTaskListItems = taskListItems;
        mTaskViewModel = ViewModelProviders.of(fragment).get(TaskViewModel.class);
//        mUserTaskJoinViewModel = userTaskJoinViewModel;
//        observerSetup();
//        mDatabase = VentiloDatabase.getInstance(context);
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_task, viewGroup, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder itemViewHolder, final int i) {
//        mTaskViewHolder = itemViewHolder;
        TaskModel item = mTaskListItems.get(i);

//        mUserTaskJoinViewModel.queryUsersForTask(String.valueOf(item.getId()));
//        List<UserModel> userModelList = mUserTaskJoinViewModel.getUsersForTaskResults().getValue();
//
//        StringBuilder assignedToList = new StringBuilder();
//        for (int j = 0; j < userModelList.size(); j++) {
//            assignedToList.append(userModelList.get(j).getUserId());
//
//            if (j == userModelList.size() - 1)
//                break;
//
//            assignedToList.append(", ");
//        }
//
//        String assignee = "Assigned To: ".concat(item.getAssignee());
//        itemViewHolder.getTvAssignedTo().setText(assignee);

//        if () {
//        itemViewHolder.getCircleImgAvatar().
//                setImageDrawable(mContext.getDrawable(R.drawable.default_soldier_icon));
//        }

//        String title = "Task: ".concat(item.getTitle());
        itemViewHolder.getTvTitle().setText(item.getTitle());

        String status = item.getStatus();
//        String statusFullText = "Status: ".concat(status);
        itemViewHolder.getTvStatus().setText(status);

        if (status.equalsIgnoreCase(EStatus.NEW.toString())) {
            itemViewHolder.getTvStatus().setTextColor(
                    mFragment.getContext().getResources().getColor(R.color.primary_text_white));
            itemViewHolder.getCircleImgStatus().
                    setImageDrawable(mFragment.getContext().getDrawable(R.drawable.task_new));
        } else if (status.equalsIgnoreCase(EStatus.IN_PROGRESS.toString())) {
            itemViewHolder.getTvStatus().setTextColor(
                    mFragment.getContext().getResources().getColor(android.R.color.holo_orange_light));
            itemViewHolder.getCircleImgStatus().
                    setImageDrawable(mFragment.getContext().getDrawable(R.drawable.task_in_progress));
        } else {
            itemViewHolder.getTvStatus().setTextColor(
                    mFragment.getContext().getResources().getColor(android.R.color.holo_green_light));
            itemViewHolder.getCircleImgStatus().
                    setImageDrawable(mFragment.getContext().getDrawable(R.drawable.task_done));
        }

        // Other RelativeLayouts
//        itemViewHolder.getRelativeLayoutDeleteIcon().setVisibility(View.GONE);
//        itemViewHolder.getRelativeLayoutStartIcon().setVisibility(View.GONE);
//        itemViewHolder.getRelativeLayoutDoneIcon().setVisibility(View.GONE);

        String dateTimeString = DateTimeUtil.getTimeDifference(mFragment.getContext(),
                DateTimeUtil.stringToDate(item.getCreatedDateTime()));
        itemViewHolder.getTvScheduledTime().setText(dateTimeString);

//        View.OnClickListener onFirstSwipeActionClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (view instanceof AppCompatImageView) {
//                    AppCompatImageView firstSwipeActionImageView = (AppCompatImageView) view;
//
//                    if (DrawableUtil.areDrawablesIdentical(firstSwipeActionImageView.getDrawable(),
//                            ResourcesCompat.getDrawable(view.getResources(),
//                                    R.drawable.btn_task_swipe_action_status_new, null))) {
//                        System.out.println("onFirstSwipeActionClickListener setTaskStatusNew");
//                        setTaskStatusNew(i);
//                    } else if (DrawableUtil.areDrawablesIdentical(firstSwipeActionImageView.getDrawable(),
//                            ResourcesCompat.getDrawable(view.getResources(),
//                                    R.drawable.btn_task_swipe_action_status_in_progress, null))) {
//                        System.out.println("onFirstSwipeActionClickListener setTaskStatusInProgress");
//                        setTaskStatusInProgress(i);
//                    } else {
//                        setTaskStatusComplete(i);
//                    }
//                }
//            }
//        };
//
//        View.OnClickListener onSecondSwipeActionClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (view instanceof AppCompatImageView) {
//                    AppCompatImageView secondSwipeActionImageView = (AppCompatImageView) view;
//
//                    if (DrawableUtil.areDrawablesIdentical(secondSwipeActionImageView.getDrawable(),
//                            ResourcesCompat.getDrawable(view.getResources(),
//                                    R.drawable.btn_task_swipe_action_status_new, null))) {
//                        System.out.println("onSecondSwipeActionClickListener setTaskStatusNew");
//                        setTaskStatusNew(i);
//                    } else if (DrawableUtil.areDrawablesIdentical(secondSwipeActionImageView.getDrawable(),
//                            ResourcesCompat.getDrawable(view.getResources(),
//                                    R.drawable.btn_task_swipe_action_status_in_progress, null))) {
//                        System.out.println("onSecondSwipeActionClickListener setTaskStatusInProgress");
//                        setTaskStatusInProgress(i);
//                    } else {
//                        setTaskStatusComplete(i);
//                    }
//                }
//            }
//        };
//
//        View.OnClickListener onDeleteSwipeActionClickListener = new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                System.out.println("onDeleteSwipeActionClickListener");
//                deleteTask(i);
//            }
//        };
//
//        // Add listener to swipe action buttons
//        itemViewHolder.getImgFirstSwipeAction().setOnClickListener(onFirstSwipeActionClickListener);
//        itemViewHolder.getImgSecondSwipeAction().setOnClickListener(onSecondSwipeActionClickListener);
//        itemViewHolder.getImgDelete().setOnClickListener(onDeleteSwipeActionClickListener);
    }

//    private void observerSetup() {
////        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
//
//        mUserTaskJoinViewModel.getUsersForTaskResults().observe((LifecycleOwner) mContext, new Observer<List<UserModel>>() {
//            @Override
//            public void onChanged(@Nullable List<UserModel> userModelList) {
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
//                String assignee = "Assigned To: ".concat(assignedToList.toString());
//                mTaskViewHolder.getTvAssignedTo().setText(assignee);
//            }
//        });
//    }

//    public void addItem(String title, String description) {
//        TaskModel newTaskModel = new TaskModel();
//        newTaskModel.setId(mTaskListItems.size() + 1);
//        newTaskModel.setAssignedBy("Desmond (D01)");
//
////        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(mContext);
////        String accessToken = pref.getString(SharedPreferenceConstants.ACCESS_TOKEN, "");
////        UserModel currentUser = mDatabase.userDao().getUserByAccessToken(accessToken).getValue();
//
////        newTaskModel.setAssignee(currentUser.getUserId().toString());
//        newTaskModel.setTitle(title);
//        newTaskModel.setDescription(description);
//        newTaskModel.setStatus(EStatus.NEW.toString());
//        Date currentDateTime = Calendar.getInstance().getTime();
//        newTaskModel.setCreatedDateTime(currentDateTime.toString());
//
//        mTaskListItems.add(newTaskModel);
//        notifyDataSetChanged();
//    }



    public void setTaskListItems(List<TaskModel> taskListItems) {
        mTaskListItems = taskListItems;
        notifyDataSetChanged();
    }

    public TaskModel getTaskModelAtPosition(int pos) {
        return mTaskListItems.get(pos);
    }

    @Override
    public int getItemCount() {
        return mTaskListItems.size();
    }

}
