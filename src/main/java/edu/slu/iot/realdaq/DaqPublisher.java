package edu.slu.iot.realdaq;

import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import edu.slu.iot.IoTClient;
import edu.slu.iot.Publisher;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.Sample;

public class DaqPublisher extends Publisher {

  private String sessionID;
  private String deviceID = "defaultDeviceID";

  public DaqPublisher(IoTClient client, String topic, AWSIotQos qos, String sessionID) {
    super(client, topic, qos);
    this.sessionID = sessionID;
  }

  @Override
  public void run() {


    File file = new File("/home/debian/ECE_Capstone_Networking/src/main/c/ECE_Capstone_ADC/SAMPLE-SESSION");
    try (
    		Scanner sc = new Scanner(file);
    ) {
      while(true){
        if(sc.hasNextLine()){
          String value = sc.nextLine();

          String[] params = value.split(" ");
          
          float parsedValue = ((float)Integer.parseInt(params[0])*1.8f)/(float)4096;
          long millis = Long.parseLong(params[1]); 
          
          try{
            Thread.sleep(1000);
          }
          catch(InterruptedException e){
            // DO NOTHING 
          }
          
          Sample s = new Sample(deviceID, sessionID, millis, parsedValue);
          String jsonSample = s.serialize();
          AWSIotMessage message = new NonBlockingPublishListener(topic, qos, jsonSample);

          publish(message);
        }
      }
    }

    catch(FileNotFoundException e){
      System.out.println("could not find file");
      e.printStackTrace();
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
      System.out.println(System.currentTimeMillis() + ": publish failed for " + sample.serialize());
    }

    @Override
    public void onTimeout() {
      System.out.println(System.currentTimeMillis() + ": publish timeout for " + sample.serialize());
    }

  }
}


