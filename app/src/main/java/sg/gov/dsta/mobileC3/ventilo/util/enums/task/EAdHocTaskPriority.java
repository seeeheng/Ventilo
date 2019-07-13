package sg.gov.dsta.mobileC3.ventilo.util.enums.task;

import java.util.HashMap;

public enum EAdHocTaskPriority {
    HIGH("High"), LOW("Low");

    public static HashMap<String, EAdHocTaskPriority> statusMapping = new HashMap<>();
    private final String name;

    static {
        statusMapping.put(HIGH.toString(), HIGH);
        statusMapping.put(LOW.toString(), LOW);
    }

    EAdHocTaskPriority(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static EAdHocTaskPriority getEAdHocTaskPriority(String adHocTaskPriorityName) {
        if (statusMapping.get(adHocTaskPriorityName) == null) {
            throw new RuntimeException("There is no EAdHocTaskPriority " +
                    "mapping with name (" + adHocTaskPriorityName + ")");
        }

        return statusMapping.get(adHocTaskPriorityName);
    }
}
