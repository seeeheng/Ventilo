package sg.gov.dsta.mobileC3.ventilo.activity.task;

import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.View;

import sg.gov.dsta.mobileC3.ventilo.util.DimensionUtil;

public class TaskItemTouchHelperCallback extends ItemTouchHelper.SimpleCallback {

    private TaskRecyclerItemTouchHelperListener mListener;

    private static final int DEFAULT_DRAG_DIRS = 0;
    private static final int DEFAULT_SWIPE_DIRS = ItemTouchHelper.LEFT;

    /**
     * Each swipe action button has a width of 43dp and there are 3 of such buttons.
     * Hence, a width of 43 * 3 = 132 is required to sufficiently display all swipe action buttons.
     */
    private static final int DX_THRESHOLD_IN_DP = -132;
    private static final int DX_THRESHOLD_IN_PIXEL = DimensionUtil.convertDpsToPixel(DX_THRESHOLD_IN_DP);

    // Divisor is needed to convert Pixel to Dp for comparison with DX_THRESHOLD_IN_DP
    private static final float DX_DIVISOR_THRESHOLD = 4.659f;

    private float mLastDXPositionInPixel;
    private boolean isSwipeActionBtnsVisible;
//    private static final int FIRST_SWIPE_ACTION_BUTTON_MARGIN_END_IN_DP = 90;
//    private static final int SECOND_SWIPE_ACTION_BUTTON_MARGIN_END_IN_DP = 45;

    public TaskItemTouchHelperCallback(TaskRecyclerItemTouchHelperListener listener) {
        this(DEFAULT_SWIPE_DIRS, listener);
    }

    public TaskItemTouchHelperCallback(int swipeDirs, TaskRecyclerItemTouchHelperListener listener) {
        this(DEFAULT_DRAG_DIRS, swipeDirs, listener);
    }

    public TaskItemTouchHelperCallback(int dragDirs, int swipeDirs, TaskRecyclerItemTouchHelperListener listener) {
        super(dragDirs, swipeDirs);
        mListener = listener;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof TaskViewHolder) {
            System.out.println("onSwiped");
            ((TaskViewHolder) viewHolder).getRelativeLayoutBackground().setElevation(2);
            ((TaskViewHolder) viewHolder).getRelativeLayoutForeground().clearFocus();
        }

//        if (viewHolder instanceof TaskViewHolder) {
//            TaskViewHolder taskViewHolder = (TaskViewHolder) viewHolder;
//            String taskStatus = taskViewHolder.getTvStatus().getText().toString().trim();
//
//            if (EStatus.NEW.toString().equalsIgnoreCase(taskStatus)) {
//                showInProgressAndCompleteSwipeActionButtons(taskViewHolder);
//            } else if (EStatus.IN_PROGRESS.toString().equalsIgnoreCase(taskStatus)) {
//                showNewAndCompleteSwipeActionButtons(taskViewHolder);
//            } else {
//                showNewAndInProgressSwipeActionButtons(taskViewHolder);
//            }
//        }
//        System.out.println("onSwiped!");

    }

    @Override
    public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {

        boolean isDirectionRight = mLastDXPositionInPixel < dX;
        mLastDXPositionInPixel = dX;
        float dXValue = DimensionUtil.convertPixelToDps(dX);

        /**
         *  isCurrentlyActive = true indicates user is currently swiping (finger is on screen);
         *  isCurrentlyActive = false indicates user's finger lets go of screen
         **/
        if (isCurrentlyActive) {
            if (isSwipeActionBtnsVisible) {
                dX = dX / DX_DIVISOR_THRESHOLD;
            }
        } else {
            dX = dX / DX_DIVISOR_THRESHOLD;

            // value at -615dp is the dX point of complete motionless where
            if (dXValue == -615) {
                isSwipeActionBtnsVisible = true;
            } else {
                isSwipeActionBtnsVisible = false;
            }
        }

        if (dXValue < DX_THRESHOLD_IN_DP && !isDirectionRight) {
            dXValue = DX_THRESHOLD_IN_PIXEL + ((dX - DX_THRESHOLD_IN_PIXEL) / DX_DIVISOR_THRESHOLD);
        } else {
            dXValue = dX;
        }

        if (viewHolder instanceof TaskViewHolder) {
            TaskViewHolder taskViewHolder = (TaskViewHolder) viewHolder;

            final View foregroundView = taskViewHolder.getRelativeLayoutForeground();
            getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView,
                    dXValue, dY, actionState, isCurrentlyActive);
            mListener.onChildDraw(taskViewHolder);
        }
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (viewHolder instanceof TaskViewHolder) {
            TaskViewHolder taskViewHolder = (TaskViewHolder) viewHolder;
            final View foregroundView = taskViewHolder.getRelativeLayoutForeground();
            getDefaultUIUtil().clearView(foregroundView);
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                            float dX, float dY, int actionState, boolean isCurrentlyActive) {
        // view the background view
        boolean isDirectionRight = mLastDXPositionInPixel < dX;
        mLastDXPositionInPixel = dX;
        float dXValue = DimensionUtil.convertPixelToDps(dX);

        System.out.println("actionState is " + actionState);
        /**
         *  isCurrentlyActive = true indicates user is currently swiping (finger is on screen);
         *  isCurrentlyActive = false indicates user's finger lets go of screen
         **/
        if (isCurrentlyActive) {
            if (isSwipeActionBtnsVisible) {
                dX = dX / DX_DIVISOR_THRESHOLD;
            }
        } else {
            dX = dX / DX_DIVISOR_THRESHOLD;

            // value at -615dp is the dX point of complete motionless where
            if (dXValue == -615) {
                isSwipeActionBtnsVisible = true;
            } else {
                isSwipeActionBtnsVisible = false;
            }
        }

        if (dXValue < DX_THRESHOLD_IN_DP && !isDirectionRight) {
            dXValue = DX_THRESHOLD_IN_PIXEL + ((dX - DX_THRESHOLD_IN_PIXEL) / DX_DIVISOR_THRESHOLD);
        } else {
            dXValue = dX;
        }

        if (viewHolder instanceof TaskViewHolder) {
            TaskViewHolder taskViewHolder = (TaskViewHolder) viewHolder;
            final View foregroundView = taskViewHolder.getRelativeLayoutForeground();

            getDefaultUIUtil().onDraw(c, recyclerView, foregroundView,
                    dXValue, dY, actionState, isCurrentlyActive);
        }
    }
}
