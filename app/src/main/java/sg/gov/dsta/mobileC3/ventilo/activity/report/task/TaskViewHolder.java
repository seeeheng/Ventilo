package sg.gov.dsta.mobileC3.ventilo.activity.report.task;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import de.hdodenhof.circleimageview.CircleImageView;
import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoBlackTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoItalicBlackTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2LatoRegularTextView;

@Data
public class TaskViewHolder extends RecyclerView.ViewHolder {

    private CircleImageView circleImgAvatar;
    private C2LatoBlackTextView tvTitle;
    private C2LatoItalicBlackTextView tvStatus;
    private C2LatoRegularTextView tvAssignee;
    private C2LatoRegularTextView tvDateTime;
    private CircleImageView circleImgStatus;

    private RelativeLayout relativeLayoutDeleteIcon;
    private RelativeLayout relativeLayoutStartIcon;
    private RelativeLayout relativeLayoutDoneIcon;

    protected TaskViewHolder(View itemView) {
        super(itemView);

        circleImgAvatar = itemView.findViewById(R.id.circle_img_view_recycler_task_avatar);
        tvTitle = itemView.findViewById(R.id.tv_recycler_task_item_header_title);
        tvStatus = itemView.findViewById(R.id.tv_recycler_task_item_status);
        tvAssignee = itemView.findViewById(R.id.tv_recycler_task_assignee);
        tvDateTime = itemView.findViewById(R.id.tv_recycler_task_reported_datetime);
        circleImgStatus = itemView.findViewById(R.id.circle_img_view_recyler_task_status);

        relativeLayoutDeleteIcon = itemView.findViewById(R.id.layout_recycler_task_delete);
        relativeLayoutStartIcon = itemView.findViewById(R.id.layout_recycler_task_start);
        relativeLayoutDoneIcon = itemView.findViewById(R.id.layout_recycler_task_done);
    }
}