package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamItemModel;
import sg.gov.dsta.mobileC3.ventilo.util.DateTimeUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;

public class VideoStreamAddFragment extends Fragment {

    private static final String TAG = "VideoStreamAddFragment";

    private RecyclerView mRecyclerView;
    private VideoStreamDeleteOrSaveRecyclerAdapter mRecyclerAdapter;
    private RecyclerView.LayoutManager mRecyclerLayoutManager;

    private List<VideoStreamItemModel> mVideoStreamListItems;
    private LinearLayout mLinearLayoutAddVideoStream;

    private boolean mIsVisibleToUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootVideoStreamAddView = inflater.inflate(R.layout.fragment_add_video_stream, container, false);
        initUI(rootVideoStreamAddView);

        return rootVideoStreamAddView;
    }

    private void initUI(View rootView) {
        mRecyclerView = rootView.findViewById(R.id.recycler_video_stream_add);
        mRecyclerView.setHasFixedSize(true);

        mRecyclerLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mRecyclerLayoutManager);

        // Set data for recycler view
        setUpRecyclerData();

        mRecyclerAdapter = new VideoStreamDeleteOrSaveRecyclerAdapter(getContext(), mVideoStreamListItems);
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mLinearLayoutAddVideoStream = rootView.findViewById(R.id.layout_video_stream_add);
        mLinearLayoutAddVideoStream.setOnClickListener(addVideoStreamOnClickListener);
    }

    private void setUpRecyclerData() {
        if (mVideoStreamListItems == null) {
            mVideoStreamListItems = new ArrayList<>();
        }

        for (int i = 0; i < getTotalNumberOfVideoStreams(); i++) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String videoStreamInitials = SharedPreferenceConstants.HEADER_VIDEO_STREAM.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(String.valueOf(i));

            VideoStreamItemModel videoStreamItemModel = new VideoStreamItemModel();
            videoStreamItemModel.setId(pref.getInt(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ID), SharedPreferenceConstants.DEFAULT_INT));
            videoStreamItemModel.setName(pref.getString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME), SharedPreferenceConstants.DEFAULT_STRING));
            videoStreamItemModel.setUrl(pref.getString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL), SharedPreferenceConstants.DEFAULT_STRING));
            videoStreamItemModel.setIconType(pref.getString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ICON_TYPE), FragmentConstants.KEY_VIDEO_STREAM_SAVE));

            mVideoStreamListItems.add(videoStreamItemModel);
        }
    }

    /*
     * Obtain total number of video streams
     */
    private int getTotalNumberOfVideoStreams() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int totalNumberOfVideoStreams = pref.getInt(SharedPreferenceConstants.VIDEO_STREAM_TOTAL_NUMBER, 0);

        return totalNumberOfVideoStreams;
    }

    /*
     * Add single video stream with respective fields
     */
    private void addSingleItemToLocalDatabase(SharedPreferences.Editor editor, int id, String name, String url, String iconType) {
        String videoStreamInitials = SharedPreferenceConstants.HEADER_VIDEO_STREAM.
                concat(SharedPreferenceConstants.SEPARATOR).concat(String.valueOf(id));

        editor.putInt(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ID), id);
        editor.putString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME), name);
        editor.putString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL), url);
        editor.putString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ICON_TYPE), iconType);
        editor.putInt(SharedPreferenceConstants.VIDEO_STREAM_TOTAL_NUMBER,
                getTotalNumberOfVideoStreams() + 1);

        editor.apply();

        mRecyclerAdapter.addItem(name, url, iconType);
    }

    private void removeSingleItemFromLocalDatabase(int position) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = pref.edit();

        // Overwrite all fields of specified video stream item with next stored item fields
        // E.g. id(2) is replaced with id(3)
        for (int i = position; i < getTotalNumberOfVideoStreams() - 1; i++) {
            String videoStreamInitialsOfCurrentPos = SharedPreferenceConstants.HEADER_VIDEO_STREAM.
                    concat(SharedPreferenceConstants.SEPARATOR).concat(String.valueOf(i));

            String videoStreamInitialsOfNextPos = SharedPreferenceConstants.HEADER_VIDEO_STREAM.
                    concat(SharedPreferenceConstants.SEPARATOR).concat(String.valueOf(i + 1));

            editor.putInt(videoStreamInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ID),
                    pref.getInt(videoStreamInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ID), SharedPreferenceConstants.DEFAULT_INT));
            editor.putString(videoStreamInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME),
                    pref.getString(videoStreamInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME), SharedPreferenceConstants.DEFAULT_STRING));
            editor.putString(videoStreamInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL),
                    pref.getString(videoStreamInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL), SharedPreferenceConstants.DEFAULT_STRING));
            editor.putString(videoStreamInitialsOfCurrentPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ICON_TYPE),
                    pref.getString(videoStreamInitialsOfNextPos.concat(SharedPreferenceConstants.SEPARATOR).
                            concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ICON_TYPE), SharedPreferenceConstants.DEFAULT_STRING));
        }

        // Safely remove all fields of video stream item at the back of the queue
        String videoStreamInitials = SharedPreferenceConstants.HEADER_VIDEO_STREAM.concat(SharedPreferenceConstants.SEPARATOR).
                concat(String.valueOf(getTotalNumberOfVideoStreams()));

        editor.remove(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ID));
        editor.remove(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME));
        editor.remove(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL));
        editor.remove(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_ICON_TYPE));

        // Decrement total number of video stream items by one, and store it
        editor.putInt(SharedPreferenceConstants.VIDEO_STREAM_TOTAL_NUMBER, getTotalNumberOfVideoStreams() - 1);

        editor.apply();
    }

    private OnClickListener addVideoStreamOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor editor = pref.edit();

            int totalNumberOfVideoStream = pref.getInt(SharedPreferenceConstants.VIDEO_STREAM_TOTAL_NUMBER, 0);
