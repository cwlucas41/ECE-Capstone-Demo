package edu.slu.iot.data;

import java.util.Objects;

import com.google.gson.annotations.Expose;

public class DaqState {
	
	StateListener listener;
		
	@Expose private String topic = null;
	@Expose private Double frequency = null;
	@Expose private Double gain = null;
	
	public DaqState() {
	}
	
	public DaqState (StateListener listener) {
		this.listener = listener;
	}
	
	public DaqState(DaqState reported) {
		this(reported, null);
	}
	
	public DaqState(DaqState reported, StateListener listener) {
		update(reported);
		
		this.listener = listener;
		callListenerIfComplete();
	}
	
	public DaqState(String topic, Double frequency, Double gain) {
		this(topic, frequency, gain, null);
	}
	
	public DaqState(String topic, Double frequency, Double gain, StateListener listener) {
		this.topic = topic;
		this.frequency = frequency;
		this.gain = gain;
		
		this.listener = listener;
		callListenerIfComplete();
	}

	public String getTopic() {
		return topic;
	}

	public Double getFrequency() {
		return frequency;
	}

	public Double getGain() {
		return gain;
	}

	public void setTopic(String topic) {
		this.topic = topic;
		callListenerIfComplete();
	}

	public void setFrequency(Double frequency) {
		this.frequency = frequency;
		callListenerIfComplete();
	}

	public void setGain(Double gain) {
		this.gain = gain;
		callListenerIfComplete();
	}

	public void update(DaqState newState) {
		if (newState != null && !this.equals(newState)) {
			if (newState.topic != null) {
				topic = newState.topic;
			}
			
			if (newState.frequency != null) {
				frequency = newState.frequency;
			}
			
			if (newState.gain != null) {
				gain = newState.gain;
			}
			
			callListenerIfComplete();
		}
	}
	
	public void update(String topic, Double frequency, Double gain) {
		DaqState newState = new DaqState(topic, frequency, gain);
		update(newState);
	}
	
	private void callListenerIfComplete() {
		if (listener != null) {
			listener.onStateChange(this);
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) { return true; }
		if (obj == null) { return false; }
		if (getClass() != obj.getClass()) { return false; }
		DaqState s = (DaqState) obj;
		return Objects.equals(topic, s.topic) && 
				Objects.equals(frequency, s.frequency) &&
				Objects.equals(gain, s.gain);
	}
	
	@Override
	public String toString() {
		return GsonSerializer.serialize(this);
	}
}
