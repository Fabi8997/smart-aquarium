package it.unipi.iot.mqtt;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import it.unipi.iot.configuration.ConfigurationParameters;
import it.unipi.iot.database.DatabaseManager;
import it.unipi.iot.kh.KHSample;
import it.unipi.iot.ph.PHSample;
import it.unipi.iot.temperature.TemperatureSample;


/**
 * 
 * @author Fabi8997
 * TODO 
 */
public class MQTTCollector implements MqttCallback {
	
	//Topic to retrieve the data published by the sensors
	private final String pHTopic;
	private final String kHTopic;
	private final String temperatureTopic;
	private final String osmoticWaterTankTopic;
	private final String temperatureControllerTopic;
	private final String co2DispenserTopic;
	
	//Names of the tables in which will be stored the samples
	private final String pHDatabaseTableName;
	private final String kHDatabaseTableName;
	private final String temperatureDatabaseTableName;
	
	//DB manager to set up the connection to the DB and to query it
	private final DatabaseManager db;
	
    //Parameters of the MQTT broker and MQTT client
	private final String broker;
	private final String clientId;
	
	//To keep track of the last value
	private float currentKH;
	private float currentPH;
	private float currentTemperature;
	
	//To know if the currentKH was already read
	private boolean newCurrentKH;
	private boolean newCurrentPH;
	private boolean newCurrentTemperature;
	
	//MqttClient to subscribe and publish(FOR SIMULATION)
	private MqttClient mqttClient;
	
	
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
		
		//Assign the passed DB to the DB manager of the class MQTT to be used in the callback
		this.db = db;
		
		//Retrieve the values from the configuration file 
		this.pHTopic = configurationParameters.pHTopic;
		this.kHTopic = configurationParameters.kHTopic;
		this.temperatureTopic = configurationParameters.temperatureTopic;
		this.osmoticWaterTankTopic = configurationParameters.osmoticWaterTankTopic;
		this.temperatureControllerTopic = configurationParameters.temperatureControllerTopic;
		this.co2DispenserTopic = configurationParameters.co2DispenserTopic;

		this.pHDatabaseTableName = configurationParameters.pHDatabaseTableName;
		this.kHDatabaseTableName = configurationParameters.kHDatabaseTableName;
		this.temperatureDatabaseTableName = configurationParameters.temperatureDatabaseTableName;
		
        this.broker = configurationParameters.MQTTBroker;
        this.clientId = configurationParameters.MQTTClientId;
		
        //Set the values that indicates that no data has been received yet
        this.currentKH = 0;
        this.currentPH = 0;
        this.currentTemperature = 0;
        
        //current values not read yet
        this.newCurrentKH = false;
        this.newCurrentPH = false;
        this.newCurrentTemperature = false;
        
        
        //Connect the mqttClient to the broker
		this.mqttClient = new MqttClient(broker, clientId);
        System.out.println("[MQTTManager] Connecting to broker: "+broker);
        
        this.mqttClient.setCallback( this );
        
        this.mqttClient.connect();
        
        //Subscribe to the pH topic
        this.mqttClient.subscribe(pHTopic);
        
        //Subscribe to the kH topic
        this.mqttClient.subscribe(kHTopic);
        
        //Subscribe to the temperature topic
        this.mqttClient.subscribe(temperatureTopic);
        
	}

	public float getCurrentKH() {
		this.newCurrentKH = false;
		return currentKH;
	}

	public float getCurrentPH() {
		this.newCurrentPH = false;
		return currentPH;
	}

	public float getCurrentTemperature() {
		this.newCurrentTemperature = false;
		return currentTemperature;
	}

	/**
	 * TODO
	 * @param message
	 */
	public void simulateOsmoticWaterTank(String message) {
		try {
			mqttClient.publish( this.osmoticWaterTankTopic , new MqttMessage(message.getBytes()));
		} catch (MqttPersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			
			//Insert in the table passed as first argument the pH value passed as second argument
			db.insertSample(this.pHDatabaseTableName, pHSample.getpHValue() );
			
			//Update the current value
			this.currentPH = pHSample.getpHValue();
			this.newCurrentPH = true;
			
			//LOG
			System.out.println("[MQTTCollector] Inserted " + pHSample + " in " + this.pHDatabaseTableName + "." );
		
		}else if(topic.equals(kHTopic)) {
			
			//Create a pH sample object passing the JSON document
			KHSample kHSample = new KHSample(new String(message.getPayload()));

			//DEBUG
			//System.out.println(String.format("[%s] %s", topic, kHSample));
			
			//Insert in the table passed as first argument the kH value passed as second argument
			db.insertSample(this.kHDatabaseTableName, kHSample.getkHValue() );
			
			//Update the current value
			this.currentKH = kHSample.getkHValue();
			this.newCurrentKH = true;
			
			//LOG
			System.out.println("[MQTTCollector] Inserted " + kHSample + " in " + this.kHDatabaseTableName + "." );
			
			
		}else if(topic.equals(temperatureTopic)) {
					
			//Create a pH sample object passing the JSON document
			TemperatureSample temperatureSample = new TemperatureSample(new String(message.getPayload()));

			//DEBUG
			//System.out.println(String.format("[%s] %s", topic, temperatureSample));
			
			//Insert in the table passed as first argument the temperature value passed as second argument
			db.insertSample(this.temperatureDatabaseTableName, temperatureSample.getTemperatureValue() );
			
			//Update the current value
			this.currentTemperature = temperatureSample.getTemperatureValue();
			this.newCurrentTemperature = true;
			
			//LOG
			System.out.println("[MQTTCollector] Inserted " + temperatureSample + " in " + this.temperatureDatabaseTableName + "." );
		}else {
			//LOG
			System.out.println("[MQTTCollector] " + String.format("[%s] %s", topic,new String(message.getPayload()) ));

		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		// TODO Auto-generated method stub

	}

}