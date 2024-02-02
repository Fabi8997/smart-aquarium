#include "contiki.h"
#include "net/netstack.h"
#include "os/dev/leds.h"
#include "sys/etimer.h"
#include "os/dev/leds.h"

#include "routing/routing.h"

#include "coap-engine.h"
#include "coap-blocking-api.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "omsotic water tank"
#define LOG_LEVEL LOG_LEVEL_INFO

// Define the server
#define SERVER_EP "coap://[fd00::1]:5683"

// Define the interval to wait before sending the new requests
#define START_INTERVAL 5
#define REGISTRATION_INTERVAL 1

// Define the resource
//extern coap_resource_t res_leds;
extern coap_resource_t res_hello;

// Service URL
char *service_url = "/registration";

// To check the connection with the BR
static bool connected = false;

// To check if the device is registered to the coap reg server
static bool registered = false;

/* Declare and auto-start this file's process */
PROCESS(osmotic_water_device, "Osmotic water tank");
AUTOSTART_PROCESSES(&osmotic_water_device);


// Timers used to make the tries for the connection and registration
static struct etimer wait_connection;
static struct etimer wait_registration;


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
PROCESS_THREAD(osmotic_water_device, ev, data)
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

  coap_activate_resource(&res_hello, "test/hello");

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
	const char msg[] = "{\"device\":\"osmoticWaterTank\"}";

	//Set the payload
	coap_set_payload(request, (uint8_t *)msg, sizeof(msg) - 1);

	//Issue the request in a blocking manner
	COAP_BLOCKING_REQUEST(&server_ep, request, client_chunk_handler);

	//Send the msg {"device":"osmoticWaterTank"}
  }

  
  LOG_INFO("Device started correctly!\n");
  
  while(1){
	PROCESS_WAIT_EVENT();
  }

  PROCESS_END();
}
