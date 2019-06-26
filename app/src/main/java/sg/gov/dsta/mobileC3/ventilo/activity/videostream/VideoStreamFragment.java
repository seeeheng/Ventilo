package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.res.ResourcesCompat;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.ByteArrayOutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.activity.sitrep.SitRepAddUpdateFragment;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.VideoStreamViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.PhotoCaptureUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.task.EAccessRight;

public class VideoStreamFragment extends Fragment implements SnackbarUtil.SnackbarActionClickListener {

    private static final String TAG = VideoStreamFragment.class.getSimpleName();
    private static final int MEDIA_CONTROLLER_SHOW_DURATION = 3000;
    private static final int DEFAULT_VIDEO_VIEW_WIDTH = 300;
    private static final int DEFAULT_VIDEO_VIEW_HEIGHT = 200;
    private static final int FIRST_VIDEO_STREAM_SPINNER_ID = 1;
    private static final int SECOND_VIDEO_STREAM_SPINNER_ID = 2;

    // View models
    private UserViewModel mUserViewModel;
    private VideoStreamViewModel mVideoStreamViewModel;

    // Main layout

    private RelativeLayout mRelativeLayoutMain;

    /*** VLC first video stream ***/
    private LibVLC mLibVlcOne;
    private VideoStreamControllerView mVideoStreamControllerViewOne;
    private MediaPlayer mMediaPlayerOne;

    // First video stream toolbar
    private ImageView mImgSetting;
    private Spinner mSpinnerVideoStreamListOne;
    private ArrayAdapter mSpinnerVideoStreamAdapterOne;
    private ArrayList<String> mVideoStreamNameListOne;

    // First video stream view
    private RelativeLayout mRelativeLayoutToolbarOne;
    private LinearLayout mLinearLayoutVideoStreamOne;
    private ConstraintLayout mConstraintLayoutSurfaceViewOne;
    private TextureView mSurfaceViewOne;
//    private SurfaceHolder mSurfaceHolderOne;

    /*** VLC second video stream ***/
    private LibVLC mLibVlcTwo;
    private VideoStreamControllerView mVideoStreamControllerViewTwo;
    private MediaPlayer mMediaPlayerTwo;

    // Second video stream toolbar
    private Spinner mSpinnerVideoStreamListTwo;
    private ArrayAdapter mSpinnerVideoStreamAdapterTwo;
    private ArrayList<String> mVideoStreamNameListTwo;

    // Second video stream view
    private RelativeLayout mRelativeLayoutToolbarTwo;
    private LinearLayout mLinearLayoutVideoStreamTwo;
    private ConstraintLayout mConstraintLayoutSurfaceViewTwo;
    private TextureView mSurfaceViewTwo;
//    private SurfaceHolder mSurfaceHolderTwo;

    private boolean mIsFragmentVisibleToUser;
    private boolean mIsFullscreen;

