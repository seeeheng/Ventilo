package sg.gov.dsta.mobileC3.ventilo.util.task;

import java.util.HashMap;

public enum EAccessRight {
    CCT("CCT"), TEAM_LEAD("Team Lead");

    public static HashMap<String, EAccessRight> statusMapping = new HashMap<>();
    private final String name;

    static {
        statusMapping.put(CCT.toString(), CCT);
        statusMapping.put(TEAM_LEAD.toString(), TEAM_LEAD);
    }

    EAccessRight(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static EAccessRight getEAccessRight(String accessRight) {
        if (statusMapping.get(accessRight) == null) {
            throw new RuntimeException("There is no EAccessRight " +
                    "mapping with name (" + accessRight + ")");
        }

        return statusMapping.get(accessRight);
    }
}
