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

import it.unipi.iot.coap.osmoticwater.OsmoticWaterTank;
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
	
	OsmoticWaterTank osmoticWaterTank;
	ConfigurationParameters configurationParameters;
	
	public CoAPNetworkController(ConfigurationParameters configurationParameters) {
		super();
		this.add(new CoAPRegistrationResource("registration"));
		this.configurationParameters = configurationParameters;
	}
	
	
	

	private class CoAPRegistrationResource extends CoapResource {

		public CoAPRegistrationResource(String name) {
			super(name);
			setObservable(true);
	 	}
		
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
	 	
		public void handlePOST(CoapExchange exchange) {
			
			System.out.println("Received " + exchange.getRequestText());
			String ipAddress = exchange.getSourceAddress().getHostAddress();
			String device = null;
			JSONParser parser = new JSONParser();
			JSONObject requestTextJSON;
			
			try {
				requestTextJSON = (JSONObject) parser.parse(exchange.getRequestText());
				device = (String) requestTextJSON.get("device");
			} catch (ParseException e) {
				exchange.respond(ResponseCode.BAD_REQUEST);
				e.printStackTrace();
			}
			
			if(device.equals("osmoticWaterTank")) {
				osmoticWaterTank = new OsmoticWaterTank(ipAddress,configurationParameters);
				
				System.out.println("[CoAPNetworkController] new " + device + " registered!");
				
				exchange.respond(ResponseCode.CREATED);
			}
					
			//exchange.respond(response);
	 	}

	}
}
