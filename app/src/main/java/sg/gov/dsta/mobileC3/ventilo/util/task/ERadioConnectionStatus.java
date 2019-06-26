package sg.gov.dsta.mobileC3.ventilo.util.task;

import java.util.HashMap;

public enum ERadioConnectionStatus {
    CONNECTED("Connected"), DISCONNECTED("Disconnected"), ONLINE("Online"), OFFLINE("Offline");

    public static HashMap<String, ERadioConnectionStatus> statusMapping = new HashMap<>();
    private final String name;

    static {
        statusMapping.put(ONLINE.toString(), ONLINE);
        statusMapping.put(OFFLINE.toString(), OFFLINE);
    }

    ERadioConnectionStatus(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static ERadioConnectionStatus getERadioConnectionStatus(String radioConnectionStatus) {
        if (statusMapping.get(radioConnectionStatus) == null) {
            throw new RuntimeException("There is no ERadioConnectionStatus " +
                    "mapping with name (" + radioConnectionStatus + ")");
        }

        return statusMapping.get(radioConnectionStatus);
    }
}
