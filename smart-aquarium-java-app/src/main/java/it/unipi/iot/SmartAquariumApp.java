package it.unipi.iot;

import it.unipi.iot.configuration.ConfigurationXML;
import it.unipi.iot.database.DatabaseManager;

public class SmartAquariumApp {

	public static void main(String[] args) {
		System.out.println("[SMART AQUARIUM] Welcome to your Smart Aquarium!");
		
		//Load configuration parameters
		ConfigurationXML configurationXML = new ConfigurationXML();
		System.out.println(configurationXML.configurationParameters);
		DatabaseManager db = new DatabaseManager(configurationXML.configurationParameters);
		db.insertSample();
        
		/*try {
        	
        	MQTTCollector mqttCollector = new MQTTCollector();
        	
        	
        } catch(MqttException me) {

            me.printStackTrace();
        }*/
	}

}