    // Screenshot
    Bitmap mBitmapTakenScreenshot;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_video_stream, container, false);


        System.out.println("rootView create");

        if (mLinearLayoutVideoStreamTwo == null) {
            System.out.println("mLinearLayoutVideoStreamTwo NULL");
        } else {
            System.out.println("mLinearLayoutVideoStreamTwo NOT");
        }

        observerSetup();
        initUI(rootView);

        return rootView;
    }

    private void initUI(View rootVideoStreamView) {
        mRelativeLayoutMain = rootVideoStreamView.findViewById(R.id.layout_video_stream_fragment);

        initVideoStreamOneToolbar(rootVideoStreamView);
        initSurfaceViewVideoOneStream(rootVideoStreamView);

        // Display second video stream panel if current user access right is 'CCT'
        mLinearLayoutVideoStreamTwo = rootVideoStreamView.findViewById(R.id.layout_video_stream_two);
        mLinearLayoutVideoStreamTwo.setVisibility(View.GONE);

        if (!EAccessRight.CCT.toString().equalsIgnoreCase(SharedPreferenceUtil.getCurrentUserAccessRight())) {
            // Set appropriate dimension for video stream layout
            setVideoStreamDefaultLayoutForOthers(mLinearLayoutVideoStreamOne, mConstraintLayoutSurfaceViewOne);
        } else {
            // Set appropriate dimension for video stream layout
            setVideoStreamDefaultLayoutForCCT(mLinearLayoutVideoStreamOne,
                    mConstraintLayoutSurfaceViewOne);

            mLinearLayoutVideoStreamTwo.setVisibility(View.VISIBLE);
            initVideoStreamTwoToolbar(rootVideoStreamView);
            initSurfaceViewVideoTwoStream(rootVideoStreamView);
        }
    }

    private void initVideoStreamOneToolbar(View rootVideoStreamView) {
        mRelativeLayoutToolbarOne = rootVideoStreamView.findViewById(R.id.layout_video_stream_one_toolbar);
        initSettingUI(rootVideoStreamView);
        initVideoListOneSpinner(rootVideoStreamView);
    }

    private void initVideoStreamTwoToolbar(View rootVideoStreamView) {
        mRelativeLayoutToolbarTwo = rootVideoStreamView.findViewById(R.id.layout_video_stream_two_toolbar);
        initVideoListTwoSpinner(rootVideoStreamView);
    }

    private void initSettingUI(View rootVideoStreamView) {
        mImgSetting = rootVideoStreamView.findViewById(R.id.img_btn_video_stream_setting);
        mImgSetting.setOnClickListener(settingOnClickListener);
    }

    private void initVideoListOneSpinner(View rootVideoStreamView) {
        mSpinnerVideoStreamListOne = rootVideoStreamView.findViewById(R.id.spinner_video_stream_selector_one);
        mVideoStreamNameListOne = new ArrayList<>();
        mVideoStreamNameListOne.add(getString(R.string.video_stream_select_spinner_item));

        mSpinnerVideoStreamAdapterOne = getSpinnerArrayAdapter(mVideoStreamNameListOne);

        mSpinnerVideoStreamListOne.setAdapter(mSpinnerVideoStreamAdapterOne);
        mSpinnerVideoStreamListOne.setOnItemSelectedListener(videoStreamOneSpinnerOnItemSelectedListener);
    }

    private void initVideoListTwoSpinner(View rootVideoStreamView) {
        mSpinnerVideoStreamListTwo = rootVideoStreamView.findViewById(R.id.spinner_video_stream_selector_two);
        mVideoStreamNameListTwo = new ArrayList<>();
        mVideoStreamNameListTwo.add(getString(R.string.video_stream_select_spinner_item));

        mSpinnerVideoStreamAdapterTwo = getSpinnerArrayAdapter(mVideoStreamNameListTwo);

        mSpinnerVideoStreamListTwo.setAdapter(mSpinnerVideoStreamAdapterTwo);
        mSpinnerVideoStreamListTwo.setOnItemSelectedListener(videoStreamTwoSpinnerOnItemSelectedListener);
    }

    private ArrayAdapter<String> getSpinnerArrayAdapter(ArrayList<String> stringArrayList) {
        return new ArrayAdapter<String> (getActivity(),
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
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = view.findViewById(R.id.tv_spinner_row_item_text);
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_text_hint_dark_grey, null));
                } else {
                    tv.setTextColor(ResourcesCompat.getColor(getResources(), R.color.primary_white, null));
                }

                return view;
            }
        };
    }

    private Spinner.OnItemSelectedListener videoStreamOneSpinnerOnItemSelectedListener =
            new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedVideoName = (String) parent.getItemAtPosition(position);
                    getUrlStreamFromName(selectedVideoName, FIRST_VIDEO_STREAM_SPINNER_ID);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };

    private Spinner.OnItemSelectedListener videoStreamTwoSpinnerOnItemSelectedListener =
            new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedVideoName = (String) parent.getItemAtPosition(position);
                    getUrlStreamFromName(selectedVideoName, SECOND_VIDEO_STREAM_SPINNER_ID);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };

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

                            if (spinnerId == FIRST_VIDEO_STREAM_SPINNER_ID) {
                                setVideoOneVLCStreamURL(videoUrl);
                            } else {
                                setVideoTwoVLCStreamURL(videoUrl);
                            }
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

        mVideoStreamViewModel.getVideoStreamUrlForUserByName(getUserID(), videoName,
                singleObserverVideoStreamForUserByName);
    }

    private String getUserID() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = pref.getString(SharedPreferenceConstants.USER_ID,
                SharedPreferenceConstants.DEFAULT_STRING);
        return userId;
    }

    private void initSurfaceViewVideoOneStream(View rootVideoStreamView) {
//        mFilePath = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";

//        Log.d(TAG, "Playing: " + mFilePath);
        mLinearLayoutVideoStreamOne = rootVideoStreamView.findViewById(R.id.layout_video_stream_one);
        mConstraintLayoutSurfaceViewOne = rootVideoStreamView.findViewById(R.id.constraint_layout_video_surface_one);
        mSurfaceViewOne = rootVideoStreamView.findViewById(R.id.texture_view_stream_one);
        mVideoStreamControllerViewOne = new VideoStreamControllerView(getContext(), false);
        mVideoStreamControllerViewOne.setMediaPlayer(videoStreamPlayerOneInterface);
        mVideoStreamControllerViewOne.setAnchorView(mConstraintLayoutSurfaceViewOne);
//        mSurfaceHolderOne = mSurfaceViewOne.getHolder();
    }

    private void initSurfaceViewVideoTwoStream(View rootVideoStreamView) {
        mConstraintLayoutSurfaceViewTwo = rootVideoStreamView.findViewById(R.id.constraint_layout_video_surface_two);
        mSurfaceViewTwo = rootVideoStreamView.findViewById(R.id.surface_view_stream_two);
//        mSurfaceHolderTwo = mSurfaceViewTwo.getHolder();
        mVideoStreamControllerViewTwo = new VideoStreamControllerView(getContext(), false);
        mVideoStreamControllerViewTwo.setMediaPlayer(videoStreamPlayerTwoInterface);
        mVideoStreamControllerViewTwo.setAnchorView(mConstraintLayoutSurfaceViewTwo);
    }

    private VideoStreamControllerView.MediaPlayerControl videoStreamPlayerOneInterface =
            new VideoStreamControllerView.MediaPlayerControl() {
                public int getBufferPercentage() {
                    return 0;
                }

                public int getCurrentPosition() {
                    float pos = mMediaPlayerOne.getPosition();
                    return (int) (pos * getDuration());
                }

                public int getDuration() {
                    return (int) mMediaPlayerOne.getLength();
                }

                public boolean isPlaying() {
                    return mMediaPlayerOne.isPlaying();
                }

                public void pause() {
                    mMediaPlayerOne.pause();
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

                            SnackbarUtil.showCustomAlertSnackbar(mRelativeLayoutMain, getSnackbarView(),
                                    screenshotStringBuilder.toString(), VideoStreamFragment.this);
                        }
                    }
                }

                public void seekTo(int pos) {
                    mMediaPlayerOne.setPosition((float) pos / getDuration());
                }

                public void start() {
                    mMediaPlayerOne.play();
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
                        setVideoStreamFullscreen(mLinearLayoutVideoStreamOne,
                                mConstraintLayoutSurfaceViewOne);
                    } else {
                        if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                                SharedPreferenceUtil.getCurrentUserAccessRight())) {
                            setVideoStreamDefaultScreenForOthers(mLinearLayoutVideoStreamOne,
                                    mConstraintLayoutSurfaceViewOne);
                        } else {
                            setVideoStreamDefaultScreenForCCT(mLinearLayoutVideoStreamOne,
                                    mConstraintLayoutSurfaceViewOne);
                        }
                    }

                    if (!EAccessRight.CCT.toString().equalsIgnoreCase(
                            SharedPreferenceUtil.getCurrentUserAccessRight())) {
                        setOtherUiElementsForFirstVideoStreamForOthers(!isFullScreen());
                    } else {
                        setOtherUiElementsForFirstVideoStreamForCCT(!isFullScreen());
                    }

                    mIsFullscreen = !mIsFullscreen;
                }
            };

    private VideoStreamControllerView.MediaPlayerControl videoStreamPlayerTwoInterface =
            new VideoStreamControllerView.MediaPlayerControl() {
                public int getBufferPercentage() {
                    return 0;
                }

                public int getCurrentPosition() {
                    float pos = mMediaPlayerTwo.getPosition();
                    return (int) (pos * getDuration());
                }

                public int getDuration() {
                    return (int) mMediaPlayerTwo.getLength();
                }

                public boolean isPlaying() {
                    return mMediaPlayerTwo.isPlaying();
                }

                public void pause() {
                    mMediaPlayerTwo.pause();
                }

                public void takeScreenshot() {
//            Bitmap imageBitmap = screenShot(mSurfaceViewOne);
                    if (mBitmapTakenScreenshot != null) {
                        mBitmapTakenScreenshot.recycle();
                    }

                    mBitmapTakenScreenshot = mSurfaceViewTwo.getBitmap();

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

                            SnackbarUtil.showCustomAlertSnackbar(mRelativeLayoutMain, getSnackbarView(),
                                    screenshotStringBuilder.toString(), VideoStreamFragment.this);
                        }
                    }
                }

                public void seekTo(int pos) {
                    mMediaPlayerTwo.setPosition((float) pos / getDuration());
                }

                public void start() {
                    mMediaPlayerTwo.play();
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
                        setVideoStreamFullscreen(mLinearLayoutVideoStreamTwo,
                                mConstraintLayoutSurfaceViewTwo);
                    } else {
                        setVideoStreamDefaultScreenForCCT(mLinearLayoutVideoStreamTwo,
                                mConstraintLayoutSurfaceViewTwo);
                    }

                    setOtherUiElementsForSecondVideoStreamForCCT(!isFullScreen());
                    mIsFullscreen = !mIsFullscreen;
                }
            };

    // ---------------------------------------- Video Stream Layouts ---------------------------------------- //

    /**
     * Set default dimension layout for users other than CCT
     *
     * @param linearLayout
     * @param constraintLayout
     */
    private void setVideoStreamDefaultLayoutForOthers(LinearLayout linearLayout,
                                                      ConstraintLayout constraintLayout) {
        DimensionUtil.setDimensions(linearLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                new LinearLayout(getContext()));

        DimensionUtil.setDimensions(constraintLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.video_stream_single_texture_view_height),
                new LinearLayout(getContext()));
    }

    /**
     * Set default dimension layout for CCT users
     *
     * @param linearLayout
     * @param constraintLayout
     */
    private void setVideoStreamDefaultLayoutForCCT(LinearLayout linearLayout,
                                                   ConstraintLayout constraintLayout) {
        DimensionUtil.setDimensions(linearLayout,
                0, LinearLayout.LayoutParams.WRAP_CONTENT,
                new LinearLayout(getContext()));

        DimensionUtil.setDimensions(constraintLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.video_stream_double_texture_view_height),
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
    }

    /**
     * Sets video stream layout to full screen
     *
     * @param linearLayout
     * @param constraintLayout
     */
    private void setVideoStreamFullscreen(LinearLayout linearLayout, ConstraintLayout constraintLayout) {

        mRelativeLayoutMain.setPadding(0, 0, 0, 0);

        setLayoutFullScreen(linearLayout);
        setLayoutFullScreen(constraintLayout);

        setMainActivityComponents(View.GONE);
    }

    /**
     * Set default screen dimension for users other than CCT
     *
     * @param linearLayout
     * @param constraintLayout
     */
    private void setVideoStreamDefaultScreenForOthers(LinearLayout linearLayout, ConstraintLayout constraintLayout) {

        mRelativeLayoutMain.setPadding((int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing));

        setVideoStreamDefaultLayoutForOthers(linearLayout, constraintLayout);
        setMainActivityComponents(View.VISIBLE);
    }

    /**
     * Sets default screen dimension for CCT users
     *
     * @param linearLayout
     * @param constraintLayout
     */
    private void setVideoStreamDefaultScreenForCCT(LinearLayout linearLayout, ConstraintLayout constraintLayout) {

        mRelativeLayoutMain.setPadding((int) getResources().getDimension(R.dimen.elements_margin_spacing),
                (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                        (int) getResources().getDimension(R.dimen.elements_margin_spacing),
                                (int) getResources().getDimension(R.dimen.elements_margin_spacing));

        setVideoStreamDefaultLayoutForCCT(linearLayout, constraintLayout);
        setMainActivityComponents(View.VISIBLE);
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

    /**
     * Sets other UI layouts accordingly based on default or fullscreen,
     * other than those that are included in the first video stream layouts;
     * This is for CCT users
     *
     * @param isFullScreen
     */
    private void setOtherUiElementsForFirstVideoStreamForCCT(boolean isFullScreen) {
        if (isFullScreen) {
            mRelativeLayoutToolbarOne.setVisibility(View.GONE);
            mLinearLayoutVideoStreamTwo.setVisibility(View.GONE);
        } else {
            mRelativeLayoutToolbarOne.setVisibility(View.VISIBLE);
            mLinearLayoutVideoStreamTwo.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Sets other UI layouts accordingly based on default or fullscreen,
     * other than those that are included in the second video stream layouts;
     * This is for CCT users
     *
     * @param isFullScreen
     */
    private void setOtherUiElementsForSecondVideoStreamForCCT(boolean isFullScreen) {
        if (isFullScreen) {
            mLinearLayoutVideoStreamOne.setVisibility(View.GONE);
            mRelativeLayoutToolbarTwo.setVisibility(View.GONE);
        } else {
            mLinearLayoutVideoStreamOne.setVisibility(View.VISIBLE);
            mRelativeLayoutToolbarTwo.setVisibility(View.VISIBLE);
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

    /**
     * Creates VLC MediaPlayerOne and plays video
     *
     * @param media
     */
    private void setVideoOneVLCStreamURL(String media) {
        releasePlayerOne();
        try {
            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            mLibVlcOne = new LibVLC(getActivity(), options);
//            mSurfaceHolderOne.setKeepScreenOn(true);

            // Create media controller
            mSurfaceViewOne.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mVideoStreamControllerViewOne.show(MEDIA_CONTROLLER_SHOW_DURATION);
                }
            });

            // Creating media player
            mMediaPlayerOne = new MediaPlayer(mLibVlcOne);
            mMediaPlayerOne.setEventListener(mPlayerOneListener);

            // Seting up video output
            final IVLCVout vout = mMediaPlayerOne.getVLCVout();
            vout.setVideoView(mSurfaceViewOne);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(firstIVLCVoutCallback);
            vout.attachViews();

            Media m = new Media(mLibVlcOne, Uri.parse(media));
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
            mLibVlcTwo = new LibVLC(getActivity(), options);
//            mSurfaceHolderTwo.setKeepScreenOn(true);

            // Create media controller
            mSurfaceViewTwo.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mVideoStreamControllerViewTwo.show(MEDIA_CONTROLLER_SHOW_DURATION);
                }
            });

            // Creating media player
            mMediaPlayerTwo = new MediaPlayer(mLibVlcTwo);
            mMediaPlayerTwo.setEventListener(mPlayerTwoListener);

            // Seting up video output
            final IVLCVout vout = mMediaPlayerTwo.getVLCVout();
            vout.setVideoView(mSurfaceViewTwo);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(secondIVLCVoutCallback);
            vout.attachViews();

            Media m = new Media(mLibVlcTwo, Uri.parse(media));
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
        if (mLibVlcOne == null)
            return;

        Log.i(TAG, "Releasing video player one");
        mMediaPlayerOne.stop();

//        mLibVlcOne.release();
//        mLibVlcOne = null;
//
//        final IVLCVout vOutOne = mMediaPlayerOne.getVLCVout();
//        mMediaPlayerOne.release();
//        vOutOne.removeCallback(firstIVLCVoutCallback);
//        vOutOne.detachViews();
    }

    private void releasePlayerTwo() {
        if (mLibVlcTwo == null)
            return;

        Log.i(TAG, "Releasing video player two");
        mMediaPlayerTwo.stop();
        mMediaPlayerTwo.release();
        final IVLCVout vOutTwo = mMediaPlayerTwo.getVLCVout();
        vOutTwo.removeCallback(secondIVLCVoutCallback);
        vOutTwo.detachViews();
//        mSurfaceHolderTwo = null;
        mLibVlcTwo.release();
        mLibVlcTwo = null;
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
     * Registering callbacks for VLC Player Two
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

    private View getSnackbarView() {
        if (getActivity() instanceof MainActivity) {
            return ((MainActivity) getActivity()).getSnackbarView();
        } else {
            return null;
        }
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
        mVideoStreamViewModel.getAllVideoStreamNamesLiveDataForUser(userId).observe(this,
                new Observer<List<String>>() {
                    @Override
                    public void onChanged(@Nullable List<String> videoStreamNameList) {
                        if (mSpinnerVideoStreamAdapterOne != null) {
                            if (mVideoStreamNameListOne == null) {
                                mVideoStreamNameListOne = new ArrayList<>();
                            } else {
                                mVideoStreamNameListOne.clear();
                            }

                            mVideoStreamNameListOne.add(getString(R.string.video_stream_select_spinner_item));
                            mVideoStreamNameListOne.addAll(videoStreamNameList);

                            /**
                             * Remove last video model item if item content is empty.
                             * Only the last item will possibly be empty because an
                             * empty entry will be stored in the database to be displayed in the
                             * add video stream fragment's recyclerview for user to fill up details
                             * of this entry and save it in the database.
                             */
                            int lastVideoStreamItemIndex = mVideoStreamNameListOne.size() - 1;
                            if (lastVideoStreamItemIndex != 0) {
                                String lastVideoStreamModel = mVideoStreamNameListOne.get(lastVideoStreamItemIndex);

                                if (TextUtils.isEmpty(lastVideoStreamModel.trim())) {
                                    mVideoStreamNameListOne.remove(lastVideoStreamItemIndex);
                                }
                            }

                            mSpinnerVideoStreamAdapterOne.notifyDataSetChanged();
                        }

                        if (mSpinnerVideoStreamAdapterTwo != null) {
                            if (mVideoStreamNameListTwo == null) {
                                mVideoStreamNameListTwo = new ArrayList<>();
                            } else {
                                mVideoStreamNameListTwo.clear();
                            }

                            mVideoStreamNameListTwo.add(getString(R.string.video_stream_select_spinner_item));
                            mVideoStreamNameListTwo.addAll(videoStreamNameList);

                            /**
                             * Remove last video model item if item content is empty.
                             * Only the last item will possibly be empty because an
                             * empty entry will be stored in the database to be displayed in the
                             * add video stream fragment's recyclerview for user to fill up details
                             * of this entry and save it in the database.
                             */
                            int lastVideoStreamItemIndex = mVideoStreamNameListTwo.size() - 1;
                            if (lastVideoStreamItemIndex != 0) {
                                String lastVideoStreamModel = mVideoStreamNameListTwo.get(lastVideoStreamItemIndex);

                                if (TextUtils.isEmpty(lastVideoStreamModel.trim())) {
                                    mVideoStreamNameListTwo.remove(lastVideoStreamItemIndex);
                                }
                            }

                            mSpinnerVideoStreamAdapterTwo.notifyDataSetChanged();
                        }
                    }
                });
    }

    private IVLCVout.Callback firstIVLCVoutCallback = new IVLCVout.Callback() {

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
            Log.e(TAG, "Error with hardware acceleration for first video stream");
            Toast.makeText(getActivity(), "Error with hardware acceleration for first video stream",
                    Toast.LENGTH_LONG).show();
            releasePlayerOne();
        }
    };

    private IVLCVout.Callback secondIVLCVoutCallback = new IVLCVout.Callback() {

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
            Log.e(TAG, "Error with hardware acceleration for second video stream");
            Toast.makeText(getActivity(), "Error with hardware acceleration for second video stream",
                    Toast.LENGTH_LONG).show();
            releasePlayerTwo();
        }
    };

    private void navigateToFragment(Fragment toFragment) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateWithAnimatedTransitionToFragment(
                    R.id.layout_video_stream_fragment, this, toFragment);
        }
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

    private void onVisible() {
        Log.d(TAG, "onVisible");
    }

    private void onInvisible() {
        Log.d(TAG, "onInvisible");
//        hideKeyboard();

        CloseVideoStreamsAsyncTask task = new CloseVideoStreamsAsyncTask();
        task.execute();
//        releasePlayers();


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
//        createPlayer(mFilePath);
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

    private class CloseVideoStreamsAsyncTask extends AsyncTask<Void, Void, Void> {

        CloseVideoStreamsAsyncTask() {
        }

        @Override
        protected Void doInBackground(final Void... param) {
            releasePlayers();

            return null;
        }
    }
}
