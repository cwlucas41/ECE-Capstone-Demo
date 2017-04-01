package edu.slu.iot.data;

import java.util.Objects;

public class DaqState extends State {
			
	private String topic = null;
	private Double frequency = null;
	private Double gain = null;
	
	public DaqState() {
		this((StateListener) null);
	}
	
	public DaqState(StateListener listener) {
		super(listener);
	}
	
	public DaqState(DaqState reported) {
		this(reported, null);
	}
	
	public DaqState(DaqState reported, StateListener listener) {
		super(listener);
		update(reported);
	}
	
	public DaqState(String topic, Double frequency, Double gain) {
		this(topic, frequency, gain, null);
	}
	
	public DaqState(String topic, Double frequency, Double gain, StateListener listener) {
		super(listener);
		this.topic = topic;
		this.frequency = frequency;
		this.gain = gain;
		getListener().onStateChangeSucceded(this);
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
		getListener().onStateChangeSucceded(this);
	}

	public void setFrequency(Double frequency) {
		this.frequency = frequency;
		getListener().onStateChangeSucceded(this);
	}

	public void setGain(Double gain) {
		this.gain = gain;
		getListener().onStateChangeSucceded(this);
	}

	@Override
	public <T extends State> void update(T newState) {
		if (newState instanceof DaqState) {
			DaqState newDaqState = (DaqState) newState;
			if (newDaqState != null && !this.equals(newDaqState)) {
				
				if (newDaqState.topic != null) {
					topic = newDaqState.topic;
				}
				
				if (newDaqState.frequency != null) {
					frequency = newDaqState.frequency;
				}
				
				if (newDaqState.gain != null) {
					gain = newDaqState.gain;
				}
				getListener().onStateChangeSucceded(this);
			}
		} else {
			getListener().onStateChangeFailed(this);
		}
		
	}
	
	public void update(String topic, Double frequency, Double gain) {
		DaqState newState = new DaqState(topic, frequency, gain);
		update(newState);
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
}
