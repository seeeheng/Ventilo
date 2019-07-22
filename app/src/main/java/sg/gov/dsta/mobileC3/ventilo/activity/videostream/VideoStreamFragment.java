package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

//import com.rvirin.onvif.onvifcamera.OnvifDevice;
//import com.rvirin.onvif.onvifcamera.OnvifListener;
//import com.rvirin.onvif.onvifcamera.OnvifRequest;
//import com.rvirin.onvif.onvifcamera.OnvifResponse;
//
//import org.jetbrains.annotations.NotNull;

//import com.google.android.exoplayer2.source.hls.HlsMediaSource;
//import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
//import com.google.android.exoplayer2.util.Util;

//import org.videolan.libvlc.IVLCVout;
//import org.videolan.libvlc.LibVLC;
//import org.videolan.libvlc.Media;
//import org.videolan.libvlc.MediaPlayer;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.activity.sitrep.SitRepAddUpdateFragment;
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
import sg.gov.dsta.mobileC3.ventilo.util.enums.user.EAccessRight;

public class VideoStreamFragment extends Fragment implements SnackbarUtil.SnackbarActionClickListener {

    private static final String TAG = VideoStreamFragment.class.getSimpleName();
    private static final int MEDIA_CONTROLLER_SHOW_DURATION = 3000;
    private static final int FIRST_VIDEO_STREAM_ID = 1;
    private static final int SECOND_VIDEO_STREAM_ID = 2;
    private static final int THIRD_VIDEO_STREAM_ID = 3;
    private static final int FOURTH_VIDEO_STREAM_ID = 4;
    private static final int FIFTH_VIDEO_STREAM_ID = 5;
    private static final int SIXTH_VIDEO_STREAM_ID = 6;

    // View models
    private UserViewModel mUserViewModel;
    private VideoStreamViewModel mVideoStreamViewModel;

    // Main layout
    private View mRootView;
    private ConstraintLayout mConstraintLayoutMain;
    private LinearLayout mLinearLayoutVideoStreamOneAndFour;
    private LinearLayout mLinearLayoutVideoStreamTwoAndFive;
    private LinearLayout mLinearLayoutVideoStreamThreeAndSix;
    private View mViewMarginOneAndFour;
    private View mViewMarginTwoAndFive;
    private View mViewMarginThreeAndSix;

    /**
     * -------------------- Exo player first video stream --------------------
     **/
    // First video stream toolbar
    private Spinner mSpinnerOneVideoStreamList;
    private ArrayAdapter mSpinnerVideoStreamAdapterOne;
    private List<String> mVideoStreamNameList;
    private List<VideoStreamModel> mVideoStreamModelList;

    // First video stream view
    private RelativeLayout mRelativeLayoutToolbarOne;
    private LinearLayout mLinearLayoutVideoStreamOne;
    private View mLayoutVideoStreamContainerOne;
    private ConstraintLayout mConstraintLayoutSurfaceViewOne;
    //    private PlayerView mSurfaceViewOne;
    private TextureView mSurfaceViewOne;
    //    private SurfaceHolder mSurfaceHolderOne;
    private C2OpenSansSemiBoldTextView mTvStreamOne;

    private SimpleExoPlayer mMediaPlayerOne;
    //    private LibVLC mLibVlcOne;
    private VideoStreamControllerView mVideoStreamControllerViewOne;
//    private MediaPlayer mMediaPlayerOne;

    private int mSpinnerOneSelectedPos;

    /**
     * -------------------- Exo player second video stream --------------------
     **/
    // Second video stream toolbar
    private Spinner mSpinnerTwoVideoStreamList;
    private ArrayAdapter mSpinnerVideoStreamAdapterTwo;

    // Second video stream view
    private RelativeLayout mRelativeLayoutToolbarTwo;
    private LinearLayout mLinearLayoutVideoStreamTwo;
    private View mLayoutVideoStreamContainerTwo;
    private ConstraintLayout mConstraintLayoutSurfaceViewTwo;
    private TextureView mSurfaceViewTwo;
    private C2OpenSansSemiBoldTextView mTvStreamTwo;

    private SimpleExoPlayer mMediaPlayerTwo;
    private VideoStreamControllerView mVideoStreamControllerViewTwo;

    private int mSpinnerTwoSelectedPos;

    /**
     * -------------------- Exo player third video stream --------------------
     **/
    // Third video stream toolbar
    private Spinner mSpinnerThreeVideoStreamList;
    private ArrayAdapter mSpinnerVideoStreamAdapterThree;

    // Third video stream view
    private RelativeLayout mRelativeLayoutToolbarThree;
    private LinearLayout mLinearLayoutVideoStreamThree;
    private View mLayoutVideoStreamContainerThree;
    private ConstraintLayout mConstraintLayoutSurfaceViewThree;
    private TextureView mSurfaceViewThree;
    private C2OpenSansSemiBoldTextView mTvStreamThree;

    private SimpleExoPlayer mMediaPlayerThree;
    private VideoStreamControllerView mVideoStreamControllerViewThree;

    private int mSpinnerThreeSelectedPos;

    /**
     * -------------------- Exo player fourth video stream --------------------
     **/
    // Fourth video stream toolbar
    private Spinner mSpinnerFourVideoStreamList;
    private ArrayAdapter mSpinnerVideoStreamAdapterFour;

    // Fourth video stream view
    private RelativeLayout mRelativeLayoutToolbarFour;
    private LinearLayout mLinearLayoutVideoStreamFour;
    private View mLayoutVideoStreamContainerFour;
    private ConstraintLayout mConstraintLayoutSurfaceViewFour;
    private TextureView mSurfaceViewFour;
    private C2OpenSansSemiBoldTextView mTvStreamFour;

    private SimpleExoPlayer mMediaPlayerFour;
    private VideoStreamControllerView mVideoStreamControllerViewFour;

    private int mSpinnerFourSelectedPos;

    /**
     * -------------------- Exo player fifth video stream --------------------
     **/
    // Fifth video stream toolbar
    private Spinner mSpinnerFiveVideoStreamList;
    private ArrayAdapter mSpinnerVideoStreamAdapterFive;

    // Fifth video stream view
    private RelativeLayout mRelativeLayoutToolbarFive;
    private LinearLayout mLinearLayoutVideoStreamFive;
    private View mLayoutVideoStreamContainerFive;
    private ConstraintLayout mConstraintLayoutSurfaceViewFive;
    private TextureView mSurfaceViewFive;
    private C2OpenSansSemiBoldTextView mTvStreamFive;

    private SimpleExoPlayer mMediaPlayerFive;
    private VideoStreamControllerView mVideoStreamControllerViewFive;

    private int mSpinnerFiveSelectedPos;

    /**
     * -------------------- Exo player sixth video stream --------------------
     **/
    // Sixth video stream toolbar
    private Spinner mSpinnerSixVideoStreamList;
    private ArrayAdapter mSpinnerVideoStreamAdapterSix;

    // Sixth video stream view
    private RelativeLayout mRelativeLayoutToolbarSix;
    private LinearLayout mLinearLayoutVideoStreamSix;
    private View mLayoutVideoStreamContainerSix;
    private ConstraintLayout mConstraintLayoutSurfaceViewSix;
    private TextureView mSurfaceViewSix;
    private C2OpenSansSemiBoldTextView mTvStreamSix;

    private SimpleExoPlayer mMediaPlayerSix;
    private VideoStreamControllerView mVideoStreamControllerViewSix;

    private int mSpinnerSixSelectedPos;

    /**
     * -------------------- Others --------------------
     **/
    private boolean mIsFragmentVisibleToUser;
    private boolean mIsFullscreen;

