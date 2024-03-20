package it.unipi.iot.temperature;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Class that represent a temperature sample, this sample is sensed by the temperature sensor and sent through a MQTT message to the broker.
 * This class allows to parse the JSON string sent by the sensor and offers the methods to insert the sample inside the DB.
 * @author Fabi8997
 */
public class TemperatureSample {
	
	
	/**
	 * temperature value present inside the JSON string sent by the sensor
	 */
	private float temperatureValue;
	
	
	/**
	 * Constructor of the class temperatureSample; it casts the JSON string in input and initialize the temperatureValue with the value
	 * associated to the key temperature.<br>
	 * Example of a JSON String: 	{"temperature": 25 }
	 * @param JSONString JSON string published in the topic "temperature"
	 */
	public TemperatureSample(String JSONString) {
		try {
			
		
			JSONParser parser = new JSONParser();
			JSONObject temperatureJSONObject= (JSONObject) parser.parse(JSONString);
			
			//Initialize the value of temperatureValue with the value associated to the temperature key
			this.temperatureValue = new Float((Double)temperatureJSONObject.get("temperature"));
			
		} catch (ParseException e) {
			System.out.println("[temperatureSample] Error during the parsing from JSON to temperatureSample object.");
			e.printStackTrace();
		}
	}
	
	

	/**
	 * Getter that returns the temperature value of the sample
	 * @return temperature value
	 */
	public float getTemperatureValue() {
		return temperatureValue;
	}



	@SuppressWarnings("unchecked")
	@Override
	public String toString() {
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("temperature", this.temperatureValue);
		return jsonObject.toJSONString();
	}
	
	
}