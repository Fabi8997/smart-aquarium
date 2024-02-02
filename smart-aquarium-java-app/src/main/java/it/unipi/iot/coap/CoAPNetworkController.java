package it.unipi.iot.coap;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Response;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import it.unipi.iot.coap.CO2.CO2Dispenser;
import it.unipi.iot.coap.osmoticwater.OsmoticWaterTank;
import it.unipi.iot.coap.temperature.TemperatureController;
import it.unipi.iot.configuration.ConfigurationParameters;



/**
 * 
 * @author Fabi8997
 *
 *	This class implements both a CoAP server and a CoAP client. It's a server since it allows the actuators to register to it, in order
 *  to make available their IP addresses to be contacted by the application. When acts as a client it sends commands to the actuators 
 *  and queries informations about their status in order to be stored inside the DB.
 */
public class CoAPNetworkController extends CoapServer {
	
	//CoAP Clients
	OsmoticWaterTank osmoticWaterTank;
	TemperatureController temperatureController;
	CO2Dispenser co2Dispenser;
	
	//ConfigurationParameters
	ConfigurationParameters configurationParameters;
	
	/**
	 * Constructs a CoAP server. <br>
	 * Add the registration resource to be handled by the server.
	 * @param configurationParameters
	 */
	public CoAPNetworkController(ConfigurationParameters configurationParameters) {
		super();
		this.add(new CoAPRegistrationResource("registration"));
		this.configurationParameters = configurationParameters;
	}
	
	/**
	 * Getter
	 * @return CoAP Client for the osmotic water tank
	 */
	public OsmoticWaterTank getOsmoticWaterTank() {
		return osmoticWaterTank;
	}

	/**
	 * Getter
	 * @return CoAP Client for the temperature controller
	 */
	public TemperatureController getTemperatureController() {
		return temperatureController;
	}

	/**
	 * Getter
	 * @return CoAP Client for the CO2 dispenser
	 */
	public CO2Dispenser getCo2Dispenser() {
		return co2Dispenser;
	}

	public boolean allDevicesRegistered() {
		return ((osmoticWaterTank != null) && (temperatureController != null) && (co2Dispenser != null));
	}

	/**
	 * 
	 * @author Fabi8997
	 *
	 *	Registration resource. It defines the methods to handle the POST requests coming from the devices that want to register to the 
	 *  application.
	 */
	private class CoAPRegistrationResource extends CoapResource {

		/**
		 * Construct a new resource with the specified name.
		 * @param name of the resource to be created.
		 */
		public CoAPRegistrationResource(String name) {
			super(name);
			//setObservable(true); USELESS?
	 	}
		
		//TODO GET handle, but maybe it is not required
	 	public void handleGET(CoapExchange exchange) {
	 		
	 		Response response = new Response(ResponseCode.CONTENT);
	 		
	 		if(exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_JSON) {
	 			response.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_JSON);
	 			response.setPayload("{\"value\":\"10\"}"); 			
	 		} else {
	 			response.setPayload("Hello");
	 		}
	 		
			exchange.respond(response);
	 	}
	 	
	 	/**
	 	 * Handles the POST request in the given CoAPExchange. It creates CoAP client to interact with the registered devices.
	 	 */
		public void handlePOST(CoapExchange exchange) {
			//Debug
			//System.out.println("[CoAPNetworkController] new message received: " + exchange.getRequestText());
			
			//Retrieve the ipAddress of the sender
			String ipAddress = exchange.getSourceAddress().getHostAddress();
			
			//To contain the device name
			String device = null;
			
			//Objects to handle the JSON format
			JSONParser parser = new JSONParser();
			JSONObject requestTextJSON;
			
			try {
				//Parse the payload of the request
				requestTextJSON = (JSONObject) parser.parse(exchange.getRequestText());
				
				//Retrieve the value associated to the key "device"
				device = (String) requestTextJSON.get("device");
				
			} catch (ParseException e) {
				
				//If the JSON document is malformed send BAD_REQUEST response
				exchange.respond(ResponseCode.BAD_REQUEST);
				e.printStackTrace();
			}
			
			//Check the device value and create a new CoAP Client accordingly
			if(device.equals("osmoticWaterTank")) {
				
				//If no device already registered
				if(osmoticWaterTank == null) {
					
					//Create a new CoAP Client
					osmoticWaterTank = new OsmoticWaterTank(ipAddress,configurationParameters);
					
					System.out.println("[CoAPNetworkController] new " + device + " registered!");
					
					//Set the response code and the payload message
					exchange.respond(ResponseCode.CREATED, "registered");
				}else {
					
					System.out.println("[CoAPNetworkController] " + device + " already registered!");
					
					//Device already registered
					exchange.respond(ResponseCode.BAD_REQUEST);
				}
				
				
			} else if(device.equals("CO2Dispenser")) {
				
				//If no device already registered
				if(co2Dispenser == null) {
					
					//Create a new CoAP Client
					co2Dispenser = new CO2Dispenser(ipAddress,configurationParameters);
				
					System.out.println("[CoAPNetworkController] new " + device + " registered!");
					
					//Set the response code and the payload message
					exchange.respond(ResponseCode.CREATED, "registered");
				}else {
					
					System.out.println("[CoAPNetworkController] " + device + " already registered!");
					
					//Device already registered
					exchange.respond(ResponseCode.BAD_REQUEST);
				}
				
				
			}else if(device.equals("temperatureController")) {
				
				//If no device already registered
				if(temperatureController == null) {
					
					//Create a new CoAP Client
					temperatureController = new TemperatureController(ipAddress,configurationParameters);
					
					System.out.println("[CoAPNetworkController] new " + device + " registered!");
					
					//Set the response code and the payload message
					exchange.respond(ResponseCode.CREATED, "registered");
				}else {
					
					System.out.println("[CoAPNetworkController] " + device + " already registered!");
					
					//Device already registered
					exchange.respond(ResponseCode.BAD_REQUEST);
				}
				
			}else {
				
				//IF IT REACHES THIS POINT SOMETHING IN THE REQUEST IS WRONG
				exchange.respond(ResponseCode.BAD_REQUEST);
			}
	 	}
	}
}
