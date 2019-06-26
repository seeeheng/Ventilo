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
import sg.gov.dsta.mobileC3.ventilo.activity.videostream.VideoStreamControllerView;
import sg.gov.dsta.mobileC3.ventilo.activity.videostream.VideoStreamFragment;
import sg.gov.dsta.mobileC3.ventilo.model.user.UserModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.UserViewModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.VideoStreamViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;
import sg.gov.dsta.mobileC3.ventilo.util.PhotoCaptureUtil;
import sg.gov.dsta.mobileC3.ventilo.util.SnackbarUtil;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;
import sg.gov.dsta.mobileC3.ventilo.util.sharedPreference.SharedPreferenceUtil;
import sg.gov.dsta.mobileC3.ventilo.util.task.EAccessRight;

public class DashboardVideoStreamFragment extends Fragment implements SnackbarUtil.SnackbarActionClickListener{

    private static final String TAG = DashboardVideoStreamFragment.class.getSimpleName();
    private static final int MEDIA_CONTROLLER_SHOW_DURATION = 3000;

    // View models
    private UserViewModel mUserViewModel;
    private VideoStreamViewModel mVideoStreamViewModel;

    // Main layout
    private RelativeLayout mRelativeLayoutMain;

    /*** VLC first video stream ***/
    private LibVLC mLibVlc;
    private VideoStreamControllerView mVideoStreamControllerView;
    private MediaPlayer mMediaPlayer;

    // First video stream toolbar
    private ImageView mImgSetting;
    private Spinner mSpinnerVideoStreamListOne;
    private ArrayAdapter mSpinnerVideoStreamAdapter;
    private ArrayList<String> mVideoStreamNameList;

    // First video stream view
    private RelativeLayout mRelativeLayoutToolbarOne;
    private LinearLayout mLinearLayoutVideoStream;
    private ConstraintLayout mConstraintLayoutSurfaceView;
    private TextureView mSurfaceView;
//    private SurfaceHolder mSurfaceHolderOne;

    private boolean mIsFullscreen;
//    private List<UserModel> mRadioLinkStatusUserListItems;

    // Screenshot
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
        mRelativeLayoutMain = rootView.findViewById(R.id.layout_dashboard_video_stream_fragment);

