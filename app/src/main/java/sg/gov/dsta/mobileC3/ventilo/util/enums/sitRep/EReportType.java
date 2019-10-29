package sg.gov.dsta.mobileC3.ventilo.util.enums.sitRep;

import java.util.HashMap;

public enum EReportType {
    MISSION("Mission"), INSPECTION("Inspection");

    private static HashMap<String, EReportType> statusMapping = new HashMap<>();
    private final String name;

    static {
        statusMapping.put(MISSION.toString(), MISSION);
        statusMapping.put(INSPECTION.toString(), INSPECTION);
    }

    EReportType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static EReportType getEReportType(String reportType) {
        if (statusMapping.get(reportType) == null) {
            throw new RuntimeException("There is no EReportType " +
                    "mapping with name (" + reportType + ")");
        }

        return statusMapping.get(reportType);
    }
}
