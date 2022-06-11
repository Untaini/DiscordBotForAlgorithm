package me.Untaini.DiscordBotForAlgorithm;

import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class WeekHomework {
	private boolean active;
	private List<Integer> problemId;
	private List<String> problemName;
	
	public WeekHomework() {
		this.problemId = new ArrayList<>();
		this.problemName = new ArrayList<>();
	}
	
	public WeekHomework(JSONObject json) {
		this.problemId = new ArrayList<>();
		this.problemName = new ArrayList<>();
		
		this.active = (boolean) json.get("isActive");
		for(Object problem : (JSONArray)json.get("problems")) {
			problemId.add((Integer) (((JSONObject)problem).get("problemId")));
			problemName.add((String) (((JSONObject)problem).get("problemName")));
		}
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}

}
