package me.Untaini.DiscordBotForAlgorithm;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class WeekHomework {
	private boolean activation;
	private List<Problem> problems;
	
	public WeekHomework() {
		this.problems = new ArrayList<>();
	}
	
	public WeekHomework(JsonObject json) {
		this.problems = new ArrayList<>();
		
		this.activation = json.get("activation").getAsBoolean();
		for(JsonElement problem : json.get("problems").getAsJsonArray()) {
			int problemId = problem.getAsJsonObject().get("id").getAsInt();
			String problemName = problem.getAsJsonObject().get("name").getAsString();
			problems.add(new Problem(problemId, problemName));
		}
	}
	
	public boolean isActive() {
		return activation;
	}
	
	public void setActive(boolean active) {
		this.activation = active;
	}
	
	public int getCount() {
		return problems.size();
	}
	
	public boolean addProblem(int problemId, String problemName) {
		return problems.add(new Problem(problemId, problemName));
	}
	
	public Problem replaceProblem(int index, int problemId, String problemName) {
		return problems.set(index, new Problem(problemId, problemName));
	}
	
	public Problem removeProblem(int index){
		return problems.remove(index);
	}
	
	public List<Problem> getProblems(){
		return problems;
	}
	
	public Problem getProblem(int index) {
		return problems.get(index);
	}
}
