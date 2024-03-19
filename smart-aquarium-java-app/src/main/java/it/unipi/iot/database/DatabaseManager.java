package it.unipi.iot.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import it.unipi.iot.configuration.ConfigurationParameters;
import it.unipi.iot.log.Colors;

/**
 * 
 * @author Fabi8997
 * This class allow the Smart Aquarium Application to interact with the MYSQL database smart_aquarium. <br>
 * Offers the methods to insert the data in the different tables.
 *
 */
public class DatabaseManager {
	
	private static final String LOG_ERROR = "[" + Colors.ANSI_RED + "Database Manager" + Colors.ANSI_RESET + "]";
	
	//Configuration parameters to access the DB
    private final String databaseUsername;
    private final String databasePassword;
    private final String databaseName;
    private final String databaseIP;
    private final int databasePort;
    
    //DB tables name
    private final String pHDatabaseTableName;
    private final String kHDatabaseTableName;
    private final String temperatureDatabaseTableName;
    private final String osmoticWaterTankDatabaseTableName;
    private final String co2DispenserDatabaseTableName;
    private final String fanDatabaseTableName;
    private final String heaterDatabaseTableName;
    
    //Prepared statement to be used during the insertion
    private PreparedStatement preparedStatementPH;
    private PreparedStatement preparedStatementKH;
    private PreparedStatement preparedStatementTemperature;
    private PreparedStatement preparedStatementOsmoticWaterTank;
    private PreparedStatement preparedStatementCO2Dispenser;
    private PreparedStatement preparedStatementFan;
    private PreparedStatement preparedStatementHeater;
    
    //Connection to the DB
    private Connection connection;
    
