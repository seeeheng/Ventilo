package sg.gov.dsta.mobileC3.ventilo.activity.radiolinkstatus;

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

public class RadioLinkStatusOfflineRecyclerAdapter extends RecyclerView.Adapter<RadioLinkStatusOfflineViewHolder> {

    List<UserModel> mUserListItems;
    List<WaveRelayRadioModel> mWaveRelayRadioListItems;

    private Context mContext;

    public RadioLinkStatusOfflineRecyclerAdapter(Context context, List<UserModel> userListItems,
                                                 List<WaveRelayRadioModel> waveRelayRadioListItems) {
        this.mContext = context;
        mUserListItems = userListItems;
        mWaveRelayRadioListItems = waveRelayRadioListItems;
    }

    @Override
    public RadioLinkStatusOfflineViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_radio_link_status, viewGroup, false);

        return new RadioLinkStatusOfflineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RadioLinkStatusOfflineViewHolder itemViewHolder, final int i) {
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
                    connectionStatusMessageStrBuilder.append(mContext.getString(
                            R.string.radio_link_status_offline_since));
                    connectionStatusMessageStrBuilder.append(StringUtil.SPACE);
                    connectionStatusMessageStrBuilder.append(DateTimeUtil.getTimeDifference(
                            DateTimeUtil.stringToDate(lastKnownOfflineDateTime)));
                } else {
                    connectionStatusMessageStrBuilder.append(mContext.getString(
                            R.string.radio_link_status_no_records));
                }
            }

        } else {
            itemViewHolder.getImgRadioLinkStatusIcon().setImageDrawable(ResourcesCompat.getDrawable(
                    mContext.getResources(), R.drawable.icon_online, null));

            // Reported date/time
            String lastKnownOnlineDateTime = userModel.getLastKnownConnectionDateTime();

            if (lastKnownOnlineDateTime != null) {
                connectionStatusMessageStrBuilder.append(mContext.getString(R.string.radio_link_status_online_since));
                connectionStatusMessageStrBuilder.append(StringUtil.SPACE);
                connectionStatusMessageStrBuilder.append(DateTimeUtil.getTimeDifference(
                        DateTimeUtil.stringToDate(lastKnownOnlineDateTime)));
            }
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
