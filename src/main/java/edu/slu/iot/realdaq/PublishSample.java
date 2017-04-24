package edu.slu.iot.realdaq;
import java.io.IOException;
import java.lang.ProcessBuilder;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTimeoutException;

import edu.slu.iot.IoTClient;
import edu.slu.iot.data.DaqState;
import edu.slu.iot.data.State;
import edu.slu.iot.data.StateListener;
import edu.slu.iot.data.StateSink;
import edu.slu.iot.data.StateSource;

public class PublishSample {

	private static final String adcReader = "src/main/c/ECE_Capstone/reader";
	private static final String i2cController = "src/main/java/edu/slu/iot/realdaq/adjustableResistors.py";
	private static final String gainToken = "g";
	private static final String freqToken = "f";

	private static Process adcReaderProcess = null;
	private static Process i2cControllerProcess = null;
	private static Thread publishThread = null;
	private static DaqState actualState;

	public static void main(String args[]) throws InterruptedException, AWSIotException, AWSIotTimeoutException {

		IoTClient client = new IoTClient("Certificate1/conf.txt");

		actualState = new StateSource<DaqState>(client, client.getActualThingName(), DaqState.class).getState();

		new StateSink<DaqState>(client, client.getTargetThingName(), DaqState.class, new StateListener() {

			@Override
			public <T extends State> void onStateChangeSucceded(T state) {
				if (state instanceof DaqState) {

					DaqState targetState = (DaqState) state;
					System.err.println("state changed to: " + targetState.serialize());

					// destroy an existing process if it exists
					if (adcReaderProcess != null) {
						adcReaderProcess.destroy();

						try {
							// wait for destruction
							adcReaderProcess.waitFor();					

							// wait for publishing to stop
							publishThread.join();

							System.err.println("publishing stopped");
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

					try{
						//adjust variable resistors 
						System.out.println("Changing Digital pots");
						i2cControllerProcess = new ProcessBuilder(i2cController,freqToken ,targetState.getFrequency().toString()).start();
						i2cControllerProcess.waitFor();
						
						System.out.println("Freq set. Updating Gain");
						i2cControllerProcess = new ProcessBuilder(i2cController,gainToken ,targetState.getGain().toString()).start();
						i2cControllerProcess.waitFor();
						
						System.out.println("Gain set. ");
						System.out.println("Dpot update complete");
					}catch(IOException | InterruptedException e ){
						e.printStackTrace();
					}
					
					// TODO: set these with feedback from python code
					double actualGain = targetState.getGain();
					double actualFreq = targetState.getFrequency();

					actualState.update(targetState.getTopic(), actualFreq, actualGain);
					
					if (targetState.getFrequency() > 0) {
						// create new process
						try {
							// start new adc process with frequency
							adcReaderProcess= new ProcessBuilder(adcReader, targetState.getFrequency().toString()).start();
						} catch (IOException e) {
							e.printStackTrace();
						}					

						// start publishing for adc
						publishThread = new Thread(new DaqPublisher(client, targetState.getTopic(), AWSIotQos.QOS0, adcReaderProcess, targetState.getGain()));
						publishThread.start();
						
						System.err.println("publishing started");
					} else {
						System.err.println("publishing stopped");
					}
				}
			}

		});  

		while (true) {
			try {
				Thread.sleep(100);
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
	}
}
