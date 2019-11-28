package sg.gov.dsta.mobileC3.ventilo.activity.timeline;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2FuturaBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;

@Data
@EqualsAndHashCode(callSuper=false)
public class TimelineViewHolder extends RecyclerView.ViewHolder {

    private C2OpenSansRegularTextView tvFinalCompletionTime;
    private AppCompatImageView imgTimelineLineStatus;
    private LinearLayout linearLayoutHeaderAdHocHighPriorityIcon;
    private C2FuturaBoldTextView tvHeaderPhaseNumber;
    private C2OpenSansBoldTextView tvHeaderTitle;
    private C2OpenSansRegularTextView tvHeaderShortDescription;
    private LinearLayout linearLayoutInnerContainerTeamTaskStatus;

//    private View viewTaskStatusDetail;

    protected TimelineViewHolder(View itemView) {
        super(itemView);

        tvFinalCompletionTime = itemView.findViewById(R.id.tv_recycler_timeline_final_completion_time);
        imgTimelineLineStatus = itemView.findViewById(R.id.img_timeline_line_status_icon);
        linearLayoutHeaderAdHocHighPriorityIcon = itemView.findViewById(R.id.layout_recycler_timeline_header_ad_hoc_high_priority);
        tvHeaderPhaseNumber = itemView.findViewById(R.id.tv_recycler_timeline_header_phase_number);
        tvHeaderTitle = itemView.findViewById(R.id.tv_recycler_timeline_header_title);
        tvHeaderShortDescription = itemView.findViewById(R.id.tv_recycler_timeline_short_description);
        linearLayoutInnerContainerTeamTaskStatus = itemView.findViewById(R.id.layout_timeline_group_inner_container_team_task_status);

//        viewTaskStatusDetail = itemView.findViewById(R.id.layout_timeline_row_inner_container_team_task_status);
    }
}