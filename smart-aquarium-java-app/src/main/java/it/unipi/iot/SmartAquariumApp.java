package it.unipi.iot;

import org.eclipse.paho.client.mqttv3.MqttException;

import it.unipi.iot.configuration.ConfigurationXML;
import it.unipi.iot.database.DatabaseManager;
import it.unipi.iot.mqtt.MQTTCollector;
import it.unipi.iot.coap.CoAPNetworkController;

/**
 * 
 * @author Fabi8997
 * TODO
 */
public class SmartAquariumApp {

	public static void main(String[] args) throws MqttException {
		System.out.println("[SMART AQUARIUM] Welcome to your Smart Aquarium!");
		
		//Load configuration parameters
		System.out.println("[SMART AQUARIUM] Loading configuration parameters...");
		ConfigurationXML configurationXML = new ConfigurationXML();
		
		System.out.println(configurationXML.configurationParameters);
		
		System.out.println("[SMART AQUARIUM] Connecting to the database...");
		
		//Initialize database manager using the configuration parameters
		DatabaseManager db = new DatabaseManager(configurationXML.configurationParameters);
		
		MQTTCollector mqttCollector = new MQTTCollector(configurationXML.configurationParameters, db);
        
		/*try {
        	//Launch mqttCollector
        	@SuppressWarnings("unused")
        	MQTTCollector mqttCollector = new MQTTCollector(configurationXML.configurationParameters, db);
        	
        	
        } catch(MqttException me) {

            me.printStackTrace();
        }*/
		
		System.out.println("\n[SMART AQUARIUM] Launching the CoAP Network Manager...\n");
		
		//Create a new CoAP Server to handle the CoAP network
		CoAPNetworkController coapNetworkController = new CoAPNetworkController(configurationXML.configurationParameters);
		
		//Start the CoAP Server
		coapNetworkController.start();
		
		System.out.println("[SMART AQUARIUM] Waiting for the registration of all the devices...");
		
		//Wait until all the devices are registered
		/*while(!coapNetworkController.allDevicesRegistered()) {
			try {
				
				//Sleep for 5 seconds to wait for registration
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		System.out.println("[SMART AQUARIUM] All the devices are registered to the CoAP Network Controller");
		
		//System.out.println("response: " + coapNetworkController.getOsmoticWaterTank().get().getResponseText());
		*/
		
		//TODO 
		
		boolean flow_active = false; //To be substituted by coapGETStatus
		while(true) {
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			if(((mqttCollector.getCurrentKH()) < 3) && !flow_active) {
				mqttCollector.simulateOsmoticWaterTank("INC");
				flow_active = true;
			}else if ((mqttCollector.getCurrentKH() > 5 ) && !flow_active) {
				mqttCollector.simulateOsmoticWaterTank("DEC");
				flow_active = true;
			}else if ((mqttCollector.getCurrentKH() > 3.8) && (mqttCollector.getCurrentKH() < 4.2) && flow_active) {
				mqttCollector.simulateOsmoticWaterTank("OFF");
				flow_active = false;
			}
			
		}
		
		
		/*
		 * Scrivere min kh e max kh in config
		 * 
		 * Aggiungere in CoapNewtCont il current flow e current level tank!
		 * usare quelli
		 * Aggiugnere la get per il json
		 * aggiungere la scrittura nel DB per flow level
		 * Implementare l'invio del put on quando il kh <= lb or >= ub
		 * Di conseguenza occorre salire gradualmente nella simulazione !!
		 * Fatto questo l'interazione tra tank e sensori è finita!!
		 * 
		 * 
		 * L'app ogni tot controlla i valori!!
		 */
		
	}

}
