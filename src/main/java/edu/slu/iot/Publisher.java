package edu.slu.iot;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

public abstract class Publisher implements Runnable {
	
	public String topic;
	public AWSIotQos qos;
	private IoTClient client;
	
	public Publisher(IoTClient client, String topic, AWSIotQos qos) {
		this.topic = topic;
		this.qos = qos;
		this.client = client;
	}
	
	public void publish(AWSIotMessage message) {
        try {
        	client.publish(message);
        } catch (AWSIotException e) {
            System.out.println(System.currentTimeMillis() + ": publish failed for " + message.getStringPayload());
        }
	}
}
