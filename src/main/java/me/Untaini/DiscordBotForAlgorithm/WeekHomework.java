package me.Untaini.DiscordBotForAlgorithm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import net.dv8tion.jda.internal.utils.tuple.ImmutablePair;
import net.dv8tion.jda.internal.utils.tuple.Pair;

public class WeekHomework {
	private boolean active;
	private List<Integer> problemIds;
	private List<String> problemNames;
	
	public WeekHomework() {
		this.problemIds = new ArrayList<>();
		this.problemNames = new ArrayList<>();
	}
	
	public WeekHomework(JSONObject json) {
		this.problemIds = new ArrayList<>();
		this.problemNames = new ArrayList<>();
		
		this.active = (boolean) json.get("isActive");
		for(Object problem : (JSONArray)json.get("problems")) {
			problemIds.add((Integer) (((JSONObject)problem).get("problemId")));
			problemNames.add((String) (((JSONObject)problem).get("problemName")));
		}
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public int getCount() {
		return problemIds.size();
	}
	
	public boolean addProblem(int problemId, String problemName) {
		return problemIds.add(problemId) && problemNames.add(problemName);
	}
	
	public Pair<Integer, String> replaceProblem(int index, int problemId, String problemName) {
		return new ImmutablePair<>(problemIds.set(index, problemId), problemNames.set(index, problemName));
	}
	
	public Pair<Integer, String> removeProblem(int index){
		return new ImmutablePair<>(problemIds.remove(index), problemNames.remove(index));
	}
	
	public JSONObject getJSONObject() {
		Map<String, Object> json = new HashMap<>();
		List<Object> jsonArr = new ArrayList<>();
		
		for(int cnt=0; cnt<problemIds.size(); ++cnt) {
			Map<String, Object> jsonMap = new HashMap<>();
			jsonMap.put("problemId", problemIds.get(cnt));
			jsonMap.put("ProblemName", problemNames.get(cnt));
			jsonArr.add(jsonMap);
		}
		
		json.put("problems", jsonArr);
		return new JSONObject(json);
	}

}
