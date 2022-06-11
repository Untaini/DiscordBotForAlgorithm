package me.Untaini.DiscordBotForAlgorithm;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class UserManager {
	private final static String userDataFileName = "userData.json";
	static private Map<String, String> userData;
	
	static {
		userData = new HashMap<>();
		
		try {
			File jsonFile = new File(userDataFileName);
			if(!jsonFile.exists())
				jsonFile.createNewFile();
			else {
				JSONObject json = JSONManager.getJSONFile(userDataFileName);
				for(Object userId : json.keySet()) 
					userData.put((String)userId, (String)json.get(userId));
			}
		}catch(Exception e) {}
	}
	
	
	public void finalize(){
		saveData();
	}
	
	public void addId(String userId, String baekjoonId) {
		userData.put(userId, baekjoonId);
	}
	
	public boolean checkRegister(String userId) {
		return userData.containsKey(userId);
	}
	
	public boolean editId(String userId, String baekjoonId) {
		if(checkRegister(userId)) {
			addId(userId, baekjoonId);			
			return true;
		}
		else return false;
	}
	
	public String getBeakjoonId(String userId) {
		return userData.get(userId);
	}
	
	public void saveData(){
		JSONManager.saveJSONFile(userDataFileName, new JSONObject(userData));
	}
	
	
}
