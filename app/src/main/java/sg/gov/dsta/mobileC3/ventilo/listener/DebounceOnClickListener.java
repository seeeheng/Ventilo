package sg.gov.dsta.mobileC3.ventilo.listener;

import android.os.SystemClock;
import android.view.View;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A Debounced OnClickListener
 * Rejects clicks that are too close together in time.
 * This class is safe to use as an OnClickListener for multiple views, and will debounce each one separately.
 */
public abstract class DebounceOnClickListener implements View.OnClickListener {

    private final long minimumIntervalMillis;
    private Map<View, Long> lastClickedMap;

    /**
     * Implement this in subclass instead of onClick
     *
     * @param v The view that was clicked
     */
    public abstract void onDebouncedClick(View v);

    /**
     * The minimum allowed time between clicks - any click sooner than this after a previous click will be rejected
     *
     * @param minimumIntervalMillis
     */
    public DebounceOnClickListener(long minimumIntervalMillis) {
        this.minimumIntervalMillis = minimumIntervalMillis;
        this.lastClickedMap = new WeakHashMap<>();
    }

    @Override
    public void onClick(View clickedView) {
        Long previousClickedTimestamp = lastClickedMap.get(clickedView);
        long currentTimestamp = SystemClock.uptimeMillis();

        lastClickedMap.put(clickedView, currentTimestamp);
        if(previousClickedTimestamp == null || Math.abs(currentTimestamp - previousClickedTimestamp) > minimumIntervalMillis) {
            onDebouncedClick(clickedView);
        }
    }
}