    /**
     * Constructor that instantiate the parameters read from the configuration, creates the connection with the DB and create the
     * prepared statements to query the different tables.
     * @param configurationParameters
     */
	public DatabaseManager(ConfigurationParameters configurationParameters) {
		
		//Retrieve the parameters from the configuration
		this.databaseUsername = configurationParameters.databaseUsername;
		this.databasePassword = configurationParameters.databasePassword;
		this.databaseName = configurationParameters.databaseName;
		this.databaseIP = configurationParameters.databaseIP;
		this.databasePort = configurationParameters.databasePort;
		this.pHDatabaseTableName = configurationParameters.pHDatabaseTableName;
		this.kHDatabaseTableName = configurationParameters.kHDatabaseTableName;
		this.temperatureDatabaseTableName = configurationParameters.temperatureDatabaseTableName;
		this.osmoticWaterTankDatabaseTableName = configurationParameters.osmoticWaterTankDatabaseTableName;
		this.co2DispenserDatabaseTableName = configurationParameters.co2DispenserDatabaseTableName;
		this.fanDatabaseTableName = configurationParameters.fanDatabaseTableName;
		this.heaterDatabaseTableName = configurationParameters.heaterDatabaseTableName;
		
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
			
			//Create a prepared statement to interact when the table is KH
			preparedStatementKH = connection.prepareStatement("INSERT INTO " +  this.kHDatabaseTableName + " (value) VALUES (?)");
			
			//Create a prepared statement to interact when the table is Temperature
			preparedStatementTemperature = connection.prepareStatement("INSERT INTO " +  this.temperatureDatabaseTableName + " (value) VALUES (?)");
		
			//Create a prepared statement to interact when the table is OsmoticWaterTank
			preparedStatementOsmoticWaterTank = connection.prepareStatement("INSERT INTO " +  this.osmoticWaterTankDatabaseTableName + " (value) VALUES (?)");
		
			//Create a prepared statement to interact when the table is CO2Dispenser
			preparedStatementCO2Dispenser = connection.prepareStatement("INSERT INTO " +  this.co2DispenserDatabaseTableName + " (level, value) VALUES (?,?)");
			
			//Create a prepared statement to interact when the table is Fan
			preparedStatementFan = connection.prepareStatement("INSERT INTO " +  this.fanDatabaseTableName + " (active) VALUES (?)");

			//Create a prepared statement to interact when the table is Heater
			preparedStatementHeater = connection.prepareStatement("INSERT INTO " +  this.heaterDatabaseTableName + " (active) VALUES (?)");

		} catch (SQLException e) {
			System.out.println(LOG_ERROR + " Error during the connection to the database.");
			e.printStackTrace();
		}   
	}
    

	/**
	 * This method allows to insert in the connected database the value passed as second parameter inside the table passed as first argument.
	 * @param table in which the value must be inserted
	 * @param value to insert inside the table
	 * @param level used if the table is CO2Dispenser
	 */
    public boolean insertSample(String table, float value, Float level) {
    	
        try {
        	
        	//If the table is the table of the pH
        	if(table.equals(pHDatabaseTableName) ) {
        		
        		//Use the prepared statement of the pH
        		preparedStatementPH.setFloat(1, value);
        		
        		//If something bad happens throw an exception, the program must continue
        		if(preparedStatementPH.executeUpdate() != 1) {
        			throw new SQLException(LOG_ERROR + " Problem during insertion in " + pHDatabaseTableName + "!\n");
        		}else {
        			
        			//Record inserted correctly
        			return true;
        		}
        	
        	//If the table is the table of the kH
        	}else if(table.equals(kHDatabaseTableName) ) {
        		
        		//Use the prepared statement of the kH
        		preparedStatementKH.setFloat(1, value);
        		
        		//If something bad happens throw an exception, the program must continue
        		if(preparedStatementKH.executeUpdate() != 1) {
        			throw new SQLException(LOG_ERROR + " Problem during insertion in " + kHDatabaseTableName + "!\n");
        		}else {
        			
        			//Record inserted correctly
        			return true;
        		}
        		
        	//If the table is the table of the temperature
        	}else if(table.equals(temperatureDatabaseTableName) ) {
        		
        		//Use the prepared statement of the temperature
        		preparedStatementTemperature.setFloat(1, value);
        		
        		//If something bad happens throw an exception, the program must continue
        		if(preparedStatementTemperature.executeUpdate() != 1) {
        			throw new SQLException(LOG_ERROR + " Problem during insertion in " + temperatureDatabaseTableName + "!\n");
        		}else {
        			
        			//Record inserted correctly
        			return true;
        		}
        	}else if(table.equals(osmoticWaterTankDatabaseTableName) ) {
        		
        		//Use the prepared statement of the temperature
        		preparedStatementOsmoticWaterTank.setFloat(1, value);
        		
        		//If something bad happens throw an exception, the program must continue
        		if(preparedStatementOsmoticWaterTank.executeUpdate() != 1) {
        			throw new SQLException(LOG_ERROR + " Problem during insertion in " + osmoticWaterTankDatabaseTableName + "!\n");
        		}else {
        			
        			//Record inserted correctly
        			return true;
        		}
        	}else if(table.equals(co2DispenserDatabaseTableName) ) {
        		
        		//Use the prepared statement of the temperature
        		preparedStatementCO2Dispenser.setFloat(1, level);
        		preparedStatementCO2Dispenser.setFloat(2, value);
        		
        		//If something bad happens throw an exception, the program must continue
        		if(preparedStatementCO2Dispenser.executeUpdate() != 1) {
        			throw new SQLException(LOG_ERROR + " Problem during insertion in " + co2DispenserDatabaseTableName + "!\n");
        		}else {
        			
        			//Record inserted correctly
        			return true;
        		}
        		
        	//If the table is the table of the fan
        	}else if(table.equals(fanDatabaseTableName) ) {
        		
        		boolean bool_value = (value == 0)?false:true;
        		
        		//Use the prepared statement of the Fan
        		preparedStatementFan.setBoolean(1, bool_value);
        		
        		//If something bad happens throw an exception, the program must continue
        		if(preparedStatementFan.executeUpdate() != 1) {
        			throw new SQLException(LOG_ERROR + " Problem during insertion in " + fanDatabaseTableName + "!\n");
        		}else {
        			
        			//Record inserted correctly
        			return true;
        		}
        		
        	//If the table is the table of the heater
        	}else if(table.equals(heaterDatabaseTableName) ) {
        		
        		boolean bool_value = (value == 0)?false:true;
        		
        		//Use the prepared statement of the Heater
        		preparedStatementHeater.setBoolean(1, bool_value);
        		
        		//If something bad happens throw an exception, the program must continue
        		if(preparedStatementHeater.executeUpdate() != 1) {
        			throw new SQLException(LOG_ERROR + " Problem during insertion in " + heaterDatabaseTableName + "!\n");
        		}else {
        			
        			//Record inserted correctly
        			return true;
        		}

        	}
		} catch (SQLException e) {
			e.printStackTrace();
		} 
        
        //If the program arrives here there is a problem
        return false;
    }

    /**
     * Releases this Connection object's database and JDBC resources immediately instead of waiting for them to be automatically released.
     */
    public void close() {
    	try {
    		this.preparedStatementCO2Dispenser.close();
    		this.preparedStatementKH.close();
    		this.preparedStatementOsmoticWaterTank.close();
    		this.preparedStatementPH.close();
    		this.preparedStatementTemperature.close();
			this.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			System.out.println(LOG_ERROR + " Problem during the closing of the connection.");
			e.printStackTrace();
		}
    }
}
