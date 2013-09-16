package com.jumplife.videoloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FiveSixLoader {
	
	public static String Loader(String videoId) {
		
		String streamLink = null;
		
		String vId = parseVId(videoId);
		String[] value = getSignValueTimeStamp(vId);
		
		String message = getMessageFromServer("GET", "http://oapi.56.com/video/mobile.json?appkey=3000002938" +
				"&sign=" + value[0] +
				"&ts=" + value[1] + 
				"&vid=" + vId, null);
		
		if(message != null) {
			try {
				JSONObject jsonObject = new JSONObject(message.toString());
				JSONArray jsonArray = jsonObject.getJSONObject("info").getJSONArray("rfiles");
				if(jsonArray.length() > 1 && 
						(jsonArray.getJSONObject(1).getString("url").contains(".mp4") || 
						jsonArray.getJSONObject(1).getString("url").contains(".flv")))
					streamLink = jsonArray.getJSONObject(1).getString("url");
				else
					streamLink = jsonArray.getJSONObject(0).getString("url"); 
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return streamLink;
	}
	
	private static String parseVId(String videoId) {
		String vId = null;
		String[] splitLink;
		
		if(videoId.contains("v_")) {
			splitLink = videoId.split("v_");
			vId = splitLink[1].substring(0, 11);
		} else if(videoId.contains("vid=")) {
			splitLink = videoId.split("vid=");
			vId = splitLink[1].substring(0, 11);
		} else if(videoId.contains("_vid-")) {
			splitLink = videoId.split("_vid-");
			vId = splitLink[1].substring(0, 11);
		} else if(videoId.contains("iframe")) {
			splitLink = videoId.split("iframe");
			vId = splitLink[1].substring(0, 11);
		} else if(videoId.contains("id-")) {
			splitLink = videoId.split("id-");
			vId = splitLink[1].substring(0, 11);
		} else if(videoId.contains("cpm_")) {
			splitLink = videoId.split("cpm_");
			vId = splitLink[1].substring(0, 11);
		}
		
		return vId;
	}
	
	private static String[] getSignValueTimeStamp(String vId) {
		String value[] = new String[2];		
		String message = getMessageFromServer("GET", "http://drama.jumplife.com.tw/api/v1/youtube_sources/get_56_sign.json?vid=" + vId, null);
		
		if(message != null) {
			JSONObject jsonObject;
			try {
				jsonObject = new JSONObject(message.toString());
				value[0] = jsonObject.getString("sign");
				value[1] = jsonObject.getString("timestamp");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return value;
	}
	
	public static String getMessageFromServer(String requestMethod, String apiPath, JSONObject json) {
		URL url;
		try {
			url = new URL(apiPath);
				
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod);
			connection.setRequestProperty("User-Agent", "<em>Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.0.10) Gecko/2009042316 Firefox/3.0.10 (.NET CLR 3.5.30729)</em>");			
			connection.setRequestProperty("Content-Type",  "application/json;charset=utf-8");
			connection.setReadTimeout(20000);
			connection.setConnectTimeout(30000);
			connection.setDoInput(true);
			connection.connect();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder lines = new StringBuilder();
			String tempStr;
			
			while ((tempStr = reader.readLine()) != null) {
	            lines = lines.append(tempStr);
	        }
			
			reader.close();
			connection.disconnect();
			
			return lines.toString();
		} 
		catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} 
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
}
