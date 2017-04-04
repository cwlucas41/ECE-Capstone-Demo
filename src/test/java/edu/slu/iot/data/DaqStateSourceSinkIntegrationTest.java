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
	
	IoTClient client;
	String shadowTopicPrefix = "$aws/things/test-daq/shadow";
	DaqState clientState;
	int c = 0;
	
	@Before
	public void setup() throws AWSIotException {
		c = 0;
		client = new IoTClient("Certificate1/conf.txt");
	}

	@Test
	public void fullTest() throws AWSIotException, InterruptedException {
		client.publish(new AWSIotMessage(shadowTopicPrefix + "/delete", AWSIotQos.QOS1, ""));
		
		// wait for delete to propogate
		Thread.sleep(2000);
		
		clientState = new StateSource<DaqState>(client, shadowTopicPrefix, DaqState.class).getState();
								
		new StateSink<DaqState>(client, shadowTopicPrefix, DaqState.class, new DaqState(new StateListener() {
			@Override
			public <T extends State> void onStateChangeSucceded(T state) {
				if (state instanceof DaqState) {
					DaqState daqState = (DaqState) state;
					assertEquals(clientState, daqState);
					c++;
				}
			}
		}));
				

		Thread.sleep(2000);
		clientState.update("a", 1.0, 2.0);
		
		Thread.sleep(2000);
		clientState.update("b", 2.0, 4.0);
		
		Thread.sleep(2000);
		assertTrue(c == 2);
	}
	
	@Test
	public void initializesWithNoDesiredState() throws AWSIotException, InterruptedException {
		client.publish(new AWSIotMessage(shadowTopicPrefix + "/delete", AWSIotQos.QOS1, ""));
		
		// wait for delete to propogate
		Thread.sleep(2000);
		
		clientState = new StateSource<DaqState>(client, shadowTopicPrefix, DaqState.class).getState();
								
		new StateSink<DaqState>(client, shadowTopicPrefix, DaqState.class, new DaqState(new StateListener() {
			@Override
			public <T extends State> void onStateChangeSucceded(T state) {
				if (state instanceof DaqState) {
					DaqState daqState = (DaqState) state;
					assertEquals(clientState, daqState);
					c++;
				}
			}
		}));
				

		Thread.sleep(2000);
		clientState.update("a", 1.0, 2.0);
		
		Thread.sleep(2000);
		clientState.update("b", 2.0, 4.0);
		
		Thread.sleep(2000);
		assertTrue(c == 2);
	}
	
	@Test
	public void initializesWithDesiredState() throws AWSIotException, InterruptedException {
		client.publish(new AWSIotMessage(shadowTopicPrefix + "/delete", AWSIotQos.QOS1, ""));
		
		// wait for delete to propogate
		Thread.sleep(2000);
		
		clientState = new StateSource<DaqState>(client, shadowTopicPrefix, DaqState.class).getState();
		clientState.update("a", 1.0, 2.0);
								
		new StateSink<DaqState>(client, shadowTopicPrefix, DaqState.class, new DaqState(new StateListener() {
			@Override
			public <T extends State> void onStateChangeSucceded(T state) {
				if (state instanceof DaqState) {
					DaqState daqState = (DaqState) state;
					assertEquals(clientState, daqState);
					c++;
				}
			}
		}));
		
		Thread.sleep(2000);
		clientState.update("b", 2.0, 4.0);
		
		Thread.sleep(2000);
		assertTrue(c == 2);
	}
	
	@After
	public void tearDown() throws AWSIotException, InterruptedException {
		client.disconnect();
		client = null;
		Thread.sleep(2000);
	}

}
