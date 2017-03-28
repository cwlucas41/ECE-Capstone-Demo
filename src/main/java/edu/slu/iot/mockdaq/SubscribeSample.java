package edu.slu.iot.mockdaq;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;

import edu.slu.iot.IoTClient;
import edu.slu.iot.mockdaq.TestTopicListener;

public class SubscribeSample {
    
	public static void main(String args[]) throws InterruptedException, AWSIotException, AWSIotTimeoutException {
    	
		IoTClient client = new IoTClient("Certificate1/conf.txt");
    	
		client.subscribe(new TestTopicListener("test", AWSIotQos.QOS0));
    }
}
