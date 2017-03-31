package edu.slu.iot.mockdaq;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import edu.slu.iot.IoTClient;
import edu.slu.iot.Publisher;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.State;

public class StatePublisher extends Publisher {
	
	public StatePublisher(IoTClient client, String topic, AWSIotQos qos) {
		super(client, topic, qos);
	}

	@Override
    public void run() {
    	
        	        	
        State s = new State("test", 50000.0, 10.0);
        String desiredState = GsonSerializer.serialize(s);
        String state = "{ \"state\": { \"desired\": " + desiredState+ " } }";
        AWSIotMessage message = new NonBlockingPublishListener(topic, qos, state);
        
        publish(message);
    }
	
	private class NonBlockingPublishListener extends AWSIotMessage {
		
	    public NonBlockingPublishListener(String topic, AWSIotQos qos, String payload) {
	        super(topic, qos, payload);
	    }

	    @Override
	    public void onSuccess() {
	        System.out.println(System.currentTimeMillis() + ": >>> " + getStringPayload());
	    }

	    @Override
	    public void onFailure() {
	        System.out.println(System.currentTimeMillis() + ": publish failed for " + getStringPayload());
	    }

	    @Override
	    public void onTimeout() {
	        System.out.println(System.currentTimeMillis() + ": publish timeout for " + getStringPayload());
	    }
	}
}
