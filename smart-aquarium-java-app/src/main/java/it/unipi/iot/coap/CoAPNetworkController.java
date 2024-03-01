package it.unipi.iot.coap;

import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
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
import it.unipi.iot.database.DatabaseManager;
import it.unipi.iot.log.Colors;



/**
 * 
 * @author Fabi8997
 *
 *	This class implements both a CoAP server and a CoAP client. It's a server since it allows the actuators to register to it, in order
 *  to make available their IP addresses to be contacted by the application. When acts as a client it sends commands to the actuators 
 *  and queries informations about their status in order to be stored inside the DB.
 */
public class CoAPNetworkController extends CoapServer {
	
	private static final String LOG = "[" + Colors.ANSI_PURPLE + "CoAP Controller" + Colors.ANSI_RESET + "]";
	
	//CoAP Clients
	OsmoticWaterTank osmoticWaterTank;
	CoapObserveRelation observeTankRelation;
	TemperatureController temperatureController;
	CO2Dispenser co2Dispenser;
	
	//DB table names
    private final String osmoticWaterTankDatabaseTableName;
	private final String co2DispenserDatabaseTableName;
	
	//ConfigurationParameters
	ConfigurationParameters configurationParameters;
	
	//DB manager to set up the connection to the DB and to query it
	private final DatabaseManager db;
	
	/**
	 * Constructs a CoAP server. <br>
	 * Add the registration resource to be handled by the server.
	 * @param configurationParameters
	 */
	public CoAPNetworkController(ConfigurationParameters configurationParameters, DatabaseManager db) {
		super();
		this.add(new CoAPRegistrationResource("registration"));
		this.configurationParameters = configurationParameters;
		this.db = db;
		this.osmoticWaterTankDatabaseTableName = configurationParameters.osmoticWaterTankDatabaseTableName;
		this.co2DispenserDatabaseTableName = configurationParameters.co2DispenserDatabaseTableName;
	}
	
	/**
	 * Getter
	 * @return CoAP Client for the osmotic water tank
	 */
	public OsmoticWaterTank getOsmoticWaterTank() {
		return osmoticWaterTank;
	}

    
	public boolean osmoticWaterTankRegistered() {
		return osmoticWaterTank != null;
	}
	
	public boolean temperatureControllerRegistered() {
		return temperatureController != null;
	}
	
	public boolean co2DispenserRegistered() {
		return co2Dispenser != null;
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
		
		//TODO GET handle, to obtain from the app what devices are registered
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
			//System.out.println(LOG + " new message received: " + exchange.getRequestText());
			
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
					
					//Create the observer relation
					observeTankRelation = osmoticWaterTank.observe(
							new CoapHandler() {
								@Override public void onLoad(CoapResponse response) {
									
									//Objects to handle the JSON format
									JSONParser parser = new JSONParser();
									JSONObject requestTextJSON = null;
									
									try {
										requestTextJSON = (JSONObject) parser.parse(response.getResponseText());
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										
									}

									//If correctly parsed
									if(requestTextJSON != null) {
										
										//Retrieve the mode field since if the flow can be stopped due to low level 
										String mode = (String) requestTextJSON.get("mode");
										
										//Check if the mode is changed and set the flag
										if(mode.equals("on") && !osmoticWaterTank.isOsmoticWaterTankFlowActive()) {
											
											//Set the flow as active
											osmoticWaterTank.setOsmoticWaterTankFlowActive(true);
											
										}else if(mode.equals("off") && osmoticWaterTank.isOsmoticWaterTankFlowActive()){
											
											//Set the flow as inactive
											osmoticWaterTank.setOsmoticWaterTankFlowActive(false);
											
										}
						
										//Retrieve the tank level
									    osmoticWaterTank.setOsmoticWaterTankLevel(new Float((Double) requestTextJSON.get("level")));
									    
									    //Insert the sample in the DB
									    db.insertSample(osmoticWaterTankDatabaseTableName, osmoticWaterTank.getOsmoticWaterTankLevel(), null);
									    
									    //LOG
									    System.out.println(LOG + " Inserted " + requestTextJSON.toJSONString() + " in " + osmoticWaterTankDatabaseTableName + "." );
									}
								}
								@Override public void onError() {
									System.err.println("-Failed--------");
								}
							});

					
					System.out.println(LOG + " new " + device + " registered!");
					
					//Set the response code and the payload message
					exchange.respond(ResponseCode.CREATED, "registered");
				}else {
					
					System.out.println(LOG + " " + device + " already registered!");
					
					//Device already registered
					exchange.respond(ResponseCode.BAD_REQUEST);
				}
				
				
			} else if(device.equals("CO2Dispenser")) {
				
				//If no device already registered
				if(co2Dispenser == null) {
					
					//Create a new CoAP Client
					co2Dispenser = new CO2Dispenser(ipAddress,configurationParameters);
					
					//Create the observer relation
					observeTankRelation = co2Dispenser.observe(
							new CoapHandler() {
								@Override public void onLoad(CoapResponse response) {
									
									//Objects to handle the JSON format
									JSONParser parser = new JSONParser();
									JSONObject requestTextJSON = null;
									
									try {
										requestTextJSON = (JSONObject) parser.parse(response.getResponseText());
									} catch (ParseException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										
									}

									//If correctly parsed
									if(requestTextJSON != null) {
										
										//Retrieve the mode field since if the flow can be stopped due to low level 
										String mode = (String) requestTextJSON.get("mode");
										
										//Check if the mode is changed and set the flag
										if(mode.equals("on") && !co2Dispenser.isCo2DispenserTankFlowActive()) {
											
											//Set the flow as active
											co2Dispenser.setCo2DispenserTankFlowActive(true);
											
										}else if(mode.equals("off") && co2Dispenser.isCo2DispenserTankFlowActive()){
											
											//Set the flow as inactive
											co2Dispenser.setCo2DispenserTankFlowActive(false);
											
										}
						
										//Retrieve the tank level
									    co2Dispenser.setCo2DispenserTankLevel(new Float((Double) requestTextJSON.get("level")));
									    
									    //Insert the sample in the DB
									    db.insertSample(co2DispenserDatabaseTableName,
									    				co2Dispenser.getCurrentCO2(),
									    				co2Dispenser.getCo2DispenserTankLevel());
									    
									    //LOG
									    System.out.println(LOG + " Inserted {" +
									    				"\"Level\": " + co2Dispenser.getCo2DispenserTankLevel() + "," +
									    				"\"Value\": " + co2Dispenser.getCurrentCO2() + 
									    				"} in " + co2DispenserDatabaseTableName + "." );
									}
								}
								@Override public void onError() {
									System.err.println("-Failed--------");
								}
							});
				
					System.out.println(LOG + " new " + device + " registered!");
					
					//Set the response code and the payload message
					exchange.respond(ResponseCode.CREATED, "registered");
				}else {
					
					System.out.println(LOG + " " + device + " already registered!");
					
					//Device already registered
					exchange.respond(ResponseCode.BAD_REQUEST);
				}
				
				
			}else if(device.equals("temperatureController")) {
				
				//If no device already registered
				if(temperatureController == null) {
					
					//Create a new CoAP Client
					temperatureController = new TemperatureController(ipAddress,configurationParameters);
					
					System.out.println(LOG + " new " + device + " registered!");
					
					//Set the response code and the payload message
					exchange.respond(ResponseCode.CREATED, "registered");
				}else {
					
					System.out.println(LOG + " " + device + " already registered!");
					
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
