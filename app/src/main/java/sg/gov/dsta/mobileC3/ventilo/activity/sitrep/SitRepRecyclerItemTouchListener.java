package sg.gov.dsta.mobileC3.ventilo.activity.sitrep;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class SitRepRecyclerItemTouchListener implements RecyclerView.OnItemTouchListener {

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private OnItemClickListener mListener;
    private GestureDetector mGestureDetector;

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
        void onLongItemClick(View view, int position);
        void onSwipeLeft(View view, int position);
        void onSwipeRight(View view, int position);
    }

    SitRepRecyclerItemTouchListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
//                return super.onSingleTapUp(e);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
//                View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
//                if(child != null && mListener != null) {
//                    mListener.onLongItemClick(child, recyclerView.getChildAdapterPosition(child));
//                }
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                return super.onFling(e1, e2, velocityX, velocityY);
//                int dx = (int) (e2.getX() - e1.getX());
//                int dy = (int) (e2.getY() - e1.getY());
//                if(Math.abs(dy) > SWIPE_MAX_OFF_PATH) {
//                    return false;
//                }
//                if(Math.abs(dx) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    View child = recyclerView.findChildViewUnder(e1.getX(), e1.getY());
//                    if(dx < 0) {
//                        // Swipe Left
//                        if(child != null && mListener != null) {
//                            mListener.onSwipeLeft(child, recyclerView.getChildAdapterPosition(child));
//                        }
//                    } else if(dx > 0){
//                        // Swipe right
//                        if(child != null && mListener != null) {
//                            mListener.onSwipeRight(child, recyclerView.getChildAdapterPosition(child));
//                        }
//                    }
//                }
//                return false;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {
        View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if(childView != null && mListener != null && mGestureDetector.onTouchEvent(motionEvent)) {
            mListener.onItemClick(childView, recyclerView.getChildAdapterPosition(childView));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView recyclerView, @NonNull MotionEvent motionEvent) {}

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {}
}
