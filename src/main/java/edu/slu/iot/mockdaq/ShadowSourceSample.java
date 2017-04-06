package edu.slu.iot.mockdaq;

import com.amazonaws.services.iot.client.AWSIotException;

import edu.slu.iot.IoTClient;
import edu.slu.iot.data.DaqState;
import edu.slu.iot.data.StateSource;

public class ShadowSourceSample {

	public static void main(String[] args) throws AWSIotException, InterruptedException {
		IoTClient client = new IoTClient("Certificate2/conf.txt");
		String thingName = "test-daq";
		
		DaqState clientState = new StateSource<DaqState>(client, thingName, DaqState.class).getState();
		
		clientState.update("a", 1.0, 1.0);
		Thread.sleep(2000);
		
		clientState.update("b", 2.0, 2.0);
		Thread.sleep(2000);
		
		clientState.update("c", 4.0, 3.0);
		Thread.sleep(2000);
		
		clientState.update("d", 8.0, 4.0);
		Thread.sleep(2000);
		
		client.disconnect();
		System.exit(0);
	}
}
