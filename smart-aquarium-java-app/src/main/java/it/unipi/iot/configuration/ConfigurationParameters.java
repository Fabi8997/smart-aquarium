package it.unipi.iot.configuration;

public class ConfigurationParameters {

	public String databaseIP;
	public int databasePort;
	public String databaseUsername;
	public String databasePassword;
	public String databaseName;
	
	@Override
	public String toString() {
		return "ConfigurationParameters [databaseIP=" + databaseIP + ", databasePort=" + databasePort
				+ ", databaseUsername=" + databaseUsername + ", databasePassword=" + databasePassword
				+ ", databaseName=" + databaseName + "]";
	}
	
	
}
