package it.unipi.iot.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import it.unipi.iot.configuration.ConfigurationParameters;
import it.unipi.iot.database.DatabaseManager;
import it.unipi.iot.ph.PHSample;


/**
 * 
 * @author Fabi8997
 * TODO 
 */
public class MQTTCollector implements MqttCallback {
	
	private final String pHTopic;
	private final String pHDatabaseTableName;
	private final DatabaseManager db;
	private final String broker;
	private final String clientId;
	
	//TODO To add temperature and the other topic
	
	/**
	 * Constructor of the class MQTT Collector. <br> It reads the configuration parameters read from the configuration file config.xml
	 * and initialize its parameters; The passed DatabaseManager is used to insert the samples inside the database.<br>
	 * The constructor sets up the connection with the MQTT broker using as its ID the MQTTClientId, sets up the callback for the incoming 
	 * messages and finally subscribe to the three topics in which the sensors publish their values: pH, kH and temperature.
	 * @param configurationParameters configuration parameters read from the configuration file
	 * @param db database manager to interact with the MYSQL database
	 * @throws MqttException
	 */
	public MQTTCollector(ConfigurationParameters configurationParameters, DatabaseManager db) throws MqttException {
		
		//Assign the passed db to the db manager of the class MQTT to be used in the callback
		this.db = db;
		
		//Retrieve the values from the configuration file
		this.pHTopic = configurationParameters.pHTopic;
        this.broker = configurationParameters.MQTTBroker;
        this.clientId = configurationParameters.MQTTClientId;
		this.pHDatabaseTableName = configurationParameters.pHDatabaseTableName;
		

		MqttClient mqttClient = new MqttClient(broker, clientId);
        System.out.println("[MQTTManager] Connecting to broker: "+broker);
        
        mqttClient.setCallback( this );
        
        mqttClient.connect();
        
        //Subscribe to the pH topic
        mqttClient.subscribe(pHTopic);
        
        // TODO GITIGNORE FOR THE BUILDS!!!
	}

	@Override
	public void connectionLost(Throwable cause) {
		// TODO Auto-generated method stub

	}


	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		if(topic.equals(pHTopic)) {
			
			//Create a pH sample object passing the JSON document
			PHSample pHSample = new PHSample(new String(message.getPayload()));

			//DEBUG
			//System.out.println(String.format("[%s] %s", topic, pHSample));
			
			//Insert in the table passed as first argument the ph value passed as second argument
			db.insertSample(this.pHDatabaseTableName, pHSample.getpHValue() );
			
			//LOG
			System.out.println("[MQTTCollector] Inserted " + pHSample + " in " + this.pHDatabaseTableName + "." );
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}

}