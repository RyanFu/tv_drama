package com.jumplife.sectionlistview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.jumplife.imageload.ImageLoader;
import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.entity.Section;

import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DramaSectionAdapter extends BaseAdapter{

	private ArrayList<Section> sectionList;
	private Context mContext;
	private ImageLoader imageLoader;
	private String currentSection;
	private int chapterNO;
	private int width;
	private int height;
	private class ItemView {
		ImageView poster;
		TextView name;
	}
	
	public DramaSectionAdapter(Context mContext, ArrayList<Section> sectionList, String currentSection, int chapterNO){
		this.sectionList = sectionList;
		this.mContext = mContext;
		this.chapterNO = chapterNO;
		this.currentSection = currentSection;
		imageLoader=new ImageLoader(mContext);
		width = 80;
		height = 120;
	}
	
	public DramaSectionAdapter(Context mContext, ArrayList<Section> sectionList, int width, int height, String currentSection, int chapterNO){
		this.sectionList = sectionList;
		this.mContext = mContext;
		this.width = width;
		this.height = height;
		this.chapterNO = chapterNO;
		this.currentSection = currentSection;
		
		imageLoader=new ImageLoader(mContext, width);
	}
	
	public int getCount() {
		return sectionList.size();
	}

	public Object getItem(int position) {
		return sectionList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public void setCurrentSection(String currentSection) {
		this.currentSection = currentSection;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemView itemView = new ItemView();;
		
		if (convertView != null) {
			itemView = (ItemView) convertView.getTag();
		} else {
			LayoutInflater myInflater = LayoutInflater.from(mContext);
			convertView = myInflater.inflate(R.layout.gridview_dramasection_item, null);
			itemView.poster = (ImageView)convertView.findViewById(R.id.drama_poster);
			itemView.name = (TextView)convertView.findViewById(R.id.drama_name);
			
			convertView.setTag(itemView);
		}
		String[] currentSectionTmp = null;
		if(currentSection != null)
			currentSectionTmp = currentSection.split(",");
		boolean mark = false;
		if(currentSectionTmp != null && currentSectionTmp.length > 1) {
			if(Integer.parseInt(currentSectionTmp[0].replaceAll(" ","")) == chapterNO)
				mark = true;
		}
			
		itemView.poster.setScaleType(ImageView.ScaleType.FIT_CENTER);
		itemView.name = (TextView)convertView.findViewById(R.id.drama_name);
		itemView.poster.getLayoutParams().height = height;
		itemView.poster.getLayoutParams().width = width;
		
		if(mark && Integer.parseInt(currentSectionTmp[1].replaceAll(" ","")) == position+1) {
			itemView.name.setTextColor(mContext.getResources().getColor(R.color.grid_item_orange));
			itemView.poster.setBackgroundResource(R.drawable.grid_item_dramasection_imageviewbg_mark);
		} else {
			itemView.name.setTextColor(mContext.getResources().getColor(R.color.grid_item_round));
			itemView.poster.setBackgroundResource(R.drawable.grid_item_dramasection_imageviewbg);
		}
		
		if(sectionList.get(position).getUrl() == null ||
    			sectionList.get(position).getUrl().equalsIgnoreCase("") ||
    			sectionList.get(position).getUrl().contains("maplestage"))
			itemView.name.setText("Part" + (position+1) + "(未提供)");
		else
			itemView.name.setText("Part" + (position+1));
		
		if(sectionList.get(position).getUrl().contains("youtube")) {
			String[] youtubeId = new String[2];
			if(sectionList.get(position).getUrl().contains("=")) {
				youtubeId = sectionList.get(position).getUrl().split("\\=");
				if(youtubeId.length > 1) 
					imageLoader.DisplayImage("http://img.youtube.com/vi/" + youtubeId[1] +"/0.jpg", itemView.poster, width);
				else
					imageLoader.DisplayImage(sectionList.get(position).getUrl(), itemView.poster, width);
			} else if(sectionList.get(position).getUrl().contains("embed")) {
				String[] tmp = sectionList.get(position).getUrl().split("embed");
				if(tmp.length > 1) {
					youtubeId = tmp[1].split("\\/");
					if(youtubeId.length > 1) 
						imageLoader.DisplayImage("http://img.youtube.com/vi/" + youtubeId[1] +"/0.jpg", itemView.poster, width);
					else
						imageLoader.DisplayImage(sectionList.get(position).getUrl(), itemView.poster, width);
				}
			}else
				imageLoader.DisplayImage(sectionList.get(position).getUrl(), itemView.poster, width);
		} else if (sectionList.get(position).getUrl().contains("dailymotion")) {
			LoadThumbnailTask task = new LoadThumbnailTask(sectionList.get(position).getUrl(), itemView.poster);
			task.execute();			
		} else {
			imageLoader.DisplayImage(sectionList.get(position).getUrl(), itemView.poster, width);
		}
		
		return convertView;
		/*LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.gridview_dramasection_item, null);
		
		ImageView poster = (ImageView)converView.findViewById(R.id.drama_poster);
		poster.setScaleType(ImageView.ScaleType.FIT_CENTER);
		TextView name = (TextView)converView.findViewById(R.id.drama_name);
		poster.getLayoutParams().height = height;
		poster.getLayoutParams().width = width;
		
		if(sectionList.get(position).getUrl() == null ||
    			sectionList.get(position).getUrl().equalsIgnoreCase("") ||
    			sectionList.get(position).getUrl().contains("maplestage"))
			name.setText("Part" + (position+1) + "(未提供)");
		else
			name.setText("Part" + (position+1));
		
		if(sectionList.get(position).getUrl().contains("youtube")) {
			String[] youtubeId = new String[2];
			if(sectionList.get(position).getUrl().contains("=")) {
				youtubeId = sectionList.get(position).getUrl().split("\\=");
				if(youtubeId.length > 1) 
					imageLoader.DisplayImage("http://img.youtube.com/vi/" + youtubeId[1] +"/0.jpg", poster, width);
				else
					imageLoader.DisplayImage(sectionList.get(position).getUrl(), poster, width);
			} else if(sectionList.get(position).getUrl().contains("embed")) {
				String[] tmp = sectionList.get(position).getUrl().split("embed");
				if(tmp.length > 1) {
					youtubeId = tmp[1].split("\\/");
					if(youtubeId.length > 1) 
						imageLoader.DisplayImage("http://img.youtube.com/vi/" + youtubeId[1] +"/0.jpg", poster, width);
					else
						imageLoader.DisplayImage(sectionList.get(position).getUrl(), poster, width);
				}
			}else
				imageLoader.DisplayImage(sectionList.get(position).getUrl(), poster, width);
		} else if (sectionList.get(position).getUrl().contains("dailymotion")) {
			LoadThumbnailTask task = new LoadThumbnailTask(sectionList.get(position).getUrl(), poster);
			task.execute();			
		} else {
			imageLoader.DisplayImage(sectionList.get(position).getUrl(), poster, width);
		}
		
		return converView;*/
	}
	
	class LoadThumbnailTask extends AsyncTask<Integer, Integer, String> {
		private String link;
		private String dailymotionUrl;
		private ImageView poster;
		
		public LoadThumbnailTask(String link, ImageView poster) {
			this.link = link;
			this.poster = poster;
		}
		
        @Override
        protected void onPreExecute() {
        	String tmp = link.substring(39);
			String[] tmpUrls = tmp.split("\\?");
			String tmpId = tmpUrls[0];
			link = "https://api.dailymotion.com/video/" + tmpId + "?fields=thumbnail_medium_url ";
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	String message = getMessage("GET", link, null);
        	if(message == null)
        		dailymotionUrl = "";
        	else {
	    		try{
	    			JSONObject responseJson = new JSONObject(message);  
	    			if(responseJson.has("thumbnail_medium_url"))
	    				dailymotionUrl = responseJson.getString("thumbnail_medium_url");
	    			else if(responseJson.has("thumbnail_large_url"))
	    				dailymotionUrl = responseJson.getString("thumbnail_large_url");
	    			else if(responseJson.has("thumbnail_small_url"))
	    				dailymotionUrl = responseJson.getString("thumbnail_small_url");
	    			else
	    				dailymotionUrl = "";
	    		} 
	    		catch (JSONException e){
	    			e.printStackTrace();
	    		}
        	}
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
        	imageLoader.DisplayImage(dailymotionUrl, poster, width);
            super.onPostExecute(result);
        }
        
        private String getMessage(String requestMethod, String apiPath, JSONObject json) {
    		URL url;
    		try {
    			url = new URL(apiPath);
    			
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
    				outputStream.write(json.toString().getBytes());
    				outputStream.flush();
    				outputStream.close();
    			}
    			
    			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
    			StringBuilder lines = new StringBuilder();;
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
}
