package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamItemModel;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.SharedPreferenceConstants;

public class VideoStreamDeleteOrSaveRecyclerAdapter extends RecyclerView.Adapter<VideoStreamDeleteOrSaveViewHolder> {

    List<VideoStreamItemModel> mVideoStreamListItems;

    private Context mContext;
    private RecyclerView mRecyclerView;

    public VideoStreamDeleteOrSaveRecyclerAdapter(Context context, List<VideoStreamItemModel> videoStreamListItems) {
        this.mContext = context;
        mVideoStreamListItems = videoStreamListItems;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public VideoStreamDeleteOrSaveViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.recycler_view_row_video_stream, viewGroup, false);

        return new VideoStreamDeleteOrSaveViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(VideoStreamDeleteOrSaveViewHolder itemViewHolder, final int i) {
        VideoStreamItemModel videoStreamItemModel = mVideoStreamListItems.get(i);

        itemViewHolder.getImgVideoStreamDelete().setImageDrawable(
                mContext.getDrawable(R.drawable.btn_video_delete));
        itemViewHolder.getEtvVideoStreamName().setText(videoStreamItemModel.getName());
        itemViewHolder.getEtvVideoStreamURL().setText(videoStreamItemModel.getUrl());

        if (FragmentConstants.KEY_VIDEO_STREAM_EDIT.equalsIgnoreCase(videoStreamItemModel.getIconType())) {
            itemViewHolder.getEtvVideoStreamName().setEnabled(false);
            itemViewHolder.getEtvVideoStreamURL().setEnabled(false);
            itemViewHolder.getImgVideoStreamEditOrSave().setImageDrawable(mContext.getDrawable(R.drawable.btn_edit));
        } else {
            itemViewHolder.getEtvVideoStreamName().setEnabled(true);
            itemViewHolder.getEtvVideoStreamURL().setEnabled(true);
            itemViewHolder.getImgVideoStreamEditOrSave().setImageDrawable(mContext.getDrawable(R.drawable.btn_save));
        }

//        itemViewHolder.getImgVideoStreamDelete().setOnClickListener(deleteVideoStreamItemListener);
//        itemViewHolder.getImgVideoStreamEditOrSave().setOnClickListener(editOrSaveVideoStreamItemListener);

//        itemViewHolder.getImgVideoStreamDelete().setOnClickListener(deleteVideoStreamOnClickListener);
    }

    public void addItem(String name, String url, String iconType) {
        VideoStreamItemModel newVideoStreamItemModel = new VideoStreamItemModel();
        newVideoStreamItemModel.setId(mVideoStreamListItems.size() + 1);
        newVideoStreamItemModel.setName(name);
        newVideoStreamItemModel.setUrl(url);
        newVideoStreamItemModel.setIconType(iconType);

        mVideoStreamListItems.add(newVideoStreamItemModel);
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        mVideoStreamListItems.remove(position);
        mRecyclerView.removeViewAt(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, mVideoStreamListItems.size());
    }

    public void updateData(List<VideoStreamItemModel> videoStreamListItems) {
        mVideoStreamListItems.clear();
        mVideoStreamListItems.addAll(videoStreamListItems);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mVideoStreamListItems.size();
    }
//
//    private View.OnClickListener deleteVideoStreamOnClickListener = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            deleteItem(view.getId());
//        }
//    };
}
