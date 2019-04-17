package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.sitrep.SitRepModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;

public class SitRepFragment extends Fragment {

    private static final String TAG = "SitRepFragment";

    private RecyclerView mRecyclerView;
    private SitRepRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;
    private FloatingActionButton mFabAddSitRep;

    private List<SitRepModel> mSitRepListItems;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_sitrep, container, false);
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.recycler_sitrep);

        mRecyclerView.setHasFixedSize(true);

        mRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);
        mRecyclerView.addOnItemTouchListener(new SitRepRecyclerItemTouchListener(getContext(), mRecyclerView, new SitRepRecyclerItemTouchListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Fragment sitRepInnerDetailFragment = new SitRepDetailFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FragmentConstants.KEY_SITREP, FragmentConstants.VALUE_SITREP_VIEW);
                bundle.putString(FragmentConstants.KEY_SITREP_LOCATION, mSitRepListItems.get(position).getLocation());
                bundle.putString(FragmentConstants.KEY_SITREP_ACTIVITY, mSitRepListItems.get(position).getActivity());
                bundle.putInt(FragmentConstants.KEY_SITREP_PERSONNEL_T, mSitRepListItems.get(position).getPersonnelT());
                bundle.putInt(FragmentConstants.KEY_SITREP_PERSONNEL_S, mSitRepListItems.get(position).getPersonnelS());
                bundle.putInt(FragmentConstants.KEY_SITREP_PERSONNEL_D, mSitRepListItems.get(position).getPersonnelD());
                bundle.putString(FragmentConstants.KEY_SITREP_NEXT_COA, mSitRepListItems.get(position).getNextCOA());
                bundle.putString(FragmentConstants.KEY_SITREP_REQUEST, mSitRepListItems.get(position).getRequest());
                sitRepInnerDetailFragment.setArguments(bundle);

                // Pass info to fragment
                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                ft.replace(R.id.layout_sitrep_inner_fragment, sitRepInnerDetailFragment, sitRepInnerDetailFragment.getClass().getSimpleName());
                ft.addToBackStack(sitRepInnerDetailFragment.getClass().getSimpleName());
                ft.commit();
            }

            @Override
            public void onLongItemClick(View view, int position) {
                Toast.makeText(getContext(), "Sit Rep onItemLongClick" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeLeft(View view, int position) {
//                Toast.makeText(getContext(), "Sit Rep onSwipeLeft" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSwipeRight(View view, int position) {
//                Toast.makeText(getContext(), "Sit Rep onSwipeRight" + "(" + position + ")", Toast.LENGTH_SHORT).show();
            }
        }));

        // Set data for recycler view
        setUpRecyclerData();

        mRecyclerAdapter = new SitRepRecyclerAdapter(getContext(), mSitRepListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set up floating action button
        mFabAddSitRep = rootView.findViewById(R.id.fab_sitrep_add);
        mFabAddSitRep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Fragment sitRepInnerAddFragment = new SitRepAddFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FragmentConstants.KEY_SITREP, FragmentConstants.VALUE_SITREP_ADD);
                sitRepInnerAddFragment.setArguments(bundle);

                FragmentManager fm = getActivity().getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
                ft.replace(R.id.layout_sitrep_inner_fragment, sitRepInnerAddFragment, sitRepInnerAddFragment.getClass().getSimpleName());
                ft.addToBackStack(sitRepInnerAddFragment.getClass().getSimpleName());
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
//        mTaskListItems.get(position).setStatus(EStatus.COMPLETE);
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

    public SitRepRecyclerAdapter getAdapter() {
        return mRecyclerAdapter;
    }

    public void refreshData() {
        Log.d(TAG, "refreshData");
        setUpRecyclerData();

//        if (mRecyclerAdapter != null) {
//            mRecyclerAdapter.notifyDataSetChanged();
//        }
    }

    public void addItemInRecycler() {
        if (mRecyclerAdapter != null) {
            mRecyclerAdapter.notifyItemInserted(mSitRepListItems.size() - 1);
            mRecyclerAdapter.notifyItemRangeChanged(mSitRepListItems.size() - 1, mSitRepListItems.size());
        }
    }

    private void setUpRecyclerData() {
        if (mSitRepListItems == null) {
            mSitRepListItems = new ArrayList<>();
        }

        if (getTotalNumberOfSitReps() == 0) {
            SitRepModel sitRepModel1 = new SitRepModel();
            sitRepModel1.setId(0);
            sitRepModel1.setReporter(SharedPreferenceUtil.getCurrentUser(getActivity()));
            sitRepModel1.setReporterAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
            sitRepModel1.setLocation("BALESTIER");
            sitRepModel1.setActivity("Fire Fighting");
            sitRepModel1.setPersonnelT(6);
            sitRepModel1.setPersonnelS(5);
            sitRepModel1.setPersonnelD(4);
            sitRepModel1.setNextCOA("Inform HQ");
            sitRepModel1.setRequest("Additional MP");
            Date date1 = DateTimeUtil.getSpecifiedDate(2016, Calendar.DECEMBER, 29,
                    Calendar.AM, 11, 30, 30);
            sitRepModel1.setReportedDateTime(date1);

            mSitRepListItems.add(sitRepModel1);

            addItemsToLocalDatabase();

        } else {
            mSitRepListItems.clear();

            for (int i = 0; i < getTotalNumberOfSitReps(); i++) {
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                String sitRepInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.HEADER_SITREP).concat(SharedPreferenceConstants.SEPARATOR).
                        concat(String.valueOf(i));

                SitRepModel sitRepModel = new SitRepModel();
                sitRepModel.setId(pref.getInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ID), SharedPreferenceConstants.DEFAULT_INT));
                sitRepModel.setReporter(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REPORTER), SharedPreferenceConstants.DEFAULT_STRING));
                sitRepModel.setReporterAvatar(getContext().getDrawable(R.drawable.default_soldier_icon));
