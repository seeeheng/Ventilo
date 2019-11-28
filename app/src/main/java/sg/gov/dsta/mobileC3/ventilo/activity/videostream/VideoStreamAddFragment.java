package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.VideoStreamViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.videoStream.EOwner;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import timber.log.Timber;

public class VideoStreamAddFragment extends Fragment {

    private static final String TAG = "VideoStreamAddFragment";

    // View Models
    private VideoStreamViewModel mVideoStreamViewModel;

    // OWN Video Stream
    private ImageView mImgOwnVideoStreamDelete;
    private C2OpenSansRegularEditTextView mEtvOwnVideoStreamName;
    private C2OpenSansRegularEditTextView mEtvOwnVideoStreamURL;
    private ImageView mImgOwnVideoStreamEditOrSave;

    // Other Video Streams
    private C2OpenSansSemiBoldTextView mTvOtherVideoStream;
    private VideoStreamDeleteOrSaveRecyclerAdapter mRecyclerAdapterForOthers;
    private RecyclerView.LayoutManager mRecyclerLayoutManagerForOthers;

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
        tvToolbarDone.setText(MainApplication.getAppContext().getString(R.string.btn_done));

        initOwnVideoStreamUI(rootView);

        initOtherVideoStreamsUI(rootView);

        LinearLayout linearLayoutAddVideoStream = rootView.findViewById(R.id.layout_video_stream_add);
        linearLayoutAddVideoStream.setOnClickListener(addOtherVideoStreamOnClickListener);
    }

    /**
     * Initialise UI for adding of own video stream
     *
     * @param rootView
     */
    private void initOwnVideoStreamUI(View rootView) {
        View layoutOwnVideoStreamAdd = rootView.findViewById(R.id.layout_own_video_stream_add);

        mImgOwnVideoStreamDelete = layoutOwnVideoStreamAdd.
                findViewById(R.id.img_video_stream_delete);
        mEtvOwnVideoStreamName = layoutOwnVideoStreamAdd.
                findViewById(R.id.etv_video_stream_name);
        mEtvOwnVideoStreamURL = layoutOwnVideoStreamAdd.
                findViewById(R.id.etv_video_stream_url);
        mImgOwnVideoStreamEditOrSave = layoutOwnVideoStreamAdd.
                findViewById(R.id.img_video_stream_edit_save);

        mImgOwnVideoStreamDelete.setOnClickListener(onDeleteOwnVideoStreamItemClickListener);
        mImgOwnVideoStreamEditOrSave.setOnClickListener(onEditOrSaveOwnVideoStreamItemClickListener);
    }

    /**
     * Initialise UI for adding of video streams belonging to others
     *
     * @param rootView
     */
    private void initOtherVideoStreamsUI(View rootView) {
        mTvOtherVideoStream = rootView.findViewById(R.id.tv_other_video_streams);

        RecyclerView recyclerViewForOthers = rootView.findViewById(R.id.recycler_other_video_stream_add);
        recyclerViewForOthers.setHasFixedSize(false);

        mRecyclerLayoutManagerForOthers = new LinearLayoutManager(getActivity());
        recyclerViewForOthers.setLayoutManager(mRecyclerLayoutManagerForOthers);

        // Set data for recycler view for others
        setUpRecyclerDataForOthers();

        mRecyclerAdapterForOthers = new VideoStreamDeleteOrSaveRecyclerAdapter(getContext(), this, mVideoStreamListItems);
        recyclerViewForOthers.setAdapter(mRecyclerAdapterForOthers);
        recyclerViewForOthers.setItemAnimator(null);
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

    // -------------------- OWN Video Stream onClick Listeners -------------------- //
    private View.OnClickListener onDeleteOwnVideoStreamItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            SingleObserver<List<VideoStreamModel>> singleObserverDeleteOwnVideoStream =
                    new SingleObserver<List<VideoStreamModel>>() {
                        @Override
                        public void onSubscribe(Disposable d) {
                            // add it to a CompositeDisposable
                        }

                        @Override
                        public void onSuccess(List<VideoStreamModel> videoStreamModelList) {

                            Timber.i("onSuccess singleObserverDeleteOwnVideoStream, onDeleteOwnVideoStreamItemClickListener. videoStreamModelList.size(): %d" , videoStreamModelList.size());


                            List<VideoStreamModel> ownVideoStreamModelList = videoStreamModelList.stream().
                                    filter(videoStreamModel -> EOwner.OWN.toString().
                                            equalsIgnoreCase(videoStreamModel.getOwner())).collect(Collectors.toList());

                            // There should only be ONE 'Self' video stream
                            if (ownVideoStreamModelList.size() == 1) {
                                VideoStreamModel ownVideoStreamModel = ownVideoStreamModelList.get(0);
                                mVideoStreamViewModel.deleteVideoStream(ownVideoStreamModel.getId());

                                mEtvOwnVideoStreamName.setText(StringUtil.EMPTY_STRING);
                                mEtvOwnVideoStreamURL.setText(StringUtil.EMPTY_STRING);
                                mEtvOwnVideoStreamName.setEnabled(true);
                                mEtvOwnVideoStreamURL.setEnabled(true);
                                mImgOwnVideoStreamEditOrSave.setImageDrawable(
                                        view.getContext().getDrawable(R.drawable.btn_save));
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            Timber.i("onError singleObserverDeleteOwnVideoStream, onDeleteOwnVideoStreamItemClickListener. Error Msg: %s" , e.toString());


                        }
                    };

            mVideoStreamViewModel.getAllVideoStreamsForUser(
                    SharedPreferenceUtil.getCurrentUserCallsignID(),
                    singleObserverDeleteOwnVideoStream);
        }
    };

    private View.OnClickListener onEditOrSaveOwnVideoStreamItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean isFieldEmpty = false;

            if (TextUtils.isEmpty(mEtvOwnVideoStreamName.getText().toString().trim())) {
                isFieldEmpty = true;
                mEtvOwnVideoStreamName.setError(view.getContext().getString(R.string.video_stream_error_field_required));
                mEtvOwnVideoStreamName.requestFocus();
            }

            if (TextUtils.isEmpty(mEtvOwnVideoStreamURL.getText().toString().trim())) {
                mEtvOwnVideoStreamURL.setError(view.getContext().getString(R.string.video_stream_error_field_required));

                if (!isFieldEmpty) {
                    mEtvOwnVideoStreamURL.requestFocus();
                }

                isFieldEmpty = true;
            }

            if (!isFieldEmpty) {
                mEtvOwnVideoStreamName.clearFocus();
                mEtvOwnVideoStreamURL.clearFocus();

                SingleObserver<List<VideoStreamModel>> singleObserverEditOrSaveOwnVideoStream =
                        new SingleObserver<List<VideoStreamModel>>() {
                            @Override
                            public void onSubscribe(Disposable d) {
                                // add it to a CompositeDisposable
                            }

                            @Override
                            public void onSuccess(List<VideoStreamModel> videoStreamModelList) {

                                Timber.i("onSuccess singleObserverEditOrSaveOwnVideoStream, onEditOrSaveOwnVideoStreamItemClickListener. videoStreamModelList.size(): %d" , videoStreamModelList.size());

                                List<VideoStreamModel> ownVideoStreamModelList = videoStreamModelList.stream().
                                        filter(videoStreamModel -> EOwner.OWN.toString().
                                                equalsIgnoreCase(videoStreamModel.getOwner())).collect(Collectors.toList());

                                if (ownVideoStreamModelList.size() == 0) {
                                    // Save new 'Self' (OWN) video stream
                                    mEtvOwnVideoStreamName.setEnabled(false);
                                    mEtvOwnVideoStreamURL.setEnabled(false);
                                    mImgOwnVideoStreamEditOrSave.setImageDrawable(view.getContext().getDrawable(R.drawable.btn_edit));

                                    addVideoStreamItem(mEtvOwnVideoStreamName.getText().toString().trim(),
                                            mEtvOwnVideoStreamURL.getText().toString().trim(),
                                            EOwner.OWN.toString());

                                } else if (ownVideoStreamModelList.size() == 1) {
                                    // If not empty, there should only be ONE 'Self' video stream in the list
                                    VideoStreamModel ownVideoStreamModel = ownVideoStreamModelList.get(0);

                                    if (ownVideoStreamModel.getIconType().equalsIgnoreCase(FragmentConstants.KEY_VIDEO_STREAM_EDIT)) {
                                        mEtvOwnVideoStreamName.setEnabled(true);
                                        mEtvOwnVideoStreamURL.setEnabled(true);
                                        mImgOwnVideoStreamEditOrSave.setImageDrawable(view.getContext().getDrawable(R.drawable.btn_save));

                                        ownVideoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_SAVE);
                                    } else {
                                        mEtvOwnVideoStreamName.setEnabled(false);
                                        mEtvOwnVideoStreamURL.setEnabled(false);
                                        mImgOwnVideoStreamEditOrSave.setImageDrawable(view.getContext().getDrawable(R.drawable.btn_edit));

                                        ownVideoStreamModel.setName(mEtvOwnVideoStreamName.getText().toString().trim());
                                        ownVideoStreamModel.setUrl(mEtvOwnVideoStreamURL.getText().toString().trim());
                                        ownVideoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_EDIT);
                                    }

                                    mVideoStreamViewModel.updateVideoStream(ownVideoStreamModel);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.i("onError singleObserverEditOrSaveOwnVideoStream, onEditOrSaveOwnVideoStreamItemClickListener. Error Msg: %S" , e.toString());
                            }
                        };

                mVideoStreamViewModel.getAllVideoStreamsForUser(
                        SharedPreferenceUtil.getCurrentUserCallsignID(),
                        singleObserverEditOrSaveOwnVideoStream);
            }
        }
    };

    // -------------------- Other Video Streams onClick Listeners -------------------- //
    private OnClickListener addOtherVideoStreamOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            addOtherVideoStream();
        }
    };

    private synchronized void addOtherVideoStream() {
        // Checks for empty entries and displays hint messages of them to be filled up and saved
        SingleObserver<List<VideoStreamModel>> singleObserverAllVideoStreamForUser =
                new SingleObserver<List<VideoStreamModel>>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(List<VideoStreamModel> videoStreamModelList) {

                        List<VideoStreamModel> otherVideoStreamModelList = null;

                        if (videoStreamModelList != null) {
                            otherVideoStreamModelList = videoStreamModelList.stream().
                                    filter(videoStreamModel -> EOwner.OTHERS.toString().
                                            equalsIgnoreCase(videoStreamModel.getOwner())).collect(Collectors.toList());
                        }

                        /**
                         * If user's video stream list is empty, allow new entry to be filled.
                         * Else, check if existing fields (name & URL) of entry are empty.
                         * If fields are empty, request for user to fill up details of existing
                         * entry and save it first before allowing for another new entry.
                         */
                        if (otherVideoStreamModelList != null) {

                            Timber.i("onSuccess singleObserverAllVideoStreamForUser, addOtherVideoStreamOnClickListener. videoStreamModelList.size(): %d" , videoStreamModelList.size());


                            boolean isEmptyVideoStreamFound = false;
                            for (int i = 0; i < otherVideoStreamModelList.size(); i++) {
                                VideoStreamModel currentVideoStreamModel = otherVideoStreamModelList.get(i);

                                if (TextUtils.isEmpty(currentVideoStreamModel.getName().trim()) ||
                                        TextUtils.isEmpty(currentVideoStreamModel.getUrl().trim())) {

                                    View view = mRecyclerLayoutManagerForOthers.findViewByPosition(i);
                                    C2OpenSansRegularEditTextView etvName = view.findViewById(R.id.etv_video_stream_name);
                                    C2OpenSansRegularEditTextView etvUrl = view.findViewById(R.id.etv_video_stream_url);

                                    boolean isFieldEmpty = false;
                                    if (TextUtils.isEmpty(etvName.getText().toString().trim())) {
                                        isFieldEmpty = true;
                                        etvName.setError(MainApplication.getAppContext().
                                                getString(R.string.video_stream_error_field_required));
                                        etvName.requestFocus();
                                    }

                                    if (TextUtils.isEmpty(etvUrl.getText().toString().trim())) {

                                        etvUrl.setError(MainApplication.getAppContext().
                                                getString(R.string.video_stream_error_field_required));

                                        if (!isFieldEmpty) {
                                            etvUrl.requestFocus();
                                        }

                                        isFieldEmpty = true;
                                    }

                                    if (!isFieldEmpty) {
                                        etvUrl.setError(MainApplication.getAppContext().
                                                getString(R.string.video_stream_error_save_required));
                                        etvUrl.requestFocus();
                                    }

                                    isEmptyVideoStreamFound = true;
                                    break;
                                }
                            }

                            if (!isEmptyVideoStreamFound) {
                                addVideoStreamItem(EOwner.OTHERS.toString());
                            }
                        } else {
                            Timber.i("onSuccess singleObserverAllVideoStreamForUser, addOtherVideoStreamOnClickListener. videoStreamModelList is null");

                            addVideoStreamItem(EOwner.OTHERS.toString());
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        Timber.e("onError singleObserverAllVideoStreamForUser, addOtherVideoStreamOnClickListener. Error Msg: %s" , e.toString());

                    }
                };

        mVideoStreamViewModel.getAllVideoStreamsForUser(SharedPreferenceUtil.getCurrentUserCallsignID(),
                singleObserverAllVideoStreamForUser);
    }

    private void addVideoStreamItem(String name, String url, String owner) {
        VideoStreamModel videoStreamModel = new VideoStreamModel();
        videoStreamModel.setUserId(SharedPreferenceUtil.getCurrentUserCallsignID());

        if (StringUtil.EMPTY_STRING.equalsIgnoreCase(name) &&
                StringUtil.EMPTY_STRING.equalsIgnoreCase(url)) {
            videoStreamModel.setName(SharedPreferenceConstants.DEFAULT_STRING);
            videoStreamModel.setUrl(SharedPreferenceConstants.DEFAULT_STRING);
            videoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_SAVE);
        } else {
            videoStreamModel.setName(name);
            videoStreamModel.setUrl(url);
            videoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_EDIT);
        }

        videoStreamModel.setOwner(owner);

        mVideoStreamListItems.add(videoStreamModel);
        addItemToSqlDatabase(videoStreamModel);

        Timber.i("Video stream item added to database.");

    }

    private void addVideoStreamItem(String owner) {
        addVideoStreamItem(StringUtil.EMPTY_STRING, StringUtil.EMPTY_STRING, owner);
    }

    private void setUpRecyclerDataForOthers() {
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

                            Timber.i("onSuccess singleObserverAllVideoStreamForUser, removeInvalidVideoStreamItems. VideoStreamId.size(): %d" , videoStreamModelList.size());



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
                        Timber.e("onError singleObserverAllVideoStreamForUser, removeInvalidVideoStreamItems. Error Msg:  %s" , e.toString());

                    }
                };

        mVideoStreamViewModel.getAllVideoStreamsForUser(SharedPreferenceUtil.getCurrentUserCallsignID(),
                singleObserverAllVideoStreamForUser);
    }

    private void addItemToSqlDatabase(VideoStreamModel videoStreamModel) {
        SingleObserver<Long> singleObserverAddVideoStream = new SingleObserver<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                // add it to a CompositeDisposable
            }

            @Override
            public void onSuccess(Long videoStreamId) {

                Timber.i("onSuccess singleObserverAddVideoStream, addItemToSqlDatabase. VideoStreamId: %d" , videoStreamId);

                mVideoStreamListItems.get(mVideoStreamListItems.size() - 1).setId(videoStreamId);
            }

            @Override
            public void onError(Throwable e) {

                Timber.e("onError singleObserverAddVideoStream, addItemToSqlDatabase. Error Msg: %s" , e.toString());

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

        Timber.i("onVisible");


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

        Timber.i("onInvisible");

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

                        // OWN Video Stream
                        List<VideoStreamModel> ownVideoStreamModelList = videoStreamModelList.stream().
                                filter(videoStreamModel -> EOwner.OWN.toString().
                                        equalsIgnoreCase(videoStreamModel.getOwner())).collect(Collectors.toList());

                        // There should only be ONE 'Self' video stream
                        if (ownVideoStreamModelList.size() == 1) {
                            VideoStreamModel ownVideoStreamModel = ownVideoStreamModelList.get(0);

                            mEtvOwnVideoStreamName.setText(ownVideoStreamModel.getName());
                            mEtvOwnVideoStreamURL.setText(ownVideoStreamModel.getUrl());

                            if (FragmentConstants.KEY_VIDEO_STREAM_EDIT.
                                    equalsIgnoreCase(ownVideoStreamModel.getIconType())) {
                                mEtvOwnVideoStreamName.setEnabled(false);
                                mEtvOwnVideoStreamURL.setEnabled(false);
                                mImgOwnVideoStreamEditOrSave.setImageDrawable(
                                        getContext().getDrawable(R.drawable.btn_edit));
                            } else {
                                mEtvOwnVideoStreamName.setEnabled(true);
                                mEtvOwnVideoStreamURL.setEnabled(true);
                                mImgOwnVideoStreamEditOrSave.setImageDrawable(
                                        getContext().getDrawable(R.drawable.btn_save));
                            }
                        }

                        // Other Video Streams
                        List<VideoStreamModel> otherVideoStreamModelList = videoStreamModelList.
                                stream().filter(videoStreamModel -> EOwner.OTHERS.toString().
                                equalsIgnoreCase(videoStreamModel.getOwner())).
                                collect(Collectors.toList());

                        if (otherVideoStreamModelList.size() > 0) {
                            mTvOtherVideoStream.setVisibility(View.VISIBLE);
                        } else {
                            mTvOtherVideoStream.setVisibility(View.GONE);
                        }

                        mRecyclerAdapterForOthers.setVideoStreamListItems(
                                otherVideoStreamModelList);
                    }
                });
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        Timber.i("setUserVisibleHint is %b ", isVisibleToUser);
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

        Timber.i("onStart");

        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Timber.i("onStop");

        if (mIsFragmentVisibleToUser) {
            onInvisible();
        }
    }
}
