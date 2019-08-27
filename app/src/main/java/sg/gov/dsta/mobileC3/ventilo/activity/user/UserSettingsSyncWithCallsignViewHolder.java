package sg.gov.dsta.mobileC3.ventilo.activity.user;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;

@Data
@EqualsAndHashCode(callSuper=false)
public class UserSettingsSyncWithCallsignViewHolder extends RecyclerView.ViewHolder {

    private C2OpenSansSemiBoldTextView tvCallsign;

    protected UserSettingsSyncWithCallsignViewHolder(View itemView, UserSettingsSyncWithCallsignRecyclerAdapter adapter) {
        super(itemView);

        tvCallsign = itemView.findViewById(R.id.tv_user_settings_sync_with_callsign);
    }
}