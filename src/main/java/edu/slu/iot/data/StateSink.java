package edu.slu.iot.data;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.slu.iot.IoTClient;

public class StateSink<T extends State> {
	
	private T state;
	private Class<T> clazz;
	IoTClient client;
	private AWSIotDevice device;
	
	public StateSink(IoTClient client, String thingName, Class<T> clazz, T state) {
		this.client = client;
		this.clazz = clazz;
		this.state = state;
		
		this.device = new AWSIotDevice(thingName) {
			@Override
			public void onShadowUpdate(String jsonState) {
				updateStateAndReport(jsonState);
			}
		};
			
		// get initial state once
		try {
			client.attach(device);
			String jsonState = device.get();
			updateStateAndReport(jsonState);
		} catch (AWSIotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
//		try {
//			client.subscribe(new AWSIotTopic(shadowTopicPrefix + "/update/delta", AWSIotQos.QOS1) {
//			    @Override
//			    public void onMessage(AWSIotMessage message) {
//			    	JsonObject desiredInJson = jp.parse(message.getStringPayload()).getAsJsonObject().getAsJsonObject("state");
//			    	updateStateAndReport(desiredInJson);
//			    }
//			});
//			
//			client.subscribe(new AWSIotTopic(shadowTopicPrefix + "/get/accepted", AWSIotQos.QOS1) {
//			    @Override
//			    public void onMessage(AWSIotMessage message) {
//			    	JsonObject desiredInJson = jp.parse(message.getStringPayload()).getAsJsonObject().getAsJsonObject("state").getAsJsonObject("desired");
//			    	
//			    	updateStateAndReport(desiredInJson);
//			    	try {
//						client.unsubscribe(this.topic, 100);
//					} catch (AWSIotException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} catch (AWSIotTimeoutException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//			    }
//			});
//			
//			// get initial state
//			client.publish(new AWSIotMessage(shadowTopicPrefix + "/get", AWSIotQos.QOS1, ""));
//			
//		} catch (AWSIotException e1) {
//			e1.printStackTrace();
//		}
	}	
	
	private void updateStateAndReport(String jsonState) {
		T newState;
		try {
			newState = GsonSerializer.deserialize(jsonState, clazz);
			state.update(newState); 
			device.update(state.serialize());
		} catch (AWSIotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	private void updateStateAndReport(JsonObject desiredInJson) {
//		if (desiredInJson != null) {
//			T updatedState = GsonSerializer.deserialize(desiredInJson.toString(), clazz);
//	    	state.update(updatedState);
//	        String stateString = "{\"state\":{\"reported\":" + state.serialize() + "}}";
//
//	    	try {
//				client.publish(new AWSIotMessage(shadowTopicPrefix + "/update", AWSIotQos.QOS1, stateString));
//			} catch (AWSIotException e) {
//				e.printStackTrace();
//			}
//		}
//	}
}
