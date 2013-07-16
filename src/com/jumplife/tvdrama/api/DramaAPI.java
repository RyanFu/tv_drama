package com.jumplife.tvdrama.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jumplife.sqlite.SQLiteTvDramaHelper;
import com.jumplife.tvdrama.entity.AppProject;
import com.jumplife.tvdrama.entity.Advertisement;
import com.jumplife.tvdrama.entity.Chapter;
import com.jumplife.tvdrama.entity.Drama;
import com.jumplife.tvdrama.entity.News;
import com.jumplife.tvdrama.entity.Section;
import com.jumplife.tvdrama.entity.Ticket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.TelephonyManager;
import android.util.Log;

public class DramaAPI {

	private String urlAddress;
	private HttpURLConnection connection;
	private String requestedMethod;
	private Activity mActivity;
	private int connectionTimeout;
	private int readTimeout;
	private boolean usercaches;
	private boolean doInput;
	private boolean doOutput;
	
	public static final String TAG = "DRAMA_API";
	public static final boolean DEBUG = true;
	
	public DramaAPI(String urlAddress, int connectionTimeout, int readTimeout) {
		this.urlAddress = new String(urlAddress + "/");
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
		this.usercaches = false;
		this.doInput = true;
		this.doOutput = true;
	}
	public DramaAPI(String urlAddress) {
		this(new String(urlAddress), 5000, 5000);
	}
	
	public DramaAPI(Activity a) {
		this(new String("http://drama.jumplife.com.tw"));
		this.mActivity = a;
	}
	
	public DramaAPI() {
		this(new String("http://drama.jumplife.com.tw"));
	}
	
	public int connect(String requestedMethod, String apiPath) {
		int status = -1;
		try {
			URL url = new URL(urlAddress + apiPath);
			
			if(DEBUG)
				Log.d(TAG, "URL: " + url.toString());
			connection = (HttpURLConnection) url.openConnection();
					
			connection.setRequestMethod(requestedMethod);
			connection.setReadTimeout(this.readTimeout);
			connection.setConnectTimeout(this.connectionTimeout);
			connection.setUseCaches(this.usercaches);
			connection.setDoInput(this.doInput);
			connection.setDoOutput(this.doOutput);
			connection.setRequestProperty("Content-Type",  "application/json;charset=utf-8");
			
			connection.connect();

		} 
		catch (MalformedURLException e1) {
			e1.printStackTrace();
			return status;
		}
		catch (IOException e) {
			e.printStackTrace();
			return status;
		}
		
		return status;
	}
	
	public void disconnect()
	{
		connection.disconnect();
	}
	
