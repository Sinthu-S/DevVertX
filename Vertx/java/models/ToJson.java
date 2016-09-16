package models;

import com.google.gson.Gson;

import io.vertx.core.json.JsonObject;


/**
 * @author sinthu
 *
 */
public class ToJson {
	private static volatile Gson instance;
	
	public static Gson getGson(){
		if(instance == null){
			instance = new Gson();
		}
		return instance;
	}
	
	public static JsonObject getJson(Object obj){
		return new JsonObject(ToJson.getGson().toJson(obj));
	}
	
	
}
