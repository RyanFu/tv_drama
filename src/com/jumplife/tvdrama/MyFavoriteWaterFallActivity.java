package com.jumplife.tvdrama;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.adapter.DramaGridAdapter;
import com.jumplife.sqlite.SQLiteTvDramaHelper;
import com.jumplife.tvdrama.entity.Drama;
import com.jumplife.tvdrama.promote.PromoteAPP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class MyFavoriteWaterFallActivity extends Activity {
    public final static int  SETTING          = 1;

    private GridView         dramaGridView;
    private ImageButton      imageButtonRefresh;
    private RelativeLayout   rlWaterfall;
    private LinearLayout	 llNoMyfavorite;
    private Button		     buttonFavorite;
    private Button		     buttonHistory;
    private ImageView	     imageviewFavorite;
    private ImageView	     imageviewHistory;
    private LoadDataTask     loadTask;
    private DramaGridAdapter adapter;
    private ArrayList<Drama> dramaList;
	private Animation animation;

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
        
        setContentView(R.layout.activity_myfavoritewaterfall);
        initViews();        
    }

    @Override
	protected void onResume() {
    	loadTask = new LoadDataTask();
    	if(Build.VERSION.SDK_INT < 11)
        	loadTask.execute();
        else
        	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
        
        super.onResume();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

        	PromoteAPP promoteAPP = new PromoteAPP(MyFavoriteWaterFallActivity.this);
        	if(!promoteAPP.isPromote) {
	        	new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.leave_app))
	            .setPositiveButton(getResources().getString(R.string.leave), new DialogInterface.OnClickListener() {
	                // do something when the button is clicked
	                public void onClick(DialogInterface arg0, int arg1) {
	                	MyFavoriteWaterFallActivity.this.finish();
	                }
	            }).setNegativeButton(getResources().getString(R.string.cancel), null)
	            .show();
		    } else
		    	promoteAPP.promoteAPPExe();

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    private void initViews() {
    	animation = AnimationUtils.loadAnimation(MyFavoriteWaterFallActivity.this, R.anim.item_movies_anim);
		
    	rlWaterfall = (RelativeLayout)findViewById(R.id.rl_waterfall);
        llNoMyfavorite = (LinearLayout)findViewById(R.id.ll_no_myfavorite);
        
        imageviewFavorite = (ImageView)findViewById(R.id.arrow_2);
    	imageviewHistory = (ImageView)findViewById(R.id.arrow_3);
    	
    	imageButtonRefresh = (ImageButton) findViewById(R.id.refresh);
        imageButtonRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	loadTask = new LoadDataTask();
            	if(Build.VERSION.SDK_INT < 11)
            		loadTask.execute();
                else
                	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
        
        buttonFavorite = (Button)findViewById(R.id.button_drama_favorite);
        buttonFavorite.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	initBottonTextViewColor();
            	buttonFavorite.setTextColor(MyFavoriteWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_press));
            	initBottonImageViewVisible();
            	imageviewFavorite.setVisibility(View.VISIBLE);
            }
        });
        buttonHistory = (Button)findViewById(R.id.button_drama_history);
        buttonHistory.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	initBottonTextViewColor();
            	buttonHistory.setTextColor(MyFavoriteWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_press));
            	initBottonImageViewVisible();
            	imageviewHistory.setVisibility(View.VISIBLE);
            }
        });
        
    }

    private void initBottonTextViewColor() {
    	buttonFavorite.setTextColor(MyFavoriteWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_normal));
    	buttonHistory.setTextColor(MyFavoriteWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_normal));
    }
    
    private void initBottonImageViewVisible() {
    	imageviewFavorite.setVisibility(View.INVISIBLE);
    	imageviewHistory.setVisibility(View.INVISIBLE);
    }
    
    // 設定畫面上的UI
    private void setViews() {
        dramaGridView = (GridView)findViewById(R.id.gridview_myfavorite);
        dramaGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	EasyTracker.getTracker().trackEvent("我的收藏", dramaList.get(position).getChineseName(), "播放", (long)0);
            	view.startAnimation(animation);
    			view.postDelayed(createRunnable(position, view), 30);
            }
        });
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        
        List<Integer> hotList = new ArrayList<Integer>(); 
        adapter = new DramaGridAdapter(MyFavoriteWaterFallActivity.this, dramaList, hotList, 5,
        		((screenWidth / 2)), (int) (((screenWidth / 2)) * 0.6));
        
        dramaGridView.setAdapter(adapter);
    }
	
	private Runnable createRunnable(final int position, final View view){
	    Runnable aRunnable = new Runnable(){
	        public void run(){
                Intent newAct = new Intent();
                newAct.putExtra("drama_id", dramaList.get(position).getId());
                newAct.putExtra("drama_name", dramaList.get(position).getChineseName());
                newAct.putExtra("drama_poster", dramaList.get(position).getPosterUrl());
                //newAct.setClass(MyFavoriteWaterFallActivity.this, DramaTabActivities.class);
                newAct.setClass(MyFavoriteWaterFallActivity.this, DramaInfoChapterActivity.class);
                startActivity(newAct);
	        }
	    };
	    return aRunnable;
	}
    
    private void fetchData() {
    	dramaList = new ArrayList<Drama>(30); 	
    	

        /*SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(this);    	
    	dramaList = sqlTvDrama.getDramaList(shIO.SharePreferenceO("like_drama", ""));
    	sqlTvDrama.closeDB();*/
    	SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
        SQLiteDatabase db = instance.getWritableDatabase();
        dramaList = instance.getDramaList(db, TvDramaApplication.shIO.getString("like_drama", ""));
        db.close();
        instance.closeHelper();
    }

    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
		      public void onCancel(DialogInterface arg0) {
		          LoadDataTask.this.cancel(true);
		          rlWaterfall.setVisibility(View.GONE);
		          llNoMyfavorite.setVisibility(View.GONE);
		          imageButtonRefresh.setVisibility(View.VISIBLE);
		      }
		  };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(MyFavoriteWaterFallActivity.this);
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

            if (dramaList == null) {
            	rlWaterfall.setVisibility(View.GONE);
            	llNoMyfavorite.setVisibility(View.GONE);
		        imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	imageButtonRefresh.setVisibility(View.GONE);
            	if(dramaList.size() > 0) {
	            	rlWaterfall.setVisibility(View.VISIBLE);
	                llNoMyfavorite.setVisibility(View.GONE);
	                setViews();
            	} else {
            		rlWaterfall.setVisibility(View.GONE);
	                llNoMyfavorite.setVisibility(View.VISIBLE);
            	}
            }

            super.onPostExecute(result);
        }

        public void closeProgressDilog() {
        	if(MyFavoriteWaterFallActivity.this != null && !MyFavoriteWaterFallActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }

    public void showReloadDialog(final Context context) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);

        alt_bld.setMessage(getResources().getString(R.string.reload_or_not))
        .setCancelable(true).setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                loadTask = new LoadDataTask();
                loadTask.execute();
                dialog.dismiss();
            }
        });

        AlertDialog alert = alt_bld.create();
        alert.setTitle(getResources().getString(R.string.load_error));
        alert.setCancelable(false);
        alert.show();

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
        	loadTask.cancel(true);
        	loadTask.closeProgressDilog();
        }
    }
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }
}
