package me.Untaini.DiscordBotForAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class User {
	private long discordId;
	private String baekjoonId;
	private int solvedProblemCount;
	private Map<Integer, List<Boolean>> passedProblemPerWeek;
	
	User(long discordId, String baekjoonId){
		this.setDiscordId(discordId);
		this.setBaekjoonId(baekjoonId);
		this.setSolvedProblemCount(0);
		this.passedProblemPerWeek = new HashMap<>();
		updateHomework();
	}
	
	User(JSONObject json){
		this.setDiscordId((Long)json.get("discordId"));
		this.setBaekjoonId((String)json.get("baekjoonId"));
		this.setSolvedProblemCount((Integer)json.get("solvedProblemCount"));
		this.passedProblemPerWeek = new HashMap<Integer, List<Boolean>>();
		
		json = (JSONObject) json.get("passedProblemPerWeek");
		for(Object week : json.keySet()) {
			List<Boolean> tempList = new ArrayList<>();
			JSONArray jsonArr = (JSONArray)json.get(week);
			for(Object isPassed : jsonArr)
				tempList.add((Boolean)isPassed);
			this.passedProblemPerWeek.put((Integer)week, tempList);
		}
		
		updateHomework();
	}

	public long getDiscordId() {
		return discordId;
	}

	public void setDiscordId(long discordId) {
		this.discordId = discordId;
	}

	public String getBaekjoonId() {
		return baekjoonId;
	}

	public void setBaekjoonId(String baekjoonId) {
		this.baekjoonId = baekjoonId;
	}

	public int getSolvedProblemCount() {
		return solvedProblemCount;
	}

	public void setSolvedProblemCount(int solvedProblemCount) {
		this.solvedProblemCount = solvedProblemCount;
	}
	
	public void updateHomework() {
		HomeworkManager homeworkManager = new HomeworkManager();
		
		while(passedProblemPerWeek.size() < HomeworkManager.totalWeek)
			passedProblemPerWeek.put(passedProblemPerWeek.size()+1, new ArrayList<>());
		
		for(int week=1; week<=HomeworkManager.totalWeek; ++week)
			while(passedProblemPerWeek.get(week).size() < homeworkManager.getHomework(week).getCount())
				passedProblemPerWeek.get(week).add(false);
	}
	
	public void removeProblem(int week, int index) {
		passedProblemPerWeek.get(week).remove(index);
	}
	
	public void clearProblem(int week, int index) {
		passedProblemPerWeek.get(week).set(index, false);
	}
	
	public JSONObject getJSONObject() {
		Map<String, Object> json = new HashMap<>();
		json.put("discordId", discordId);
		json.put("baekjoonId", baekjoonId);
		json.put("solvedProblemCount", solvedProblemCount);
		json.put("passedProblemPerWeek", new JSONObject(passedProblemPerWeek));
		return new JSONObject(json);
	}
	
	
}
