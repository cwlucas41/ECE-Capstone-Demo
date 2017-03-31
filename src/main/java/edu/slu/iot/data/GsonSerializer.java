package edu.slu.iot.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonSerializer {
	private static final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
	
	public static String serialize(Object object) {
		return gson.toJson(object);
	}
	
	public static <T> T deserialize(String json, Class<T> classOfT) {
		return gson.fromJson(json, classOfT);
	}
}
