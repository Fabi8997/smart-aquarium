package iot.unipi.it;

import org.eclipse.paho.client.mqttv3.MqttException;

import mqtt.iot.unipi.it.MQTTCollector;

public class Main {

	public static void main(String[] args) {
		System.out.println("Hello world iot!");
        
        try {
        	
        	MQTTCollector mqttCollector = new MQTTCollector();

        } catch(MqttException me) {

            me.printStackTrace();
        }
	}

}
