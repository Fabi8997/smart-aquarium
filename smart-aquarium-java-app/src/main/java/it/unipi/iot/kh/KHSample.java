package it.unipi.iot.kh;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**

 * Class that represent a pH sample, this sample is sensed by the pH sensor and sent through a MQTT message to the broker.
 * This class allows to parse the JSON string sent by the sensor and offers the methods to insert the sample inside the DB.
 * @author Fabi8997 
 */

public class KHSample {
	
	
	/**
	 * pH value present inside the JSON string sent by the sensor
	 */
	private float kHValue;
	
	
	/**
	 * Constructor of the class KHSample; it casts the JSON string in input and initialize the kHValue with the value
	 * associated to the key kH.<br>
	 * Example of a JSON String: 	{"kH": 4.12}
	 * @param JSONString JSON string published in the topic "kH"
	 */
	public KHSample(String JSONString) {
		try {
			
		
			JSONParser parser = new JSONParser();
			JSONObject phJSONObject= (JSONObject) parser.parse(JSONString);
			
			//Initialize the value of pHValue with the value associated to the pH key
			this.kHValue = new Float((Double)phJSONObject.get("kH"));
			
		} catch (ParseException e) {
			System.out.println("[kHSample] Error during the parsing from JSON to kHSample object.");
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Getter that returns the kH value of the sample
	 * @return kH value
	 */
	public float getkHValue() {
		return kHValue;
	}



	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("kH", this.kHValue);
		return jsonObject.toJSONString();
	}
	
	
}