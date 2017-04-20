package edu.slu.iot.data;

public interface StateListener {
	// default no-ops
	public default <T extends State> void onStateChangeSucceded(T state) {};
	public default <T extends State> void onStateChangeFailed(T state) {};
}
