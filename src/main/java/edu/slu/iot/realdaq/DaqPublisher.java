package edu.slu.iot.realdaq;


import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import java.util.Scanner;

import edu.slu.iot.IoTClient;
import edu.slu.iot.Publisher;
import edu.slu.iot.data.Batch;
import edu.slu.iot.data.DaqState;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.Sample;

public class DaqPublisher extends Publisher {

	private Scanner s;
	private DaqState targetState;

	public DaqPublisher(IoTClient client, AWSIotQos qos, Process p, DaqState targetState) {
		super(client, targetState.getTopic(), qos);
		s = new Scanner(p.getInputStream());
		this.targetState = targetState;
	}

	@Override
	public void run() {	

		System.out.println("publisher running");
				
		Batch batch = new Batch(targetState.getTopic(), targetState.getFrequency());

		while (s.hasNextLine()) {
			// get line
			String line = s.nextLine();
			// System.out.println(line);
			String[] fields = line.split(" ");

			// parse line
			float adcValue = (float) Integer.parseInt(fields[1]);
			float value = (float) (adcValue * 1.8f / 65536f / targetState.getGain());
			
			String[] times = fields[0].split(":");
			long s = Long.parseLong(times[0]);
			long ns = Long.parseLong(times[1]);
			long timeStamp = (s << 32) + ns;
			
			// publish sample
			Sample sample = new Sample(timeStamp, value);
			batch.add(sample);
			
			if (batch.size() > 50) {
				AWSIotMessage message = new NonBlockingPublishListener(topic, qos, batch.serialize());
				publish(message);
				batch = new Batch(targetState.getTopic(), targetState.getFrequency());
			}
		}
	}

	private class NonBlockingPublishListener extends AWSIotMessage {

		public NonBlockingPublishListener(String topic, AWSIotQos qos, String payload) {
			super(topic, qos, payload);
		}

		@Override
		public void onFailure() {
			System.out.println(this.errorCode + " " + this.errorMessage);
		}

		@Override
		public void onTimeout() {
			System.out.println(this.errorCode + " " + this.errorMessage);
		}

	}
}
