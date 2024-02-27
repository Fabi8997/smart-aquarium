package it.unipi.iot.configuration;

/**
 * 
 * @author Fabi8997
 * TODO
 */
public class ConfigurationParameters {

	public String databaseIP;
	public int databasePort;
	public String databaseUsername;
	public String databasePassword;
	public String databaseName;
	public String pHDatabaseTableName;
	public String kHDatabaseTableName;
	public String temperatureDatabaseTableName;
	public String osmoticWaterTankDatabaseTableName;
	public String co2DispenserDatabaseTableName;
	public String MQTTBroker;
	public String MQTTClientId;
	public String pHTopic;
	public String kHTopic;
	public String temperatureTopic;
	public String osmoticWaterTankTopic;
	public String fanTopic;
	public String heaterTopic;
	public String co2DispenserTopic;
	public float kHLowerBound;
	public float kHUpperBound;
	public float kHOptimalValue;
	public float temperatureLowerBound;
	public float temperatureUpperBound;
	public float temperatureOptimalValue;
	public float pHLowerBound;
	public float pHUpperBound;
	public float pHOptimalValue;
	public float epsilon;
	public float epsilonTemperature;
	@Override
	public String toString() {
		return "ConfigurationParameters [databaseIP=" + databaseIP + ",\n databasePort=" + databasePort
				+ ",\n databaseUsername=" + databaseUsername + ",\n databasePassword=" + databasePassword
				+ ",\n databaseName=" + databaseName + ",\n pHDatabaseTableName=" + pHDatabaseTableName
				+ ",\n kHDatabaseTableName=" + kHDatabaseTableName + ",\n temperatureDatabaseTableName="
				+ temperatureDatabaseTableName + ",\n osmoticWaterTankDatabaseTableName="
				+ osmoticWaterTankDatabaseTableName + ",\n co2DispenserDatabaseTableName="
				+ co2DispenserDatabaseTableName + ",\n MQTTBroker=" + MQTTBroker + ",\n MQTTClientId=" + MQTTClientId
				+ ",\n pHTopic=" + pHTopic + ",\n kHTopic=" + kHTopic + ",\n temperatureTopic=" + temperatureTopic
				+ ",\n osmoticWaterTankTopic=" + osmoticWaterTankTopic + ",\n fanTopic=" + fanTopic
				+ ",\n heaterTopic=" + heaterTopic + ",\n co2DispenserTopic=" + co2DispenserTopic
				+ ",\n kHLowerBound=" + kHLowerBound + ",\n kHUpperBound=" + kHUpperBound + ",\n kHOptimalValue="
				+ kHOptimalValue + ",\n temperatureLowerBound=" + temperatureLowerBound + ",\n temperatureUpperBound="
				+ temperatureUpperBound + ",\n temperatureOptimalValue=" + temperatureOptimalValue
				+ ",\n pHLowerBound=" + pHLowerBound + ",\n pHUpperBound=" + pHUpperBound + ",\n pHOptimalValue="
				+ pHOptimalValue + ",\n epsilon=" + epsilon + ",\n epsilonTemperature=" + epsilonTemperature + "]";
	}
	

	
	

	
}
