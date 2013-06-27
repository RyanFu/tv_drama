package com.jumplife.tvdrama;

import java.util.ArrayList;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.adwhirl.AdWhirlLayout.ViewAdRunnable;
import com.google.analytics.tracking.android.EasyTracker;
import com.hodo.HodoADView;
import com.hodo.listener.HodoADListener;
import com.jumplife.sectionlistview.DramaSectionAdapter;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.sqlite.SQLiteTvDrama;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.Section;
import com.jumplife.youtubeapi.PlayerControlsActivity;
import com.kuad.KuBanner;
import com.kuad.kuADListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DramaSectionActivity_1305210935 extends Activity implements AdWhirlInterface{
    private GridView        sectioGridView;
    private ImageButton     imageButtonRefresh;
    private TextView		textViewFeedback;
    private TextView 		tvChapterNO;
    private TextView 		tvNotify;
    private String currentSection = "";
    private int screenWidth;
    private int screenHeight;
    private LoadDataTask    loadTask;
    private TextView topbar_text;
	private ArrayList<Section> sectionList;
	private String youtubeIds = null;

	private int dramaId = 0;
	private String dramaName = "";
	private int chapterNo = 0;
	private SharePreferenceIO shIO;
	private DramaSectionAdapter dramaSectionAdapter;
	private Boolean developerMode = false;
	private static String TAG = "DramaSectionActivity";
	private AdWhirlLayout adWhirlLayout;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_dramasection);
        
        Log.d(TAG, "init view begin");
        
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

    private void initViews() {
    	shIO = new SharePreferenceIO(this);
    	
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
		Resources res = getResources();
    	String adwhirlKey = res.getString(R.string.adwhirl_key);
    	
    	RelativeLayout adLayout = (RelativeLayout)findViewById(R.id.ad_layout);
    	
    	AdWhirlManager.setConfigExpireTimeout(1000 * 30); 

        AdWhirlTargeting.setTestMode(false);
   		
        adWhirlLayout = new AdWhirlLayout(this, adwhirlKey);	
        
        adWhirlLayout.setAdWhirlInterface(this);
    	
        adWhirlLayout.setGravity(Gravity.CENTER_HORIZONTAL);
	 	
    	adLayout.addView(adWhirlLayout);
   
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
    
    private void setGridClickListener() {
    	sectioGridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				currentSection = chapterNo + ", " + (position + 1);
            	dramaSectionAdapter.setCurrentSection(currentSection);
            	dramaSectionAdapter.notifyDataSetChanged();
            	shIO.SharePreferenceI("views", true);
            	
            	if(sectionList.get(position).getUrl() == null ||
            			sectionList.get(position).getUrl().equalsIgnoreCase("")) {
            		Builder dialog = new AlertDialog.Builder(DramaSectionActivity_1305210935.this);
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
            		
            	}
			}    		
    	});
    }
    
    // 設定畫面上的UI
    private void setViews() {
    	
    	textViewFeedback.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				LayoutInflater factory = LayoutInflater.from(DramaSectionActivity_1305210935.this);
	            View viewFeedBack = factory.inflate(R.layout.dialog_feedback,null);
	            AlertDialog dialogFeedBack = new AlertDialog.Builder(DramaSectionActivity_1305210935.this).create();
	            dialogFeedBack.setView(viewFeedBack);
	            ((Button)viewFeedBack.findViewById(R.id.dialog_button_feedback)).setOnClickListener(
	                new OnClickListener(){
	                    public void onClick(View v) {
	                    	Uri uri = Uri.parse("mailto:jumplives@gmail.com");  
	        				String[] ccs={"abooyaya@gmail.com, raywu07@gmail.com, supermfb@gmail.com, form.follow.fish@gmail.com"};
	        				Intent it = new Intent(Intent.ACTION_SENDTO, uri);
	        				it.putExtra(Intent.EXTRA_CC, ccs); 
	        				it.putExtra(Intent.EXTRA_SUBJECT, "[電視連續劇] 建議回饋(" + dramaName + "第" + chapterNo + "集)"); 
	        				it.putExtra(Intent.EXTRA_TEXT, dramaName + "第" + chapterNo + "集 \n\n發生於 Part___ \n\n請詳述發生情況 : ");      
	        				startActivity(it);  
	                    }
	                }
	            );
	            dialogFeedBack.show();				
			}			
		});
    	sectioGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	currentSection = chapterNo + ", " + (position + 1);
            	dramaSectionAdapter.setCurrentSection(currentSection);
            	dramaSectionAdapter.notifyDataSetChanged();
            	shIO.SharePreferenceI("views", true);
            	Uri uri;
            	if(sectionList.get(position).getUrl() == null ||
            			sectionList.get(position).getUrl().equalsIgnoreCase("") ||
            			sectionList.get(position).getUrl().contains("maplestage")) {
            		Builder dialog = new AlertDialog.Builder(DramaSectionActivity_1305210935.this);
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
            		/*if (sectionList.get(position).getUrl().contains("http://touch.dailymotion.com/video/")) {
            			String url = sectionList.get(position).getUrl();
            			url = url.substring(39);
            			String[] tmpUrls = url.split("\\?");
            			String tmpId = null;
            			if(tmpUrls.length > 0)
            				tmpId = tmpUrls[0];
            			if(tmpId != null)
            				sectionList.get(position).setUrl("http://www.dailymotion.com/embed/video/" + tmpId);
            		}*/
            		
            		uri = Uri.parse(sectionList.get(position).getUrl());
            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
            		startActivity(it);
            		/*if (sectionList.get(position).getUrl().contains("http://www.dailymotion.com/embed/video/")) {
            			String url = sectionList.get(position).getUrl();
            			url = url.substring(39);
            			String[] tmpUrls = url.split("\\?");
            			String tmpId = null;
            			if(tmpUrls.length > 0)
            				tmpId = tmpUrls[0];
            			if(tmpId != null)
            				sectionList.get(position).setUrl("http://touch.dailymotion.com/video/" + tmpId);
            			uri = Uri.parse(sectionList.get(position).getUrl());
	            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
	            		startActivity(it);
            		} else if(youtubeIds != null) {
            			Intent newAct = new Intent();
    					newAct.putExtra("youtube_ids", youtubeIds);
    					newAct.putExtra("youtube_index", position);
    					newAct.putExtra("youtube_link", sectionList.get(position).getUrl());
    	                newAct.setClass(DramaSectionActivity.this, PlayerControlsActivity.class);
    					//newAct.setClass(VarietySectionActivity.this, YouTubePlayerActivity.class);
    	                startActivity(newAct);
        			} else {	            			
	            		uri = Uri.parse(sectionList.get(position).getUrl());
	            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
	            		startActivity(it);
        			}*/
            	}
            	//new UpdateDramaSectionRecordTask().execute();
            	SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(DramaSectionActivity_1305210935.this);
    			sqlTvDrama.updateDramaSectionRecord(dramaId, currentSection);
    			sqlTvDrama.closeDB();
            }
        });
    	
    	if(developerMode) {
	    	sectioGridView.setOnItemLongClickListener(new OnItemLongClickListener() {
	            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					shIO.SharePreferenceI("views", true);
	            	Uri uri;
	            	if(sectionList.get(position).getUrl() == null ||
	            			sectionList.get(position).getUrl().equalsIgnoreCase("") ||
	            			sectionList.get(position).getUrl().contains("maplestage")) {
	            		Builder dialog = new AlertDialog.Builder(DramaSectionActivity_1305210935.this);
	    		        dialog.setTitle(DramaSectionActivity_1305210935.this.getResources().getString(R.string.no_link));
	    		        dialog.setMessage(DramaSectionActivity_1305210935.this.getResources().getString(R.string.use_google_search));
	    		        dialog.setPositiveButton(DramaSectionActivity_1305210935.this.getResources().getString(R.string.google_search)
	    		        		, new DialogInterface.OnClickListener() {
	    		            public void onClick(DialogInterface dialog, int which) {
	    		            	Intent search = new Intent(Intent.ACTION_WEB_SEARCH);  
	    	            		search.putExtra(SearchManager.QUERY, dramaName + " " + getResources().getString(R.string.episode) 
	    	            				+ chapterNo + getResources().getString(R.string.no));  
	    	            		startActivity(search);
	    		            }
	    		        });
	    		        dialog.setNegativeButton(DramaSectionActivity_1305210935.this.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
	    		            public void onClick(DialogInterface dialog, int which) {
	    		            }
	    		        });
	    		        dialog.show();
	    		        return true;
	            	} else {
	            		if (sectionList.get(position).getUrl().contains("http://www.dailymotion.com/embed/video/")) {
	            			String url = sectionList.get(position).getUrl();
	            			url = url.substring(39);
	            			String[] tmpUrls = url.split("\\?");
	            			String tmpId = null;
	            			if(tmpUrls.length > 0)
	            				tmpId = tmpUrls[0];
	            			if(tmpId != null)
	            				sectionList.get(position).setUrl("http://touch.dailymotion.com/video/" + tmpId);
	            			uri = Uri.parse(sectionList.get(position).getUrl());
		            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		            		startActivity(it);
		    		        return true;
	            		} else if(youtubeIds != null) {
	            			Intent newAct = new Intent();
	    					newAct.putExtra("youtube_ids", youtubeIds);
	    					newAct.putExtra("youtube_index", position);
	    					newAct.putExtra("youtube_link", sectionList.get(position).getUrl());
	    					//newAct.setClass(DramaSectionActivity.this, FullscreenDemoActivity.class);
	    	                newAct.setClass(DramaSectionActivity_1305210935.this, PlayerControlsActivity.class);
	    					//newAct.setClass(VarietySectionActivity.this, YouTubePlayerActivity.class);
	    	                startActivity(newAct);
	        		        return true;
	        			} else {	            			
		            		uri = Uri.parse(sectionList.get(position).getUrl());
		            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		            		startActivity(it);
		    		        return true;
	        			}
	            	}
				}
	        });
    	}
    	
    	DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        dramaSectionAdapter = new DramaSectionAdapter(DramaSectionActivity_1305210935.this, sectionList, screenWidth,
        		screenHeight, currentSection, chapterNo);
        sectioGridView.setAdapter(dramaSectionAdapter);
        
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

    private void checkYoutubeId() {
    	String youtubeIdstmp = "";
    	Boolean isAllYoutube = true;
    	for(int i=0; i<sectionList.size(); i++) {
    		if(sectionList.get(i).getUrl().contains("youtube")) {
    			String[] youtubeId = new String[2];
    			if(sectionList.get(i).getUrl().contains("=")) {
    				youtubeId = sectionList.get(i).getUrl().split("\\=");
    				if(youtubeId.length > 1)
    					youtubeIdstmp = youtubeIdstmp + youtubeId[1] + ",";
    				else 
    					isAllYoutube = false;
    			} else if(sectionList.get(i).getUrl().contains("embed")) {
    				String[] tmp = sectionList.get(i).getUrl().split("embed");
    				if(tmp.length > 1) {
    					youtubeId = tmp[1].split("\\/");
    					if(youtubeId.length > 1)
        					youtubeIdstmp = youtubeIdstmp + youtubeId[1] + ",";
    					else 
        					isAllYoutube = false;
    				}
    			}
    		}else
    			isAllYoutube = false;
    	}
    	if(isAllYoutube && youtubeIdstmp.length()>1)
    		youtubeIds = youtubeIdstmp.substring(0, youtubeIdstmp.length()-1);
    	else
    		youtubeIds = null;
    }
    
    private void fetchData() {
    	Log.d(TAG, "load data API begin");
    	DramaAPI dramaAPI = new DramaAPI(this);
    	sectionList = new ArrayList<Section>();
    	sectionList = dramaAPI.getChapterSectionNew(dramaId, chapterNo);
    	SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(this);
    	currentSection = sqlTvDrama.getDramaSectionRecord(dramaId);
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
	          public void onCancel(DialogInterface arg0) {
	              LoadDataTask.this.cancel(true);
	              imageButtonRefresh.setVisibility(View.VISIBLE);
	              finish();
	          }
	      };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(DramaSectionActivity_1305210935.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            if(DramaSectionActivity_1305210935.this != null && !DramaSectionActivity_1305210935.this.isFinishing() 
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
        	if(DramaSectionActivity_1305210935.this != null && !DramaSectionActivity_1305210935.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();

            if (sectionList == null) {
            	sectioGridView.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	sectioGridView.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
                checkYoutubeId();
                setViews();
            }
            Log.d(TAG, "load data onPostExecute");
            
            super.onPostExecute(result);
        }
       
        public void closeProgressDilog() {
        	if(DramaSectionActivity_1305210935.this != null && !DramaSectionActivity_1305210935.this.isFinishing() 
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
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	if(shIO.SharePreferenceO("views", false)) {
        		DramaAPI dramaAPI = new DramaAPI(DramaSectionActivity_1305210935.this);
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
    
    class UpdateDramaSectionRecordTask extends AsyncTask<Integer, Integer, String>{  
        
		@Override  
        protected void onPreExecute() {
			super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(DramaSectionActivity_1305210935.this);
			sqlTvDrama.updateDramaSectionRecord(dramaId, currentSection);
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
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
    
    @Override
	protected void onDestroy(){
        super.onDestroy();
        if (loadTask!= null && loadTask.getStatus() != AsyncTask.Status.FINISHED) {
        	loadTask.closeProgressDilog();
        	loadTask.cancel(true);
        }
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
    
    public void showHodoAd() {
    	Resources res = getResources();
    	String hodoKey = res.getString(R.string.hodo_key);
    	Log.d("hodo", "showHodoAd");
    	AdWhirlManager.setConfigExpireTimeout(1000 * 30); 
		final HodoADView hodoADview = new HodoADView(this);
        hodoADview.requestAD(hodoKey);
        //關掉自動輪撥功能,交由adWhirl輪撥
        hodoADview.setAutoRefresh(false);
        
        hodoADview.setListener(new HodoADListener() {
            public void onGetBanner() {
                //成功取得banner
            	Log.d("hodo", "onGetBanner");
		        adWhirlLayout.adWhirlManager.resetRollover();
	            adWhirlLayout.handler.post(new ViewAdRunnable(adWhirlLayout, hodoADview));
	            adWhirlLayout.rotateThreadedDelayed();
            }
            public void onFailed(String msg) {
                //失敗取得banner
                Log.d("hodo", "onFailed :" +msg);
                adWhirlLayout.rollover();
            }
            public void onBannerChange(){
                //banner 切換
                Log.d("hodo", "onBannerChange");
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

	public void adWhirlGeneric()
	{
		// TODO Auto-generated method stub
		
	}
	
	public void showKuAd() {
		setKuAd();
	}
}
