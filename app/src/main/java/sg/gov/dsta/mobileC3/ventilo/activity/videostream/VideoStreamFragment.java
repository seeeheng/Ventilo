package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

//import com.rvirin.onvif.onvifcamera.OnvifDevice;
//import com.rvirin.onvif.onvifcamera.OnvifListener;
//import com.rvirin.onvif.onvifcamera.OnvifRequest;
//import com.rvirin.onvif.onvifcamera.OnvifResponse;
//
//import org.jetbrains.annotations.NotNull;

//import com.google.android.exoplayer2.source.hls.HlsMediaSource;
//import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
//import com.google.android.exoplayer2.util.Util;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.util.CustomKeyboardUtil;
import sg.gov.dsta.mobileC3.ventilo.util.ReportSpinnerBank;
import sg.gov.dsta.mobileC3.ventilo.util.ValidationUtil;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansBlackButton;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansItalicLightEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;

public class VideoStreamFragment extends Fragment implements IVLCVout.Callback {
// implements OnvifListener {

    private static final String TAG = "VideoStreamFragment";

    private ImageView mImgSetting;

    // VLC
    private LibVLC libVlcOne;
    private MediaPlayer mMediaPlayerOne = null;
    private SurfaceView mSurfaceViewOne;
    private SurfaceHolder mSurfaceHolderOne;
//    private int mVideoWidthOne;
//    private int mVideoHeightOne;

    private LibVLC libVlcTwo;
    private MediaPlayer mMediaPlayerTwo = null;
    private SurfaceView mSurfaceViewTwo;
    private SurfaceHolder mSurfaceHolderTwo;
//    private int mVideoWidthTwo;
//    private int mVideoHeightTwo;

    // ExoPlayer
//    private PlayerView playerView;
//    private SimpleExoPlayer player;
//    private ProgressBar loading;
//    private boolean playWhenReady = true;
//    private int currentWindow = 0;
//    private long playbackPosition = 0;

    private VideoView mVideoViewOne;
    private VideoView mVideoViewTwo;
    private Spinner mSpinnerFirstVideoStreamList;

    private C2OpenSansItalicLightEditTextView mEtvFirstVideoURLLink;
    private C2OpenSansItalicLightEditTextView mEtvSecondVideoURLLink;
    //    private LinearLayout mLinearLayoutFirstVideoConfirm;
//    private LinearLayout mLinearLayoutSecondVideoConfirm;
    private C2OpenSansBlackButton mImgBtnFirstVideoStream;
    private C2OpenSansBlackButton mImgBtnSecondVideoStream;
    private C2OpenSansBlackButton mImgBtnFirstVideoEdit;
    private C2OpenSansBlackButton mImgBtnSecondVideoEdit;

    private MediaController mMediaControllerOne;
    private MediaController mMediaControllerTwo;

//    private OnvifDevice mMyDevice;

    private boolean mIsVisibleToUser;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootVideoStreamView = inflater.inflate(R.layout.fragment_video_stream, container, false);
        initUI(rootVideoStreamView);

//        mMyDevice = new OnvifDevice("192.168.43.194:6608", "", "");
//        mMyDevice.setListener(this);

        return rootVideoStreamView;
    }

    private void initUI(View rootVideoStreamView) {
        initSettingUI(rootVideoStreamView);
        initURLLinkUI(rootVideoStreamView);
//        initVideoOneStream(rootVideoStreamView);
//        initVideoTwoStream(rootVideoStreamView);

        initSpinners(rootVideoStreamView);

//        initExoVideoOneStream(rootVideoStreamView);
        initSurfaceViewVideoOneStream(rootVideoStreamView);
        initSurfaceViewVideoTwoStream(rootVideoStreamView);
    }

    private void initSettingUI(View rootVideoStreamView) {
        mImgSetting = rootVideoStreamView.findViewById(R.id.img_btn_video_stream_setting);
        mImgSetting.setOnClickListener(settingOnClickListener);
    }

    private void initSpinners(View rootVideoStreamView) {
        initFirstVideoListSpinner(rootVideoStreamView);
    }

    private void initFirstVideoListSpinner(View rootVideoStreamView) {
        mSpinnerFirstVideoStreamList = rootVideoStreamView.findViewById(R.id.spinner_video_stream_selector);
        String[] locationStringArray = ReportSpinnerBank.getInstance(getActivity()).getLocationList();

        ArrayAdapter adapter = new ArrayAdapter(getActivity(),
                R.layout.spinner_row_first_video_stream_list, R.id.text_item_first_video_stream, locationStringArray) {

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
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = view.findViewById(R.id.text_item_first_video_stream);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_text_hint_dark_grey, null));
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_text_white, null));
                }
                return view;
            }
        };

        mSpinnerFirstVideoStreamList.setAdapter(adapter);