	public ArrayList<Integer> getDramasId(){
		ArrayList<Integer> dramasId = new ArrayList<Integer>(100);
		String message = getMessageFromServer("GET", "api/v1/dramas.json", null);
		if(message == null) {
			return null;
		}
		else {			
			try {
				JSONArray dramaIdsJson;
				dramaIdsJson = new JSONArray(message.toString());
				for(int i=0; i<dramaIdsJson.length(); i++)
					dramasId.add(dramaIdsJson.getInt(i));
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return dramasId;
	}
	
	public ArrayList<Drama> getDramasIdViewsEpsV2(){
		ArrayList<Drama> dramas = new ArrayList<Drama>(100);
		String message = getMessageFromServer("GET", "api/v1/dramas/dramas_with_views_v2.json", null);
		if(message == null) {
			return null;
		}
		else {			
			try {
				JSONArray dramasArray = new JSONArray(message.toString());
				for(int i=0; i<dramasArray.length(); i++) {
					JSONObject dramaObject = dramasArray.getJSONObject(i);
					Drama tmp = new Drama();
					tmp.setId(dramaObject.getInt("id"));
					tmp.setViews(dramaObject.getInt("views"));
					tmp.setEps(dramaObject.getString("eps_num_str"));
					dramas.add(tmp);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return dramas;
	}
	
	public ArrayList<Drama> getDramasIdViewsEps(){
		ArrayList<Drama> dramas = new ArrayList<Drama>(100);
		String message = getMessageFromServer("GET", "api/v1/dramas/dramas_with_views.json", null);
		if(message == null) {
			return null;
		}
		else {			
			try {
				JSONArray dramasArray = new JSONArray(message.toString());
				for(int i=0; i<dramasArray.length(); i++) {
					JSONObject dramaObject = dramasArray.getJSONObject(i);
					Drama tmp = new Drama();
					tmp.setId(dramaObject.getInt("id"));
					tmp.setViews(dramaObject.getInt("views"));
					tmp.setEps(dramaObject.getString("eps_num_str"));
					dramas.add(tmp);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return dramas;
	}
	
	//取得電影時刻表
	public ArrayList<Chapter> getDramaChapter(int dramaId) {
		ArrayList<Chapter> chapterList = new ArrayList<Chapter>(10);
	    String message = getMessageFromServer("GET", "api/v1/eps.json?drama_id=" + dramaId, null);
		
		if(message == null) {
			return null;
		}
		else {
			try {
				JSONArray chaptersJson = new JSONArray(message.toString());
				for(int i=0; i<chaptersJson.length(); i++) {
					JSONObject chapterJson = chaptersJson.getJSONObject(i);
					Chapter chapter = new Chapter();
					
					if(!chapterJson.isNull("id"))
						chapter.setId(chapterJson.getInt("id"));
					
					if(!chapterJson.isNull("num"))
						chapter.setNumber(chapterJson.getInt("num"));
					
					chapterList.add(chapter);
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return chapterList;
			}
		}

		return chapterList;
	}
	
	//取得電影時刻表
	public ArrayList<Section> getChapterSection(int chapterId) {
		ArrayList<Section> sectionList = new ArrayList<Section>(10);
	    String message = getMessageFromServer("GET", "api/v1/youtube_sources.json?ep_id=" + chapterId, null);
		
		if(message == null) {
			return null;
		}
		else {
			try {
				JSONArray sectionsJson = new JSONArray(message.toString());
				for(int i=0; i<sectionsJson.length(); i++) {
					JSONObject sectionJson = sectionsJson.getJSONObject(i);
					Section section = new Section();
					
					if(!sectionJson.isNull("id"))
						section.setId(sectionJson.getInt("id"));
					
					if(!sectionJson.isNull("link"))
						section.setUrl(sectionJson.getString("link"));
					
					sectionList.add(section);
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return sectionList;
			}
		}

		return sectionList;
	}
		
	//取得電影時刻表
	public ArrayList<Section> getChapterSectionNew(int dramaId, int chapterNo) {
		ArrayList<Section> sectionList = new ArrayList<Section>(10);
	    String message = getMessageFromServer("GET", "api/v1/" +
	    		"youtube_sources/find_by_drama_and_ep_num.json?drama_id=" + dramaId + "&num=" + chapterNo, null);
		
		if(message == null) {
			return sectionList;
		}
		else {
			try {
				JSONArray sectionsJson = new JSONArray(message.toString());
				for(int i=0; i<sectionsJson.length(); i++) {
					JSONObject sectionJson = sectionsJson.getJSONObject(i);
					Section section = new Section();
					
					if(!sectionJson.isNull("ep_id"))
						section.setId(sectionJson.getInt("ep_id"));
					
					if(!sectionJson.isNull("link"))
						section.setUrl(sectionJson.getString("link"));
					
					sectionList.add(section);
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return sectionList;
			}
		}

		return sectionList;
	}
		
	public void AddDramasFromInfo(SQLiteTvDramaHelper instance, SQLiteDatabase db, String idlst) {
		Log.d(TAG, "id list : " + idlst);
		String message = getMessageFromServer("GET", "api/v1/dramas/new_dramas_info.json?dramas_id=" + idlst, null);
		
		if(message != null) {
			try {
				JSONArray dramaArray;		
				dramaArray = new JSONArray(message.toString());
				ArrayList<Drama> dramas = new ArrayList<Drama>();
				for (int i = 0; i < dramaArray.length() ; i++) {
					JSONObject dramaJson = dramaArray.getJSONObject(i);
					Drama drama = DramaJsonToClass(dramaJson);
					dramas.add(drama);
				}
				if(dramas != null && dramas.size() > 0) {
					Log.d(TAG, "Insert New Drama.");
					instance.insertDramas(db, dramas);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	public String getDramaEps(int id) {
		String message = getMessageFromServer("GET", "api/v1/dramas/new_dramas_info.json?dramas_id=" + id, null);
		//SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(mActivity);
		
		if(message != null) {
			try {
				JSONArray dramaArray;		
				dramaArray = new JSONArray(message.toString());
				JSONObject dramaJson = dramaArray.getJSONObject(0);
				Drama drama = DramaJsonToClass(dramaJson);
				return drama.getEps();
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return null;
	}
	
	public String getMessageFromServer(String requestMethod, String apiPath, JSONObject json) {
		URL url;
		try {
			url = new URL(this.urlAddress +  apiPath);
			if(DEBUG)
				Log.d(TAG, "URL: " + url);
				
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod);
			
			connection.setRequestProperty("Content-Type",  "application/json;charset=utf-8");
			if(requestMethod.equalsIgnoreCase("POST"))
				connection.setDoOutput(true);
			connection.setDoInput(true);
			connection.connect();
			
			
			if(requestMethod.equalsIgnoreCase("POST")) {
				OutputStream outputStream;
				
				outputStream = connection.getOutputStream();
				if(json != null)
					outputStream.write(json.toString().getBytes());
				outputStream.flush();
				outputStream.close();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder lines = new StringBuilder();
			String tempStr;
			
			while ((tempStr = reader.readLine()) != null) {
	            lines = lines.append(tempStr);
	        }
			if(DEBUG)
				Log.d(TAG, lines.toString());
			
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
	
	public Drama DramaJsonToClass (JSONObject dramaJson) {
		Drama drama = null;
		String releaseDate = "";
		Boolean isShow = true;
		
		if(dramaJson == null) {
			return null;
		}
		else {
			try {
				if(!dramaJson.isNull("release"))
					releaseDate = dramaJson.getString("release");
				else if(!dramaJson.isNull("release_date"))
					releaseDate = dramaJson.getString("release_date");
						
				if(!dramaJson.isNull("is_show"))
					isShow = dramaJson.getBoolean("is_show");
				
				drama = new Drama(dramaJson.getInt("id"), dramaJson.getString("name"), dramaJson.getString("poster_url"), 
						dramaJson.getString("introduction"), dramaJson.getInt("area_id"), releaseDate, isShow, 0,
						dramaJson.getString("eps_num_str"));
				 
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}	
		}
		return drama;
	}
	
	public void getPromotion(String picUrl, String link, String title, String description) {
				
		String message = getMessageFromServer("GET", "api/promotion.json", null);
		
		try{
			JSONObject responseJson = new JSONObject(message);
			
			picUrl = responseJson.getString("picture_link");
			link = responseJson.getString("link");
			title = responseJson.getString("tilte");
			description = responseJson.getString("description");
		} 
		catch (JSONException e){
			e.printStackTrace();
		}
	}
	
	public String[] getPromotion() {
				
		String message = getMessageFromServer("GET", "api/promotion.json", null);
		String[] tmp = new String[5];
				
		if(message == null) {
			return null;
		}
		try{
			JSONObject responseJson = new JSONObject(message);
			
			tmp[0] = (responseJson.getString("picture_link"));
			tmp[1] = (responseJson.getString("link"));
			tmp[2] = (responseJson.getString("tilte"));
			tmp[3] = (responseJson.getString("description"));
			tmp[4] = (responseJson.getString("version"));
		} 
		catch (JSONException e){
			e.printStackTrace();
			return null;
		}
		
		return tmp;
	}
	
	public boolean updateViews(int DramaId) {
		boolean result = false;

		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			String url = urlAddress + "api/v1/dramas/" + DramaId + ".json";						
			if(DEBUG)
				Log.d(TAG, "URL : " + url);
			
			HttpPut httpPut = new HttpPut(url);
			HttpResponse response = httpClient.execute(httpPut);
			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
		} 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} 
		catch (IOException e){
			e.printStackTrace();
			return result;
		}	
		return result;
	}
	
	public boolean updateViewsWithDevice(int DramaId, int epId) {
		final TelephonyManager tm = (TelephonyManager) mActivity.getSystemService(Context.TELEPHONY_SERVICE);
		boolean result = false;
		String DeviceId;
		if(tm.getLine1Number() != null && !tm.getLine1Number().equals(""))
			DeviceId = tm.getLine1Number();
		else 
			DeviceId = tm.getDeviceId();
		
		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			String url = urlAddress + "api/v1/dramas/update_device_watch.json?" +
					"registration_id=" + DeviceId + 
					"&drama_id=" + DramaId +
					"&ep_num=" + epId;						
			if(DEBUG)
				Log.d(TAG, "URL : " + url);
			
			HttpPut httpPut = new HttpPut(url);
			HttpResponse response = httpClient.execute(httpPut);
			
			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
		} 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} 
		catch (IOException e){
			e.printStackTrace();
			return result;
		}	
		return result;
	}
	
	@SuppressWarnings("null")
	public ArrayList<AppProject> getAppProjectList (Activity mActivity) {
		ArrayList<AppProject> appList = new ArrayList<AppProject>(10);
		String requestMethod = "GET";
		URL url;
		String message = null;
		try {
			url = new URL("http://mmedia.jumplife.com.tw/api/v1/appprojects.json");
			JSONObject json = null;
			
			HttpURLConnection connection;
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod(requestMethod);
			
			connection.setRequestProperty("Content-Type",  "application/json;charset=utf-8");
			if(requestMethod.equalsIgnoreCase("POST"))
				connection.setDoOutput(true);
			else
				connection.setDoInput(true);
			connection.connect();
			
			
			if(requestMethod.equalsIgnoreCase("POST")) {
				OutputStream outputStream;
				
				outputStream = connection.getOutputStream();
				if(DEBUG)
					Log.d("post message", json.toString());
				
				outputStream.write(json.toString().getBytes());
				outputStream.flush();
				outputStream.close();
			}
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			StringBuilder lines = new StringBuilder();
			String tempStr;
			
			while ((tempStr = reader.readLine()) != null) {
	            lines = lines.append(tempStr);
	        }
			if(DEBUG)
				Log.d(TAG, lines.toString());
			
			reader.close();
			connection.disconnect();
			
			message =  lines.toString();
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray appArray;
			
			try {
				appArray = new JSONArray(message.toString());
				for (int i = 0; i < appArray.length() ; i++) {
					JSONObject appJson = appArray.getJSONObject(i);
					String name = appJson.getString("name"); 
					String iconurl = appJson.getString("iconurl");
					String pack = appJson.getString("pack");
					String clas = appJson.getString("clas");
					
					if(!mActivity.getApplicationContext().getPackageName().equals(pack)) {
				    	AppProject appProject = new AppProject(name, iconurl, pack, clas);
				    	appList.add(appProject);
				    }
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		return appList;
	}
	
	//取得新聞列表
	@SuppressLint("SimpleDateFormat")
	public ArrayList<News> getNewsList(int page) {
		ArrayList<News> newsList = new ArrayList<News>(10);
		
		String message = getMessageFromServer("GET", "/api/v1/news.json?page=" + page, null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray newsArray;
			
			try {
				newsArray = new JSONArray(message.toString());
				for(int i = 0; i < newsArray.length(); i++) {
					JSONObject newsJson = newsArray.getJSONObject(i).getJSONObject("news");
					String title = newsJson.getString("title");
					String thumbnailUrl = newsJson.getString("thumbnail_url");
					
					News news = new News();
				
					if(!(newsJson.isNull("created_at"))){
						DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
						Date date = releaseFormatter.parse(newsJson.getString("created_at"));
						news.setReleaseDate(date);
					}
					
					if(!(newsJson.isNull("source")))
						news.setSource(newsJson.getString("source"));
					
					news.setTitle(title);
					news.setThumbnailUrl(thumbnailUrl);
					int type = newsJson.getInt("news_type");
					news.setType(type);
					
					if(type == News.TYPE_LINK){
						if(!(newsJson.isNull("link"))){
							String link = newsJson.getString("link");
							news.setLink(link);
						}
					}
					else if (type == News.TYPE_PIC){
						if(!(newsJson.isNull("picture_url"))){
							String pictureUrl = newsJson.getString("picture_url");
							news.setPictureUrl(pictureUrl);
						}
						if(!(newsJson.isNull("content"))){
							String content = newsJson.getString("content");
							news.setContent(content);
						}
					}
					
					newsList.add(news);
					
					int a = 0;
					a = a + 1;
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			} catch (ParseException e){
				e.printStackTrace();
				return null;
			}
			
		}
		
		return newsList;
	}
	
	public ArrayList<Advertisement> getAdvertisementList(int type) {
		ArrayList<Advertisement> advertisementList = new ArrayList<Advertisement>(10);
		
		String message = getMessageFromServer("GET", "api/v1/advertisements.json?type_id=" + type, null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray advertisementArray;
			
			try {
				advertisementArray = new JSONArray(message.toString());
				for(int i = 0; i < advertisementArray.length(); i++) {
					JSONObject advertisementJson = advertisementArray.getJSONObject(i);
					String imagelUrl = advertisementJson.getString("imageurl");
					String title = "";
					String description = "";
					
					if(advertisementJson.has("title")){
						title = advertisementJson.getString("title");
					}
					
					if(advertisementJson.has("description")){
						description = advertisementJson.getString("description");
					}
					
					Advertisement advertisement = new Advertisement(imagelUrl, title, description);
					advertisementList.add(advertisement);
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
			
		}
		
		return advertisementList;
	}

	public ArrayList<Ticket> getCampaignList() {
		ArrayList<Ticket> campaignList = new ArrayList<Ticket>(10);
		
		String message = getMessageFromServer("GET", "api/v1/campaigns.json", null);
		
		if(message == null) {
			return null;
		}
		else {
			JSONArray campaignArray;
			
			try {
				campaignArray = new JSONArray(message.toString());
				for(int i = 0; i < campaignArray.length(); i++) {
					JSONObject campaignJson = campaignArray.getJSONObject(i);
					int id = campaignJson.getInt("id");
					String imagelUrl = campaignJson.getString("imageurl");
					String title = campaignJson.getString("title");
					String description = campaignJson.getString("description");
					int count = campaignJson.getInt("ticket_count");
					
					Ticket campaign = new Ticket(id, imagelUrl, title, description, count);
					campaignList.add(campaign);
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
			
		}
		
		return campaignList;
	}
	
	public ArrayList<Ticket> getMyTicketList(String email) {
		ArrayList<Ticket> myTicketList = new ArrayList<Ticket>(10);
		
		String message = getMessageFromServer("GET", "api/v1/tickets.json?email=" + email, null);
		
		if(message == null) {
			return myTicketList;
		}
		else {
			JSONArray myTicketArray;
			
			try {
				myTicketArray = new JSONArray(message.toString());
				for(int i = 0; i < myTicketArray.length(); i++) {
					JSONObject myTicketJson = myTicketArray.getJSONObject(i);
					String imagelUrl = myTicketJson.getString("inverse_imageurl");
					String title = myTicketJson.getString("inverse_title");
					String description = myTicketJson.getString("precaution");
					int serialNum = myTicketJson.getInt("serial_num");
					
					Ticket myTicket = new Ticket(-1, imagelUrl, title, description, serialNum);
					myTicketList.add(myTicket);
				}
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
			
		}
		
		return myTicketList;
	}
	
	public Ticket requestTicket(String name, String email, String registerId, int campaignId){
		Ticket ticket = new Ticket();
		
		String message;
		try {
			message = getMessageFromServer("POST", "api/v1/tickets.json?name=" + URLEncoder.encode(name, "UTF-8") +
					"&email=" + email +
					"&registration_id=" + registerId +
					"&campaign_id=" + campaignId
					, null);
		} catch (UnsupportedEncodingException e1) {
			message = getMessageFromServer("POST", "api/v1/tickets.json?name=" + name +
					"&email=" + email +
					"&registration_id=" + registerId +
					"&campaign_id=" + campaignId
					, null);
		}
		
		if(message == null) {
			return null;
		} else if(message.contains("mail duplicate")) {
			return ticket;
		} else {
			try {
				JSONObject ticketJson = new JSONObject(message);
				ticket.setTitle(ticketJson.getString("inverse_title"));
				ticket.setDescription(ticketJson.getString("inverse_title"));
				ticket.setUrl(ticketJson.getString("inverse_imageurl"));
				ticket.setSerialNum(ticketJson.getInt("serial_num"));			
			} 
			catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
			
		}
		
		return ticket;
	}
	
	public boolean postGcm(String regId, Context context) {
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		boolean result = false;
		String deviceId;
		if(tm.getLine1Number() != null && !tm.getLine1Number().equals(""))
			deviceId = tm.getLine1Number();
		else 
			deviceId = tm.getDeviceId();
		
		try{
			DefaultHttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost(urlAddress + "api/v1/devices?registration_id=" 
					+ regId + "&device_id=" + deviceId);
			/*HttpPost httpPost = new HttpPost("http://show.jumplife.com.tw" + "/" + "api/v2/devices.json?registration_id="
					+ regId + "&device_id=" + deviceId);*/
			HttpResponse response = httpClient.execute(httpPost);

			StatusLine statusLine =  response.getStatusLine();
			if (statusLine.getStatusCode() == 200){
				result = true;
			}
		} 
	    catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return result;
		} 
		catch (ClientProtocolException e) {
			e.printStackTrace();
			return result;
		} 
		catch (IOException e){
			e.printStackTrace();
			return result;
		}	
		return result;
	}
	
	public String getDramasHistory(){
		String history = "";
		String message = getMessageFromServer("GET", "api/v1/drama_history.json", null);
		if(message == null) {
			return null;
		}
		else {			
			try {
				JSONArray dramasArray = new JSONArray(message.toString());
				for(int i=0; i<dramasArray.length(); i++) {
					JSONObject dramaObject = dramasArray.getJSONObject(i);
					history = history 
							+ "<b>" + dramaObject.getString("release_date") + "</b>" 
							+ "<p>" + dramaObject.getString("dramas_str") + "</p><br><br><hr>";
				}
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}
		}
		return history;
	}
	
	public void getVersionCode(int[] mVersionCode, String[] msg){
		String message = getMessageFromServer("GET", "api/version_check.json", null);
		if(message == null) {
			return;
		}
		else {			
			try {
				JSONObject jsonObject =  new JSONObject(message.toString());
				mVersionCode[0] = jsonObject.getInt("version_code");
				msg[0] = jsonObject.getString("message");		
			} catch (JSONException e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	public String getUrlAddress() {
		return urlAddress;
	}
	public void setUrlAddress(String urlAddress) {
		this.urlAddress = urlAddress;
	}
	public HttpURLConnection getConnection() {
		return connection;
	}
	public void setConnection(HttpURLConnection connection) {
		this.connection = connection;
	}
	public String getRequestedMethod() {
		return requestedMethod;
	}
	public void setRequestedMethod(String requestedMethod) {
		this.requestedMethod = requestedMethod;
	}
	public int getConnectionTimeout() {
		return connectionTimeout;
	}
	public void setConnectionTimeout(int connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}
	public int getReadTimeout() {
		return readTimeout;
	}
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}
	public boolean isUsercaches() {
		return usercaches;
	}
	public void setUsercaches(boolean usercaches) {
		this.usercaches = usercaches;
	}
	public boolean isDoInput() {
		return doInput;
	}
	public void setDoInput(boolean doInput) {
		this.doInput = doInput;
	}
	public boolean isDoOutput() {
		return doOutput;
	}
	public void setDoOutput(boolean doOutput) {
		this.doOutput = doOutput;
	}
}
