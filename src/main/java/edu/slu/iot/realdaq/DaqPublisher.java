package edu.slu.iot.realdaq;


import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.google.gson.Gson;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import edu.slu.iot.IoTClient;
import edu.slu.iot.Publisher;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.Sample;

public class DaqPublisher extends Publisher {

  private String sessionID;
  private String deviceID = "defaultDeviceID";
  private static final Gson gson = new Gson();
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
  private final AdcReader reader = new AdcReader();
  public AtomicInteger c = new AtomicInteger(0);
  public DaqPublisher(IoTClient client, String topic, AWSIotQos qos, String sessionID) {
    super(client, topic, qos);
    this.sessionID = sessionID;
  }

  @Override
  public void run() {
    try {
      Process p = Runtime.getRuntime().exec("../../../../../c/ECE_Capstone_ADC/reader");
      BufferedReader in = 
        new BufferedReader(new InputStreamReader(p.getInputStream()));

      while (in.ready()) {
        //System.out.println(in.readLine());
        String s = in.readLine();
        String jsonSample = gson.toJson(s);
        AWSIotMessage message = new NonBlockingPublishListener(topic, qos, jsonSample);
        publish(message);
      }

      try{ 
        Thread.sleep(1000);
      }catch(InterruptedException e){

      }
    }catch(IOException e){

      //NOT HADNLING SHIT
    }
    System.err.println("FINAL: " + c.intValue());
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
