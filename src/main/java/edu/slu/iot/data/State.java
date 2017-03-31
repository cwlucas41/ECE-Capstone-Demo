package edu.slu.iot.data;

import com.google.gson.annotations.Expose;

public class State {
	
	private StateListener listener = new StateListener() {};
	private boolean isInitialized = false;
	
	@Expose private String topic = null;
	@Expose private Double frequency = null;
	@Expose private Double gain = null;
	
	public State() {
	}
	
	public State(State reported) {
		update(reported);
	}
	
	public State(String topic, Double frequency, Double gain) {
		setTopic(topic);
		setFrequency(frequency);
		setGain(gain);
		isInitialized = true;
	}

	private void setTopic(String topic) {
		this.topic = topic;
		listener.onTopicChange(topic);
	}

	private void setFrequency(Double frequency) {
		this.frequency = frequency;
		listener.onFrequencyChange(frequency);
	}

	private void setGain(Double gain) {
		this.gain = gain;
		listener.onGainChange(gain);
	}

	public void update(State newState) {
		if (newState.topic != null) {
			setTopic(newState.topic);
		}
		
		if (newState.frequency != null) {
			setFrequency(newState.frequency);
		}
		
		if (newState.gain != null) {
			setGain(newState.gain);
		}
		isInitialized = true;
	}

	public boolean isInitialized() {
		return isInitialized;
	}
	
	@Override
	public String toString() {
		return "topic: " + topic + ", frequency: " + frequency + ", gain: " + gain;
	}
}
