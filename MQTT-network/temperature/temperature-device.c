#include "contiki.h"
#include "net/routing/routing.h"
#include "mqtt.h"
#include "net/ipv6/uip.h"
#include "net/ipv6/uip-icmp6.h"
#include "net/ipv6/sicslowpan.h"
#include "sys/etimer.h"
#include "sys/ctimer.h"
#include "lib/sensors.h"
#include "dev/button-hal.h"
#include "dev/leds.h"
#include "os/sys/log.h"
#include "mqtt-client.h"

#include <string.h>
#include <strings.h>
/*---------------------------------------------------------------------------*/
#define LOG_MODULE "mqtt-client"
#ifdef MQTT_CLIENT_CONF_LOG_LEVEL
#define LOG_LEVEL MQTT_CLIENT_CONF_LOG_LEVEL
#else
#define LOG_LEVEL LOG_LEVEL_DBG
#endif

/*---------------------------------------------------------------------------*/
/* MQTT broker address. */
#define MQTT_CLIENT_BROKER_IP_ADDR "fd00::1"

static const char *broker_ip = MQTT_CLIENT_BROKER_IP_ADDR;

// Defaukt config values
#define DEFAULT_BROKER_PORT         1883
#define DEFAULT_PUBLISH_INTERVAL    (30 * CLOCK_SECOND)


// We assume that the broker does not require authentication


/*---------------------------------------------------------------------------*/
/* Various states */
static uint8_t state;

#define STATE_INIT    		  0
#define STATE_NET_OK    	  1
#define STATE_CONNECTING      2
#define STATE_CONNECTED       3
#define STATE_SUBSCRIBED      4
#define STATE_DISCONNECTED    5

/*---------------------------------------------------------------------------*/
PROCESS_NAME(mqtt_temperature_process);
AUTOSTART_PROCESSES(&mqtt_temperature_process);

/*---------------------------------------------------------------------------*/
/* Maximum TCP segment size for outgoing segments of our socket */
#define MAX_TCP_SEGMENT_SIZE    32
#define CONFIG_IP_ADDR_STR_LEN   64
/*---------------------------------------------------------------------------*/
/*
 * Buffers for Client ID and Topics.
 * Make sure they are large enough to hold the entire respective string
 */
#define BUFFER_SIZE 64

static char client_id[BUFFER_SIZE];
static char pub_topic[BUFFER_SIZE];
static char sub_topic[BUFFER_SIZE];

// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static struct etimer periodic_timer;

/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];
/*---------------------------------------------------------------------------*/
static struct mqtt_message *msg_ptr = 0;

static struct mqtt_connection conn;

/*---------------------------------------------------------------------------*/
PROCESS(mqtt_temperature_process, "MQTT temperature Client");