//                taskItem.setAssigneeAvatar(Objects.requireNonNull(getContext()).getDrawable(pref.getInt(taskInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                        concat(SharedPreferenceConstants.SUB_HEADER_TASK_ASSIGNEE_AVATAR_ID), SharedPreferenceConstants.DEFAULT_INT)));
                sitRepModel.setLocation(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_LOCATION), SharedPreferenceConstants.DEFAULT_STRING));
                sitRepModel.setActivity(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ACTIVITY), SharedPreferenceConstants.DEFAULT_STRING));
                sitRepModel.setPersonnelT(pref.getInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_T), SharedPreferenceConstants.DEFAULT_INT));
                sitRepModel.setPersonnelS(pref.getInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_S), SharedPreferenceConstants.DEFAULT_INT));
                sitRepModel.setPersonnelD(pref.getInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_D), SharedPreferenceConstants.DEFAULT_INT));
                sitRepModel.setNextCOA(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_NEXT_COA), SharedPreferenceConstants.DEFAULT_STRING));
                sitRepModel.setRequest(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REQUEST), SharedPreferenceConstants.DEFAULT_STRING));
                sitRepModel.setReportedDateTime(DateTimeUtil.stringToDate(pref.getString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                        concat(SharedPreferenceConstants.SUB_HEADER_SITREP_DATE), SharedPreferenceConstants.DEFAULT_STRING)));

                mSitRepListItems.add(sitRepModel);
            }
        }
    }

    /*
     * Obtain total number of sit reps of user
     */
    private int getTotalNumberOfSitReps() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int totalNumberOfSitReps = pref.getInt(SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SITREP_TOTAL_NUMBER), 0);

        return totalNumberOfSitReps;
    }

    /*
     * Add sit rep items to local database
     */
    private void addItemsToLocalDatabase() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();

        // Increment total number of sit reps by one, and store it
        String totalNumberOfSitRepsKey = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SITREP_TOTAL_NUMBER);

        editor.putInt(totalNumberOfSitRepsKey, mSitRepListItems.size());

        for (int i = 0; i < mSitRepListItems.size(); i++) {
            SitRepModel sitRepModel = mSitRepListItems.get(i);
            addSingleItemToLocalDatabase(editor, sitRepModel.getId(), sitRepModel.getReporter(),
                    R.drawable.default_soldier_icon, sitRepModel.getLocation(), sitRepModel.getActivity(),
                    sitRepModel.getPersonnelT(), sitRepModel.getPersonnelS(), sitRepModel.getPersonnelD(),
                    sitRepModel.getNextCOA(), sitRepModel.getRequest(),
                    DateTimeUtil.dateToString(sitRepModel.getReportedDateTime()));
        }
    }

    /*
     * Add single sit rep with respective fields
     */
    private void addSingleItemToLocalDatabase(SharedPreferences.Editor editor, int id, String reporter, int reporterAvatarId,
                                              String location, String activity, int personnelT, int personnelS, int personnelD,
                                              String nextCoa, String request, String date) {

        String sitRepInitials = SharedPreferenceConstants.INITIALS.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.HEADER_SITREP).concat(SharedPreferenceConstants.SEPARATOR).
                concat(String.valueOf(id));

        editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ID), id);
        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REPORTER), reporter);
        editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REPORTER_AVATAR_ID), reporterAvatarId);
        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_LOCATION), location);
        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_ACTIVITY), activity);
        editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_T), personnelT);
        editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_S), personnelS);
        editor.putInt(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_PERSONNEL_D), personnelD);
        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_NEXT_COA), nextCoa);
        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_REQUEST), request);
        editor.putString(sitRepInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_SITREP_DATE), date);

        editor.apply();
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
