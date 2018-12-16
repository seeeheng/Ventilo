package dsta.sg.com.ventilo.activity.videostream;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.VideoView;

import dsta.sg.com.ventilo.R;

public class VideoStreamFragment extends Fragment {

    private VideoView mVideoViewOne;
    private VideoView mVideoViewTwo;
    private VideoView mVideoViewThree;
    private VideoView mVideoViewFour;

    private MediaController mMediaControllerOne;
    private MediaController mMediaControllerTwo;
    private MediaController mMediaControllerThree;
    private MediaController mMediaControllerFour;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootVideoStreamView = inflater.inflate(R.layout.fragment_video_stream, container, false);
        initUI(rootVideoStreamView);

        setVideoOneStream();
        setVideoTwoStream();
//        setVideoThreeStream();
//        setVideoFourStream();

        return rootVideoStreamView;
    }

    private void initUI(View rootVideoStreamView) {
        mVideoViewOne = rootVideoStreamView.findViewById(R.id.video_view_stream_one);
        mVideoViewTwo = rootVideoStreamView.findViewById(R.id.video_view_stream_two);
        mVideoViewThree = rootVideoStreamView.findViewById(R.id.video_view_stream_three);
        mVideoViewFour = rootVideoStreamView.findViewById(R.id.video_view_stream_four);
    }

    private void setVideoOneStream() {
        mMediaControllerOne = new MediaController(getActivity());
//        mediaControllerOne.setAnchorView(mVideoViewOne);
//        mediaControllerOne.setMediaPlayer(mVideoViewOne);
        mVideoViewOne.setMediaController(mMediaControllerOne);

        Uri video = Uri.parse("rtsp://184.72.239.149/vod/mp4:BigBuckBunny_175k.mov");
        mVideoViewOne.setVideoURI(video);

        mVideoViewOne.post(new Runnable() {
            @Override
            public void run() {
                mMediaControllerOne.show(0);
            }
        });

//        mVideoViewOne.start();
//        mVideoViewOne.requestFocus();
    }

    private void setVideoTwoStream() {
        mMediaControllerTwo = new MediaController(getActivity());
        mVideoViewTwo.setMediaController(mMediaControllerTwo);

        Uri video = Uri.parse("http://archive.org/download/SampleMpeg4_201307/sample_mpeg4.mp4");
        mVideoViewTwo.setVideoURI(video);

        mVideoViewTwo.post(new Runnable() {
            @Override
            public void run() {
                mMediaControllerTwo.show(0);
            }
        });

//        mVideoViewTwo.start();
    }

    private void setVideoThreeStream() {
        mMediaControllerThree = new MediaController(getActivity());
        mVideoViewThree.setMediaController(mMediaControllerThree);

        Uri video = Uri.parse("rtsp://mm2.pcslab.com/mm/7h800.mp4");
        mVideoViewThree.setVideoURI(video);

        mVideoViewThree.post(new Runnable() {
            @Override
            public void run() {
                mMediaControllerThree.show(0);
            }
        });

//        mVideoViewThree.start();
    }

    private void setVideoFourStream() {
        mMediaControllerFour = new MediaController(getActivity());
        mVideoViewFour.setMediaController(mMediaControllerFour);
//        mVideoViewFour.setVideoPath("https://drive.google.com/file/d/0BwxFVkl63-lETWMzM1dRZF9XMDA/view");

        Uri video = Uri.parse("https://drive.google.com/file/d/0BwxFVkl63-lETWMzM1dRZF9XMDA/view");
        mVideoViewFour.setVideoURI(video);

        mVideoViewFour.post(new Runnable() {
            @Override
            public void run() {
                mMediaControllerFour.show(0);
            }
        });

//        mVideoViewFour.start();
    }
}
