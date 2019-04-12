package sg.gov.dsta.mobileC3.ventilo.util.task;

import java.util.HashMap;

public enum EStatus {
    NEW("New"), IN_PROGRESS("In Progress"), COMPLETE("Complete"), NA("N.A.");

    public static HashMap<String, EStatus> statusMapping = new HashMap<>();
    private final String name;

    static {
        statusMapping.put(NEW.toString(), NEW);
        statusMapping.put(IN_PROGRESS.toString(), IN_PROGRESS);
        statusMapping.put(COMPLETE.toString(), COMPLETE);
        statusMapping.put(NA.toString(), NA);
    }

    EStatus(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static EStatus getEStatus(String statusName) {
        if (statusMapping.get(statusName) == null) {
            throw new RuntimeException("There is no EStatus mapping with name (" + statusName + ")");
        }

        return statusMapping.get(statusName);
    }
}
