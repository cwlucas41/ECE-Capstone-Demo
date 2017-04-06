package edu.slu.iot.client;

import java.io.File;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;

import edu.slu.iot.IoTClient;

class Strand {
    
	private File configurationFile;
	private String topicString;
	private IoTClient client;
	
	public Strand(String topic, File configFile) throws InterruptedException, AWSIotException, AWSIotTimeoutException {
		/*configurationFile = configFile;
		topicString = topic;
		client = new IoTClient(configurationFile.getPath());
        client.subscribe(new StrandListener(topicString, AWSIotQos.QOS0));*/
    }
}
