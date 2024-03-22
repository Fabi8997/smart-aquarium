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
#define SHORT_PUBLISH_INTERVAL (8*CLOCK_SECOND)
#define SIMULATION_INTERVAL (2*CLOCK_SECOND)


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
PROCESS_NAME(mqtt_device_process);
AUTOSTART_PROCESSES(&mqtt_device_process);

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
static char pub_topic1[BUFFER_SIZE];
static char pub_topic2[BUFFER_SIZE];
//static char sub_topic1[BUFFER_SIZE];
//static char sub_topic2[BUFFER_SIZE];
static char sub_topic[BUFFER_SIZE];
//static char sub_topic2[BUFFER_SIZE];

// Periodic timer to check the state of the MQTT client
#define STATE_MACHINE_PERIODIC     (CLOCK_SECOND >> 1)
static struct etimer periodic_timer;
static struct etimer simulation_timer;

/*---------------------------------------------------------------------------*/
/*
 * The main MQTT buffers.
 * We will need to increase if we start publishing more data.
 */
#define APP_BUFFER_SIZE 512
static char app_buffer[APP_BUFFER_SIZE];
static char app_buffer1[APP_BUFFER_SIZE];
//static char app_buffer2[APP_BUFFER_SIZE];
/*---------------------------------------------------------------------------*/
static struct mqtt_message *msg_ptr = 0;

static struct mqtt_connection conn;

/*---------------------------------------------------------------------------*/
PROCESS(mqtt_device_process, "MQTT device");



/*---------------------------------------------------------------------------*/

//VARIABLE TO IMPLEMENT CORRECTLY THE TEMPERATURE SIMULATION, IT'S RELATED TO THE ACTUATOR IMPLEMENTED IN THE CoAP NETWORK
static bool heater_on = false;
static bool fan_on = false;
static bool heater_subscribed = false;
static bool fan_subscribed = false;

//VARIABLE TO IMPLEMENT CORRECTLY THE KH SIMULATION, IT'S RELATED TO THE ACTUATOR IMPLEMENTED IN THE CoAP NETWORK
static int osmotic_water_flow = 0;
static bool osmotic_water_tank_subscribed = false;

//when the heater or the fan are activated a msg is published in the following topic
static void
pub_handler(const char *topic, uint16_t topic_len, const uint8_t *chunk,
            uint16_t chunk_len)
{

//THIS IS A FICTITIOUS MESSAGE!
  //Just for simulation purposes, in order to set the variable to the correct value!!

  LOG_INFO("Pub Handler: topic: ['%s'] message: ['%s']\n", topic, (const char*) chunk);

  //Topic related to the fan status SIMULATION
  if(strcmp(topic, "fan") == 0) {

    	if(strcmp((const char*) chunk, "on") == 0) { //Fan activated

		//It's not possible to have the fan and the heater on simultanously
		heater_on = false;
		fan_on = true;
	} else if(strcmp((const char*) chunk, "off") == 0) { //Fan stopped
		fan_on = false;
	}
    
    return;

  //Topic related to the heater status SIMULATION
  }else if(strcmp(topic, "heater") == 0){

    	if(strcmp((const char*) chunk, "on") == 0) { //Heater activated

		//It's not possible to have the fan and the heater on simultanously
		fan_on = false; 
		heater_on = true;
	} else if(strcmp((const char*) chunk, "off") == 0) { //Heater stopped
		heater_on = false;
	}
    
    return;

  //Topic related to the osmotic water tank status SIMULATION
  }else if(strcmp(topic, "OsmoticWaterTank") == 0) {
	
	if(strcmp((const char*) chunk, "OFF") == 0) { //No water flow
		osmotic_water_flow = 0;
	} else if(strcmp((const char*) chunk, "DEC") == 0) { //Water flow to reduce the kH
		osmotic_water_flow = -1;
	} else if(strcmp((const char*) chunk, "INC") == 0)  { //Water flow to increase the kH
		osmotic_water_flow = 1;
	}

    return;
  }


}

