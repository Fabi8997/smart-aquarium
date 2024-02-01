package it.unipi.iot.coap.CO2;

import org.eclipse.californium.core.CoapClient;

import it.unipi.iot.configuration.ConfigurationParameters;

public class CO2Dispenser extends CoapClient {
	
	//TODO
	
public CO2Dispenser(String ipAddress, ConfigurationParameters configurationParameters) {
		
		//MAX_CAPACITY = configurationParameters.maxCapacityOsmoticWaterTank;
		//current_level = MAX_CAPACITY;
		
		super("coap://[" + ipAddress + "]/test/hello");
	}

}
