package it.unipi.iot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;

import it.unipi.iot.configuration.ConfigurationParameters;

public class DatabaseManager {
	
    private final String databaseUsername;
    private final String databasePassword;
    private final String databaseName;
    private final String databaseIP;
    private final int databasePort;
    private PreparedStatement preparedStatement;
    private Connection connection;
    
	public DatabaseManager(ConfigurationParameters configurationParameters) {
		this.databaseUsername = configurationParameters.databaseUsername;
		this.databasePassword = configurationParameters.databasePassword;
		this.databaseName = configurationParameters.databaseName;
		this.databaseIP = configurationParameters.databaseIP;
		this.databasePort = configurationParameters.databasePort;
		
		//Create the connection with the mysql dbms
		StringBuilder stringBuilder = new StringBuilder("jdbc:mysql://");
		stringBuilder.append(this.databaseIP).append(":")
		.append(this.databasePort).append("/")
		//.append(this.databaseName);
		.append("prova");
		
		try {
			connection = DriverManager.getConnection(stringBuilder.toString(), this.databaseUsername, this.databasePassword);
			preparedStatement = connection.prepareStatement("INSERT INTO data (id, timestamp, value) VALUES (?, ?, ?)");
		} catch (SQLException e) {
			System.out.println("[DatabaseManager] Error during the connection to the database.");
			e.printStackTrace();
		}   
	}
    
	//TODO This works correctly, next step:
	//	   - Create the db and the correct entries smart_aquarium and pH with autoincremental id, timestamp, value
	//     - try to use directly the insert sample passing the sample
    public void insertSample() {
    	
        try {
        	//preparedStatement.setString(1,"data");
        	//preparedStatement.setInt(2, 99999);
            //preparedStatement.setString(3,"prova");
            //preparedStatement.setInt(4, 222);
        	
        	preparedStatement.setInt(1, 212112);
        	preparedStatement.setTimestamp(2, null, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            preparedStatement.setInt(3, 222);
        	
			System.out.println("Rows affected: " + preparedStatement.executeUpdate());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    }

}
