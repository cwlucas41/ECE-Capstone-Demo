package edu.slu.iot.data;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import edu.slu.iot.data.Sample;

public class SampleTest {

	@Test
	public void testSorting() {
		List<Sample> l = new ArrayList<Sample>();
		for (int i = 0; i < 100; i++) {
			l.add(new Sample(i, 0));
		}
		
		Collections.shuffle(l);
		Collections.sort(l);
		
		int i = 0;
		for (Sample s : l) {
			assertEquals(s.getTimestamp(), i);
			i++;
		}
	}
	
	@Test
	public void batchSerialization() {
		List<Sample> l = new ArrayList<Sample>();
		for (int i = 0; i < 100; i++) {
			l.add(new Sample(i, 0));
		}
		String serialized = GsonSerializer.serialize(l);		
		List<Sample> l2 = GsonSerializer.deserialize(serialized, new TypeToken<List<Sample>>(){}.getType());
		
		for (int i = 0; i < 100; i++) {
			assertEquals(l.get(i).getTimestamp(), l2.get(i).getTimestamp());
		} 
	}

}
