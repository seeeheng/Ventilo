package sg.gov.dsta.mobileC3.ventilo.activity.timeline;

import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import org.qap.ctimelineview.TimelineViewAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import sg.gov.dsta.mobileC3.ventilo.R;

public class TimelineFragment extends Fragment {

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootTimelineView = inflater.inflate(R.layout.fragment_timeline, container, false);

        //Timeline
        ArrayList<TimelineRow> timelineRowsList = createTimelineData();

        // Create the Timeline Adapter
        // Param: orderTheList - if true, it will be sorted by date
        ArrayAdapter<TimelineRow> myAdapter = new TimelineViewAdapter(getActivity(), 0,
                timelineRowsList, false);

        // Get the ListView and Bind it with the Timeline Adapter
        ListView myListView = (ListView) rootTimelineView.findViewById(R.id.timeline_listView);
        myListView.setAdapter(myAdapter);
        return rootTimelineView;
    }

    private ArrayList<TimelineRow> createTimelineData() {
        // Create Timeline rows List
        ArrayList<TimelineRow> timelineRowsList = new ArrayList<>();

        // Create new timeline row (Row Id)
        TimelineRow myRow = new TimelineRow(0);

        Date date1 = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        calendar.set(Calendar.HOUR_OF_DAY, 10);// for 6 hour
        calendar.set(Calendar.MINUTE, 30);// for 0 min
        calendar.set(Calendar.SECOND, 0);// for 0 sec
        date1 = calendar.getTime();
//        System.out.println(calendar.getTime());// print 'Mon Mar 28 06:00:00 ALMT 2016'

        // To set the row Date (optional)
        myRow.setDate(date1);
        // To set the row Title (optional)
        myRow.setTitle("Phase 3 - SWG Ops");
        // To set the row Description (optional)
        myRow.setDescription("Mission is to capture target...");
        // To set the row bitmap image (optional)
        myRow.setImage(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        // To set row Below Line Color (optional) - Grey
        myRow.setBellowLineColor(Color.argb(255, 187, 187, 187));
        // To set row Below Line Size in dp (optional)
        myRow.setBellowLineSize(6);
        // To set row Image Size in dp (optional)
        myRow.setImageSize(40);
        // To set background color of the row image (optional)
        myRow.setBackgroundColor(Color.argb(255, 187, 187, 187));
        // To set the Background Size of the row image in dp (optional)
        myRow.setBackgroundSize(60);
        // To set row Date text color (optional)
        myRow.setDateColor(Color.argb(255, 255, 255, 255));
        // To set row Title text color (optional)
        myRow.setTitleColor(Color.argb(255, 255, 255, 255));
        // To set row Description text color (optional)
        myRow.setDescriptionColor(Color.argb(255, 255, 255, 255));

        // Create new timeline row 2 (Row Id)
        TimelineRow myRow2 = new TimelineRow(0);

        Date date2 = new Date();
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(date2);
        calendar2.set(Calendar.HOUR_OF_DAY, 11);// for 6 hour
        calendar2.set(Calendar.MINUTE, 45);// for 0 min
        calendar2.set(Calendar.SECOND, 0);// for 0 sec
        date2 = calendar2.getTime();

        // To set the row Date (optional)
        myRow2.setDate(date2);
        // To set the row Title (optional)
        myRow2.setTitle("Phase 4 - SWG AAR");
        // To set the row Description (optional)
        myRow2.setDescription("Points to take note are...");
        // To set the row bitmap image (optional)
        myRow2.setImage(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        // To set row Below Line Color (optional) - Grey
        myRow2.setBellowLineColor(Color.argb(255, 187, 187, 187));
        // To set row Below Line Size in dp (optional)
        myRow2.setBellowLineSize(6);
        // To set row Image Size in dp (optional)
        myRow2.setImageSize(40);
        // To set background color of the row image (optional)
        myRow2.setBackgroundColor(Color.argb(255, 187, 187, 187));
        // To set the Background Size of the row image in dp (optional)
        myRow2.setBackgroundSize(60);
        // To set row Date text color (optional)
        myRow2.setDateColor(Color.argb(255, 255, 255, 255));
        // To set row Title text color (optional)
        myRow2.setTitleColor(Color.argb(255, 255, 255, 255));
        // To set row Description text color (optional)
        myRow2.setDescriptionColor(Color.argb(255, 255, 255, 255));

        // Add the new row to the list
        timelineRowsList.add(myRow);
        timelineRowsList.add(myRow2);

        return timelineRowsList;
    }
}
