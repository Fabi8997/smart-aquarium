package it.unipi.iot;

import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttException;

import it.unipi.iot.configuration.ConfigurationParameters;
import it.unipi.iot.configuration.ConfigurationXML;
import it.unipi.iot.control.ControlLogicThread;
import it.unipi.iot.database.DatabaseManager;
import it.unipi.iot.log.Colors;
import it.unipi.iot.mqtt.MQTTCollector;
import it.unipi.iot.coap.CoAPNetworkController;

/**
 * Main class of the smart aquarium application. It retrieves the configuration
 * parameters from the configuration file, starts the database manager, the MQTT
 * Collector, the CoAP Network Controller (that acts as a registration server
 * to handle the devices on the CoAP network, provides different CoAP clients to
 * interact with the actuators and finally it starts the observers for the
 * resources provided by the devices), starts the thread to check the sensors data
 * and to interact with the actuators and starts a loop to interact with the user 
 * receiving commands from the console.<br>
 * 
 * 
 * @author Fabi8997
 */
public class SmartAquariumApp {

	// To retrieve the configuration parameters
	private static ConfigurationParameters configurationParameters;

	// To better visualize the terminal logs
	private static final String LOG = "[" + Colors.ANSI_CYAN + "Smart Aquarium " + Colors.ANSI_RESET + "]";
	
	// Possible commands
    private static String[] possibleCommands = {"add", "delete", "update", "search", "quit"};


	public static void main(String[] args) throws MqttException {

		System.out.println(LOG + " Welcome to your Smart Aquarium!");

		// Load configuration parameters
		System.out.println(LOG + " Loading configuration parameters...");

		ConfigurationXML configurationXML = new ConfigurationXML();
		configurationParameters = configurationXML.configurationParameters;

		System.out.println(configurationParameters);

		System.out.println(LOG + " Connecting to the database...");

		// Initialize database manager using the configuration parameters
		DatabaseManager db = new DatabaseManager(configurationParameters);

		// Launch mqttCollector
		MQTTCollector mqttCollector = new MQTTCollector(configurationParameters, db);

		System.out.println(LOG + " Launching the CoAP Network Manager...");

		// Create a new CoAP Server to handle the CoAP network
		CoAPNetworkController coapNetworkController = new CoAPNetworkController(configurationParameters, db);

		// Start the CoAP Server
		coapNetworkController.start();

		System.out.println(LOG + " Waiting for the registration of all the devices...");

		// Wait until all the devices are registered
		while (!coapNetworkController.allDevicesRegistered()) {
			try {

				// Sleep for 5 seconds to wait for registration
				Thread.sleep(configurationParameters.sleepIntervalApp);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		System.out.println(LOG + " All the devices are registered to the CoAP Network Controller");

		// When all the devices are registered then the flow of CO2 starts
		if (coapNetworkController.co2DispenserRegistered()) {
			coapNetworkController.getCo2Dispenser().startDispenser();
		}
		
		//Once all the devices are correctly started and registered then start the control logic loop
		ControlLogicThread controlLogic = new ControlLogicThread(configurationParameters, mqttCollector, coapNetworkController);
		controlLogic.start();
		
		//Start the loop to receive commands from the user
		Scanner scanner = new Scanner(System.in);
		
		//Print the list of possible commands
		printPossibleCommands();
		
		while(true) {

	        // Read user input
	        String userInput = scanner.nextLine().trim().toLowerCase();

	        // Process the user input
	        if (isValidCommand(userInput)) {
	            System.out.println(LOG + " Executing command: " + userInput);
	            
	            if (userInput.equals("quit")) {
	            	
	            	//Remove the registration of the CoAP elements
	                //So they stop and tries to register again, so they move back to the previous state!
	            	//Close the connection with the DB
	            	//Close the connection with the scanner
	            	
	                break; // Exit loop if the user wants to quit
	            }
	        } else {
	            System.out.println(LOG + " Invalid command. Please try again.");
	            System.out.println(LOG + " "+Colors.WHITE_UNDERLINED+":help"+Colors.ANSI_RESET+" to see the list of available commands.");
	        }
		}
		
		// Gestire le temporizzazioni
		 
	}

	/**
	 * Prints the available commands
	 */
	private static void printPossibleCommands() {
		
		// Display available commands to the user
        System.out.println(LOG + " Available commands:");
        
        for (String command : possibleCommands) {
            System.out.println(LOG + " -  :" + command);
        }
        
        System.out.println(LOG + " Enter a command: ");
		
	}
	
	/**
	 * Checks if the user input is in the list of available commands
	 * @param userInput user input
	 * @return true if the user input is in the list of available commands, otherwise false.
	 */
	private static boolean isValidCommand(String userInput) {
		
		//Check
		for (String command : possibleCommands) {
            if (userInput.equals(command)) {
                return true;
            }
        }
		return false;
	}
}
