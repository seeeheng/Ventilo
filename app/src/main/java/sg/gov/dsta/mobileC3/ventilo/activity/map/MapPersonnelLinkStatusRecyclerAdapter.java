package sg.gov.dsta.mobileC3.ventilo.activity.map;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.waverelay.WaveRelayRadioModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;

public class MapPersonnelLinkStatusRecyclerAdapter extends RecyclerView.Adapter<MapPersonnelLinkStatusViewHolder> {

    List<UserModel> mUserListItems;
    List<WaveRelayRadioModel> mWaveRelayRadioListItems;

    private Context mContext;

    public MapPersonnelLinkStatusRecyclerAdapter(Context context, List<UserModel> userListItems,
                                                 List<WaveRelayRadioModel> waveRelayRadioListItems) {
        this.mContext = context;
        mUserListItems = userListItems;
        mWaveRelayRadioListItems = waveRelayRadioListItems;
    }

    @Override
    public MapPersonnelLinkStatusViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_radio_link_status, viewGroup, false);

        return new MapPersonnelLinkStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MapPersonnelLinkStatusViewHolder itemViewHolder, final int i) {
        UserModel userModel = mUserListItems.get(i);

        // Radio connection status
        StringBuilder connectionStatusMessageStrBuilder = new StringBuilder();
        if (ERadioConnectionStatus.OFFLINE.toString().equalsIgnoreCase(userModel.getRadioFullConnectionStatus())) {
            itemViewHolder.getImgRadioLinkStatusIcon().setImageDrawable(ResourcesCompat.getDrawable(
                    mContext.getResources(), R.drawable.icon_offline, null));

            // Last known offline status datetime
            String lastKnownOfflineDateTime = userModel.getLastKnownConnectionDateTime();

            if (lastKnownOfflineDateTime != null) {
                if (!lastKnownOfflineDateTime.equalsIgnoreCase(StringUtil.INVALID_STRING)) {
                    String lastKnownOfflineDateTimeInCustomStrFormat = DateTimeUtil.dateToCustomTimeStringFormat(
                            DateTimeUtil.stringToDate(lastKnownOfflineDateTime));
                    connectionStatusMessageStrBuilder.append(lastKnownOfflineDateTimeInCustomStrFormat);
                } else {
                    connectionStatusMessageStrBuilder.append(mContext.getString(
                            R.string.map_blueprint_personnel_link_status_no_records));
                }
            }

            itemViewHolder.getTvLastConnectedTime().setTextColor(ResourcesCompat.getColor(mContext.getResources(),
                    R.color.primary_text_grey, null));

        } else {
            itemViewHolder.getImgRadioLinkStatusIcon().setImageDrawable(ResourcesCompat.getDrawable(
                    mContext.getResources(), R.drawable.icon_ally, null));

            // Reported date/time
            String lastKnownOnlineDateTime = userModel.getLastKnownConnectionDateTime();

            if (lastKnownOnlineDateTime != null) {
                String lastKnownOnlineDateTimeInCustomStrFormat = DateTimeUtil.dateToCustomTimeStringFormat(
                        DateTimeUtil.stringToDate(lastKnownOnlineDateTime));
                connectionStatusMessageStrBuilder.append(lastKnownOnlineDateTimeInCustomStrFormat);
            }

            itemViewHolder.getTvLastConnectedTime().setTextColor(ResourcesCompat.getColor(mContext.getResources(),
                    R.color.primary_highlight_cyan, null));
        }

        String snr = StringUtil.N_A;

        for (int j = 0; j < mWaveRelayRadioListItems.size(); j++) {
            WaveRelayRadioModel waveRelayRadioModel = mWaveRelayRadioListItems.get(j);
            String userId = waveRelayRadioModel.getUserId();

            if (userModel.getUserId().equalsIgnoreCase(userId)) {
                snr = waveRelayRadioModel.getSignalToNoiseRatio();
            }
        }

        String snrDisplayString = mContext.getString(
                R.string.radio_link_snr).concat(StringUtil.COLON).
                concat(StringUtil.SPACE).concat(snr);

        // Team
        itemViewHolder.getTvTeam().setText(userModel.getUserId());
        itemViewHolder.getTvSnr().setText(snrDisplayString);
        itemViewHolder.getTvLastConnectedTime().setText(connectionStatusMessageStrBuilder.toString().trim());

    }

    public void setUserListItems(List<UserModel> userListItems) {
        mUserListItems = userListItems;
        notifyDataSetChanged();
    }

    public void setWaveRelayListItems(List<WaveRelayRadioModel> waveRelayRadioListItems) {
        mWaveRelayRadioListItems = waveRelayRadioListItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mUserListItems.size();
    }
}
