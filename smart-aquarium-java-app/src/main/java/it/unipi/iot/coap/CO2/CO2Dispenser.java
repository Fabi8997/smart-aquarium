package it.unipi.iot.coap.CO2;

import org.eclipse.californium.core.CoapClient;

import it.unipi.iot.configuration.ConfigurationParameters;

public class CO2Dispenser extends CoapClient {
	
	private static float currentCO2 = 25; //change 
	private static float currentVariation = 0;
	
	public CO2Dispenser(String ipAddress, ConfigurationParameters configurationParameters) {
			
			super("coap://[" + ipAddress + "]/test/hello");
			
			this.currentCO2 = 25;
			this.currentVariation = 0;
		}
	
	
	public static void computeNewCO2(float pH, float kH, float temperature) {
		
		float PKa = (float) (((3404.71)/(temperature + 273.15)) + (0.032786*(temperature + 273.15) - 14.8435));
		float newCO2 = (float) (15.69692*kH*Math.pow(10, PKa - pH));
		
		currentVariation = Math.abs(newCO2 - currentCO2);
		
		/*if ((newCO2 - currentCO2) > 0.5){
			currentCO2 = newCO2;
		}*/
		
	}


	public float getCurrentCO2() {
		return currentCO2;
	}


	public float getCurrentVariation() {
		return currentVariation;
	}
	
	
}
