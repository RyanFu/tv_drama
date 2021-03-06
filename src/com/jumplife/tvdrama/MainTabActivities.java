package com.jumplife.tvdrama;

import java.util.HashMap;


import org.json.JSONException;
import org.json.JSONObject;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.tvdrama.api.DramaAPI;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class MainTabActivities extends TabActivity {
	
	private TabHost tabHost;
	private TabHost.TabSpec spec;  // Resusable TabSpec for each tab
	private TextView topbar_text;
	private LinearLayout topbarLayout;
	private int openCount;
	private int version;
	private LoadPromoteTask loadPromoteTask;
	//private AdWhirlLayout adWhirlLayout;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private AdView adView;
	
	public static String TAG = "MainTabActivities";
	
	/** Called when the activity is first created. */
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
        
        setContentView(R.layout.activity_maintab);
        
        long startTime = System.currentTimeMillis();
        topbarLayout = (LinearLayout) findViewById(R.id.topbar);
        
        topbar_text = (TextView)findViewById(R.id.topbar_text);
        topbar_text.setText(getResources().getString(R.string.app_name));
        
        // set unread count
        LocalBroadcastManager.getInstance(this).registerReceiver(mInfoReceiver, new IntentFilter("info_center"));
        
        
        tabHost = getTabHost();  // The activity TabHost
        tabHost.setup();
               
        tabTvChannel();
        tabMyFavorite();
        tabSearch();
        tabAboutUs();
        
        tabHost.setCurrentTab(0);
        
        topbarLayout.setVisibility(View.GONE);	
        openCount = TvDramaApplication.shIO.getInt("opencount", 0);
        version = TvDramaApplication.shIO.getInt("version", 0);
        
        options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.stub)
		.showImageForEmptyUri(R.drawable.stub)
		.showImageOnFail(R.drawable.stub)
		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		.cacheOnDisc()
		.cacheInMemory()
		.displayer(new SimpleBitmapDisplayer())
		.build();
        
        /*
        AdTask adTask = new AdTask();
    	adTask.execute();
        */
        this.setAd();
        
        loadPromoteTask = new LoadPromoteTask();
    	if(openCount > 5) {
        	loadPromoteTask.execute();
        	openCount = 0;
        }
        openCount += 1;
        TvDramaApplication.shIO.edit().putInt("opencount", openCount).commit();
    	long endTime = System.currentTimeMillis();
    	Log.e(TAG, "sample method took %%%%%%%%%%%%%%%%%%%%%%%%%%%%"+(endTime-startTime)+"ms");
    	
    	setTabClickLog();
    	
    }
    
    private BroadcastReceiver mInfoReceiver = new BroadcastReceiver() {    	  
		@Override
		public void onReceive(Context arg0, Intent intent) {
			//int unreadCount = intent.getIntExtra("unread_count", 0);
		}
	};
    
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
    
    
    public void setTabClickLog() {
    	tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				if(tabId.equalsIgnoreCase("tab1")) {
					EasyTracker.getTracker().trackEvent("主選單", "點擊", "連續劇", (long)0);
					topbarLayout.setVisibility(View.GONE);		
				}
				else if(tabId.equalsIgnoreCase("tab2")) {
					EasyTracker.getTracker().trackEvent("主選單", "點擊", "我的收藏", (long)0);
					topbarLayout.setVisibility(View.VISIBLE);		
				}
				else if(tabId.equalsIgnoreCase("tab3")) {
					EasyTracker.getTracker().trackEvent("主選單", "點擊", "戲劇搜尋", (long)0);
					topbarLayout.setVisibility(View.VISIBLE);		
				}
				else if(tabId.equalsIgnoreCase("tab4")) {
					EasyTracker.getTracker().trackEvent("主選單", "點擊", "關於我們", (long)0);
					topbarLayout.setVisibility(View.VISIBLE);		
				}
			}
		});
    }
    
    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }
    
    @Override
	protected void onDestroy(){
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mInfoReceiver);
        if (loadPromoteTask!= null && loadPromoteTask.getStatus() != AsyncTask.Status.FINISHED) {
        	loadPromoteTask.closeProgressDilog();
        	loadPromoteTask.cancel(true);
        }

        if (adView != null) {
            adView.destroy();
        }
        
        super.onDestroy();
	}
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	super.onActivityResult(requestCode, resultCode, data);
    	Log.d(TAG, "onActivityResult");
    }
     
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN){
			return true;
	    } else
	    	return super.onKeyDown(keyCode, event);
	}
    
    private void tabTvChannel() {
    	
    	View ActivitysTab = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
    	ImageView image = (ImageView) ActivitysTab.findViewById(R.id.imageview_tabicon);
        image.setImageResource(R.drawable.tab_imageview_tvchannel);
		TextView ActivitysTabLabel = (TextView) ActivitysTab.findViewById(R.id.textview_tabicon);
		ActivitysTabLabel.setText(getResources().getString(R.string.episodes));
		
		// Create an Intent to launch an Activity for the tab (to be reused)
		// Initialize a TabSpec for each tab and add it to the TabHost
        //Intent intentTvChannel = new Intent().setClass(this, TvChannelWaterFallActivity.class);
		//Bundle extras = getIntent().getExtras();
        Intent intentTvChannel = new Intent().setClass(this, TvChannelViewPagerActivity.class);
        /*intentTvChannel.putExtra("type_id", extras.getInt("type_id", 0));
        intentTvChannel.putExtra("sort_id", extras.getInt("sort_id", 0));*/
        spec = tabHost.newTabSpec("tab1")
        				.setIndicator(ActivitysTab)
        				.setContent(intentTvChannel);
        tabHost.addTab(spec);
    }
    
    private void tabMyFavorite() {
    	View MyFavoriteTab = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
    	ImageView image = (ImageView) MyFavoriteTab.findViewById(R.id.imageview_tabicon);
        image.setImageResource(R.drawable.tab_imageview_myfavorite);
		TextView ActivitysTabLabel = (TextView) MyFavoriteTab.findViewById(R.id.textview_tabicon);
		ActivitysTabLabel.setText(getResources().getString(R.string.favorite));
        
        Intent intentTheater = new Intent().setClass(this, MyFavoriteWaterFallActivity.class);
        intentTheater.putExtra("theaterType", 0);
        spec = tabHost.newTabSpec("tab2")
        				.setIndicator(MyFavoriteTab)
        				.setContent(intentTheater);
        tabHost.addTab(spec);
    }
    
    private void tabSearch() {
    	View SearchTab = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
    	ImageView image = (ImageView) SearchTab.findViewById(R.id.imageview_tabicon);
        image.setImageResource(R.drawable.tab_imageview_search);
		TextView ActivitysTabLabel = (TextView) SearchTab.findViewById(R.id.textview_tabicon);
		ActivitysTabLabel.setText(getResources().getString(R.string.search));
        
        Intent intentNewRound = new Intent().setClass(this, SearchDramaActivity.class);
        spec = tabHost.newTabSpec("tab3")
                		.setIndicator(SearchTab)
                		.setContent(intentNewRound);
        tabHost.addTab(spec);
    }

    private void tabAboutUs() {
    	View AboutUsTab = LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
    	ImageView image = (ImageView) AboutUsTab.findViewById(R.id.imageview_tabicon);
        image.setImageResource(R.drawable.tab_imageview_about);
		TextView ActivitysTabLabel = (TextView) AboutUsTab.findViewById(R.id.textview_tabicon);
		ActivitysTabLabel.setText(getResources().getString(R.string.about_us));
        
        Intent intentNewRound = new Intent().setClass(this, AboutUsActivity.class);
        spec = tabHost.newTabSpec("tab4")
                		.setIndicator(AboutUsTab)
                		.setContent(intentNewRound);
        tabHost.addTab(spec);
    }
    
    
    
    class LoadPromoteTask extends AsyncTask<Integer, Integer, String>{  
        
		private String[] promotion = null;
        private ProgressDialog progressdialogInit;
        private AlertDialog dialogPromotion;
        
        private OnCancelListener cancelListener = new OnCancelListener(){
		    public void onCancel(DialogInterface arg0){
		    	LoadPromoteTask.this.cancel(true);
		    }
    	};

    	@Override  
        protected void onPreExecute() {
    		progressdialogInit= new ProgressDialog(MainTabActivities.this);
        	progressdialogInit.setTitle("Load");
        	progressdialogInit.setMessage("Loading…");
        	progressdialogInit.setOnCancelListener(cancelListener);
        	progressdialogInit.setCanceledOnTouchOutside(false);
        	if(progressdialogInit != null && !progressdialogInit.isShowing())
        		progressdialogInit.show();
			super.onPreExecute();  
        }  
    	
		@Override  
        protected String doInBackground(Integer... params) {
			DramaAPI dramaAPI = new DramaAPI();
			promotion = new String[5];
			promotion = dramaAPI.getPromotion();
			return "progress end";  
        }  
  
        @Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {
        	closeProgressDilog();
        	
        	if(promotion != null && !promotion[1].equals("null") && Integer.valueOf(promotion[4]) > version) {
	        	View viewPromotion;
	            LayoutInflater factory = LayoutInflater.from(MainTabActivities.this);
	            viewPromotion = factory.inflate(R.layout.dialog_promotion,null);
	            dialogPromotion = new AlertDialog.Builder(MainTabActivities.this).create();
	            dialogPromotion.setView(viewPromotion);
	            ImageView imageView = (ImageView)viewPromotion.findViewById(R.id.imageView1);
	            TextView textviewTitle = (TextView)viewPromotion.findViewById(R.id.textView1);
	            TextView textviewDescription = (TextView)viewPromotion.findViewById(R.id.textView2);
				if(!promotion[0].equals("null"))
					imageLoader.displayImage(promotion[0], imageView, options);
				else
					imageView.setVisibility(View.GONE);
				if(!promotion[2].equals("null"))
					textviewTitle.setText(promotion[2]);
				else
					textviewTitle.setVisibility(View.GONE);
				if(!promotion[3].equals("null"))
					textviewDescription.setText(promotion[3]);
				else
					textviewDescription.setVisibility(View.GONE);
	            dialogPromotion.setOnKeyListener(new OnKeyListener(){
	                public boolean onKey(DialogInterface dialog, int keyCode,
	                        KeyEvent event) {
	                	TvDramaApplication.shIO.edit().putInt("version", Integer.valueOf(promotion[4])).commit();
	                    if(KeyEvent.KEYCODE_BACK==keyCode)
	                    	if(dialogPromotion != null && dialogPromotion.isShowing())
	                    		dialogPromotion.cancel();
	                    return false;
	                }
	            });
	            ((Button)viewPromotion.findViewById(R.id.button2))
	            .setOnClickListener(
	                new OnClickListener(){
	                    public void onClick(View v) {
	                        //取得文字方塊中的關鍵字字串
	                    	TvDramaApplication.shIO.edit().putInt("version", Integer.valueOf(promotion[4])).commit();
	                    	if(dialogPromotion != null && dialogPromotion.isShowing())
	                    		dialogPromotion.cancel();
	                    	
	                    	HashMap<String, String> parameters = new HashMap<String, String>();
	                    	parameters.put("LINK", promotion[1]);
	    					
	                    	Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(promotion[1]));
	                    	MainTabActivities.this.startActivity(intent);
	                    }
	                }
	            );
	            dialogPromotion.setCanceledOnTouchOutside(false);
	            dialogPromotion.show();
        	}
	       	super.onPostExecute(result);  
        } 
        
        public void closeProgressDilog() {
        	if(MainTabActivities.this != null && !MainTabActivities.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }   
    }
    
    class AdTask extends AsyncTask<Integer, Integer, String> {
		@Override
		protected String doInBackground(Integer... arg0) {
			
			return null;
		}
		
		 @Override  
	     protected void onPostExecute(String result) {
			 setAd();
			 //setKuAd();
			 super.onPostExecute(result);

		 }
    	
    }
}