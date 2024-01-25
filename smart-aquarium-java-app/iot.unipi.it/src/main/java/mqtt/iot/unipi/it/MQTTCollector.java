package mqtt.iot.unipi.it;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MQTTCollector implements MqttCallback {
	
	public MQTTCollector() throws MqttException {
        String topic        = "ph";
        String broker       = "tcp://127.0.0.1:1883";
        String clientId     = "JavaApp";
		
		MqttClient mqttClient = new MqttClient(broker, clientId);
        System.out.println("Connecting to broker: "+broker);
        
        mqttClient.setCallback( this );
        
        mqttClient.connect();
        
        mqttClient.subscribe(topic);
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println(String.format("[%s] %s", topic, new String(message.getPayload())));
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}

}
