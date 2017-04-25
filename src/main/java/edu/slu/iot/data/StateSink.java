package edu.slu.iot.data;

import java.lang.reflect.InvocationTargetException;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import edu.slu.iot.IoTClient;

public class StateSink<T extends State> {
	
	private static JsonParser jp = new JsonParser();
	
	private T state;
	private Class<T> clazz;
	private AWSIotDevice device;
	
	public StateSink(IoTClient client, String thingName, Class<T> clazz, StateListener listener) {
		this.clazz = clazz;
		try {
			state = clazz
			.getConstructor(new Class[] {StateListener.class})
			.newInstance(new Object [] {listener});
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		
		String initialState = null;
		// get initial state once
		try {
			initialState = device.get();
		} catch (AWSIotException e) {
			// no state exists, continue
		}
		
		if (initialState != null) {
			JsonObject desired = jp.parse(initialState).getAsJsonObject().getAsJsonObject("state").getAsJsonObject("desired");
			updateStateAndReport(desired.toString());
		}
	}	
	
	private void updateStateAndReport(String jsonState) {
		T newState;
		try {
			newState = GsonSerializer.deserialize(jsonState, clazz);
			state.update(newState); 
			String reported = "{\"state\": {\"reported\":" + state.serialize() + "}}";			
			device.update(new AWSIotMessage("", AWSIotQos.QOS1, reported), 500);
		} catch (AWSIotException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
