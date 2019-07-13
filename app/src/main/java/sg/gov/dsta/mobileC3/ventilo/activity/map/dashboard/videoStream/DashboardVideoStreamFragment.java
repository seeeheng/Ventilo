package sg.gov.dsta.mobileC3.ventilo.activity.map.dashboard.videoStream;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.rtsp.RtspDefaultClient;
import com.google.android.exoplayer2.source.rtsp.RtspMediaSource;
import com.google.android.exoplayer2.source.rtsp.core.Client;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.activity.map.MapShipBlueprintFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.sitrep.SitRepAddUpdateFragment;
import sg.gov.dsta.mobileC3.ventilo.activity.videostream.VideoStreamControllerView;
import sg.gov.dsta.mobileC3.ventilo.application.MainApplication;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.VideoStreamViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.PhotoCaptureUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.StringUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansSemiBoldTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.enums.videoStream.EOwner;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;

public class DashboardVideoStreamFragment extends Fragment implements SnackbarUtil.SnackbarActionClickListener {

    private static final String TAG = DashboardVideoStreamFragment.class.getSimpleName();
    private static final int MEDIA_CONTROLLER_SHOW_DURATION = 3000;
    private static final int FIRST_VIDEO_STREAM_ID = 1;
//    private static final int SECOND_VIDEO_STREAM_ID = 2;

    // View models
    private UserViewModel mUserViewModel;
    private VideoStreamViewModel mVideoStreamViewModel;

    // Main layout
    private ConstraintLayout mConstraintLayoutMain;

    /**
     * -------------------- Exo player first video stream --------------------
     **/
    // First video stream toolbar
    private Spinner mSpinnerOneVideoStreamList;
    private ArrayAdapter mSpinnerVideoStreamAdapterOne;
    private ArrayList<String> mVideoStreamNameList;
    private List<VideoStreamModel> mVideoStreamModelList;

    // First video stream view
    private RelativeLayout mRelativeLayoutToolbarOne;
    private LinearLayout mLinearLayoutVideoStreamOne;
    private ConstraintLayout mConstraintLayoutSurfaceViewOne;
    private TextureView mSurfaceViewOne;
    private C2OpenSansSemiBoldTextView mTvStreamOne;

    private SimpleExoPlayer mMediaPlayerOne;
    private VideoStreamControllerView mVideoStreamControllerViewOne;

    private int mSpinnerOneSelectedPos;

    /**
     * -------------------- Exo player second video stream --------------------
     **/
//    // Second video stream toolbar
//    private ArrayAdapter mSpinnerVideoStreamAdapterTwo;
//    private ArrayList<String> mVideoStreamNameListTwo;
//
//    // Second video stream view
//    private RelativeLayout mRelativeLayoutToolbarTwo;
//    private LinearLayout mLinearLayoutVideoStreamTwo;
//    private ConstraintLayout mConstraintLayoutSurfaceViewTwo;
//    private TextureView mSurfaceViewTwo;
//
//    private SimpleExoPlayer mMediaPlayerTwo;
//    private VideoStreamControllerView mVideoStreamControllerViewTwo;