    // Screenshot
    Bitmap mBitmapTakenScreenshot;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setRetainInstance(true);

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_video_stream, container, false);
            observerSetup();
            initUI(mRootView);
        }

        return mRootView;
    }

    private void initUI(View rootVideoStreamView) {
        mConstraintLayoutMain = rootVideoStreamView.findViewById(R.id.layout_video_stream_fragment);

        initMainLayout(rootVideoStreamView);
        initVideoStreamOneUI(rootVideoStreamView);

        // Display more video stream panels if current user access right is 'CCT'
        mLinearLayoutVideoStreamTwo = rootVideoStreamView.findViewById(R.id.layout_video_stream_two);
        mLinearLayoutVideoStreamThree = rootVideoStreamView.findViewById(R.id.layout_video_stream_three);
        mLinearLayoutVideoStreamFour = rootVideoStreamView.findViewById(R.id.layout_video_stream_four);
        mLinearLayoutVideoStreamFour.setVisibility(View.GONE);
        mLinearLayoutVideoStreamFive = rootVideoStreamView.findViewById(R.id.layout_video_stream_five);
        mLinearLayoutVideoStreamSix = rootVideoStreamView.findViewById(R.id.layout_video_stream_six);

        if (!EAccessRight.CCT.toString().equalsIgnoreCase(SharedPreferenceUtil.getCurrentUserAccessRight())) {
            // Set appropriate dimension for video stream layout
            setVideoStreamDefaultLayoutForOthers(mLinearLayoutVideoStreamOneAndFour,
                    mLayoutVideoStreamContainerOne, mConstraintLayoutSurfaceViewOne);
        } else {
            // Set appropriate dimension for video stream layout
            setVideoStreamDefaultLayoutForCCT(mLinearLayoutVideoStreamOneAndFour,
                    mLayoutVideoStreamContainerOne, mConstraintLayoutSurfaceViewOne);

            mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.VISIBLE);
            mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.VISIBLE);
            initVideoStreamTwoUI(rootVideoStreamView);
            initVideoStreamThreeUI(rootVideoStreamView);
            initVideoStreamFourUI(rootVideoStreamView);
            initVideoStreamFiveUI(rootVideoStreamView);
            initVideoStreamSixUI(rootVideoStreamView);
        }

        initMediaPlayers();
    }

    private void initMainLayout(View rootVideoStreamView) {
        mLinearLayoutVideoStreamOneAndFour = rootVideoStreamView.findViewById(R.id.layout_video_stream_one_and_four);
        mLinearLayoutVideoStreamTwoAndFive = rootVideoStreamView.findViewById(R.id.layout_video_stream_two_and_five);
        mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.GONE);
        mLinearLayoutVideoStreamThreeAndSix = rootVideoStreamView.findViewById(R.id.layout_video_stream_three_and_six);
        mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.GONE);

        mViewMarginOneAndFour = rootVideoStreamView.findViewById(R.id.view_margin_one_four);
        mViewMarginTwoAndFive = rootVideoStreamView.findViewById(R.id.view_margin_two_five);
        mViewMarginThreeAndSix = rootVideoStreamView.findViewById(R.id.view_margin_three_six);
    }

    private void initVideoStreamOneUI(View rootVideoStreamView) {
        initVideoStreamOneToolbar(rootVideoStreamView);
        initSurfaceViewVideoOneStream(rootVideoStreamView);
    }

    /**
     * -------------------- Initialise first video stream UI --------------------
     **/
    private void initVideoStreamOneToolbar(View rootVideoStreamView) {
        mRelativeLayoutToolbarOne = rootVideoStreamView.findViewById(R.id.layout_video_stream_one_toolbar);
        initSettingUI(rootVideoStreamView);
        initVideoListOneSpinner(rootVideoStreamView);
    }

    private void initSettingUI(View rootVideoStreamView) {
        ImageView imgSetting = rootVideoStreamView.findViewById(R.id.img_btn_video_stream_setting);
        imgSetting.setOnClickListener(settingOnClickListener);
    }

    private void initVideoListOneSpinner(View rootVideoStreamView) {
        mSpinnerOneVideoStreamList = rootVideoStreamView.findViewById(R.id.spinner_video_stream_selector_one);

        mVideoStreamNameList = new ArrayList<>();
        mVideoStreamNameList.add(getString(R.string.video_stream_select_spinner_item));

        mSpinnerVideoStreamAdapterOne = getSpinnerArrayAdapter(mVideoStreamNameList, FIRST_VIDEO_STREAM_ID);

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

    private void initSurfaceViewVideoOneStream(View rootVideoStreamView) {
//        mFilePath = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";

        mLinearLayoutVideoStreamOne = rootVideoStreamView.findViewById(R.id.layout_video_stream_one);

        // Video Stream Container
        mLayoutVideoStreamContainerOne = rootVideoStreamView.findViewById(R.id.layout_video_stream_container_one);
        mConstraintLayoutSurfaceViewOne = mLayoutVideoStreamContainerOne.findViewById(R.id.constraint_layout_video_surface);
        mSurfaceViewOne = mLayoutVideoStreamContainerOne.findViewById(R.id.texture_view_stream);
        ProgressBar progressBar = mLayoutVideoStreamContainerOne.findViewById(R.id.spinner_stream);
        progressBar.setVisibility(View.GONE);
        mTvStreamOne = mLayoutVideoStreamContainerOne.
                findViewById(R.id.tv_stream_no_video_selected);

        mVideoStreamControllerViewOne = new VideoStreamControllerView(getContext(), false);
        mVideoStreamControllerViewOne.setMediaPlayer(videoStreamPlayerOneInterface);
        mMediaPlayerOne = ExoPlayerFactory.newSimpleInstance(getContext());
        mMediaPlayerOne.setVideoTextureView(mSurfaceViewOne);
        Player.EventListener playerListener = getPlayerEventListener(mTvStreamOne,
                progressBar);
        mMediaPlayerOne.addListener(playerListener);

        mVideoStreamControllerViewOne.setAnchorView(mConstraintLayoutSurfaceViewOne);
//        mSurfaceHolderOne = mSurfaceViewOne.getHolder();
    }

    /**
     * -------------------- Initialise second video stream UI --------------------
     **/
    private void initVideoStreamTwoUI(View rootVideoStreamView) {
        mLinearLayoutVideoStreamTwo.setVisibility(View.VISIBLE);
        initVideoStreamTwoToolbar(rootVideoStreamView);
        initSurfaceViewVideoTwoStream(rootVideoStreamView);
    }

    private void initVideoStreamTwoToolbar(View rootVideoStreamView) {
        mRelativeLayoutToolbarTwo = rootVideoStreamView.findViewById(R.id.layout_video_stream_two_toolbar);
        initVideoListTwoSpinner(rootVideoStreamView);
    }

    private void initVideoListTwoSpinner(View rootVideoStreamView) {
        mSpinnerTwoVideoStreamList = rootVideoStreamView.findViewById(R.id.spinner_video_stream_selector_two);

        mSpinnerVideoStreamAdapterTwo = getSpinnerArrayAdapter(mVideoStreamNameList, SECOND_VIDEO_STREAM_ID);

        mSpinnerTwoVideoStreamList.setAdapter(mSpinnerVideoStreamAdapterTwo);
        mSpinnerTwoVideoStreamList.setOnItemSelectedListener(videoStreamTwoSpinnerOnItemSelectedListener);
    }

    private Spinner.OnItemSelectedListener videoStreamTwoSpinnerOnItemSelectedListener =
            new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSpinnerTwoSelectedPos = position;
                    String selectedVideoName = (String) parent.getItemAtPosition(position);
                    getUrlStreamFromName(selectedVideoName, SECOND_VIDEO_STREAM_ID);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };

    private void initSurfaceViewVideoTwoStream(View rootVideoStreamView) {
        // Video Stream Container
        mLayoutVideoStreamContainerTwo = rootVideoStreamView.findViewById(R.id.layout_video_stream_container_two);
        mConstraintLayoutSurfaceViewTwo = mLayoutVideoStreamContainerTwo.findViewById(R.id.constraint_layout_video_surface);
        mSurfaceViewTwo = mLayoutVideoStreamContainerTwo.findViewById(R.id.texture_view_stream);
        ProgressBar progressBar = mLayoutVideoStreamContainerTwo.findViewById(R.id.spinner_stream);
        progressBar.setVisibility(View.GONE);
        mTvStreamTwo = mLayoutVideoStreamContainerTwo.
                findViewById(R.id.tv_stream_no_video_selected);

        mVideoStreamControllerViewTwo = new VideoStreamControllerView(getContext(), false);
        mMediaPlayerTwo = ExoPlayerFactory.newSimpleInstance(getContext());
        mMediaPlayerTwo.setVideoTextureView(mSurfaceViewTwo);

        VideoStreamControllerView.MediaPlayerControl videoStreamPlayerInterface =
                getMediaPlayerControlInterface(mMediaPlayerTwo, mSurfaceViewTwo,
                        mLinearLayoutVideoStreamTwoAndFive, mLayoutVideoStreamContainerTwo,
                        mConstraintLayoutSurfaceViewTwo, SECOND_VIDEO_STREAM_ID);
        mVideoStreamControllerViewTwo.setMediaPlayer(videoStreamPlayerInterface);

        Player.EventListener playerListener = getPlayerEventListener(mTvStreamTwo,
                progressBar);
        mMediaPlayerTwo.addListener(playerListener);

        mVideoStreamControllerViewTwo.setAnchorView(mConstraintLayoutSurfaceViewTwo);
    }

    /**
     * -------------------- Initialise third video stream UI --------------------
     **/
    private void initVideoStreamThreeUI(View rootVideoStreamView) {
        mLinearLayoutVideoStreamThree.setVisibility(View.VISIBLE);
        initVideoStreamThreeToolbar(rootVideoStreamView);
        initSurfaceViewVideoThreeStream(rootVideoStreamView);
    }

    private void initVideoStreamThreeToolbar(View rootVideoStreamView) {
        mRelativeLayoutToolbarThree = rootVideoStreamView.findViewById(R.id.layout_video_stream_three_toolbar);
        initVideoListThreeSpinner(rootVideoStreamView);
    }

    private void initVideoListThreeSpinner(View rootVideoStreamView) {
        mSpinnerThreeVideoStreamList = rootVideoStreamView.findViewById(R.id.spinner_video_stream_selector_three);

        mSpinnerVideoStreamAdapterThree = getSpinnerArrayAdapter(mVideoStreamNameList, THIRD_VIDEO_STREAM_ID);

        mSpinnerThreeVideoStreamList.setAdapter(mSpinnerVideoStreamAdapterThree);
        mSpinnerThreeVideoStreamList.setOnItemSelectedListener(videoStreamThreeSpinnerOnItemSelectedListener);
    }

    private Spinner.OnItemSelectedListener videoStreamThreeSpinnerOnItemSelectedListener =
            new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSpinnerThreeSelectedPos = position;
                    String selectedVideoName = (String) parent.getItemAtPosition(position);
                    getUrlStreamFromName(selectedVideoName, THIRD_VIDEO_STREAM_ID);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };

    private void initSurfaceViewVideoThreeStream(View rootVideoStreamView) {
        // Video Stream Container
        mLayoutVideoStreamContainerThree = rootVideoStreamView.findViewById(R.id.layout_video_stream_container_three);
        mConstraintLayoutSurfaceViewThree = mLayoutVideoStreamContainerThree.findViewById(R.id.constraint_layout_video_surface);
        mSurfaceViewThree = mLayoutVideoStreamContainerThree.findViewById(R.id.texture_view_stream);
        ProgressBar progressBar = mLayoutVideoStreamContainerThree.findViewById(R.id.spinner_stream);
        progressBar.setVisibility(View.GONE);
        mTvStreamThree = mLayoutVideoStreamContainerThree.
                findViewById(R.id.tv_stream_no_video_selected);

        mVideoStreamControllerViewThree = new VideoStreamControllerView(getContext(), false);
        mMediaPlayerThree = ExoPlayerFactory.newSimpleInstance(getContext());
        mMediaPlayerThree.setVideoTextureView(mSurfaceViewThree);

        VideoStreamControllerView.MediaPlayerControl videoStreamPlayerInterface =
                getMediaPlayerControlInterface(mMediaPlayerThree, mSurfaceViewThree,
                        mLinearLayoutVideoStreamThreeAndSix, mLayoutVideoStreamContainerThree,
                        mConstraintLayoutSurfaceViewThree, THIRD_VIDEO_STREAM_ID);
        mVideoStreamControllerViewThree.setMediaPlayer(videoStreamPlayerInterface);

        Player.EventListener playerListener = getPlayerEventListener(mTvStreamThree,
                progressBar);
        mMediaPlayerThree.addListener(playerListener);

        mVideoStreamControllerViewThree.setAnchorView(mConstraintLayoutSurfaceViewThree);
    }

    /**
     * -------------------- Initialise fourth video stream UI --------------------
     **/
    private void initVideoStreamFourUI(View rootVideoStreamView) {
        mLinearLayoutVideoStreamFour.setVisibility(View.VISIBLE);
        initVideoStreamFourToolbar(rootVideoStreamView);
        initSurfaceViewVideoFourStream(rootVideoStreamView);
    }

    private void initVideoStreamFourToolbar(View rootVideoStreamView) {
        mRelativeLayoutToolbarFour = rootVideoStreamView.findViewById(R.id.layout_video_stream_four_toolbar);
        initVideoListFourSpinner(rootVideoStreamView);
    }

    private void initVideoListFourSpinner(View rootVideoStreamView) {
        mSpinnerFourVideoStreamList = rootVideoStreamView.findViewById(R.id.spinner_video_stream_selector_four);

        mSpinnerVideoStreamAdapterFour = getSpinnerArrayAdapter(mVideoStreamNameList, FOURTH_VIDEO_STREAM_ID);

        mSpinnerFourVideoStreamList.setAdapter(mSpinnerVideoStreamAdapterFour);
        mSpinnerFourVideoStreamList.setOnItemSelectedListener(videoStreamFourSpinnerOnItemSelectedListener);
    }

    private Spinner.OnItemSelectedListener videoStreamFourSpinnerOnItemSelectedListener =
            new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSpinnerFourSelectedPos = position;
                    String selectedVideoName = (String) parent.getItemAtPosition(position);
                    getUrlStreamFromName(selectedVideoName, FOURTH_VIDEO_STREAM_ID);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };

    private void initSurfaceViewVideoFourStream(View rootVideoStreamView) {
        // Video Stream Container
        mLayoutVideoStreamContainerFour = rootVideoStreamView.findViewById(R.id.layout_video_stream_container_four);
        mConstraintLayoutSurfaceViewFour = mLayoutVideoStreamContainerFour.findViewById(R.id.constraint_layout_video_surface);
        mSurfaceViewFour = mLayoutVideoStreamContainerFour.findViewById(R.id.texture_view_stream);
        ProgressBar progressBar = mLayoutVideoStreamContainerFour.findViewById(R.id.spinner_stream);
        progressBar.setVisibility(View.GONE);
        mTvStreamFour = mLayoutVideoStreamContainerFour.
                findViewById(R.id.tv_stream_no_video_selected);

        mVideoStreamControllerViewFour = new VideoStreamControllerView(getContext(), false);
        mMediaPlayerFour = ExoPlayerFactory.newSimpleInstance(getContext());
        mMediaPlayerFour.setVideoTextureView(mSurfaceViewFour);

        VideoStreamControllerView.MediaPlayerControl videoStreamPlayerInterface =
                getMediaPlayerControlInterface(mMediaPlayerFour, mSurfaceViewFour,
                        mLinearLayoutVideoStreamOneAndFour, mLayoutVideoStreamContainerFour,
                        mConstraintLayoutSurfaceViewFour, FOURTH_VIDEO_STREAM_ID);
        mVideoStreamControllerViewFour.setMediaPlayer(videoStreamPlayerInterface);

        Player.EventListener playerListener = getPlayerEventListener(mTvStreamFour,
                progressBar);
        mMediaPlayerFour.addListener(playerListener);

        mVideoStreamControllerViewFour.setAnchorView(mConstraintLayoutSurfaceViewFour);
    }

    /**
     * -------------------- Initialise fifth video stream UI --------------------
     **/
    private void initVideoStreamFiveUI(View rootVideoStreamView) {
        mLinearLayoutVideoStreamFive.setVisibility(View.VISIBLE);
        initVideoStreamFiveToolbar(rootVideoStreamView);
        initSurfaceViewVideoFiveStream(rootVideoStreamView);
    }

    private void initVideoStreamFiveToolbar(View rootVideoStreamView) {
        mRelativeLayoutToolbarFive = rootVideoStreamView.findViewById(R.id.layout_video_stream_five_toolbar);
        initVideoListFiveSpinner(rootVideoStreamView);
    }

    private void initVideoListFiveSpinner(View rootVideoStreamView) {
        mSpinnerFiveVideoStreamList = rootVideoStreamView.findViewById(R.id.spinner_video_stream_selector_five);

        mSpinnerVideoStreamAdapterFive = getSpinnerArrayAdapter(mVideoStreamNameList, FIFTH_VIDEO_STREAM_ID);

        mSpinnerFiveVideoStreamList.setAdapter(mSpinnerVideoStreamAdapterFive);
        mSpinnerFiveVideoStreamList.setOnItemSelectedListener(videoStreamFiveSpinnerOnItemSelectedListener);
    }

    private Spinner.OnItemSelectedListener videoStreamFiveSpinnerOnItemSelectedListener =
            new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSpinnerFiveSelectedPos = position;
                    String selectedVideoName = (String) parent.getItemAtPosition(position);
                    getUrlStreamFromName(selectedVideoName, FIFTH_VIDEO_STREAM_ID);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };

    private void initSurfaceViewVideoFiveStream(View rootVideoStreamView) {
        // Video Stream Container
        mLayoutVideoStreamContainerFive = rootVideoStreamView.findViewById(R.id.layout_video_stream_container_five);
        mConstraintLayoutSurfaceViewFive = mLayoutVideoStreamContainerFive.findViewById(R.id.constraint_layout_video_surface);
        mSurfaceViewFive = mLayoutVideoStreamContainerFive.findViewById(R.id.texture_view_stream);
        ProgressBar progressBar = mLayoutVideoStreamContainerFive.findViewById(R.id.spinner_stream);
        progressBar.setVisibility(View.GONE);
        mTvStreamFive = mLayoutVideoStreamContainerFive.
                findViewById(R.id.tv_stream_no_video_selected);

        mVideoStreamControllerViewFive = new VideoStreamControllerView(getContext(), false);
        mMediaPlayerFive = ExoPlayerFactory.newSimpleInstance(getContext());
        mMediaPlayerFive.setVideoTextureView(mSurfaceViewFive);

        VideoStreamControllerView.MediaPlayerControl videoStreamPlayerInterface =
                getMediaPlayerControlInterface(mMediaPlayerFive, mSurfaceViewFive,
                        mLinearLayoutVideoStreamTwoAndFive, mLayoutVideoStreamContainerFive,
                        mConstraintLayoutSurfaceViewFive, FIFTH_VIDEO_STREAM_ID);
        mVideoStreamControllerViewFive.setMediaPlayer(videoStreamPlayerInterface);

        Player.EventListener playerListener = getPlayerEventListener(mTvStreamFive,
                progressBar);
        mMediaPlayerFive.addListener(playerListener);

        mVideoStreamControllerViewFive.setAnchorView(mConstraintLayoutSurfaceViewFive);
    }

    /**
     * -------------------- Initialise sixth video stream UI --------------------
     **/
    private void initVideoStreamSixUI(View rootVideoStreamView) {
        mLinearLayoutVideoStreamSix.setVisibility(View.VISIBLE);
        initVideoStreamSixToolbar(rootVideoStreamView);
        initSurfaceViewVideoSixStream(rootVideoStreamView);
    }

    private void initVideoStreamSixToolbar(View rootVideoStreamView) {
        mRelativeLayoutToolbarSix = rootVideoStreamView.findViewById(R.id.layout_video_stream_six_toolbar);
        initVideoListSixSpinner(rootVideoStreamView);
    }

    private void initVideoListSixSpinner(View rootVideoStreamView) {
        mSpinnerSixVideoStreamList = rootVideoStreamView.findViewById(R.id.spinner_video_stream_selector_six);

        mSpinnerVideoStreamAdapterSix = getSpinnerArrayAdapter(mVideoStreamNameList, SIXTH_VIDEO_STREAM_ID);

        mSpinnerSixVideoStreamList.setAdapter(mSpinnerVideoStreamAdapterSix);
        mSpinnerSixVideoStreamList.setOnItemSelectedListener(videoStreamSixSpinnerOnItemSelectedListener);
    }

    private Spinner.OnItemSelectedListener videoStreamSixSpinnerOnItemSelectedListener =
            new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    mSpinnerSixSelectedPos = position;
                    String selectedVideoName = (String) parent.getItemAtPosition(position);
                    getUrlStreamFromName(selectedVideoName, SIXTH_VIDEO_STREAM_ID);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };

    private void initSurfaceViewVideoSixStream(View rootVideoStreamView) {
        // Video Stream Container
        mLayoutVideoStreamContainerSix = rootVideoStreamView.findViewById(R.id.layout_video_stream_container_six);
        mConstraintLayoutSurfaceViewSix = mLayoutVideoStreamContainerSix.findViewById(R.id.constraint_layout_video_surface);
        mSurfaceViewSix = mLayoutVideoStreamContainerSix.findViewById(R.id.texture_view_stream);
        ProgressBar progressBar = mLayoutVideoStreamContainerSix.findViewById(R.id.spinner_stream);
        progressBar.setVisibility(View.GONE);
        mTvStreamSix = mLayoutVideoStreamContainerSix.
                findViewById(R.id.tv_stream_no_video_selected);

        mVideoStreamControllerViewSix = new VideoStreamControllerView(getContext(), false);
        mMediaPlayerSix = ExoPlayerFactory.newSimpleInstance(getContext());
        mMediaPlayerSix.setVideoTextureView(mSurfaceViewSix);

        VideoStreamControllerView.MediaPlayerControl videoStreamPlayerInterface =
                getMediaPlayerControlInterface(mMediaPlayerSix, mSurfaceViewSix,
                        mLinearLayoutVideoStreamThreeAndSix, mLayoutVideoStreamContainerSix,
                        mConstraintLayoutSurfaceViewSix, SIXTH_VIDEO_STREAM_ID);
        mVideoStreamControllerViewSix.setMediaPlayer(videoStreamPlayerInterface);

        Player.EventListener playerListener = getPlayerEventListener(mTvStreamSix,
                progressBar);
        mMediaPlayerSix.addListener(playerListener);

        mVideoStreamControllerViewSix.setAnchorView(mConstraintLayoutSurfaceViewSix);
    }

    private ArrayAdapter<String> getSpinnerArrayAdapter(List<String> stringArrayList,
                                                        int videoStreamNo) {
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

                switch (videoStreamNo) {
                    case FIRST_VIDEO_STREAM_ID:
                        setSelectedItemUI(mSpinnerOneSelectedPos, position, img, tv);
                        break;
                    case SECOND_VIDEO_STREAM_ID:
                        setSelectedItemUI(mSpinnerTwoSelectedPos, position, img, tv);
                        break;
                    case THIRD_VIDEO_STREAM_ID:
                        setSelectedItemUI(mSpinnerThreeSelectedPos, position, img, tv);
                        break;
                    case FOURTH_VIDEO_STREAM_ID:
                        setSelectedItemUI(mSpinnerFourSelectedPos, position, img, tv);
                        break;
                    case FIFTH_VIDEO_STREAM_ID:
                        setSelectedItemUI(mSpinnerFiveSelectedPos, position, img, tv);
                        break;
                    case SIXTH_VIDEO_STREAM_ID:
                        setSelectedItemUI(mSpinnerSixSelectedPos, position, img, tv);
                        break;
                    default:
                        break;
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

    private void setSelectedItemUI(int selectedPos, int currentPos,
                                   AppCompatImageView img, TextView tv) {
        if (selectedPos == currentPos) {
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

                                case SECOND_VIDEO_STREAM_ID:
                                    setVideoVLCStreamURL(videoUrl,
                                            mConstraintLayoutSurfaceViewTwo,
                                            mVideoStreamControllerViewTwo,
                                            mMediaPlayerTwo);
                                    break;

                                case THIRD_VIDEO_STREAM_ID:
                                    setVideoVLCStreamURL(videoUrl,
                                            mConstraintLayoutSurfaceViewThree,
                                            mVideoStreamControllerViewThree,
                                            mMediaPlayerThree);
                                    break;

                                case FOURTH_VIDEO_STREAM_ID:
                                    setVideoVLCStreamURL(videoUrl,
                                            mConstraintLayoutSurfaceViewFour,
                                            mVideoStreamControllerViewFour,
                                            mMediaPlayerFour);
                                    break;

                                case FIFTH_VIDEO_STREAM_ID:
                                    setVideoVLCStreamURL(videoUrl,
                                            mConstraintLayoutSurfaceViewFive,
                                            mVideoStreamControllerViewFive,
                                            mMediaPlayerFive);
                                    break;

                                case SIXTH_VIDEO_STREAM_ID:
                                    setVideoVLCStreamURL(videoUrl,
                                            mConstraintLayoutSurfaceViewSix,
                                            mVideoStreamControllerViewSix,
                                            mMediaPlayerSix);
                                    break;
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

    private void initMediaPlayers() {
        if (mSurfaceViewOne != null && mMediaPlayerOne == null) {
            // 1. Create a default TrackSelector
//            LoadControl loadControl = new DefaultLoadControl();
//
////            BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
//            TrackSelection.Factory videoTrackSelectionFactory =
//                    new AdaptiveTrackSelection.Factory();
//            TrackSelector trackSelector =
//                    new DefaultTrackSelector(videoTrackSelectionFactory);
////            // 2. Create the player
//            mMediaPlayerOne = ExoPlayerFactory.newSimpleInstance(getContext(),
//                    new DefaultRenderersFactory(getActivity()), trackSelector, loadControl);

            mMediaPlayerOne = ExoPlayerFactory.newSimpleInstance(getContext());
//            ExoPlayerFactory.newSimpleInstance(
//                    /* context= */ this, new DefaultRenderersFactory(getContext()), videoTrackSelectionFactory, null);
            mMediaPlayerOne.setVideoTextureView(mSurfaceViewOne);
//            mSurfaceViewOne.setPlayer(mMediaPlayerOne);
        }

        if (mSurfaceViewTwo != null && mMediaPlayerTwo == null) {
            mMediaPlayerTwo = ExoPlayerFactory.newSimpleInstance(getContext());
            mMediaPlayerTwo.setVideoTextureView(mSurfaceViewTwo);
        }

        if (mSurfaceViewThree != null && mMediaPlayerThree == null) {
            mMediaPlayerThree = ExoPlayerFactory.newSimpleInstance(getContext());
            mMediaPlayerThree.setVideoTextureView(mSurfaceViewThree);
        }

        if (mSurfaceViewFour != null && mMediaPlayerFour == null) {
            mMediaPlayerFour = ExoPlayerFactory.newSimpleInstance(getContext());
            mMediaPlayerFour.setVideoTextureView(mSurfaceViewFour);
        }

        if (mSurfaceViewFive != null && mMediaPlayerFive == null) {
            mMediaPlayerFive = ExoPlayerFactory.newSimpleInstance(getContext());
            mMediaPlayerFive.setVideoTextureView(mSurfaceViewFive);
        }

        if (mSurfaceViewSix != null && mMediaPlayerSix == null) {
            mMediaPlayerSix = ExoPlayerFactory.newSimpleInstance(getContext());
            mMediaPlayerSix.setVideoTextureView(mSurfaceViewSix);
        }
    }

    private VideoStreamControllerView.MediaPlayerControl videoStreamPlayerOneInterface =
            new VideoStreamControllerView.MediaPlayerControl() {
                public int getBufferPercentage() {
                    return 0;
                }

                public int getCurrentPosition() {
                    float pos = mMediaPlayerOne.getCurrentPosition();
//                    float pos = mMediaPlayerOne.getPosition();
                    return (int) (pos * getDuration());
                }

                public int getDuration() {
                    return (int) mMediaPlayerOne.getDuration();
                }

                public boolean isPlaying() {
                    return mMediaPlayerOne.getPlayWhenReady();
                }

                public void pause() {
                    pausePlayerOne();
                }

                public void takeScreenshot() {
//            Bitmap imageBitmap = screenShot(mSurfaceViewOne);
                    if (mBitmapTakenScreenshot != null) {
                        mBitmapTakenScreenshot.recycle();
                    }

                    mBitmapTakenScreenshot = mSurfaceViewOne.getBitmap();

                    if (mBitmapTakenScreenshot != null) {
                        String imagePath = PhotoCaptureUtil.insertImage(getActivity().getContentResolver(),
                                mBitmapTakenScreenshot, "Video_Streaming_Screenshot.jpg", "VLC screenshot");
//                System.out.println("imagePath is " + imagePath);

                        if (imagePath != null && getSnackbarView() != null) {
                            StringBuilder screenshotStringBuilder = new StringBuilder();
                            screenshotStringBuilder.append(getString(R.string.snackbar_screenshot_taken_message));
                            screenshotStringBuilder.append(System.lineSeparator());
                            screenshotStringBuilder.append(System.lineSeparator());
                            screenshotStringBuilder.append(getString(R.string.snackbar_screenshot_create_sitrep_message));

                            SnackbarUtil.showCustomAlertSnackbar(mConstraintLayoutMain, getSnackbarView(),
                                    screenshotStringBuilder.toString(), VideoStreamFragment.this);
                        }
                    }
                }

                public void seekTo(int pos) {
                    mMediaPlayerOne.seekTo(pos / getDuration());
                }

                public void start() {
                    resumePlayerOne();
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
                        setVideoStreamFullscreen(mLinearLayoutVideoStreamOneAndFour,
                                mLayoutVideoStreamContainerOne, mConstraintLayoutSurfaceViewOne);
                    } else {
                        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                                SharedPreferenceUtil.getCurrentUserAccessRight())) {
                            setVideoStreamDefaultScreenForOthers(mLinearLayoutVideoStreamOneAndFour,
                                    mLayoutVideoStreamContainerOne, mConstraintLayoutSurfaceViewOne);
                        } else {
                            setVideoStreamDefaultScreenForCCT(mLinearLayoutVideoStreamOneAndFour,
                                    mLayoutVideoStreamContainerOne, mConstraintLayoutSurfaceViewOne);
                        }
                    }

                    if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                            SharedPreferenceUtil.getCurrentUserAccessRight())) {
                        setOtherUiElementsForFirstVideoStreamForOthers(!isFullScreen());
                    } else {
                        setOtherUiElementsForVideoStreamsForCCT(!isFullScreen(),
                                FIRST_VIDEO_STREAM_ID);
                    }

                    mIsFullscreen = !mIsFullscreen;
                }
            };

    private synchronized VideoStreamControllerView.MediaPlayerControl getMediaPlayerControlInterface(
            SimpleExoPlayer mediaPlayer, TextureView textureView, LinearLayout linearLayout,
            View parentConstraintLayout, View childConstraintLayout, int streamNo) {

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
                        screenshotStringBuilder.append(getString(R.string.snackbar_screenshot_taken_message));
                        screenshotStringBuilder.append(System.lineSeparator());
                        screenshotStringBuilder.append(System.lineSeparator());
                        screenshotStringBuilder.append(getString(R.string.snackbar_screenshot_create_sitrep_message));

                        SnackbarUtil.showCustomAlertSnackbar(mConstraintLayoutMain, getSnackbarView(),
                                screenshotStringBuilder.toString(), VideoStreamFragment.this);
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
                    setVideoStreamFullscreen(linearLayout, parentConstraintLayout,
                            childConstraintLayout);
                } else {
                    setVideoStreamDefaultScreenForCCT(linearLayout, parentConstraintLayout,
                            childConstraintLayout);
                }

                setOtherUiElementsForVideoStreamsForCCT(!isFullScreen(), streamNo);
                mIsFullscreen = !mIsFullscreen;
            }
        };
    }


    // -------------------- Pause players -------------------- //
    private void pausePlayerOne() {
        if (mMediaPlayerOne != null) {
            mMediaPlayerOne.setPlayWhenReady(false);
        }
    }

    private void pausePlayerTwo() {
        if (mMediaPlayerTwo != null) {
            mMediaPlayerTwo.setPlayWhenReady(false);
        }
    }

    private void pausePlayerThree() {
        if (mMediaPlayerThree != null) {
            mMediaPlayerThree.setPlayWhenReady(false);
        }
    }

    private void pausePlayerFour() {
        if (mMediaPlayerFour != null) {
            mMediaPlayerFour.setPlayWhenReady(false);
        }
    }

    private void pausePlayerFive() {
        if (mMediaPlayerFive != null) {
            mMediaPlayerFive.setPlayWhenReady(false);
        }
    }

    private void pausePlayerSix() {
        if (mMediaPlayerSix != null) {
            mMediaPlayerSix.setPlayWhenReady(false);
        }
    }

    private void pausePlayers() {
        pausePlayerOne();
        pausePlayerTwo();
        pausePlayerThree();
        pausePlayerFour();
        pausePlayerFive();
        pausePlayerSix();
    }

    // -------------------- Resume players -------------------- //
    private void resumePlayerOne() {
        if (mMediaPlayerOne != null) {
            mMediaPlayerOne.setPlayWhenReady(true);
        }
    }

    private void resumePlayerTwo() {
        if (mMediaPlayerTwo != null) {
            mMediaPlayerTwo.setPlayWhenReady(true);
        }
    }

    private void resumePlayerThree() {
        if (mMediaPlayerThree != null) {
            mMediaPlayerThree.setPlayWhenReady(true);
        }
    }

    private void resumePlayerFour() {
        if (mMediaPlayerFour != null) {
            mMediaPlayerFour.setPlayWhenReady(true);
        }
    }

    private void resumePlayerFive() {
        if (mMediaPlayerFive != null) {
            mMediaPlayerFive.setPlayWhenReady(true);
        }
    }

    private void resumePlayerSix() {
        if (mMediaPlayerSix != null) {
            mMediaPlayerSix.setPlayWhenReady(true);
        }
    }

    private void resumePlayers() {
        resumePlayerOne();
        resumePlayerTwo();
        resumePlayerThree();
        resumePlayerFour();
        resumePlayerFive();
        resumePlayerSix();
    }

    // -------------------- Stop players -------------------- //
    private void stopPlayerOne(boolean reset) {
        if (mMediaPlayerOne != null) {
            mMediaPlayerOne.stop(reset);
        }
    }

    private void stopPlayerTwo(boolean reset) {
        if (mMediaPlayerTwo != null) {
            mMediaPlayerTwo.stop(reset);
        }
    }

    private void stopPlayerThree(boolean reset) {
        if (mMediaPlayerThree != null) {
            mMediaPlayerThree.stop(reset);
        }
    }

    private void stopPlayerFour(boolean reset) {
        if (mMediaPlayerFour != null) {
            mMediaPlayerFour.stop(reset);
        }
    }

    private void stopPlayerFive(boolean reset) {
        if (mMediaPlayerFive != null) {
            mMediaPlayerFive.stop(reset);
        }
    }

    private void stopPlayerSix(boolean reset) {
        if (mMediaPlayerSix != null) {
            mMediaPlayerSix.stop(reset);
        }
    }

    private void stopPlayers(boolean reset) {
        stopPlayerOne(reset);
        stopPlayerTwo(reset);
        stopPlayerThree(reset);
        stopPlayerFour(reset);
        stopPlayerFive(reset);
        stopPlayerSix(reset);
    }

    // -------------------- Release players -------------------- //
    private void releasePlayerOne() {
        if (mMediaPlayerOne != null) {
            mMediaPlayerOne.release();
            mMediaPlayerOne = null;
        }
    }

    private void releasePlayerTwo() {
        if (mMediaPlayerTwo != null) {
            mMediaPlayerTwo.release();
            mMediaPlayerTwo = null;
        }
    }

    private void releasePlayerThree() {
        if (mMediaPlayerThree != null) {
            mMediaPlayerThree.release();
            mMediaPlayerThree = null;
        }
    }

    private void releasePlayerFour() {
        if (mMediaPlayerFour != null) {
            mMediaPlayerFour.release();
            mMediaPlayerFour = null;
        }
    }

    private void releasePlayerFive() {
        if (mMediaPlayerFive != null) {
            mMediaPlayerFive.release();
            mMediaPlayerFive = null;
        }
    }

    private void releasePlayerSix() {
        if (mMediaPlayerSix != null) {
            mMediaPlayerSix.release();
            mMediaPlayerSix = null;
        }
    }

    private void releasePlayers() {
        releasePlayerOne();
        releasePlayerTwo();
        releasePlayerThree();
        releasePlayerFour();
        releasePlayerFive();
        releasePlayerSix();
    }

    // ---------------------------------------- Video Stream Layouts ---------------------------------------- //

    /**
     * Set default dimension layout for users other than CCT
     *
     * @param linearLayout
     * @param parentConstraintLayout
     * @param childConstraintLayout
     */
    private void setVideoStreamDefaultLayoutForOthers(LinearLayout linearLayout,
                                                      View parentConstraintLayout,
                                                      View childConstraintLayout) {
        DimensionUtil.setDimensions(linearLayout,
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                99,
                new LinearLayout(getContext()));

        DimensionUtil.setMargins(linearLayout,
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                0, 0, 0);

        DimensionUtil.setDimensions(parentConstraintLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                new LinearLayout(getContext()));

        DimensionUtil.setDimensions(childConstraintLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.video_stream_single_texture_view_height),
                new LinearLayout(getContext()));

