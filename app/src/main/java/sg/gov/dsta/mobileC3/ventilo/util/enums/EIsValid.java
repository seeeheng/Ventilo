package sg.gov.dsta.mobileC3.ventilo.util.enums;

import java.util.HashMap;

public enum EIsValid {
    YES("Yes"), NO("No");

    public static HashMap<String, EIsValid> statusMapping = new HashMap<>();
    private final String name;

    static {
        statusMapping.put(YES.toString(), YES);
        statusMapping.put(NO.toString(), NO);
    }

    EIsValid(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static EIsValid getEIsValid(String isValid) {
        if (statusMapping.get(isValid) == null) {
            throw new RuntimeException("There is no EIsValid " +
                    "mapping with name (" + isValid + ")");
        }

        return statusMapping.get(isValid);
    }
}
