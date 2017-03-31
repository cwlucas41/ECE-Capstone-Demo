package edu.slu.iot.data;

import static org.junit.Assert.*;

import org.junit.Test;

public class StateTest {

	@Test
	public void cons1() {
		DaqState state = new DaqState();
		assertNotNull(state);
	}
	
	@Test
	public void cons2() {
		DaqState state = new DaqState(new TestListener());
		assertNotNull(state);
	}
	
	@Test
	public void cons3() {
		DaqState state = new DaqState(new DaqState());
		assertNotNull(state);
	}
	
	@Test
	public void cons4() {
		DaqState state = new DaqState(new DaqState(), new TestListener());
		assertNotNull(state);
	}
	
	@Test
	public void cons5() {
		DaqState state = new DaqState("test", 1.0, 2.0);
		assertNotNull(state);
	}
	
	@Test
	public void cons6() {
		DaqState state = new DaqState("test", 1.0, 2.0, new TestListener());
		assertNotNull(state);
	}
	
	public static class TestListener implements StateListener {
		@Override
		public void onStateChange(DaqState state) {
		}
	}

}
