package sg.gov.dsta.mobileC3.ventilo.activity.task;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import org.greenrobot.eventbus.EventBus;

import de.hdodenhof.circleimageview.CircleImageView;
import lombok.Data;
import lombok.EqualsAndHashCode;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.listener.DebounceOnClickListener;
import sg.gov.dsta.mobileC3.ventilo.model.eventbus.TaskEvent;
import sg.gov.dsta.mobileC3.ventilo.util.ListenerUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2FuturaBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;

@Data
@EqualsAndHashCode(callSuper=false)
public class TaskViewHolder extends RecyclerView.ViewHolder {

    private LinearLayout linearLayoutAdHocHighPriority;
    private C2FuturaBoldTextView tvPhase;
    private C2OpenSansBoldTextView tvTitle;
    private C2OpenSansSemiBoldTextView tvStatus;
    private C2OpenSansRegularTextView tvAssignedTo;
    private C2OpenSansRegularTextView tvScheduledTime;
    private C2OpenSansRegularTextView tvShortDescription;
    private CircleImageView circleImgStatus;

    private RelativeLayout relativeLayoutBackground;
    private RelativeLayout relativeLayoutForeground;
    private RelativeLayout relativeLayoutDeleteButton;
    private RelativeLayout relativeLayoutSecondSwipeActionButton;
    private RelativeLayout relativeLayoutFirstSwipeActionButton;

    private AppCompatImageView imgDelete;
    private AppCompatImageView imgSecondSwipeAction;
    private AppCompatImageView imgFirstSwipeAction;

    protected TaskViewHolder(View itemView) {
        super(itemView);

        linearLayoutAdHocHighPriority = itemView.findViewById(R.id.layout_recycler_task_header_ad_hoc_high_priority);
        tvPhase = itemView.findViewById(R.id.tv_recycler_task_header_phase);
        tvTitle = itemView.findViewById(R.id.tv_recycler_task_header_title);
        tvStatus = itemView.findViewById(R.id.tv_recycler_task_header_status);
        tvAssignedTo = itemView.findViewById(R.id.tv_recycler_task_assigned_to);
        tvScheduledTime = itemView.findViewById(R.id.tv_recycler_task_scheduled_time);
        tvShortDescription = itemView.findViewById(R.id.tv_recycler_task_short_description);
        circleImgStatus = itemView.findViewById(R.id.circle_img_view_recyler_task_status);

        relativeLayoutBackground = itemView.findViewById(R.id.layout_recycler_task_view_background);
        relativeLayoutForeground = itemView.findViewById(R.id.layout_recycler_task_view_foreground);
        relativeLayoutDeleteButton = itemView.findViewById(R.id.layout_task_swipe_action_status_delete);
        relativeLayoutSecondSwipeActionButton = itemView.findViewById(R.id.layout_task_second_swipe_action_status);
        relativeLayoutFirstSwipeActionButton = itemView.findViewById(R.id.layout_task_first_swipe_action_status);

        imgDelete = itemView.findViewById(R.id.img_task_swipe_action_status_delete);
        imgSecondSwipeAction = itemView.findViewById(R.id.img_task_second_swipe_action_status);
        imgFirstSwipeAction = itemView.findViewById(R.id.img_task_first_swipe_action_status);

        itemView.setOnClickListener(onMainForegroundLayoutClickListener);
    }

    private DebounceOnClickListener onMainForegroundLayoutClickListener =
            new DebounceOnClickListener(ListenerUtil.LONG_MINIMUM_ON_CLICK_INTERVAL_IN_MILLISEC) {

        @Override
        public void onDebouncedClick(View view) {
            EventBus.getDefault().post(TaskEvent.getInstance().setItemPos(getAdapterPosition()));
        }
    };
}