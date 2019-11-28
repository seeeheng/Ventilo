package sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.sitRepPersonnelStatus;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;

@Data
@EqualsAndHashCode(callSuper=false)
public class DashboardSitRepPersonnelStatusViewHolder extends RecyclerView.ViewHolder {

    private C2OpenSansRegularTextView tvTeam;
    private C2OpenSansRegularTextView tvT;
    private C2OpenSansRegularTextView tvS;
    private C2OpenSansRegularTextView tvD;

    protected DashboardSitRepPersonnelStatusViewHolder(View itemView) {
        super(itemView);

        tvTeam = itemView.findViewById(R.id.tv_dashboard_sitrep_personnel_status_team);
        tvT = itemView.findViewById(R.id.tv_dashboard_sitrep_personnel_status_T);
        tvS = itemView.findViewById(R.id.tv_dashboard_sitrep_personnel_status_S);
        tvD = itemView.findViewById(R.id.tv_dashboard_sitrep_personnel_status_D);
    }
}