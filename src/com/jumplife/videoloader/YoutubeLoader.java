package com.jumplife.videoloader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;

import android.os.Build;
import android.util.Log;


public class YoutubeLoader {
	
	static final String YOUTUBE_VIDEO_INFORMATION_URL = "http://www.youtube.com/get_video_info?video_id=";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ArrayList<String> Loader(boolean pFallback, String pYouTubeVideoId) {
		InputStream ins = null;
		List<NameValuePair> params = new ArrayList();
		String video_info = "";
		HttpPost httpRequest = new HttpPost(YOUTUBE_VIDEO_INFORMATION_URL + pYouTubeVideoId + "&eurl=http://kej.tw/");
		HttpResponse httpResponse;
	    try
	    {
	      BasicHttpParams localBasicHttpParams = new BasicHttpParams();
	      HttpConnectionParams.setConnectionTimeout(localBasicHttpParams, 10000);
	      HttpConnectionParams.setSoTimeout(localBasicHttpParams, 10000);
	      httpRequest.addHeader("Cache-Control", "no-cache");
	      httpRequest.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));
	      httpRequest.setParams(localBasicHttpParams);
	      httpResponse = new DefaultHttpClient().execute(httpRequest);
	      if (httpResponse.getStatusLine().getStatusCode() == 200) {
	        ins = new ByteArrayInputStream(EntityUtils.toString(httpResponse.getEntity(), "utf-8").getBytes());
	        String str = inputStream2String(ins);
	        video_info = str;
	      }
	    } catch (Exception localException) {
	      Log.e("PostHttp", "request e:" + localException);
	    }
	    
	    HashMap<String, String> localHashMap1 = new HashMap<String, String>();
	    HashMap tmp = parse_str(video_info);
	    String currentType = "";
	    String currentQuality = "";
	    Log.d("player", "video info : " + tmp);
	    
	    if(tmp.containsKey("url_encoded_fmt_stream_map")) {
		    String[] arrayOfString1 = ((String)tmp.get("url_encoded_fmt_stream_map")).split(",");
		    for (int i=0; i<arrayOfString1.length; i++) {
		      HashMap localHashMap2 = parse_str(arrayOfString1[i]);
		      String str1 = (String)localHashMap2.get("type");
		      String str2 = (String)localHashMap2.get("quality");
		      
		      Log.d("player", "type : " + str1 + " quality : " + str2);
		      
		      if(str1 != null && str2 != null) {
			      String[] arrayOfString2 = str1.split(";");
			      if (arrayOfString2.length > 1)
			    	  str1 = arrayOfString2[0];
			      if(Build.VERSION.SDK_INT < 11) {
				      if (((str1.equals("video/mp4")) || (str1.equals("video/3gpp"))) && 
				    		  ((str2.equals("large")) || (str2.equals("medium")))) {
				    	  if(str2.equals(currentQuality) && (currentType.equals("video/mp4") || currentType.equals("video/3gpp"))) {
			    			  
			    		  } else {
			    			  currentType = str1;
			    			  currentQuality = str2;
				    		  String str3 = (String)localHashMap2.get("quality");
					    	  String str4 = (String)localHashMap2.get("url") + "&signature=" + (String)localHashMap2.get("sig");
					    	  if(localHashMap1.containsKey(str3))
					    		  localHashMap1.remove(str3);
						      localHashMap1.put(str3, str4);
			    		  }
				      }
			      } else {
			    	  if (((str1.equals("video/mp4")) || (str1.equals("video/webm")) || (str1.equals("video/3gpp"))) && 
				    		  ((str2.equals("hd720")) || (str2.equals("large")) || (str2.equals("medium")))) {
			    		  if(str2.equals(currentQuality) && (currentType.equals("video/mp4") || currentType.equals("video/webm") || currentType.equals("video/3gpp"))) {
			    			  
			    		  } else {
			    			  currentType = str1;
			    			  currentQuality = str2;
				    		  String str3 = (String)localHashMap2.get("quality");
					    	  String str4 = (String)localHashMap2.get("url") + "&signature=" + (String)localHashMap2.get("sig");
					    	  if(localHashMap1.containsKey(str3))
					    		  localHashMap1.remove(str3);
						      localHashMap1.put(str3, str4);
			    		  }
				      }
			      }
		      }
		    }
	    }
	    
	    ArrayList<String> qualityList = new ArrayList<String>();
	    if(localHashMap1.containsKey("hd720"))
	    	qualityList.add(localHashMap1.get("hd720"));
	    if(localHashMap1.containsKey("large"))
	    	qualityList.add(localHashMap1.get("large"));
	    if(localHashMap1.containsKey("medium"))
	    	qualityList.add(localHashMap1.get("medium"));
	    	
	    return qualityList;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static HashMap<String, String> parse_str(String paramString) {
		HashMap localHashMap = new HashMap();
		if(paramString == null)
		    return localHashMap;
		
	    try {
	    	String[] arrayOfString1 = paramString.split("&");
	    	for (int i = 0; ; i++) {
	    		if (i >= arrayOfString1.length)
	    			return localHashMap;
	    		String[] arrayOfString2 = arrayOfString1[i].split("=");
	    		if (arrayOfString2.length > 1)
	    			localHashMap.put(arrayOfString2[0], URLDecoder.decode(arrayOfString2[1], "utf-8"));
	    	}
	    } catch (UnsupportedEncodingException localUnsupportedEncodingException) {
	      localUnsupportedEncodingException.printStackTrace();
	    }
	    return localHashMap;
	  }
	
	@SuppressWarnings("deprecation")
	public static String calculateYouTubeUrl(String pYouTubeFmtQuality, boolean pFallback, String pYouTubeVideoId) {

		String lUriStr = null;
		HttpClient lClient = new DefaultHttpClient();

		HttpGet lGetMethod = new HttpGet(YOUTUBE_VIDEO_INFORMATION_URL + pYouTubeVideoId + "&eurl=http://kej.tw/");
		HttpResponse lResp;
		
		String lInfoStr = null;
		ByteArrayOutputStream lBOS = new ByteArrayOutputStream();			
		
		try {
			lResp = lClient.execute(lGetMethod);
			lResp.getEntity().writeTo(lBOS);
			lInfoStr = new String(lBOS.toString("UTF-8"));
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String[] lArgs = lInfoStr.split("&");
		Map<String, String> lArgMap = new HashMap<String, String>();
		for (int i = 0; i < lArgs.length; i++) {
			String[] lArgValStrArr = lArgs[i].split("=");
			if (lArgValStrArr != null) {
				if (lArgValStrArr.length >= 2) {
					lArgMap.put(lArgValStrArr[0], URLDecoder.decode(lArgValStrArr[1]));
				}
			}
		}

		//Find out the URI string from the parameters
		//Populate the list of formats for the video
		String lFmtList = null;
		if(lArgMap.get("fmt_list") != null)
			lFmtList = URLDecoder.decode(lArgMap.get("fmt_list"));
		ArrayList<Format> lFormats = new ArrayList<Format>();
		if (null != lFmtList) {
			String lFormatStrs[] = lFmtList.split(",");
			YoutubeLoader tmp = new YoutubeLoader();
			for (String lFormatStr : lFormatStrs) {
				Format lFormat = tmp.new Format(lFormatStr);
				lFormats.add(lFormat);
			}
		}

		//Populate the list of streams for the video
		String lStreamList = lArgMap.get("url_encoded_fmt_stream_map");
		if (null != lStreamList) {
			String lStreamStrs[] = lStreamList.split(",");
			ArrayList<VideoStream> lStreams = new ArrayList<VideoStream>();
			YoutubeLoader tmp = new YoutubeLoader();
			for (String lStreamStr : lStreamStrs) {
				VideoStream lStream = tmp.new VideoStream(lStreamStr);
				lStreams.add(lStream);
			}

			//Search for the given format in the list of video formats
			//if it is there, select the corresponding stream
			//otherwise if fallback is requested, check for next lower format
			int lFormatId = Integer.parseInt(pYouTubeFmtQuality);

			Format lSearchFormat = tmp.new Format(lFormatId);
			while (!lFormats.contains(lSearchFormat) && pFallback) {
				int lOldId = lSearchFormat.getId();
				int lNewId = getSupportedFallbackId(lOldId);

				if (lOldId == lNewId) {
					break;
				}
				lSearchFormat = tmp.new Format(lNewId);
			}

			int lIndex = lFormats.indexOf(lSearchFormat);
			if (lIndex >= 0) {
				VideoStream lSearchStream = lStreams.get(lIndex);
				lUriStr = lSearchStream.getUrl();
			}

		}
		//Return the URI string. It may be null if the format (or a fallback format if enabled)
		//is not found in the list of formats for the video
		return lUriStr;
	}
	


	private static String inputStream2String(InputStream paramInputStream) {
		BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(paramInputStream));
		StringBuffer localStringBuffer = new StringBuffer();
		while (true) {
			String str;
			try {
				str = localBufferedReader.readLine();
				if (str == null)
					return localStringBuffer.toString();
				localStringBuffer.append(str);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	  
	public static int getSupportedFallbackId(int pOldId) {
        final int lSupportedFormatIds[] = {13,  //3GPP (MPEG-4 encoded) Low quality
                17,  //3GPP (MPEG-4 encoded) Medium quality
                18,  //MP4  (H.264 encoded) Normal quality
                22,  //MP4  (H.264 encoded) High quality
                37   //MP4  (H.264 encoded) High quality
        };
        int lFallbackId = pOldId;
        for (int i = lSupportedFormatIds.length - 1; i >= 0; i--) {
            if (pOldId == lSupportedFormatIds[i] && i > 0) {
                lFallbackId = lSupportedFormatIds[i - 1];
            }
        }
        return lFallbackId;
    }
	
	public class Format {
		
	    protected int mId;

	    public Format(String pFormatString) {
	        String lFormatVars[] = pFormatString.split("/");
	        mId = Integer.parseInt(lFormatVars[0]);
	    }
	    public Format(int pId) {
	        this.mId = pId;
	    }

	    public int getId() {
	        return mId;
	    }
	    public boolean equals(Object pObject) {
	        if (!(pObject instanceof Format)) {
	            return false;
	        }
	        return ((Format) pObject).mId == mId;
	    }
	}
	
	public class VideoStream {

	    protected String mUrl;

	    public VideoStream(String pStreamStr) {
	        String[] lArgs = pStreamStr.split("&");
	        Map<String, String> lArgMap = new HashMap<String, String>();
	        for (int i = 0; i < lArgs.length; i++) {
	            String[] lArgValStrArr = lArgs[i].split("=");
	            if (lArgValStrArr != null) {
	                if (lArgValStrArr.length >= 2) {
	                    lArgMap.put(lArgValStrArr[0], lArgValStrArr[1]);
	                }
	            }
	        }
	        mUrl = lArgMap.get("url") + "&signature=" + lArgMap.get("sig");
	    }

	    public String getUrl() {
	        return mUrl;
	    }
	}
}
