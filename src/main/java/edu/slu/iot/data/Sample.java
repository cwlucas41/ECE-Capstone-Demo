package edu.slu.iot.data;
import com.amazonaws.services.dynamodbv2.document.Item;

public class Sample implements Comparable<Sample> {
	private long t;
	private float v;
	
	public Sample(long timestamp, float value) {
		this.t = timestamp;
		this.v = value;
	}
	
	// construct from dynamoDB Item object; used for database query conversion
	public Sample(Item item) {
		this.t = item.getLong("timestamp");
		this.v = item.getFloat("value");
	}

	public long getTimestamp() {
		return t;
	}
	public float getValue() {
		return v;
	}

	public void setTimestamp(long t) {
		this.t= t;
	}
	
	public void setValue(float v) {
		this.v = v;
	}

	@Override
	public int compareTo(Sample o) {
		// sorts by increasing timestamp
		return ((Long) t).compareTo(o.t);
	}
	
	public String serialize() {
		return GsonSerializer.serialize(this);
	}

	@Override
	public String toString() {
		return "timestamp: " + t + " value: " + v;
	}
}
