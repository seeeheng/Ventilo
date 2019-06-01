package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainStatePagerAdapter;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.VideoStreamViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;

public class VideoStreamAddFragment extends Fragment {

    private static final String TAG = "VideoStreamAddFragment";

    // View Models
    private VideoStreamViewModel mVideoStreamViewModel;

    private VideoStreamDeleteOrSaveRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private List<VideoStreamModel> mVideoStreamListItems;

    private boolean mIsFragmentVisibleToUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootVideoStreamAddView = inflater.inflate(R.layout.fragment_add_video_stream, container, false);
        observerSetup();
        initUI(rootVideoStreamAddView);

        return rootVideoStreamAddView;
    }

    private void initUI(View rootView) {
        View layoutToolbar = rootView.findViewById(R.id.layout_toolbar_video_stream_text_left_text_right);
        layoutToolbar.setClickable(true);

        LinearLayout linearLayoutBtnBack = layoutToolbar.findViewById(R.id.layout_toolbar_top_left_btn);
        linearLayoutBtnBack.setOnClickListener(onBackClickListener);
        LinearLayout linearLayoutBtnDone = layoutToolbar.findViewById(R.id.layout_toolbar_top_right_btn);
        linearLayoutBtnDone.setOnClickListener(onDoneClickListener);

        C2OpenSansSemiBoldTextView tvToolbarDone = layoutToolbar.findViewById(R.id.toolbar_top_right_btn_text);
        tvToolbarDone.setText(getString(R.string.btn_done));

        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_video_stream_add);
        recyclerView.setHasFixedSize(true);

        mRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mRecyclerLayoutManager);

        // Set data for recycler view
        setUpRecyclerData();

        mRecyclerAdapter = new VideoStreamDeleteOrSaveRecyclerAdapter(getContext(), this, mVideoStreamListItems);
        recyclerView.setAdapter(mRecyclerAdapter);
        recyclerView.setItemAnimator(null);

        LinearLayout linearLayoutAddVideoStream = rootView.findViewById(R.id.layout_video_stream_add);
        linearLayoutAddVideoStream.setOnClickListener(addVideoStreamOnClickListener);
    }

    private View.OnClickListener onBackClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            removeInvalidAndGetVideoStreamItems();
            popChildBackStack();
        }
    };

    private View.OnClickListener onDoneClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            removeInvalidAndGetVideoStreamItems();
            popChildBackStack();
        }
    };

    private OnClickListener addVideoStreamOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            // Checks for empty entries and displays hint messages of them to be filled up and saved
            SingleObserver<List<VideoStreamModel>> singleObserverAllVideoStreamForUser =
                    new SingleObserver<List<VideoStreamModel>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            // add it to a CompositeDisposable
                        }

                        @Override
                        public void onSuccess(List<VideoStreamModel> videoStreamModelList) {

                            /**
                             * If user's video stream list is empty, allow new entry to be filled.
                             * Else, check if existing fields (name & URL) of entry are empty.
                             * If fields are empty, request for user to fill up details of existing
                             * entry and save it first before allowing for another new entry.
                             */
                            if (videoStreamModelList != null) {
                                Log.d(TAG, "onSuccess singleObserverAllVideoStreamForUser, " +
                                        "addVideoStreamOnClickListener. " +
                                        "VideoStreamId.size(): " + videoStreamModelList.size());

                                boolean isEmptyVideoStreamFound = false;
                                for (int i = 0; i < videoStreamModelList.size(); i++) {
                                    VideoStreamModel currentVideoStreamModel = videoStreamModelList.get(i);

                                    if (TextUtils.isEmpty(currentVideoStreamModel.getName().trim()) ||
                                            TextUtils.isEmpty(currentVideoStreamModel.getUrl().trim())) {

                                        View view = mRecyclerLayoutManager.findViewByPosition(i);
                                        C2OpenSansRegularEditTextView etvName = view.findViewById(R.id.etv_video_stream_name);
                                        C2OpenSansRegularEditTextView etvUrl = view.findViewById(R.id.etv_video_stream_url);

                                        boolean isFieldEmpty = false;
                                        if (TextUtils.isEmpty(etvName.getText().toString().trim())) {
                                            isFieldEmpty = true;
                                            etvName.setError(getString(R.string.video_stream_error_field_required));
                                            etvName.requestFocus();
                                        }

                                        if (TextUtils.isEmpty(etvUrl.getText().toString().trim())) {

                                            etvUrl.setError(getString(R.string.video_stream_error_field_required));

                                            if (!isFieldEmpty) {
                                                etvUrl.requestFocus();
                                            }

                                            isFieldEmpty = true;
                                        }

                                        if (!isFieldEmpty) {
                                            etvUrl.setError(getString(R.string.video_stream_error_save_required));
                                            etvUrl.requestFocus();
                                        }

                                        isEmptyVideoStreamFound = true;
                                        break;
                                    }
                                }

                                if (!isEmptyVideoStreamFound) {
                                    addVideoStreamItem();
                                }
                            } else {
                                Log.d(TAG, "onSuccess singleObserverAllVideoStreamForUser, " +
                                        "addVideoStreamOnClickListener. " +
                                        "videoStreamModelLiveDataList is null");
                                addVideoStreamItem();
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "onError singleObserverAllVideoStreamForUser, " +
                                    "addVideoStreamOnClickListener. " +
                                    "Error Msg: " + e.toString());
                        }
                    };

            mVideoStreamViewModel.getAllVideoStreamsForUser(getUserID(), singleObserverAllVideoStreamForUser);
        }
    };

    private void addVideoStreamItem() {
        VideoStreamModel videoStreamModel = new VideoStreamModel();
        videoStreamModel.setUserId(getUserID());
        videoStreamModel.setName(SharedPreferenceConstants.DEFAULT_STRING);
        videoStreamModel.setUrl(SharedPreferenceConstants.DEFAULT_STRING);
        videoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_SAVE);
        mVideoStreamListItems.add(videoStreamModel);

        addItemToSqlDatabase(videoStreamModel);

        Log.d(TAG, "Video stream item added to database.");
    }

    private String getUserID() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = pref.getString(SharedPreferenceConstants.USER_ID,
                SharedPreferenceConstants.DEFAULT_STRING);
        return userId;
    }

    private void setUpRecyclerData() {
        if (mVideoStreamListItems == null) {
            mVideoStreamListItems = new ArrayList<>();
        }

        removeInvalidAndGetVideoStreamItems();
    }

    /*
     * Remove invalid video stream items when navigate to other fragments
     * Invalid means either name or url is empty
     */
    private void removeInvalidAndGetVideoStreamItems() {
        SingleObserver<List<VideoStreamModel>> singleObserverAllVideoStreamForUser =
                new SingleObserver<List<VideoStreamModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<VideoStreamModel> videoStreamModelList) {
                        if (videoStreamModelList != null) {
                            Log.d(TAG, "onSuccess singleObserverAllVideoStreamForUser, " +
                                    "removeInvalidVideoStreamItems. " +
                                    "VideoStreamId.size(): " + videoStreamModelList.size());

                            for (int i = 0; i < videoStreamModelList.size(); i++) {
                                VideoStreamModel currentVideoStreamModel = videoStreamModelList.get(i);

                                if (TextUtils.isEmpty(currentVideoStreamModel.getName().trim()) ||
                                        TextUtils.isEmpty(currentVideoStreamModel.getUrl().trim())) {
                                    mVideoStreamViewModel.deleteVideoStream(currentVideoStreamModel.getId());
                                }
                            }

                            mVideoStreamListItems = videoStreamModelList;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverAllVideoStreamForUser, " +
                                "removeInvalidVideoStreamItems. " +
                                "Error Msg: " + e.toString());
                    }
                };

        mVideoStreamViewModel.getAllVideoStreamsForUser(getUserID(), singleObserverAllVideoStreamForUser);
    }

    private void addItemToSqlDatabase(VideoStreamModel videoStreamModel) {
        SingleObserver<Long> singleObserverAddVideoStream = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(Long videoStreamId) {
                Log.d(TAG, "onSuccess singleObserverAddVideoStream, " +
                        "addItemToSqlDatabase. " +
                        "VideoStreamId: " + videoStreamId);
                mVideoStreamListItems.get(mVideoStreamListItems.size() - 1).setId(videoStreamId);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "onError singleObserverAddVideoStream, " +
                        "addItemToSqlDatabase. " +
                        "Error Msg: " + e.toString());
            }
        };

        mVideoStreamViewModel.insertVideoStream(videoStreamModel, singleObserverAddVideoStream);
    }

    /**
     * Accesses child base fragment of current selected view pager item and remove this fragment
     * from child base fragment's stack.
     *
     * Selected View Pager Item: Video Stream
     * Child Base Fragment: VideoStreamFragment
     */
    private void popChildBackStack() {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = ((MainActivity) getActivity());
            mainActivity.popChildFragmentBackStack(
                    MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID);
        }
    }

    private void onVisible() {
        Log.d(TAG, "onVisible");

//        FragmentManager fm = getActivity().getSupportFragmentManager();
//        boolean isFragmentFound = false;
//
//        int count = fm.getBackStackEntryCount();
//
//        // Checks if current fragment exists in Back stack
//        for (int i = 0; i < count; i++) {
//            if (this.getClass().getSimpleName().equalsIgnoreCase(fm.getBackStackEntryAt(i).getName())) {
//                isFragmentFound = true;
//            }
//        }
//
//        // If not found, add to current fragment to Back stack
//        if (!isFragmentFound) {
//            FragmentTransaction ft = fm.beginTransaction();
//            ft.addToBackStack(this.getClass().getSimpleName());
//            ft.commit();
//        }
    }

    private void onInvisible() {
        Log.d(TAG, "onInvisible");
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mVideoStreamViewModel = ViewModelProviders.of(this).get(VideoStreamViewModel.class);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = sharedPrefs.getString(SharedPreferenceConstants.USER_ID,
                SharedPreferenceConstants.DEFAULT_STRING);

        /*
         * Refreshes recyclerview UI whenever there is a change in video streams (insert, update or delete)
         */
        mVideoStreamViewModel.getAllVideoStreamsLiveDataForUser(userId).observe(this,
                new Observer<List<VideoStreamModel>>() {
                    @Override
                    public void onChanged(@Nullable List<VideoStreamModel> videoStreamModelList) {
                        mRecyclerAdapter.setVideoStreamListItems(videoStreamModelList);
                    }
                });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint is " + isVisibleToUser);
        mIsFragmentVisibleToUser = isVisibleToUser;
        if (isResumed()) { // fragment has been created at this point
            if (mIsFragmentVisibleToUser) {
                onVisible();
            } else {
                onInvisible();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");

        if (mIsFragmentVisibleToUser) {
            onInvisible();
        }
    }
}
