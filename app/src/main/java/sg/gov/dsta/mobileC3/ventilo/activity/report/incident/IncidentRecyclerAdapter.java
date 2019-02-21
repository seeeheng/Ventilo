package sg.gov.dsta.mobileC3.ventilo.activity.report.incident;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.report.task.TaskViewHolder;
import sg.gov.dsta.mobileC3.ventilo.model.incident.IncidentItemModel;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskItemModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.task.EStatus;

public class IncidentRecyclerAdapter extends RecyclerView.Adapter<IncidentViewHolder> {

    List<IncidentItemModel> mIncidentListItems;

    private Context mContext;

    public IncidentRecyclerAdapter(Context context, List<IncidentItemModel> incidentListItems) {
        this.mContext = context;
        mIncidentListItems = incidentListItems;
    }

    @Override
    public IncidentViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_incident, viewGroup, false);

        return new IncidentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(IncidentViewHolder itemViewHolder, final int i) {
        IncidentItemModel item = mIncidentListItems.get(i);

        String reporter = "Reported By: ".concat(item.getReporter());
        itemViewHolder.getTvReporter().setText(reporter);
        itemViewHolder.getCircleImgAvatar().setImageDrawable(item.getReporterAvatar());

        String title = "Title: ".concat(item.getTitle());
        itemViewHolder.getTvTitle().setText(title);

        // Other RelativeLayouts
//        itemViewHolder.getRelativeLayoutDeleteIcon().setVisibility(View.GONE);
//        itemViewHolder.getRelativeLayoutStartIcon().setVisibility(View.GONE);
//        itemViewHolder.getRelativeLayoutDoneIcon().setVisibility(View.GONE);

        String dateTimeString = "Reported Time: ".concat(DateTimeUtil.getTimeDifference(mContext, item.getReportedDateTime()));
        itemViewHolder.getTvDateTime().setText(dateTimeString);

//        itemViewHolder.get().setText(item.getCreatedBy());
//        itemViewHolder.getMNumCommentTV().setText(String.valueOf(item.getComments().size()));
////        itemViewHolder.getMTagTV().setText(item.get);
//        Date date = new Date();
//        itemViewHolder.getMTimeAgoTV().setText(DataAccess.timeAgo(date.getTime() - item.getCreatedDate().getTime()));
        // left images


//        itemViewHolder.mParentLinearLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Log.d(TAG, "onClick:" + mTaskListItems.get(i));
//                Intent intent = new Intent(context, TasksViewActivity.class);
//                context.startActivity(intent);
//            }
//        });
    }

    public void addItem(String title, String description) {
        IncidentItemModel newTaskItemModel = new IncidentItemModel();
        newTaskItemModel.setId(String.valueOf(mIncidentListItems.size() + 1));
        newTaskItemModel.setReporter("Desmond (D01)");
        newTaskItemModel.setReporterAvatar(mContext.getDrawable(R.drawable.default_soldier_icon));
        newTaskItemModel.setTitle(title);
        newTaskItemModel.setDescription(description);
        Date currentDateTime = Calendar.getInstance().getTime();
        newTaskItemModel.setReportedDateTime(currentDateTime);

        mIncidentListItems.add(newTaskItemModel);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mIncidentListItems.size();
    }

}
