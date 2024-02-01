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
/*void client_chunk_handler(coap_message_t *response)
{
  const uint8_t *chunk;

  if(response == NULL) {
    puts("Request timed out");
    return;
  }

  //IF CREATED OR OK
	registerd = true! e set a verde il led
        ora si entra nel comportamento base del dispositivo!
	Copiare questo codice per CO2 e per Temperature
	

  int len = coap_get_payload(response, &chunk);

  printf("|%.*s", len, (char *)chunk);
}
*/

/*---------------------------------------------------------------------------*/
PROCESS_THREAD(osmotic_water_device, ev, data)
{
  //static coap_endpoint_t server_ep;
  //static coap_message_t request[1]; 

  PROCESS_BEGIN();

  PROCESS_PAUSE();

  //coap_endpoint_parse(SERVER_EP, strlen(SERVER_EP), &server_ep);

  etimer_set( &wait_connection, CLOCK_SECOND * START_INTERVAL);

  //Led yellow to notify that the device is not connected yet
  leds_toggle(LEDS_YELLOW);

  LOG_INFO("[osmotic water device] device started! \n");

  //coap_activate_resource(&res_leds, "led");

  LOG_INFO("[osmotic water device] Connectiong to the Border Router... \n");

  while(!connected){
	//Wait CONNECTION_INTERVAL seconds to check if there is a connection to the BR
	PROCESS_WAIT_UNTIL(etimer_expired(&wait_connection));

	//Check if there is a connection with the BR
	if(NETSTACK_ROUTING.node_is_reachable()){
		LOG_INFO("[osmotic water device] Connected to the Border Router! \n");
		connected = true;
		leds_toggle(LEDS_YELLOW);

	}else {
		etimer_reset( &wait_connection );	
	}
  }

  etimer_set( &wait_registration, CLOCK_SECOND * REGISTRATION_INTERVAL);
  while(!registered){
	PROCESS_WAIT_UNTIL(etimer_expired(&wait_registration));

	//Blink the yellow led until registered
	leds_toggle(LEDS_GREEN);

	//Send the msg {"device":"osmoticWaterTank"}

	etimer_reset( &wait_registration);
  }
  

  PROCESS_END();
}