//        mSpinnerFirstVideoStreamList.setOnItemSelectedListener(getLocationSpinnerItemSelectedListener);
    }



//    private void initExoVideoOneStream(View rootVideoStreamView) {
//        playerView = rootVideoStreamView.findViewById(R.id.video_view_stream_one_exo);
//        loading = rootVideoStreamView.findViewById(R.id.loading);
//
//        //--------------------------------------
//        //Creating default track selector
//        //and init the player
//        TrackSelection.Factory adaptiveTrackSelection = new AdaptiveTrackSelection.Factory(new DefaultBandwidthMeter());
//        player = ExoPlayerFactory.newSimpleInstance(
//                new DefaultRenderersFactory(getActivity().getApplicationContext()),
//                new DefaultTrackSelector(adaptiveTrackSelection),
//                new DefaultLoadControl());
//
//        //init the player
//        playerView.setPlayer(player);
//    }

    private void initSurfaceViewVideoOneStream(View rootVideoStreamView) {
//        mFilePath = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov";

//        Log.d(TAG, "Playing: " + mFilePath);
        mSurfaceViewOne = rootVideoStreamView.findViewById(R.id.surface_view_stream_one);
        mSurfaceHolderOne = mSurfaceViewOne.getHolder();
    }

    private void initSurfaceViewVideoTwoStream(View rootVideoStreamView) {
        mSurfaceViewTwo = rootVideoStreamView.findViewById(R.id.surface_view_stream_two);
        mSurfaceHolderTwo = mSurfaceViewTwo.getHolder();
    }

    private void initURLLinkUI(View rootVideoStreamView) {
        mEtvFirstVideoURLLink = rootVideoStreamView.findViewById(R.id.etv_first_video_url_title_detail);
//        mEtvFirstVideoURLLink.addTextChangedListener(firstVideoTextWatcher);

//        mLinearLayoutFirstVideoConfirm = rootVideoStreamView.findViewById(R.id.layout_first_video_confirm);
        mImgBtnFirstVideoStream = rootVideoStreamView.findViewById(R.id.btn_first_video_stream_link);
        mImgBtnFirstVideoStream.setOnClickListener(firstVideoStreamOnClickListener);
        mImgBtnFirstVideoEdit = rootVideoStreamView.findViewById(R.id.btn_first_video_edit_link);
        mImgBtnFirstVideoEdit.setOnClickListener(firstVideoEditOnClickListener);

        mEtvSecondVideoURLLink = rootVideoStreamView.findViewById(R.id.etv_second_video_url_title_detail);
//        mEtvSecondVideoURLLink.addTextChangedListener(secondVideoTextWatcher);
//        mLinearLayoutSecondVideoConfirm = rootVideoStreamView.findViewById(R.id.layout_second_video_confirm);
        mImgBtnSecondVideoStream = rootVideoStreamView.findViewById(R.id.btn_second_video_stream_link);
        mImgBtnSecondVideoStream.setOnClickListener(secondVideoStreamOnClickListener);
        mImgBtnSecondVideoEdit = rootVideoStreamView.findViewById(R.id.btn_second_video_edit_link);
        mImgBtnSecondVideoEdit.setOnClickListener(secondVideoEditOnClickListener);
    }

