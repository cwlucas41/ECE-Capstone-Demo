package edu.slu.iot.data;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import edu.slu.iot.IoTClient;

public class DaqStateSourceSinkIntegrationTest {
	
	IoTClient client1, client2;
	String shadowTopicPrefix = "$aws/things/test-daq/shadow";
	String thingName = "test-daq";
	DaqState clientState;
	int c;
	StateListener listener = new StateListener() {
		@Override
		public <T extends State> void onStateChangeSucceded(T state) {
			if (state instanceof DaqState) {
				DaqState daqState = (DaqState) state;
				System.out.println("sink changed to: " + daqState.serialize());
				assertEquals(clientState, daqState);
				c++;
			}
		}
	};
	
	@Before
	public void setup() throws AWSIotException {
		c = 0;
		client1 = new IoTClient("Certificate1/conf.txt");
		client2 = new IoTClient("Certificate2/conf.txt");
	}
	
	@Test
	public void initializesWithNoDesiredState() throws AWSIotException, InterruptedException {
		client1.publish(new AWSIotMessage(shadowTopicPrefix + "/delete", AWSIotQos.QOS1, ""));
		
		// wait for delete to propogate
		Thread.sleep(2000);
		
		clientState = new StateSource<DaqState>(client1, thingName, DaqState.class).getState();
		
		Thread.sleep(2000);
								
		new StateSink<DaqState>(client2, thingName, DaqState.class, new DaqState(listener));
				

		Thread.sleep(2000);
		clientState.update("a", 1.0, 2.0);
		
		Thread.sleep(2000);
		clientState.update("b", 2.0, 4.0);
		
		Thread.sleep(2000);
		clientState.setFrequency(50000.0);

		Thread.sleep(2000);
		clientState.setTopic("test");

		Thread.sleep(2000);
		clientState.setGain(10.0);
		
		Thread.sleep(2000);
		assertTrue(c == 5);
	}
	
	@Test
	public void initializesWithDesiredState() throws AWSIotException, InterruptedException {
		client1.publish(new AWSIotMessage(shadowTopicPrefix + "/delete", AWSIotQos.QOS1, ""));
		
		// wait for delete to propogate
		Thread.sleep(2000);
		
		clientState = new StateSource<DaqState>(client1, thingName, DaqState.class).getState();
		
		// set initial desired state
		clientState.update("a", 1.0, 2.0);
		Thread.sleep(2000);
						
		System.out.println("creating sink");
		new StateSink<DaqState>(client2, thingName, DaqState.class, new DaqState(listener));
		
		
		// update desired state
		Thread.sleep(2000);
		clientState.update("b", 2.0, 4.0);
		
		Thread.sleep(2000);
		clientState.setFrequency(50000.0);

		Thread.sleep(2000);
		clientState.setTopic("test");

		Thread.sleep(2000);
		clientState.setGain(10.0);
		
		Thread.sleep(2000);
		assertTrue(c == 5);
	}
	
	@After
	public void tearDown() throws AWSIotException, InterruptedException {
		client1.disconnect();
		client2.disconnect();
	}

}
