package edu.slu.iot.data;

public class State {
	private String topic;
	private double frequency;
	private double gain;
	
	public State(String topic, double frequency, double gain) {
		this.topic = topic;
		this.frequency = frequency;
		this.gain = gain;
	}

	public String getTopic() {
		return topic;
	}

	public double getFrequency() {
		return frequency;
	}

	public double getGain() {
		return gain;
	}
}
