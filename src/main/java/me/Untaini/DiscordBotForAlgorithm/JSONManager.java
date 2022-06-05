package me.Untaini.DiscordBotForAlgorithm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class JSONManager {
	static JSONObject getJSON(String fileName) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			JSONObject json = (JSONObject)new JSONParser().parse(br);
			br.close();
			return json;
		}catch(Exception e) {}
		
		return new JSONObject();
	}
	
	static void saveJSON(String fileName, JSONObject json) {
		try {
			FileWriter fw = new FileWriter(new File(fileName));
			fw.write(json.toJSONString());
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	
		
	}
}
