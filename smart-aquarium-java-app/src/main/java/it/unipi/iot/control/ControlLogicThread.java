package it.unipi.iot.control;

import it.unipi.iot.coap.CoAPNetworkController;
import it.unipi.iot.configuration.ConfigurationParameters;
import it.unipi.iot.mqtt.MQTTCollector;


/**
 * 
 * Class extending Thread, that implements a control loop.<br>
 * The main function of this class is to manage the different devices in order
 * to provide a safe environment for the tank life.<br>
 * It periodically checks the different values retrieved by the sensors and,
 * when it's needed, it sends commands to the actuator aimed at balancing the
 * values in order to keep them inside the safe intervals.<br>
 * 
 * @author Fabi8997
 *
 */
public class ControlLogicThread extends Thread {
	
	private ConfigurationParameters configurationParameters;
	private MQTTCollector mqttCollector;
	private CoAPNetworkController coapNetworkController;
	
	// To keep track of the pH simulation status
	private static String pHSimulationType = "OFF";
	
	
	/**
	 * Class constructor.
	 * @param configurationParameters configuration parameters.
	 * @param mqttCollector MQTT collector to retrieve the current values and interact with the sensors.
	 * @param coapNetworkController CoAP controller to interact with the actuators.
	 */
	public ControlLogicThread(ConfigurationParameters configurationParameters, MQTTCollector mqttCollector, CoAPNetworkController coapNetworkController) {
		super();
		this.configurationParameters = configurationParameters;
		this.mqttCollector = mqttCollector;
		this.coapNetworkController = coapNetworkController;
	}



