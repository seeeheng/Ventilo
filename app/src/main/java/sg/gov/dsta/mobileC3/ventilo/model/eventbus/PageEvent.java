package sg.gov.dsta.mobileC3.ventilo.model.eventbus;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class PageEvent {

    public static final int FRAGMENT_KEY = 0;
    public static final int ACTIVITY_KEY = 0;
    private static final PageEvent INSTANCE = new PageEvent();
    private Map<Integer, String> mPastPages;
//    private String previousFragmentName;

    private PageEvent() {
        mPastPages = new LinkedHashMap<>();
    }

    public static PageEvent getInstance() {
        return INSTANCE;
    }

    public PageEvent addPage(int key, String previousActivityOrFragmentName) {
        if (mPastPages.size() == 5) {
            mPastPages.remove(0);
        }

        mPastPages.put(key, previousActivityOrFragmentName);

        return INSTANCE;
//        this.previousFragmentName = previousFragmentName;
    }

    public String getPreviousActivityName() {
        Map.Entry<Integer, String> lastElement = getLastPageEntry();
        String previousActivityName = "";

        if (lastElement != null && lastElement.getKey() == ACTIVITY_KEY) {
            previousActivityName = lastElement.getValue();
        }

        return previousActivityName;
    }

    public String getPreviousFragmentName() {
        Map.Entry<Integer, String> lastElement = getLastPageEntry();
        String previousFragmentName = "";

        if (lastElement != null && lastElement.getKey() == FRAGMENT_KEY) {
            previousFragmentName = lastElement.getValue();
        }

        return previousFragmentName;
    }

    private Map.Entry<Integer, String> getLastPageEntry() {
        Map.Entry<Integer, String> lastElement = null;

        Iterator iterator = mPastPages.entrySet().iterator();
        while (iterator.hasNext()) {
            lastElement = (Map.Entry<Integer, String>) iterator.next();
        }

        return lastElement;
    }
}
