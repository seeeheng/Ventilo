package sg.gov.dsta.mobileC3.ventilo.activity.report.task;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskItemModel;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class TaskRecyclerAdapter extends RecyclerView.Adapter<TaskViewHolder> {

    List<TaskItemModel> mTaskListItems;

    private Context mContext;

    public TaskRecyclerAdapter(Context context, List<TaskItemModel> taskListItems) {
        this.mContext = context;
        mTaskListItems = taskListItems;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_task, viewGroup, false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder itemViewHolder, final int i) {
        TaskItemModel item = mTaskListItems.get(i);

        String assignee = "Assignee: ".concat(item.getAssignee());
        itemViewHolder.getTvAssignee().setText(assignee);
        itemViewHolder.getCircleImgAvatar().setImageDrawable(item.getAssigneeAvatar());

        String title = "Task: ".concat(item.getTitle());
        itemViewHolder.getTvTitle().setText(title);

        String status = item.getStatus().toString();
        String statusFullText = "Status: ".concat(status);
        itemViewHolder.getTvStatus().setText(statusFullText);

        if (status.equalsIgnoreCase(EStatus.NEW.toString())) {
            itemViewHolder.getTvStatus().setTextColor(
                    mContext.getResources().getColor(R.color.primary_text_white));
            itemViewHolder.getCircleImgStatus().
                    setImageDrawable(mContext.getDrawable(R.drawable.task_new));
        } else if (status.equalsIgnoreCase(EStatus.IN_PROGRESS.toString())) {
            itemViewHolder.getTvStatus().setTextColor(
                    mContext.getResources().getColor(android.R.color.holo_orange_light));
            itemViewHolder.getCircleImgStatus().
                    setImageDrawable(mContext.getDrawable(R.drawable.task_in_progress));
        } else {
            itemViewHolder.getTvStatus().setTextColor(
                    mContext.getResources().getColor(android.R.color.holo_green_light));
            itemViewHolder.getCircleImgStatus().
                    setImageDrawable(mContext.getDrawable(R.drawable.task_done));
        }

        // Other RelativeLayouts
        itemViewHolder.getRelativeLayoutDeleteIcon().setVisibility(View.GONE);
        itemViewHolder.getRelativeLayoutStartIcon().setVisibility(View.GONE);
        itemViewHolder.getRelativeLayoutDoneIcon().setVisibility(View.GONE);

        SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        String currentTimeString = currentDateFormat.format(new Date());
        currentTimeString = "Created Time: ".concat(currentTimeString);
        itemViewHolder.getTvDateTime().setText(currentTimeString);

//        itemViewHolder.get().setText(item.getCreatedBy());
//        itemViewHolder.getMNumCommentTV().setText(String.valueOf(item.getComments().size()));
////        itemViewHolder.getMTagTV().setText(item.get);
//        Date date = new Date();
//        itemViewHolder.getMTimeAgoTV().setText(DataAccess.timeAgo(date.getTime() - item.getCreatedDate().getTime()));
        // left images


//        itemViewHolder.mParentLinearLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClick:" + mTaskListItems.get(i));
//                Intent intent = new Intent(context, TasksViewActivity.class);
//                context.startActivity(intent);
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mTaskListItems.size();
    }

}
