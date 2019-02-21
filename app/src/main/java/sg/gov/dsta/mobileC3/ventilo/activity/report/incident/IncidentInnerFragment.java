package sg.gov.dsta.mobileC3.ventilo.activity.report.incident;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.report.task.TaskRecyclerAdapter;
import sg.gov.dsta.mobileC3.ventilo.model.incident.IncidentItemModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.ReportFragmentConstants;

public class IncidentInnerFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private IncidentRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private FloatingActionButton mFabAddIncident;

    private List<IncidentItemModel> mIncidentListItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_inner_incident, container, false);
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.recycler_incident);

        mRecyclerView.setHasFixedSize(true);

        mRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        mRecyclerView.addOnItemTouchListener(new IncidentRecyclerItemTouchListener(getContext(), mRecyclerView, new IncidentRecyclerItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Fragment incidentDetailFragment = new IncidentInnerDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString(ReportFragmentConstants.KEY_INCIDENT, ReportFragmentConstants.VALUE_INCIDENT_VIEW);
                bundle.putString(ReportFragmentConstants.KEY_INCIDENT_TITLE, mIncidentListItems.get(position).getTitle());
                bundle.putString(ReportFragmentConstants.KEY_INCIDENT_DESCRIPTION, mIncidentListItems.get(position).getDescription());
                incidentDetailFragment.setArguments(bundle);

                // Pass info to fragment
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                ft.replace(R.id.layout_incident_inner_fragment, incidentDetailFragment, incidentDetailFragment.getClass().getSimpleName());
                ft.addToBackStack(incidentDetailFragment.getClass().getSimpleName());
                ft.commit();

            }

            @Override
            public void onLongItemClick(View view, int position) {
                Toast.makeText(getContext(), "Task onItemLongClick" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeLeft(View view, int position) {
//                Toast.makeText(getContext(), "Task onSwipeLeft" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeRight(View view, int position) {
//                Toast.makeText(getContext(), "Task onSwipeRight" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }
        }));

        // Set data for recycler view
        setUpDummyData();

        mRecyclerAdapter = new IncidentRecyclerAdapter(getContext(), mIncidentListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up floating action button
        mFabAddIncident = rootView.findViewById(R.id.fab_incident_add);
        mFabAddIncident.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment incidentDetailFragment = new IncidentInnerDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString(ReportFragmentConstants.KEY_INCIDENT, ReportFragmentConstants.VALUE_INCIDENT_ADD);
                incidentDetailFragment.setArguments(bundle);

                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                ft.replace(R.id.layout_incident_inner_fragment, incidentDetailFragment, incidentDetailFragment.getClass().getSimpleName());
                ft.addToBackStack(incidentDetailFragment.getClass().getSimpleName());
                ft.commit();
            }
        });

//        initOtherLayouts(inflater, container);
    }

