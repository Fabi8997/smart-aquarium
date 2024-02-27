package it.unipi.iot.ph;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class that represent a pH sample, this sample is sensed by the pH sensor and sent through a MQTT message to the broker.
 * This class allows to parse the JSON string sent by the sensor and offers the methods to insert the sample inside the DB.
 * @author Fabi8997
 */

public class PHSample {
	
	
	/**
	 * pH value present inside the JSON string sent by the sensor
	 */
	private float pHValue;
	
	
	/**
	 * Constructor of the class PHSample; it casts the JSON string in input and initialize the pHValue with the value
	 * associated to the key pH.<br>
	 * Example of a JSON String: 	{"pH": 7.53}
	 * @param JSONString JSON string published in the topic "pH"
	 */
	public PHSample(String JSONString) {
		try {
			
		
			JSONParser parser = new JSONParser();
			JSONObject phJSONObject= (JSONObject) parser.parse(JSONString);
			
			//Initialize the value of pHValue with the value associated to the pH key
			this.pHValue = new Float((Double)phJSONObject.get("pH"));
			
		} catch (ParseException e) {
			System.out.println("[pHSample] Error during the parsing from JSON to pHSample object.");
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Getter that returns the pH value of the sample
	 * @return pH value
	 */
	public float getpHValue() {
		return pHValue;
	}



	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("pH", this.pHValue);
		return jsonObject.toJSONString();
	}
	
	
}