        initVideoStreamOneToolbar(rootView);
        initSurfaceViewVideoOneStream(rootView);
        setVideoStreamDefaultLayoutForOthers(mLinearLayoutVideoStream, mConstraintLayoutSurfaceView);
    }

    private void initVideoStreamOneToolbar(View rootVideoStreamView) {
        mRelativeLayoutToolbarOne = rootVideoStreamView.findViewById(R.id.layout_dashboard_video_stream_toolbar);
//        initSettingUI(rootVideoStreamView);
        initVideoListOneSpinner(rootVideoStreamView);
    }

    private void initSurfaceViewVideoOneStream(View rootVideoStreamView) {
//        mFilePath = "rtsp://184.72.239.149/vod/mp4:BigBuckBunny_115k.mov";

        mLinearLayoutVideoStream = rootVideoStreamView.findViewById(R.id.layout_dashboard_video_stream);
        mConstraintLayoutSurfaceView = rootVideoStreamView.findViewById(R.id.constraint_layout_dashboard_video_surface);
        mSurfaceView = rootVideoStreamView.findViewById(R.id.texture_view_dashboard_stream);
        mVideoStreamControllerView = new VideoStreamControllerView(getContext(), false);
        mVideoStreamControllerView.setMediaPlayer(videoStreamPlayerInterface);
        mVideoStreamControllerView.setAnchorView(mConstraintLayoutSurfaceView);
//        mSurfaceHolderOne = mSurfaceView.getHolder();
    }

    private void initVideoListOneSpinner(View rootVideoStreamView) {
        mSpinnerVideoStreamListOne = rootVideoStreamView.findViewById(R.id.spinner_dashboard_video_stream_selector);
        mVideoStreamNameList = new ArrayList<>();
        mVideoStreamNameList.add(getString(R.string.video_stream_select_spinner_item));

        mSpinnerVideoStreamAdapter = getSpinnerArrayAdapter(mVideoStreamNameList);

        mSpinnerVideoStreamListOne.setAdapter(mSpinnerVideoStreamAdapter);
        mSpinnerVideoStreamListOne.setOnItemSelectedListener(videoStreamSpinnerOnItemSelectedListener);
    }

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

    private Spinner.OnItemSelectedListener videoStreamSpinnerOnItemSelectedListener =
            new Spinner.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedVideoName = (String) parent.getItemAtPosition(position);
                    getUrlStreamFromName(selectedVideoName);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };

    private VideoStreamControllerView.MediaPlayerControl videoStreamPlayerInterface =
            new VideoStreamControllerView.MediaPlayerControl() {
                public int getBufferPercentage() {
                    return 0;
                }

                public int getCurrentPosition() {
                    float pos = mMediaPlayer.getPosition();
                    return (int) (pos * getDuration());
                }

                public int getDuration() {
                    return (int) mMediaPlayer.getLength();
                }

                public boolean isPlaying() {
                    return mMediaPlayer.isPlaying();
                }

                public void pause() {
                    mMediaPlayer.pause();
                }

                public void takeScreenshot() {
//            Bitmap imageBitmap = screenShot(mSurfaceView);
                    if (mBitmapTakenScreenshot != null) {
                        mBitmapTakenScreenshot.recycle();
                    }

                    mBitmapTakenScreenshot = mSurfaceView.getBitmap();
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
                                    screenshotStringBuilder.toString(), DashboardVideoStreamFragment.this);
                        }
                    }
                }

                public void seekTo(int pos) {
                    mMediaPlayer.setPosition((float) pos / getDuration());
                }

                public void start() {
                    mMediaPlayer.play();
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
                        setVideoStreamFullscreen(mLinearLayoutVideoStream,
                                mConstraintLayoutSurfaceView);
                    } else {
                        setVideoStreamDefaultScreenForOthers(mLinearLayoutVideoStream,
                                mConstraintLayoutSurfaceView);
                    }

                    setOtherUiElementsForFirstVideoStream(!isFullScreen());

                    mIsFullscreen = !mIsFullscreen;
                }
            };

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
     * Sets other UI layouts accordingly based on default or fullscreen,
     * other than those that are included in the video stream layouts
     *
     * @param isFullScreen
     */
    private void setOtherUiElementsForFirstVideoStream(boolean isFullScreen) {
        if (isFullScreen) {
            mRelativeLayoutToolbarOne.setVisibility(View.GONE);
        } else {
            mRelativeLayoutToolbarOne.setVisibility(View.VISIBLE);
        }
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
    private void setVideoStreamDefaultLayoutForOthers(LinearLayout linearLayout,
                                                      ConstraintLayout constraintLayout) {
        DimensionUtil.setDimensions(linearLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                new LinearLayout(getContext()));

        DimensionUtil.setDimensions(constraintLayout,
                LinearLayout.LayoutParams.MATCH_PARENT,
                (int) getResources().getDimension(R.dimen.dashboard_video_stream_texture_view_height),
                new LinearLayout(getContext()));
    }

    private void getUrlStreamFromName(String videoName) {

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

                            setVideoVLCStreamURL(videoUrl);

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

        mVideoStreamViewModel.getVideoStreamUrlForUserByName(SharedPreferenceUtil.getCurrentUserCallsignID(),
                videoName, singleObserverVideoStreamForUserByName);
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
//            Toast.makeText(getActivity(), "Error with hardware acceleration for first video stream",
//                    Toast.LENGTH_LONG).show();

            if (getSnackbarView() != null) {
                SnackbarUtil.showCustomInfoSnackbar(mRelativeLayoutMain, getSnackbarView(),
                        "Error with hardware acceleration for video stream");
            }

            releasePlayer();
        }
    };

    /**
     * Creates VLC MediaPlayerOne and plays video
     *
     * @param media
     */
    private void setVideoVLCStreamURL(String media) {
        releasePlayer();
        try {
            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            mLibVlc = new LibVLC(getActivity(), options);

            // Create media controller
            mSurfaceView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mVideoStreamControllerView.show(MEDIA_CONTROLLER_SHOW_DURATION);
                }
            });

            // Creating media player
            mMediaPlayer = new MediaPlayer(mLibVlc);
            mMediaPlayer.setEventListener(mPlayerOneListener);

            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mSurfaceView);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(firstIVLCVoutCallback);
            vout.attachViews();

            Media m = new Media(mLibVlc, Uri.parse(media));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Exception e) {
//            Toast.makeText(getActivity(), "Error in creating player", Toast
//                    .LENGTH_LONG).show();

            if (getSnackbarView() != null) {
                SnackbarUtil.showCustomInfoSnackbar(mRelativeLayoutMain, getSnackbarView(),
                        "Error in creating player");
            }
        }
    }

    /**
     * Registering callbacks for VLC Player One
     */
    private MediaPlayer.EventListener mPlayerOneListener = new DashboardVideoStreamFragment.
            MyPlayerOneListener(this);

    private static class MyPlayerOneListener implements MediaPlayer.EventListener {
        private WeakReference<DashboardVideoStreamFragment> mOwner;

        public MyPlayerOneListener(DashboardVideoStreamFragment owner) {
            mOwner = new WeakReference<>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            DashboardVideoStreamFragment player = mOwner.get();

            switch (event.type) {
                case MediaPlayer.Event.EndReached:
                    Log.d(TAG, "MediaPlayerOne EndReached");
                    player.releasePlayer();
                    break;
                case MediaPlayer.Event.Playing:
                case MediaPlayer.Event.Paused:
                case MediaPlayer.Event.Stopped:
                default:
                    break;
            }
        }
    }

    private void releasePlayer() {
        if (mLibVlc == null)
            return;

        mMediaPlayer.stop();
        final IVLCVout vOutOne = mMediaPlayer.getVLCVout();
        vOutOne.removeCallback(firstIVLCVoutCallback);
        vOutOne.detachViews();
//        mSurfaceHolderOne = null;
        mLibVlc.release();
        mLibVlc = null;
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
                        if (mSpinnerVideoStreamAdapter != null) {
                            if (mVideoStreamNameList == null) {
                                mVideoStreamNameList = new ArrayList<>();
                            } else {
                                mVideoStreamNameList.clear();
                            }

                            mVideoStreamNameList.add(getString(R.string.video_stream_select_spinner_item));
                            mVideoStreamNameList.addAll(videoStreamNameList);

                            /**
                             * Remove last video model item if item content is empty.
                             * Only the last item will possibly be empty because an
                             * empty entry will be stored in the database to be displayed in the
                             * add video stream fragment's recyclerview for user to fill up details
                             * of this entry and save it in the database.
                             */
                            int lastVideoStreamItemIndex = mVideoStreamNameList.size() - 1;
                            if (lastVideoStreamItemIndex != 0) {
                                String lastVideoStreamModel = mVideoStreamNameList.get(lastVideoStreamItemIndex);

                                if (TextUtils.isEmpty(lastVideoStreamModel.trim())) {
                                    mVideoStreamNameList.remove(lastVideoStreamItemIndex);
                                }
                            }

                            mSpinnerVideoStreamAdapter.notifyDataSetChanged();
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
}
