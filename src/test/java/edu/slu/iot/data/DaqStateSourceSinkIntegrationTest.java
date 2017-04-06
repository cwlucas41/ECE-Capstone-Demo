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
	int c = 0;
	
	@Before
	public void setup() throws AWSIotException {
		c = 0;
		client1 = new IoTClient("Certificate1/conf.txt");
		client2 = new IoTClient("Certificate2/conf.txt");
	}

//	@Test
//	public void fullTest() throws AWSIotException, InterruptedException {
//		client.publish(new AWSIotMessage(shadowTopicPrefix + "/delete", AWSIotQos.QOS1, ""));
//		
//		// wait for delete to propogate
//		Thread.sleep(2000);
//		
//		clientState = new StateSource<DaqState>(client, shadowTopicPrefix, DaqState.class).getState();
//								
//		new StateSink<DaqState>(client, thingName, DaqState.class, new DaqState(new StateListener() {
//			@Override
//			public <T extends State> void onStateChangeSucceded(T state) {
//				if (state instanceof DaqState) {
//					System.out.println("updated");
//					DaqState daqState = (DaqState) state;
//					assertEquals(clientState, daqState);
//					c++;
//				}
//			}
//		}));
//				
//
//		Thread.sleep(2000);
//		clientState.update("a", 1.0, 2.0);
//		
//		Thread.sleep(2000);
//		clientState.update("b", 2.0, 4.0);
//		
//		Thread.sleep(2000);
//		assertTrue(c == 2);
//	}
	
//	@Test
//	public void initializesWithNoDesiredState() throws AWSIotException, InterruptedException {
//		client.publish(new AWSIotMessage(shadowTopicPrefix + "/delete", AWSIotQos.QOS1, ""));
//		
//		// wait for delete to propogate
//		Thread.sleep(2000);
//		
//		clientState = new StateSource<DaqState>(client, shadowTopicPrefix, DaqState.class).getState();
//								
//		new StateSink<DaqState>(client, thingName, DaqState.class, new DaqState(new StateListener() {
//			@Override
//			public <T extends State> void onStateChangeSucceded(T state) {
//				if (state instanceof DaqState) {
//					DaqState daqState = (DaqState) state;
//					assertEquals(clientState, daqState);
//					c++;
//				}
//			}
//		}));
//				
//
//		Thread.sleep(2000);
//		clientState.update("a", 1.0, 2.0);
//		
//		Thread.sleep(2000);
//		clientState.update("b", 2.0, 4.0);
//		
//		Thread.sleep(2000);
//		assertTrue(c == 2);
//	}
	
	@Test
	public void initializesWithDesiredState() throws AWSIotException, InterruptedException {
//		client1.publish(new AWSIotMessage(shadowTopicPrefix + "/delete", AWSIotQos.QOS1, ""));
		
//		// wait for delete to propogate
//		Thread.sleep(2000);
//		
//		clientState = new StateSource<DaqState>(client1, thingName, DaqState.class).getState();
//		
//		clientState.update("a", 1.0, 2.0);
//		Thread.sleep(2000);
						
		System.out.println("creating sink");
		new StateSink<DaqState>(client2, thingName, DaqState.class, new DaqState(new StateListener() {
			@Override
			public <T extends State> void onStateChangeSucceded(T state) {
				if (state instanceof DaqState) {
					DaqState daqState = (DaqState) state;
//					assertEquals(clientState, daqState);
					c++;
				}
			}
		}));
		
		while(true) {
			Thread.sleep(50);
		}
		
		
//		Thread.sleep(2000);
//		clientState.update("b", 2.0, 4.0);
//		
//		Thread.sleep(2000);
//		assertTrue(c == 2);
	}
	
	@After
	public void tearDown() throws AWSIotException, InterruptedException {
		client1.disconnect();
		client1 = null;
		Thread.sleep(2000);
	}

}
