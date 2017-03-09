package edu.slu.iot.realdaq;

public class Sample implements Comparable<Sample> {
	
	private int sequenceNumber;
	private String deviceID;
	private String sessionID;
	private long timestamp;
	private float value;
	
	public static int nextSequenceNumber(int sequenceNumber) {
		if (0 <= sequenceNumber && sequenceNumber < Integer.MAX_VALUE) {
			return sequenceNumber + 1;
		} else {
			return 0;
		}
	}
	
	public Sample(Sample prevSample, String deviceID, String sessionID, long timestamp, float value) {
		
		if (prevSample == null) {
			sequenceNumber = 0;
		} else {
			sequenceNumber = nextSequenceNumber(prevSample.getSequenceNumber());
		}
		
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
		return ((Integer) sequenceNumber).compareTo(o.sequenceNumber);
	}

	@Override
	public String toString() {
		return "deviceID: " + deviceID + ", sessionID: " + sessionID + ", timestamp: " + timestamp + " value: " + value;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}
}