    /**
     * -------------------- Others --------------------
     **/
    private boolean mIsFullscreen;
    private boolean mIsFragmentVisibleToUser;
    Bitmap mBitmapTakenScreenshot;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dashboard_video_stream, container, false);

        observerSetup();
        initUI(rootView);

        return rootView;
    }

    /**
     * Initialise view with recycler data
     *
     * @param rootView
     */
    private void initUI(View rootView) {
        mConstraintLayoutMain = rootView.findViewById(R.id.layout_dashboard_video_stream_fragment);

//        initVideoStreamOneToolbar(rootView);
//        initSurfaceViewVideoOneStream(rootView);
        initVideoStreamOneUI(rootView);
//        initVideoStreamTwoUI(rootView);
//        setVideoStreamDefaultLayout(mLinearLayoutVideoStreamOne, mConstraintLayoutSurfaceViewOne);
    }

    // -------------------- Initialise first video stream UI -------------------- //
    private void initVideoStreamOneUI(View rootView) {
        mLinearLayoutVideoStreamOne = rootView.findViewById(R.id.layout_dashboard_video_stream_one);
        mLinearLayoutVideoStreamOne.setVisibility(View.VISIBLE);
        initVideoStreamOneToolbar(rootView);
        initSurfaceViewVideoOneStream(rootView);
    }

    private void initVideoStreamOneToolbar(View rootView) {
        mRelativeLayoutToolbarOne = rootView.findViewById(R.id.layout_dashboard_video_stream_one_toolbar);
        initVideoListOneSpinner(rootView);
    }

    private void initVideoListOneSpinner(View rootView) {
        mSpinnerOneVideoStreamList = rootView.findViewById(R.id.spinner_dashboard_video_stream_one_selector);
        mVideoStreamNameList = new ArrayList<>();
        mVideoStreamNameList.add(MainApplication.getAppContext().
                getString(R.string.video_stream_select_spinner_item));

        mSpinnerVideoStreamAdapterOne = getSpinnerArrayAdapter(mVideoStreamNameList);

        mSpinnerOneVideoStreamList.setAdapter(mSpinnerVideoStreamAdapterOne);
        mSpinnerOneVideoStreamList.setOnItemSelectedListener(videoStreamOneSpinnerOnItemSelectedListener);
    }

    private Spinner.OnItemSelectedListener videoStreamOneSpinnerOnItemSelectedListener =
            new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSpinnerOneSelectedPos = position;
                    String selectedVideoName = (String) parent.getItemAtPosition(position);
                    getUrlStreamFromName(selectedVideoName, FIRST_VIDEO_STREAM_ID);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };

    private void initSurfaceViewVideoOneStream(View rootView) {
        // Video Stream Container
        mConstraintLayoutSurfaceViewOne = rootView.findViewById(R.id.constraint_layout_dashboard_video_surface_one);
        mSurfaceViewOne = rootView.findViewById(R.id.texture_view_dashboard_stream_one);
        ProgressBar progressBar = rootView.findViewById(R.id.spinner_dashboard_stream_one);
        progressBar.setVisibility(View.GONE);
        mTvStreamOne = rootView.
                findViewById(R.id.tv_dashboard_stream_one_no_video_selected);

        mVideoStreamControllerViewOne = new VideoStreamControllerView(getContext(), false);
        mMediaPlayerOne = ExoPlayerFactory.newSimpleInstance(getContext());
        mMediaPlayerOne.setVideoTextureView(mSurfaceViewOne);

        VideoStreamControllerView.MediaPlayerControl videoStreamPlayerInterface =
                getMediaPlayerControlInterface(mMediaPlayerOne, mSurfaceViewOne,
                        mLinearLayoutVideoStreamOne, mConstraintLayoutSurfaceViewOne,
                        FIRST_VIDEO_STREAM_ID);
        mVideoStreamControllerViewOne.setMediaPlayer(videoStreamPlayerInterface);

        Player.EventListener playerListener = getPlayerEventListener(mTvStreamOne,
                progressBar);
        mMediaPlayerOne.addListener(playerListener);

        mVideoStreamControllerViewOne.setAnchorView(mConstraintLayoutSurfaceViewOne);
    }

    // -------------------- Initialise second video stream UI -------------------- //
