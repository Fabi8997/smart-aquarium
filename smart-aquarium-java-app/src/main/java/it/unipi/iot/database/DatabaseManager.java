package it.unipi.iot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import it.unipi.iot.configuration.ConfigurationParameters;

/**
 * 
 * @author Fabi8997
 * This class allow the Smart Aquarium Application to interact with the MYSQL database smart_aquarium. <br>
 * Offers the methods to insert the data in the different tables.
 *
 */
public class DatabaseManager {
	
    private final String databaseUsername;
    private final String databasePassword;
    private final String databaseName;
    private final String databaseIP;
    private final String pHDatabaseTableName;
    private final int databasePort;
    private PreparedStatement preparedStatementPH;
    private Connection connection;
    
	public DatabaseManager(ConfigurationParameters configurationParameters) {
		this.databaseUsername = configurationParameters.databaseUsername;
		this.databasePassword = configurationParameters.databasePassword;
		this.databaseName = configurationParameters.databaseName;
		this.databaseIP = configurationParameters.databaseIP;
		this.databasePort = configurationParameters.databasePort;
		this.pHDatabaseTableName = configurationParameters.pHDatabaseTableName;
		
		//Create the connection to MYSQL
		StringBuilder stringBuilder = new StringBuilder("jdbc:mysql://");
		stringBuilder.append(this.databaseIP).append(":")
		.append(this.databasePort).append("/")
		.append(this.databaseName);
		
		
		try {
			//Connect to database
			connection = DriverManager.getConnection(stringBuilder.toString(), this.databaseUsername, this.databasePassword);
			
			//Create a prepared statement to interact when the table is PH
			preparedStatementPH = connection.prepareStatement("INSERT INTO " +  this.pHDatabaseTableName + " (value) VALUES (?)");
		} catch (SQLException e) {
			System.out.println("[DatabaseManager] Error during the connection to the database.");
			e.printStackTrace();
		}   
	}
    

	/**
	 * This method allows to insert in the connected database the value passed as second parameter inside the table passed as first argument.
	 * @param table in which the value must be inserted
	 * @param value to insert inside the table
	 */
    public void insertSample(String table, float value) {
    	
        try {
        	if(table.equals(pHDatabaseTableName) ) {
        		preparedStatementPH.setFloat(1, value);
        		if(preparedStatementPH.executeUpdate() != 1) {
        			throw new SQLException("[DatabaseManager] Problem during insertion in " + pHDatabaseTableName + "!\n");
        		}
        	}
        	
		} catch (SQLException e) {
			e.printStackTrace();
		} 
    }

}