//            editor.putInt(SharedPreferenceConstants.VIDEO_STREAM_TOTAL_NUMBER, totalNumberOfVideoStream + 1);

            addSingleItemToLocalDatabase(editor, totalNumberOfVideoStream, SharedPreferenceConstants.DEFAULT_STRING,
                    SharedPreferenceConstants.DEFAULT_STRING, FragmentConstants.KEY_VIDEO_STREAM_SAVE);
        }
    };

    private void refreshUI() {
        setUpRecyclerData();
        mRecyclerAdapter.updateData(mVideoStreamListItems);
    }

    private void onVisible() {
        refreshUI();
    }

    private void onInvisible() {
        System.out.println("onInvisible OUT");
        // Remove invalid video stream items when navigate to other fragments
        // Invalid means either name or url is empty
        for (int i = 0; i < getTotalNumberOfVideoStreams(); i++) {
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String videoStreamInitials = SharedPreferenceConstants.HEADER_VIDEO_STREAM.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(String.valueOf(i));

            String name = pref.getString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_NAME), SharedPreferenceConstants.DEFAULT_STRING);
            String url = pref.getString(videoStreamInitials.concat(SharedPreferenceConstants.SEPARATOR).
                    concat(SharedPreferenceConstants.SUB_HEADER_VIDEO_STREAM_URL), SharedPreferenceConstants.DEFAULT_STRING);

            System.out.println("onInvisible");
            if (SharedPreferenceConstants.DEFAULT_STRING.equalsIgnoreCase(name) ||
                    SharedPreferenceConstants.DEFAULT_STRING.equalsIgnoreCase(url)) {
                removeSingleItemFromLocalDatabase(i);
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint is " + isVisibleToUser);
        mIsVisibleToUser = isVisibleToUser;
        if (isResumed()) { // fragment has been created at this point
            if (mIsVisibleToUser) {
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


        if (mIsVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");

        if (mIsVisibleToUser) {
            onInvisible();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");

        if (mIsVisibleToUser) {
            onInvisible();
        }
    }
}
