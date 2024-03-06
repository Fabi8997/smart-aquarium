package it.unipi.iot.mqtt;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;

import it.unipi.iot.configuration.ConfigurationParameters;
import it.unipi.iot.database.DatabaseManager;
import it.unipi.iot.kh.KHSample;
import it.unipi.iot.log.Colors;
import it.unipi.iot.ph.PHSample;
import it.unipi.iot.temperature.TemperatureSample;


/**
 * This class is used to handle the interaction between the MQTT-based devices and the SmartAquariumAPP. <br>
 * It subscribes to the topics in which the sensors will publish their values; It manages the interaction with the database inserting 
 * the received values in the correct tables and manages the publishes messages in order to implement the simulation of the values
 * of the sensors in the correct way.
 * @author Fabi8997
 * 
 */
public class MQTTCollector implements MqttCallback {
	
	
	private static final String LOG = "[" + Colors.ANSI_YELLOW + "MQTT Collector" + Colors.ANSI_RESET + " ]";
	private static final String LOG_ERROR = "[" + Colors.ANSI_RED + "MQTT Collector" + Colors.ANSI_RESET + " ]";
	
	//Topic to retrieve the data published by the sensors
	private final String pHTopic;
	private final String kHTopic;
	private final String temperatureTopic;
	private final String osmoticWaterTankTopic;
	private final String fanTopic;
	private final String heaterTopic;
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
	
	//MqttClient to subscribe and publish
	private MqttClient mqttClient;
	
	//Thread safe variable accessed by the control loop thread!
	private AtomicBoolean closed;
	
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
		this.fanTopic = configurationParameters.fanTopic;
		this.heaterTopic = configurationParameters.heaterTopic;
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
        System.out.println(LOG + " Connecting to broker: "+broker);
        
        this.mqttClient.setCallback( this );
        
        this.mqttClient.connect();
        
        //Subscribe to the pH topic
        this.mqttClient.subscribe(pHTopic);
        
        //Subscribe to the kH topic
        this.mqttClient.subscribe(kHTopic);
        
        //Subscribe to the temperature topic
        this.mqttClient.subscribe(temperatureTopic);
        
        //Not closed
        this.closed = new AtomicBoolean(false);
        
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

	public boolean isNewCurrentKH() {
		return newCurrentKH;
	}

	public boolean isNewCurrentPH() {
		return newCurrentPH;
	}

	public boolean isNewCurrentTemperature() {
		return newCurrentTemperature;
	}
	
	public boolean isClosed() {
		return this.closed.get();
	}

	/**
	 * Send a message to simulate the osmotic water tank status. This methods is published in the osmotic water tank topic,
	 * the temperature device will read it and simulates its behavior accordingly.
	 * @param message
	 */
	public void simulateOsmoticWaterTank(String message) {
		try {
			mqttClient.publish( this.osmoticWaterTankTopic , new MqttMessage(message.getBytes()));
		} catch (MqttPersistenceException e) {
			System.out.println(LOG_ERROR + " " + e.getMessage());
		} catch (MqttException e) {
			System.out.println(LOG_ERROR + " " + e.getMessage());
		}
	}
	
	/**
	 * Send a message to simulate the fan status. This methods is published in the fan topic, the temperature device will read it
	 * and simulates its behavior accordingly.
	 * @param message
	 */
	public void simulateFan(String message) {
		try {
			mqttClient.publish( this.fanTopic , new MqttMessage(message.getBytes()));
		} catch (MqttPersistenceException e) {
			System.out.println(LOG_ERROR + " " + e.getMessage());
		} catch (MqttException e) {
			System.out.println(LOG_ERROR + " " + e.getMessage());
		}
	}
	
	/**
	 * Send a message to simulate the heater status. This methods is published in the heater topic, the temperature device will read it
	 * and simulates its behavior accordingly.
	 * @param message
	 */
	public void simulateHeater(String message) {
		try {
			mqttClient.publish( this.heaterTopic , new MqttMessage(message.getBytes()));
		} catch (MqttPersistenceException e) {
			System.out.println(LOG_ERROR + " " + e.getMessage());
		} catch (MqttException e) {
			System.out.println(LOG_ERROR + " " + e.getMessage());
		}
	}
	
