package sg.gov.dsta.mobileC3.ventilo.network.jeroMQ;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZThread;

public class JeroMQSubZThreadRunnable implements ZThread.IAttachedRunnable {

    @Override
    public void run(Object[] args, ZContext ctx, ZMQ.Socket pipe) {
        JeroMQSubscriber.getInstance().start();
    }
}
