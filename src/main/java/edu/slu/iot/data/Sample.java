package edu.slu.iot.data;

public class Sample implements Comparable<Sample> {
	//TODO: sequence number
	private String deviceID;
	private String sessionID;
	private long timestamp;
	private float value;
	
	public Sample(String deviceID, String sessionID, long timestamp, float value) {
		this.deviceID = deviceID;
		this.sessionID = sessionID;
		this.timestamp = timestamp;
		this.value = value;
	}
	
	public String getDeviceID() {
		return deviceID;
	}
	public String getSessionID() {
		return sessionID;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public float getValue() {
		return value;
	}

	@Override
	public int compareTo(Sample o) {
		// sorts by increasing timestamp
		return ((Long) timestamp).compareTo(o.timestamp);
	}

	@Override
	public String toString() {
		return "deviceID: " + deviceID + ", sessionID: " + sessionID + ", timestamp: " + timestamp + " value: " + value;
	}
}
