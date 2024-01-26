package mqtt.iot.unipi.it;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import ph.mqtt.iot.unipi.it.PHSample;

public class MQTTCollector implements MqttCallback {
	
	private final String pH_topic = "pH";
	//TODO To add temperature and the other topic
	
	public MQTTCollector() throws MqttException {
		
        String broker       = "tcp://127.0.0.1:1883";
        String clientId     = "SmartAquariumMQTTCollector";
		
		MqttClient mqttClient = new MqttClient(broker, clientId);
        System.out.println("Connecting to broker: "+broker);
        
        mqttClient.setCallback( this );
        
        mqttClient.connect();
        
        //Subscribe to the pH topic
        mqttClient.subscribe(pH_topic);
        // TODO GITIGNORE FOR THE BUILDS!!!
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}

	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		if(topic.equals(pH_topic)) {
			
			
			//Create a pH sample object passing the JSON document
			PHSample pHSample = new PHSample(new String(message.getPayload()));

			//TODO instead of print use the log
			System.out.println(String.format("[%s] %s", topic, pHSample));
			
			//TODO Insert the sample in the database
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}

}
