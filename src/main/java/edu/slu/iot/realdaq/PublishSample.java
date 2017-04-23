package edu.slu.iot.realdaq;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;

import edu.slu.iot.IoTClient;

public class PublishSample {
	
  private class Gain{
    
    public float gain;
    public int hexcodeR1;
    public int hexcodeR2;

    Gain(float gain,int hexcodeR1,int hexcodeR2){
      this.gain = gain;
      this.hexcodeR1 = hexcodeR1;
      this.hexcodeR2 = hexcodeR2;
    
    }
  }

  private class Frequency{
    public float sampleFreq;
    public int hexcode;

    Frequency(float sampleFreq, int hexcode){
      this.sampleFreq = sampleFreq;
      this.hexcode = hexcode;
    }
  }


	public static void main(String args[]) throws InterruptedException, AWSIotException, AWSIotTimeoutException {
    
    //init 
    Runtime.getRuntime.exec("python adjustableResistors.py");		
    IoTClient client = new IoTClient("/home/debian/ECE-Capstone-Demo/Certificate1/conf.txt");
    

    

		client.publish(new DaqPublisher(client, "RealDaq", AWSIotQos.QOS0, "DAQ"));

    }
}
