package edu.slu.iot.data;

import java.lang.reflect.InvocationTargetException;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import edu.slu.iot.IoTClient;

public class StateSource<T extends State> {
	
	private T state;
	private AWSIotDevice device;
	
	public StateSource(IoTClient client, String thingName, Class<T> clazz) {
		
		this.device = new AWSIotDevice(thingName);
		try {
			client.attach(device);
		} catch (AWSIotException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			state = clazz
			.getConstructor(new Class[] {StateListener.class})
			.newInstance(new Object [] {
					new StateListener() {
						@Override
						public <S extends State> void onStateChangeSucceded(S state) {
							try {
								String desired = "{\"state\":{\"desired\":" + state.serialize() + "}}";
								device.update(new AWSIotMessage("", AWSIotQos.QOS1, desired), 500);
								System.out.println("source changed to: " + state.serialize());
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
