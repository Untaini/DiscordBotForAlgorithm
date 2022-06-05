package me.Untaini.DiscordBotForAlgorithm;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class UserManager {
	static private Map<String, String> userInfo;
	private final static String userInfoFileName = "userInfo.json";
	
	static {
		userInfo = new HashMap<String, String>();
		JSONObject json = JSONManager.getJSON(userInfoFileName);
		for(Object userId : json.keySet()) 
			userInfo.put((String)userId, (String)json.get(userId));
		
		try {
			File jsonFile = new File(userInfoFileName);
			if(!jsonFile.exists()) jsonFile.createNewFile();
		}catch(Exception e) {}
	}
	
	
	public void finalize(){
		saveInfo();
	}
	
	public static void addId(String userId, String baekjoonId) {
		userInfo.put(userId, baekjoonId);
	}
	
	public static boolean checkRegister(String userId) {
		return userInfo.containsKey(userId);
	}
	
	public static boolean editId(String userId, String baekjoonId) {
		if(checkRegister(userId)) {
			addId(userId, baekjoonId);			
			return true;
		}
		else return false;
	}
	
	public static String getBeakjoonId(String userId) {
		return userInfo.get(userId);
	}
	
	public static void saveInfo(){
		JSONManager.saveJSON(userInfoFileName, new JSONObject(userInfo));
	}
	
	
}
