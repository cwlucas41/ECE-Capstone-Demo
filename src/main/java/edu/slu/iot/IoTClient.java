package edu.slu.iot;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil.KeyStorePasswordPair;

public class IoTClient {
	
	private final int parallelism = 2;

	public List<AWSIotMqttClient> clientList = new ArrayList<AWSIotMqttClient>(parallelism);
	private String targetThingName;
	private String actualThingName;
	private Executor executor = Executors.newCachedThreadPool();
	
	
	
	public IoTClient(String filename) throws AWSIotException {
		for (int i = 0; i < parallelism; i++) {
			AWSIotMqttClient client = initClient(filename, i);
			client.connect();
		}
	}
	
	public void publish(AWSIotMessage message) throws AWSIotException {
		awsIotClient.publish(message);
	}
	
	public void publish(Publisher publisher) throws InterruptedException {
        executor.execute(publisher);
   	}
	
	public void subscribe(AWSIotTopic topic) throws AWSIotException {
	    awsIotClient.subscribe(topic, 1000);
	}
	
	public void disconnect() throws AWSIotException {
		awsIotClient.disconnect();
	}
	
	public void attach(AWSIotDevice device) throws AWSIotException {
		awsIotClient.attach(device);
	}
	
	public void unsubscribe(AWSIotTopic topic) throws AWSIotException, AWSIotTimeoutException {
	    awsIotClient.unsubscribe(topic);
	}
	
	public String getTargetThingName() {
		return targetThingName;
	}
	
	public String getActualThingName() {
		return actualThingName;
	}
	
    public AWSIotMqttClient initClient(String filename, int number) {
    	
    	File config = new File(filename);
    	Map<String, String> configMap = new HashMap<String, String>();
    	
		try (
			Scanner sc = new Scanner(config);
    ) {

	    	while (sc.hasNextLine()) {
	    		String line = sc.nextLine();
	    		String[] fields = line.split("\\s+");

	    		if (fields.length != 2) {
	    			throw new IllegalArgumentException("invalid format for config file");
	    		}
	    		
	    		configMap.put(fields[0], fields[1]);
	    	}	    	
		} catch (FileNotFoundException e) {
			throw new IllegalArgumentException("bad filename for config file");
		}
    	
        String clientEndpoint = configMap.get("clientEndpoint");
        String clientId = configMap.get("clientId" + number);
        String certificateFile = configMap.get("certificateFile");
        String privateKeyFile = configMap.get("privateKeyFile");
        targetThingName = configMap.get("targetThingName");
        actualThingName = configMap.get("actualThingName");
                
        if (clientEndpoint != null && clientId != null && certificateFile != null && privateKeyFile != null) {
        	KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
            return new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
        } else {
        	throw new IllegalArgumentException("Failed to construct client due to missing arguments");
        }
    }
}
