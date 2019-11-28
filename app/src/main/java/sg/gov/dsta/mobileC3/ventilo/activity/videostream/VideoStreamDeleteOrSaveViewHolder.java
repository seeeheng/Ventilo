package sg.gov.dsta.mobileC3.ventilo.activity.videostream;

import androidx.lifecycle.ViewModelProviders;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sg.gov.dsta.mobileC3.ventilo.R;
import sg.gov.dsta.mobileC3.ventilo.activity.main.MainActivity;
import sg.gov.dsta.mobileC3.ventilo.model.videostream.VideoStreamModel;
import sg.gov.dsta.mobileC3.ventilo.model.viewmodel.VideoStreamViewModel;
import sg.gov.dsta.mobileC3.ventilo.util.component.C2OpenSansRegularEditTextView;
import sg.gov.dsta.mobileC3.ventilo.util.constant.FragmentConstants;
import sg.gov.dsta.mobileC3.ventilo.util.constant.MainNavigationConstants;

@Data
@EqualsAndHashCode(callSuper = false)
public class VideoStreamDeleteOrSaveViewHolder extends RecyclerView.ViewHolder {

    private ImageView imgVideoStreamDelete;
    private C2OpenSansRegularEditTextView etvVideoStreamName;
    private C2OpenSansRegularEditTextView etvVideoStreamURL;
    private ImageView imgVideoStreamEditOrSave;

    private VideoStreamDeleteOrSaveRecyclerAdapter mAdapter;

    protected VideoStreamDeleteOrSaveViewHolder(View itemView, VideoStreamDeleteOrSaveRecyclerAdapter adapter) {
        super(itemView);

        imgVideoStreamDelete = itemView.findViewById(R.id.img_video_stream_delete);
        etvVideoStreamName = itemView.findViewById(R.id.etv_video_stream_name);
        etvVideoStreamURL = itemView.findViewById(R.id.etv_video_stream_url);
        imgVideoStreamEditOrSave = itemView.findViewById(R.id.img_video_stream_edit_save);

        imgVideoStreamDelete.setOnClickListener(onDeleteVideoStreamItemClickListener);
        imgVideoStreamEditOrSave.setOnClickListener(onEditOrSaveVideoStreamItemClickListener);

        mAdapter = adapter;
    }

    private View.OnClickListener onDeleteVideoStreamItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getContext() instanceof MainActivity) {
                MainActivity mainActivity = ((MainActivity) view.getContext());

                FragmentManager manager = mainActivity.getBaseChildFragmentOfCurrentFragment(
                        MainNavigationConstants.SIDE_MENU_TAB_VIDEO_STREAM_POSITION_ID).
                        getChildFragmentManager();
                VideoStreamAddFragment videoStreamAddFragment = (VideoStreamAddFragment)
                        manager.findFragmentByTag(VideoStreamAddFragment.class.getSimpleName());

                VideoStreamViewModel videoStreamViewModel = ViewModelProviders.
                        of(videoStreamAddFragment).get(VideoStreamViewModel.class);
                videoStreamViewModel.deleteVideoStream(mAdapter.getVideoStreamListItems().
                        get(getAdapterPosition()).getId());
            }
        }
    };

    private View.OnClickListener onEditOrSaveVideoStreamItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            boolean isFieldEmpty = false;

            if (TextUtils.isEmpty(etvVideoStreamName.getText().toString().trim())) {
                isFieldEmpty = true;
                etvVideoStreamName.setError(view.getContext().getString(R.string.video_stream_error_field_required));
                etvVideoStreamName.requestFocus();
            }

            if (TextUtils.isEmpty(etvVideoStreamURL.getText().toString().trim())) {
                etvVideoStreamURL.setError(view.getContext().getString(R.string.video_stream_error_field_required));

                if (!isFieldEmpty) {
                    etvVideoStreamURL.requestFocus();
                }

                isFieldEmpty = true;
            }

            if (!isFieldEmpty) {
                etvVideoStreamName.clearFocus();
                etvVideoStreamName.setError(null);
                etvVideoStreamURL.clearFocus();
                etvVideoStreamURL.setError(null);

                if (mAdapter.getVideoStreamAddFragment() != null &&
                        mAdapter.getVideoStreamAddFragment() instanceof VideoStreamAddFragment) {
                    VideoStreamAddFragment fragment = (VideoStreamAddFragment) mAdapter.getVideoStreamAddFragment();

                    VideoStreamViewModel videoStreamViewModel = ViewModelProviders.
                            of(fragment).get(VideoStreamViewModel.class);
                    VideoStreamModel selVideoStreamModel = mAdapter.getVideoStreamListItems().get(getAdapterPosition());

                    if (selVideoStreamModel.getIconType().equalsIgnoreCase(FragmentConstants.KEY_VIDEO_STREAM_EDIT)) {
                        etvVideoStreamName.setEnabled(true);
                        etvVideoStreamURL.setEnabled(true);
                        imgVideoStreamEditOrSave.setImageDrawable(view.getContext().getDrawable(R.drawable.btn_save));

                        selVideoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_SAVE);
                    } else {
                        etvVideoStreamName.setEnabled(false);
                        etvVideoStreamURL.setEnabled(false);
                        imgVideoStreamEditOrSave.setImageDrawable(view.getContext().getDrawable(R.drawable.btn_edit));

                        selVideoStreamModel.setName(etvVideoStreamName.getText().toString().trim());
                        selVideoStreamModel.setUrl(etvVideoStreamURL.getText().toString().trim());
                        selVideoStreamModel.setIconType(FragmentConstants.KEY_VIDEO_STREAM_EDIT);
                    }

                    videoStreamViewModel.updateVideoStream(selVideoStreamModel);
                }
            }
        }
    };
}