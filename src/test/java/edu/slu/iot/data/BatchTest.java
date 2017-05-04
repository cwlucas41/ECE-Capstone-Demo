package edu.slu.iot.data;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class BatchTest {

	@Test
	public void batchSortSet() {
		Batch b = new Batch("", 0, 0);
		b.add(new Sample(5,5));
		b.add(new Sample(6,6));
		assertEquals(b.s, 5);
	}
	
	@Test
	public void serializeTest() {
		Batch b1 = new Batch("", 0, 0);
		for (int i = 0; i < 100; i++) {
			b1.add(new Sample(i, 0));
		}
		String serialized = b1.serialize();
		System.out.println(serialized);
		Batch b2 = GsonSerializer.deserialize(serialized, Batch.class);
		
		List<Sample> s1 = b1.getSampleList();
		List<Sample> s2 = b2.getSampleList();
		
		for (int i = 0; i < 100; i++) {
			assertEquals(s1.get(i).getTimestamp(), i);
			assertEquals(s2.get(i).getTimestamp(), i);
		}
	}

}
