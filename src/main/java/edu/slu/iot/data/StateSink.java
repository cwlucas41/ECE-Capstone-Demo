package edu.slu.iot.data;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.slu.iot.IoTClient;

public class StateSink<T extends State> {
	
	private T state;
	private Class<T> clazz;
	private AWSIotDevice device;
	
	public StateSink(IoTClient client, String thingName, Class<T> clazz, T state) {
		this.clazz = clazz;
		this.state = state;
		
		this.device = new AWSIotDevice(thingName) {
			@Override
			public void onShadowUpdate(String jsonState) {
				updateStateAndReport(jsonState);
			}
		};
				
		try {
			client.attach(device);
		} catch (AWSIotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		// get initial state once
		try {
			JsonParser jp = new JsonParser();
			String jsonState = device.get();
			JsonObject desired = jp.parse(jsonState).getAsJsonObject().getAsJsonObject("state").getAsJsonObject("desired");
			updateStateAndReport(desired.toString());
		} catch (AWSIotException e) {
			System.out.println("skipping get");
		}
	}	
	
	private void updateStateAndReport(String jsonState) {
		T newState;
		try {
			newState = GsonSerializer.deserialize(jsonState, clazz);
			state.update(newState); 
			String reported = "{\"state\": {\"reported\":" + state.serialize() + "}}";			
			device.update(new AWSIotMessage("", AWSIotQos.QOS1, reported) {
				@Override
				public void onFailure() {
					System.out.println("failure");
				}
				
				
				@Override
				public void onSuccess() {
					System.out.println("success");
				}
				
				@Override
				public void onTimeout() {
					System.out.println("timeout");
				}
			}, 500);
		} catch (AWSIotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
