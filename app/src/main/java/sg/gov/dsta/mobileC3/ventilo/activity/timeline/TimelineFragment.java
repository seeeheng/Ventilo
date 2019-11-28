package sg.gov.dsta.mobileC3.ventilo.activity.timeline;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.task.TaskModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.TaskViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularTextView;
import sg.gov.dsta.mobileC3.ventilo.util.enums.EIsValid;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EAdHocTaskPriority;
import sg.gov.dsta.mobileC3.ventilo.util.enums.task.EPhaseNo;
import timber.log.Timber;

public class TimelineFragment extends Fragment {

    private static final String TAG = TimelineFragment.class.getSimpleName();

//    private static final int BG_SIZE_IN_DP = 60;
//    private static final int IMG_SIZE_IN_DP = 40;
//    private static final int BELLOW_SIZE_IN_DP = 6;

    // View Models
    private TaskViewModel mTaskViewModel;

    // Main layout
    private View mRootView;

    // Recycler
    private RecyclerView mRecyclerViewPlanned;
    private RecyclerView mRecyclerViewAdHoc;
    private TimelinePlannedRecyclerAdapter mRecyclerAdapterPlanned;
    private TimelineAdHocRecyclerAdapter mRecyclerAdapterAdHoc;

    private List<TaskModel> mTimelinePhaseListItems;
    private List<TaskModel> mTimelineAdHocListItems;

    View mViewTimelineLine;
    View mViewCategoryHeaderPlanned;
    View mViewCategoryHeaderAdHoc;

    // Add new item UI
    private View mLayoutAddNewItem;

    private boolean mIsFragmentVisibleToUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);
        observerSetup();

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_timeline, container, false);
            initUI(mRootView);
        }

        return mRootView;
    }

    private void initUI(View rootView) {
        initAddNewItemUI(rootView);
        initHeaderUI(rootView);
        initRecyclerViewUI(rootView);
    }

    /**
     * Creates message when no Task is available
     * @param rootView
     */
    private void initAddNewItemUI(View rootView) {
        mLayoutAddNewItem = rootView.findViewById(R.id.layout_timeline_add_new_item);
        C2OpenSansRegularTextView tvAddNewItemCategory = mLayoutAddNewItem.findViewById(R.id.tv_add_new_item_category);
        AppCompatImageView imgAddNewItemCategory = mLayoutAddNewItem.findViewById(R.id.img_add_new_item_category);
        C2OpenSansRegularTextView tvAddNewItemCategoryBelow = mLayoutAddNewItem.findViewById(R.id.tv_add_new_item_category_below);
        imgAddNewItemCategory.setVisibility(View.GONE);
        tvAddNewItemCategoryBelow.setVisibility(View.GONE);

        tvAddNewItemCategory.setText(getString(R.string.no_task_timeline));
    }

    private void initHeaderUI(View rootView) {
        mViewTimelineLine = rootView.findViewById(R.id.view_recycler_timeline_top_line);
        mViewCategoryHeaderPlanned = rootView.findViewById(R.id.layout_recycler_category_header_planned);
        mViewCategoryHeaderAdHoc = rootView.findViewById(R.id.layout_recycler_category_header_ad_hoc);

        C2OpenSansRegularTextView tvHeaderTitlePlanned = mViewCategoryHeaderPlanned.findViewById(
                R.id.tv_recycler_category_header_title);
        C2OpenSansRegularTextView tvHeaderTitleAdHoc = mViewCategoryHeaderAdHoc.findViewById(
                R.id.tv_recycler_category_header_title);

        tvHeaderTitlePlanned.setText(getString(R.string.timeline_category_header_planned));
        tvHeaderTitleAdHoc.setText(getString(R.string.timeline_category_header_ad_hoc));
    }

    private void initRecyclerViewUI(View rootView) {
        mRecyclerViewPlanned = rootView.findViewById(R.id.recycler_timeline_planned);
        mRecyclerViewPlanned.setHasFixedSize(true);
        mRecyclerViewPlanned.setNestedScrollingEnabled(false);

        mRecyclerViewAdHoc = rootView.findViewById(R.id.recycler_timeline_ad_hoc);
        mRecyclerViewAdHoc.setHasFixedSize(false);
        mRecyclerViewAdHoc.setNestedScrollingEnabled(false);

        RecyclerView.LayoutManager recyclerLayoutManagerPlanned = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };

        RecyclerView.LayoutManager recyclerLayoutManagerAdHoc = new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return true;
            }
        };

        mRecyclerViewPlanned.setLayoutManager(recyclerLayoutManagerPlanned);
        mRecyclerViewAdHoc.setLayoutManager(recyclerLayoutManagerAdHoc);

        // Set data for recycler view
        if (mTimelinePhaseListItems == null) {
            mTimelinePhaseListItems = new ArrayList<>();
        }

        if (mTimelineAdHocListItems == null) {
            mTimelineAdHocListItems = new ArrayList<>();
        }

