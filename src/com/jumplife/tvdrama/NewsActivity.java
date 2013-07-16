package com.jumplife.tvdrama;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.TrackedActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jumplife.adapter.NewsAdapter;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.News;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class NewsActivity extends TrackedActivity {
	private PullToRefreshListView newsListView;
	private ArrayList<News> newsList;
	private ImageButton imageButtonRefresh;
	private NewsAdapter newsAdapter;
	private LoadDataTask loadtask;
	private int page = 1;
	private AdView adView;
	//private AdWhirlLayout adWhirlLayout;
	public static final String TAG = "NewsActivity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		JSONObject crittercismConfig = new JSONObject();
        try {
        	crittercismConfig.put("delaySendingAppLoad", true); // send app load data with Crittercism.sendAppLoadData()
            crittercismConfig.put("shouldCollectLogcat", true); // send logcat data for devices with API Level 16 and higher
        	crittercismConfig.put("includeVersionCode", true); // include version code in version name
        }
        catch (JSONException je){}
        //Crittercism.init(getApplicationContext(), "51ccf765558d6a0c25000003", crittercismConfig);
        
	    setContentView(R.layout.activity_news);
	    
		findViews();
		
		/*
		AdTask adTask = new AdTask();
    	adTask.execute();
		*/
		this.setAd();
		
    	loadtask = new LoadDataTask();
	    if(Build.VERSION.SDK_INT < 11)
	    	loadtask.execute();
        else
        	loadtask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
	    
	    
	}
	
	private void findViews() {
		TextView topbar_text = (TextView)findViewById(R.id.topbar_text);
        topbar_text.setText(getResources().getString(R.string.entertainment_news));
        
		newsListView = (PullToRefreshListView)findViewById(R.id.listview_news);
		imageButtonRefresh = (ImageButton)findViewById(R.id.refresh);
		imageButtonRefresh.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				loadtask = new LoadDataTask();
				if(Build.VERSION.SDK_INT < 11)
					loadtask.execute();
                else
                	loadtask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
			}			
		});
	}
	
	private void setView() {
		if(newsList != null && newsList.size() > 0) {
			newsListView.setVisibility(View.VISIBLE);
			imageButtonRefresh.setVisibility(View.GONE);
		} else {
			newsListView.setVisibility(View.GONE);
			imageButtonRefresh.setVisibility(View.VISIBLE);
		}
	}
	
	private void FetchData() {
		DramaAPI dramaAPI = new DramaAPI("http://106.187.101.252");
		newsList = dramaAPI.getNewsList(page);
	}
	
	private void setListener() {
		newsListView.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View view, int position, long id)
			{
				HashMap<String, String> parameters = new HashMap<String, String>();
				int pos = position - 1;
				News news = newsList.get(pos);
				
				if(news.getType() == News.TYPE_LINK) {
					parameters.put("TYPE", "LINK");
					
					Uri uri = Uri.parse(news.getLink());  
		    		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		    		startActivity(it);
		    		
				}
				else if(news.getType() == News.TYPE_PIC) {
					parameters.put("TYPE", "PIC");
					Intent intent = new Intent();
					intent.putExtra("picture_url", news.getPictureUrl());
					intent.putExtra("content", news.getContent());
					intent.setClass( NewsActivity.this, NewsPic.class );
			        startActivity( intent );
				}
			}
		});
		
		newsListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				
				RefreshTask task = new RefreshTask();
				if(Build.VERSION.SDK_INT < 11)
					task.execute();
		        else
		        	task.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
			}
		
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				String label = DateUtils.formatDateTime(getApplicationContext(), System.currentTimeMillis(),
						DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_ABBREV_ALL);

				// Update the LastUpdatedLabel
				refreshView.getLoadingLayoutProxy().setLastUpdatedLabel(label);
				
				NextPageTask task = new NextPageTask();
				if(Build.VERSION.SDK_INT < 11)
					task.execute();
				else
					task.executeOnExecutor(NextPageTask.THREAD_POOL_EXECUTOR, 0);
		     }
		});
	}  

	private void setListAdatper() {
		newsAdapter = new NewsAdapter(NewsActivity.this, newsList);
		newsListView.setAdapter(newsAdapter);
	}
	
	class LoadDataTask extends AsyncTask<Integer, Integer, String>{  
        
    	private ProgressDialog progressdialogInit;
    	private OnCancelListener cancelListener = new OnCancelListener(){
		    public void onCancel(DialogInterface arg0){
		    	LoadDataTask.this.cancel(true);
		    	newsListView.setVisibility(View.GONE);
				imageButtonRefresh.setVisibility(View.VISIBLE);
		    }
    	};

    	@Override  
        protected void onPreExecute() {
    		progressdialogInit= new ProgressDialog(NewsActivity.this);
        	progressdialogInit.setTitle("Load");
        	progressdialogInit.setMessage("Loading…");
        	progressdialogInit.setOnCancelListener(cancelListener);
        	progressdialogInit.setCanceledOnTouchOutside(false);
        	progressdialogInit.show();
        	newsListView.setVisibility(View.VISIBLE);
			imageButtonRefresh.setVisibility(View.GONE);
			super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	FetchData();
            return "progress end";  
        }  
  
        @Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {
        	if(NewsActivity.this != null && !NewsActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        	if(newsList != null && newsList.size() > 0){
        		Log.d(TAG, "List Size: " + newsList.size());
        		setListAdatper();
            	setListener();
            	page += 1;
        	}
        	super.onPostExecute(result);
        }
    }
	
	class RefreshTask  extends AsyncTask<Integer, Integer, String>{

		@Override  
        protected void onPreExecute() {
			page = 1;
        	super.onPreExecute();  
        }  
        @Override
		protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	FetchData();
			return "progress end";
		}
		@Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        } 
		protected void onPostExecute(String result) {
			setView();		
			if(newsList != null && newsList.size() > 0){
        		setListAdatper();
            	setListener();
            	page += 1;
        	}
			newsListView.onRefreshComplete();
        	super.onPostExecute(result);
        }
	}
	
	class NextPageTask  extends AsyncTask<Integer, Integer, String>{

		private ArrayList<News> tmpList;
		
		@Override  
        protected void onPreExecute() {
			super.onPreExecute();  
        }  
        @Override
		protected String doInBackground(Integer... params) {
        	DramaAPI dramaAPI = new DramaAPI("http://106.187.101.252");
        	tmpList = dramaAPI.getNewsList(page);
        	return "progress end";
		}
		@Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        } 
		protected void onPostExecute(String result) {
			if(tmpList != null && tmpList.size() > 0){
				newsList.addAll(tmpList);
				newsAdapter.notifyDataSetChanged();
				page += 1;
    			// Call onRefreshComplete when the list has been refreshed.
        	}
			super.onPostExecute(result);
        }
	}
	
	public void setAd() {
		
		RelativeLayout adLayout = (RelativeLayout)findViewById(R.id.ad_layout);
    	Resources res = getResources();
    	String admoblKey = res.getString(R.string.admob_key);
    	
    	// Create the adView
    	adView = new AdView(this, AdSize.BANNER, admoblKey);

    	// Add the adView to it
    	adLayout.addView(adView);
    	
    	// Initiate a generic request to load it with an ad
        adView.loadAd(new AdRequest());
		/*
		Resources res = getResources();
    	String adwhirlKey = res.getString(R.string.adwhirl_key);
    	
    	RelativeLayout adLayout = (RelativeLayout)findViewById(R.id.ad_layout);
    	
    	AdWhirlManager.setConfigExpireTimeout(1000 * 30); 

        AdWhirlTargeting.setTestMode(false);
   		
        adWhirlLayout = new AdWhirlLayout(this, adwhirlKey);	
        
        adWhirlLayout.setAdWhirlInterface(this);
    	
        adWhirlLayout.setGravity(Gravity.CENTER_HORIZONTAL);
	 	
    	adLayout.addView(adWhirlLayout);
   		*/
    }
    
	class AdTask extends AsyncTask<Integer, Integer, String> {
		@Override
		protected String doInBackground(Integer... arg0) {
			
			return null;
		}	
		 @Override  
	     protected void onPostExecute(String result) {
			 setAd();
			 super.onPostExecute(result);
		 }
    	
    }
	@Override
	protected void onStart() {
	    super.onStart();
	}
	@Override
	protected void onDestroy(){
        if (loadtask!= null && loadtask.getStatus() != AsyncTask.Status.FINISHED)
        	loadtask.cancel(true);
        
        if (adView != null) {
            adView.destroy();
        }
        
        super.onDestroy();
	}
	@Override
	protected void onStop() {
	    super.onStop();
	}
	@Override
	protected void onResume(){
        super.onResume();
	}
	public void adWhirlGeneric()
	{
		// TODO Auto-generated method stub
		
	}
}
