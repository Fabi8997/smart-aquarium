#include "contiki.h"
#include "coap-engine.h"

#include <string.h>

#include "sys/etimer.h"

/* Log configuration */
#include "sys/log.h"
#define LOG_MODULE "omsotic water tank"
#define LOG_LEVEL LOG_LEVEL_INFO

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_get_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset);
static void res_event_handler(void);

bool flow = false;
float tank_level = 5000.0;

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

  coap_set_header_content_format(response, APPLICATION_JSON);
  coap_set_payload(response, buffer, snprintf((char *)buffer, COAP_MAX_CHUNK_SIZE, "{\"level\":%.2f}", tank_level));

}

static void res_put_handler(coap_message_t *request, coap_message_t *response, uint8_t *buffer, uint16_t preferred_size, int32_t *offset)
{

  size_t len = 0;
  const char *mode = NULL;
  int success = 1;  

  LOG_INFO("Received put request\n");
  if((len = coap_get_post_variable(request, "mode", &mode))) {
    LOG_DBG("mode %s\n", mode);

    if(strncmp(mode, "on", len) == 0) {
      flow = true;
      LOG_INFO("Osmotic water is flowing...\n");
    } else if(strncmp(mode, "off", len) == 0) {
      flow = false;
      LOG_INFO("Osmotic water flow stopped.\n");
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
		tank_level -= 100.0;
		LOG_INFO("Level: %f\n", tank_level);
		coap_notify_observers(&res_tank);
	}
}
