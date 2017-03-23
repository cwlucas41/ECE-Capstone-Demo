package edu.slu.iot.realdaq;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class SampleTest {

	@Test
	public void testSorting() {
		List<Sample> l = new ArrayList<Sample>();
		for (int i = 0; i < 100; i++) {
			l.add(new Sample("", "", i, 0));
		}
		
		Collections.shuffle(l);
		Collections.sort(l);
		
		int i = 0;
		for (Sample s : l) {
			assertEquals(s.getTimestamp(), i);
			i++;
		}
	}

}