//        ConstraintSet constraintSet = new ConstraintSet();
//        constraintSet.clone(constraintLayout);
//        constraintSet.connect(mConstraintLayoutSurfaceViewOne.getId(),
//                ConstraintSet.LEFT, mLinearLayoutVideoStreamOne.getId(), ConstraintSet.LEFT, 0);
//        constraintSet.connect(mConstraintLayoutSurfaceViewOne.getId(),
//                ConstraintSet.TOP, mLinearLayoutVideoStreamOne.getId(), ConstraintSet.TOP, 0);
//        constraintSet.connect(mConstraintLayoutSurfaceViewOne.getId(),
//                ConstraintSet.RIGHT, mLinearLayoutVideoStreamOne.getId(), ConstraintSet.RIGHT, 0);
//        constraintSet.connect(mConstraintLayoutSurfaceViewOne.getId(),
//                ConstraintSet.BOTTOM, mLinearLayoutVideoStreamOne.getId(), ConstraintSet.BOTTOM, 0);
//        constraintSet.applyTo(constraintLayout);
    }

    /**
     * Set default dimension layout for CCT users
     *
     * @param linearLayout
     * @param parentConstraintLayout
     * @param childConstraintLayout
     */
    private void setVideoStreamDefaultLayoutForCCT(LinearLayout linearLayout,
                                                   View parentConstraintLayout,
                                                   View childConstraintLayout) {
//        DimensionUtil.setDimensions(linearLayout,
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT,
//                new LinearLayout(getContext()));

        DimensionUtil.setDimensions(linearLayout,
                0, LinearLayout.LayoutParams.WRAP_CONTENT,
                33, new LinearLayout(getContext()));

        DimensionUtil.setMargins(linearLayout,
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                0, 0, 0);

        DimensionUtil.setDimensions(parentConstraintLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                new LinearLayout(getContext()));

        DimensionUtil.setDimensions(childConstraintLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.video_stream_six_texture_view_height),
                new LinearLayout(getContext()));
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
     * Sets given view's dimension to fullscreen
     *
     * @param view
     */
    private void setLayoutFullScreen(View view) {
        DimensionUtil.setDimensions(view,
                DimensionUtil.getScreenWidth(),
                DimensionUtil.getScreenHeightWithoutNavAndStatusBar(),
                new LinearLayout(getContext()));

        DimensionUtil.setMargins(view, 0, 0, 0, 0);
    }

    /**
     * Sets video stream layout to full screen
     *
     * @param linearLayout
     * @param childConstraintLayout
     */
    private void setVideoStreamFullscreen(LinearLayout linearLayout, View parentConstraintLayout,
                                          View childConstraintLayout) {

        mConstraintLayoutMain.setPadding(0, 0, 0, 0);

        setLayoutFullScreen(linearLayout);
        setLayoutFullScreen(parentConstraintLayout);
        setLayoutFullScreen(childConstraintLayout);

        setMainActivityComponents(View.GONE);
    }

    /**
     * Set default screen dimension for users other than CCT
     *
     * @param linearLayout
     * @param parentConstraintLayout
     * @param childConstraintLayout
     */
    private void setVideoStreamDefaultScreenForOthers(LinearLayout linearLayout,
                                                      View parentConstraintLayout,
                                                      View childConstraintLayout) {

        mConstraintLayoutMain.setPadding(0,
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing));

        setMainActivityComponents(View.VISIBLE);
        setVideoStreamDefaultLayoutForOthers(linearLayout, parentConstraintLayout,
                childConstraintLayout);
    }

    /**
     * Sets default screen dimension for CCT users
     *
     * @param linearLayout
     * @param parentConstraintLayout
     * @param childConstraintLayout
     */
    private void setVideoStreamDefaultScreenForCCT(LinearLayout linearLayout,
                                                   View parentConstraintLayout,
                                                   View childConstraintLayout) {

        mConstraintLayoutMain.setPadding(0,
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing));

        setMainActivityComponents(View.VISIBLE);
        setVideoStreamDefaultLayoutForCCT(linearLayout, parentConstraintLayout,
                childConstraintLayout);
    }

    /**
     * Sets other UI layouts accordingly based on default or fullscreen,
     * other than those that are included in the first video stream layouts;
     * This is for users other than CCT
     *
     * @param isFullScreen
     */
    private void setOtherUiElementsForFirstVideoStreamForOthers(boolean isFullScreen) {
        if (isFullScreen) {
            mRelativeLayoutToolbarOne.setVisibility(View.GONE);
        } else {
            mRelativeLayoutToolbarOne.setVisibility(View.VISIBLE);
        }
    }

