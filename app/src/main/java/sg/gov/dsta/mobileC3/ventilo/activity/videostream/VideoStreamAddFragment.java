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
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.VideoStreamViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;

public class VideoStreamAddFragment extends Fragment {

    private static final String TAG = "VideoStreamAddFragment";

    // View Models
    private UserViewModel mUserViewModel;
    private VideoStreamViewModel mVideoStreamViewModel;

    private LinearLayout mLinearLayoutBtnBack;
    private LinearLayout mLinearLayoutBtnDone;

    private RecyclerView mRecyclerView;
    private VideoStreamDeleteOrSaveRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private List<VideoStreamModel> mVideoStreamListItems;
    private C2OpenSansRegularEditTextView mEtvName;
    private C2OpenSansRegularEditTextView mEtvUrl;
    private LinearLayout mLinearLayoutAddVideoStream;

    private boolean mIsVisibleToUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootVideoStreamAddView = inflater.inflate(R.layout.fragment_add_video_stream, container, false);
        observerSetup();
        initUI(rootVideoStreamAddView);

        return rootVideoStreamAddView;
    }

    private void initUI(View rootView) {
        mLinearLayoutBtnBack = rootView.findViewById(R.id.layout_video_stream_btn_back);
        mLinearLayoutBtnBack.setOnClickListener(onBackClickListener);
        mLinearLayoutBtnDone = rootView.findViewById(R.id.layout_video_stream_btn_done);
        mLinearLayoutBtnDone.setOnClickListener(onDoneClickListener);

        mRecyclerView = rootView.findViewById(R.id.recycler_video_stream_add);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        // Set data for recycler view
        setUpRecyclerData();

        mRecyclerAdapter = new VideoStreamDeleteOrSaveRecyclerAdapter(getContext(), mVideoStreamListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(null);

        mLinearLayoutAddVideoStream = rootView.findViewById(R.id.layout_video_stream_add);
        mLinearLayoutAddVideoStream.setOnClickListener(addVideoStreamOnClickListener);
    }

    private View.OnClickListener onBackClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.popBackStack();
        }
    };

    private View.OnClickListener onDoneClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
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
                                        mEtvName = view.findViewById(R.id.etv_video_stream_name);
                                        mEtvUrl = view.findViewById(R.id.etv_video_stream_url);

                                        boolean isFieldEmpty = false;
                                        if (TextUtils.isEmpty(mEtvName.getText().toString().trim())) {
                                            isFieldEmpty = true;
                                            mEtvName.setError(getString(R.string.video_stream_error_field_required));
                                            mEtvName.requestFocus();
                                        }

                                        if (TextUtils.isEmpty(mEtvUrl.getText().toString().trim())) {

                                            mEtvUrl.setError(getString(R.string.video_stream_error_field_required));

                                            if (!isFieldEmpty) {
                                                mEtvUrl.requestFocus();
                                            }

                                            isFieldEmpty = true;
                                        }

                                        if (!isFieldEmpty) {
                                            mEtvUrl.setError(getString(R.string.video_stream_error_save_required));
                                            mEtvUrl.requestFocus();
                                        }

                                        isEmptyVideoStreamFound = true;
                                        break;
                                    }
                                }

                                if (!isEmptyVideoStreamFound) {
                                    addEmptyVideoStreamItem();
                                }
                            } else {
                                Log.d(TAG, "onSuccess singleObserverAllVideoStreamForUser, " +
                                        "addVideoStreamOnClickListener. " +
                                        "videoStreamModelLiveDataList is null");
                                addEmptyVideoStreamItem();
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

    private void addEmptyVideoStreamItem() {
        VideoStreamModel videoStreamModel = new VideoStreamModel();
        videoStreamModel.setUserId(getUserID());
        videoStreamModel.setName(SharedPreferenceConstants.DEFAULT_STRING);
        videoStreamModel.setUrl(SharedPreferenceConstants.DEFAULT_STRING);
        videoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_SAVE);
        mVideoStreamListItems.add(videoStreamModel);

        addItemToSqlDatabase(videoStreamModel);
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
//        Log.d(TAG, "getTotalNumberOfVideoStreams is " + getTotalNumberOfVideoStreams());
//
//        boolean isItemReplaced = false;
//        int posToDelete = 0;
//        int totalCountOfVideoStreams = getTotalNumberOfVideoStreams();
//
//        for (int i = 0; i < totalCountOfVideoStreams; i++) {
//            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//            String videoStreamInitials = SharedPreferenceConstants.HEADER_VIDEO_STREAM.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(String.valueOf(posToDelete));
//
//            String name = pref.getString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME), SharedPreferenceConstants.DEFAULT_STRING);
//            String url = pref.getString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
//                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL), SharedPreferenceConstants.DEFAULT_STRING);
//
//            if (SharedPreferenceConstants.DEFAULT_STRING.equalsIgnoreCase(name) ||
//                    SharedPreferenceConstants.DEFAULT_STRING.equalsIgnoreCase(url)) {
//                removeSingleItemFromLocalDatabase(posToDelete);
//                isItemReplaced = true;
//            }
//
//            if (!isItemReplaced) {
//                posToDelete++;
//            }
//
//            isItemReplaced = false;
//        }

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

        mVideoStreamViewModel.addVideoStream(videoStreamModel, singleObserverAddVideoStream);
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
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
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
