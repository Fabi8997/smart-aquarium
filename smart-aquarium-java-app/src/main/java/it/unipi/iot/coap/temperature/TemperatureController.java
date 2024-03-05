package it.unipi.iot.coap.temperature;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import it.unipi.iot.configuration.ConfigurationParameters;
import it.unipi.iot.log.Colors;

/**
 * 
 * This class handles two CoapClients to interact with the assigned URIs. <br> It provides the methods to: <br>
 * - activate the fan <br>
 * - stop the fan <br>
 * - activate the heater <br>
 * - stop the heater <br>
 * 
 * @author Fabi8997
 * 
 */
public class TemperatureController {
	
	private static final String LOG = "[" + Colors.ANSI_CYAN + "Smart Aquarium " + Colors.ANSI_RESET + "]";
	private static final String LOG_ERROR = "[" + Colors.ANSI_RED + "Smart Aquarium " + Colors.ANSI_RESET + " ]";
	
	
	//Status
	private boolean fanActive;
	private boolean heaterActive;
	
	//CoAP Clients
	private CoapClient fanClient;
	private CoapClient heaterClient;
	
	/**
	 * Class constructor. It creates two CoapClient to interact with the fan resource and the heater resource.
	 * 
	 * @param ipAddress of the URI
	 * @param configurationParameters configuration parameters
	 */
	public TemperatureController(String ipAddress, ConfigurationParameters configurationParameters) {
			
			//Create two clients to interact with the specified URI
			this.fanClient = new CoapClient("coap://[" + ipAddress + "]/temperature/fan");
			this.heaterClient = new CoapClient("coap://[" + ipAddress + "]/temperature/heater");
		
			this.fanActive = false;
			this.heaterActive = false;
	}
	
	/**
	 * Send a put request to activate the fan and set accordingly the flag in case of success.
	 */
	public void activateFan() {
		
		//send put mode on
		fanClient.put(new CoapHandler() {
            
			@Override
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess()) {
                    	
                        System.out.println(LOG + " Put operation failed [device: temperatureController].");
                    }else {
                    	
                    	System.out.println(LOG + " Fan [ mode = "+Colors.ANSI_GREEN+"on"+Colors.ANSI_RESET+" ].");
                    	
                    	//Set the flag to signal that the flow is active
                		fanActive = true;
                    }
                }
            }

            public void onError() {
                System.err.println(LOG + " Put operation failed [device: temperatureController].");
            }

        }, "mode=on", MediaTypeRegistry.TEXT_PLAIN);
	}

	/**
	 * Send a put request to activate the heater and set accordingly the flag in case of success.
	 */
	public void activateHeater() {
		
		//send put mode on
		heaterClient.put(new CoapHandler() {
            
			@Override
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess()) {
                    	
                        System.out.println(LOG + " Put operation failed [device: temperatureController].");
                    }else {
                    	
                    	System.out.println(LOG + " Heater [ mode = "+Colors.ANSI_GREEN+"on"+Colors.ANSI_RESET+" ].");
                    	
                    	//Set the flag to signal that the flow is active
                		heaterActive = true;
                    }
                }
            }

            public void onError() {
                System.err.println(LOG + " Put operation failed [device: temperatureController].");
            }

        }, "mode=on", MediaTypeRegistry.TEXT_PLAIN);
	}
	
	/**
	 * Send a put request to stop the fan and set accordingly the flag in case of success.
	 */
	public void stopFan() {
		
		//send put mode off
		fanClient.put(new CoapHandler() {
		            
				@Override
		        public void onLoad(CoapResponse response) {
					if (response != null) {
						if(!response.isSuccess()) {
		                    	
							System.out.println(LOG + " Put operation failed [device: temperatureController].");
		                }else {
		                    	
		                	System.out.println(LOG + " Fan [ mode = "+Colors.ANSI_RED+"off"+Colors.ANSI_RESET+" ].");
		                    	
		                    //Set the flag to signal that the fan is stopped
		                	fanActive = false;
		                }
		             }
		        }
	
		        public void onError() {
		                System.err.println(LOG + " Put operation failed [device: temperatureController].");
		        }

		}, "mode=off", MediaTypeRegistry.TEXT_PLAIN);
	}
	
	/**
	 * Send a put request to stop the heater and set accordingly the flag in case of success.
	 */
	public void stopHeater() {
		
		//send put mode off
		heaterClient.put(new CoapHandler() {
		            
				@Override
		        public void onLoad(CoapResponse response) {
					if (response != null) {
						if(!response.isSuccess()) {
		                    	
							System.out.println(LOG + " Put operation failed [device: temperatureController].");
		                }else {
		                    	
		                	System.out.println(LOG + " Heater [ mode = "+Colors.ANSI_RED+"off"+Colors.ANSI_RESET+" ].");
		                    	
		                    //Set the flag to signal that the heater is stopped
		                	heaterActive = false;
		                }
		             }
		        }
	
		        public void onError() {
		                System.out.println(LOG_ERROR + " Put operation failed [device: temperatureController].");
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
	
	/**
	 * Stop the fan, the heater and send a message to stop the devices.
	 */
	public void stop() {
		this.stopFan();
		this.stopHeater();
		
		this.getFanClient().delete();
		this.getHeaterClient().delete();
	}
}