//    private void setImageListeners(View recyclerView) {
//        AppCompatImageView imgDelete = recyclerView.findViewById(R.id.img_task_delete);
//        AppCompatImageView imgStart = recyclerView.findViewById(R.id.img_task_start);
//        AppCompatImageView imgDone = recyclerView.findViewById(R.id.img_task_done);
//
//        imgDelete.setOnClickListener(onDeleteClickListener);
//        imgStart.setOnClickListener(onStartClickListener);
//        imgDone.setOnClickListener(onDoneClickListener);
//
//        imgDelete.bringToFront();
//        imgStart.bringToFront();
//        imgDone.bringToFront();
//    }
//
//    private View.OnClickListener onDeleteClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            TaskViewHolder holder = (TaskViewHolder) view.getTag();
//            int position = holder.getAdapterPosition();
//            removeItemInRecycler(position);
//        }
//    };
//
//    private View.OnClickListener onStartClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            TaskViewHolder holder = (TaskViewHolder) view.getTag();
//            int position = holder.getAdapterPosition();
//            startItemInRecycler(position);
//        }
//    };
//
//    private View.OnClickListener onDoneClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            TaskViewHolder holder = (TaskViewHolder) view.getTag();
//            int position = holder.getAdapterPosition();
//            completeItemInRecycler(position);
//        }
//    };
//
//    private void startItemInRecycler(int position) {
//        mTaskListItems.get(position).setStatus(EStatus.IN_PROGRESS);
//        mRecyclerAdapter.notifyDataSetChanged();
//    }
//
//    private void completeItemInRecycler(int position) {
//        mTaskListItems.get(position).setStatus(EStatus.DONE);
//        mRecyclerAdapter.notifyDataSetChanged();
//    }
//
//    private void removeItemInRecycler(int position) {
//        mTaskListItems.remove(position);
//        mRecyclerView.removeViewAt(position);
//        mRecyclerAdapter.notifyItemRemoved(position);
//        mRecyclerAdapter.notifyItemRangeChanged(position, mTaskListItems.size());
//    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
//        super.onActivityCreated(savedInstanceState);
//
//        //Floating Action Button to add new task
//        mAddBtn = getView().findViewById(R.id.fab_task_add);
//
//        mAddBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                Intent intent = new Intent(getContext(), TasksAddActivity.class);
//                startActivity(intent);
//            }
//        });
//    }

    public IncidentRecyclerAdapter getAdapter() {
        return mRecyclerAdapter;
    }

    private void setUpDummyData() {
        mIncidentListItems = new ArrayList<>();

        IncidentItemModel incidentItemModel1 = new IncidentItemModel();
        incidentItemModel1.setId("1");
        incidentItemModel1.setReporter("Andy (A22)");
        incidentItemModel1.setReporterAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
        incidentItemModel1.setTitle("Found Explosive");
        incidentItemModel1.setDescription("IAD is located in the Engine Room. Beware of enemy intruders in adjacent rooms.");
        Date date1 = DateTimeUtil.getSpecifiedDate(2016, Calendar.DECEMBER, 29,
                Calendar.AM, 11, 30, 30);
        incidentItemModel1.setReportedDateTime(date1);

        IncidentItemModel incidentItemModel2 = new IncidentItemModel();
        incidentItemModel2.setId("2");
        incidentItemModel2.setReporter("Bastian (B22)");
        incidentItemModel2.setReporterAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
        incidentItemModel2.setTitle("Casualty");
        incidentItemModel2.setDescription("Casualty found in the Heli Deck. Beware of enemy intruders in adjacent rooms.");
        Date date2 = DateTimeUtil.getSpecifiedDateByMonth(Calendar.JANUARY);
        incidentItemModel2.setReportedDateTime(date2);

        IncidentItemModel incidentItemModel3 = new IncidentItemModel();
        incidentItemModel3.setId("3");
        incidentItemModel3.setReporter("Cassie (C22)");
        incidentItemModel3.setReporterAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
        incidentItemModel3.setTitle("Found Unidentified Object");
        incidentItemModel3.setDescription("Unidentified object is located in the Bridge. Beware of enemy intruders in adjacent rooms.");
        Date date3 = DateTimeUtil.getSpecifiedDateByDayOfMonth(1);
        incidentItemModel3.setReportedDateTime(date3);

        mIncidentListItems.add(incidentItemModel1);
        mIncidentListItems.add(incidentItemModel2);
        mIncidentListItems.add(incidentItemModel3);
    }

//    private BroadcastReceiver receiver = new BroadcastReceiver() {
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            String messageType = intent.getStringExtra(RestConstants.REST_REQUEST_TYPE);
//            String response = intent.getStringExtra(RestConstants.REST_REQUEST_RESULT);
//            int responseCode = intent.getIntExtra(RestConstants.REST_HTTP_STATUS, -1);
//
//            if ((responseCode < 200) || (responseCode >= 300)) {
//                //Error
//                Log.e("SuperC2", "HTTP Error: " + responseCode + " " + response);
//                return;
//            }
//
//            Gson gson = GsonCreator.createGson();
//            switch (messageType) {
//                case MessageType.GET_TASKS:
//
//
//                    break;
//
//                default:
//                    Log.e("SuperC2", "TaskFragment Receiver: Unknown MessageType: " + messageType + ": " + response);
//                    break;
//            }
//
//        }
//    };
}
