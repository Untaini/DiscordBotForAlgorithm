package me.Untaini.DiscordBotForAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


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
	
	User(JsonObject json){
		this.setDiscordId(json.get("discordId").getAsLong());
		this.setBaekjoonId(json.get("baekjoonId").getAsString());
		this.setSolvedProblemCount(json.get("solvedProblemCount").getAsInt());
		this.passedProblemPerWeek = new HashMap<>();
		
		json = json.get("passedProblemPerWeek").getAsJsonObject();
		for(String week : json.keySet()) {
			List<Boolean> tempList = new ArrayList<>();
			for(JsonElement isPassed : json.get(week).getAsJsonArray())
				tempList.add(isPassed.getAsBoolean());
			this.passedProblemPerWeek.put(Integer.parseInt(week), tempList);
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
		
		while(this.passedProblemPerWeek.size() < HomeworkManager.totalWeek)
			this.passedProblemPerWeek.put(this.passedProblemPerWeek.size()+1, new ArrayList<>());
		
		for(int week=1; week<=HomeworkManager.totalWeek; ++week)
			while(this.passedProblemPerWeek.get(week).size() < homeworkManager.getHomework(week).getCount())
				this.passedProblemPerWeek.get(week).add(false);
		
		solvedProblemCount = 0;
	}
	
	public void removeProblem(int week, int index) {
		passedProblemPerWeek.get(week).remove(index);
	}
	
	public void clearProblem(int week, int index) {
		passedProblemPerWeek.get(week).set(index, false);
	}
	
	public List<Boolean> getProblemStatus(int week){
		return passedProblemPerWeek.get(week);
	}
	
	public List<Problem> checkHomework() {
		SolvedacAPIManager api = new SolvedacAPIManager();
		int totalSolvedProblemCount = 0;
		try {
			if(this.solvedProblemCount < (totalSolvedProblemCount = api.getSolvedProblems(this))) {
				List<Problem> newSolvedProblemList = new ArrayList<>();
				Set<Integer> totalSolvedProblemSet = api.getSolvedProblemSet(this);
				
				HomeworkManager homeworkManager = new HomeworkManager();
				
				for(int week=1; week<=HomeworkManager.totalWeek; ++week) {
					WeekHomework homework = homeworkManager.getHomework(week);
					if(!homework.isActive()) continue;
					
					for(int cnt=0; cnt<homework.getCount(); ++cnt) {
						Problem problem = homework.getProblem(cnt);
						if(!this.passedProblemPerWeek.get(week).get(cnt) && totalSolvedProblemSet.contains(problem.getID())) {
							this.passedProblemPerWeek.get(week).set(cnt, true);
							newSolvedProblemList.add(problem);
						}
					}
				}
				
				this.solvedProblemCount = totalSolvedProblemCount;
				
				return newSolvedProblemList;
			}
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	
}
