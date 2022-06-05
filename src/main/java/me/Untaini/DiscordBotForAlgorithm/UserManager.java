package me.Untaini.DiscordBotForAlgorithm;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class UserManager {
	static private Map<String, String> userInfo;
	
	static {
		userInfo = new HashMap<String, String>();
		
		try {
			File jsonFile = new File("UserInfo.json");
			if(jsonFile.exists()) {
				JSONObject json = (JSONObject)new JSONParser().parse(new BufferedReader(new FileReader("UserInfo.json")));
				for(Object userId : json.keySet()) 
					userInfo.put((String)userId, (String)json.get(userId));
			}
			else jsonFile.createNewFile();
		}catch(Exception e) {}
	}
	
	
	public void finalize(){
		saveJSONFile();
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
	
	public static void saveJSONFile(){
		try {
			FileWriter fw = new FileWriter(new File("UserInfo.json"));
			fw.write(new JSONObject(userInfo).toJSONString());
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	
}
