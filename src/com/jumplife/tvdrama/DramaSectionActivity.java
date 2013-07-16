package com.jumplife.tvdrama;

import java.util.ArrayList;


import org.json.JSONException;
import org.json.JSONObject;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.adapter.DramaSectionAdapter;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.sqlite.SQLiteTvDramaHelper;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.Advertisement;
import com.jumplife.tvdrama.entity.Section;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DramaSectionActivity extends Activity {
    private GridView        sectioGridView;
    private ImageButton     imageButtonRefresh;
    private TextView		textViewFeedback;
    private TextView 		tvChapterNO;
    private TextView 		tvNotify;
    private Boolean			hasAdvertisment = false;
    private static String currentSection = "";
    private int screenWidth;
    private int screenHeight;
    private LoadDataTask    loadTask;
    private TextView topbar_text;
	private ArrayList<Section> sectionList;

	private static int dramaId = 0;
	private static String dramaName = "";
	private static int chapterNo = 0;
	private SharePreferenceIO shIO;
	private DramaSectionAdapter dramaSectionAdapter;
	private static String TAG = "DramaSectionActivity";
	private AdView adView;
	//private AdWhirlLayout adWhirlLayout;
	//private SQLiteTvDrama sqlTvDrama;
	
	private final static int LOADERPLAYER = 100;
	public final static int LOADERPLAYER_CHANGE = 101;
	
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

        setContentView(R.layout.activity_dramasection);
        
        initViews();
        /*
        AdTask adTask = new AdTask();
    	adTask.execute();
        */
        
        this.setAd();
        
        Log.d(TAG, "init view end");
        
        loadTask = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	loadTask.execute();
        else
        	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);    
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
		
        int currentPart = -1;
        if(data != null && data.hasExtra("currentPart"))
        	currentPart = data.getIntExtra("currentPart", -1);
        if(currentPart > 0)
        	currentSection = chapterNo + ", " + currentPart;
        if(currentSection != null && dramaId > 0) {
			shIO.SharePreferenceI("views", true);
			/*sqlTvDrama.updateDramaSectionRecord(dramaId, currentSection);
	    	sqlTvDrama.closeDB();*/
			SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
	        SQLiteDatabase db = instance.getWritableDatabase();
	        instance.updateDramaSectionRecord(db, dramaId, currentSection);
	        db.close();
	        instance.closeHelper();
        }
		
        switch (requestCode) {
        case LOADERPLAYER:
        	if (resultCode == LOADERPLAYER_CHANGE) {    			
        		if(dramaSectionAdapter == null || sectioGridView == null) {
        			if (loadTask!= null && loadTask.getStatus() != AsyncTask.Status.FINISHED) {
        	        	loadTask.closeProgressDilog();
        	        	loadTask.cancel(true);
        	        }
        			loadTask = new LoadDataTask();
                    if(Build.VERSION.SDK_INT < 11)
                    	loadTask.execute();
                    else
                    	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
        		} else {
        			dramaSectionAdapter.setCurrentSection(currentSection);
        			dramaSectionAdapter.notifyDataSetChanged();
        		}
            }
            break;
        }
    }
    
    private void initViews() {
    	shIO = new SharePreferenceIO(this);
    	//sqlTvDrama = new SQLiteTvDrama(this);
    	
    	Bundle extras = getIntent().getExtras();
        if(extras != null) {
        	dramaId = extras.getInt("drama_id");
        	dramaName = extras.getString("drama_name");
        	chapterNo = extras.getInt("chapter_no");
        }
        
        topbar_text = (TextView)findViewById(R.id.topbar_text);
        topbar_text.setText(dramaName);
        
        tvChapterNO = (TextView)findViewById(R.id.textview_chapternumber);
        tvChapterNO.setText(getResources().getString(R.string.episode) + chapterNo + getResources().getString(R.string.no));
        
        tvNotify = (TextView)findViewById(R.id.textview_notify);
        
        sectioGridView = (GridView)findViewById(R.id.gridview_section);
    	imageButtonRefresh = (ImageButton)findViewById(R.id.refresh);
    	textViewFeedback = (TextView)findViewById(R.id.textview_feedback);
    	
    	imageButtonRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Log.d("", "Click imageButtonRefresh");
                loadTask = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	loadTask.execute();
                else
                	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
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
    
    
    private void setPlayerGridClickListener() {
    	sectioGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				/*
				 *  id = -1 is promote.
				 */
				if(sectionList.get(position).getId() != -1) {
					currentSection = chapterNo + ", " + (position + 1);
	            	dramaSectionAdapter.setCurrentSection(currentSection);
	            	dramaSectionAdapter.notifyDataSetChanged();
	            	shIO.SharePreferenceI("views", true);
	            	
	            	if(sectionList.get(position).getUrl() == null ||
	            			sectionList.get(position).getUrl().equalsIgnoreCase("")) {
	            		Builder dialog = new AlertDialog.Builder(DramaSectionActivity.this);
	    		        dialog.setTitle(getResources().getString(R.string.no_link));
	    		        dialog.setMessage(getResources().getString(R.string.use_google_search));
	    		        dialog.setPositiveButton(getResources().getString(R.string.google_search), new DialogInterface.OnClickListener() {
	    		            public void onClick(DialogInterface dialog, int which) {
	    		            	Intent search = new Intent(Intent.ACTION_WEB_SEARCH);  
	    	            		search.putExtra(SearchManager.QUERY, dramaName + " " + getResources().getString(R.string.episode) 
	    	            				+ chapterNo + getResources().getString(R.string.no));  
	    	            		startActivity(search);
	    		            }
	    		        });
	    		        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	    		            public void onClick(DialogInterface dialog, int which) {
	    		            }
	    		        });
	    		        dialog.show();
	            	} else {
	            		ArrayList<String> videoIds = new ArrayList<String>();
	            		int size = sectionList.size();
	            		if(hasAdvertisment) 
	            			size = size-1;
	            		for(int i = 0; i < size; i++)
	            			videoIds.add(sectionList.get(i).getUrl());
	            		
	            		Intent newAct = new Intent(DramaSectionActivity.this, LoaderPlayerActivity.class);
	            		//Intent newAct = new Intent(DramaSectionActivity.this, CustomPlayerActivity.class);            		
	            		newAct.putExtra("currentPart", position + 1);
	            		newAct.putStringArrayListExtra("videoIds", videoIds);
	            		startActivityForResult(newAct, LOADERPLAYER);            		
	            	}
				} else {
					Intent newAct = new Intent();
					newAct.setClass( DramaSectionActivity.this, TicketCenterActivity.class );
	                startActivity( newAct );
				}
			}    		
    	});
    }
    
    private void setNormalGridClickListener() {
    	sectioGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	/*
				 *  id = -1 is promote.
				 */
				if(sectionList.get(position).getId() != -1) {
	            	currentSection = chapterNo + ", " + (position + 1);
	            	dramaSectionAdapter.setCurrentSection(currentSection);
	            	dramaSectionAdapter.notifyDataSetChanged();
	            	shIO.SharePreferenceI("views", true);
	            	Uri uri;
	            	if(sectionList.get(position).getUrl() == null ||
	            			sectionList.get(position).getUrl().equalsIgnoreCase("") ||
	            			sectionList.get(position).getUrl().contains("maplestage")) {
	            		Builder dialog = new AlertDialog.Builder(DramaSectionActivity.this);
	    		        dialog.setTitle(getResources().getString(R.string.no_link));
	    		        dialog.setMessage(getResources().getString(R.string.use_google_search));
	    		        dialog.setPositiveButton(getResources().getString(R.string.google_search), new DialogInterface.OnClickListener() {
	    		            public void onClick(DialogInterface dialog, int which) {
	    		            	Intent search = new Intent(Intent.ACTION_WEB_SEARCH);  
	    	            		search.putExtra(SearchManager.QUERY, dramaName + " " + getResources().getString(R.string.episode) 
	    	            				+ chapterNo + getResources().getString(R.string.no));  
	    	            		startActivity(search);
	    		            }
	    		        });
	    		        dialog.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	    		            public void onClick(DialogInterface dialog, int which) {
	    		            }
	    		        });
	    		        dialog.show();            		 
	            	} else {
	            		if (sectionList.get(position).getUrl().contains("http://www.dailymotion.com/")) {
	            			if(sectionList.get(position).getUrl().contains("embed/video/")) {
		            			String url = sectionList.get(position).getUrl();
		            			url = url.substring(39);
		            			String[] tmpUrls = url.split("\\?");
		            			String tmpId = null;
		            			if(tmpUrls.length > 0)
		            				tmpId = tmpUrls[0];
		            			if(tmpId != null)
		            				sectionList.get(position).setUrl("http://touch.dailymotion.com/video/" + tmpId);
	            			} else {
	            				String url = sectionList.get(position).getUrl();
		            			url = url.substring(33);
		            			String[] tmpUrls = url.split("&");	            			
		            			String tmpId = null;
		            			if(tmpUrls.length > 0)
		            				tmpId = tmpUrls[0];
		            			if(tmpId != null)
		            				sectionList.get(position).setUrl("http://touch.dailymotion.com/video/" + tmpId);
	            			}
	            		}
	            		
	            		uri = Uri.parse(sectionList.get(position).getUrl());
	            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
	            		startActivity(it);
	            	}
            	}
            }
        });
    }
    
    // 設定畫面上的UI
    private void setViews() {
    	
    	textViewFeedback.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				LayoutInflater factory = LayoutInflater.from(DramaSectionActivity.this);
	            View viewFeedBack = factory.inflate(R.layout.dialog_feedback,null);
	            AlertDialog dialogFeedBack = new AlertDialog.Builder(DramaSectionActivity.this).create();
	            dialogFeedBack.setView(viewFeedBack);
	            ((Button)viewFeedBack.findViewById(R.id.dialog_button_feedback)).setOnClickListener(
	                new OnClickListener(){
	                    public void onClick(View v) {
	                    	PackageInfo packageInfo = null;
	                    	int tmpVersionCode = -1;
	            			try {
	            				packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
	            			} catch (NameNotFoundException e) {
	            				// TODO Auto-generated catch block
	            				e.printStackTrace();
	            			}
	            			if(packageInfo != null)
	            				tmpVersionCode = packageInfo.versionCode;
	            			
	                    	Uri uri = Uri.parse("mailto:jumplives@gmail.com");  
	        				String[] ccs={"abooyaya@gmail.com, raywu07@gmail.com, supermfb@gmail.com, form.follow.fish@gmail.com"};
	        				Intent it = new Intent(Intent.ACTION_SENDTO, uri);
	        				it.putExtra(Intent.EXTRA_CC, ccs); 
	        				it.putExtra(Intent.EXTRA_SUBJECT, "[電視連續劇] 建議回饋(" + dramaName + "第" + chapterNo + "集)"); 
	        				it.putExtra(Intent.EXTRA_TEXT, dramaName + "第" + chapterNo + "集 " +
	        						"\n\n發生於 Part___ " +
	        						"\n\n請詳述發生情況 : " +
	        						"\n\n\n\nAPP版本號 : " + tmpVersionCode +
	        						"\n\nAndroid版本號 : " + Build.VERSION.RELEASE +
	        						"\n\n裝置型號 : " + Build.MANUFACTURER + " " + Build.PRODUCT + "(" + Build.MODEL + ")");      
	        				startActivity(it);  
	                    }
	                }
	            );
	            dialogFeedBack.show();				
			}			
		});
    	
    	SharePreferenceIO shIO = new SharePreferenceIO(this);
        boolean shareKey = true;;
        shareKey = shIO.SharePreferenceO("repeat_key", shareKey);
        if(shareKey)
        	setPlayerGridClickListener();
        else
        	setNormalGridClickListener();
    	
    	
    	DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        dramaSectionAdapter = new DramaSectionAdapter(DramaSectionActivity.this, sectionList, screenWidth,
        		screenHeight, currentSection, chapterNo);
        sectioGridView.setAdapter(dramaSectionAdapter);
        sectioGridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        
        Boolean notify = false;
        for(int i=0; i<sectionList.size(); i++) {
        	if(sectionList.get(i).getUrl().contains("www.wat.tv") 
        			|| sectionList.get(i).getUrl().contains("http://106.187.51.230"))
        		notify = true;
        }
        
        if(notify)
        	tvNotify.setVisibility(View.VISIBLE);
        else
        	tvNotify.setVisibility(View.GONE);
    }
    
    private void fetchData() {
    	Log.d(TAG, "load data API begin");
    	DramaAPI dramaAPI = new DramaAPI(this);
    	sectionList = new ArrayList<Section>();
    	sectionList = dramaAPI.getChapterSectionNew(dramaId, chapterNo);
    	
    	ArrayList<Advertisement> advertisements = dramaAPI.getAdvertisementList(2);
    	if(advertisements != null && advertisements.size() > 0) {
	    	Advertisement advertisement = advertisements.get(0);
	    	/*
	    	 * 廣告模擬Grid Item
	    	 */
	    	if(advertisement != null && !advertisement.getUrl().equals("") && !advertisement.getTitle().equals("")) {
		    	Section promote = new Section();
		    	promote.setUrl(advertisement.getUrl());
		    	promote.setTitle(advertisement.getTitle());
		    	sectionList.add(promote);
		    	hasAdvertisment = true;
	    	} else
	    		hasAdvertisment = false;
    	}
    	
    	/*currentSection = sqlTvDrama.getDramaSectionRecord(dramaId);
    	sqlTvDrama.closeDB();*/
    	SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
        SQLiteDatabase db = instance.getReadableDatabase();
        currentSection = instance.getDramaSectionRecord(db, dramaId);
        db.close();
        instance.closeHelper();
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
	          public void onCancel(DialogInterface arg0) {
	        	  closeProgressDilog();
	              LoadDataTask.this.cancel(true);
	              imageButtonRefresh.setVisibility(View.VISIBLE);
	              finish();
	              //DramaSectionActivity.this.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
	          }
	      };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(DramaSectionActivity.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            if(DramaSectionActivity.this != null && !DramaSectionActivity.this.isFinishing() 
        			&& progressdialogInit != null && !progressdialogInit.isShowing())
        		progressdialogInit.show();
            
            Log.d(TAG, "load data onPreExecute");
            
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            fetchData();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
        	closeProgressDilog();

            if (sectionList == null || sectionList.size() < 1) {
            	sectioGridView.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	sectioGridView.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
                setViews();
            }
            Log.d(TAG, "load data onPostExecute");
            
            super.onPostExecute(result);
        }
       
        public void closeProgressDilog() {
        	if(DramaSectionActivity.this != null && !DramaSectionActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }

    class UpdateViewTask extends AsyncTask<Integer, Integer, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
        	if(shIO.SharePreferenceO("views", false)) {
        		DramaAPI dramaAPI = new DramaAPI(DramaSectionActivity.this);
        		//dramaAPI.updateViews(dramaId);
        		dramaAPI.updateViewsWithDevice(dramaId, chapterNo);
        		shIO.SharePreferenceI("views", false);
        	}
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
        	super.onPostExecute(result);
        }

    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
        	UpdateViewTask task = new UpdateViewTask();
            task.execute();            
        }

        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //DramaSectionActivity.this.overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_right);
    }

    @Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
	protected void onDestroy(){
        if (loadTask!= null && loadTask.getStatus() != AsyncTask.Status.FINISHED) {
        	loadTask.closeProgressDilog();
        	loadTask.cancel(true);
        }
        
        if (adView != null) {
            adView.destroy();
        }
        
        super.onDestroy();
    }
    
    @Override
    protected void onPause() {
        super.onPause();
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
	        screenWidth = displayMetrics.widthPixels / 2;
	        screenHeight = (int)(screenWidth * 0.6);
        }else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
        	screenWidth = displayMetrics.widthPixels / 4;
	        screenHeight = (int)(screenWidth * 0.6);
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
			 super.onPostExecute(result);

		 }
    	
    }
}
