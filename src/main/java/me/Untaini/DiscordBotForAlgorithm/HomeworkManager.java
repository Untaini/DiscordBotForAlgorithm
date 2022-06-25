package me.Untaini.DiscordBotForAlgorithm;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class HomeworkManager {
	private static final String homeworkFileName = "homeworkData.json";
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
				JsonObject json = JsonManager.getJsonFile(homeworkFileName).getAsJsonObject();
				for(JsonElement homework : json.get("homeworkData").getAsJsonArray()) {
					homeworkData.add(new WeekHomework(homework.getAsJsonObject()));
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public void finalize() {
		saveData();
	}
	
	public static void saveData() {
		Map<String, List<WeekHomework>> jsonMap = new HashMap<>();
		jsonMap.put("homeworkData", homeworkData);
		JsonManager.saveJsonFile(homeworkFileName, jsonMap);
	}
	
	public WeekHomework getHomework(int week) {
		return homeworkData.get(week-1);
	}
}
