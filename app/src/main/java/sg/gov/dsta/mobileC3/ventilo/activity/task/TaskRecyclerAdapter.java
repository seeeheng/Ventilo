package sg.gov.dsta.mobileC3.ventilo.activity.task;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.task.EAdHocTaskPriority;
import sg.gov.dsta.mobileC3.ventilo.util.task.EPhaseNo;
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

        if (mTaskListItems.get(i).getAdHocTaskPriority() != null &&
                mTaskListItems.get(i).getAdHocTaskPriority().equalsIgnoreCase(
                        EAdHocTaskPriority.HIGH.toString())) {
            itemViewHolder.getLinearLayoutAdHocHighPriority().setVisibility(View.VISIBLE);
        } else {
            itemViewHolder.getLinearLayoutAdHocHighPriority().setVisibility(View.GONE);
        }

        String phaseNo;
        if (mTaskListItems.get(i).getPhaseNo().equalsIgnoreCase(EPhaseNo.AD_HOC.toString())) {
            itemViewHolder.getTvPhase().setBackground(ResourcesCompat.getDrawable(mContext.getResources(),
                    R.drawable.rounded_corner_white_background, null));
            itemViewHolder.getTvPhase().setTextColor(ContextCompat.getColor(mContext, R.color.primary_highlight_cyan));
            phaseNo = item.getPhaseNo();
        } else {
            itemViewHolder.getTvPhase().setBackground(ResourcesCompat.getDrawable(mContext.getResources(),
                    R.drawable.rounded_corner_grey_background, null));
            itemViewHolder.getTvPhase().setTextColor(ContextCompat.getColor(mContext, R.color.primary_white));
            phaseNo = mContext.getString(R.string.task_phase).concat(StringUtil.SPACE).
                     concat(item.getPhaseNo());
        }

        itemViewHolder.getTvPhase().setText(phaseNo);

        itemViewHolder.getTvTitle().setText(item.getTitle());

        // -------------------- Get Current User Task Status --------------------
        String[] allTeams = StringUtil.removeCommasAndExtraSpaces(item.getAssignedTo());

        String team = "Team ".concat(item.getAssignedTo());
        itemViewHolder.getTvAssignedTo().setText(team);

        String currentUserCallsign = SharedPreferenceUtil.getCurrentUserCallsignID();
        int currentUserTaskStatusIndex = 0;

        for (int j = 0; j < allTeams.length; j++) {
            if (allTeams[j].equalsIgnoreCase(currentUserCallsign)) {
                currentUserTaskStatusIndex = j;
            }
        }

        itemViewHolder.getTvShortDescription().setText(item.getDescription());

        String[] allStatuses = StringUtil.removeCommasAndExtraSpaces(item.getStatus());
        String currentUserTaskStatus = allStatuses[currentUserTaskStatusIndex];

        itemViewHolder.getTvStatus().setText(currentUserTaskStatus);

        if (currentUserTaskStatus.equalsIgnoreCase(EStatus.NEW.toString())) {
            itemViewHolder.getTvStatus().setTextColor(ContextCompat.getColor(mContext,
                            R.color.primary_white));
            itemViewHolder.getCircleImgStatus().
                    setImageDrawable(mContext.getDrawable(R.drawable.icon_new_unselected));
        } else if (currentUserTaskStatus.equalsIgnoreCase(EStatus.IN_PROGRESS.toString())) {
            itemViewHolder.getTvStatus().setTextColor(ContextCompat.getColor(mContext,
                    R.color.task_status_yellow));
            itemViewHolder.getCircleImgStatus().
                    setImageDrawable(mContext.getDrawable(R.drawable.task_in_progress));
        } else {
            itemViewHolder.getTvStatus().setTextColor(ContextCompat.getColor(mContext,
                    R.color.dull_green));
            itemViewHolder.getCircleImgStatus().
                    setImageDrawable(mContext.getDrawable(R.drawable.task_completed));
        }

        // Reported date/time
        StringBuilder createdDateTimeStringBuilder = new StringBuilder();
        String createdDateTime = item.getCreatedDateTime();
        String createdDateTimeInCustomStrFormat = DateTimeUtil.dateToCustomStringFormat(
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

//        String dateTimeString = DateTimeUtil.getTimeDifference(mContext,
//                DateTimeUtil.stringToDate(item.getCreatedDateTime()));
        itemViewHolder.getTvScheduledTime().setText(createdDateTimeStringBuilder.toString());
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
