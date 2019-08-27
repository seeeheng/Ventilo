package sg.gov.dsta.mobileC3.ventilo.model.eventbus;

public class BftEvent {
    private static final BftEvent INSTANCE = new BftEvent();
    private String mBftMessage;

    private BftEvent() {}

    public static BftEvent getInstance() {
        return INSTANCE;
    }

    public BftEvent setBftMessage(String bftMessage) {
        this.mBftMessage = bftMessage;
        return INSTANCE;
    }

    public String getBftMessage() {
        return mBftMessage;
    }
}
