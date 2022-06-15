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
	public static final int totalWeek = 8;
	
	static {
		homeworkData = new ArrayList<>();
		
		try {
			File jsonFile = new File(homeworkFileName);
			
			if(!jsonFile.exists()) {
				jsonFile.createNewFile();
				for(int cnt=0; cnt<totalWeek; ++cnt)
					homeworkData.add(new WeekHomework());
			}
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
		Map<String, List<JSONObject>> homeworkMap = new HashMap<>();
		List<JSONObject> homeworkList = new ArrayList<>();
		
		for(WeekHomework wh : homeworkData)
			homeworkList.add(wh.getJSONObject());
		homeworkMap.put("homeworks", homeworkList);
		
		JSONManager.saveJSONFile(homeworkFileName, new JSONObject(homeworkMap));
	}
	
	public WeekHomework getHomework(int week) {
		return homeworkData.get(week-1);
	}
}
