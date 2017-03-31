package edu.slu.iot.data;

import com.google.gson.annotations.Expose;

public class Sample implements Comparable<Sample> {
	
	@Expose private String deviceID;
	@Expose private String sessionID;
	@Expose private long timestamp;
	@Expose private float value;
	
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
		return GsonSerializer.serialize(this);
	}
}