	@Override
	public void run() {
		//Main cycle
				while(true) {
					
					//Every sleepIntervalApp milliseconds the status of the values is checked
					try {
						Thread.sleep(configurationParameters.sleepIntervalApp);
					} catch (InterruptedException e) {
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
					
					
					//If the pH sensor has published a new pH value then check its value
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
					
					//If all the values are good, then compute the new level of CO2 to be dispensed
					if((coapNetworkController != null) && (coapNetworkController.getCo2Dispenser() != null) && (areAllMeasuresStable(mqttCollector))) {
						coapNetworkController.getCo2Dispenser().computeNewCO2(
								mqttCollector.getCurrentPH(),
								mqttCollector.getCurrentKH(),
								mqttCollector.getCurrentTemperature());
					}
				}
		
	}
	
	/**
	 * Checks if the kH value is under the lower bound, above the upper bound or around the optimal value. In the first two
	 * cases activates the flow of osmotic water to bring the kH around the optimal value, while in the latter it turns off the 
	 * osmotic water flow.<br>
	 * To implement the simulation are sent MQTT messages to the sensors.
	 * 
	 * @param mqttCollector to retrieve the current values and interact with the sensors.
	 * @param coapNetworkController to interact with the actuator.
	 * @param lowerBound of kH interval.
	 * @param upperBound of kH interval.
	 * @param optimalValue of kH.
	 * @param epsilon around the optimal value.
	 */
	private void checkKHStatus(MQTTCollector mqttCollector, CoAPNetworkController coapNetworkController, float lowerBound, float upperBound, float optimalValue, float epsilon) {
		
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


	/**
	 * Checks if the temperature is under the lower bound, above the upper bound or around the optimal value. In the first case
	 * turn on the heater, in the second turn on the fan and in the latter case turn off the fan or the heater.<br>
	 * To implement the simulation are sent MQTT messages to the sensors.
	 * 
	 * @param mqttCollector to retrieve the current values and interact with the sensors.
	 * @param coapNetworkController to interact with the actuator.
	 * @param lowerBound of temperature interval.
	 * @param upperBound of temperature interval.
	 * @param optimalValue of temperature.
	 * @param epsilon around the optimal value.
	 */
	private void checkTemperatureStatus(MQTTCollector mqttCollector, CoAPNetworkController coapNetworkController, float lowerBound, float upperBound, float optimalValue, float epsilon) {
		
	
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

	/**
	 * Checks the pH status: if it is under the lower bound or above the upper bound and the kH and the temperature are 
	 * close to their optimal value, then the pH can be modified and the simulation of the pH sensor can be started accordingly
	 * to the strength of the variation of the new CO2 level computed (It is computed only if the the kH and temperature are stable). <br>
	 * If the pH value is now inside the desired interval then the simulation is stopped.<br>
	 * The simulation is performed using MQTT messages.
	 * 
	 * @param mqttCollector to retrieve the current values and interact with the sensors.
	 * @param coapNetworkController to interact with the actuator.
	 * @param lowerBound of pH interval.
	 * @param upperBound of pH interval.
	 * @param optimalValue of pH.
	 * @param epsilon around the optimal value.
	 */
	private void checkPHStatus(MQTTCollector mqttCollector, CoAPNetworkController coapNetworkController, float lowerBound, float upperBound, float optimalValue, float epsilon) {
		
		//If kH < LB ADD; The pH can be modified only when the temperature and the kH is stable
		if(((mqttCollector.getCurrentPH()) < lowerBound) && tempAndKHStable(mqttCollector)) {
				
			//Compute the new value of CO2 to be dispensed
			coapNetworkController.getCo2Dispenser().computeNewCO2(
					mqttCollector.getCurrentPH(),
					mqttCollector.getCurrentKH(),
					mqttCollector.getCurrentTemperature());	
				
			//Activate the simulation on pH device
			if(!coapNetworkController.getCo2Dispenser().isHighVariation() && !pHSimulationType.equals("SDEC")) {
	
				//If the variation in CO2 is low => low variation of PH
				mqttCollector.simulateCo2Dispenser("SDEC");
					
			}else if(coapNetworkController.getCo2Dispenser().isHighVariation() && !pHSimulationType.equals("DEC")){
					
				//If the variation in CO2 is high => high variation of PH
				mqttCollector.simulateCo2Dispenser("DEC");
			}	
			
			
		//If kH > UB ADD; The pH can be modified only when the temperature and the kH is stable
		}else if ((mqttCollector.getCurrentPH() > upperBound ) && tempAndKHStable(mqttCollector) ) {
			
			//Compute the new value of CO2 to be dispensed
			coapNetworkController.getCo2Dispenser().computeNewCO2(
					mqttCollector.getCurrentPH(),
					mqttCollector.getCurrentKH(),
					mqttCollector.getCurrentTemperature());	
				
			//Activate the simulation on pH device
			if(!coapNetworkController.getCo2Dispenser().isHighVariation() && !pHSimulationType.equals("SINC")) {
	
				//If the variation in CO2 is low => low variation of PH
				mqttCollector.simulateCo2Dispenser("SINC");
					
			}else if(coapNetworkController.getCo2Dispenser().isHighVariation() && !pHSimulationType.equals("INC")){
					
				//If the variation in CO2 is high => high variation of PH
				mqttCollector.simulateCo2Dispenser("INC");
			}				
			
		//If pH in [ OptPH - epsilon, OptPH + epsilon] where optPH is the optimum value for kH
		}else if ((mqttCollector.getCurrentPH() > optimalValue - epsilon) && (mqttCollector.getCurrentPH() < (optimalValue + epsilon)) && !pHSimulationType.equals("OFF")) {
			
			//Activate the simulation on kH device
			mqttCollector.simulateCo2Dispenser("OFF");
			
			//The flow of CO2 is always active!
		}
	}
	
	/**
	 * Checks if all the measures are inside the required interval.
	 * @param mqttCollector
	 * @return true if all the measures are inside the required interval, false otherwise.
	 */
	private boolean areAllMeasuresStable(MQTTCollector mqttCollector) {
		
		//Check if the kH belongs to (LB, UB)
		if(mqttCollector.getCurrentKH() < configurationParameters.kHLowerBound || mqttCollector.getCurrentKH() > configurationParameters.kHUpperBound) {
			return false;
		}
		
		//Check if the pH belongs to (LB, UB)
		if(mqttCollector.getCurrentPH() < configurationParameters.pHLowerBound || mqttCollector.getCurrentPH() > configurationParameters.pHUpperBound) {
			return false;
		}
		
		//Check if the temperature belongs to (LB, UB)
		if(mqttCollector.getCurrentTemperature() < configurationParameters.temperatureLowerBound || mqttCollector.getCurrentTemperature() > configurationParameters.temperatureUpperBound) {
			return false;
		}
		
		//All values are stable
		return true;
	}
	
	
	/**
	 * Checks if the temperature and the kH are inside the desired interval.
	 * @param mqttCollector to retrieve the values.
	 * @return true if the temperature and the kH are inside the desired interval, false otherwise.
	 */
	private boolean tempAndKHStable(MQTTCollector mqttCollector) {
		
		//Check if the kH belongs to (LB, UB)
		if(mqttCollector.getCurrentKH() < configurationParameters.kHLowerBound || mqttCollector.getCurrentKH() > configurationParameters.kHUpperBound) {
			return false;
		}
		
		//Check if the temperature belongs to (LB, UB)
		if(mqttCollector.getCurrentTemperature() < configurationParameters.temperatureLowerBound || mqttCollector.getCurrentTemperature() > configurationParameters.temperatureUpperBound) {
			return false;
		}
		
		//All values are stable
		return true;
	}
	

}
