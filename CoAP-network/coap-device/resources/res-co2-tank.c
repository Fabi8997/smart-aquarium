#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"
#include <string.h>

#include "sys/etimer.h"

#include "CO2-dispenser-var.h"

#if PLATFORM_HAS_LEDS || LEDS_COUNT

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "CO2 dispenser"
#define LOG_LEVEL LOG_LEVEL_INFO

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_delete_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler();

//To handle the error propagation and the fact that the floats cannot be sent, then are used integers, then converted into float strings
bool co2_flow = false;
int co2_tank_level = 700000; //float 7000.0
static int minimum_tank_level = 40000; //float 400.0
bool co2_to_be_filled = false;
int co2_value = 0; // float 0.0

EVENT_RESOURCE(res_co2_tank,
         "title=\"Tank resource",
         res_get_handler,
         NULL,
         res_put_handler,
	 res_delete_handler,
         res_event_handler);


static void 
res_delete_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
	LOG_INFO("Received delete request:\n");
	co2_to_stop = true;
}

static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  
  char* mode = (co2_flow)?"on":"off";
  coap_set_header_content_format(response, APPLICATION_JSON);

  if( (co2_tank_level%100) >= 0 && (co2_tank_level%100)<=9){
	coap_set_payload(
		response,
		buffer,
 		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"level\":%d.0%d , \"mode\":\"%s\"}", (int)(co2_tank_level/100), co2_tank_level%100, mode)
	);			
  }else{
	coap_set_payload(
		response,
		buffer,
 		snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"level\":%d.%d , \"mode\":\"%s\"}", (int)(co2_tank_level/100), co2_tank_level%100, mode)
	);	
  }
}

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  
  size_t len = 0;
  const char *mode = NULL;
  const char *value = NULL;
  int success_mode = 1;
  int success_value = 1;  

  LOG_INFO("Received put request:\n");

  //If the request has the mode variable
  if((len = coap_get_post_variable(request, "mode", &mode))) {

    LOG_DBG("mode %s\n", mode);

    //If the mode is on
    if(strncmp(mode, "on", len) == 0) {

      //If the tank is filled
      if(co2_to_be_filled == false){

	//Start the flow
	co2_flow = true;
        LOG_INFO("CO2 is flowing...\n");
	
      }else{
	LOG_INFO("The tank must be changed!\n");
      }
      
    //If the mode is off
    } else if(strncmp(mode, "off", len) == 0) {

      //If the tank is filled
      if(co2_to_be_filled == false){

	//Stop the flow
        co2_flow = false;
        LOG_INFO("CO2 flow stopped.\n");

      //There is on flow, the tank must be filled
      }else{
	LOG_INFO("The tank must be changed!\n");
      }

    //Invalid value
    } else {
      success_mode = 0;
    }
  } else {
    success_mode = 0;
  } 


  //If the request has the value variable
  if((len = coap_get_post_variable(request, "value", &value))) {

    LOG_INFO("New CO2 value: %s\n", value);

    //New quantity of CO2 to dispense
    float new_co2_value = atof(value);

    //Check if the value is greater than 0
    if(new_co2_value > 0){
	co2_value = (int)(new_co2_value*100);
    }else{
	success_value = 0;
    }
  } else {
    success_value = 0;
  }

  //If the post variable is neither mode nor value, then send a BAD REQ RESPONSE
  if(!success_mode && !success_value) {
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
}

static void res_event_handler(){

	if(co2_flow == true){
		
		//Reduce the level of the CO2 tank by the value that is currently flowing
		co2_tank_level -= co2_value;


		if( (co2_tank_level%100) >= 0 && (co2_tank_level%100)<=9){
			LOG_INFO("Level: %d.0%d\n", (int)(co2_tank_level/100), co2_tank_level%100);				
		}else{
			LOG_INFO("Level: %d.%d\n", (int)(co2_tank_level/100), co2_tank_level%100);
		}

		//If the level is too low stop the flow and signal that the tank must be changed
		if ( co2_tank_level <= minimum_tank_level){
			LOG_INFO("Tank level too low! Flow stopped!\n");
			co2_flow = false;
			co2_to_be_filled = true;
			leds_off(LEDS_GREEN);
			leds_on(LEDS_RED);
		}
		
		coap_notify_observers(&res_co2_tank);
	}
}

#endif /* PLATFORM_HAS_LEDS */
