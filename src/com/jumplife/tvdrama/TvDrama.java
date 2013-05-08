package com.jumplife.tvdrama;

import java.util.ArrayList;

import static com.jumplife.tvdrama.CommonUtilities.SERVER_URL;
import static com.jumplife.tvdrama.CommonUtilities.SENDER_ID;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;
import com.jumplife.sqlite.SQLiteTvDrama;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.Drama;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TvDrama extends Activity {

	private LoadDataTask taskLoad;
	private RelativeLayout rlLoading;
	private ImageView imageviewPBar;
	private TextView loading;
	private AnimationDrawable animationDrawable;

    private AsyncTask<Void, Void, Void> mRegisterTask;
    
	public static String TAG = "TvDrama";
	
	@SuppressWarnings("deprecation")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tvdrama);
        
        rlLoading = (RelativeLayout)findViewById(R.id.rl_loading);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(120, (int) (displayMetrics.heightPixels*0.65), 120
        		, (int) (displayMetrics.heightPixels - (displayMetrics.heightPixels*0.85)));
        rlLoading.setLayoutParams(lp);
        
        loading = (TextView)findViewById(R.id.textview_load);
        loading.setText(getResources().getString(R.string.loading));
        imageviewPBar = (ImageView)findViewById(R.id.imageview_progressbar);
        
        checkNotNull(SERVER_URL, "SERVER_URL");
        checkNotNull(SENDER_ID, "SENDER_ID");
        GCMRegistrar.checkDevice(this);
        GCMRegistrar.checkManifest(this);
        final String regId = GCMRegistrar.getRegistrationId(this);
        if (regId.equals("")) {
            GCMRegistrar.register(this, SENDER_ID);
        } else {
        	if (!GCMRegistrar.isRegisteredOnServer(this)) {
                final Context context = this;
                mRegisterTask = new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        boolean registered = ServerUtilities.register(context, regId);
                        if (!registered) {
                            GCMRegistrar.unregister(context);
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mRegisterTask = null;
                    }

                };
                mRegisterTask.execute(null, null, null);
            }
        }

        taskLoad = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	taskLoad.execute();
        else
        	taskLoad.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
    }
	
	private void checkNotNull(Object reference, String name) {
        if (reference == null) {
        	throw new NullPointerException("error");
        }
    }    
	
	@Override  
    public void onWindowFocusChanged(boolean hasFocus) {  
        super.onWindowFocusChanged(hasFocus);  
        imageviewPBar.setBackgroundResource(R.anim.progressbar_video);
        animationDrawable = (AnimationDrawable) imageviewPBar.getBackground();
        animationDrawable.start();
    } 
	
	@SuppressWarnings("unchecked")
	private String fetchData(){
		long startTime = System.currentTimeMillis();
		DramaAPI api = new DramaAPI(this);
        SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(this);
        ArrayList<Drama> dramas = api.getDramasIdViewsEpsV2();
        if(dramas != null) {
        	ArrayList<Integer> a = new ArrayList<Integer>(100);
        	for(int i=0; i<dramas.size(); i++)
        		a.add(dramas.get(i).getId());
	        ArrayList<Integer> dramasInsertId = new ArrayList<Integer>();
	        ArrayList<Integer> dramasShowId = new ArrayList<Integer>();
	        dramasInsertId = sqlTvDrama.findDramasIdNotInDB(a);
	        dramasShowId = (ArrayList<Integer>) a.clone();
	        
	        if (dramasInsertId.size() > 0){
	        	String idLst = "";
		        for(int i=0; i<dramasInsertId.size(); i++)
		           idLst = dramasInsertId.get(i) + "," +idLst;
		        api.AddDramasFromInfo(idLst);
	        }	        
	        sqlTvDrama.updateDramaIsShow(dramasShowId);
	        sqlTvDrama.updateDramaViews(dramas);
	        sqlTvDrama.updateDramaEps(dramas);
	        
	        long endTime = System.currentTimeMillis();
	        Log.e(TAG, "sample method took（movie time activity) %%%%%%%%%%%%%%%%%%%%%%%%%%%%"+(endTime-startTime)+"ms");

			return "progress end";
        } else {
        	return "progress fail";
        }
	}
	
	private void setData(){
		Bundle extras = getIntent().getExtras();
        Intent newAct = new Intent();
        if(extras != null) {
        	newAct.putExtra("type_id", extras.getInt("type_id", 0));
        	newAct.putExtra("sort_id", extras.getInt("sort_id", 0));
        } else {
        	newAct.putExtra("type_id", 0);
        	newAct.putExtra("sort_id", 0);
        }
		newAct.setClass( TvDrama.this, MainTabActivities.class );
		startActivity(newAct);
    	finish();
	}
	
	class LoadDataTask extends AsyncTask<Integer, Integer, String>{  
        
        @Override  
        protected void onPreExecute() {
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
        	if(result.equals("progress fail")) {
	        	imageviewPBar.setVisibility(View.INVISIBLE);
	        	loading.setText(Html.fromHtml(getResources().getString(R.string.loading_failed1) + "<br>"
	        	+ getResources().getString(R.string.loading_failed2)));
	        	try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	setData();
	        super.onPostExecute(result);  
        }  
          
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
    public void onDestroy() {
      super.onDestroy();
      if(animationDrawable != null && animationDrawable.isRunning())
    	  animationDrawable.stop();
      if (taskLoad!= null && taskLoad.getStatus() != AsyncTask.Status.FINISHED)
			taskLoad.cancel(true);
    }
}
