package edu.slu.iot.data;

public interface StateListener {
	
	// default used to provide default no-op
	public default void onTopicChange(String topic) {};
	public default void onFrequencyChange(double frequency) {};
	public default void onGainChange(double gain) {}
}
