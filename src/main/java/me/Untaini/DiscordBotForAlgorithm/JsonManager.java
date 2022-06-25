package me.Untaini.DiscordBotForAlgorithm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;



public class JsonManager {
	static JsonElement getJsonFile(String fileName) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			JsonElement json = new Gson().fromJson(br, JsonElement.class);
			br.close();
			return json;
		}
		catch(Exception e) {
			return new JsonObject();
		}
	}
	
	static JsonElement getJson(String jsonString) {
		try {
			return new Gson().fromJson(jsonString, JsonElement.class);
		}
		catch(Exception e) {
			return new JsonObject();
		}
	}
	
	static void saveJsonFile(String fileName, Object data) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileName)));
			new GsonBuilder().setPrettyPrinting().create().toJson(data, bw);
			bw.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
		
	}
}
