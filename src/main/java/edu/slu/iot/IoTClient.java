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

	private final int parallelism = 5;

	public List<AWSIotMqttClient> clientList = new ArrayList<AWSIotMqttClient>(parallelism);
	public AWSIotMqttClient stateClient;
	private String targetThingName;
	private String actualThingName;
	private Executor executor = Executors.newCachedThreadPool();
	private int i = 0;


	public IoTClient(String filename) throws AWSIotException {

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
		
		targetThingName = configMap.get("targetThingName");
		actualThingName = configMap.get("actualThingName");

		for (int i = 0; i < parallelism; i++) {
			AWSIotMqttClient client = initClient(configMap, i);
			client.connect();
			clientList.add(client);
		}
		stateClient = initClient(configMap, parallelism);
		stateClient.connect();
	}

	public void publish(AWSIotMessage message) throws AWSIotException {
		String topic = message.getTopic();
		topic += "/" + i;
		message.setTopic(topic);

		clientList.get(i).publish(message);
		i = (i + 1) % parallelism;
	}

	public void publish(Publisher publisher) throws InterruptedException {
		executor.execute(publisher);
	}
	
	public void statePublish(AWSIotMessage message) throws AWSIotException {
		stateClient.publish(message);
	}

	public void subscribe(AWSIotTopic iotTopic) throws AWSIotException {
		int i = 0;
		String topic = iotTopic.getTopic();
		for(AWSIotMqttClient client : clientList) {
			String newTopic = topic + "/" + i;
			iotTopic.setTopic(newTopic);
			client.subscribe(iotTopic, 1000);
			i++;
		}
	}

	public void disconnect() throws AWSIotException {
		for(AWSIotMqttClient client : clientList) {
			client.disconnect();
		}
		stateClient.disconnect();
	}

	public void attach(AWSIotDevice device) throws AWSIotException {
		stateClient.attach(device);
	}

	public void unsubscribe(AWSIotTopic iotTopic) throws AWSIotException, AWSIotTimeoutException {
		int i = 0;
		for(AWSIotMqttClient client : clientList) {
			String topic = iotTopic.getTopic();
			topic += "/" + i;
			iotTopic.setTopic(topic);
			client.unsubscribe(iotTopic);
		}
	}

	public String getTargetThingName() {
		return targetThingName;
	}

	public String getActualThingName() {
		return actualThingName;
	}

	public AWSIotMqttClient initClient(Map<String, String> configMap, int number) {

		String clientEndpoint = configMap.get("clientEndpoint");
		String clientId = configMap.get("clientId") + "-" + number;
		String certificateFile = configMap.get("certificateFile");
		String privateKeyFile = configMap.get("privateKeyFile");
		

		if (clientEndpoint != null && clientId != null && certificateFile != null && privateKeyFile != null) {
			KeyStorePasswordPair pair = SampleUtil.getKeyStorePasswordPair(certificateFile, privateKeyFile);
			return new AWSIotMqttClient(clientEndpoint, clientId, pair.keyStore, pair.keyPassword);
		} else {
			throw new IllegalArgumentException("Failed to construct client due to missing arguments");
		}
	}
}
