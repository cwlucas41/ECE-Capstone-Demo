package edu.slu.iot.realdaq;


import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;

import java.util.Scanner;

import edu.slu.iot.IoTClient;
import edu.slu.iot.Publisher;
import edu.slu.iot.data.GsonSerializer;
import edu.slu.iot.data.Sample;

public class DaqPublisher extends Publisher {

	private String deviceID = "defaultDeviceID";
	private Scanner s;
	private double gain;

	public DaqPublisher(IoTClient client, String topic, AWSIotQos qos, Process p, double gain) {
		super(client, topic, qos);
		s = new Scanner(p.getInputStream());
		this.gain = gain;
	}

	@Override
	public void run() {	

		System.out.println("publisher running");
		
		int i = 0;

		while (s.hasNextLine()) {
			// get line
			String line = s.nextLine();
			// System.out.println(line);
			String[] fields = line.split(" ");

			// parse line
			float adcValue = (float) Integer.parseInt(fields[1]);
			float value = (float) (adcValue * 1.8f / 65536f / gain);
			
			String[] times = fields[0].split(":");
			long s = Long.parseLong(times[0]);
			long ns = Long.parseLong(times[1]);
			long timeStamp = (s << 32) + ns;
			
			// publish sample
			Sample sample = new Sample(deviceID, topic, timeStamp, value );
			AWSIotMessage message = new NonBlockingPublishListener(topic, qos, sample.serialize());
			publish(message);
			i++;
		}
		System.out.println("Published: " + i);
	}

	private class NonBlockingPublishListener extends AWSIotMessage {

		Sample sample;

		public NonBlockingPublishListener(String topic, AWSIotQos qos, String payload) {
			super(topic, qos, payload);
			sample = GsonSerializer.deserialize(getStringPayload(), Sample.class);
		}

		@Override
		public void onSuccess() {
			//System.out.println("daq: >>> " + sample.serialize());
		}

		@Override
		public void onFailure() {
			System.out.println(this.errorCode + " " + this.errorMessage);
			System.out.println(System.currentTimeMillis() + ": publish failed for " + sample.serialize());

		}

		@Override
		public void onTimeout() {
			System.out.println(System.currentTimeMillis() + ": publish timeout for " + sample.serialize());
		}

	}
}
