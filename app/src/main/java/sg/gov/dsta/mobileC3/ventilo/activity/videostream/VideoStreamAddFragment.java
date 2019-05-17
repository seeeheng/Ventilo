package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.VideoStreamViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;

public class VideoStreamAddFragment extends Fragment {

    private static final String TAG = "VideoStreamAddFragment";

    // View Models
    private VideoStreamViewModel mVideoStreamViewModel;

    private VideoStreamDeleteOrSaveRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private List<VideoStreamModel> mVideoStreamListItems;

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

        mRecyclerAdapter = new VideoStreamDeleteOrSaveRecyclerAdapter(getContext(), mVideoStreamListItems);
        recyclerView.setAdapter(mRecyclerAdapter);
        recyclerView.setItemAnimator(null);

        LinearLayout linearLayoutAddVideoStream = rootView.findViewById(R.id.layout_video_stream_add);
        linearLayoutAddVideoStream.setOnClickListener(addVideoStreamOnClickListener);
    }

    private View.OnClickListener onBackClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            removeInvalidAndGetVideoStreamItems();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    };

    private View.OnClickListener onDoneClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            removeInvalidAndGetVideoStreamItems();
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
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

//        for (int i = 0; i < getTotalNumberOfVideoStreams(); i++) {
//            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String videoStreamInitials = SharedPreferenceConstants.HEADER_VIDEO_STREAM.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(String.valueOf(i));
//
//            VideoStreamModel videoStreamModel = new VideoStreamModel();
//            videoStreamModel.setId(pref.getInt(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ID), SharedPreferenceConstants.DEFAULT_INT));
//            videoStreamModel.setName(pref.getString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME), SharedPreferenceConstants.DEFAULT_STRING));
//            videoStreamModel.setUrl(pref.getString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL), SharedPreferenceConstants.DEFAULT_STRING));
//            videoStreamModel.setIconType(pref.getString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ICON_TYPE), FragmentConstants.KEY_VIDEO_STREAM_SAVE));
//
//            mVideoStreamListItems.add(videoStreamModel);
//        }
    }

    /*
     * Obtain total number of video streams
     */
//    private int getTotalNumberOfVideoStreams() {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        int totalNumberOfVideoStreams = pref.getInt(SharedPreferenceConstants.VIDEO_STREAM_TOTAL_NUMBER, 0);
//
//        return totalNumberOfVideoStreams;
//    }

    /*
     * Add single video stream with respective fields
     */
//    private void addSingleItemToLocalDatabase(SharedPreferences.Editor editor, int id, String name, String url, String iconType) {
//        String videoStreamInitials = SharedPreferenceConstants.HEADER_VIDEO_STREAM.
//                concat(SharedPreferenceConstants.SEPARATOR).concat(String.valueOf(id));
//
//        editor.putInt(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ID), id);
//        editor.putString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME), name);
//        editor.putString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL), url);
//        editor.putString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ICON_TYPE), iconType);
//        editor.putInt(SharedPreferenceConstants.VIDEO_STREAM_TOTAL_NUMBER,
//                getTotalNumberOfVideoStreams() + 1);
//
//        editor.apply();
//
//        mRecyclerAdapter.addItem(name, url, iconType);
//    }