//    private void initVideoStreamTwoUI(View rootView) {
//        mLinearLayoutVideoStreamTwo = rootView.findViewById(R.id.layout_dashboard_video_stream_two);
//        mLinearLayoutVideoStreamTwo.setVisibility(View.VISIBLE);
//        initVideoStreamTwoToolbar(rootView);
//        initSurfaceViewVideoTwoStream(rootView);
//    }
//
//    private void initVideoStreamTwoToolbar(View rootView) {
//        mRelativeLayoutToolbarTwo = rootView.findViewById(R.id.layout_dashboard_video_stream_two_toolbar);
//        initVideoListTwoSpinner(rootView);
//    }
//
//    private void initVideoListTwoSpinner(View rootView) {
//        Spinner spinnerVideoStreamList = rootView.findViewById(R.id.spinner_dashboard_video_stream_two_selector);
//        mVideoStreamNameListTwo = new ArrayList<>();
//        mVideoStreamNameListTwo.add(getString(R.string.video_stream_select_spinner_item));
//
//        mSpinnerVideoStreamAdapterTwo = getSpinnerArrayAdapter(mVideoStreamNameListTwo);
//
//        spinnerVideoStreamList.setAdapter(mSpinnerVideoStreamAdapterTwo);
//        spinnerVideoStreamList.setOnItemSelectedListener(videoStreamTwoSpinnerOnItemSelectedListener);
//    }
//
//    private Spinner.OnItemSelectedListener videoStreamTwoSpinnerOnItemSelectedListener =
//            new Spinner.OnItemSelectedListener() {
//                @Override
//                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                    String selectedVideoName = (String) parent.getItemAtPosition(position);
//                    getUrlStreamFromName(selectedVideoName, SECOND_VIDEO_STREAM_ID);
//                }
//
//                @Override
//                public void onNothingSelected(AdapterView<?> adapterView) {
//
//                }
//            };
//
//    private void initSurfaceViewVideoTwoStream(View rootView) {
//        // Video Stream Container
//        mConstraintLayoutSurfaceViewTwo = rootView.findViewById(R.id.constraint_layout_dashboard_video_surface_two);
//        mSurfaceViewTwo = rootView.findViewById(R.id.texture_view_dashboard_stream_two);
//        ProgressBar progressBar = rootView.findViewById(R.id.spinner_dashboard_stream_two);
//        progressBar.setVisibility(View.GONE);
//        C2OpenSansSemiBoldTextView tvStream = rootView.
//                findViewById(R.id.tv_dashboard_stream_two_no_video_selected);
//
//        mVideoStreamControllerViewTwo = new VideoStreamControllerView(getContext(), false);
//        mMediaPlayerTwo = ExoPlayerFactory.newSimpleInstance(getContext());
//        mMediaPlayerTwo.setVideoTextureView(mSurfaceViewTwo);
//
//        VideoStreamControllerView.MediaPlayerControl videoStreamPlayerInterface =
//                getMediaPlayerControlInterface(mMediaPlayerTwo, mSurfaceViewTwo,
//                        mLinearLayoutVideoStreamTwo, mConstraintLayoutSurfaceViewTwo,
//                        SECOND_VIDEO_STREAM_ID);
//        mVideoStreamControllerViewTwo.setMediaPlayer(videoStreamPlayerInterface);
//
//        Player.EventListener playerListener = getPlayerEventListener(tvStream,
//                progressBar);
//        mMediaPlayerTwo.addListener(playerListener);
//
//        mVideoStreamControllerViewTwo.setAnchorView(mConstraintLayoutSurfaceViewTwo);
//    }

    private ArrayAdapter<String> getSpinnerArrayAdapter(ArrayList<String> stringArrayList) {
        return new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_row_item, R.id.tv_spinner_row_item_text, stringArrayList) {

            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be used as hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                AppCompatImageView img = view.findViewById(R.id.img_spinner_home_icon);
                View viewExtraMargin = view.findViewById(R.id.view_extra_margin);

                img.setVisibility(View.GONE);
                viewExtraMargin.setVisibility(View.GONE);

                if (position == 1 && EOwner.SELF.toString().
                        equalsIgnoreCase(mVideoStreamModelList.get(0).getOwner())) {
                    img.setVisibility(View.VISIBLE);
                } else if (position != 0) {
                    viewExtraMargin.setVisibility(View.VISIBLE);
                }

                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);

                AppCompatImageView img = view.findViewById(R.id.img_spinner_home_icon);
                View viewExtraMargin = view.findViewById(R.id.view_extra_margin);
                TextView tv = view.findViewById(R.id.tv_spinner_row_item_text);

                img.setVisibility(View.GONE);
                viewExtraMargin.setVisibility(View.GONE);

                if (mSpinnerOneSelectedPos == position) {
                    img.setColorFilter(ResourcesCompat.getColor(
                            getResources(), R.color.primary_highlight_cyan, null));
                    tv.setTextColor(ResourcesCompat.getColor(getResources(),
                            R.color.primary_highlight_cyan, null));
                } else {
                    img.setColorFilter(ResourcesCompat.getColor(
                            getResources(), R.color.primary_white, null));
                    tv.setTextColor(ResourcesCompat.getColor(getResources(),
                            R.color.primary_white, null));
                }

                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_text_hint_dark_grey, null));
                } else if (position == 1 && EOwner.SELF.toString().
                        equalsIgnoreCase(mVideoStreamModelList.get(0).getOwner())) {
                    img.setVisibility(View.VISIBLE);
                } else {
                    viewExtraMargin.setVisibility(View.VISIBLE);
                }

                return view;
            }
        };
    }

    private synchronized VideoStreamControllerView.MediaPlayerControl getMediaPlayerControlInterface(
            SimpleExoPlayer mediaPlayer, TextureView textureView, View linearLayout,
            View constraintLayout, int streamNo) {

        return new VideoStreamControllerView.MediaPlayerControl() {
            public int getBufferPercentage() {
                return 0;
            }

            public int getCurrentPosition() {
                float pos = mediaPlayer.getCurrentPosition();
                return (int) (pos * getDuration());
            }

            public int getDuration() {
                return (int) mediaPlayer.getDuration();
            }

            public boolean isPlaying() {
                return mediaPlayer.getPlayWhenReady();
            }

            public void pause() {
                if (mediaPlayer != null) {
                    mediaPlayer.setPlayWhenReady(false);
                }
            }

            public void takeScreenshot() {
//            Bitmap imageBitmap = screenShot(mSurfaceViewOne);
                if (mBitmapTakenScreenshot != null) {
                    mBitmapTakenScreenshot.recycle();
                }

                mBitmapTakenScreenshot = textureView.getBitmap();

                if (mBitmapTakenScreenshot != null) {
                    String imagePath = PhotoCaptureUtil.insertImage(getActivity().getContentResolver(),
                            mBitmapTakenScreenshot, "Video_Streaming_Screenshot.jpg", "VLC screenshot");
//                System.out.println("imagePath is " + imagePath);

                    if (imagePath != null && getSnackbarView() != null) {
                        StringBuilder screenshotStringBuilder = new StringBuilder();
                        screenshotStringBuilder.append(MainApplication.getAppContext().
                                getString(R.string.snackbar_screenshot_taken_message));
                        screenshotStringBuilder.append(System.lineSeparator());
                        screenshotStringBuilder.append(System.lineSeparator());
                        screenshotStringBuilder.append(MainApplication.getAppContext().
                                getString(R.string.snackbar_screenshot_create_sitrep_message));

                        SnackbarUtil.showCustomAlertSnackbar(mConstraintLayoutMain, getSnackbarView(),
                                screenshotStringBuilder.toString(),
                                DashboardVideoStreamFragment.this);
                    }
                }
            }

            public void seekTo(int pos) {
                mediaPlayer.seekTo(pos / getDuration());
            }

            public void start() {
                if (mediaPlayer != null) {
                    mediaPlayer.setPlayWhenReady(true);
                }
            }

            public boolean canPause() {
                return true;
            }

            public boolean canSeekBackward() {
                return false;
            }

            public boolean canSeekForward() {
                return false;
            }

            @Override
            public boolean isFullScreen() {
                return mIsFullscreen;
            }

            @Override
            public void toggleFullScreen() {
                if (!isFullScreen()) {
                    setVideoStreamFullscreen(linearLayout, constraintLayout);
                } else {
                    setVideoStreamDefaultScreen(linearLayout, constraintLayout);
                }

                setOtherUiElementsForVideoStreams(!isFullScreen(), streamNo);
                mIsFullscreen = !mIsFullscreen;
            }
        };
    }

    /**
     * Set default screen dimension
     *
     * @param linearLayout
     * @param constraintLayout
     */
    private void setVideoStreamDefaultScreen(View linearLayout, View constraintLayout) {

        mConstraintLayoutMain.setPadding((int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing));

        setVideoStreamDefaultLayout(linearLayout, constraintLayout);
        setBlueprintComponents(View.VISIBLE);
        setMainActivityComponents(View.VISIBLE);
    }

    /**
     * Sets other UI layouts accordingly based on default or fullscreen,
     * other than those that are included in the video stream layouts
     *
     * @param isFullScreen
     * @param streamNo
     */
    private void setOtherUiElementsForVideoStreams(boolean isFullScreen, int streamNo) {
        if (isFullScreen) {
            switch (streamNo) {
                case FIRST_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarOne.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamTwo.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamFour.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.GONE);
                    break;

//                case SECOND_VIDEO_STREAM_ID:
//                    mRelativeLayoutToolbarTwo.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamOne.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamFive.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.GONE);
//                    break;
//
//                case THIRD_VIDEO_STREAM_ID:
//                    mRelativeLayoutToolbarThree.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamSix.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.GONE);
//                    break;
//
//                case FOURTH_VIDEO_STREAM_ID:
//                    mRelativeLayoutToolbarFour.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamOne.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.GONE);
//                    break;
//
//                case FIFTH_VIDEO_STREAM_ID:
//                    mRelativeLayoutToolbarFive.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamTwo.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.GONE);
//                    break;
//
//                case SIXTH_VIDEO_STREAM_ID:
//                    mRelativeLayoutToolbarSix.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamThree.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.GONE);
//                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.GONE);
//                    break;
            }
        } else {
            switch (streamNo) {
                case FIRST_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarOne.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamTwo.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamFour.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.VISIBLE);
                    break;

//                case SECOND_VIDEO_STREAM_ID:
//                    mRelativeLayoutToolbarTwo.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamOne.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamFive.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.VISIBLE);
//                    break;

//                case THIRD_VIDEO_STREAM_ID:
//                    mRelativeLayoutToolbarThree.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamSix.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.VISIBLE);
//                    break;
//
//                case FOURTH_VIDEO_STREAM_ID:
//                    mRelativeLayoutToolbarFour.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamOne.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.VISIBLE);
//                    break;
//
//                case FIFTH_VIDEO_STREAM_ID:
//                    mRelativeLayoutToolbarFive.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamTwo.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.VISIBLE);
//                    break;
//
//                case SIXTH_VIDEO_STREAM_ID:
//                    mRelativeLayoutToolbarSix.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamThree.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.VISIBLE);
//                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.VISIBLE);
//                    break;
            }
        }
    }

    /**
     * Sets video stream layout to full screen
     *
     * @param linearLayout
     * @param constraintLayout
     */
    private void setVideoStreamFullscreen(View linearLayout, View constraintLayout) {

        mConstraintLayoutMain.setPadding(0, 0, 0, 0);

        setLayoutFullScreen(linearLayout);
        setLayoutFullScreen(constraintLayout);

        setBlueprintComponents(View.GONE);
        setMainActivityComponents(View.GONE);
    }

    /**
     * Sets given view's dimension to fullscreen
     *
     * @param view
     */
    private void setLayoutFullScreen(View view) {
        DimensionUtil.setDimensions(view,
                DimensionUtil.getScreenWidth(),
                DimensionUtil.getScreenHeightWithoutNavAndStatusBar(),
                new LinearLayout(getContext()));

        DimensionUtil.setPaddings(view, 0, 0, 0, 0);
        DimensionUtil.setMargins(view, 0, 0, 0, 0);
    }

    /**
     * Set visibility of map blueprint components including blueprint and other fragments
     *
     * @param visibility
     */
    private void setBlueprintComponents(int visibility) {
        if (getParentFragment() instanceof MapShipBlueprintFragment) {
            MapShipBlueprintFragment parentFragment =
                    (MapShipBlueprintFragment) getParentFragment();
            parentFragment.setBlueprintVisibility(visibility);
            parentFragment.setMiddleDividerVisibility(visibility);
            parentFragment.setBottomDividerVisibility(visibility);
            parentFragment.setSitRepPersonnelStatusFragmentVisibility(visibility);
            parentFragment.setTaskPhaseStatusFragmentVisibility(visibility);
            parentFragment.setRadioLinkStatusFragmentVisibility(visibility);

            if (visibility == View.GONE) {
                setLayoutFullScreen(parentFragment.getHorizontalSVDashboardFragments());
                setLayoutFullScreen(parentFragment.getVideoStreamFragment());

            } else {
                DimensionUtil.setDimensions(parentFragment.getHorizontalSVDashboardFragments(),
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        0, 335, new LinearLayout(getContext()));

                DimensionUtil.setDimensions(parentFragment.getVideoStreamFragment(),
                        (int) getResources().getDimension(
                                R.dimen.dashboard_video_stream_main_container_width),
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        new LinearLayout(getContext()));
            }
        }
    }

    /**
     * Set visibility of main activity components including Side and Bottom Panels
     *
     * @param visibility
     */
    private void setMainActivityComponents(int visibility) {
        if (getActivity() instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) getActivity();
            if (mainActivity.getMainSidePanel() != null) {
                mainActivity.getMainSidePanel().setVisibility(visibility);
            }
            if (mainActivity.getMainBottomPanel() != null) {
                mainActivity.getMainBottomPanel().setVisibility(visibility);
            }
        }
    }

    /**
     * Set default dimension layout for users other than CCT
     *
     * @param linearLayout
     * @param constraintLayout
     */
    private void setVideoStreamDefaultLayout(View linearLayout,
                                             View constraintLayout) {
        DimensionUtil.setDimensions(linearLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                new LinearLayout(getContext()));

        DimensionUtil.setMargins(linearLayout,
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                0, (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing));

        DimensionUtil.setDimensions(constraintLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.dashboard_video_stream_texture_view_height),
                new LinearLayout(getContext()));
    }

    private void getUrlStreamFromName(String videoName, int spinnerId) {

        SingleObserver<String> singleObserverVideoStreamForUserByName =
                new SingleObserver<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        // add it to a CompositeDisposable
                    }

                    @Override
                    public void onSuccess(String videoUrl) {
                        if (videoUrl != null) {
                            Log.d(TAG, "onSuccess singleObserverVideoStreamForUserByName, " +
                                    "getUrlStreamFromName. " +
                                    "videoUrl: " + videoUrl);

//                            Runnable videoStreamRunnable = new Runnable() {
//                                @Override
//                                public void run() {
                            switch (spinnerId) {
                                case FIRST_VIDEO_STREAM_ID:
                                    setVideoVLCStreamURL(videoUrl,
                                            mConstraintLayoutSurfaceViewOne,
                                            mVideoStreamControllerViewOne,
                                            mMediaPlayerOne);
                                    break;

//                                case SECOND_VIDEO_STREAM_ID:
//                                    setVideoVLCStreamURL(videoUrl,
//                                            mConstraintLayoutSurfaceViewTwo,
//                                            mVideoStreamControllerViewTwo,
//                                            mMediaPlayerTwo);
//                                    break;
//
//                                case THIRD_VIDEO_STREAM_ID:
//                                    setVideoVLCStreamURL(videoUrl,
//                                            mConstraintLayoutSurfaceViewThree,
//                                            mVideoStreamControllerViewThree,
//                                            mMediaPlayerThree);
//                                    break;
//
//                                case FOURTH_VIDEO_STREAM_ID:
//                                    setVideoVLCStreamURL(videoUrl,
//                                            mConstraintLayoutSurfaceViewFour,
//                                            mVideoStreamControllerViewFour,
//                                            mMediaPlayerFour);
//                                    break;
//
//                                case FIFTH_VIDEO_STREAM_ID:
//                                    setVideoVLCStreamURL(videoUrl,
//                                            mConstraintLayoutSurfaceViewFive,
//                                            mVideoStreamControllerViewFive,
//                                            mMediaPlayerFive);
//                                    break;
//
//                                case SIXTH_VIDEO_STREAM_ID:
//                                    setVideoVLCStreamURL(videoUrl,
//                                            mConstraintLayoutSurfaceViewSix,
//                                            mVideoStreamControllerViewSix,
//                                            mMediaPlayerSix);
//                                    break;
                            }
//                                }
//                            };

//                            Thread videoStreamThread = new Thread(videoStreamRunnable);
//                            videoStreamThread.run();

                        } else {
                            Log.d(TAG, "onSuccess singleObserverVideoStreamForUserByName, " +
                                    "getUrlStreamFromName. " +
                                    "videoUrl is null");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError singleObserverVideoStreamForUserByName, " +
                                "getUrlStreamFromName. " +
                                "Error Msg: " + e.toString());
                    }
                };

        mVideoStreamViewModel.getVideoStreamUrlForUserByName(
                SharedPreferenceUtil.getCurrentUserCallsignID(), videoName,
                singleObserverVideoStreamForUserByName);
    }

    private synchronized void setVideoVLCStreamURL(String media, ConstraintLayout constraintLayout,
                                                   VideoStreamControllerView videoStreamControllerView,
                                                   SimpleExoPlayer mediaPlayer) {
        constraintLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                videoStreamControllerView.show(MEDIA_CONTROLLER_SHOW_DURATION);
            }
        });

        // Measures bandwidth during playback. Can be null if not required.
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();

        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getContext(),
                Util.getUserAgent(getContext(), MainApplication.getAppContext().
                        getString(R.string.app_name)), bandwidthMeter);

        String extension = media.substring(media.lastIndexOf(StringUtil.DOT) + 1);

        Log.i(TAG, "stream media url extension: " + extension);

        // This is the MediaSource representing the media to be played.
        MediaSource videoSource = buildMediaSource(Uri.parse(media), extension, dataSourceFactory);

        // Prepare the player with the source.
        mediaPlayer.prepare(videoSource);
        mediaPlayer.setPlayWhenReady(true);
    }

    private MediaSource buildMediaSource(Uri uri, @Nullable String overrideExtension,
                                         DataSource.Factory dataSourceFactory) {
        @C.ContentType int type = Util.inferContentType(uri, overrideExtension);
        switch (type) {
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_SS:
                return new SsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(dataSourceFactory)
                        .createMediaSource(uri);
            case C.TYPE_OTHER:
                if (Util.isRtspUri(uri)) {
                    return new RtspMediaSource.Factory(RtspDefaultClient.factory()
                            .setFlags(Client.FLAG_ENABLE_RTCP_SUPPORT)
                            .setNatMethod(Client.RTSP_NAT_DUMMY))
                            .createMediaSource(uri);
                } else {
                    return new ExtractorMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(uri);
                }
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    private Player.EventListener getPlayerEventListener(C2OpenSansSemiBoldTextView tvStream,
                                                        ProgressBar progressBar) {
        return new Player.EventListener() {
            @Override
            public void onTimelineChanged(Timeline timeline, Object o, int i) {
            }

            @Override
            public void onTracksChanged(TrackGroupArray trackGroupArray, TrackSelectionArray trackSelectionArray) {
            }

            @Override
            public void onLoadingChanged(boolean isLoading) {
            }

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                switch (playbackState) {

                    case Player.STATE_BUFFERING:
                        tvStream.setVisibility(View.GONE);
                        progressBar.setVisibility(View.VISIBLE);
                        break;
                    case Player.STATE_ENDED:
                        // Activate the force enable
                        tvStream.setVisibility(View.VISIBLE);
                        tvStream.setText(MainApplication.getAppContext().
                                getString(R.string.video_stream_stream_ended));
                        break;
                    case Player.STATE_IDLE:
                        tvStream.setVisibility(View.VISIBLE);
                        tvStream.setText(MainApplication.getAppContext().
                                getString(R.string.video_stream_no_video_selected));
                        progressBar.setVisibility(View.GONE);

                        if (tvStream == mTvStreamOne && mSpinnerOneSelectedPos != 0) {
                            tvStream.setText(MainApplication.getAppContext().
                                    getString(R.string.video_stream_unable_to_load));
                        }

                        break;
                    case Player.STATE_READY:
                        tvStream.setVisibility(View.GONE);
                        progressBar.setVisibility(View.GONE);
                        break;
                    default:
                        // status = PlaybackStatus.IDLE;
                        break;
                }
            }

            @Override
            public void onRepeatModeChanged(int i) {
            }

            @Override
            public void onShuffleModeEnabledChanged(boolean b) {
            }

            @Override
            public void onPlayerError(ExoPlaybackException e) {
            }

            @Override
            public void onPositionDiscontinuity(int i) {
            }

            @Override
            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            }

            @Override
            public void onSeekProcessed() {
            }
        };
    }

    /**
     * Initialise Media Players, if not already done so
     * This is primarily for the resume of fragment
     */
    private void initMediaPlayers() {
        if (mSurfaceViewOne != null && mMediaPlayerOne == null) {
            mMediaPlayerOne = ExoPlayerFactory.newSimpleInstance(getContext());
            mMediaPlayerOne.setVideoTextureView(mSurfaceViewOne);
        }

//        if (mSurfaceViewTwo != null && mMediaPlayerTwo == null) {
//            mMediaPlayerTwo = ExoPlayerFactory.newSimpleInstance(getContext());
//            mMediaPlayerTwo.setVideoTextureView(mSurfaceViewTwo);
//        }
//
//        if (mSurfaceViewThree != null && mMediaPlayerThree == null) {
//            mMediaPlayerThree = ExoPlayerFactory.newSimpleInstance(getContext());
//            mMediaPlayerThree.setVideoTextureView(mSurfaceViewThree);
//        }
//
//        if (mSurfaceViewFour != null && mMediaPlayerFour == null) {
//            mMediaPlayerFour = ExoPlayerFactory.newSimpleInstance(getContext());
//            mMediaPlayerFour.setVideoTextureView(mSurfaceViewFour);
//        }
//
//        if (mSurfaceViewFive != null && mMediaPlayerFive == null) {
//            mMediaPlayerFive = ExoPlayerFactory.newSimpleInstance(getContext());
//            mMediaPlayerFive.setVideoTextureView(mSurfaceViewFive);
//        }
//
//        if (mSurfaceViewSix != null && mMediaPlayerSix == null) {
//            mMediaPlayerSix = ExoPlayerFactory.newSimpleInstance(getContext());
//            mMediaPlayerSix.setVideoTextureView(mSurfaceViewSix);
//        }
    }


