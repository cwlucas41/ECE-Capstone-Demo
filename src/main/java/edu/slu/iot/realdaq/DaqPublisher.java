package edu.slu.iot.realdaq;


import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.IOException;
import edu.slu.iot.IoTClient;
import edu.slu.iot.Publisher;
import edu.slu.iot.data.DaqState;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.Sample;
import edu.slu.iot.data.State;
import edu.slu.iot.data.StateListener;
import edu.slu.iot.data.StateSink;

public class DaqPublisher extends Publisher {

  private String sessionID;
  private String deviceID = "defaultDeviceID";
  private IoTClient client;
  private static final String adcReader = "/home/debian/ECE-Capstone-Demo/src/main/c/ECE_Capstone/reader";
  private ExecutorService executor = Executors.newCachedThreadPool();
  private Process p = null;
  
  public DaqPublisher(IoTClient client, String topic, AWSIotQos qos, String sessionID) {
    super(client, topic, qos);
    this.sessionID = sessionID;
    this.client = client;
  }
  
  @Override
  public void run() {
	  	  
	  new StateSink<DaqState>(client, DaqState.class, new StateListener() {
		  
			@Override
			public <T extends State> void onStateChangeSucceded(T state) {
				if (state instanceof DaqState) {
					
					// destroy an existing process if it exists
					if (p != null) {
						p.destroy();
					}
					
					// CONFIGURE DIGITAL POTS HERE
					
					// create new process
					try {
						p = Runtime.getRuntime().exec(adcReader);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					// create and start new runnable to handle output of process, runnable will terminate when process is destroyed
					Runnable dataPublisher = () -> {
						BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
						try {
							while (in.ready()) {
								String[] line = in.readLine().split(" ");
								//System.out.println(String.join(" ",line));
								float in_volts = (float) Integer.parseInt(line[1]);
								Sample s = new Sample(deviceID,sessionID,Long.parseLong(line[0].split(":")[1]), in_volts );
								AWSIotMessage message = new NonBlockingPublishListener(topic, qos, s.serialize());
								publish(message);
							}
						} catch(IOException e) {
							e.printStackTrace();
						}
					};
					executor.submit(dataPublisher);
				}
			}
		  
	  });  
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