//        setUpRecyclerData();

        mRecyclerAdapterPlanned = new TimelinePlannedRecyclerAdapter(getContext(), mTimelinePhaseListItems);
        mRecyclerViewPlanned.setAdapter(mRecyclerAdapterPlanned);
        mRecyclerViewPlanned.setItemAnimator(new DefaultItemAnimator());

        mRecyclerAdapterAdHoc = new TimelineAdHocRecyclerAdapter(getContext(), mTimelineAdHocListItems);
        mRecyclerViewAdHoc.setAdapter(mRecyclerAdapterAdHoc);
        mRecyclerViewAdHoc.setItemAnimator(new DefaultItemAnimator());
    }

    /**
     * Removes 'Ad-Hoc' Tasks models, extracts and re-groups the remaining Task models into lists based on phases
     * and store these lists into a main list for the Recycler view
     *
     * @param taskModelList
     * @return
     */
    private List<TaskModel> getTimelinePlannedListItems(List<TaskModel> taskModelList) {
//        List<List<TaskModel>> timelinePlannedPhaseGroupListItems = new ArrayList<>();
        List<TaskModel> timelinePlannedListItems = new ArrayList<>();

        // Remove 'Ad Hoc' Tasks from list
        for (int i = 0; i < taskModelList.size(); i++) {
            if (!EPhaseNo.AD_HOC.toString().equalsIgnoreCase(taskModelList.get(i).getPhaseNo())) {
                timelinePlannedListItems.add(taskModelList.get(i));
            }
        }

        // Sort according to ascending order of phases (e.g. 1, 2, 3, 4...)
        timelinePlannedListItems.sort(TaskModel.getPhaseNoComparator());

//        // Extracts a list of all PhaseNo from the list of all TaskModel objects
//        List<String> timelinePhaseNoList = timelinePlannedListItems.stream().map(
//                TaskModel -> TaskModel.getPhaseNo()).collect(Collectors.toList());
//
//        // Obtain distinct list of PhaseNo (e.g. 3, 1, 4, 2...)
//        List<String> timelineDistinctPhaseNoList = timelinePhaseNoList.stream().distinct().collect(Collectors.toList());
//
//        // Store taskModels of each phase into a list, then stores each of these lists into a main list
//        List<TaskModel> timelinePhaseGroupList = new ArrayList<>();
//        for (int i = 0; i < timelineDistinctPhaseNoList.size(); i++) {
//            for (int j = 0; j < timelinePlannedListItems.size(); j++) {
//                if (timelinePlannedListItems.get(i).getPhaseNo().equalsIgnoreCase(timelineDistinctPhaseNoList.get(i))) {
//                    timelinePhaseGroupList.add(timelinePlannedListItems.get(i));
//                }
//            }
//
//            timelinePlannedPhaseGroupListItems.add(timelinePhaseGroupList);
//            timelinePhaseGroupList.clear();
//        }

        return timelinePlannedListItems;
    }

    /**
     * Removes 'Ad-Hoc' Tasks models, extracts and re-groups the remaining Task models into lists based on phases
     * and store these lists into a main list for the Recycler view
     *
     * @param taskModelList
     * @return
     */
    private List<TaskModel> getTimelineAdHocListItems(List<TaskModel> taskModelList) {
        List<TaskModel> timelineAdHocListItems = new ArrayList<>();

        // Retrieve 'Ad Hoc' Tasks from list
        for (int i = 0; i < taskModelList.size(); i++) {
            Timber.i("taskModelList.get(i).getPhaseNo() is %s" , taskModelList.get(i).getPhaseNo());
            if (EPhaseNo.AD_HOC.toString().equalsIgnoreCase(taskModelList.get(i).getPhaseNo())) {
                timelineAdHocListItems.add(taskModelList.get(i));
            }
        }

        List<TaskModel> timelineAdHocHighPriorityListItems = new ArrayList<>();
        List<TaskModel> timelineAdHocLowPriorityListItems = new ArrayList<>();
        for (int i = 0; i < timelineAdHocListItems.size(); i++) {
            String adHocTaskPriority = timelineAdHocListItems.get(i).getAdHocTaskPriority();
            if (adHocTaskPriority != null) {
                if (EAdHocTaskPriority.HIGH.toString().equalsIgnoreCase(adHocTaskPriority)) {
                    timelineAdHocHighPriorityListItems.add(timelineAdHocListItems.get(i));
                } else {
                    timelineAdHocLowPriorityListItems.add(timelineAdHocListItems.get(i));
                }
            }
        }

        timelineAdHocListItems.clear();
        timelineAdHocListItems.addAll(timelineAdHocHighPriorityListItems);
        timelineAdHocListItems.addAll(timelineAdHocLowPriorityListItems);

        Collections.sort(timelineAdHocListItems, new Comparator<TaskModel>() {
            public int compare(TaskModel taskModelOne, TaskModel taskModelTwo) {
                if (taskModelOne.getCreatedDateTime() == null ||
                        taskModelTwo.getCreatedDateTime() == null)
                    return 0;
                return taskModelOne.getCreatedDateTime().compareTo(taskModelTwo.getCreatedDateTime());
            }
        });

        return timelineAdHocListItems;
    }

    private void refreshUI() {
        if (mTimelinePhaseListItems.size() == 0 && mTimelineAdHocListItems.size() == 0) {
            mLayoutAddNewItem.setVisibility(View.VISIBLE);
            mViewTimelineLine.setVisibility(View.GONE);
            mViewCategoryHeaderPlanned.setVisibility(View.GONE);
            mViewCategoryHeaderAdHoc.setVisibility(View.GONE);
        } else if (mTimelinePhaseListItems.size() == 0) {
            mLayoutAddNewItem.setVisibility(View.GONE);
            mViewTimelineLine.setVisibility(View.VISIBLE);
            mViewCategoryHeaderPlanned.setVisibility(View.GONE);
            mViewCategoryHeaderAdHoc.setVisibility(View.VISIBLE);
        } else if (mTimelineAdHocListItems.size() == 0) {
            mLayoutAddNewItem.setVisibility(View.GONE);
            mViewTimelineLine.setVisibility(View.VISIBLE);
            mViewCategoryHeaderPlanned.setVisibility(View.VISIBLE);
            mViewCategoryHeaderAdHoc.setVisibility(View.GONE);
        } else {
            mLayoutAddNewItem.setVisibility(View.GONE);
            mViewTimelineLine.setVisibility(View.VISIBLE);
            mViewCategoryHeaderPlanned.setVisibility(View.VISIBLE);
            mViewCategoryHeaderAdHoc.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
//        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mTaskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);
//        mUserTaskJoinViewModel = ViewModelProviders.of(this).get(UserTaskJoinViewModel.class);

        /*
         * Refreshes recyclerview UI whenever there is a change in task (insert, update or delete)
         */
        mTaskViewModel.getAllTasksLiveData().observe(this, new Observer<List<TaskModel>>() {
            @Override
            public void onChanged(@Nullable List<TaskModel> taskModelList) {

                synchronized (mTimelinePhaseListItems) {
                    if (mTimelinePhaseListItems == null) {
                        mTimelinePhaseListItems = new ArrayList<>();
                    } else {
                        mTimelinePhaseListItems.clear();
                    }

                    if (mTimelineAdHocListItems == null) {
                        mTimelineAdHocListItems = new ArrayList<>();
                    } else {
                        mTimelineAdHocListItems.clear();
                    }

                    if (taskModelList != null) {
                        List<TaskModel> validTaskModelList = taskModelList.stream().
                                filter(taskModel -> EIsValid.YES.toString().
                                        equalsIgnoreCase(taskModel.getIsValid())).
                                collect(Collectors.toList());

                        List<TaskModel> timelinePlannedListItems = getTimelinePlannedListItems(validTaskModelList);
                        mTimelinePhaseListItems.addAll(timelinePlannedListItems);

                        List<TaskModel> timelineAdHocListItems = getTimelineAdHocListItems(validTaskModelList);
                        mTimelineAdHocListItems.addAll(timelineAdHocListItems);

                    }

                    if (mRecyclerAdapterPlanned != null) {
                        mRecyclerAdapterPlanned.setTimelineListItems(mTimelinePhaseListItems);
                    }

                    if (mRecyclerAdapterAdHoc != null) {
                        mRecyclerAdapterAdHoc.setTimelineListItems(mTimelineAdHocListItems);
                    }

                    refreshUI();
                }
            }
        });
    }

    /**
     * Pops back stack of ONLY current tab
     *
     * @return
     */
    public boolean popBackStack() {
        if (!isAdded())
            return false;

        if(getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
            return true;
        } else
            return false;
    }

    public void onVisible() {
        Timber.i("onVisible");
        mRecyclerAdapterAdHoc.notifyDataSetChanged();
        mRecyclerAdapterPlanned.notifyDataSetChanged();
    }

    private void onInvisible() {
        Timber.i("onInvisible");
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;

        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                Timber.i("setUserVisibleHint onVisible");
                onVisible();
            } else {
                Timber.i("setUserVisibleHint onInvisible");
                onInvisible();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }
}
