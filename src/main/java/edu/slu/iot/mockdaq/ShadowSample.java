package edu.slu.iot.mockdaq;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.slu.iot.IoTClient;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.State;

public class ShadowSample {
		
	public static void main(String args[]) throws InterruptedException, AWSIotException, AWSIotTimeoutException {
    	
		IoTClient client = new IoTClient("Certificate1/conf.txt");
		
		State state = new State();
		
		client.subscribe(new StateListener("$aws/things/dev-daq/shadow/update/delta", AWSIotQos.QOS0, state, client));
		client.subscribe(new InitialStateListener("$aws/things/dev-daq/shadow/get/accepted", AWSIotQos.QOS0, state, client));
		
		// get initial state
		client.publish(new AWSIotMessage("$aws/things/dev-daq/shadow/get", AWSIotQos.QOS0, ""));
		
		// wait for state to be set (async)
		while (!state.isInitialized()) {
			Thread.sleep(50);
		}
		
		client.publish(new StatePublisher(client, "$aws/things/dev-daq/shadow/update", AWSIotQos.QOS0));
		
		Thread.sleep(1000);
		
		System.exit(0);

    }
	
	private static class StateListener extends AWSIotTopic {
		
		private State state;
		private IoTClient client;
		
	    public StateListener(String topic, AWSIotQos qos, State state, IoTClient client) {
	        super(topic, qos);
	        this.state = state;
	        this.client = client;
	    }

	    @Override
	    public void onMessage(AWSIotMessage message) {
	    	JsonParser jp = new JsonParser();
	    	JsonObject jsonState = jp.parse(message.getStringPayload()).getAsJsonObject().getAsJsonObject("state");
	    	State deltaState = GsonSerializer.deserialize(jsonState.toString(), State.class);
	    	state.update(deltaState);
	        
	        String stateString = "{\"state\":{\"reported\":" + GsonSerializer.serialize(state) + "}}";
	    	try {
				client.publish(new AWSIotMessage("$aws/things/dev-daq/shadow/update", AWSIotQos.QOS0, stateString));
			} catch (AWSIotException e) {
				e.printStackTrace();
			}
	    }

	}
	
	private static class InitialStateListener extends AWSIotTopic {	
		
		private State state;
		private IoTClient client;
		
		public InitialStateListener(String topic, AWSIotQos qos, State state, IoTClient client) {
			super(topic, qos);
			this.state = state;
			this.client = client;
		}
		
	    @Override
	    public void onMessage(AWSIotMessage message) {
	    	JsonParser jp = new JsonParser();
	    	JsonObject jsonState = jp.parse(message.getStringPayload()).getAsJsonObject().getAsJsonObject("state").getAsJsonObject("desired");
	    	State desiredState = GsonSerializer.deserialize(jsonState.toString(), State.class);
	    	state.update(desiredState);
	    	
	    	String stateString = "{\"state\":{\"reported\":" + GsonSerializer.serialize(state) + "}}";
	    	try {
				client.publish(new AWSIotMessage("$aws/things/dev-daq/shadow/update", AWSIotQos.QOS0, stateString));
			} catch (AWSIotException e) {
				e.printStackTrace();
			}
	    }
	}
}
