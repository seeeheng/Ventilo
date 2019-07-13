package sg.gov.dh.mq;


/**
 * This interface allows parent classes to receive<br>
 * 1. MQ Messages<br>
 */
public interface MQListener {
    /**
     * Provide Message Updates to parent classes <br>
     * The implementation of MQ Interface should call this method when they receive new message updates.
     * @param message
     */
    void onNewMessage(String message);
}
