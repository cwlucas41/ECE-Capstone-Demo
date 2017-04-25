package edu.slu.iot.data;

import java.lang.reflect.Type;

import com.google.gson.Gson;

public class GsonSerializer {
	private static final Gson gson = new Gson();
	
	public static String serialize(Object object) {
		return gson.toJson(object);
	}
	
	public static <T> T deserialize(String json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}
	
	public static <T> T deserialize(String json, Type typeOfT) {
		return gson.fromJson(json, typeOfT);
	}
}
