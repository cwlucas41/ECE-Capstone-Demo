package edu.slu.iot.data;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import edu.slu.iot.IoTClient;

public class StateSource {
	
	private DaqState state;
	
	public StateSource(IoTClient client, String shadowTopicPrefix) {
		this.state = new DaqState(new StateListener() {
			
			@Override
			public void onStateChange(DaqState state) {
				try {
					String desired = "{\"state\":{\"desired\":" + state + "}}";
					client.publish(new AWSIotMessage(shadowTopicPrefix + "/update", AWSIotQos.QOS1, desired));
				} catch (AWSIotException e) {
					e.printStackTrace();
				}
				
			}
		});
	}
	
	public DaqState getState() {
		return state;
	}
}
