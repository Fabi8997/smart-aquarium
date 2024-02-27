#include "contiki.h"
#include "net/netstack.h"
#include "os/dev/leds.h"
#include "sys/etimer.h"
#include "os/dev/leds.h"
#include "os/dev/button-hal.h"

#include "routing/routing.h"

#include "coap-engine.h"
#include "coap-blocking-api.h"

#include "CO2-dispenser-var.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "CO2 dispenser"
#define LOG_LEVEL LOG_LEVEL_INFO

// Define the server
#define SERVER_EP "coap://[fd00::1]:5683"

// Define the interval to wait before sending the new requests
#define START_INTERVAL 5
#define REGISTRATION_INTERVAL 1
#define FLOW_INTERVAL 5 //TODO change it

// Define the resource
extern coap_resource_t res_tank;

// Service URL
char *service_url = "/registration";

// To check the connection with the BR
static bool connected = false;

// To check if the device is registered to the coap reg server
static bool registered = false;

/* Declare and auto-start this file's process */
PROCESS(co2_dispenser, "CO2 dispenser");
AUTOSTART_PROCESSES(&co2_dispenser);


// Timers used to make the tries for the connection and registration
static struct etimer wait_connection;
static struct etimer wait_registration;
static struct etimer flow_timer;

//Button to be used to signal the fill of the tank
static button_hal_button_t *btn;

/* This function is will be passed to COAP_BLOCKING_REQUEST() to handle responses. */
void client_chunk_handler(coap_message_t *response)
{
  const uint8_t *chunk;

  //If no response received
  if(response == NULL) {
    	LOG_INFO("Request timed out\n");

	//Reset the timer for the registration
	etimer_reset( &wait_registration);

   	return;
  }	

  coap_get_payload(response, &chunk);
	
  if(strcmp((char*)chunk, "registered") == 0){
	LOG_INFO("Registration completed!\n");

	//Turn off the yellow led
	leds_single_off(LEDS_YELLOW);

	//Turn on the green led
	leds_set(LEDS_GREEN);

	//Set the flag to signal that the registration is completed
	registered = true;

  }else{
	LOG_INFO("Sending a new registration request...\n");

	//Bad response from the server, retry the registration
	etimer_reset( &wait_registration);
  }

}

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(co2_dispenser, ev, data)
{
  //Represent the endpoint
  static coap_endpoint_t server_ep;

  //Represent the message
  static coap_message_t request[1]; 

  PROCESS_BEGIN();

  PROCESS_PAUSE();

  //Populate the coap_endpoint_t data structure
  coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

  etimer_set( &wait_connection, CLOCK_SECOND * START_INTERVAL);

  //Led yellow to notify that the device is not connected yet
  leds_toggle(LEDS_YELLOW);

  coap_activate_resource(&res_tank, "co2Dispenser/tank");

  LOG_INFO("Connectiong to the Border Router... \n");

  while(!connected){
	//Wait CONNECTION_INTERVAL seconds to check if there is a connection to the BR
	PROCESS_WAIT_UNTIL(etimer_expired(&wait_connection));

	//Check if there is a connection with the BR
	if(NETSTACK_ROUTING.node_is_reachable()){
		LOG_INFO("Connected to the Border Router! \n");

		//Set the flag to signal the end of the connection
		connected = true;

		//Start the blink of the yellow led
		leds_toggle(LEDS_YELLOW);

	}else {

		//If the border router is not yet reachable retry after CONNECTION_INTERVAL
		etimer_reset( &wait_connection );	
	}
  }

  LOG_INFO("Registering to the CoAP Network Controller... \n");
  
  //Start the registration timer
  etimer_set( &wait_registration, CLOCK_SECOND * REGISTRATION_INTERVAL);


  while(!registered){
	PROCESS_WAIT_UNTIL(etimer_expired(&wait_registration));

	//Blink the yellow led until registered
	leds_toggle(LEDS_YELLOW);

	//Prepare the message
	coap_init_message(request, COAP_TYPE_CON, COAP_POST, 0);
	coap_set_header_uri_path(request, service_url);
	const char msg[] = "{\"device\":\"CO2Dispenser\"}";

	//Set the payload
	coap_set_payload(request, (uint8_t *)msg, sizeof(msg) - 1);

	//Issue the request in a blocking manner
	COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

  }

  
  LOG_INFO("Device started correctly!\n");

  //Set the timer that check if every FLOW_INTERVAL seconds an info about the resource must be sent to the server
  etimer_set( &flow_timer, CLOCK_SECOND * FLOW_INTERVAL);

  
  while(1){

	//Wait for the expiration of the timer OR a button event
	PROCESS_WAIT_EVENT_UNTIL( ev == PROCESS_EVENT_TIMER || ev == button_hal_periodic_event );
	
	//If the event is related to the timer then trigger the event resource
	if(ev == PROCESS_EVENT_TIMER){

		res_tank.trigger();

		//Reset the timer 
		etimer_reset(&flow_timer);

	}else if((ev == button_hal_periodic_event) && (to_be_filled == true)) {
	//IF the button has been pressed AND the co2 tank must be filled		
	  
	  //Retrieve the data about the button
	  btn = (button_hal_button_t *)data;

          //Blink (every second the red blink during the pression)
	  leds_toggle(LEDS_RED);

	  //If it is pressed for 5 second it means that the co2 is filled
	  if(btn->press_duration_seconds == 5) {
		
		  //Reset the boolean variable
		  to_be_filled = false;

	          //TODO Put the tank to its maximum level
		  tank_level = 5000.0;

		  //Turn on the green led and turn off the red led
		  leds_off(LEDS_RED);
		  leds_on(LEDS_GREEN);

		  //Start the flow again
		  flow = true;

		  res_tank.trigger();
	
		  LOG_INFO("Tank filled correctly!\n");

	  //At the release of the button check if the press duration is lower than 5
	  }else if(ev == button_hal_release_event){

		//Retrieve the data about the button
		btn = (button_hal_button_t *)data;

		//If the button was not pressed for sufficient time keep the red led on
		if(btn->press_duration_seconds < 5){
			leds_on(LEDS_RED);
		}
	  }
 	}
  }


  PROCESS_END();
}
