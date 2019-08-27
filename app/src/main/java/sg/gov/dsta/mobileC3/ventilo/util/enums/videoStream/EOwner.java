package sg.gov.dsta.mobileC3.ventilo.util.enums.videoStream;

import java.util.HashMap;

public enum EOwner {
    OWN("OWN"), OTHERS("Others");

    public static HashMap<String, EOwner> ownerMapping = new HashMap<>();
    private final String name;

    static {
        ownerMapping.put(OWN.toString(), OWN);
        ownerMapping.put(OTHERS.toString(), OTHERS);
    }

    EOwner(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public static EOwner getEOwner(String owner) {
        if (ownerMapping.get(owner) == null) {
            throw new RuntimeException("There is no EOwner " +
                    "mapping with name (" + owner + ")");
        }

        return ownerMapping.get(owner);
    }
}
