package edu.slu.iot.data;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.slu.iot.IoTClient;

public class StateSink {
	
	private static JsonParser jp = new JsonParser();
	private IoTClient client;
	private DaqState state;
	private String shadowTopicPrefix;
	
	public StateSink(IoTClient client, String shadowTopicPrefix, StateListener listener) {
		this.client = client;
		this.state = new DaqState(listener);
		this.shadowTopicPrefix = shadowTopicPrefix;
		
		try {
			client.subscribe(new AWSIotTopic(shadowTopicPrefix + "/update/delta", AWSIotQos.QOS1) {
			    @Override
			    public void onMessage(AWSIotMessage message) {
			    	JsonObject desiredInJson = jp.parse(message.getStringPayload()).getAsJsonObject().getAsJsonObject("state");
			    	updateStateAndReport(desiredInJson);
			    }
			});
			
			client.subscribe(new AWSIotTopic(shadowTopicPrefix + "/get/accepted", AWSIotQos.QOS1) {
			    @Override
			    public void onMessage(AWSIotMessage message) {
			    	JsonObject desiredInJson = jp.parse(message.getStringPayload()).getAsJsonObject().getAsJsonObject("state").getAsJsonObject("desired");
			    	updateStateAndReport(desiredInJson);
			    }
			});
			
			// get initial state
			client.publish(new AWSIotMessage(shadowTopicPrefix + "/get", AWSIotQos.QOS1, ""));
			
		} catch (AWSIotException e1) {
			e1.printStackTrace();
		}
	}	
	
	private void updateStateAndReport(JsonObject desiredInJson) {
		DaqState updatedState = GsonSerializer.deserialize(desiredInJson.toString(), DaqState.class);
    	state.update(updatedState);
        String stateString = "{\"state\":{\"reported\":" + GsonSerializer.serialize(state) + "}}";
    	try {
			client.publish(new AWSIotMessage(shadowTopicPrefix + "/update", AWSIotQos.QOS1, stateString));
		} catch (AWSIotException e) {
			e.printStackTrace();
		}
	}
}
