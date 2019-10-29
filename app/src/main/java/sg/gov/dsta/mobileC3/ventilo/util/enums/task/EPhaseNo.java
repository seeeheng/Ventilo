package sg.gov.dsta.mobileC3.ventilo.util.enums.task;

import java.util.HashMap;

public enum EPhaseNo {
    ONE("One"), TWO("Two"), THREE("Three"), FOUR("Four"), AD_HOC("Ad Hoc");

    private static HashMap<String, EPhaseNo> statusMapping = new HashMap<>();
    private final String name;

    static {
        statusMapping.put(ONE.toString(), ONE);
        statusMapping.put(TWO.toString(), TWO);
        statusMapping.put(THREE.toString(), THREE);
        statusMapping.put(FOUR.toString(), FOUR);
        statusMapping.put(AD_HOC.toString(), AD_HOC);
    }

    EPhaseNo(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static EPhaseNo getEPhaseNo(String phaseNo) {
        if (statusMapping.get(phaseNo) == null) {
            throw new RuntimeException("There is no EPhaseNo " +
                    "mapping with name (" + phaseNo + ")");
        }

        return statusMapping.get(phaseNo);
    }
}
