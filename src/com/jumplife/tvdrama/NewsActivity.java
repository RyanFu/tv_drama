package com.jumplife.tvdrama;

import java.util.ArrayList;

import java.util.HashMap;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.google.analytics.tracking.android.TrackedActivity;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.jumplife.sectionlistview.NewsAdapter;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.News;
import com.kuad.KuBanner;
import com.kuad.kuADListener;

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
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class NewsActivity extends TrackedActivity  implements AdWhirlInterface{
	private PullToRefreshListView newsListView;
	private ArrayList<News> newsList;
	private ImageButton imageButtonRefresh;
	private LinearLayout pullMore;
	private NewsAdapter newsAdapter;
	private LoadDataTask loadtask;
	private int page = 1;
	public static final String TAG = "NewsActivity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_news);
		findViews();
		loadtask = new LoadDataTask();
	    if(Build.VERSION.SDK_INT < 11)
	    	loadtask.execute();
        else
        	loadtask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
	    
	    AdTask adTask = new AdTask();
    	adTask.execute();
	}
	
	private void findViews() {
		TextView topbar_text = (TextView)findViewById(R.id.topbar_text);
        topbar_text.setText(getResources().getString(R.string.entertainment_news));
        
		newsListView = (PullToRefreshListView)findViewById(R.id.listview_news);
		pullMore = (LinearLayout)findViewById(R.id.progressBar_pull_more);
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
		
		newsListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				newsListView.setLastUpdatedLabel(DateUtils.formatDateTime(getApplicationContext(),
						System.currentTimeMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE
								| DateUtils.FORMAT_ABBREV_ALL));

				RefreshTask refreshtask = new RefreshTask();
				if(Build.VERSION.SDK_INT < 11)
					refreshtask.execute();
                else
                	refreshtask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
			}
		});
		
		newsListView.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			public void onLastItemVisible() {
				// TODO Auto-generated method stub
				NextPageTask nextPageTask = new NextPageTask();
			    if(Build.VERSION.SDK_INT < 11)
			    	nextPageTask.execute();
		        else
		        	nextPageTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
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
        	if(progressdialogInit != null && progressdialogInit.isShowing())
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
				newsAdapter.notifyDataSetChanged();
				page += 1;
    			// Call onRefreshComplete when the list has been refreshed.
        	}
			newsListView.onRefreshComplete();
        	super.onPostExecute(result);
        }
	}
	
	class NextPageTask  extends AsyncTask<Integer, Integer, String>{

		private ArrayList<News> tmpList;
		
		@Override  
        protected void onPreExecute() {
			pullMore.setVisibility(View.VISIBLE);
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
			pullMore.setVisibility(View.GONE);
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
    	
    	Resources res = getResources();
    	String adwhirlKey = res.getString(R.string.adwhirl_key);
    	
    	RelativeLayout adLayout = (RelativeLayout)findViewById(R.id.ad_layout);
    	
    	AdWhirlManager.setConfigExpireTimeout(1000 * 60); 
        //AdWhirlTargeting.setAge(23);
        //AdWhirlTargeting.setGender(AdWhirlTargeting.Gender.MALE);
        //AdWhirlTargeting.setKeywords("online games gaming");
        //AdWhirlTargeting.setPostalCode("94123");
        AdWhirlTargeting.setTestMode(false);
   		
        AdWhirlLayout adwhirlLayout = new AdWhirlLayout(this, adwhirlKey);	
        
    	adwhirlLayout.setAdWhirlInterface(this);
    	
    	adwhirlLayout.setGravity(Gravity.CENTER_HORIZONTAL);
    	//adwhirlLayout.setLayoutParams();
    	
    	/*TextView ta  = (TextView) findViewById(R.layout.text_view);
       LayoutParams lp = new LayoutParams();
       lp.gravity= Gravity.CENTER_HORIZONTAL; 
       ta.setLayoutParams(lp);
    	 * 
    	 */

	 	
    	adLayout.addView(adwhirlLayout);
   
    }
    
    public void setKuAd() {
    	KuBanner banner;
    	banner = new KuBanner(this);
    	
    	Resources res = getResources();
    	String kuAdKey = res.getString(R.string.kuad_key);
    	
    	banner.setAPID(kuAdKey);
    	banner.appStart();
    	RelativeLayout adLayout = (RelativeLayout)findViewById(R.id.ad_layout);

        // Add the adView to it
    	adLayout.addView(banner);
        
        banner.setkuADListener(new kuADListener(){
        	public void onRecevie(String msg) {
			//成功接收廣告
				Log.i("AdOn", "OnReceviekuAd");
			}
			public void onFailedRecevie(String msg) {
			//失敗接收廣告
				Log.i("AdOn", "OnFailesToReceviekuAd");
			}
			});
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
        super.onDestroy();
        if (loadtask!= null && loadtask.getStatus() != AsyncTask.Status.FINISHED)
        	loadtask.cancel(true);
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
