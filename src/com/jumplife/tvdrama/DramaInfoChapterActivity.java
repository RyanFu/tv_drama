package com.jumplife.tvdrama;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.content.Intent;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.imageload.ImageLoader;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.sqlite.SQLiteTvDrama;
import com.jumplife.tvdrama.entity.Drama;
import com.kuad.KuBanner;
import com.kuad.kuADListener;

public class DramaInfoChapterActivity extends Activity implements AdWhirlInterface{

	private ImageButton imageButtonRefresh;
	private ImageView[] mark;
	private ImageView poster;
	private ImageView like;
	private TextView textviewDramaContent;
	private TextView textviewChapter;
	private TextView textviewIntro;
	private TextView topbar_text;
	private View viewLine1;
	private View viewLine2;
	private LinearLayout llChapter;
	
    //private ArrayList<Chapter> chapters;
	private LoadDataTask taskLoad;
	private ImageLoader imageLoader;
	private SharePreferenceIO shIO;
	private int tabFlag = 1;
	private int chapterCount = 0;
	private int currentChapter = 1;
	private String[] chapters;
	private String[] likeDramas;
	private boolean likeDrama = false;
	
	private Drama drama;
	private int dramaId = 0;
	private String dramaName = "";

	private static String TAG = "DramaInfoChapterActivity";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drama_info_chapter);
        initView();
        taskLoad = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	taskLoad.execute();
        else
        	taskLoad.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
        
        AdTask adTask = new AdTask();
    	adTask.execute();
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
	
	private void initView() {
		Bundle extras = getIntent().getExtras();
		if(extras != null) {
        	dramaId = extras.getInt("drama_id");
        	dramaName = extras.getString("drama_name");
        }
		
		imageLoader = new ImageLoader(this);
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
	
	private String fetchData(){
		SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(this);
		sqlTvDrama.getDramaChapterRecord(dramaId);
		
		drama = new Drama();
        drama = sqlTvDrama.getDrama(dramaId);
        String tmp = sqlTvDrama.getDramaChapter(dramaId);
        currentChapter = sqlTvDrama.getDramaChapterRecord(dramaId);
        if(!tmp.equals("")) {
        	chapters = tmp.split(",");
        	chapterCount = chapters.length;
        }
        
        SQLiteTvDrama.closeDB();
        
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
		imageButtonRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	taskLoad = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	taskLoad.execute();
                else
                	taskLoad.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
		
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
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        imageLoader.DisplayImage(drama.getPosterUrl(), poster, displayMetrics.widthPixels);
        
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

	@SuppressWarnings("deprecation")
	private void setFakeGrid() {
		mark = new ImageView[chapterCount];
		for(int i=0; i<chapterCount; i+=3) {
			TableRow Schedule_row = new TableRow(this);
			for(int j=0; j<3; j++) {
				int index = i + j;
				RelativeLayout rl = new RelativeLayout(this);
				TextView tv = new TextView(this);
				
				if(index < chapterCount) {
					tv.setText(chapters[index]);				
					tv.setId(index);
					tv.setBackgroundResource(R.drawable.grid_item_dramachapter_bg);
					tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
					tv.setTextColor(getResources().getColor(R.drawable.grid_item_dramachapter_textcolor));
					tv.setGravity(Gravity.CENTER);
					tv.setPadding(5, 10, 5, 10);
					tv.setOnClickListener(new OnClickListener() {
						public void onClick(View arg0) {
							int position = arg0.getId();
							currentChapter = position;
							new UpdateDramaChapterRecordTask().execute();
							
							Log.d(TAG, "leave drama info chapter");
							
							Intent newAct = new Intent();
							newAct.putExtra("chapter_no", position+1);
							newAct.putExtra("drama_id", dramaId);
							newAct.putExtra("drama_name", dramaName);
			                newAct.setClass(DramaInfoChapterActivity.this, DramaSectionActivity.class);
			                startActivity(newAct);
						}					
					});
					RelativeLayout.LayoutParams rlTextParams = new RelativeLayout.LayoutParams
							(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					rlTextParams.setMargins(15, 15, 15, 15);
					tv.setLayoutParams(rlTextParams);
					rl.addView(tv);
					
					mark[index] = new ImageView(this);
					mark[index].setId(index + chapterCount);
					mark[index].setImageResource(R.drawable.mark);
					RelativeLayout.LayoutParams rlMarkParams = new RelativeLayout.LayoutParams
							(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
					rlMarkParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
					rlMarkParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					rlMarkParams.setMargins(16, 16, 16, 16);
					rlMarkParams.width = 20;
					rlMarkParams.height = 20;
					mark[index].setLayoutParams(rlMarkParams);				
					rl.addView(mark[index]);
				}				
				
				TableRow.LayoutParams Params = new TableRow.LayoutParams
						(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 0.33f);
				rl.setLayoutParams(Params);
				
				Schedule_row.addView(rl);				
			}
			Schedule_row.setLayoutParams(new LayoutParams
					(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
			llChapter.addView(Schedule_row);
		}
	}
	
	private void setFakeMark() {
		for(int i=0; i<chapterCount; i+=1) {
			if(currentChapter == -1) {
				if(i == 1)
					mark[i].setVisibility(View.VISIBLE);
				else
					mark[i].setVisibility(View.GONE);
			} else {
				if(i == currentChapter)
					mark[i].setVisibility(View.VISIBLE);
				else
					mark[i].setVisibility(View.GONE);
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
        	setView();

	        super.onPostExecute(result);  
        }  
          
    }
	
	class UpdateDramaChapterRecordTask extends AsyncTask<Integer, Integer, String>{  
        
		@Override  
        protected void onPreExecute() {
			setFakeMark();
			super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(DramaInfoChapterActivity.this);
			sqlTvDrama.updateDramaChapterRecord(dramaId, currentChapter);
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
	
	@Override
	protected void onDestroy(){
        super.onDestroy();
        if (taskLoad!= null && taskLoad.getStatus() != AsyncTask.Status.FINISHED)
        	taskLoad.cancel(true);
    }
}
