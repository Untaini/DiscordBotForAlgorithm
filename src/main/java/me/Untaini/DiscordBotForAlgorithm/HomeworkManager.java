package me.Untaini.DiscordBotForAlgorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class HomeworkManager {
	private static final String homeworkFileName = "homework.json";
	private static List<WeekHomework> homeworkData;
	
	static {
		homeworkData = new ArrayList<>();
		
		try {
			File jsonFile = new File(homeworkFileName);
			
			if(!jsonFile.exists()) 
				jsonFile.createNewFile();
			else {
				JSONObject json = JSONManager.getJSONFile(homeworkFileName);
				for(Object homework : (JSONArray)json.get("homeworks")) 
					homeworkData.add(new WeekHomework((JSONObject) homework));
			}
		}catch(Exception e) {}
	}
	
	public void finalize() {
		saveData();
	}
	
	public void saveData() {
		Map<String, List<WeekHomework>> homeworkMap = new HashMap<>();
		homeworkMap.put("homeworks", homeworkData);
		JSONManager.saveJSONFile(homeworkFileName, new JSONObject(homeworkMap));
	}
}
