package com.jumplife.tvdrama;

import java.util.ArrayList;

import org.json.JSONException;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

<<<<<<< HEAD

import com.crittercism.app.Crittercism;
import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
=======
import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.adwhirl.AdWhirlLayout.ViewAdRunnable;
>>>>>>> a22bcef6b69adf6bbd3b6b628359d17ae0de2008
import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.sqlite.SQLiteTvDramaHelper;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.Advertisement;
import com.jumplife.tvdrama.entity.Drama;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

public class DramaInfoChapterActivity extends Activity{

	private ImageButton imageButtonRefresh;
	//private ImageView[] mark;
	private ImageView poster;
	private ImageView like;
	private TextView[] mark_2;
	private TextView textviewDramaContent;
	private TextView textviewChapter;
	private TextView textviewIntro;
	private TextView topbar_text;
	private View viewLine1;
	private View viewLine2;
	private LinearLayout llChapter;
	private RelativeLayout rlPromote;
	private ImageView ivPromote;
	private ImageView ivDelete;
	
    //private ArrayList<Chapter> chapters;
	private LoadDataTask taskLoad;
	private RefreshDataTask refreshTaskLoad;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private SharePreferenceIO shIO;
	private int tabFlag = 1;
	private int chapterCount = 0;
	private int lastChapterCount = 0;
	private int currentChapter = 0;
	private String[] chapters;
	private String[] likeDramas;
	private boolean likeDrama = false;
	//private AdWhirlLayout adWhirlLayout;
	
	private Drama drama;
	private int dramaId = 0;
	private String dramaName = "";
	private AdView adView;

	//private static String TAG = "DramaInfoChapterActivity";
	
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
        
        setContentView(R.layout.activity_drama_info_chapter);
        