/*---------------------------------------------------------------------------*/
static void
pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
            uint16_t chunk_len)
{
  printf("Pub Handler: topic='%s' (len=%u), chunk_len=%u\n", topic,
          topic_len, chunk_len);

  if(strcmp(topic, "temperatureController") == 0) {
    printf("temperatureController\n");
	printf("%s\n", chunk);

    //TODO Do something :)
    // Set the temperature_controller_status to the value contained in the payload!

    return;
  }
}
/*---------------------------------------------------------------------------*/
static void
mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
  switch(event) {
  case MQTT_EVENT_CONNECTED: {
    printf("[temperature device] Application has a MQTT connection\n");

    state = STATE_CONNECTED;
    break;
  }
  case MQTT_EVENT_DISCONNECTED: {
    printf("[temperature device] MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));

    state = STATE_DISCONNECTED;
    process_poll(&mqtt_temperature_process);
    break;
  }
  case MQTT_EVENT_PUBLISH: {
    msg_ptr = data;

    pub_handler(msg_ptr->topic, strlen(msg_ptr->topic),
                msg_ptr->payload_chunk, msg_ptr->payload_length);
    break;
  }
  case MQTT_EVENT_SUBACK: {
#if MQTT_311
    mqtt_suback_event_t *suback_event = (mqtt_suback_event_t *)data;

    if(suback_event->success) {
      printf("[temperature device] Application is subscribed to topic successfully\n");
    } else {
      printf("[temperature device] Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
    }
#else
    printf("[temperature device] Application is subscribed to topic successfully\n");
#endif
    break;
  }
  case MQTT_EVENT_UNSUBACK: {
    printf("[temperature device] Application is unsubscribed to topic successfully\n");
    break;
  }
  case MQTT_EVENT_PUBACK: {
    printf("[temperature device] Publishing complete.\n");
    break;
  }
  default:
    printf("[temperature device] Application got a unhandled MQTT event: %i\n", event);
    break;
  }
}

static bool
have_connectivity(void)
{
  if(uip_ds6_get_global(ADDR_PREFERRED) == NULL ||
     uip_ds6_defrt_choose() == NULL) {
    return false;
  }
  return true;
}

/*---------------------------------------------------------------------------*/
/*----------------------------------SIMULATION-------------------------------*/
/*---------------------------------------------------------------------------*/


/*Initialized the value of the temperature to the value at the center of the interval*/
static float temperature_value = 25.0;

/*Extreme of the safe interval for the temperature (to be used for leds)*/
//static float min_temperature_value = 23.0;
//static float max_temperature_value = 27.0;

/*Values used respectively to define the upper bound of the possible variation interval and for the standard pH
  variation in case of stabilization using CO2*/
static float max_temperature_variation = 0.2;
static float temperature_variation_temperature_controller = 0.05;

//TODO change with variation of temperature
/*If CO2_variation = 0 => no stabilization of pH active (START STATE)
  if CO2_variation = -1 => the CO2 erogation tries to reduce the pH gradually
  if CO2_variation = 1 => the CO2 erogation tries to increase the pH gradually

  NOTE: for simulation purposes this value is changed based on the value publiced in the topic related to the CO2
	changes, that is a topic created ONLY to make the simulation coherent. 
*/

//static int CO2_variation = 0;

/*The following function is used to simulate the changes of the temperature sensed by the temperature device; it require a parameter
  that indicates if the temperature controller is active increase/reduce the temperature value.*/
static void change_temperature_simulation(int temperature_controller){

	
	/*If no change in the erogation of CO2 is active, so random behaviour*/
	if(temperature_controller == 0){
		/*Generate an integer belonging to the set {0,1,2} to take a decision for the simulation*/
		int decision = rand() % 3;

		/*Generate a float in the interval [0.0, 0.5] used to vary the temperature*/
		//float temperature_variation = (float)rand()/(float)(RAND_MAX/max_temperature_variation);

		//Add or dec temperature in blocks of 0.2
		switch(decision){
			/*No variation*/
			case 0:{
				break;
			}
			/*Increment the temperature*/
			case 1:{
				temperature_value += max_temperature_variation;
				break;
			}
			/*Decrease the temperature*/
			case 2:{
				temperature_value -= max_temperature_variation;
				break;
			}			
		}
	/*The temperature controller tries to reduce the temperature value,
	 it is done to keep the temperature inside the interval in which the pH can be modified, since the CO2 erogated is dependanto on the temperature*/
	}else if(temperature_controller == -1){
		temperature_value -= temperature_variation_temperature_controller;

	/*The osmotic water erogation tries to increase the temperature value,
	 it is done to keep the temperature inside the interval in which the pH can be modified, since the CO2 erogated is dependanto on the temperature*/
	} else if(temperature_controller == 1){
		temperature_value += temperature_variation_temperature_controller;
	}
}

/*TODO custom behavior of each sensor*/
/*TODO: add the CO2 behaviour both here and in the MQTTCollector;
        maybe before I have to implement the database part! in this way
	the mqtt network can be completed without problems.
	Cambiamenti dati dalla CO2 proporzionali al kH????? maggiore è il kh
	minore sarà il cambiamento del pH, molto bella come idea*/

/*---------------------------------------------------------------------------*/
/*---------------------------------------------------------------------------*/
/*---------------------------------------------------------------------------*/

PROCESS_THREAD(mqtt_temperature_process, ev, data)
{

  PROCESS_BEGIN();
  
  mqtt_status_t status;
  char broker_address[CONFIG_IP_ADDR_STR_LEN];

  printf("MQTT temperature Process\n");

  // Initialize the ClientID as MAC address
  snprintf(client_id, BUFFER_SIZE, "%02x%02x%02x%02x%02x%02x",
                     linkaddr_node_addr.u8[0], linkaddr_node_addr.u8[1],
                     linkaddr_node_addr.u8[2], linkaddr_node_addr.u8[5],
                     linkaddr_node_addr.u8[6], linkaddr_node_addr.u8[7]);

  // Broker registration					 
  mqtt_register(&conn, &mqtt_temperature_process, client_id, mqtt_event,
                  MAX_TCP_SEGMENT_SIZE);
				  
  state=STATE_INIT;
				    
  // Initialize periodic timer to check the status 
  etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);

  /* Main loop */
  while(1) {

    PROCESS_YIELD();

    if((ev == PROCESS_EVENT_TIMER && data == &periodic_timer) || 
	      ev == PROCESS_EVENT_POLL){
			  			  
		  if(state==STATE_INIT){
			 if(have_connectivity()==true)  
				 state = STATE_NET_OK;
		  } 
		  
		  if(state == STATE_NET_OK){
			  // Connect to MQTT server
			  printf("[temperature device] Connecting to the MQTT server!\n");
			  
			  memcpy(broker_address, broker_ip, strlen(broker_ip));
			  
			  mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
						   (DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
						   MQTT_CLEAN_SESSION_ON);
			  state = STATE_CONNECTING;
		  }
		  
		  if(state==STATE_CONNECTED){
		  
			  // Subscribe to a topic
			  strcpy(sub_topic,"temperatureController");

			  status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);

			  printf("[temperature device] Subscribing to topic osmoticWater for simulation purposes!\n");
			  if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
				LOG_ERR("[temperature device] Tried to subscribe but command queue was full!\n");
				PROCESS_EXIT();
			  }
			  
			  state = STATE_SUBSCRIBED;
		  }

			  
		if(state == STATE_SUBSCRIBED){
			// Publish something
		    sprintf(pub_topic, "%s", "temperature");
			
			//TODO pass the global var osmotic_water_status
			change_temperature_simulation(0);

			// Since the precision of the temperature sensor is limeted to +=0.1 then are sent just the first two digit of the fractional part
			sprintf(app_buffer, "{\"temperature\":%.1f}", temperature_value);
			
				
			mqtt_publish(&conn, NULL, pub_topic, (uint8_t *)app_buffer,
               strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
		
		} else if ( state == STATE_DISCONNECTED ){
		   LOG_ERR("[temperature device] Disconnected from MQTT broker\n");	
		   // Recover from error
		}
		
		etimer_set(&periodic_timer, STATE_MACHINE_PERIODIC);
      
    }

  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
