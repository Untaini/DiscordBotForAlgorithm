package me.Untaini.DiscordBotForAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class User {
	private String discordId;
	private String baekjoonId;
	private int solvedProblemCount;
	private Map<Integer, List<Boolean>> passedProblemPerWeek;
	
	User(String discordId, String baekjoonId){
		this.setDiscordId(discordId);
		this.setBaekjoonId(baekjoonId);
		this.setSolvedProblemCount(0);
		this.passedProblemPerWeek = new HashMap<>();
	}
	
	User(JSONObject json){
		this.setDiscordId((String)json.get("discordId"));
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
	}

	public String getDiscordId() {
		return discordId;
	}

	public void setDiscordId(String discordId) {
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
	
	
}