/*---------------------------------------------------------------------------*/
static void
mqtt_event(struct mqtt_connection *m, mqtt_event_t event, void *data)
{
  switch(event) {
  case MQTT_EVENT_CONNECTED: {
    LOG_INFO("Application has a MQTT connection\n");

    state = STATE_CONNECTED;
    break;
  }
  case MQTT_EVENT_DISCONNECTED: {
    LOG_INFO("MQTT Disconnect. Reason %u\n", *((mqtt_event_t *)data));

    state = STATE_DISCONNECTED;
    process_poll(&mqtt_device_process);
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
      LOG_INFO("Application is subscribed to topic successfully\n");
    } else {
      LOG_INFO("Application failed to subscribe to topic (ret code %x)\n", suback_event->return_code);
    }
#else
    LOG_INFO("Application is subscribed to topic successfully\n");
#endif
    break;
  }
  case MQTT_EVENT_UNSUBACK: {
    LOG_INFO("Application is unsubscribed to topic successfully\n");
    break;
  }
  case MQTT_EVENT_PUBACK: {
    LOG_INFO("Publishing complete.\n");
    break;
  }
  default:
    LOG_INFO("Application got a unhandled MQTT event: %i\n", event);
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
/*--------------------------TEMPERATURE-SIMULATION---------------------------*/
/*---------------------------------------------------------------------------*/


/*To avoid the propagation of the error using the float are used integer numbers, multiplied by 10.
The actual values are 25.0, 0.2 and 0.4*/

/*Initialized the value of the temperature to the value at the center of the interval*/
static int temperature_value = 250;

/*Values used respectively to define the upper bound of the possible variation interval and for the standard pH
  variation in case of stabilization using CO2*/
static int max_temperature_variation = 2;
static int temperature_variation_temperature_controller = 4;

/*
  NOTE: for simulation purposes this value is changed based on the value publiced in the topic related to the fan and the heater
	changes, that is a topic created ONLY to make the simulation coherent. 
*/

/*The following function is used to simulate the changes of the temperature sensed by the temperature device; it require a parameter
  that indicates if the temperature controller is active increase/reduce the temperature value.*/
static void change_temperature_simulation(){
	
	/*If no change in the erogation of CO2 is active, so random behaviour*/
	if((fan_on == false) && (heater_on == false)){

		/*Generate an integer belonging to the set {0,1,2} to take a decision for the simulation*/
		int decision = rand() % 3;

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
	 it is done to keep the temperature inside the interval in which the pH can be modified, since the CO2 erogated is dependant on the temperature*/
	}else if(fan_on == true){
		temperature_value -= temperature_variation_temperature_controller;

	/*The osmotic water erogation tries to increase the temperature value,
	 it is done to keep the temperature inside the interval in which the pH can be modified, since the CO2 erogated is dependant on the temperature*/
	} else if(heater_on == true){
		temperature_value += temperature_variation_temperature_controller;
	}

}

/*---------------------------------------------------------------------------*/
/*-------------------------------KH-SIMULATION-------------------------------*/
/*---------------------------------------------------------------------------*/

/*To avoid the propagation of the error using the float are used integer numbers, multiplied by 100.
The actual values are 5.0, 0.1 and 0.2*/

/*Initialized the value of the kH to the value at the center of the interval*/
static int kH_value = 500;

/*Values used respectively to define the upper bound of the possible variation interval and for the standard pH
  variation in case of stabilization using CO2*/
static int max_kH_variation = 10;
static int kH_variation_osmotic_water = 20;

/*
  NOTE: for simulation purposes this value is changed based on the value publiced in the topic related to the OsmoticWaterTank
	changes, that is a topic created ONLY to make the simulation coherent. 
*/


/*The following function is used to simulate the changes of the kH sensed by the kH device; it reads the variable
  that indicates icf osmotic water is being released into the aquarium to increase/reduce the kH value.*/
static void change_kH_simulation(){

	
	/*If no change in the erogation of osmotic water is active, so random behaviour*/
	if(osmotic_water_flow == 0){

		/*Generate an integer belonging to the set {0,1,2} to take a decision for the simulation*/
		int decision = rand() % 3;

		/*Generate a float in the interval [0.0, 0.2] used to vary the kH*/
		int kH_variation = rand() % (max_kH_variation + 1);

		switch(decision){
			/*No variation*/
			case 0:{
				break;
			}
			/*Increment the kH*/
			case 1:{
				kH_value += kH_variation;
				break;
			}
			/*Decrease the kH*/
			case 2:{
				kH_value -= kH_variation;
				break;
			}			
		}
	/*The osmotic water erogation tries to reduce the kH value, it is done to keep the kH inside the interval in which the pH can be modified*/
	}else if(osmotic_water_flow == -1){
		kH_value -= kH_variation_osmotic_water;

	/*The osmotic water erogation tries to increase the kH value, it is done to keep the kH inside the interval in which the pH can be modified*/
	} else if(osmotic_water_flow == 1){
		kH_value += kH_variation_osmotic_water;
	}
}


/*---------------------------------------------------------------------------*/
/*---------------------------------------------------------------------------*/
/*---------------------------------------------------------------------------*/

PROCESS_THREAD(mqtt_device_process, ev, data)
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
  mqtt_register(&conn, &mqtt_device_process, client_id, mqtt_event,
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
			  LOG_INFO("Connecting to the MQTT server!\n");
			  
			  memcpy(broker_address, broker_ip, strlen(broker_ip));
			  
			  mqtt_connect(&conn, broker_address, DEFAULT_BROKER_PORT,
						   (DEFAULT_PUBLISH_INTERVAL * 3) / CLOCK_SECOND,
						   MQTT_CLEAN_SESSION_ON);
			  state = STATE_CONNECTING;
		  }
		  
		  if(state==STATE_CONNECTED){
		  
			  // Subscribe to the fan topic
			  if(fan_subscribed == false){

			  	  strcpy(sub_topic,"fan");

			  	  status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);

				  LOG_INFO("Subscribing to topic fan for simulation purposes!\n");
				  if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
					LOG_ERR("Tried to subscribe but command queue was full!\n");
					//PROCESS_EXIT();
				  }else{
					fan_subscribed = true;
				  }
			  }else if(heater_subscribed == false){
			  
				  // Subscribe to the heater topic
				  strcpy(sub_topic,"heater");

				  status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);

				  LOG_INFO("Subscribing to topic heater for simulation purposes!\n");
				  if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
					LOG_ERR("Tried to subscribe but command queue was full!\n");			
					//PROCESS_EXIT();
				  }else{
					heater_subscribed = true;
				  }
			  }else if(osmotic_water_tank_subscribed == false){

				  //Subscribe to the OsmoticWaterTank topic
				  strcpy(sub_topic,"OsmoticWaterTank");

				  status = mqtt_subscribe(&conn, NULL, sub_topic, MQTT_QOS_LEVEL_0);

				  LOG_INFO("Subscribing to topic OsmoticWaterTank for simulation purposes!\n");
				  if(status == MQTT_STATUS_OUT_QUEUE_FULL) {
					LOG_ERR("Tried to subscribe but command queue was full!\n");
					//PROCESS_EXIT();
				  }else{
					osmotic_water_tank_subscribed = true;
				  }
			  }else if((fan_subscribed == true) && (heater_subscribed == true) && (osmotic_water_tank_subscribed == true)){
					LOG_INFO("Successfully subscribed to all topics!\n");
					state = STATE_SUBSCRIBED;
			  }
		  }

			  
		if(state == STATE_SUBSCRIBED){

			/*---------------------------------------------------------------------------*/
			/*---------------------------------TEMPERATURE-------------------------------*/
			/*---------------------------------------------------------------------------*/

			// Publish something
		        sprintf(pub_topic1, "%s", "temperature");
			
			change_temperature_simulation();

			// Since the precision of the temperature sensor is limeted to +=0.1 then are sent just the first two digit of the fractional part
			sprintf(app_buffer, "{\"temperature\":%d.%d}", (int)(temperature_value/10), temperature_value%10);
			
			mqtt_publish(&conn, NULL, pub_topic1, (uint8_t *)app_buffer, strlen(app_buffer), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);

			//bzero(app_buffer, sizeof(app_buffer));
			//bzero(pub_topic1, sizeof(pub_topic1));

			LOG_INFO("Message: %s published on: %s\n", app_buffer, pub_topic1);

			etimer_set(&simulation_timer, SIMULATION_INTERVAL);
			PROCESS_YIELD();

			/*---------------------------------------------------------------------------*/
			/*-------------------------------------KH------------------------------------*/
			/*---------------------------------------------------------------------------*/

			// Publish something
			sprintf(pub_topic2, "%s", "sensors/kH");
			
			//Pass the global var osmotic_water_flow to the simulation in order to simulate the value correctly
			change_kH_simulation(osmotic_water_flow);

			// Since the precision of the kH sensor is limeted to +=0.01 then are sent just the first two digit of the fractional part
			sprintf(app_buffer1, "{\"kH\":%d.%d}", (int)(kH_value/100), kH_value%100);
			
			//bzero(app_buffer, sizeof(app_buffer1));
			//bzero(pub_topic1, sizeof(pub_topic2));				

			//mqtt_publish(&conn, NULL, pub_topic2, (uint8_t *)app_buffer1, strlen(app_buffer1), MQTT_QOS_LEVEL_0, MQTT_RETAIN_OFF);
			LOG_INFO("Message: %s published on: %s\n", app_buffer1, pub_topic2);


			/*---------------------------------------------------------------------------*/
			/*-------------------------------------PH------------------------------------*/
			/*---------------------------------------------------------------------------*/
		
		} else if ( state == STATE_DISCONNECTED ){
		   LOG_ERR("Disconnected from MQTT broker\n");	
		   state = STATE_INIT;
		}
		
		etimer_set(&periodic_timer, SHORT_PUBLISH_INTERVAL);
      
    }

  }

  PROCESS_END();
}
/*---------------------------------------------------------------------------*/
