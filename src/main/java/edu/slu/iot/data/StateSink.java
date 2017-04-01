package edu.slu.iot.data;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.slu.iot.IoTClient;

public class StateSink<T extends State> {
	
	private static JsonParser jp = new JsonParser();
	private IoTClient client;
	private T state;
	private String shadowTopicPrefix;
	private Class<T> clazz;
	
	public StateSink(IoTClient client, String shadowTopicPrefix, Class<T> clazz, T state) {
		this.client = client;
		this.shadowTopicPrefix = shadowTopicPrefix;
		this.clazz = clazz;
		this.state = state;
		
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
		if (desiredInJson != null) {
			T updatedState = GsonSerializer.deserialize(desiredInJson.toString(), clazz);
	    	state.update(updatedState);
	        String stateString = "{\"state\":{\"reported\":" + state.serialize() + "}}";

	    	try {
				client.publish(new AWSIotMessage(shadowTopicPrefix + "/update", AWSIotQos.QOS1, stateString));
			} catch (AWSIotException e) {
				e.printStackTrace();
			}
		}
	}
}
