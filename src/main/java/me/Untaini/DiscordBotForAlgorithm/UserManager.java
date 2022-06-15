package me.Untaini.DiscordBotForAlgorithm;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;

public class UserManager {
	private final static String userDataFileName = "userData.json";
	static private Map<Long, User> userData;
	
	static {
		userData = new HashMap<>();
		
		try {
			File jsonFile = new File(userDataFileName);
			if(!jsonFile.exists())
				jsonFile.createNewFile();
			else {
				JSONObject json = JSONManager.getJSONFile(userDataFileName);
				for(Object userId : json.keySet()) 
					userData.put((Long)userId, new User((JSONObject)json.get(userId)));
			}
		}catch(Exception e) {}
	}
	
	
	public void finalize(){
		saveData();
	}
	
	public void addId(long userId, String baekjoonId) {
		userData.put(userId, new User(userId, baekjoonId));
	}
	
	public boolean checkRegister(long userId) {
		return userData.containsKey(userId);
	}
	
	public boolean editId(long userId, String baekjoonId) {
		if(checkRegister(userId)) {
			addId(userId, baekjoonId);			
			return true;
		}
		else return false;
	}
	
	public User getUser(long userId) {
		return userData.get(userId);
	}
	
	public void saveData(){
		JSONManager.saveJSONFile(userDataFileName, new JSONObject(userData));
	}
	
	public void allUsersUpdateHomework() {
		for(User user : userData.values())
			user.updateHomework();
	}
	
	public void allUsersRemoveProblem(int week, int index) {
		for(User user : userData.values())
			user.removeProblem(week, index);
	}
	
	public void allUsersClearProblem(int week, int index) {
		for(User user : userData.values())
			user.clearProblem(week, index);
	}
	
	
}
