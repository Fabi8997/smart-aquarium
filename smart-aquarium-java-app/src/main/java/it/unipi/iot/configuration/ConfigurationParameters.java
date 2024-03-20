package it.unipi.iot.configuration;

/**
 * 
 * @author Fabi8997
 * Class that contains all the configuration parameters.
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
	public String fanDatabaseTableName;
	public String heaterDatabaseTableName;
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
	public float minCO2tankLevel;
	public float minOsmoticWaterTankLevel;
	public float maxCO2tankLevel;
	public float maxOsmoticWaterTankLevel;
	public int sleepIntervalApp;
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Configuration parameters:\n databaseIP=");
		builder.append(databaseIP);
		builder.append(",\n databasePort=");
		builder.append(databasePort);
		builder.append(",\n databaseUsername=");
		builder.append(databaseUsername);
		builder.append(",\n databasePassword=");
		builder.append(databasePassword);
		builder.append(",\n databaseName=");
		builder.append(databaseName);
		builder.append(",\n pHDatabaseTableName=");
		builder.append(pHDatabaseTableName);
		builder.append(",\n kHDatabaseTableName=");
		builder.append(kHDatabaseTableName);
		builder.append(",\n temperatureDatabaseTableName=");
		builder.append(temperatureDatabaseTableName);
		builder.append(",\n osmoticWaterTankDatabaseTableName=");
		builder.append(osmoticWaterTankDatabaseTableName);
		builder.append(",\n co2DispenserDatabaseTableName=");
		builder.append(co2DispenserDatabaseTableName);
		builder.append(",\n fanDatabaseTableName=");
		builder.append(fanDatabaseTableName);
		builder.append(",\n heaterDatabaseTableName=");
		builder.append(heaterDatabaseTableName);
		builder.append(",\n MQTTBroker=");
		builder.append(MQTTBroker);
		builder.append(",\n MQTTClientId=");
		builder.append(MQTTClientId);
		builder.append(",\n pHTopic=");
		builder.append(pHTopic);
		builder.append(",\n kHTopic=");
		builder.append(kHTopic);
		builder.append(",\n temperatureTopic=");
		builder.append(temperatureTopic);
		builder.append(",\n osmoticWaterTankTopic=");
		builder.append(osmoticWaterTankTopic);
		builder.append(",\n fanTopic=");
		builder.append(fanTopic);
		builder.append(",\n heaterTopic=");
		builder.append(heaterTopic);
		builder.append(",\n co2DispenserTopic=");
		builder.append(co2DispenserTopic);
		builder.append(",\n kHLowerBound=");
		builder.append(kHLowerBound);
		builder.append(",\n kHUpperBound=");
		builder.append(kHUpperBound);
		builder.append(",\n kHOptimalValue=");
		builder.append(kHOptimalValue);
		builder.append(",\n temperatureLowerBound=");
		builder.append(temperatureLowerBound);
		builder.append(",\n temperatureUpperBound=");
		builder.append(temperatureUpperBound);
		builder.append(",\n temperatureOptimalValue=");
		builder.append(temperatureOptimalValue);
		builder.append(",\n pHLowerBound=");
		builder.append(pHLowerBound);
		builder.append(",\n pHUpperBound=");
		builder.append(pHUpperBound);
		builder.append(",\n pHOptimalValue=");
		builder.append(pHOptimalValue);
		builder.append(",\n epsilon=");
		builder.append(epsilon);
		builder.append(",\n epsilonTemperature=");
		builder.append(epsilonTemperature);
		builder.append(",\n minCO2tankLevel=");
		builder.append(minCO2tankLevel);
		builder.append(",\n minOsmoticWaterTankLevel=");
		builder.append(minOsmoticWaterTankLevel);
		builder.append(",\n maxCO2tankLevel=");
		builder.append(maxCO2tankLevel);
		builder.append(",\n maxOsmoticWaterTankLevel=");
		builder.append(maxOsmoticWaterTankLevel);
		builder.append(",\n sleepIntervalApp=");
		builder.append(sleepIntervalApp);
		builder.append("\n");
		return builder.toString();
	}
}
