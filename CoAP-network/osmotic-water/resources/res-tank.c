#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

#include <string.h>

#include "sys/etimer.h"

#include "osmotic-water-device-var.h"

#if PLATFORM_HAS_LEDS || LEDS_COUNT

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "omsotic water tank"
#define LOG_LEVEL LOG_LEVEL_INFO

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler();

static bool flow = false;
float tank_level = 5000.0;
static float minimum_tank_level = 500.0;
bool to_be_filled = false;


EVENT_RESOURCE(res_tank,
         "title=\"Tank resource",
         res_get_handler,
         NULL,
         res_put_handler,
	 NULL,
         res_event_handler);

static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  
  char* mode = (flow)?"on":"off";
  coap_set_header_content_format(response, APPLICATION_JSON);
  coap_set_payload(response, buffer, snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"level\":%.2f , \"mode\":\"%s\"}", tank_level, mode));

}

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  
  size_t len = 0;
  const char *mode = NULL;
  int success = 1;  

  LOG_INFO("Received put request\n");

  //If the request has the mode variable
  if((len = coap_get_post_variable(request, "mode", &mode))) {

    LOG_DBG("mode %s\n", mode);

    //If the mode is on
    if(strncmp(mode, "on", len) == 0) {

      //If the tank is filled
      if(to_be_filled == false){

	//Start the flow
	flow = true;
        LOG_INFO("Osmotic water is flowing...\n");
	
      }else{
	LOG_INFO("The tank must be filled!\n");
      }
      
    //If the mode is off
    } else if(strncmp(mode, "off", len) == 0) {

      //If the tank is filled
      if(to_be_filled == false){

	//Stop the flow
        flow = false;
        LOG_INFO("Osmotic water flow stopped.\n");

      //There is on flow, the tank must be filled
      }else{
	LOG_INFO("The tank must be filled!\n");
      }

    //Invalid value
    } else {
      success = 0;
    }
  } else {
    success = 0;
  } if(!success) {
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
}

static void res_event_handler(){

	if(flow == true){

		//Reduce the tank level
		tank_level -= 50.0;

		LOG_INFO("Level: %f\n", tank_level);

		if ( tank_level <= minimum_tank_level){
			LOG_INFO("Tank level too low! Flow stopped!");
			flow = false;
			to_be_filled = true;
			leds_off(LEDS_GREEN);
			leds_on(LEDS_RED);
		}
		
		coap_notify_observers(&res_tank);
	}
}

#endif /* PLATFORM_HAS_LEDS */
