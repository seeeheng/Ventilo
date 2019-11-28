package sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.sitRepPersonnelStatus;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;

public class DashboardSitRepPersonnelStatusRecyclerAdapter extends RecyclerView.Adapter<DashboardSitRepPersonnelStatusViewHolder> {

    List<SitRepModel> mSitRepListItems;
    private Context mContext;

    public DashboardSitRepPersonnelStatusRecyclerAdapter(Context context, List<SitRepModel> sitRepListItems) {
        this.mContext = context;
        mSitRepListItems = sitRepListItems;
    }

    @Override
    public DashboardSitRepPersonnelStatusViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_dashboard_sitrep_personnel_status,
                viewGroup, false);

        return new DashboardSitRepPersonnelStatusViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DashboardSitRepPersonnelStatusViewHolder itemViewHolder, final int i) {
        SitRepModel sitRepModel = mSitRepListItems.get(i);

        itemViewHolder.getTvTeam().setText(mContext.getString(R.string.team_header).
                concat(StringUtil.SPACE).concat(String.valueOf(sitRepModel.getReporter())));
        itemViewHolder.getTvT().setText(String.valueOf(sitRepModel.getPersonnelT()));
        itemViewHolder.getTvS().setText(String.valueOf(sitRepModel.getPersonnelS()));
        itemViewHolder.getTvD().setText(String.valueOf(sitRepModel.getPersonnelD()));
    }

    public void setSitRepListItems(List<SitRepModel> sitRepListItems) {
        mSitRepListItems = sitRepListItems;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mSitRepListItems.size();
    }
}
