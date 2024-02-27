package it.unipi.iot.coap.osmoticwater;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import it.unipi.iot.configuration.ConfigurationParameters;

/**
 * 
 * This class extends the CoapClient class. <br> It provides the methods to: <br>
 * 
 * - activate the flow of osmotic water <br>
 * - stop the flow of osmotic water
 * 
 * @author Fabi8997
 * 
 */
public class OsmoticWaterTank extends CoapClient{
	
	//Osmotic water tank status
	float osmoticWaterTankLevel;
	boolean osmoticWaterTankFlowActive;
	
	/**
	 * Class constructor.
	 * 
	 * @param ipAddress of the URI
	 * @param configurationParameters configuration parameters
	 */
	public OsmoticWaterTank(String ipAddress, ConfigurationParameters configurationParameters) {
		
		super("coap://[" + ipAddress + "]/"+configurationParameters.osmoticWaterTankTopic+"/tank");
		
		this.osmoticWaterTankFlowActive = false;
	}
	
	public void activateFlow() {
		
		//send put mode on
		this.put(new CoapHandler() {
           
			@Override
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess()) {
                    	
                        System.out.println("[CoAP Network Controller] Put operation failed [device: OsmoticWaterTank].");
                    }else {
                    	
                    	System.out.println("[CoAP Network Controller] Osmotic water tank --> mode = on.");
                    	
                    	//Set the flag to signal that the flow is active
                		osmoticWaterTankFlowActive = true;
                    }
                }
            }

            public void onError() {
                System.err.println("[CoAP Network Controller] Put operation failed [device: OsmoticWaterTank].");
            }

			

        }, "mode=on", MediaTypeRegistry.TEXT_PLAIN);

	
	}
	
	public void stopFlow() {
		
		//send put mode off
				this.put(new CoapHandler() {
		            
					@Override
		            public void onLoad(CoapResponse response) {
		                if (response != null) {
		                    if(!response.isSuccess()) {
		                    	
		                        System.out.println("[CoAP Network Controller] Put operation failed [device: OsmoticWaterTank].");
		                    }else {
		                    	
		                    	System.out.println("[CoAP Network Controller] Osmotic water tank mode = off.");
		                    	
		                    	//Set the flag to signal that the flow is stopped
		                		osmoticWaterTankFlowActive = false;
		                    }
		                }
		            }

		            public void onError() {
		                System.err.println("[CoAP Network Controller] Put operation failed [device: OsmoticWaterTank].");
		            }

					

		        }, "mode=off", MediaTypeRegistry.TEXT_PLAIN);
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