        options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.stub)
		.showImageForEmptyUri(R.drawable.stub)
		.showImageOnFail(R.drawable.stub)
		.imageScaleType(ImageScaleType.EXACTLY)
		.cacheOnDisc()
		.displayer(new SimpleBitmapDisplayer())
		.build();
        
        initView();
        /*
        AdTask adTask = new AdTask();
    	adTask.execute();
        */
        
        this.setAd();
        
        new LoadPromoteImage().execute();
        
        taskLoad = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	taskLoad.execute();
        else
        	taskLoad.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
    }
	
	@Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(this);
    }
    
	@Override
    public void onPause() {
      super.onPause();
      //imageLoader.clearCache();
    }
	
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }
	
	private void initView() {
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
        	dramaId = extras.getInt("drama_id");
        	dramaName = extras.getString("drama_name");
        }
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		//imageLoader = new ImageLoader(this, displayMetrics.widthPixels);
		topbar_text = (TextView)findViewById(R.id.topbar_text);
        poster = (ImageView)findViewById(R.id.imageview_daramaposter);
        like = (ImageView)findViewById(R.id.like);
        
        shIO = new SharePreferenceIO(DramaInfoChapterActivity.this);
		likeDramas = shIO.SharePreferenceO("like_drama", "").split(",");
		for(int i=0; i<likeDramas.length; i++)
			if(likeDramas[i].equals(String.valueOf(dramaId)))
				likeDrama = true;		
		
		textviewChapter = (TextView)findViewById(R.id.textview_chapter);
		textviewIntro = (TextView)findViewById(R.id.textview_intro);
		imageButtonRefresh = (ImageButton)findViewById(R.id.refresh);
		llChapter = (LinearLayout)findViewById(R.id.ll_chapter);
		textviewDramaContent = (TextView)findViewById(R.id.textview_dramacontent);
		viewLine1 = (View)findViewById(R.id.view_tabline1);
		viewLine2 = (View)findViewById(R.id.view_tabline2);
		rlPromote = (RelativeLayout)findViewById(R.id.rl_promote);
		ivPromote = (ImageView)findViewById(R.id.iv_promote);
		ivDelete = (ImageView)findViewById(R.id.iv_delete);
		
		imageButtonRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	taskLoad = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	taskLoad.execute();
                else
                	taskLoad.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
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
    
	private String fetchData(){
		
		Log.d(null, "drama id : " + dramaId);		
		drama = new Drama();
		
        //SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(this);
		SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
        SQLiteDatabase db = instance.getReadableDatabase();
        db.beginTransaction();
        /*drama = sqlTvDrama.getDrama(dramaId);
        String tmp = sqlTvDrama.getDramaChapter(dramaId);
        currentChapter = sqlTvDrama.getDramaChapterRecord(dramaId);        
        sqlTvDrama.closeDB();*/
        drama = instance.getDrama(db, dramaId);
        String tmp = instance.getDramaChapter(db, dramaId);
        currentChapter = instance.getDramaChapterRecord(db, dramaId);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        instance.closeHelper();
        
		if(tmp != null && !tmp.equals("")) {
        	chapters = tmp.split(",");
        	chapterCount = chapters.length;
        }
        
		return "progress end";
	}
	
	private String reNewData(){		
		DramaAPI dramaAPI = new DramaAPI(this);
		String eps = dramaAPI.getDramaEps(dramaId);
		
		//SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(this);
		//SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(this);
		SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
        SQLiteDatabase db = instance.getWritableDatabase();
        db.beginTransaction();
		
		//currentChapter = sqlTvDrama.getDramaChapterRecord(dramaId);
        currentChapter = instance.getDramaChapterRecord(db, dramaId);
        if(eps != null && !eps.equals("")) {
        	chapters = eps.split(",");
        	chapterCount = chapters.length;
        	drama.setEps(eps);    		
        	//sqlTvDrama.updateDramaEps(dramaId, eps);
        	instance.updateDramaEps(db, dramaId, eps);
        }        
        //sqlTvDrama.closeDB();
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        instance.closeHelper();        
        
		return "progress end";
	}

	private void setLike(){		
	    if(likeDrama) {
	    	like.setBackgroundResource(R.drawable.love_press);
	    } else {
	    	like.setBackgroundResource(R.drawable.love_normal);
	    }
	}
	
	private void setOnClickListener() {
		textviewChapter.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				tabFlag = 1;
				setFakeTabView();
			}			
		});
		
		textviewIntro.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tabFlag = 2;
				setFakeTabView();
			}			
		});
		
		like.setOnClickListener(new OnClickListener(){
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String tmpLikeDramas = "";
				if(likeDrama) {
					EasyTracker.getTracker().trackEvent("我的收藏", "取消", dramaName, (long)0);
					for(int i=0; i<likeDramas.length; i++)
						if(!likeDramas[i].equals(String.valueOf(dramaId)))
							tmpLikeDramas = tmpLikeDramas + likeDramas[i] + ",";
					shIO.SharePreferenceI("like_drama", tmpLikeDramas);
					likeDrama = false;
					Toast.makeText(DramaInfoChapterActivity.this, 
							DramaInfoChapterActivity.this.getResources().getString(R.string.remove_favorite), Toast.LENGTH_LONG)
							.show();					
				} else {
					EasyTracker.getTracker().trackEvent("我的收藏", "加入", dramaName, (long)0);
					for(int i=0; i<likeDramas.length; i++)
						tmpLikeDramas = tmpLikeDramas + likeDramas[i] + ",";
					tmpLikeDramas = tmpLikeDramas + dramaId;
					shIO.SharePreferenceI("like_drama", tmpLikeDramas);
					likeDrama = true;
					Toast.makeText(DramaInfoChapterActivity.this, 
							DramaInfoChapterActivity.this.getResources().getString(R.string.add_favorite), Toast.LENGTH_LONG)
							.show();
				}
				setLike();
			}			
		});
	}
	
	private void setView(){
		topbar_text.setText(drama.getChineseName());
        textviewDramaContent.setText(drama.getIntroduction());       
        
        imageLoader.displayImage(drama.getPosterUrl(), poster, options);
        
        setOnClickListener();
        setFakeTabView();
        //if(chapters != null && chapters.size() != 0) {
        if(chapterCount > 0) {
        	//upsidedownChapter();
            setFakeGrid();		
			setFakeMark();
		}
        setLike();
	}
	
	private void setFakeTabView() {

		if(tabFlag == 1) {
			textviewDramaContent.setVisibility(View.GONE);
			viewLine1.setBackgroundResource(R.color.channel_button_text_press);
			viewLine2.setBackgroundResource(R.color.transparent100);
			textviewChapter.setTextColor(getResources().getColor(R.color.channel_button_text_press));
			textviewIntro.setTextColor(getResources().getColor(R.color.channel_button_text_normal));
			//if(chapters != null && chapters.size() != 0) {
			if(chapterCount > 0) {
	    		llChapter.setVisibility(View.VISIBLE);
	            imageButtonRefresh.setVisibility(View.GONE);
	    	} else {
	    		llChapter.setVisibility(View.GONE);
	            imageButtonRefresh.setVisibility(View.VISIBLE);
	    	}
		} else {
			viewLine1.setBackgroundResource(R.color.transparent100);
			viewLine2.setBackgroundResource(R.color.channel_button_text_press);
			textviewChapter.setTextColor(getResources().getColor(R.color.channel_button_text_normal));
			textviewIntro.setTextColor(getResources().getColor(R.color.channel_button_text_press));
			llChapter.setVisibility(View.GONE);
            imageButtonRefresh.setVisibility(View.GONE);
            textviewDramaContent.setVisibility(View.VISIBLE);
		}
		
	}

	private void setFakeGrid() {
		//mark = new ImageView[chapterCount];
		mark_2 = new TextView[chapterCount];
		for(int i=0; i<chapterCount; i+=4) {
			TableRow Schedule_row = new TableRow(this);
			for(int j=0; j<4; j++) {
				int index = i + j;
				RelativeLayout rl = new RelativeLayout(this);
				//TextView tv = new TextView(this);
				
				
				if(index < chapterCount) {
					mark_2[index] = new TextView(this);
					//mark_2[index].setId(index + chapterCount);
					mark_2[index].setText(chapters[index]);				
					mark_2[index].setId(index);
					mark_2[index].setBackgroundResource(R.drawable.grid_item_dramachapter_bg);
					mark_2[index].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
					mark_2[index].setTextColor(getResources().getColor(R.drawable.grid_item_dramachapter_textcolor));
					mark_2[index].setGravity(Gravity.CENTER);
					mark_2[index].setPadding(5, 10, 5, 10);
					mark_2[index].setOnClickListener(new OnClickListener() {
						public void onClick(View arg0) {
							int position = arg0.getId();
							currentChapter = position;
							//new UpdateDramaChapterRecordTask().execute();
							setFakeMark();  
				        
							Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				        	//SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(DramaInfoChapterActivity.this);
							SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(DramaInfoChapterActivity.this);
					        SQLiteDatabase db = instance.getWritableDatabase();
					        /*sqlTvDrama.updateDramaChapterRecord(dramaId, currentChapter);        
					        sqlTvDrama.closeDB();*/
					        instance.updateDramaChapterRecord(db, dramaId, currentChapter);
					        db.close();
					        instance.closeHelper();
							
							Intent newAct = new Intent();
							//newAct.putExtra("chapter", chapters);
							newAct.putExtra("chapter_no", Integer.parseInt(chapters[position]));
							newAct.putExtra("drama_id", dramaId);
							newAct.putExtra("drama_name", dramaName);
			                newAct.setClass(DramaInfoChapterActivity.this, DramaSectionActivity.class);
			                DramaInfoChapterActivity.this.startActivity(newAct);			                
			                //DramaInfoChapterActivity.this.overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);
						}					
					});
					RelativeLayout.LayoutParams rlTextParams = new RelativeLayout.LayoutParams
							(RelativeLayout.LayoutParams.MATCH_PARENT, (int) (mark_2[index].getTextSize() + 60));
					rlTextParams.setMargins(16, 16, 16, 16);
					mark_2[index].setLayoutParams(rlTextParams);
					rl.addView(mark_2[index]);
					
					/*mark[index] = new ImageView(this);
					mark[index].setId(index + chapterCount);
					mark[index].setImageResource(R.drawable.mark);RelativeLayout.LayoutParams rlMarkParams = new RelativeLayout.LayoutParams
							(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					rlMarkParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
					rlMarkParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					rlMarkParams.setMargins(26, 26, 26, 26);
					rlMarkParams.width = 22;
					rlMarkParams.height = 22;
					mark[index].setLayoutParams(rlMarkParams);				
					rl.addView(mark[index]);*/
				}				
				
				TableRow.LayoutParams Params = new TableRow.LayoutParams
						(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.25f);
				rl.setLayoutParams(Params);
				
				Schedule_row.addView(rl);				
			}
			Schedule_row.setLayoutParams(new LayoutParams
					(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));  
			llChapter.addView(Schedule_row);
		}
		
		Button buttonReNew = new Button(this);
		buttonReNew.setText(this.getResources().getString(R.string.renew_eps));
		buttonReNew.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
		buttonReNew.setTextColor(this.getResources().getColor(R.color.main_color_orange));
		buttonReNew.setBackgroundResource(R.drawable.button_reneweps_background);
		buttonReNew.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				refreshTaskLoad = new RefreshDataTask();
		        if(Build.VERSION.SDK_INT < 11)
		        	refreshTaskLoad.execute();
		        else
		        	refreshTaskLoad.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
			}				
		});
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(30, 30, 0, 0);
		layoutParams.gravity = Gravity.CENTER;
		buttonReNew.setLayoutParams(layoutParams);
		llChapter.addView(buttonReNew);
	}
	
	private void setFakeMark() {
		for(int i=0; i<chapterCount; i+=1) {
			/*if(currentChapter < 0) {
				if(i == 0)
					mark[i].setVisibility(View.VISIBLE);
				else
					mark[i].setVisibility(View.GONE);
			} else {
				if(i == currentChapter)
					mark[i].setVisibility(View.VISIBLE);
				else
					mark[i].setVisibility(View.GONE);
			}*/
			if(currentChapter < 0) {				
				if(i == 0) {
					mark_2[i].setBackgroundResource(R.drawable.grid_item_dramachapter_mark_bg);
					mark_2[i].setTextColor(getResources().getColor(R.color.white));
				} else {
					mark_2[i].setBackgroundResource(R.drawable.grid_item_dramachapter_bg);
					mark_2[i].setTextColor(getResources().getColor(R.drawable.grid_item_dramachapter_textcolor));
				}
			} else {
				if(i == currentChapter) {
					mark_2[i].setBackgroundResource(R.drawable.grid_item_dramachapter_mark_bg);
					mark_2[i].setTextColor(getResources().getColor(R.color.white));
				} else {
					mark_2[i].setBackgroundResource(R.drawable.grid_item_dramachapter_bg);
					mark_2[i].setTextColor(getResources().getColor(R.drawable.grid_item_dramachapter_textcolor));
				}
			}
		}
	}
	
	class LoadDataTask extends AsyncTask<Integer, Integer, String>{  
        
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
        	 progressdialogInit = new ProgressDialog(DramaInfoChapterActivity.this);
             progressdialogInit.setTitle("Load");
             progressdialogInit.setMessage("Loading…");
             progressdialogInit.setOnCancelListener(cancelListener);
             progressdialogInit.setCanceledOnTouchOutside(false);
             progressdialogInit.show();
             super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	return fetchData();
        }  
 

		@Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {
        	if(DramaInfoChapterActivity.this != null && !DramaInfoChapterActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        	
        	if(chapters != null && chapters.length > 0) {
        		imageButtonRefresh.setVisibility(View.GONE);
        		setView();
        	} else
        		imageButtonRefresh.setVisibility(View.VISIBLE);
        	
	        super.onPostExecute(result);  
        }  
         
        public void closeProgressDilog() {
        	if(DramaInfoChapterActivity.this != null && !DramaInfoChapterActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }
	
	class RefreshDataTask extends AsyncTask<Integer, Integer, String>{  
        
		private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				RefreshDataTask.this.cancel(true);
				imageButtonRefresh.setVisibility(View.VISIBLE);
				finish();
			}
        };
	      
        @Override  
        protected void onPreExecute() {
        	lastChapterCount = chapterCount;
        	progressdialogInit = new ProgressDialog(DramaInfoChapterActivity.this);
        	progressdialogInit.setTitle("Load");
        	progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
             super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	return reNewData();
        }  
 

		@Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {
        	if(DramaInfoChapterActivity.this != null && !DramaInfoChapterActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        	llChapter.removeAllViews();
        	if(chapters != null && chapters.length > 0) {
        		imageButtonRefresh.setVisibility(View.GONE);
        		setView();
        	
        		if(lastChapterCount == chapterCount) {
        			Toast toast = Toast.makeText(DramaInfoChapterActivity.this, "目前集數已為最新", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
        		} else {
        			Toast toast = Toast.makeText(DramaInfoChapterActivity.this, "集數更新完成", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
        		}
        	} else {
        		imageButtonRefresh.setVisibility(View.VISIBLE);
        	}

	        super.onPostExecute(result);  
        }  
         
        public void closeProgressDilog() {
        	if(DramaInfoChapterActivity.this != null && !DramaInfoChapterActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }

	class LoadPromoteImage extends AsyncTask<Integer, Void, String>{  
        
		@Override  
        protected void onPreExecute() {
             super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	DramaAPI dramaAPI = new DramaAPI(DramaInfoChapterActivity.this);
        	ArrayList<Advertisement> advertisements = dramaAPI.getAdvertisementList(1);
            String url = "";
        	/*
        	 * 廣告模擬Grid Item
        	 */
            if(advertisements != null && advertisements.size() > 0) {
            	Advertisement advertisement = advertisements.get(0);
            	if(advertisement != null && !advertisement.getUrl().equals("")) {
            		url = advertisement.getUrl();
            	}
        	}
            return url;
        }
  
        @Override  
        protected void onPostExecute(String result) {        	
        	if(result != null) {
	        	ivDelete.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						rlPromote.setVisibility(View.GONE);
					}    			
	    		});
	    		
	        	ivPromote.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						Intent newAct = new Intent();
						newAct.setClass( DramaInfoChapterActivity.this, TicketCenterActivity.class );
		                startActivity( newAct );
					}        		
	        	});
	        	
	        	DisplayImageOptions adOptions = new DisplayImageOptions.Builder()
	    		.imageScaleType(ImageScaleType.EXACTLY)
	    		.cacheOnDisc()
	    		.displayer(new SimpleBitmapDisplayer())
	    		.build();
	    		
	    		imageLoader.displayImage(result, ivPromote, adOptions, new SimpleImageLoadingListener() {
	    		    @Override
	    		    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
	    		    	if(imageUri != null && !imageUri.equals("")) {
		    		    	rlPromote.setVisibility(View.VISIBLE);
		    	    		Animation animation = AnimationUtils.loadAnimation(DramaInfoChapterActivity.this, R.anim.alpha); 
		    	    		rlPromote.startAnimation(animation);
	    		    	}
	    		    }
	    		});
        	}
    		
	        super.onPostExecute(result);  
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

	@Override
	protected void onDestroy(){
        super.onDestroy();
        if (taskLoad!= null && taskLoad.getStatus() != AsyncTask.Status.FINISHED) {
        	taskLoad.closeProgressDilog();
        	taskLoad.cancel(true);
        }
        if (refreshTaskLoad!= null && refreshTaskLoad.getStatus() != AsyncTask.Status.FINISHED) {
        	refreshTaskLoad.closeProgressDilog();
        	refreshTaskLoad.cancel(true);
        }
    }
}
