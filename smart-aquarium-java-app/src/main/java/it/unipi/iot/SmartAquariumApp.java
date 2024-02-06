package it.unipi.iot;

import org.eclipse.paho.client.mqttv3.MqttException;

import it.unipi.iot.configuration.ConfigurationParameters;
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

	static boolean flow_active = false; //To be substituted by coapGETStatus
	
	public static void main(String[] args) throws MqttException {
		System.out.println("[SMART AQUARIUM] Welcome to your Smart Aquarium!");
		
		//Load configuration parameters
		System.out.println("[SMART AQUARIUM] Loading configuration parameters...");
		ConfigurationXML configurationXML = new ConfigurationXML();
		ConfigurationParameters configurationParameters = configurationXML.configurationParameters;
		
		System.out.println(configurationParameters);
		
		System.out.println("[SMART AQUARIUM] Connecting to the database...");
		
		//Initialize database manager using the configuration parameters
		DatabaseManager db = new DatabaseManager(configurationParameters);
		
		//Launch mqttCollector
		MQTTCollector mqttCollector = new MQTTCollector(configurationParameters, db);
        
		/*try {
        	//Launch mqttCollector
        	@SuppressWarnings("unused")
        	MQTTCollector mqttCollector = new MQTTCollector(configurationXML.configurationParameters, db);
        	
        	
        } catch(MqttException me) {

            me.printStackTrace();
        }*/
		
		System.out.println("\n[SMART AQUARIUM] Launching the CoAP Network Manager...\n");
		
		//Create a new CoAP Server to handle the CoAP network
		CoAPNetworkController coapNetworkController = new CoAPNetworkController(configurationParameters, db);
		
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
		
		while(true) {
			try {
				Thread.sleep(15000);//ADD in configuration the 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		/*
			//If the kH sensor has published a new kH value then check its value
			if(mqttCollector.isNewCurrentKH()) {
				checkKHStatus(
						mqttCollector,
						configurationParameters.kHLowerBound,
						configurationParameters.kHUpperBound,
						configurationParameters.kHOptimalValue,
						configurationParameters.epsilon);
			}
			
			//check temp
			
			//check ph
			// the ph can be changed IFF KH OK AND TEMP OK!!
		*/	
		}
		
		
		
		/*
		 * Ricordare di mettere il trigger anche quando viene riempito il coso
		 * Mettere MQTTCollector e CoAPNetwork globali!
		 * Implement activateFlow and stopFlow
		 * Sostituite flow active con getOsmoticFlowActive
		 * Implementare l'invio del put on quando il kh <= lb or >= ub
		 * Di conseguenza occorre salire gradualmente nella simulazione !!
		 * Fatto questo l'interazione tra tank e sensori è finita!!
		 * 
		 * 
		 * L'app ogni tot controlla i valori!!
		 */
		
	}
	
	private static void checkKHStatus(MQTTCollector mqttCollector, float lowerBound, float upperBound, float optimalValue, float epsilon) {
		
		//If kH < LB
		if(((mqttCollector.getCurrentKH()) < lowerBound) && !flow_active) {
			
			//Activate the simulation on kH device
			mqttCollector.simulateOsmoticWaterTank("INC");
			flow_active = true;//Lo farà la put
			
			//TODO Send the command to the actuator [put] mode on;
			
		//If kH > UB	
		}else if ((mqttCollector.getCurrentKH() > upperBound ) && !flow_active) {
			
			//Activate the simulation on kH device
			mqttCollector.simulateOsmoticWaterTank("DEC");
			flow_active = true;
			
			//TODO Send the command to the actuator [put] mode on;
			
		//If    kH in [ OptKH - epsilon, OptKH + epsilon] where optKH is the optimum value for kH
		}else if ((mqttCollector.getCurrentKH() > optimalValue - epsilon) && (mqttCollector.getCurrentKH() < (optimalValue + epsilon)) && flow_active) {
			
			//Activate the simulation on kH device
			mqttCollector.simulateOsmoticWaterTank("OFF");
			flow_active = false;
			
			//TODO Send the command to the actuator [put] mode off;
		}
	}

}
