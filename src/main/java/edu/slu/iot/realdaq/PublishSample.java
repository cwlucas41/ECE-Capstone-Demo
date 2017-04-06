package edu.slu.iot.realdaq;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;

import edu.slu.iot.IoTClient;

public class PublishSample {
	
	public static void main(String args[]) throws InterruptedException, AWSIotException, AWSIotTimeoutException {
    	
		IoTClient client = new IoTClient("/home/debian/ECE_Capstone_Networking/Certificate1/conf.txt");
    	
		client.publish(new DaqPublisher("RealDaq", AWSIotQos.QOS1, "DAQ"));

    }
}
