package com.jumplife.videoloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class DailymotionLoader {
	
	public static String Loader(String videoId) {
		InputStream localInputStream = null;
		Log.d("DailyMotiondownloader", "parselinkUrl link is: " + videoId);
		try {
			HttpURLConnection localHttpURLConnection = (HttpURLConnection)new URL(videoId).openConnection();
		    localHttpURLConnection.setRequestProperty("User-Agent", "<em>Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.0.10) Gecko/2009042316 Firefox/3.0.10 (.NET CLR 3.5.30729)</em>");
		    localHttpURLConnection.setReadTimeout(20000);
		    localHttpURLConnection.setConnectTimeout(30000);
		    localHttpURLConnection.setInstanceFollowRedirects(false);
		    localHttpURLConnection.setRequestMethod("GET");
		    localHttpURLConnection.setDoInput(true);
		    localHttpURLConnection.connect();
		    int i = localHttpURLConnection.getResponseCode();
		    Log.d("DailyMotiondownloader", "The response is: " + i);
		    localInputStream = localHttpURLConnection.getInputStream();
		    //InputStreamReader localInputStreamReader = new InputStreamReader(localInputStream, "UTF-8");
		    //char[] arrayOfChar = new char[20000];
		    //localInputStreamReader.read(arrayOfChar);
		    //String str1 = new String(arrayOfChar);
		    
		    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		    byte[] data = new byte[4096];  
	        int count = -1;  
	        while((count = localInputStream.read(data, 0, 4096)) != -1)  
	            outStream.write(data, 0, count);  	          
	        data = null;
	        String str1 = new String(outStream.toByteArray(),"UTF-8");
	        
		    Matcher localMatcher1 = Pattern.compile("var info = \\{(.+)\\}\\}").matcher(str1);
		    String[] arrayOfString = null;
		    if (localMatcher1.find())
		    	arrayOfString = localMatcher1.group(1).split(",");
		    if(arrayOfString != null) {
			    for (int j = 0; ; j++) {
			    	int k = arrayOfString.length;
			        if (j >= k)
			        	return null;
			        Matcher localMatcher2 = Pattern.compile("\"(.+)\":\"(.+)\"").matcher(arrayOfString[j]);
			        Log.d(null, arrayOfString[j]);
			        if ((localMatcher2.find()) && ((localMatcher2.group(1).equalsIgnoreCase("stream_h264_url")) || 
			        		(localMatcher2.group(1).equalsIgnoreCase("stream_h264_hq_url")))) {
			        	String localurlData = null;
			        	String str2 = localMatcher2.group(2).replace("\\/", "/");
			        	localurlData = str2;
			        	return localurlData;
			        }
			    }
		    }
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (localInputStream != null)
				try {
					localInputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return null;
	}
}
