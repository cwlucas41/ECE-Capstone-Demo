package edu.slu.iot.data;

import static org.junit.Assert.*;

import org.junit.Test;

import com.amazonaws.services.iot.client.AWSIotException;

import edu.slu.iot.IoTClient;

public class StateTest {

//	@Test
//	public void cons1() {
//		DaqState state = new DaqState() {
//			@Override
//			public void onStateChangeSucceded(DaqState state) {
//				System.out.println("test");
//			}
//		};
//		assertNotNull(state);
//	}
//	
//	@Test
//	public void cons2() {
//		DaqState state = new DaqState(new DaqState());
//		assertNotNull(state);
//	}
//	
//	@Test
//	public void cons3() {
//		DaqState state = new DaqState("test", 1.0, 2.0);
//		assertNotNull(state);
//	}
//	
//	@Test
//	public void serializeEmpty() {
//		DaqState state = new DaqState();
//		assertNotNull(GsonSerializer.serialize(state));
//	}
//	
//	static DaqState state;
//	@Test
//	public void serializeFull() {
//		
//		state = new DaqState();
//		assertNotNull(GsonSerializer.serialize(state));
//		
//		state = new DaqState() {
//			@Override
//			public void onStateChangeSucceded() {
//				// TODO Auto-generated method stub
//				System.out.println("state: " + state);
//			}
//		};
//		assertNotNull(state);
//	}

	public static void main(String args[]) throws InterruptedException, AWSIotException {
		
		DaqState state = new DaqState("test", 4.0, 5.0, new StateListener() {

			@Override
			public <T extends State> void onStateChangeSucceded(T state) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public <T extends State> void onStateChangeFailed(T state) {
				// TODO Auto-generated method stub
				
			}
		});
		
		System.out.println(GsonSerializer.serialize(new DaqState("lkj", 5.0, 4.0)));
		System.out.println(GsonSerializer.serialize(state));
    }
	
}
