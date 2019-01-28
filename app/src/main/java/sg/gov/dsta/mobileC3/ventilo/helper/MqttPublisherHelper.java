package sg.gov.dsta.mobileC3.ventilo.helper;

import org.eclipse.paho.client.mqttv3.MqttClient;

public class MqttPublisherHelper {
    public static final String BROKER_URL = "tcp://broker.mqttdashboard.com:1883";
    private MqttClient client;

    public MqttPublisherHelper()
    {

//        String clientId = Utils.getMacAddress() + "-pub";
//        try
//        {
//            client = new MqttClient(BROKER_URL, clientId);
//        }
//        catch (MqttException e)
//        {
//            e.printStackTrace();
//            System.exit(1);
//        }
    }
}