package it.unipi.iot.coap.osmoticwater;

import org.eclipse.californium.core.CoapClient;

import it.unipi.iot.configuration.ConfigurationParameters;

public class OsmoticWaterTank extends CoapClient{
	
	//Osmotic water tank status
	float osmoticWaterTankLevel;
	boolean osmoticWaterTankFlowActive;
	
	public OsmoticWaterTank(String ipAddress, ConfigurationParameters configurationParameters) {
		
		//MAX_CAPACITY = configurationParameters.maxCapacityOsmoticWaterTank;
		//current_level = MAX_CAPACITY;
		
		super("coap://[" + ipAddress + "]/tank");
		
		this.osmoticWaterTankFlowActive = false;
	}
	
	public void activateFlow() {
		//send put mode on
		//set flow active to false
	}
	
	public void stopFlow() {
		//send put mode off
		//set flow active to false
	}

	public float getOsmoticWaterTankLevel() {
		return osmoticWaterTankLevel;
	}

	public void setOsmoticWaterTankLevel(float osmoticWaterTankLevel) {
		this.osmoticWaterTankLevel = osmoticWaterTankLevel;
	}

	public boolean isOsmoticWaterTankFlowActive() {
		return osmoticWaterTankFlowActive;
	}

	public void setOsmoticWaterTankFlowActive(boolean osmoticWaterTankFlowActive) {
		this.osmoticWaterTankFlowActive = osmoticWaterTankFlowActive;
	}
			
	
}
