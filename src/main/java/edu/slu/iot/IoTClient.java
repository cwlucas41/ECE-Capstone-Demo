package edu.slu.iot;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import com.amazonaws.services.iot.client.AWSIotDevice;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil;
import com.amazonaws.services.iot.client.sample.sampleUtil.SampleUtil.KeyStorePasswordPair;

public class IoTClient {
	
	public static AWSIotMqttClient awsIotClient;
	private String tableName;
	
	public IoTClient(String filename) throws AWSIotException {
        initClient(filename);
        awsIotClient.connect();
	}
	
	public void publish(AWSIotMessage message) throws AWSIotException {
		awsIotClient.publish(message);
	}
	
	public Thread publish(Publisher publisher) throws InterruptedException {
        Thread nonBlockingPublishThread = new Thread(publisher);

        nonBlockingPublishThread.start();
        
        return nonBlockingPublishThread;
	}
	
	public void subscribe(AWSIotTopic topic) throws AWSIotException {
	    awsIotClient.subscribe(topic, true);
	}
	
	public void unsubscribe(String topic, int timeout) throws AWSIotException, AWSIotTimeoutException {
		awsIotClient.unsubscribe(topic, timeout);
	}
	
	public void disconnect() throws AWSIotException {
		awsIotClient.disconnect();
	}
	
	public void attach(AWSIotDevice device) throws AWSIotException {
		awsIotClient.attach(device);
	}
	
	public void unsubscribe(AWSIotTopic topic, int timeout) throws AWSIotException, AWSIotTimeoutException {
	    awsIotClient.unsubscribe(topic, timeout);
	}
	
	public String getTableName() {
		return tableName;
	}
	
    public void initClient(String filename) {
    	
    	File config = new File(filename);
    	Map<String, String> configMap = new HashMap<String, String>();
    	
    	Scanner sc = null;
		try {
			sc = new Scanner(config);

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
		} finally {
			sc.close();
		}
    	
        String clientEndpoint = configMap.get("clientEndpoint");
        String clientId = configMap.get("clientId");
        tableName = configMap.get("tableName");
        String certificateFile = configMap.get("certificateFile");
        String privateKeyFile = configMap.get("privateKeyFile");
                
        if (clientEndpoint != null && clientId != null && certificateFile != null && privateKeyFile != null) {
        	KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
            awsIotClient = new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
        } else {
        	throw new IllegalArgumentException("Failed to construct client due to missing arguments");
        }
    }
}
