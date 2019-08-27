package sg.gov.dsta.mobileC3.ventilo.util.enums.bft;

import java.util.HashMap;

public enum EBftType {
    OWN("Own"), OWN_STALE("Own-Stale"),
    HAZARD("Hazard"), HAZARD_STALE("Hazard-Stale"),
    DECEASED("Deceased"), DECEASED_STALE("Deceased-Stale");

    public static HashMap<String, EBftType> statusMapping = new HashMap<>();
    private final String name;

    static {
        statusMapping.put(OWN.toString(), OWN);
        statusMapping.put(OWN_STALE.toString(), OWN_STALE);
        statusMapping.put(HAZARD.toString(), HAZARD);
        statusMapping.put(HAZARD_STALE.toString(), HAZARD_STALE);
        statusMapping.put(DECEASED.toString(), DECEASED);
        statusMapping.put(DECEASED_STALE.toString(), DECEASED_STALE);
    }

    EBftType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static EBftType getEBftType(String bftType) {
        if (statusMapping.get(bftType) == null) {
            throw new RuntimeException("There is no getEBftType " +
                    "mapping with name (" + bftType + ")");
        }

        return statusMapping.get(bftType);
    }
}
