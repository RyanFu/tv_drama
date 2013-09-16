package com.jumplife.adapter;

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

import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.entity.Section;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import android.content.Context;
import android.graphics.Bitmap.Config;
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
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private int currentSection;
	private LayoutInflater myInflater;
	private String[] youtubeId;
	private int width;
	private int height;
	private class ItemView {
		ImageView poster;
		TextView name;
	}
	
	public DramaSectionAdapter(Context mContext, ArrayList<Section> sectionList, int width, int height, int currentSection){
		this.sectionList = sectionList;
		this.mContext = mContext;
		this.width = width;
		this.height = height;
		this.currentSection = currentSection;		
		//imageLoader=new ImageLoader(mContext, width);
		myInflater = LayoutInflater.from(mContext);
		
		options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.stub)
		.showImageForEmptyUri(R.drawable.stub)
		.showImageOnFail(R.drawable.stub)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Config.RGB_565)
		.cacheOnDisc(true)
		.displayer(new SimpleBitmapDisplayer())
		.build();
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

	public void setCurrentSection(int currentSection) {
		this.currentSection = currentSection;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemView itemView = new ItemView();
		String postLink = "";
		
		if (convertView != null) {
			itemView = (ItemView) convertView.getTag();
		} else {
			convertView = myInflater.inflate(R.layout.gridview_dramasection_item, null);
			itemView.poster = (ImageView)convertView.findViewById(R.id.drama_poster);
			itemView.name = (TextView)convertView.findViewById(R.id.drama_name);
			
			convertView.setTag(itemView);
		}
			
		itemView.poster.setScaleType(ImageView.ScaleType.FIT_CENTER);
		itemView.poster.getLayoutParams().height = height;
		itemView.poster.getLayoutParams().width = width;
		
		/*
		 * Set Item Data
		 */
		if(sectionList.get(position).getUrl() == null ||
    			sectionList.get(position).getUrl().equalsIgnoreCase("") ||
    			sectionList.get(position).getUrl().contains("maplestage"))
			itemView.name.setText("Part" + (position+1) + mContext.getResources().getString(R.string.no_provide));
		else
			itemView.name.setText("Part" + (position+1));
		
		if(sectionList.get(position).getUrl().contains("youtube")) {
			youtubeId = new String[2];
			if(sectionList.get(position).getUrl().contains("=")) {
				youtubeId = sectionList.get(position).getUrl().split("\\=");
				if(youtubeId.length > 1) 
					postLink = "http://img.youtube.com/vi/" + youtubeId[1] +"/0.jpg";
			} else if(sectionList.get(position).getUrl().contains("embed")) {
				String[] tmp = sectionList.get(position).getUrl().split("embed");
				if(tmp.length > 1) {
					youtubeId = tmp[1].split("\\/");
					if(youtubeId.length > 1) 
						postLink = "http://img.youtube.com/vi/" + youtubeId[1];
				}
			}
		} else if (sectionList.get(position).getUrl().contains("dailymotion")) {
			LoadThumbnailTask task = new LoadThumbnailTask(sectionList.get(position).getUrl(), itemView);
			task.execute();			
		} else {
			postLink = sectionList.get(position).getUrl();
		}

		imageLoader.displayImage(postLink, itemView.poster, options);
		
		/*
		 * Set Item Background and Set Promote Text
		 */
		if(!sectionList.get(position).getTitle().equals(""))
			itemView.name.setText(sectionList.get(position).getTitle());
		else {
			if(currentSection == (position+1)) {
				itemView.name.setTextColor(mContext.getResources().getColor(R.color.grid_item_orange));
				itemView.poster.setBackgroundResource(R.drawable.grid_item_dramasection_imageviewbg_mark);
			} else {
				itemView.name.setTextColor(mContext.getResources().getColor(R.color.grid_item_round));
				itemView.poster.setBackgroundResource(R.drawable.grid_item_dramasection_imageviewbg);
			}
		}
			
		return convertView;
	}
	
	class LoadThumbnailTask extends AsyncTask<Integer, Integer, String> {
		private String link;
		private String dailymotionUrl;
		private ItemView itemView;
		
		public LoadThumbnailTask(String link, ItemView itemView) {
			this.link = link;
			this.itemView = itemView;
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
        	imageLoader.displayImage(dailymotionUrl, itemView.poster, options);
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
