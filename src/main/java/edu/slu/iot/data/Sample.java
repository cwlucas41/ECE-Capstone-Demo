package edu.slu.iot.data;
import com.amazonaws.services.dynamodbv2.document.Item;

public class Sample implements Comparable<Sample> {
	private String sessionID;
	private long timestamp;
	private float value;
	
	public Sample(String deviceID, String sessionID, long timestamp, float value) {
		this.sessionID = sessionID;
		this.timestamp = timestamp;
		this.value = value;
	}
	
	// construct from dynamoDB Item object; used for database query conversion
	public Sample(Item item) {
		this.sessionID = item.getString("sessionID");
		this.timestamp = item.getLong("timestamp");
		this.value = item.getFloat("value");
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
		return "sessionID: " + sessionID + ", timestamp: " + timestamp + " value: " + value;
	}
}
