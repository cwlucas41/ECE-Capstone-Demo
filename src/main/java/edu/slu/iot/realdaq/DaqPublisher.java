package edu.slu.iot.realdaq;


import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
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
  private static final String adcReader = "/home/debian/ECE-Capstone-Demo/src/main/c/ECE_Capstone/reader";
  public DaqPublisher(IoTClient client, String topic, AWSIotQos qos, String sessionID) {
    super(client, topic, qos);
    this.sessionID = sessionID;
  }

  @Override
  public void run() {
    try {
      //System.out.println("About to Create Process");
      Process p = Runtime.getRuntime().exec(adcReader);
      BufferedReader in = 
        new BufferedReader(new InputStreamReader(p.getInputStream()));

      //System.out.println("Procss Created. Getting input");
      while (in.ready()) {
        String[] line = in.readLine().split(" ");
        //System.out.println(String.join(" ",line));
        float in_volts = (float) Integer.parseInt(line[1]);
        Sample s = new Sample(deviceID,sessionID,Long.parseLong(line[0].split(":")[1]), in_volts );
        AWSIotMessage message = new NonBlockingPublishListener(topic, qos, s.serialize());
        publish(message);
      }

      try{ 
        Thread.sleep(1000);
      }catch(InterruptedException e){
        e.printStackTrace();
      }
    }catch(IOException e){
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

      System.out.println(this.errorCode + " " + this.errorMessage);
      System.out.println(System.currentTimeMillis() + ": publish failed for " + sample);

    }

    @Override
    public void onTimeout() {
      System.out.println(System.currentTimeMillis() + ": publish timeout for " + sample.serialize());
    }

  }
}