//    private TextWatcher firstVideoTextWatcher = new TextWatcher() {
//        @Override
//        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//        }
//
//        @Override
//        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            mLinearLayoutFirstVideoConfirm.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        public void afterTextChanged(Editable editable) {
//        }
//    };

//    private TextWatcher secondVideoTextWatcher = new TextWatcher() {
//        @Override
//        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//        }
//
//        @Override
//        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            mLinearLayoutSecondVideoConfirm.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        public void afterTextChanged(Editable editable) {
//        }
//    };

    private View.OnClickListener settingOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Fragment videoStreamAddFragment = new VideoStreamAddFragment();

            FragmentManager fm = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_to_right, R.anim.slide_in_from_right, R.anim.slide_out_to_right);
            ft.replace(R.id.layout_video_stream_fragment, videoStreamAddFragment, videoStreamAddFragment.getClass().getSimpleName());
            ft.addToBackStack(videoStreamAddFragment.getClass().getSimpleName());
            ft.commit();
        }
    };

    private View.OnClickListener firstVideoStreamOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (ValidationUtil.validateEditTextField(mEtvFirstVideoURLLink,
                    getString(R.string.error_empty_video_url_detail))) {

                String videoURL = mEtvFirstVideoURLLink.getText().toString().trim();

//                setVideoOneStreamURL(videoURL);
//                setVideoOneExoStreamURL(videoURL);
                setVideoOneVLCStreamURL(videoURL);
//                mLinearLayoutFirstVideoConfirm.setVisibility(View.GONE);
                editFirstVideoSetUp();

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = pref.edit();

                editor.putString(SharedPreferenceConstants.SUB_HEADER_FIRST_VIDEO_URL, videoURL);
                editor.apply();

                hideKeyboard();
            }
        }
    };

    private View.OnClickListener firstVideoEditOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            initFirstVideoSetUp();
        }
    };

    private View.OnClickListener secondVideoStreamOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (ValidationUtil.validateEditTextField(mEtvSecondVideoURLLink,
                    getString(R.string.error_empty_video_url_detail))) {
                String videoURL = mEtvSecondVideoURLLink.getText().toString().trim();

//                setVideoTwoStreamURL(videoURL);
                setVideoTwoVLCStreamURL(videoURL);
//                mLinearLayoutSecondVideoConfirm.setVisibility(View.GONE);
                editSecondVideoSetUp();

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
                SharedPreferences.Editor editor = pref.edit();

                editor.putString(SharedPreferenceConstants.SUB_HEADER_SECOND_VIDEO_URL, videoURL);
                editor.apply();

                hideKeyboard();
            }
        }
    };

    private View.OnClickListener secondVideoEditOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            initSecondVideoSetUp();
        }
    };

//    private void initVideoOneStream(View rootVideoStreamView) {
//        mVideoViewOne = rootVideoStreamView.findViewById(R.id.video_view_stream_one);
//        mMediaControllerOne = new MediaController(getActivity());
//        mVideoViewOne.setMediaController(mMediaControllerOne);
//    }
//
//    private void initVideoTwoStream(View rootVideoStreamView) {
//        mVideoViewTwo = rootVideoStreamView.findViewById(R.id.video_view_stream_two);
//        mMediaControllerTwo = new MediaController(getActivity());
//        mVideoViewTwo.setMediaController(mMediaControllerTwo);
//    }

    // Test URL is "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov"
//    private void setVideoOneStreamURL(String urlLink) {
//        try {
//            Uri video = Uri.parse(urlLink);
//
//            mVideoViewOne.setVideoURI(video);
//
//            mVideoViewOne.post(new Runnable() {
//                @Override
//                public void run() {
////                    mMediaControllerOne.show(0);
//                }
//            });
//        } catch (RuntimeException e) {
//            Log.d(TAG, "Video Stream One - Invalid URI");
//        }
//
//        mVideoViewOne.start();
//    }

    // Test URL is "http://archive.org/download/SampleMpeg4_201307/sample_mpeg4.mp4"
