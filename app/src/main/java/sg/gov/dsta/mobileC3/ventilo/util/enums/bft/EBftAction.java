package sg.gov.dsta.mobileC3.ventilo.util.enums.bft;

import java.util.HashMap;

public enum EBftAction {
    FORWARD("FORWARD"), FIDGETING("FIDGETING"),
    STATIONARY("STATIONARY"), BEACONDROP("BEACONDROP");

    public static HashMap<String, EBftAction> statusMapping = new HashMap<>();
    private final String name;

    static {
        statusMapping.put(FORWARD.toString(), FORWARD);
        statusMapping.put(FIDGETING.toString(), FIDGETING);
        statusMapping.put(STATIONARY.toString(), STATIONARY);
        statusMapping.put(BEACONDROP.toString(), BEACONDROP);
    }

    EBftAction(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static EBftAction getEBftType(String bftType) {
        if (statusMapping.get(bftType) == null) {
            throw new RuntimeException("There is no getEBftType " +
                    "mapping with name (" + bftType + ")");
        }

        return statusMapping.get(bftType);
    }
}
