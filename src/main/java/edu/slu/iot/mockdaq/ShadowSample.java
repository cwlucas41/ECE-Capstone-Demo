package edu.slu.iot.mockdaq;

import com.amazonaws.services.iot.client.AWSIotException;

import edu.slu.iot.IoTClient;
import edu.slu.iot.data.DaqState;
import edu.slu.iot.data.StateListener;
import edu.slu.iot.data.StateSink;
import edu.slu.iot.data.StateSource;

public class ShadowSample {
		
	public static void main(String args[]) throws InterruptedException, AWSIotException {
    	
		IoTClient client = new IoTClient("Certificate1/conf.txt");
		String shadowTopicPrefix = "$aws/things/dev-daq/shadow";
						
		new StateSink(client, shadowTopicPrefix, new StateListener() {
			@Override
			public void onStateChange(DaqState state) {
				System.out.println("state changed to: " + state);
			}
		});	
		
		DaqState state = new StateSource(client, shadowTopicPrefix).getState();
		
		Thread.sleep(2000);
		state.update("a", 1.0, 2.0);
		
		Thread.sleep(2000);
		state.update("b", 2.0, 4.0);
		
		Thread.sleep(2000);
		state.update("c", 4.0, 8.0);
		
		Thread.sleep(2000);
		state.update("d", 8.0, 16.0);
    }
}
