package edu.slu.iot.realdaq;
import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;

import edu.slu.iot.IoTClient;

public class PublishSample {
	
  private final String I2C = "python adjustableResistors.py";
  String args = " g 2.0"; //TODO placeholder
	public static void main(String args[]) throws InterruptedException, AWSIotException, AWSIotTimeoutException {
   

    ProcessBuilder pb = new ProcessBuilder(I2C + args)
    pb.inheritIO();
    pb.director(new File("bin"));
    pb.start();
    
    
    //init 
    //Runtime.getRuntime().exec(I2C);		
    IoTClient client = new IoTClient("/home/debian/ECE-Capstone-Demo/Certificate1/conf.txt");
    

    

		client.publish(new DaqPublisher(client, "RealDaq", AWSIotQos.QOS0, "DAQ"));

    }
}
