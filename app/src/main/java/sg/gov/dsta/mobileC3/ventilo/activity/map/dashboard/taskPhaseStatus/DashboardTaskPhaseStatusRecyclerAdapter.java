package sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.taskPhaseStatus;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class DashboardTaskPhaseStatusRecyclerAdapter extends RecyclerView.Adapter<DashboardTaskPhaseStatusViewHolder> {

    private Context mContext;
    private List<String> mTeamListItems;
    private List<String> mPhaseStatusListItems;

    public DashboardTaskPhaseStatusRecyclerAdapter(Context context, List<String> teamListItems,
                                                   List<String> phaseStatusListItems) {
        this.mContext = context;
        mTeamListItems = teamListItems;
        mPhaseStatusListItems = phaseStatusListItems;
    }

    @Override
    public DashboardTaskPhaseStatusViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_dashboard_task_phase_status,
                viewGroup, false);

        return new DashboardTaskPhaseStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DashboardTaskPhaseStatusViewHolder itemViewHolder, final int i) {
        String team = mTeamListItems.get(i);
        String phaseStatus = mPhaseStatusListItems.get(i);

        itemViewHolder.getTvTeam().setText(mContext.getString(R.string.team_header).concat(StringUtil.SPACE).concat(team));

        String[] statusStrArray = StringUtil.
                removeCommasAndExtraSpaces(phaseStatus);

        if (statusStrArray.length == 4) {
            if (EStatus.COMPLETE.toString().equalsIgnoreCase(statusStrArray[0])) {
                itemViewHolder.getTvPhaseOne().setTextColor(ContextCompat.getColor(
                        mContext, R.color.dull_green));
            } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(statusStrArray[0])) {
                itemViewHolder.getTvPhaseOne().setTextColor(ContextCompat.getColor(
                        mContext, R.color.task_status_yellow));
            } else {
                itemViewHolder.getTvPhaseOne().setTextColor(ContextCompat.getColor(
                        mContext, R.color.primary_white));
            }

            if (EStatus.COMPLETE.toString().equalsIgnoreCase(statusStrArray[1])) {
                itemViewHolder.getTvPhaseTwo().setTextColor(ContextCompat.getColor(
                        mContext, R.color.dull_green));
            } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(statusStrArray[1])) {
                itemViewHolder.getTvPhaseTwo().setTextColor(ContextCompat.getColor(
                        mContext, R.color.task_status_yellow));
            } else {
                itemViewHolder.getTvPhaseTwo().setTextColor(ContextCompat.getColor(
                        mContext, R.color.primary_white));
            }

            if (EStatus.COMPLETE.toString().equalsIgnoreCase(statusStrArray[2])) {
                itemViewHolder.getTvPhaseThree().setTextColor(ContextCompat.getColor(
                        mContext, R.color.dull_green));
            } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(statusStrArray[2])) {
                itemViewHolder.getTvPhaseThree().setTextColor(ContextCompat.getColor(
                        mContext, R.color.task_status_yellow));
            } else {
                itemViewHolder.getTvPhaseThree().setTextColor(ContextCompat.getColor(
                        mContext, R.color.primary_white));
            }

            if (EStatus.COMPLETE.toString().equalsIgnoreCase(statusStrArray[3])) {
                itemViewHolder.getTvPhaseFour().setTextColor(ContextCompat.getColor(
                        mContext, R.color.dull_green));
            } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(statusStrArray[3])) {
                itemViewHolder.getTvPhaseFour().setTextColor(ContextCompat.getColor(
                        mContext, R.color.task_status_yellow));
            } else {
                itemViewHolder.getTvPhaseFour().setTextColor(ContextCompat.getColor(
                        mContext, R.color.primary_white));
            }

            itemViewHolder.getTvPhaseOne().setText(statusStrArray[0]);
            itemViewHolder.getTvPhaseTwo().setText(statusStrArray[1]);
            itemViewHolder.getTvPhaseThree().setText(statusStrArray[2]);
            itemViewHolder.getTvPhaseFour().setText(statusStrArray[3]);
        }
    }

    public void setTeamPhaseStatusListItems(List<String> teamListItems, List<String> phaseStatusListItems) {
        mTeamListItems = teamListItems;
        mPhaseStatusListItems = phaseStatusListItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mTeamListItems.size();
    }
}
