package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;

@Data
@EqualsAndHashCode(callSuper=false)
public class SitRepViewHolder extends RecyclerView.ViewHolder {

    private C2OpenSansBoldTextView tvHeaderTitle;
    private C2OpenSansRegularTextView tvLocation;
    private C2OpenSansRegularTextView tvRequest;
    private C2OpenSansRegularTextView tvTeam;
    private C2OpenSansRegularTextView tvScheduledTime;

    protected SitRepViewHolder(View itemView) {
        super(itemView);

        tvHeaderTitle = itemView.findViewById(R.id.tv_recycler_sitrep_header_title);
        tvLocation = itemView.findViewById(R.id.tv_recycler_sitrep_location);
        tvRequest = itemView.findViewById(R.id.tv_recycler_sitrep_request);
        tvTeam = itemView.findViewById(R.id.tv_recycler_sitrep_team);
        tvScheduledTime = itemView.findViewById(R.id.tv_recycler_sitrep_scheduled_time);
    }
}