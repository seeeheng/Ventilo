package sg.gov.dsta.mobileC3.ventilo.model.eventbus;

public class TaskEvent {
    private static final TaskEvent INSTANCE = new TaskEvent();
    private int mItemPos;
    private boolean mIsNavigateToViewTask;

    private TaskEvent() {}

    public static TaskEvent getInstance() {
        return INSTANCE;
    }

    public TaskEvent setItemPos(int itemPos) {
        this.mItemPos = itemPos;
        this.mIsNavigateToViewTask = true;
        return INSTANCE;
    }

    public TaskEvent setNavigateToViewTask(boolean isNavigateToViewTask) {
        this.mIsNavigateToViewTask = isNavigateToViewTask;
        return INSTANCE;
    }

    public int getItemPos() {
        return mItemPos;
    }

    public boolean isNavigateToViewTask() {
        return mIsNavigateToViewTask;
    }
}
