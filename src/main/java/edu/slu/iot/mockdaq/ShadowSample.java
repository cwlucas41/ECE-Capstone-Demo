package edu.slu.iot.mockdaq;

import com.amazonaws.services.iot.client.AWSIotException;

import edu.slu.iot.IoTClient;
import edu.slu.iot.data.DaqState;
import edu.slu.iot.data.State;
import edu.slu.iot.data.StateListener;
import edu.slu.iot.data.StateSink;
import edu.slu.iot.data.StateSource;

public class ShadowSample {
	
	static DaqState stateObject;
		
	public static void main(String args[]) throws InterruptedException, AWSIotException {
    	
		IoTClient client = new IoTClient("Certificate1/conf.txt");
		String shadowTopicPrefix = "$aws/things/dev-daq/shadow";
								
		new StateSink<DaqState>(client, shadowTopicPrefix, DaqState.class, new DaqState(new StateListener() {
			@Override
			public <T extends State> void onStateChangeSucceded(T state) {
				if (state instanceof DaqState) {
					DaqState daqState = (DaqState) state;
					
					// replace to handle state change as desired
					System.out.println(
							"new state: topic: " + daqState.getTopic() +
							", frequency: " + daqState.getFrequency() +
							", gain: " + daqState.getGain()
					);
				}
			}
		}));	
		
		DaqState state = new StateSource<DaqState>(client, shadowTopicPrefix, DaqState.class).getState();
		
		Thread.sleep(2000);
		state.update("a", 1.0, 2.0);
		
		Thread.sleep(2000);
		state.update("b", 2.0, 4.0);
		
		Thread.sleep(2000);
		state.update("c", 4.0, 8.0);
		
		Thread.sleep(2000);
		state.update("d", 8.0, 16.0);
		
		Thread.sleep(2000);
		System.exit(0);
    }
}
