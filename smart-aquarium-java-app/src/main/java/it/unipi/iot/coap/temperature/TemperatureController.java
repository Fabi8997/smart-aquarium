package it.unipi.iot.coap.temperature;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import it.unipi.iot.configuration.ConfigurationParameters;

public class TemperatureController {
	
	private boolean fanActive;
	private boolean heaterActive;
	private CoapClient fanClient;
	private CoapClient heaterClient;
	
	public TemperatureController(String ipAddress, ConfigurationParameters configurationParameters) {
			
			//MAX_CAPACITY = configurationParameters.maxCapacityOsmoticWaterTank;
			//current_level = MAX_CAPACITY;
			
			this.fanClient = new CoapClient("coap://[" + ipAddress + "]/temperature/fan");
			this.heaterClient = new CoapClient("coap://[" + ipAddress + "]/temperature/heater");
		
			this.fanActive = false;
			this.heaterActive = false;
	}
	
	public void activateFan() {
		
		//send put mode on
		fanClient.put(new CoapHandler() {
            
			@Override
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess()) {
                    	
                        System.out.println("[CoAP Network Controller] Put operation failed [device: temperatureController].");
                    }else {
                    	
                    	System.out.println("[CoAP Network Controller] Fan mode = on.");
                    	
                    	//Set the flag to signal that the flow is active
                		fanActive = true;
                    }
                }
            }

            public void onError() {
                System.err.println("[CoAP Network Controller] Put operation failed [device: temperatureController].");
            }

        }, "mode=on", MediaTypeRegistry.TEXT_PLAIN);
	}

	public void activateHeater() {
		
		//send put mode on
		heaterClient.put(new CoapHandler() {
            
			@Override
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess()) {
                    	
                        System.out.println("[CoAP Network Controller] Put operation failed [device: temperatureController].");
                    }else {
                    	
                    	System.out.println("[CoAP Network Controller] Heater mode = on.");
                    	
                    	//Set the flag to signal that the flow is active
                		heaterActive = true;
                    }
                }
            }

            public void onError() {
                System.err.println("[CoAP Network Controller] Put operation failed [device: temperatureController].");
            }

        }, "mode=on", MediaTypeRegistry.TEXT_PLAIN);
	}
	
	
	public void stopFan() {
		
		//send put mode off
		fanClient.put(new CoapHandler() {
		            
				@Override
		        public void onLoad(CoapResponse response) {
					if (response != null) {
						if(!response.isSuccess()) {
		                    	
							System.out.println("[CoAP Network Controller] Put operation failed [device: temperatureController].");
		                }else {
		                    	
		                	System.out.println("[CoAP Network Controller] Fan mode = off.");
		                    	
		                    //Set the flag to signal that the fan is stopped
		                	fanActive = false;
		                }
		             }
		        }
	
		        public void onError() {
		                System.err.println("[CoAP Network Controller] Put operation failed [device: temperatureController].");
		        }

		}, "mode=off", MediaTypeRegistry.TEXT_PLAIN);
	}
	
public void stopHeater() {
		
		//send put mode off
		heaterClient.put(new CoapHandler() {
		            
				@Override
		        public void onLoad(CoapResponse response) {
					if (response != null) {
						if(!response.isSuccess()) {
		                    	
							System.out.println("[CoAP Network Controller] Put operation failed [device: temperatureController].");
		                }else {
		                    	
		                	System.out.println("[CoAP Network Controller] Heater mode = off.");
		                    	
		                    //Set the flag to signal that the heater is stopped
		                	heaterActive = false;
		                }
		             }
		        }
	
		        public void onError() {
		                System.err.println("[CoAP Network Controller] Put operation failed [device: temperatureController].");
		        }

		}, "mode=off", MediaTypeRegistry.TEXT_PLAIN);
	}

	public boolean isFanActive() {
		return fanActive;
	}
	
	public boolean isHeaterActive() {
		return heaterActive;
	}
	
	public boolean areFanHeaterInactive() {
		return ((!fanActive) && (!heaterActive));
	}
	
	public CoapClient getFanClient() {
		return fanClient;
	}
	
	public CoapClient getHeaterClient() {
		return heaterClient;
	}
	
}
