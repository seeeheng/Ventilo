package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import org.videolan.libvlc.IVLCVout;
import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class VideoStreamRunnable implements Runnable {

    private static final String TAG = VideoStreamRunnable.class.getSimpleName();
    private static final int MEDIA_CONTROLLER_SHOW_DURATION = 3000;

    private Context mContext;
    private TextureView mTextureView;
    private VideoStreamControllerView mVideoStreamControllerView;
    private LibVLC mLibVlc;
    private MediaPlayer mMediaPlayer;
    private String mMediaLink;


    private IVLCVout.Callback mFirstIVLCVoutCallback;

//    protected VideoStreamRunnable(Context context, TextureView textureView,
//                                  VideoStreamControllerView videoStreamControllerView,
//                                  LibVLC libVlc, MediaPlayer mediaPlayer, String media) {
//        mContext = context;
//        mTextureView = textureView;
//        mVideoStreamControllerView = videoStreamControllerView;
//        mLibVlc = libVlc;
//        mMediaPlayer = mediaPlayer;
//        mMediaLink = media;
//    }

    protected VideoStreamRunnable(LibVLC libVlc, MediaPlayer mediaPlayer,
                                  IVLCVout.Callback firstIVLCVoutCallback) {
        mLibVlc = libVlc;
        mMediaPlayer = mediaPlayer;
        mFirstIVLCVoutCallback = firstIVLCVoutCallback;
    }

    @Override
    public void run() {
        releasePlayer();

//        setVideoOneVLCStreamURL(mMediaLink);


//        mScheduledExecutorService.scheduleAtFixedRate(new Runnable() {
//            @Override
//            public void run() {

//            }
//        }, 0, RUNNABLE_INTERVAL_IN_SEC, TimeUnit.SECONDS);
    }


    /**
     * Creates VLC MediaPlayerOne and plays video
     *
     * @param media
     */
    private void setVideoOneVLCStreamURL(String media) {
        releasePlayer();
        try {
            // Create LibVLC
            // TODO: make this more robust, and sync with audio demo
            ArrayList<String> options = new ArrayList<String>();
            //options.add("--subsdec-encoding <encoding>");
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            mLibVlc = new LibVLC(mContext, options);
//            mSurfaceHolderOne.setKeepScreenOn(true);

            // Create media controller
            mTextureView.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    mVideoStreamControllerView.show(MEDIA_CONTROLLER_SHOW_DURATION);
                }
            });

            // Creating media player
            mMediaPlayer = new MediaPlayer(mLibVlc);
            mMediaPlayer.setEventListener(mPlayerOneListener);

            // Seting up video output
            final IVLCVout vout = mMediaPlayer.getVLCVout();
            vout.setVideoView(mTextureView);
            //vout.setSubtitlesView(mSurfaceSubtitles);
            vout.addCallback(firstIVLCVoutCallback);
            vout.attachViews();

            Media m = new Media(mLibVlc, Uri.parse(media));
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Exception e) {
//            Toast.makeText(getActivity(), "Error in creating player!", Toast
//                    .LENGTH_LONG).show();
        }
    }

    /**
     * Registering callbacks for VLC Player One
     */
    private MediaPlayer.EventListener mPlayerOneListener = new VideoStreamRunnable.MyPlayerOneListener(this);

    private static class MyPlayerOneListener implements MediaPlayer.EventListener {
        private WeakReference<VideoStreamRunnable> mOwner;

        public MyPlayerOneListener(VideoStreamRunnable owner) {
            mOwner = new WeakReference<>(owner);
        }

        @Override
        public void onEvent(MediaPlayer.Event event) {
            VideoStreamRunnable player = mOwner.get();

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
            releasePlayer();
        }
    };

    private void releasePlayer() {
        if (mLibVlc == null)
            return;

        Log.i(TAG, "Releasing video player one");
        mMediaPlayer.stop();

        mLibVlc.release();
        mLibVlc = null;

        final IVLCVout vOutOne = mMediaPlayer.getVLCVout();
        mMediaPlayer.release();
        vOutOne.removeCallback(mFirstIVLCVoutCallback);
        vOutOne.detachViews();
    }
}
