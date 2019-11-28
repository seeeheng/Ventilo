package sg.gov.dsta.mobileC3.ventilo.activity.map;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansLightTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;

@Data
@EqualsAndHashCode(callSuper=false)
public class MapPersonnelLinkStatusViewHolder extends RecyclerView.ViewHolder {

    private AppCompatImageView imgRadioLinkStatusIcon;
    private C2OpenSansBoldTextView tvTeam;
    private C2OpenSansLightTextView tvLastConnectedTime;
    private C2OpenSansSemiBoldTextView tvSnr;

    protected MapPersonnelLinkStatusViewHolder(View itemView) {
        super(itemView);

        imgRadioLinkStatusIcon = itemView.findViewById(R.id.img_radio_link_status_row_icon);
        tvTeam = itemView.findViewById(R.id.tv_radio_link_status_row_team);
        tvLastConnectedTime = itemView.findViewById(R.id.tv_radio_link_status_row_last_connected_time);
        tvSnr = itemView.findViewById(R.id.tv_radio_link_snr);
    }
}