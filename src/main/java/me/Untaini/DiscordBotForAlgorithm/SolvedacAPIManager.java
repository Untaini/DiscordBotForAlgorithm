package me.Untaini.DiscordBotForAlgorithm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SolvedacAPIManager {
	private String apiGet(String apiUrl) throws Exception {
	    int connTimeout = 5000;
	    int readTimeout = 3000;	
	    
	    URL url = new URL(apiUrl);
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setConnectTimeout(connTimeout);
        urlConnection.setReadTimeout(readTimeout);
        urlConnection.setRequestProperty("Accept", "application/json;");
			
	    StringBuilder buffer = new StringBuilder();
        if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream(),"UTF-8"));
            String readLine;
            while((readLine = bufferedReader.readLine()) != null) 
                buffer.append(readLine);
            bufferedReader.close();
        }
        else if(urlConnection.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)
        	return null;
        else
        	throw new Exception(urlConnection.getResponseMessage());
			
	    return buffer.toString();
    }
	
	public Set<Integer> getSolvedProblemSet(User user) throws Exception {
		Set<Integer> resultSet = new HashSet<>();
		String apiUrlFormat = "https://solved.ac/api/v3/search/problem?query=solved_by:%s&page=%d";
		int solvedProblemCount = 1; 
		for(int cnt=0; cnt*100<solvedProblemCount; ++cnt) {
			JsonObject json = JsonManager.getJson(apiGet(String.format(apiUrlFormat, user.getBaekjoonId(), cnt+1))).getAsJsonObject();
			solvedProblemCount = json.get("count").getAsInt();
			for(JsonElement problem : json.get("items").getAsJsonArray()) 
				resultSet.add(problem.getAsJsonObject().get("problemId").getAsInt());
			
		}
		
		return resultSet;
	}
	
	public int getSolvedProblems(User user) throws Exception {
		String apiUrlFormat = "https://solved.ac/api/v3/search/problem?query=solved_by:%s&page=1";
		JsonObject json = JsonManager.getJson(apiGet(String.format(apiUrlFormat, user.getBaekjoonId()))).getAsJsonObject();
		return json.get("count").getAsInt();
	}
	
	public String getProblemName(int problemId) throws Exception {
		String apiUrlFormat = "https://solved.ac/api/v3/problem/show?problemId=%d";
		JsonObject json = JsonManager.getJson(apiGet(String.format(apiUrlFormat, problemId))).getAsJsonObject();
		return json.get("titleKo").getAsString();
	}
	
	public String getProblemLink(int problemId) {
		return String.format("https://boj.kr/%d", problemId);
	}
	
	public boolean isBaekjoonId(String baekjoonId) {
		String apiUrlFormat = "https://solved.ac/api/v3/user/show?handle=%s";
		try {
			return apiGet(String.format(apiUrlFormat, baekjoonId)) != null;
		} catch (Exception e) {}
		return false;
	}
	
	
}
