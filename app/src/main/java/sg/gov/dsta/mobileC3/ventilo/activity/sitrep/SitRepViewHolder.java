package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import de.hdodenhof.circleimageview.CircleImageView;
import lombok.Data;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansItalicBlackTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;

@Data
public class SitRepViewHolder extends RecyclerView.ViewHolder {

    private CircleImageView circleImgAvatar;
    private C2OpenSansItalicBlackTextView tvReporter;
    private C2OpenSansRegularTextView tvDateTime;

//    private RelativeLayout relativeLayoutDeleteIcon;

    protected SitRepViewHolder(View itemView) {
        super(itemView);

        circleImgAvatar = itemView.findViewById(R.id.circle_img_view_recycler_sitrep_avatar);
        tvReporter = itemView.findViewById(R.id.tv_recycler_sitrep_reported_by);
        tvDateTime = itemView.findViewById(R.id.tv_recycler_sitrep_reported_datetime);

//        relativeLayoutDeleteIcon = itemView.findViewById(R.id.layout_recycler_task_delete);
    }
}