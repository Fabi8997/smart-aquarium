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
		
		System.out.println("\n[SMART AQUARIUM] Launching the CoAP Network Manager...\n");
		
		//Create a new CoAP Server to handle the CoAP network
		CoAPNetworkController coapNetworkController = new CoAPNetworkController(configurationParameters, db);
		
		//Start the CoAP Server
		coapNetworkController.start();
		
		System.out.println("[SMART AQUARIUM] Waiting for the registration of all the devices...");
		
		//Wait until all the devices are registered
		while(!coapNetworkController.allDevicesRegistered()) {
			try {
				
				//Sleep for 5 seconds to wait for registration
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		//Start the co2 flow with the basic amount of co2dispensed!
		//every time the co2 change then it must be sent the new level to be dispensed
		
		System.out.println("[SMART AQUARIUM] All the devices are registered to the CoAP Network Controller");
		
		//System.out.println("response: " + coapNetworkController.getOsmoticWaterTank().get().getResponseText());
		
		if(coapNetworkController.co2DispenserRegistered()) {
			coapNetworkController.getCo2Dispenser().startDispenser();
		}
		
		//TODO only when all are registered!!
		
		while(true) {
			try {
				Thread.sleep(5000);//ADD in configuration the 
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
			//If the kH sensor has published a new kH value then check its value
			if((coapNetworkController != null) && (mqttCollector.isNewCurrentKH())) {
				checkKHStatus(
						mqttCollector,
						coapNetworkController,
						configurationParameters.kHLowerBound,
						configurationParameters.kHUpperBound,
						configurationParameters.kHOptimalValue,
						configurationParameters.epsilon);
			}
			
			//If the temperature sensor has published a new temperature value then check its value
			if((coapNetworkController != null) && (mqttCollector.isNewCurrentTemperature())) {
				checkTemperatureStatus(
						mqttCollector,
						coapNetworkController,
						configurationParameters.temperatureLowerBound,
						configurationParameters.temperatureUpperBound,
						configurationParameters.temperatureOptimalValue,
						configurationParameters.epsilonTemperature);
			}
			
			
			//If the pH sensor has published a new temperature value then check its value
			//The control of the pH is more difficult, since we've to modify it only when the kH and the temperature  is stable
			// only in this case we can modify the pH in order to not harm the fishes.
			if((coapNetworkController != null) && (mqttCollector.isNewCurrentPH())) {
				checkPHStatus(
						mqttCollector,
						coapNetworkController,
						configurationParameters.pHLowerBound,
						configurationParameters.pHUpperBound,
						configurationParameters.pHOptimalValue,
						configurationParameters.epsilon);
			}
			
			//TODO change the value IFF all the parameters are ok and inside their intervals! this must be used only when 
			//The PH is not good
			if((coapNetworkController != null) && (coapNetworkController.getCo2Dispenser() != null)) {
				coapNetworkController.getCo2Dispenser().computeNewCO2(
						mqttCollector.getCurrentPH(),
						mqttCollector.getCurrentKH(),
						mqttCollector.getCurrentTemperature());
			}
		}
		
		
		
		
		
		//CO2 computation at the end
		
		
		/* Calcolo CO2 solo se KH, temperatura e PH sono all'interno dell'intervall
		 * Inoltre forzare il cambio del PH solamente quando temperatura a KH sono dentro l'intervallo giusto! 
		 * Gestire le temporizzazioni
		 * Le variazioni imposte devono essere maggiori di quelle randomiche, altrimenti non funzionerà mai
		 * 
		 * ULTIMO STEP:
		 * 
		 * Calcolo livello co2 si può fare come ultima cosa
		 * Forse il check del ph è da modificare e la variazione va cambiata solo quando il livello di ph è troppo basso o alto
		 * Quindi la CO2 è sempre attiva, in base alla variazione di CO2 il ph cambia, da poco a molto!!
		 * Decidere gli istanti temporali, l'istante di pubblicazione deve essere lo stesso
		 *
		 * 
		 * L'app ogni tot controlla i valori!!
		 */
		
	}
	
	private static void checkKHStatus(MQTTCollector mqttCollector, CoAPNetworkController coapNetworkController, float lowerBound, float upperBound, float optimalValue, float epsilon) {
		
		//If kH < LB
		if(((mqttCollector.getCurrentKH()) < lowerBound) && !coapNetworkController.getOsmoticWaterTank().isOsmoticWaterTankFlowActive()) {
			
			//Activate the simulation on kH device
			mqttCollector.simulateOsmoticWaterTank("INC");
			
			//Send the command to the actuator to start the flow: mode=on
			coapNetworkController.getOsmoticWaterTank().activateFlow();
			
		//If kH > UB	
		}else if ((mqttCollector.getCurrentKH() > upperBound ) && !coapNetworkController.getOsmoticWaterTank().isOsmoticWaterTankFlowActive()) {
			
			//Activate the simulation on kH device
			mqttCollector.simulateOsmoticWaterTank("DEC");
			
			//Send the command to the actuator to start the flow: mode=on
			coapNetworkController.getOsmoticWaterTank().activateFlow();
			
			
		//If    kH in [ OptKH - epsilon, OptKH + epsilon] where optKH is the optimum value for kH
		}else if ((mqttCollector.getCurrentKH() > optimalValue - epsilon) && (mqttCollector.getCurrentKH() < (optimalValue + epsilon)) && coapNetworkController.getOsmoticWaterTank().isOsmoticWaterTankFlowActive()) {
			
			//Activate the simulation on kH device
			mqttCollector.simulateOsmoticWaterTank("OFF");
			
			//Send the command to the actuator to stop the flow: mode=off
			coapNetworkController.getOsmoticWaterTank().stopFlow();
		}
	}

	
	private static void checkTemperatureStatus(MQTTCollector mqttCollector, CoAPNetworkController coapNetworkController, float lowerBound, float upperBound, float optimalValue, float epsilon) {
		
	
		//If kH < LB and the heater is not active
		if(((mqttCollector.getCurrentTemperature()) < lowerBound) && coapNetworkController.getTemperatureController().areFanHeaterInactive()) {
			
			//Activate the simulation on temperature device
			mqttCollector.simulateHeater("on");
			
			//Send the command to the actuator to start the heater: mode=on
			coapNetworkController.getTemperatureController().activateHeater();
			
		//If kH > UB and the fan is not active
		}else if ((mqttCollector.getCurrentTemperature() > upperBound && coapNetworkController.getTemperatureController().areFanHeaterInactive()) ) {
			
			//Activate the simulation on temperature device
			mqttCollector.simulateFan("on");
			
			//Send the command to the actuator to start the fan: mode=on
			coapNetworkController.getTemperatureController().activateFan();
			
			
		//If temperature in [ OptTemp - epsilon, OptTemp + epsilon] where optTemp is the optimum value for temperature
		}else if ((mqttCollector.getCurrentTemperature() > optimalValue - epsilon) && (mqttCollector.getCurrentTemperature() < (optimalValue + epsilon) && (coapNetworkController.getTemperatureController().isFanActive() || coapNetworkController.getTemperatureController().isHeaterActive()))) {
			
			//If the fan is active, turn it off
			if(coapNetworkController.getTemperatureController().isFanActive()) {
				
				//Activate the simulation on temperature device
				mqttCollector.simulateFan("off");
				
				//Send the command to the actuator to stop the fan: mode=off
				coapNetworkController.getTemperatureController().stopFan();
				
			//If the heater is active, turn it off
			}else if(coapNetworkController.getTemperatureController().isHeaterActive()) {
				
				//Activate the simulation on temperature device
				mqttCollector.simulateHeater("off");
				
				//Send the command to the actuator to stop the heater: mode=off
				coapNetworkController.getTemperatureController().stopHeater();;
			}
		}
	}

	//TODO CALCOLARE LA CO2 DA EROGARE, SE IL VALORE ATTUALE MENO QUELLO PRECEDENTE È MAGGIORE DI UNA CERTA SOGLIA
	//		IL DEC/INC È MAGGIORE NELLA SIMULAZIONE DEL PH!!!
	private static void checkPHStatus(MQTTCollector mqttCollector, CoAPNetworkController coapNetworkController, float lowerBound, float upperBound, float optimalValue, float epsilon) {
		
		//If kH < LB ADD && !coapNetworkController.getOsmoticWaterTank().isOsmoticWaterTankFlowActive()
		if(((mqttCollector.getCurrentPH()) < lowerBound) ) {
			
			//Compute the CO2 to be released
			//If the difference in CO2 level to be dispensed is significant then change the pH
			
			//Activate the simulation on pH device
			mqttCollector.simulateCo2Dispenser("SDEC");
			
			//Send the command to the actuator to start the flow: mode=on
			//coapNetworkController.getOsmoticWaterTank().activateFlow();
			
		//If kH > UB ADD && !coapNetworkController.getOsmoticWaterTank().isOsmoticWaterTankFlowActive()
		}else if ((mqttCollector.getCurrentPH() > upperBound ) ) {
			
			//Activate the simulation on pH device
			mqttCollector.simulateCo2Dispenser("SINC");
			
			//Compute the CO2 to be released
			
			//Send the command to the actuator to start the flow: mode=on
			//coapNetworkController.getOsmoticWaterTank().activateFlow();
			
			
		//If    kH in [ OptKH - epsilon, OptKH + epsilon] where optKH is the optimum value for kH && coapNetworkController.getOsmoticWaterTank().isOsmoticWaterTankFlowActive()
		}else if ((mqttCollector.getCurrentPH() > optimalValue - epsilon) && (mqttCollector.getCurrentPH() < (optimalValue + epsilon)) ) {
			
			//Activate the simulation on kH device
			mqttCollector.simulateCo2Dispenser("OFF");
			
			//Compute the CO2 to be released
			
			//Send the command to the actuator to stop the flow: mode=off
			//coapNetworkController.getOsmoticWaterTank().stopFlow();
		}
	}
	
}
