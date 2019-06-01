package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;

public class SitRepRecyclerAdapter extends RecyclerView.Adapter<SitRepViewHolder> {

    List<SitRepModel> mSitRepListItems;

    private Context mContext;

    public SitRepRecyclerAdapter(Context context, List<SitRepModel> sitRepListItems) {
        this.mContext = context;
        mSitRepListItems = sitRepListItems;
    }

    @Override
    public SitRepViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_sitrep, viewGroup, false);

        return new SitRepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SitRepViewHolder itemViewHolder, final int i) {
        SitRepModel item = mSitRepListItems.get(i);

        StringBuilder sitRepTitleBuilder = new StringBuilder();
        sitRepTitleBuilder.append("Team ");
        sitRepTitleBuilder.append(item.getReporter());
        sitRepTitleBuilder.append(" SITREP");
        itemViewHolder.getTvHeaderTitle().setText(sitRepTitleBuilder.toString().trim());
        itemViewHolder.getTvLocation().setText(item.getLocation());
        itemViewHolder.getTvRequest().setText(item.getRequest());
        itemViewHolder.getTvTeam().setText(item.getReporter());

        itemViewHolder.getTvScheduledTime().setText(DateTimeUtil.getTimeDifference(
                DateTimeUtil.stringToDate(item.getCreatedDateTime())));
    }

    public void setSitRepListItems(List<SitRepModel> sitRepListItems) {
        mSitRepListItems = sitRepListItems;
        notifyDataSetChanged();
    }

    public SitRepModel getSitRepModelAtPosition(int pos) {
        return mSitRepListItems.get(pos);
    }

    @Override
    public int getItemCount() {
        return mSitRepListItems.size();
    }

}