//    /**
//     * Sets other UI layouts accordingly based on default or fullscreen,
//     * other than those that are included in the first video stream layouts;
//     * This is for CCT users
//     *
//     * @param isFullScreen
//     */
//    private void setOtherUiElementsForFirstVideoStreamForCCT(boolean isFullScreen) {
//        if (isFullScreen) {
//            mRelativeLayoutToolbarOne.setVisibility(View.GONE);
//            mLinearLayoutVideoStreamTwo.setVisibility(View.GONE);
//        } else {
//            mRelativeLayoutToolbarOne.setVisibility(View.VISIBLE);
//            mLinearLayoutVideoStreamTwo.setVisibility(View.VISIBLE);
//        }
//    }

    /**
     * Sets other UI layouts accordingly based on default or fullscreen,
     * other than those that are included in the second video stream layouts;
     * This is for CCT users
     *
     * @param isFullScreen
     * @param streamNo
     */
    private void setOtherUiElementsForVideoStreamsForCCT(boolean isFullScreen, int streamNo) {
        if (isFullScreen) {
            switch (streamNo) {
                case FIRST_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarOne.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamFour.setVisibility(View.GONE);
                    mViewMarginOneAndFour.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.GONE);
                    break;

                case SECOND_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarTwo.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamFive.setVisibility(View.GONE);
                    mViewMarginTwoAndFive.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.GONE);
                    break;

                case THIRD_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarThree.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamSix.setVisibility(View.GONE);
                    mViewMarginThreeAndSix.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.GONE);
                    break;

                case FOURTH_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarFour.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamOne.setVisibility(View.GONE);
                    mViewMarginOneAndFour.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.GONE);
                    break;

                case FIFTH_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarFive.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamTwo.setVisibility(View.GONE);
                    mViewMarginTwoAndFive.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.GONE);
                    break;

                case SIXTH_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarSix.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamThree.setVisibility(View.GONE);
                    mViewMarginThreeAndSix.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.GONE);
                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.GONE);
                    break;
            }
        } else {
            switch (streamNo) {
                case FIRST_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarOne.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamFour.setVisibility(View.VISIBLE);
                    mViewMarginOneAndFour.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.VISIBLE);
                    break;

                case SECOND_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarTwo.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamFive.setVisibility(View.VISIBLE);
                    mViewMarginTwoAndFive.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.VISIBLE);
                    break;

                case THIRD_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarThree.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamSix.setVisibility(View.VISIBLE);
                    mViewMarginThreeAndSix.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.VISIBLE);
                    break;

                case FOURTH_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarFour.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamOne.setVisibility(View.VISIBLE);
                    mViewMarginOneAndFour.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.VISIBLE);
                    break;

                case FIFTH_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarFive.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamTwo.setVisibility(View.VISIBLE);
                    mViewMarginTwoAndFive.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamThreeAndSix.setVisibility(View.VISIBLE);
                    break;

                case SIXTH_VIDEO_STREAM_ID:
                    mRelativeLayoutToolbarSix.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamThree.setVisibility(View.VISIBLE);
                    mViewMarginThreeAndSix.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamOneAndFour.setVisibility(View.VISIBLE);
                    mLinearLayoutVideoStreamTwoAndFive.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private View.OnClickListener settingOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Fragment videoStreamAddFragment = new VideoStreamAddFragment();
            navigateToFragment(videoStreamAddFragment);
        }
    };

    private Bitmap screenShot(View view) {
        if (view != null) {
            Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
                    view.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            view.draw(canvas);
            return bitmap;
        }
        return null;
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
                Util.getUserAgent(getContext(), getString(R.string.app_name)), bandwidthMeter);