//    private void setVideoTwoStreamURL(String urlLink) {
//        mMediaControllerTwo = new MediaController(getActivity());
//        mVideoViewTwo.setMediaController(mMediaControllerTwo);
//
//        try {
//            Uri video = Uri.parse(urlLink);
//            mVideoViewTwo.setVideoURI(video);
//
//            mVideoViewTwo.post(new Runnable() {
//                @Override
//                public void run() {
////                    mMediaControllerTwo.show(0);
//                }
//            });
//        } catch (RuntimeException e) {
//            Log.d(TAG, "Video Stream Two - Invalid URI");
//        }
//
//        mVideoViewTwo.start();
//    }

//    private void setVideoOneExoStreamURL(String urlLink) {
////-------------------------------------------------
//        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
//        // Produces DataSource instances through which media data is loaded.
//        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity().getApplicationContext(),
//                Util.getUserAgent(getActivity().getApplicationContext(), "Exo2"), defaultBandwidthMeter);
//
//        DataSource.Factory manifestDataSourceFactory =
//                new DefaultHttpDataSourceFactory("ua");
//        DashChunkSource.Factory dashChunkSourceFactory =
//                new DefaultDashChunkSource.Factory(
//                        new DefaultHttpDataSourceFactory("ua", new DefaultBandwidthMeter()));
//
////        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(getActivity().getApplicationContext(),
////                Util.getUserAgent(getActivity().getApplicationContext(), "Exo2"), defaultBandwidthMeter);
//
//        //-----------------------------------------------
//        //Create media source
////        String hls_url = "YOUR STREAMING URL HERE";
//        Uri uri = Uri.parse(urlLink);
//        Handler mainHandler = new Handler();
////        MediaSource mediaSource = new SsMediaSource(manifestDataSourceFactory,
////                dataSourceFactory, mainHandler, null);
//        MediaSource mediaSource = new DashMediaSource.Factory(dashChunkSourceFactory,
//                dataSourceFactory).createMediaSource(uri);
//
//        player.prepare(mediaSource);
//
//
//        player.setPlayWhenReady(playWhenReady);
//        player.addListener(new Player.EventListener() {
//            @Override
//            public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
//
//            }
//
//            @Override
//            public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
//
//            }
//
//            @Override
//            public void onLoadingChanged(boolean isLoading) {
//
//            }
//
//            @Override
//            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
//                switch (playbackState) {
//                    case Player.STATE_READY:
//                        loading.setVisibility(View.GONE);
//                        break;
//                    case Player.STATE_BUFFERING:
//                        loading.setVisibility(View.VISIBLE);
//                        break;
//                }
//            }
//
//            @Override
//            public void onRepeatModeChanged(int repeatMode) {
//
//            }
//
//            @Override
//            public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
//
//            }
//
//            @Override
//            public void onPlayerError(ExoPlaybackException error) {
//
//            }
//
//            @Override
//            public void onPositionDiscontinuity(int reason) {
//
//            }
//
//            @Override
//            public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
//
//            }
//
//            @Override
//            public void onSeekProcessed() {
//
//            }
//        });
//        player.seekTo(currentWindow, playbackPosition);
//        player.prepare(mediaSource, true, false);
//    }

    private void initFirstVideoSetUp() {
        mImgBtnFirstVideoEdit.setVisibility(View.GONE);
        mEtvFirstVideoURLLink.setVisibility(View.VISIBLE);
        mImgBtnFirstVideoStream.setVisibility(View.VISIBLE);
        mEtvFirstVideoURLLink.requestFocus();
        CustomKeyboardUtil.showKeyboard(getActivity());
    }

    private void editFirstVideoSetUp() {
        mEtvFirstVideoURLLink.setVisibility(View.INVISIBLE);
        mImgBtnFirstVideoStream.setVisibility(View.GONE);
        mImgBtnFirstVideoEdit.setVisibility(View.VISIBLE);
    }

    private void initSecondVideoSetUp() {
        mImgBtnSecondVideoEdit.setVisibility(View.GONE);
        mEtvSecondVideoURLLink.setVisibility(View.VISIBLE);
        mImgBtnSecondVideoStream.setVisibility(View.VISIBLE);
    }

    private void editSecondVideoSetUp() {
        mEtvSecondVideoURLLink.setVisibility(View.INVISIBLE);
        mImgBtnSecondVideoStream.setVisibility(View.GONE);
        mImgBtnSecondVideoEdit.setVisibility(View.VISIBLE);
    }

    private void onVisible() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String videoFirstURL = pref.getString(SharedPreferenceConstants.SUB_HEADER_FIRST_VIDEO_URL,
                SharedPreferenceConstants.DEFAULT_STRING);

        if (!SharedPreferenceConstants.DEFAULT_STRING.equalsIgnoreCase(videoFirstURL)) {
            editFirstVideoSetUp();
//            setVideoOneStreamURL(videoFirstURL);
//            setVideoOneExoStreamURL(videoFirstURL);
            setVideoOneVLCStreamURL(videoFirstURL);
        } else {
            initFirstVideoSetUp();
        }

        String videoSecondURL = pref.getString(SharedPreferenceConstants.SUB_HEADER_SECOND_VIDEO_URL,
                SharedPreferenceConstants.DEFAULT_STRING);

        if (!SharedPreferenceConstants.DEFAULT_STRING.equalsIgnoreCase(videoSecondURL)) {
            editSecondVideoSetUp();
//            setVideoTwoStreamURL(videoSecondURL);
//            setVideoTwoExoStreamURL(videoSecondURL);
            setVideoTwoVLCStreamURL(videoSecondURL);
        } else {
            initSecondVideoSetUp();
        }
    }

    private void onInvisible() {
        Log.d(TAG, "onInvisible");
        hideKeyboard();
        releasePlayers();
//        if (Util.SDK_INT <= 23) {
//            releasePlayer();
//        }
    }

    private void hideKeyboard() {
        mEtvFirstVideoURLLink.clearFocus();
        mEtvSecondVideoURLLink.clearFocus();
        CustomKeyboardUtil.hideKeyboard(getActivity(), mEtvFirstVideoURLLink);
        CustomKeyboardUtil.hideKeyboard(getActivity(), mEtvSecondVideoURLLink);
    }

