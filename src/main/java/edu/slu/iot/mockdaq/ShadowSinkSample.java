package edu.slu.iot.mockdaq;

import com.amazonaws.services.iot.client.AWSIotException;

import edu.slu.iot.IoTClient;
import edu.slu.iot.data.DaqState;
import edu.slu.iot.data.State;
import edu.slu.iot.data.StateListener;
import edu.slu.iot.data.StateSink;

public class ShadowSinkSample {

	public static void main(String[] args) throws InterruptedException, AWSIotException {
		IoTClient client = new IoTClient("Certificate1/conf.txt");
		String thingName = "test-daq";
		
		new StateSink<DaqState>(client, thingName, DaqState.class, new DaqState(new StateListener() {
			@Override
			public <T extends State> void onStateChangeSucceded(T state) {
				if (state instanceof DaqState) {
					DaqState daqState = (DaqState) state;
					System.out.println("state changed to: " + daqState.serialize());
				}
			}
		}));
		
		while(true) {
			Thread.sleep(50);
		}
	}

}
