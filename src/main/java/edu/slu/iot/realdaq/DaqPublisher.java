package edu.slu.iot.realdaq;


import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.slu.iot.IoTClient;
import edu.slu.iot.Publisher;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.Sample;

public class DaqPublisher extends Publisher {

  private String deviceID = "defaultDeviceID";
  private Process p;
  private long periodInMillis;
  private BufferedReader sampleStream;
  
  public DaqPublisher(IoTClient client, String topic, AWSIotQos qos, Process p, double freq) {
    super(client, topic, qos);
    this.p = p;
    sampleStream = new BufferedReader(new InputStreamReader(p.getInputStream()));
    
    
    periodInMillis = (long) (1/ ((long) freq)) * 1000;
    System.out.println("wait time: " + periodInMillis);
  }
  
  @Override
  public void run() {		  
		
			while (p.isAlive()) {
				try {
					while (sampleStream.ready()) {
						// get line
						String line = sampleStream.readLine();
						String[] fields = line.split(" ");
						
						// parse line
						float value = (float) Integer.parseInt(fields[1]);
						long timeStamp = Long.parseLong(fields[0].split(":")[1]);
						
						// publish sample
						Sample s = new Sample(deviceID, topic, timeStamp, value );
						AWSIotMessage message = new NonBlockingPublishListener(topic, qos, s.serialize());
						publish(message);
					}
			} catch(IOException e) {
				e.printStackTrace();
			}
		} 
  }

  private class NonBlockingPublishListener extends AWSIotMessage {

    Sample sample;

    public NonBlockingPublishListener(String topic, AWSIotQos qos, String payload) {
      super(topic, qos, payload);
      sample = GsonSerializer.deserialize(getStringPayload(), Sample.class);
    }

    @Override
    public void onSuccess() {
      System.out.println(System.currentTimeMillis() + ": >>> " + sample.serialize());
    }

    @Override
    public void onFailure() {

      System.out.println(this.errorCode + " " + this.errorMessage);
      System.out.println(System.currentTimeMillis() + ": publish failed for " + sample);

    }

    @Override
    public void onTimeout() {
      System.out.println(System.currentTimeMillis() + ": publish timeout for " + sample.serialize());
    }

  }
}
