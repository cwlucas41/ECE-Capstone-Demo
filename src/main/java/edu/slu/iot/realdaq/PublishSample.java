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

public class PublishSample {

	private static final String adcReader = "src/main/c/ECE_Capstone/reader";
	private static Process adcReaderProcess = null;
	private static Thread publishThread = null;

	public static void main(String args[]) throws InterruptedException, AWSIotException, AWSIotTimeoutException {

		IoTClient client = new IoTClient("Certificate1/conf.txt");


		new StateSink<DaqState>(client, DaqState.class, new StateListener() {

			@Override
			public <T extends State> void onStateChangeSucceded(T state) {
				if (state instanceof DaqState) {

					DaqState daqState = (DaqState) state;
					System.err.println("state changed to: " + daqState.serialize());

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
					
					// CONFIGURE DIGITAL POTS HERE
					
					if (daqState.getFrequency() > 0) {
						// create new process
						try {
							// start new adc process with frequency
							adcReaderProcess = new ProcessBuilder(adcReader, daqState.getFrequency().toString()).start();
						} catch (IOException e) {
							e.printStackTrace();
						}					
	
						// start publishing for adc
						publishThread = new Thread(new DaqPublisher(client, daqState.getTopic(), AWSIotQos.QOS0, adcReaderProcess, daqState.getGain()));
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
				Thread.sleep(1000);
			} catch (Exception e) { 
				e.printStackTrace();
			}
		}
	}
}