//    private void releasePlayer() {
//        if (player != null) {
//            playbackPosition = player.getCurrentPosition();
//            currentWindow = player.getCurrentWindowIndex();
//            playWhenReady = player.getPlayWhenReady();
//            player.release();
//            player = null;
//        }
//    }

    /**
     * Creates VLC MediaPlayerOne and plays video
     *
     * @param media
     */
    private void setVideoOneVLCStreamURL(String media) {
        releasePlayerOne();
        try {
//            if (media.length() > 0) {
//                Toast toast = Toast.makeText(getActivity(), media, Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0,
//                        0);
//                toast.show();
//            }

            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libVlcOne = new LibVLC(getActivity(), options);
            mSurfaceHolderOne.setKeepScreenOn(true);

            // Creating media player
            mMediaPlayerOne = new MediaPlayer(libVlcOne);
            mMediaPlayerOne.setEventListener(mPlayerOneListener);

            // Seting up video output
            final IVLCVout vout = mMediaPlayerOne.getVLCVout();
            vout.setVideoView(mSurfaceViewOne);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libVlcOne, Uri.parse(media));
            mMediaPlayerOne.setMedia(m);
            mMediaPlayerOne.play();
        } catch (Exception e) {
//            Toast.makeText(getActivity(), "Error in creating player!", Toast
//                    .LENGTH_LONG).show();
        }
    }

    /**
     * Creates VLC MediaPlayerTwo and plays video
     *
     * @param media
     */
    private void setVideoTwoVLCStreamURL(String media) {
        releasePlayerTwo();
        try {
            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            libVlcTwo = new LibVLC(getActivity(), options);
            mSurfaceHolderTwo.setKeepScreenOn(true);

            // Creating media player
            mMediaPlayerTwo = new MediaPlayer(libVlcTwo);
            mMediaPlayerTwo.setEventListener(mPlayerTwoListener);

            // Seting up video output
            final IVLCVout vout = mMediaPlayerTwo.getVLCVout();
            vout.setVideoView(mSurfaceViewTwo);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(this);
            vout.attachViews();

            Media m = new Media(libVlcTwo, Uri.parse(media));
            mMediaPlayerTwo.setMedia(m);
            mMediaPlayerTwo.play();
        } catch (Exception e) {
//            Toast.makeText(getActivity(), "Error in creating player!", Toast
//                    .LENGTH_LONG).show();
        }
    }

    private void releasePlayers() {
        releasePlayerOne();
        releasePlayerTwo();
    }

    private void releasePlayerOne() {
        if (libVlcOne == null)
            return;

        mMediaPlayerOne.stop();
        final IVLCVout vOutOne = mMediaPlayerOne.getVLCVout();
        vOutOne.removeCallback(this);
        vOutOne.detachViews();
        mSurfaceHolderOne = null;
        libVlcOne.release();
        libVlcOne = null;

//        mVideoWidthOne = 0;
//        mVideoHeightOne = 0;
    }

    private void releasePlayerTwo() {
        if (libVlcTwo == null)
            return;

        mMediaPlayerTwo.stop();
        final IVLCVout vOutTwo = mMediaPlayerTwo.getVLCVout();
        vOutTwo.removeCallback(this);
        vOutTwo.detachViews();
        mSurfaceHolderTwo = null;
        libVlcTwo.release();
        libVlcTwo = null;

//        mVideoWidthTwo = 0;
//        mVideoHeightTwo = 0;
    }

    /**
     * Registering callbacks for VLC Player One
     */
    private MediaPlayer.EventListener mPlayerOneListener = new MyPlayerOneListener(this);

    private static class MyPlayerOneListener implements MediaPlayer.EventListener {
        private WeakReference<VideoStreamFragment> mOwner;

        public MyPlayerOneListener(VideoStreamFragment owner) {
            mOwner = new WeakReference<>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            VideoStreamFragment player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerOne EndReached");
                    player.releasePlayerOne();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }

    /**
     * Registering callbacks for VLC Player One
     */
    private MediaPlayer.EventListener mPlayerTwoListener = new MyPlayerTwoListener(this);

    private static class MyPlayerTwoListener implements MediaPlayer.EventListener {
        private WeakReference<VideoStreamFragment> mOwner;

        public MyPlayerTwoListener(VideoStreamFragment owner) {
            mOwner = new WeakReference<>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            VideoStreamFragment player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerTwo EndReached");
                    player.releasePlayerTwo();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }

    @Override
    public void onNewLayout(IVLCVout vlcVout, int width, int height, int visibleWidth, int visibleHeight, int sarNum, int sarDen) {

    }

    @Override
    public void onSurfacesCreated(IVLCVout vlcVout) {

    }

    @Override
    public void onSurfacesDestroyed(IVLCVout vlcVout) {

    }

    @Override
    public void onHardwareAccelerationError(IVLCVout vlcVout) {
        Log.e(TAG, "Error with hardware acceleration");
        this.releasePlayers();
        Toast.makeText(getActivity(), "Error with hardware acceleration", Toast.LENGTH_LONG).show();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
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
    public void onResume() {
        super.onResume();
//        createPlayer(mFilePath);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mIsVisibleToUser) {
            onVisible();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
//        releasePlayer();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIsVisibleToUser) {
            onInvisible();
        }


//        if (Util.SDK_INT > 23) {
//            releasePlayer();
//        }
    }

//    @Override
//    public void requestPerformed(@NotNull OnvifResponse result) {
//
////        if (result.getSuccess()) {
////            mMyDevice.getDeviceInformation();
////
////        } else
//
//
//        if (result.getRequest().getType() == OnvifRequest.Type.GetDeviceInformation) {
//            mMyDevice.getProfiles();
//        } else if (result.getRequest().getType() == OnvifRequest.Type.GetProfiles) {
//            mMyDevice.getStreamURI();
//        } else if (result.getRequest().getType() == OnvifRequest.Type.GetStreamURI) {
//            Log.d("ONVIF", "Stream URI retrieved: ${currentDevice.rtspURI}");
//        }
//
//    }
}
