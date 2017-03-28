package edu.slu.iot.mockdaq;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;

import edu.slu.iot.IoTClient;
import edu.slu.iot.mockdaq.TestPublisher;

public class ShadowSample {
	
	public static void main(String args[]) throws InterruptedException, AWSIotException, AWSIotTimeoutException {
    	
		IoTClient client = new IoTClient("Certificate2/conf.txt");
    	
//		client.publish(new TestPublisher("test", AWSIotQos.QOS0, "demo"));
		client.subscribe(new TestTopicListener("aws/things/dev-daq/shadow/update/accepted", AWSIotQos.QOS0));

    }
}
