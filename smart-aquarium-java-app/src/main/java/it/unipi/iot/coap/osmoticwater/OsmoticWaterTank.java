package it.unipi.iot.coap.osmoticwater;

import org.eclipse.californium.core.CoapClient;

import it.unipi.iot.configuration.ConfigurationParameters;

public class OsmoticWaterTank extends CoapClient{
	
	int MAX_CAPACITY;
	int current_level;
	boolean flow_active = false;

	public OsmoticWaterTank(String ipAddress, ConfigurationParameters configurationParameters) {
		
		//MAX_CAPACITY = configurationParameters.maxCapacityOsmoticWaterTank;
		//current_level = MAX_CAPACITY;
		
		super("coap://[" + ipAddress + "]/tank");
		
	}
			
	
}
