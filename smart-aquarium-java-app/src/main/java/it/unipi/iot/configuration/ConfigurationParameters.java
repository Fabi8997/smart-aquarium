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
	public String MQTTBroker;
	public String MQTTClientId;
	public String pHTopic;
	public String kHTopic;
	public String temperatureTopic;
	@Override
	public String toString() {
		return "ConfigurationParameters [databaseIP=" + databaseIP + ",\n databasePort=" + databasePort
				+ ",\n databaseUsername=" + databaseUsername + ",\n databasePassword=" + databasePassword
				+ ",\n databaseName=" + databaseName + ",\n pHDatabaseTableName=" + pHDatabaseTableName
				+ ",\n kHDatabaseTableName=" + kHDatabaseTableName + ",\n temperatureDatabaseTableName="
				+ temperatureDatabaseTableName + ",\n MQTTBroker=" + MQTTBroker + ",\n MQTTClientId=" + MQTTClientId
				+ ",\n pHTopic=" + pHTopic + ",\n kHTopic=" + kHTopic + ",\n temperatureTopic=" + temperatureTopic + "]\n";
	}
	
	
	

	
}
