package sg.gov.dsta.mobileC3.ventilo.activity.timeline;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class TimelinePlannedRecyclerAdapter extends RecyclerView.Adapter<TimelineViewHolder> {

    private List<TaskModel> mTimelinePhaseListItems;
    private Context mContext;
//    private View mMainView;
//    private View mContentContainerView;
//    private View mInnerContainerView;

    public TimelinePlannedRecyclerAdapter(Context context, List<TaskModel> timelinePhaseListItems) {
        this.mContext = context;
        mTimelinePhaseListItems = timelinePhaseListItems;
    }

    @Override
    public TimelineViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

//        mInnerContainerView = inflater.inflate(R.layout.layout_timeline_row_inner_container_team_task_status,
//                viewGroup, false);
        View view = inflater.inflate(R.layout.recycler_view_row_timeline, viewGroup, false);
//        mContentContainerView = inflater.inflate(R.layout.layout_row_timeline_container,
//                viewGroup, false);

        return new TimelineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TimelineViewHolder itemViewHolder, final int i) {
        LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        TaskModel taskModel = mTimelinePhaseListItems.get(i);
//        List<TaskModel> taskModelListItem = mTimelinePhaseListItems.get(i);

        // -------------------- Set Phase Number, Task Title and Description --------------------
        itemViewHolder.getTvHeaderPhaseNumber().setBackground(ResourcesCompat.getDrawable(mContext.getResources(),
                R.drawable.rounded_corner_grey_background, null));
        itemViewHolder.getTvHeaderPhaseNumber().setTextColor(
                ContextCompat.getColor(mContext, R.color.primary_white));

        itemViewHolder.getLinearLayoutHeaderAdHocHighPriorityIcon().setVisibility(View.GONE);

        String phase = mContext.getString(R.string.task_phase).concat(StringUtil.SPACE).concat(taskModel.getPhaseNo());
        itemViewHolder.getTvHeaderPhaseNumber().setText(phase);

        itemViewHolder.getTvHeaderTitle().setText(taskModel.getTitle());
        itemViewHolder.getTvHeaderShortDescription().setText(taskModel.getDescription());

        String finalCompletionTime = StringUtil.EMPTY_STRING;
        boolean isAllCompleted = true;
        boolean isAllNew = true;

        String[] assignedToStrArray = StringUtil.removeCommasAndExtraSpaces(taskModel.getAssignedTo());
        String[] statusStrArray = StringUtil.removeCommasAndExtraSpaces(taskModel.getStatus());
        String[] completedDateTimeStrArray = StringUtil.removeCommasAndExtraSpaces(taskModel.getCompletedDateTime());

        // -------------------- Set Inner Container's Team Task Status Components --------------------
        // Remove all child views, if any; On updates
        itemViewHolder.getLinearLayoutInnerContainerTeamTaskStatus().removeAllViews();

        /**
         * Layout view which includes item row consisting of the following for each team:
         * 1) Status Icon & Description
         * 2) Team Name
         * 3) Time of Completion
         */
        for (int j = 0; j < assignedToStrArray.length; j++) {
            View innerContainerView = vi.inflate(R.layout.layout_timeline_row_inner_container_team_task_status,
                    null);

            AppCompatImageView imgInnerContainerTaskStatusIcon = innerContainerView.findViewById(
                    R.id.img_timeline_task_status_icon);
            C2OpenSansSemiBoldTextView tvInnerContainerTaskStatus = innerContainerView.findViewById(
                    R.id.tv_timeline_task_status);
            C2OpenSansRegularTextView tvInnerContainerTaskTeam = innerContainerView.findViewById(
                    R.id.tv_timeline_task_status_team);
            C2OpenSansRegularTextView tvInnerContainerTaskCompletionTime = innerContainerView.findViewById(
                    R.id.tv_timeline_task_status_completion_time);

            String taskTeam = mContext.getString(R.string.team_header).
                    concat(StringUtil.SPACE).concat(assignedToStrArray[j]);
            tvInnerContainerTaskTeam.setText(taskTeam);
            tvInnerContainerTaskTeam.setTextColor(ContextCompat.getColor(mContext, R.color.primary_white));

            String completedTime;

            // If true, it means completedDateTimeStrArray does NOT contain invalid string
            // and completedDateTime is not an invalid string
            if (completedDateTimeStrArray.length == assignedToStrArray.length &&
                    !completedDateTimeStrArray[j].equalsIgnoreCase(StringUtil.INVALID_STRING)) {
                completedTime = DateTimeUtil.dateToCustomTimeStringFormat(
                        DateTimeUtil.stringToDate(completedDateTimeStrArray[j]));
            } else { // Else, set empty string
                completedTime = StringUtil.EMPTY_STRING;
            }

            tvInnerContainerTaskCompletionTime.setText(completedTime);
            tvInnerContainerTaskCompletionTime.setTextColor(ContextCompat.getColor(mContext, R.color.primary_white));

            itemViewHolder.getLinearLayoutInnerContainerTeamTaskStatus().addView(innerContainerView);

            // -------------------- Get Final Completion Time --------------------
            if (StringUtil.EMPTY_STRING.equalsIgnoreCase(finalCompletionTime)) {
                finalCompletionTime = completedTime;
            }

            // Compare to get latest completion time with current one
            // True if completedTime is more than finalCompletionTime
            if (completedTime.compareTo(finalCompletionTime) > 0) {
                finalCompletionTime = completedTime;
            }

            // -------------------- Get Final Status and Set Inner Container Statuses --------------------
            if (EStatus.NEW.toString().equalsIgnoreCase(statusStrArray[j])) {
                isAllCompleted = false;
                imgInnerContainerTaskStatusIcon.setImageDrawable(ResourcesCompat.
                        getDrawable(mContext.getResources(), R.drawable.icon_new_unselected, null));
                tvInnerContainerTaskStatus.setText(mContext.getString(R.string.task_status_new));
                tvInnerContainerTaskStatus.setTextColor(ContextCompat.getColor(mContext, R.color.primary_white));
            } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(statusStrArray[j])) {
                isAllCompleted = false;
                isAllNew = false;
                imgInnerContainerTaskStatusIcon.setImageDrawable(ResourcesCompat.
                        getDrawable(mContext.getResources(), R.drawable.task_in_progress, null));
                tvInnerContainerTaskStatus.setText(mContext.getString(R.string.task_status_in_progress));
                tvInnerContainerTaskStatus.setTextColor(ContextCompat.getColor(mContext, R.color.task_status_yellow));
            } else {
                isAllNew = false;
                imgInnerContainerTaskStatusIcon.setImageDrawable(ResourcesCompat.
                        getDrawable(mContext.getResources(), R.drawable.task_completed, null));
                tvInnerContainerTaskStatus.setText(mContext.getString(R.string.task_status_completed));
                tvInnerContainerTaskStatus.setTextColor(ContextCompat.getColor(mContext, R.color.dull_green));
            }
        }

        // -------------------- Set Final Completion Time and Final Status --------------------
        if (isAllCompleted) {
            itemViewHolder.getTvFinalCompletionTime().setText(finalCompletionTime);
            itemViewHolder.getImgTimelineLineStatus().setImageDrawable(ResourcesCompat.
                    getDrawable(mContext.getResources(), R.drawable.task_completed, null));
        } else if (isAllNew) {
            itemViewHolder.getImgTimelineLineStatus().setImageDrawable(ResourcesCompat.
                    getDrawable(mContext.getResources(), R.drawable.icon_new_unselected, null));
        } else {
            itemViewHolder.getImgTimelineLineStatus().setImageDrawable(ResourcesCompat.
                    getDrawable(mContext.getResources(), R.drawable.task_in_progress, null));
        }
    }

    public void setTimelineListItems(List<TaskModel> timelinePhaseListItems) {
        mTimelinePhaseListItems = timelinePhaseListItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTimelinePhaseListItems.size();
    }
}
