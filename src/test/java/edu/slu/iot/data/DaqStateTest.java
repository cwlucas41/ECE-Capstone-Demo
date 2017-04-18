package edu.slu.iot.data;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class DaqStateTest {
	
	int c;
	StateListener counterListener = new StateListener() {
		@Override
		public <T extends State> void onStateChangeSucceded(T state) {
			c++;
		}
	};
	
	@Before
	public void setup() {
		c = 0;
	}
	
	@Test
	public void listenerIsCalledOnFieldChanges() {
		DaqState state = new DaqState("test", 1.0, 2.0, counterListener);
		state.setTopic("a");
		assertTrue("not called on topic update", c == 1);
		
		state.setFrequency(50.0);
		assertTrue("not called on frequency update", c == 2);
		
		state.setGain(50.0);
		assertTrue("not called on gain update", c == 3);
	}
	
	@Test 
	public void listenerIsCalledOnUpdateFromFields() {
		DaqState state = new DaqState("test", 1.0, 2.0, counterListener);
		state.update("a", 0.0, 10.0);
		assertTrue(c == 1);
	}
	
	@Test 
	public void listenerIsCalledOnUpdateFromState() {
		DaqState state = new DaqState("test", 1.0, 2.0, counterListener);
		state.update(new DaqState("a", 0.0, 10.0));
		assertTrue(c == 1);
	}
	
	@Test
	public void updateFromFields() {
		DaqState s1 = new DaqState("test", 1.0, 2.0);
		s1.update("a", 0.0, 10.0);
		assertEquals(s1, new DaqState("a", 0.0, 10.0));
	}
	
	@Test
	public void updateFromState() {
		DaqState s1 = new DaqState("test", 1.0, 2.0);
		DaqState s2 = new DaqState("test", 3.0, 2.0);
		s1.update(s2);
		assertEquals(s1, s2);
	}
	
	@Test
	public void equalityTestFalse() {
		DaqState s1 = new DaqState("test", 1.0, 2.0);
		DaqState s2 = new DaqState("test", 1.0, 2.0, new StateListener() {});
		assertEquals(s1, s2);
	}
	
	@Test
	public void equalityTestTrue() {
		DaqState s1 = new DaqState("test", 1.0, 2.0);
		DaqState s2 = new DaqState("test", 3.0, 2.0);
		assertNotEquals(s1, s2);
	}

	@Test
	public void cons1() {
		DaqState state = new DaqState();
		assertNotNull(state);
	}
	
	@Test
	public void cons2() {
		DaqState state = new DaqState(new StateListener() {});
		assertNotNull(state);
	}
	
	@Test
	public void cons3() {
		DaqState state = new DaqState(new DaqState());
		assertNotNull(state);
	}
	
	@Test
	public void cons4() {
		DaqState state = new DaqState(new DaqState(), new StateListener() {});
		assertNotNull(state);
	}
	
	@Test
	public void cons5() {
		DaqState state = new DaqState("test", 1.0, 2.0);
		assertNotNull(state);
	}
	
	@Test
	public void cons6() {
		DaqState state = new DaqState("test", 1.0, 2.0, new StateListener() {});
		assertNotNull(state);
	}
}
