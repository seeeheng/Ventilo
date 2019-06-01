package sg.gov.dsta.mobileC3.ventilo.activity.user;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.videostream.VideoStreamDeleteOrSaveViewHolder;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;

public class UserSettingsCallsignTeamProfileRecyclerAdapter extends RecyclerView.Adapter<UserSettingsCallsignTeamProfileViewHolder> {

    List<UserModel> mUserListItems;

    private Context mContext;

    public UserSettingsCallsignTeamProfileRecyclerAdapter(Context context, List<UserModel> userListItems) {
        this.mContext = context;
        mUserListItems = userListItems;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public UserSettingsCallsignTeamProfileViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_user_settings_callsign_team_profile, viewGroup, false);

        return new UserSettingsCallsignTeamProfileViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(UserSettingsCallsignTeamProfileViewHolder itemViewHolder, final int i) {
        UserModel userModel = mUserListItems.get(i);

        itemViewHolder.getTvCallsign().setText(userModel.getUserId());

        String[] teamProfileGroup = StringUtil.removeCommasAndExtraSpaces(userModel.getTeam());
        StringBuilder teamProfileName = new StringBuilder();

        for (int j = 0; j < teamProfileGroup.length; j++) {
            teamProfileName.append(mContext.getString(R.string.team_header));
            teamProfileName.append(StringUtil.SPACE);
            teamProfileName.append(teamProfileGroup[j]);

            if (j != teamProfileGroup.length - 1) {
                teamProfileName.append(StringUtil.COMMA);
                teamProfileName.append(StringUtil.SPACE);
            }
        }

        itemViewHolder.getTvTeamProfile().setText(teamProfileName.toString());
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