	/**
	 * Send a message to simulate the CO2 dispensed. This methods is published in the co2 topic, the PH device will read it
	 * and simulates its behavior accordingly.
	 * @param message
	 */
	public void simulateCo2Dispenser(String message) {
		try {
			
			mqttClient.publish( this.co2DispenserTopic , new MqttMessage(message.getBytes()));
		} catch (MqttPersistenceException e) {
			System.out.println(LOG_ERROR + " " + e.getMessage());
		} catch (MqttException e) {
			System.out.println(LOG_ERROR + " " + e.getMessage());
		}
	}
	
	@Override
	public void connectionLost(Throwable cause) {
		System.out.println(LOG_ERROR + " Connection lost due to the following cause: " + cause.getMessage());

	}


	@Override
	public void messageArrived(String topic, MqttMessage message) throws Exception {
		
		if(topic.equals(pHTopic)) {
			
			//Create a pH sample object passing the JSON document
			PHSample pHSample = new PHSample(new String(message.getPayload()));

			//DEBUG
			//System.out.println(String.format("[%s] %s", topic, pHSample));
			
			//Insert in the table passed as first argument the pH value passed as second argument
			db.insertSample(this.pHDatabaseTableName, pHSample.getpHValue(), null);
			
			//Update the current value
			this.currentPH = pHSample.getpHValue();
			this.newCurrentPH = true;
			
			//LOG
			System.out.println(LOG + " Inserted " + pHSample + " in " + this.pHDatabaseTableName + "." );
		
		}else if(topic.equals(kHTopic)) {
			
			//Create a pH sample object passing the JSON document
			KHSample kHSample = new KHSample(new String(message.getPayload()));

			//DEBUG
			//System.out.println(String.format("[%s] %s", topic, kHSample));
			
			//Insert in the table passed as first argument the kH value passed as second argument
			db.insertSample(this.kHDatabaseTableName, kHSample.getkHValue() , null);
			
			//Update the current value
			this.currentKH = kHSample.getkHValue();
			this.newCurrentKH = true;
			
			//LOG
			System.out.println(LOG + " Inserted " + kHSample + " in " + this.kHDatabaseTableName + "." );
			
			
		}else if(topic.equals(temperatureTopic)) {
					
			//Create a pH sample object passing the JSON document
			TemperatureSample temperatureSample = new TemperatureSample(new String(message.getPayload()));

			//DEBUG
			//System.out.println(String.format("[%s] %s", topic, temperatureSample));
			
			//Insert in the table passed as first argument the temperature value passed as second argument
			db.insertSample(this.temperatureDatabaseTableName, temperatureSample.getTemperatureValue() , null);
			
			//Update the current value
			this.currentTemperature = temperatureSample.getTemperatureValue();
			this.newCurrentTemperature = true;
			
			//LOG
			System.out.println(LOG + " Inserted " + temperatureSample + " in " + this.temperatureDatabaseTableName + "." );
		}else {
			//LOG
			System.out.println(LOG + " " + String.format("[%s] %s", topic,new String(message.getPayload()) ));

		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken token) {
		//This is not required since the messages sent are only used for simulation purposes
	}
	
	/**
	 * Unsubscribe from all the topics, disconnect from the MQTT server and close the client releasing all the resources.
	 */
	public void close() {
		
		this.closed.set(true);
		
		try {
			
			//Unsubscribe from the topics
			System.out.println(LOG + " Unsubscribing from the topics...");
			this.mqttClient.unsubscribe(new String[]{
					this.co2DispenserTopic,
					this.fanTopic,
					this.heaterTopic,
					this.kHTopic,
					this.osmoticWaterTankTopic,
					this.pHTopic,
					this.temperatureTopic});
			
			//Disconnect from the server
			System.out.println(LOG + " Disconnecting from the server...");
			this.mqttClient.disconnect();
			
			//Close the client
			System.out.println(LOG + " Closing the client and releasing the resources...");
			this.mqttClient.close();
			
			System.out.println(LOG + " MQTT Collector closed successfully.");
			
		} catch (MqttException e) {
			System.out.println(LOG_ERROR + " Problem during the closing of the MQTT collector!");
			e.printStackTrace();
		}
	}
	
}