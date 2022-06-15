package me.Untaini.DiscordBotForAlgorithm;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
        else 
        	throw new Exception();
			
	    return buffer.toString();
    }
	
	public Set<Integer> getSolvedProblemSet(User user) throws Exception {
		Set<Integer> resultSet = new HashSet<>();
		String apiUrlFormat = "https://solved.ac/api/v3/search/problem?query=solved_by:%s&page=%d";
		for(int cnt=0; cnt*100<user.getSolvedProblemCount(); ++cnt) {
			JSONObject json = JSONManager.getJSON(apiGet(String.format(apiUrlFormat, user.getBaekjoonId(), cnt)));
			for(Object problem : ((JSONArray)json.get("items"))) 
				resultSet.add((Integer) (((JSONObject)problem).get("problemId")));
		}
		
		return resultSet;
	}
	
	public String getProblemName(int problemId) throws Exception{
		String apiUrlFormat = "https://solved.ac/api/v3/problem/show?problemId=%d";
		JSONObject json = JSONManager.getJSON(apiGet(String.format(apiUrlFormat, problemId)));
		return (String) json.get("titleKo");
	}
	
}
