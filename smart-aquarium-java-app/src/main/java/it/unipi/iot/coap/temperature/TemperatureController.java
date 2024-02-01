package it.unipi.iot.coap.temperature;

import org.eclipse.californium.core.CoapClient;

import it.unipi.iot.configuration.ConfigurationParameters;

public class TemperatureController extends CoapClient {
	
	//TODO Parameters
	
public TemperatureController(String ipAddress, ConfigurationParameters configurationParameters) {
		
		//MAX_CAPACITY = configurationParameters.maxCapacityOsmoticWaterTank;
		//current_level = MAX_CAPACITY;
		
		super("coap://[" + ipAddress + "]/test/hello");
	}
}
