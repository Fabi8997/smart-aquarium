package it.unipi.iot.coap.CO2;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import it.unipi.iot.configuration.ConfigurationParameters;
import it.unipi.iot.log.Colors;

/**
 * 
 * This class extends the CoapClient class. <br> It provides the methods to: <br>
 * - compute a new level of Co2 <br>
 * - activate the dispenser <br>
 * - change the CO2 dispensed
 * 
 * @author Fabi8997
 * 
 */
public class CO2Dispenser extends CoapClient {
	
	private static final String LOG = "[" + Colors.ANSI_CYAN + "Smart Aquarium " + Colors.ANSI_RESET + "]";
	private static final String LOG_ERROR = "[" + Colors.ANSI_RED + "Smart Aquarium " + Colors.ANSI_RESET + " ]";
	
	//Status
	float co2DispenserTankLevel; 
	boolean co2DispenserTankFlowActive;
	private float currentVariation;
	private float currentCO2;
	float minLevel;
	
	//TODO Define the threshold
	private static float THRESHOLD = (float) 2;
	
	private static float HIGH_VARIATION_THRESHOLD = 5;
	
	/**
	 * Class constructor.
	 * 
	 * @param ipAddress of the URI
	 * @param configurationParameters configuration parameters
	 */
	public CO2Dispenser(String ipAddress, ConfigurationParameters configurationParameters) {
			
			super("coap://[" + ipAddress + "]/"+configurationParameters.co2DispenserTopic+"/tank");
			
			this.minLevel = configurationParameters.minCO2tankLevel;
			
			this.co2DispenserTankFlowActive = false;
			
			//Initialize current variation, this is needed to know how much must be increase or decrease the PH value
			this.currentVariation = 0;
			
			//Set to 0 so the new value is over the threshold always
			this.currentCO2 = 0;
			
			//Set the initial level of CO2 to be dispensed based on the optimal values
			computeNewCO2(configurationParameters.pHOptimalValue,
						  configurationParameters.kHOptimalValue,
						  configurationParameters.temperatureOptimalValue);
		}
	
	
	/**
	 * Compute the new value of CO2 using the formula better explained in the documentation. <br>
	 * The new value depends on the three other measures observed by the sensors.<br>
	 * The new value is accepted only if it is up to a certain threshold read from the configuration file. This
	 * is done in order to not change too frequently the CO2 dispensed but at the same time keeping its value inside a 
	 * safe interval for the aquarium life.
	 * 
	 * @param pH value observed from the PH sensor
	 * @param kH value observed from the KH sensor
	 * @param temperature observed from the temperature sensor
	 */
	public void computeNewCO2(float pH, float kH, float temperature) {
		
		float PKa = (float) (((3404.71)/(temperature + 273.15)) + (0.032786*(temperature + 273.15) - 14.8435));
		float newCO2 = (float) (15.69692*kH*Math.pow(10, PKa - pH));
		
		currentVariation = Math.abs(newCO2 - currentCO2);
		
		if (currentVariation > THRESHOLD){
			currentCO2 = newCO2;
			this.setCO2Dispensed();
			
		}
		
	}
	
	
	/**
	 * Activates the flow using the initial CO2 value to be dispensed.
	 */
	public void startDispenser() {
		setCO2Dispensed();
		activateFlow();
	}
	
	
	/**
	 * Send a put request to activate the flow of CO2, the post variable set is mode = on.<br>
	 * The put request is handled by a CoapHandler that onLoad changes the value of the flag to check the flow status,
	 * while onError \\TODO.
	 * 
	 */
	public void activateFlow() {
		
		//send put mode on
		this.put(new CoapHandler() {
            
			@Override
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess()) {
                    	
                        System.out.println(LOG + " Put operation failed [device: CO2Dispenser].");
                    }else {
                    	
                    	System.out.println(LOG + " CO2 dispenser [ mode = "+Colors.ANSI_GREEN+"on"+Colors.ANSI_RESET+" ].");
                    	
                    	//Set the flag to signal that the flow is active
                		co2DispenserTankFlowActive = true;
                    }
                }
            }

            public void onError() {
                System.out.println(LOG_ERROR + " Put operation failed [device: CO2Dispenser].");
            }

			

        }, "mode=on", MediaTypeRegistry.TEXT_PLAIN);
	}
	
	
	/**
	 * Send a put request to change the value of CO2 dispensed, the post variable set is value = currentCO2.<br>
	 * The put request is handled by a CoapHandler.
	 * 
	 */
	public void setCO2Dispensed() {
		
		//send put mode on
		this.put(new CoapHandler() {
            
			@Override
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess()) {
                    	
                        System.out.println(LOG + " Put operation failed [device: CO2Dispenser].");
                    }else {
                    	
                    	System.out.println(LOG + " Changed CO2 dispensed [ value = "+Colors.ANSI_GREEN+String.format("%.2f",currentCO2)+Colors.ANSI_RESET+" ].");
                    }
                }
            }

            public void onError() {
                System.out.println(LOG_ERROR + " Put operation failed [device: CO2Dispenser].");
            }

			

        }, "value="+String.format("%.2f",currentCO2), MediaTypeRegistry.TEXT_PLAIN);
	}
	
	
	/**
	 * Send a put request to stop the flow of CO2, the post variable set is mode = off.<br>
	 * The put request is handled by a CoapHandler that onLoad changes the value of the flag to check the flow status,
	 * while onError \\TODO.
	 * 
	 */
	public void stopFlow() {
		
		//send put mode off
		this.put(new CoapHandler() {
            
			@Override
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess()) {
                    	
                        System.out.println(LOG + " Put operation failed [device: CO2Dispenser].");
                    }else {
                    	
                    	System.out.println(LOG + " CO2 dispenser [ mode = "+Colors.ANSI_RED+"off"+Colors.ANSI_RESET+" ].");
                    	
                    	//Set the flag to signal that the flow is active
                		co2DispenserTankFlowActive = false;
                    }
                }
            }

            public void onError() {
                System.out.println(LOG_ERROR + " Put operation failed [device: CO2Dispenser].");
            }

			

        }, "mode=off", MediaTypeRegistry.TEXT_PLAIN);
	}
	
	public boolean isHighVariation() {
		return currentVariation > HIGH_VARIATION_THRESHOLD;
	}
	
	public float getCurrentCO2() {
		return currentCO2;
	}

	public float getCurrentVariation() {
		return currentVariation;
	}


	public boolean isCo2DispenserTankFlowActive() {
		return co2DispenserTankFlowActive;
	}

	public boolean toBeFilled() {
		return (this.co2DispenserTankLevel <= this.minLevel);
	}

	public void setCo2DispenserTankFlowActive(boolean co2DispenserTankFlowActive) {
		this.co2DispenserTankFlowActive = co2DispenserTankFlowActive;
	}


	public float getCo2DispenserTankLevel() {
		return co2DispenserTankLevel;
	}


	public void setCo2DispenserTankLevel(float co2DispenserTankLevel) {
		this.co2DispenserTankLevel = co2DispenserTankLevel;
	}
	
	/**
	 * Stop the flow of CO2 and stop the device.
	 */
	public void stop() {
		this.stopFlow();
		this.delete();
	}
	
}