//    private Fragment getCurrentParentFragment() {
//        if (getActivity() instanceof MainActivity) {
//            Fragment currentFragment = ((MainActivity) getActivity()).getCurrentFragment();
//            if (currentFragment instanceof MapShipBlueprintFragment) {
//                return currentFragment;
//            }
//        }
//
//        getParentFragment()
//        return null;
//    }

    // -------------------- Pause players -------------------- //
    private void pausePlayerOne() {
        if (mMediaPlayerOne != null) {
            mMediaPlayerOne.setPlayWhenReady(false);
        }
    }

    private void pausePlayers() {
        pausePlayerOne();
    }

    // -------------------- Resume players -------------------- //
    private void resumePlayerOne() {
        if (mMediaPlayerOne != null) {
            mMediaPlayerOne.setPlayWhenReady(true);
        }
    }

    private void resumePlayers() {
        resumePlayerOne();
    }

    // -------------------- Stop players -------------------- //
    private void stopPlayerOne(boolean reset) {
        if (mMediaPlayerOne != null) {
            mMediaPlayerOne.stop(reset);
        }
    }

    private void stopPlayers(boolean reset) {
        stopPlayerOne(reset);
    }

    // -------------------- Release players -------------------- //
    private void releasePlayerOne() {
        if (mMediaPlayerOne != null) {
            mMediaPlayerOne.release();
            mMediaPlayerOne = null;
        }
    }

    private void releasePlayers() {
        releasePlayerOne();
    }

    private void navigateToFragment(Fragment toFragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateWithAnimatedTransitionToFragment(
                    R.id.layout_dashboard_video_stream_fragment, this, toFragment);
        }
    }

    private View getSnackbarView() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getSnackbarView();
        } else {
            return null;
        }
    }

    private void refreshVideoStreamSpinnerData(ArrayAdapter spinnerVideoStreamAdapter,
                                               List<String> currentVideoStreamNameList,
                                               List<String> newVideoStreamNameList) {
        if (spinnerVideoStreamAdapter != null) {
            if (currentVideoStreamNameList == null) {
                currentVideoStreamNameList = new ArrayList<>();
            } else {
                currentVideoStreamNameList.clear();
            }

            currentVideoStreamNameList.add(MainApplication.getAppContext().
                    getString(R.string.video_stream_select_spinner_item));
            currentVideoStreamNameList.addAll(newVideoStreamNameList);

            /**
             * Remove last video model item if item content is empty.
             * Only the last item will possibly be empty because an
             * empty entry will be stored in the database to be displayed in the
             * add video stream fragment's recyclerview for user to fill up details
             * of this entry and save it in the database.
             */
            int lastVideoStreamItemIndex = currentVideoStreamNameList.size() - 1;
            if (lastVideoStreamItemIndex != 0) {
                String lastVideoStreamModel = currentVideoStreamNameList.get(lastVideoStreamItemIndex);

                if (TextUtils.isEmpty(lastVideoStreamModel.trim())) {
                    currentVideoStreamNameList.remove(lastVideoStreamItemIndex);
                }
            }

            spinnerVideoStreamAdapter.notifyDataSetChanged();
        }
    }

    private List<String> sortVideoStreamNameList(List<VideoStreamModel> videoStreamModelList) {

        List<String> videoStreamNameList = null;

        if (videoStreamModelList != null) {
            if (mVideoStreamModelList == null) {
                mVideoStreamModelList = new ArrayList<>();
            } else {
                mVideoStreamModelList.clear();
            }

            List<VideoStreamModel> otherVideoStreamModelList = videoStreamModelList.stream().
                    filter(videoStreamModel -> EOwner.OTHERS.toString().equalsIgnoreCase(
                            videoStreamModel.getOwner())).collect(Collectors.toList());

            mVideoStreamModelList.addAll(otherVideoStreamModelList);

            List<VideoStreamModel> ownVideoStreamModelList = videoStreamModelList.stream().
                    filter(videoStreamModel -> EOwner.SELF.toString().equalsIgnoreCase(
                            videoStreamModel.getOwner())).collect(Collectors.toList());

            // There should only be ONE 'Self' video stream in the list
            if (ownVideoStreamModelList.size() == 1) {
                mVideoStreamModelList.add(0, ownVideoStreamModelList.get(0));
            }

            videoStreamNameList = mVideoStreamModelList.stream().
                    map(videoStreamModel -> videoStreamModel.getName()).collect(Collectors.toList());
        }

        return videoStreamNameList;
    }

    /**
     * Set up observer for live updates on view models and update UI accordingly
     */
    private void observerSetup() {
        mUserViewModel = ViewModelProviders.of(this).get(UserViewModel.class);
        mVideoStreamViewModel = ViewModelProviders.of(this).get(VideoStreamViewModel.class);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = sharedPrefs.getString(SharedPreferenceConstants.USER_ID,
                SharedPreferenceConstants.DEFAULT_STRING);

        /*
         * Refreshes spinner UI whenever there is a change in video streams (insert, update or delete)
         */
        mVideoStreamViewModel.getAllVideoStreamsLiveDataForUser(userId).observe(this,
                new Observer<List<VideoStreamModel>>() {
                    @Override
                    public void onChanged(@Nullable List<VideoStreamModel> videoStreamModelList) {
                        List<String> videoStreamNameList = sortVideoStreamNameList(videoStreamModelList);

                        refreshVideoStreamSpinnerData(mSpinnerVideoStreamAdapterOne,
                                mVideoStreamNameList, videoStreamNameList);

                        if (mVideoStreamNameList.size() > FIRST_VIDEO_STREAM_ID) {
                            mSpinnerOneVideoStreamList.setSelection(FIRST_VIDEO_STREAM_ID);
                        } else {
                            mSpinnerOneSelectedPos = 0;
                            mSpinnerOneVideoStreamList.setSelection(0);
                            stopPlayerOne(true);
                            mTvStreamOne.setText(MainApplication.getAppContext().
                                    getString(R.string.video_stream_no_video_selected));
                        }
                    }
                });
    }

    @Override
    public void onSnackbarActionClick() {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mBitmapTakenScreenshot.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] takenScreenshotByteArray = stream.toByteArray();

        Fragment sitRepAddUpdateFragment = new SitRepAddUpdateFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FragmentConstants.KEY_SITREP, FragmentConstants.VALUE_SITREP_ADD);
        bundle.putByteArray(FragmentConstants.KEY_SITREP_PICTURE, takenScreenshotByteArray);
        sitRepAddUpdateFragment.setArguments(bundle);

        navigateToFragment(sitRepAddUpdateFragment);
    }

    private void onVisible() {
        Log.d(TAG, "onVisible");
        initMediaPlayers();
        resumePlayers();
    }

    private void onInvisible() {
        Log.d(TAG, "onInvisible");
//        hideKeyboard();

//        CloseVideoStreamsAsyncTask task = new CloseVideoStreamsAsyncTask();
//        task.execute();
        pausePlayers();
        releasePlayers();


//        if (Util.SDK_INT <= 23) {
//            releasePlayer();
//        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
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
        if (mIsFragmentVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
//        releasePlayers();

    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIsFragmentVisibleToUser) {
            onInvisible();
        }

//        releasePlayers();

//        if (Util.SDK_INT > 23) {
//            releasePlayer();
//        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

//        releasePlayers();
    }
}
