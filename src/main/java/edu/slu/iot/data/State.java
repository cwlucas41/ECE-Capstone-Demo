package edu.slu.iot.data;

public abstract class State {
	
	private StateListener listener;
	
	public State(StateListener listener) {
		if (listener != null) {
			this.listener = listener;
		} else {
			this.listener = new StateListener() {};
		}
	}
	
	public StateListener getListener() {
		return listener;
	}
	
	public abstract <T extends State> void update(T newState);
	
	public String serialize() {
		return GsonSerializer.serialize(this);
	}
}
