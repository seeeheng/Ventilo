package sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.taskPhaseStatus;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;

@Data
@EqualsAndHashCode(callSuper=false)
public class DashboardTaskPhaseStatusViewHolder extends RecyclerView.ViewHolder {

    private C2OpenSansRegularTextView tvTeam;
    private C2OpenSansRegularTextView tvPhaseOne;
    private C2OpenSansRegularTextView tvPhaseTwo;
    private C2OpenSansRegularTextView tvPhaseThree;
    private C2OpenSansRegularTextView tvPhaseFour;

    protected DashboardTaskPhaseStatusViewHolder(View itemView) {
        super(itemView);

        tvTeam = itemView.findViewById(R.id.tv_dashboard_task_phase_status_team);
        tvPhaseOne = itemView.findViewById(R.id.tv_dashboard_task_phase_status_phase_one);
        tvPhaseTwo = itemView.findViewById(R.id.tv_dashboard_task_phase_status_phase_two);
        tvPhaseThree = itemView.findViewById(R.id.tv_dashboard_task_phase_status_phase_three);
        tvPhaseFour = itemView.findViewById(R.id.tv_dashboard_task_phase_status_phase_four);
    }
}