//        DataSource.Factory dataSourceFactory = new DefaultHttpDataSourceFactory("exoplayer-codelab");

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
                        tvStream.setText(getString(R.string.video_stream_stream_ended));
                        break;
                    case Player.STATE_IDLE:
                        tvStream.setVisibility(View.VISIBLE);
                        tvStream.setText(getString(R.string.video_stream_no_video_selected));
                        progressBar.setVisibility(View.GONE);

                        if (tvStream == mTvStreamOne && mSpinnerOneSelectedPos != 0) {
                            tvStream.setText(getString(R.string.video_stream_unable_to_load));
                        }

                        if (tvStream == mTvStreamTwo && mSpinnerTwoSelectedPos != 0) {
                            tvStream.setText(getString(R.string.video_stream_unable_to_load));
                        }

                        if (tvStream == mTvStreamThree && mSpinnerThreeSelectedPos != 0) {
                            tvStream.setText(getString(R.string.video_stream_unable_to_load));
                        }

                        if (tvStream == mTvStreamFour && mSpinnerFourSelectedPos != 0) {
                            tvStream.setText(getString(R.string.video_stream_unable_to_load));
                        }

                        if (tvStream == mTvStreamFive && mSpinnerFiveSelectedPos != 0) {
                            tvStream.setText(getString(R.string.video_stream_unable_to_load));
                        }

                        if (tvStream == mTvStreamSix && mSpinnerSixSelectedPos != 0) {
                            tvStream.setText(getString(R.string.video_stream_unable_to_load));
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
                tvStream.setVisibility(View.VISIBLE);
                tvStream.setText(getString(R.string.video_stream_unable_to_load));
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

//    /**
//     * Registering callbacks for VLC Player One
//     */
//    private MediaPlayer.EventListener mPlayerOneListener = new MyPlayerOneListener(this);
//
//    private static class MyPlayerOneListener implements MediaPlayer.EventListener {
//        private WeakReference<VideoStreamFragment> mOwner;
//
//        public MyPlayerOneListener(VideoStreamFragment owner) {
//            mOwner = new WeakReference<>(owner);
//        }
//
//        @Override
//        public void onEvent(MediaPlayer.Event event) {
//            VideoStreamFragment player = mOwner.get();
//
//            switch (event.type) {
//                case MediaPlayer.Event.EndReached:
//                    Log.d(TAG, "MediaPlayerOne EndReached");
//                    player.releasePlayerOne();
//                    break;
//                case MediaPlayer.Event.Playing:
//                case MediaPlayer.Event.Paused:
//                case MediaPlayer.Event.Stopped:
//                default:
//                    break;
//            }
//        }
//    }
//
//    /**
//     * Registering callbacks for VLC Player Two
//     */
//    private MediaPlayer.EventListener mPlayerTwoListener = new MyPlayerTwoListener(this);
//
//    private static class MyPlayerTwoListener implements MediaPlayer.EventListener {
//        private WeakReference<VideoStreamFragment> mOwner;
//
//        public MyPlayerTwoListener(VideoStreamFragment owner) {
//            mOwner = new WeakReference<>(owner);
//        }
//
//        @Override
//        public void onEvent(MediaPlayer.Event event) {
//            VideoStreamFragment player = mOwner.get();
//
//            switch (event.type) {
//                case MediaPlayer.Event.EndReached:
//                    Log.d(TAG, "MediaPlayerTwo EndReached");
//                    player.releasePlayerTwo();
//                    break;
//                case MediaPlayer.Event.Playing:
//                case MediaPlayer.Event.Paused:
//                case MediaPlayer.Event.Stopped:
//                default:
//                    break;
//            }
//        }
//    }

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

            currentVideoStreamNameList.add(getString(R.string.video_stream_select_spinner_item));
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

                        if (EAccessRight.CCT.toString().equalsIgnoreCase(
                                SharedPreferenceUtil.getCurrentUserAccessRight()) ||
                                (mSpinnerVideoStreamAdapterTwo != null ||
                                        mSpinnerVideoStreamAdapterThree != null ||
                                        mSpinnerVideoStreamAdapterFour != null ||
                                        mSpinnerVideoStreamAdapterFive != null ||
                                        mSpinnerVideoStreamAdapterSix != null)) {
                            mSpinnerVideoStreamAdapterTwo.notifyDataSetChanged();
                            mSpinnerVideoStreamAdapterThree.notifyDataSetChanged();
                            mSpinnerVideoStreamAdapterFour.notifyDataSetChanged();
                            mSpinnerVideoStreamAdapterFive.notifyDataSetChanged();
                            mSpinnerVideoStreamAdapterSix.notifyDataSetChanged();

                            if (mVideoStreamNameList.size() > SIXTH_VIDEO_STREAM_ID) {
                                mSpinnerSixVideoStreamList.setSelection(SIXTH_VIDEO_STREAM_ID);
                            } else {
                                mSpinnerSixSelectedPos = 0;
                                mSpinnerSixVideoStreamList.setSelection(0);
                                stopPlayerSix(true);
                                mTvStreamSix.setText(getString(R.string.video_stream_no_video_selected));
                            }

                            if (mVideoStreamNameList.size() > FIFTH_VIDEO_STREAM_ID) {
                                mSpinnerFiveVideoStreamList.setSelection(FIFTH_VIDEO_STREAM_ID);
                            } else {
                                mSpinnerFiveSelectedPos = 0;
                                mSpinnerFiveVideoStreamList.setSelection(0);
                                stopPlayerFive(true);
                                mTvStreamFive.setText(getString(R.string.video_stream_no_video_selected));
                            }

                            if (mVideoStreamNameList.size() > FOURTH_VIDEO_STREAM_ID) {
                                mSpinnerFourVideoStreamList.setSelection(FOURTH_VIDEO_STREAM_ID);
                            } else {
                                mSpinnerFourSelectedPos = 0;
                                mSpinnerFourVideoStreamList.setSelection(0);
                                stopPlayerFour(true);
                                mTvStreamFour.setText(getString(R.string.video_stream_no_video_selected));
                            }

                            if (mVideoStreamNameList.size() > THIRD_VIDEO_STREAM_ID) {
                                mSpinnerThreeVideoStreamList.setSelection(THIRD_VIDEO_STREAM_ID);
                            } else {
                                mSpinnerThreeSelectedPos = 0;
                                mSpinnerThreeVideoStreamList.setSelection(0);
                                stopPlayerThree(true);
                                mTvStreamThree.setText(getString(R.string.video_stream_no_video_selected));
                            }

                            if (mVideoStreamNameList.size() > SECOND_VIDEO_STREAM_ID) {
                                mSpinnerTwoVideoStreamList.setSelection(SECOND_VIDEO_STREAM_ID);
                            } else {
                                mSpinnerTwoSelectedPos = 0;
                                mSpinnerTwoVideoStreamList.setSelection(0);
                                stopPlayerTwo(true);
                                mTvStreamTwo.setText(getString(R.string.video_stream_no_video_selected));
                            }
                        }

                        if (mVideoStreamNameList.size() > FIRST_VIDEO_STREAM_ID) {
                            mSpinnerOneVideoStreamList.setSelection(FIRST_VIDEO_STREAM_ID);
                        } else {
                            mSpinnerOneSelectedPos = 0;
                            mSpinnerOneVideoStreamList.setSelection(0);
                            stopPlayerOne(true);
                            mTvStreamOne.setText(getString(R.string.video_stream_no_video_selected));
                        }
                    }
                });
    }

    private void navigateToFragment(Fragment toFragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateWithAnimatedTransitionToFragment(
                    R.id.layout_video_stream_fragment, this, toFragment);
        }
    }

    /**
     * Properly releases all resources related to / referenced by fragment (itself)
     * when main (parent) activity is destroyed
     *
     */
    public void destroySelf() {
        releasePlayers();
    }

    /**
     * Pops back stack of ONLY current tab
     *
     * @return
     */
    public boolean popBackStack() {
        if (!isAdded())
            return false;

        if (getChildFragmentManager().getBackStackEntryCount() > 0) {
            getChildFragmentManager().popBackStackImmediate();
            return true;
        } else
            return false;
    }

    private void onVisible() {
        Log.d(TAG, "onVisible");
//        initMediaPlayers();
//        resumePlayers();
    }

    private void onInvisible() {
        Log.d(TAG, "onInvisible");
//        hideKeyboard();

//        pausePlayers();

//        if (Util.SDK_INT <= 23) {
//            releasePlayer();
//        }
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
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
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
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIsFragmentVisibleToUser) {
            onInvisible();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy");
    }
}
