package edu.slu.iot.mockdaq;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import edu.slu.iot.Publisher;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.Sample;

public class StatePublisher extends Publisher {
	
	private String sessionID;
	private String deviceID = "defaultDeviceID";
	
	public StatePublisher(String topic, AWSIotQos qos, String sessionID) {
		super(topic, qos);
		this.sessionID = sessionID;
	}

	@Override
    public void run() {
    	
        while (true) {
        	
        	long millis = System.currentTimeMillis();
        	
            Sample s = new Sample(deviceID, sessionID, millis, (float) Math.sin((double) millis / 1000));
            String jsonSample = GsonSerializer.serialize(s);
            AWSIotMessage message = new NonBlockingPublishListener(topic, qos, jsonSample);
            
            publish(message);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                System.out.println(System.currentTimeMillis() + ": NonBlockingPublisher was interrupted");
                return;
            }
        }
    }
	
	private class NonBlockingPublishListener extends AWSIotMessage {
		
		Sample sample;

	    public NonBlockingPublishListener(String topic, AWSIotQos qos, String payload) {
	        super(topic, qos, payload);
	        sample = GsonSerializer.deserialize(getStringPayload(), Sample.class);
	    }

	    @Override
	    public void onSuccess() {
	        System.out.println(System.currentTimeMillis() + ": >>> " + sample);
	    }

	    @Override
	    public void onFailure() {
	        System.out.println(System.currentTimeMillis() + ": publish failed for " + sample);
	    }

	    @Override
	    public void onTimeout() {
	        System.out.println(System.currentTimeMillis() + ": publish timeout for " + sample);
	    }
	}
}
