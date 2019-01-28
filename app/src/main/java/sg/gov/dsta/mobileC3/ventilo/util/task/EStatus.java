package sg.gov.dsta.mobileC3.ventilo.util.task;

public enum EStatus {
    NEW("New"), IN_PROGRESS("In Progress"), DONE("Done");

    private final String name;

    EStatus(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }
}
