package edu.slu.iot.data;

import java.lang.reflect.InvocationTargetException;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import edu.slu.iot.IoTClient;

public class StateSource<T extends State> {
	
	private T state;
	
	public StateSource(IoTClient client, String shadowTopicPrefix, Class<T> clazz) {
		try {
			state = clazz
			.getConstructor(new Class[] {StateListener.class})
			.newInstance(new Object [] {
					new StateListener() {
						@Override
						public <S extends State> void onStateChangeSucceded(S state) {
							try {
								String desired = "{\"state\":{\"desired\":" + state.serialize() + "}}";
								client.publish(new AWSIotMessage(shadowTopicPrefix + "/update", AWSIotQos.QOS1, desired));
							} catch (AWSIotException e) {
								e.printStackTrace();
							}
						}
					}
			});
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public T getState() {
		return state;
	}
}
