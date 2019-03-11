package sg.gov.dsta.mobileC3.ventilo.activity.radiolinkstatus;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.qap.ctimelineview.TimelineRow;

import java.util.ArrayList;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.timeline.TimelineViewAdapter;

public class RadioLinkStatusFragment extends Fragment {

    AppCompatImageView mImgRadioLinkStatus;
    FloatingActionButton mFabSwitchView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootRadioLinkStatusView = inflater.inflate(R.layout.fragment_radio_link_status, container, false);

        mImgRadioLinkStatus = rootRadioLinkStatusView.findViewById(R.id.img_radio_link_status);
        mFabSwitchView = rootRadioLinkStatusView.findViewById(R.id.fab_task_change);
        mFabSwitchView.setOnClickListener(onSwitchClickListener);

        return rootRadioLinkStatusView;
    }

    private View.OnClickListener onSwitchClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (mImgRadioLinkStatus.getVisibility() == View.GONE) {
                mImgRadioLinkStatus.setVisibility(View.VISIBLE);
            } else {
                mImgRadioLinkStatus.setVisibility(View.GONE);
            }
        }
    };
}
