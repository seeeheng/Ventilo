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
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.radioLinkStatus.ERadioConnectionStatus;

public class RadioLinkStatusOfflineRecyclerAdapter extends RecyclerView.Adapter<RadioLinkStatusOfflineViewHolder> {

    List<UserModel> mUserListItems;

    private Context mContext;

    public RadioLinkStatusOfflineRecyclerAdapter(Context context, List<UserModel> userListItems) {
        this.mContext = context;
        mUserListItems = userListItems;
    }

    @Override
    public RadioLinkStatusOfflineViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_radio_link_status_offline, viewGroup, false);

        return new RadioLinkStatusOfflineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RadioLinkStatusOfflineViewHolder itemViewHolder, final int i) {
        UserModel userModel = mUserListItems.get(i);

        // Radio connection status
        if (ERadioConnectionStatus.OFFLINE.toString().equalsIgnoreCase(userModel.getRadioFullConnectionStatus())) {
            itemViewHolder.getImgRadioLinkStatusIcon().setImageDrawable(ResourcesCompat.getDrawable(
                    mContext.getResources(), R.drawable.icon_offline, null));
        } else {
            itemViewHolder.getImgRadioLinkStatusIcon().setImageDrawable(ResourcesCompat.getDrawable(
                    mContext.getResources(), R.drawable.icon_online, null));
        }

        // Team
        itemViewHolder.getTvTeam().setText(userModel.getUserId());

        // Last known online status datetime
        StringBuilder lastKnownOnlineDateTimeStrBuilder = new StringBuilder();
        String lastKnownOnlineDateTime = userModel.getLastKnownOnlineDateTime();

        if (lastKnownOnlineDateTime != null) {
            if (!lastKnownOnlineDateTime.equalsIgnoreCase(StringUtil.INVALID_STRING)) {
                lastKnownOnlineDateTimeStrBuilder.append(mContext.getString(
                        R.string.radio_link_status_offline_since));
                lastKnownOnlineDateTimeStrBuilder.append(StringUtil.SPACE);
                lastKnownOnlineDateTimeStrBuilder.append(DateTimeUtil.getTimeDifference(
                        DateTimeUtil.stringToDate(lastKnownOnlineDateTime)));
            } else {
                lastKnownOnlineDateTimeStrBuilder.append(mContext.getString(
                        R.string.radio_link_status_no_records));
            }
        }

        itemViewHolder.getTvLastConnectedTime().setText(lastKnownOnlineDateTimeStrBuilder.toString().trim());
    }

    public void setUserListItems(List<UserModel> userListItems) {
        mUserListItems = userListItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mUserListItems.size();
    }
}
