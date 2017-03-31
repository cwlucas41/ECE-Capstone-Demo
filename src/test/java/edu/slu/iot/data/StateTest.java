package edu.slu.iot.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class StateTest {

	@Test
	public void test() {
		State state = new State("test", 4.0, 3.0);
		String json = GsonSerializer.serialize(state);
		assertTrue(!json.contains("isInitialized"));
	}

}
