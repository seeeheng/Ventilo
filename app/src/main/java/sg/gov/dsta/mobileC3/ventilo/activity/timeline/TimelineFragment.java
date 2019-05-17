package sg.gov.dsta.mobileC3.ventilo.activity.timeline;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.qap.ctimelineview.TimelineRow;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;

public class TimelineFragment extends Fragment {

    private static final int BG_SIZE_IN_DP = 60;
    private static final int IMG_SIZE_IN_DP = 40;
    private static final int BELLOW_SIZE_IN_DP = 6;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootTimelineView = inflater.inflate(R.layout.fragment_timeline, container, false);

        //Timeline
        ArrayList<TimelineRow> timelineRowList = createTimelineData();

        // Create the Timeline Adapter
        // Param: orderTheList - if true, it will be sorted by date
        ArrayAdapter<TimelineRow> timelineAdapter = new TimelineViewAdapter(getActivity(), 0,
                timelineRowList, false);

        // Get the ListView and Bind it with the Timeline Adapter
        ListView myListView = rootTimelineView.findViewById(R.id.timeline_listView);
        myListView.setAdapter(timelineAdapter);
        return rootTimelineView;
    }

    private ArrayList<TimelineRow> createTimelineData() {
        // Create Timeline rows List
        ArrayList<TimelineRow> timelineRowsList = new ArrayList<>();

        String phaseOneTitle = "Phase 1 - SWG Brief";
        String phaseOneDescription = "Upon set off...";
        Bitmap phaseOneStatusIcon = BitmapFactory.decodeResource(getResources(), R.drawable.task_done);
        TimelineRow rowPhaseOne = createSingleTimelineRowData(0, 9, 30, 0,
                phaseOneTitle, phaseOneDescription, phaseOneStatusIcon);

        String phaseTwoTitle = "Phase 2 - SWG Planning";
        String phaseTwoDescription = "Alpha One Eight is tasked to...";
        Bitmap phaseTwoStatusIcon = BitmapFactory.decodeResource(getResources(), R.drawable.task_done);
        TimelineRow rowPhaseTwo = createSingleTimelineRowData(1, 10, 00, 0,
                phaseTwoTitle, phaseTwoDescription, phaseTwoStatusIcon);

        String phaseThreeTitle = "Phase 3 - SWG Ops";
        String phaseThreeDescription = "Mission is to capture target...";
        Bitmap phaseThreeStatusIcon = BitmapFactory.decodeResource(getResources(), R.drawable.task_in_progress);
        TimelineRow rowPhaseThree = createSingleTimelineRowData(2, 10, 30, 0,
                phaseThreeTitle, phaseThreeDescription, phaseThreeStatusIcon);

        String phaseFourTitle = "Phase 4 - SWG AAR";
        String phaseFourDescription = "Points to take note are...";
        Bitmap phaseFourStatusIcon = BitmapFactory.decodeResource(getResources(), R.drawable.task_new);
        TimelineRow rowPhaseFour = createSingleTimelineRowData(3, 16, 45, 0,
                phaseFourTitle, phaseFourDescription, phaseFourStatusIcon);

        // Add the new row to the list
        timelineRowsList.add(rowPhaseOne);
        timelineRowsList.add(rowPhaseTwo);
        timelineRowsList.add(rowPhaseThree);
        timelineRowsList.add(rowPhaseFour);

        return timelineRowsList;
    }

    private TimelineRow createSingleTimelineRowData(int id, int hour, int minute, int second,
                                                    String title, String description, Bitmap statusIcon) {
        TimelineRow timelineRow = new TimelineRow(id);

        Date date = DateTimeUtil.getSpecifiedDate(Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                Calendar.getInstance().get(Calendar.AM_PM), 11, 30, 30);

        // To set the row Date (optional)
        timelineRow.setDate(date);
        // To set the row Title (optional)
        timelineRow.setTitle(title);
        // To set the row Description (optional)
        timelineRow.setDescription(description);
        // To set the row bitmap image (optional)
        timelineRow.setImage(statusIcon);
        // To set row Below Line Color (optional) - Grey
        timelineRow.setBellowLineColor(getResources().getColor(R.color.background_main_dark_charcoal));
        // To set row Below Line Size in dp (optional)
        timelineRow.setBellowLineSize(BELLOW_SIZE_IN_DP);
        // To set row Image Size in dp (optional)
        timelineRow.setImageSize(IMG_SIZE_IN_DP);
        // To set background color of the row image (optional)
        timelineRow.setBackgroundColor(getResources().getColor(R.color.background_main_dark_charcoal));
        // To set the Background Size of the row image in dp (optional)
        timelineRow.setBackgroundSize(BG_SIZE_IN_DP);
        // To set row Date text color (optional)
        timelineRow.setDateColor(getResources().getColor(R.color.primary_white));
        // To set row Title text color (optional)
        timelineRow.setTitleColor(getResources().getColor(R.color.primary_white));
        // To set row Description text color (optional)
        timelineRow.setDescriptionColor(getResources().getColor(R.color.primary_white));

        return timelineRow;
    }
}
