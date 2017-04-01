package edu.slu.iot.data;

import com.google.gson.Gson;

public class GsonSerializer {
	public static final Gson gson = new Gson();
	
	public static String serialize(Object object) {
		return gson.toJson(object);
	}
	
	public static <T> T deserialize(String json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}
}
