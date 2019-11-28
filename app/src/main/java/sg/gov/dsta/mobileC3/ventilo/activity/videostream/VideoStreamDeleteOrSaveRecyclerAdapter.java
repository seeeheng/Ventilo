package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import androidx.lifecycle.ViewModelProviders;
import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;

public class VideoStreamDeleteOrSaveRecyclerAdapter extends RecyclerView.Adapter<VideoStreamDeleteOrSaveViewHolder> {

    List<VideoStreamModel> mVideoStreamListItems;

    private Context mContext;
    private Fragment mFragment;

    public VideoStreamDeleteOrSaveRecyclerAdapter(Context context, Fragment fragment, List<VideoStreamModel> videoStreamListItems) {
        this.mContext = context;
        mFragment = fragment;
        mVideoStreamListItems = videoStreamListItems;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public VideoStreamDeleteOrSaveViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_video_stream, viewGroup, false);

        return new VideoStreamDeleteOrSaveViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(VideoStreamDeleteOrSaveViewHolder itemViewHolder, final int i) {
        VideoStreamModel videoStreamModel = mVideoStreamListItems.get(i);

        itemViewHolder.getImgVideoStreamDelete().setImageDrawable(
                mContext.getDrawable(R.drawable.btn_video_delete));
        itemViewHolder.getEtvVideoStreamName().setText(videoStreamModel.getName());
        itemViewHolder.getEtvVideoStreamURL().setText(videoStreamModel.getUrl());

        if (FragmentConstants.KEY_VIDEO_STREAM_EDIT.equalsIgnoreCase(videoStreamModel.getIconType())) {
            itemViewHolder.getEtvVideoStreamName().setEnabled(false);
            itemViewHolder.getEtvVideoStreamURL().setEnabled(false);
            itemViewHolder.getImgVideoStreamEditOrSave().setImageDrawable(mContext.getDrawable(R.drawable.btn_edit));
        } else {
            itemViewHolder.getEtvVideoStreamName().setEnabled(true);
            itemViewHolder.getEtvVideoStreamURL().setEnabled(true);
            itemViewHolder.getImgVideoStreamEditOrSave().setImageDrawable(mContext.getDrawable(R.drawable.btn_save));
        }
    }

    public void setVideoStreamListItems(List<VideoStreamModel> videoStreamListItems) {
        mVideoStreamListItems = videoStreamListItems;
        notifyDataSetChanged();
    }

    public Fragment getVideoStreamAddFragment() {
        return mFragment;
    }

    public List<VideoStreamModel> getVideoStreamListItems() {
        return mVideoStreamListItems;
    }

    @Override
    public int getItemCount() {
        return mVideoStreamListItems.size();
    }
}
