package edu.slu.iot.realdaq;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class SampleTest {

	@Test
	public void testSorting() {
		Sample sample = null;
		List<Sample> l = new ArrayList<Sample>();
		for (int i = 0; i < 100; i++) {
			sample = new Sample(sample, "", "", System.currentTimeMillis(), 0);
			l.add(sample);
		}
		
		Collections.shuffle(l);
		Collections.sort(l);
		
		int i = 0;
		for (Sample s : l) {
			assertEquals(s.getSequenceNumber(), i);
			i++;
		}
	}

}
