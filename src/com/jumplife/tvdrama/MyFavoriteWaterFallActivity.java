package com.jumplife.tvdrama;

import java.util.ArrayList;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.sectionlistview.DramaGridAdapter;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.sqlite.SQLiteTvDrama;
import com.jumplife.tvdrama.entity.Drama;
import com.jumplife.tvdrama.promote.PromoteAPP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
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
    private SharePreferenceIO shIO;
    private SQLiteTvDrama sqlTvDrama;
    private ArrayList<Drama> dramaList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
    	sqlTvDrama = new SQLiteTvDrama(this);
    	
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
                Intent newAct = new Intent();
                newAct.putExtra("drama_id", dramaList.get(position).getId());
                newAct.putExtra("drama_name", dramaList.get(position).getChineseName());
                newAct.putExtra("drama_poster", dramaList.get(position).getPosterUrl());
                //newAct.setClass(MyFavoriteWaterFallActivity.this, DramaTabActivities.class);
                newAct.setClass(MyFavoriteWaterFallActivity.this, DramaInfoChapterActivity.class);
                startActivity(newAct);
            }
        });
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        
        adapter = new DramaGridAdapter(MyFavoriteWaterFallActivity.this, dramaList,
        		((screenWidth / 2)), (int) (((screenWidth / 2)) * 0.6));
        
        dramaGridView.setAdapter(adapter);
    }
    
    private void fetchData() {
    	shIO = new SharePreferenceIO(MyFavoriteWaterFallActivity.this);
    	dramaList = new ArrayList<Drama>(30); 	
    	dramaList = sqlTvDrama.getDramaList(shIO.SharePreferenceO("like_drama", ""));
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
        	if(MyFavoriteWaterFallActivity.this != null && !MyFavoriteWaterFallActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();

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
        if (loadTask!= null && loadTask.getStatus() != AsyncTask.Status.FINISHED)
        	loadTask.cancel(true);
    }
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(this);
    }
}
