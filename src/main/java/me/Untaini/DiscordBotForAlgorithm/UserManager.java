package me.Untaini.DiscordBotForAlgorithm;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;

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
				JsonObject json = JsonManager.getJsonFile(userDataFileName).getAsJsonObject();
				for(String userId : json.keySet()) 
					userData.put(Long.parseLong(userId), new User(json.get(userId).getAsJsonObject()));
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public void finalize(){
		saveData();
	}
	
	public void addId(long userId, String baekjoonId) {
		userData.put(userId, new User(userId, baekjoonId));
		saveData();
	}
	
	public boolean checkRegister(long userId) {
		return userData.containsKey(userId);
	}
	
	public boolean editId(long userId, String baekjoonId) {
		if(checkRegister(userId)) {
			userData.get(userId).setBaekjoonId(baekjoonId);	
			return true;
		}
		else return false;
	}
	
	public User getUser(long userId) {
		return userData.get(userId);
	}
	
	public static void saveData(){
		JsonManager.saveJsonFile(userDataFileName, userData);
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
	
	public Map<User, List<Problem>> allUsersCheckHomework() {
		Map<User, List<Problem>> solvedMap = new HashMap<>();
		for(User user : userData.values()) {
			List<Problem> solvedList = user.checkHomework();
			if(solvedList != null) 
				solvedMap.put(user, solvedList);
		}
		return solvedMap;
	}
	
}
