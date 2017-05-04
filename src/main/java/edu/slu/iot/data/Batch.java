package edu.slu.iot.data;

import java.util.LinkedList;
import java.util.List;

public class Batch implements Comparable<Batch> {

	private List<Sample> p = new LinkedList<Sample>();
	protected long s = -1;
	
	@SuppressWarnings("unused")
	private String t;
	@SuppressWarnings("unused")
	private double f;
	
	public Batch(String topicName, double sampledFrequency) {
		this.f = sampledFrequency;
		this.t = topicName;
	}
	
	public long getTimeStamp() {
		return s;
	}
	
	public void add(Sample sample) {
		p.add(sample);
		
		if (s == -1) {
			s = sample.getTimestamp();
		}
	}
	
	public int size() {
		return p.size();
	}
	
	public List<Sample> getSampleList() {
		return p;
	}
	
	public String serialize() {
		return GsonSerializer.serialize(this);
	}

	@Override
	public int compareTo(Batch o) {
		return ((Long) s).compareTo(o.s);
	}
}
