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
	public String MQTTBroker;
	public String MQTTClientId;
	public String pHTopic;
	
	@Override
	public String toString() {
		return "ConfigurationParameters [databaseIP=" + databaseIP + ", databasePort=" + databasePort
				+ ", databaseUsername=" + databaseUsername + ", databasePassword=" + databasePassword
				+ ", databaseName=" + databaseName + ", pHDatabaseTableName=" + pHDatabaseTableName + ", MQTTBroker="
				+ MQTTBroker + ", MQTTClientId=" + MQTTClientId + ", pHTopic=" + pHTopic + "]";
	}
	

	
}