//    private void removeSingleItemFromLocalDatabase(int position) {
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        SharedPreferences.Editor editor = pref.edit();
//
//        // Overwrite all fields of specified video stream item with next stored item fields
//        // E.g. id(2) is replaced with id(3)
//        boolean isReplaced = false;
//        for (int i = position; i < getTotalNumberOfVideoStreams() - 1; i++) {
//            isReplaced = true;
//            String videoStreamInitialsOfCurrentPos = SharedPreferenceConstants.HEADER_VIDEO_STREAM.
//                    concat(SharedPreferenceConstants.SEPARATOR).concat(String.valueOf(i));
//
//            String videoStreamInitialsOfNextPos = SharedPreferenceConstants.HEADER_VIDEO_STREAM.
//                    concat(SharedPreferenceConstants.SEPARATOR).concat(String.valueOf(i + 1));
//
//            editor.putInt(videoStreamInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ID),
//                    pref.getInt(videoStreamInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ID), SharedPreferenceConstants.DEFAULT_INT));
//            editor.putString(videoStreamInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME),
//                    pref.getString(videoStreamInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME), SharedPreferenceConstants.DEFAULT_STRING));
//            editor.putString(videoStreamInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL),
//                    pref.getString(videoStreamInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL), SharedPreferenceConstants.DEFAULT_STRING));
//            editor.putString(videoStreamInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ICON_TYPE),
//                    pref.getString(videoStreamInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
//                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ICON_TYPE), SharedPreferenceConstants.DEFAULT_STRING));
//        }
//
//        // Safely remove all fields of video stream item at the back of the queue
//        int newPosToDelete;
//
//        if (isReplaced) {
//            newPosToDelete = getTotalNumberOfVideoStreams() - 1;
//        } else {
//            newPosToDelete = position;
//        }
//
//        String videoStreamInitials = SharedPreferenceConstants.HEADER_VIDEO_STREAM.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(String.valueOf(newPosToDelete));
//
//        editor.remove(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ID));
//        editor.remove(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME));
//        editor.remove(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL));
//        editor.remove(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ICON_TYPE));
//
//        // Decrement total number of video stream items by one, and store it
//        editor.putInt(SharedPreferenceConstants.VIDEO_STREAM_TOTAL_NUMBER, getTotalNumberOfVideoStreams() - 1);
//
//        editor.apply();
//    }

//    private void onVisible() {
//        refreshUI();
//    }
//
//    private void onInvisible() {
//        removeInvalidVideoStreamItems();
//    }

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

//    private void attemptAddVideoStream() {
//        // Reset errors.
//        mEt.setError(null);
//        mEtvPassword.setError(null);
//
//        // Store values at the time of the login attempt.
//        String userId = mEtvUserId.getText().toString();
//        String password = mEtvPassword.getText().toString();
//
//        boolean cancel = false;
//        View focusView = null;
//
//        // Check for a valid password, if the user entered one.
//        if (TextUtils.isEmpty(password)) {
//            mEtvPassword.setError(getString(R.string.error_field_required));
//            focusView = mEtvPassword;
//            cancel = true;
//        }
//
//        // Check for a valid user Id.
//        if (TextUtils.isEmpty(userId)) {
//            mEtvUserId.setError(getString(R.string.error_field_required));
//            focusView = mEtvUserId;
//            cancel = true;
//        }
//
//        if (cancel) {
//            // There was an error; don't attempt login and focus the first
//            // form field with an error.
//            focusView.requestFocus();
//        } else {
//
//            // TODO: Add after design review
//            // Show a progress spinner, and kick off a background task to
//            // perform the user login attempt.
//
//            checkIfValidUser();
////            LoginTx loginTx = new LoginTx(mEtvUserId.getText().toString()
////                    , mPasswordET.getText().toString());
////            StringRequest stringRequest = VolleyPostBuilder.getRequest(RestConstants.BASE_URL + RestConstants.POST_LOGIN, loginTx, loginListener, errorListener, getApplicationContext());
////            QueueSingleton.getInstance(this).addToRequestQueue(stringRequest);
////            showProgress(true);
//        }
//    }

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

//    @Override
//    public void setUserVisibleHint(boolean isVisibleToUser) {
//        super.setUserVisibleHint(isVisibleToUser);
//        Log.d(TAG, "setUserVisibleHint is " + isVisibleToUser);
//        mIsVisibleToUser = isVisibleToUser;
//        if (isResumed()) { // fragment has been created at this point
//            if (mIsVisibleToUser) {
//                onVisible();
//            } else {
//                onInvisible();
//            }
//        }
//    }

    private void refreshUI() {
        setUpRecyclerData();
    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");


//        if (mIsVisibleToUser) {
//            onVisible();
//        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");

//        if (mIsVisibleToUser) {
//            onInvisible();
//        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");

//        if (mIsVisibleToUser) {
//            onInvisible();
//        }
    }
}
