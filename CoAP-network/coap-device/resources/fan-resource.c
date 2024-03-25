#include "contiki.h"
#include "coap-engine.h"
#include "dev/leds.h"

#include <string.h>

#include "sys/etimer.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "Fan resource"
#define LOG_LEVEL LOG_LEVEL_INFO


static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);

static bool active = false;

RESOURCE(res_fan,
         "title=\"Fan resource",
         res_get_handler,
         NULL,
         res_put_handler,
	 NULL);


static void
res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  LOG_INFO("Received GET request\n");
  char* mode = (active)?"on":"off";
  coap_set_header_content_format(response, APPLICATION_JSON);
  coap_set_payload(response, buffer, snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"mode\":\"%s\"}", mode));

}

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{
  
  size_t len = 0;
  const char *mode = NULL;
  int success = 1;  

  LOG_INFO("Received PUT request\n");

  //If the request has the mode variable
  if((len = coap_get_post_variable(request, "mode", &mode))) {

    LOG_DBG("mode %s\n", mode);

    //If the mode is on
    if(strncmp(mode, "on", len) == 0) {

	    //Start the fan
	    active = true;
	    LOG_INFO("Fan activated.\n");

    //If the mode is off
    } else if(strncmp(mode, "off", len) == 0) {

	    //Stop the fan
            active = false;
            LOG_INFO("Fan stopped.\n");

    //Invalid value
    } else {
      success = 0;
    }
  } else {
    success = 0;
  } 
  
  if(!success) {
    coap_set_status_code(response, BAD_REQUEST_4_00);
  }
}
