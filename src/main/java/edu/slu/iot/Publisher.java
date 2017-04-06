package edu.slu.iot;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

public abstract class Publisher implements Runnable {
	
	public String topic;
	public AWSIotQos qos;
	
	public Publisher(String topic, AWSIotQos qos) {
		this.topic = topic;
		this.qos = qos;
	}
	
	public void publish(AWSIotMessage message) {
        try {
        	IoTClient.awsIotClient.publish(message);
        } catch (AWSIotException e) {
            System.out.println(System.currentTimeMillis() + ": publish failed for " + message.getStringPayload());
        }
	}
}
