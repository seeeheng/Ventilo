package sg.gov.dsta.mobileC3.ventilo.activity.user;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;

public class UserSettingsSyncWithCallsignRecyclerAdapter extends RecyclerView.Adapter<UserSettingsSyncWithCallsignViewHolder> {

    List<UserModel> mUserListItems;
    private Context mContext;

    public UserSettingsSyncWithCallsignRecyclerAdapter(Context context, List<UserModel> userListItems) {
        this.mContext = context;
        mUserListItems = userListItems;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public UserSettingsSyncWithCallsignViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_user_settings_sync_with_callsign, viewGroup, false);

        return new UserSettingsSyncWithCallsignViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(UserSettingsSyncWithCallsignViewHolder itemViewHolder, final int i) {
        UserModel userModel = mUserListItems.get(i);

        StringBuilder cctStrBuilder = new StringBuilder();
        cctStrBuilder.append(userModel.getUserId());

        // For CCT; For e.g. "999 (CCT)"
        if (EAccessRight.CCT.toString().equalsIgnoreCase(userModel.getRole())) {
            cctStrBuilder.append(StringUtil.SPACE);
            cctStrBuilder.append(StringUtil.OPEN_BRACKET);
            cctStrBuilder.append(mContext.getString(R.string.sync_with_callsign_cct));
            cctStrBuilder.append(StringUtil.CLOSE_BRACKET);
        }

        itemViewHolder.getTvCallsign().setText(cctStrBuilder.toString());
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
