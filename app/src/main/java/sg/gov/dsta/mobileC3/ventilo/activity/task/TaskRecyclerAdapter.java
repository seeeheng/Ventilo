package sg.gov.dsta.mobileC3.ventilo.activity.task;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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

    private List<TaskModel> mTaskListItems;
    private Context mContext;

    public TaskRecyclerAdapter(Context context, List<TaskModel> taskListItems) {
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
        TaskModel item = mTaskListItems.get(i);

        itemViewHolder.getTvTitle().setText(item.getTitle());

        String team = "Team ".concat(item.getAssignedTo());
        itemViewHolder.getTvAssignedTo().setText(team);

        itemViewHolder.getTvShortDescription().setText(item.getDescription());

        String status = item.getStatus();
        itemViewHolder.getTvStatus().setText(status);

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

        String dateTimeString = DateTimeUtil.getTimeDifference(mContext,
                DateTimeUtil.stringToDate(item.getCreatedDateTime()));
        itemViewHolder.getTvScheduledTime().setText(dateTimeString);
    }

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
