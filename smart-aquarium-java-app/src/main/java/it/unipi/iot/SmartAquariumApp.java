package it.unipi.iot;

import org.eclipse.paho.client.mqttv3.MqttException;

import it.unipi.iot.configuration.ConfigurationXML;
import it.unipi.iot.database.DatabaseManager;
import it.unipi.iot.mqtt.MQTTCollector;

/**
 * 
 * @author Fabi8997
 * TODO
 */
public class SmartAquariumApp {

	public static void main(String[] args) {
		System.out.println("[SMART AQUARIUM] Welcome to your Smart Aquarium!");
		
		//Load configuration parameters
		System.out.println("[SMART AQUARIUM] Loading configuration parameters...");
		ConfigurationXML configurationXML = new ConfigurationXML();
		
		System.out.println(configurationXML.configurationParameters);
		
		System.out.println("[SMART AQUARIUM] Connecting to the database...");
		
		//Initialize database manager using the configuration parameters
		DatabaseManager db = new DatabaseManager(configurationXML.configurationParameters);
        
		try {
        	//Launch mqttCollector
        	@SuppressWarnings("unused")
			MQTTCollector mqttCollector = new MQTTCollector(configurationXML.configurationParameters, db);
        	
        	
        } catch(MqttException me) {

            me.printStackTrace();
        }
	}

}
