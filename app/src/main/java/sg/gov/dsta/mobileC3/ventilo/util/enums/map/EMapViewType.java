package sg.gov.dsta.mobileC3.ventilo.util.enums.map;

import java.util.HashMap;

public enum EMapViewType {
    DECK("Deck"), SIDE("Side"), FRONT("Front");

    public static HashMap<String, EMapViewType> statusMapping = new HashMap<>();
    private final String name;

    static {
        statusMapping.put(DECK.toString(), DECK);
        statusMapping.put(SIDE.toString(), SIDE);
        statusMapping.put(FRONT.toString(), FRONT);
    }

    EMapViewType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static EMapViewType getEMapViewType(String mapViewType) {
        if (statusMapping.get(mapViewType) == null) {
            throw new RuntimeException("There is no EMapViewType mapping with name (" + mapViewType + ")");
        }

        return statusMapping.get(mapViewType);
    }
}
