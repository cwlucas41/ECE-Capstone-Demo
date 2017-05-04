
package edu.slu.iot.mockdaq;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import edu.slu.iot.IoTClient;
import edu.slu.iot.Publisher;
import edu.slu.iot.data.Batch;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.Sample;

public class TestPublisher extends Publisher {
	
	private String topic;
	
	public TestPublisher(IoTClient client, String topic, AWSIotQos qos) {
		super(client, topic, qos);
		this.topic = topic;
	}

	@Override
    public void run() {
		
		Batch batch = new Batch(topic, 200, 1);
    	
        while (true) {
        	
        	long millis = System.currentTimeMillis();
        	
            Sample s = new Sample(millis, (float) Math.sin((double) millis / 1000));
            batch.add(s);
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                System.out.println(System.currentTimeMillis() + ": NonBlockingPublisher was interrupted");
                return;
            }
            
			if (batch.size() > 4) {
				AWSIotMessage message = new NonBlockingPublishListener(topic, qos, batch.serialize());
				publish(message);
				batch = new Batch(topic, 200, 1);
			}
        }
    }
	
	private class NonBlockingPublishListener extends AWSIotMessage {
		
		Batch batch;

	    public NonBlockingPublishListener(String topic, AWSIotQos qos, String payload) {
	        super(topic, qos, payload);
	        batch = GsonSerializer.deserialize(getStringPayload(), Batch.class);
	    }

	    @Override
	    public void onSuccess() {
	        System.out.println(System.currentTimeMillis() + ": >>> " + batch.serialize());
	    }

	    @Override
	    public void onFailure() {
	        System.out.println(System.currentTimeMillis() + ": publish failed for " + batch.serialize());
	    }

	    @Override
	    public void onTimeout() {
	        System.out.println(System.currentTimeMillis() + ": publish timeout for " + batch.serialize());
	    }

	}
}
