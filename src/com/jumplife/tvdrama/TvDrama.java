package com.jumplife.tvdrama;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import static com.jumplife.tvdrama.CommonUtilities.SERVER_URL;
import static com.jumplife.tvdrama.CommonUtilities.SENDER_ID;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;
import com.jumplife.sqlite.SQLiteTvDramaHelper;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.Drama;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
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
	private CheckVersionTask taskVersion;
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

        JSONObject crittercismConfig = new JSONObject();
        try {
        	crittercismConfig.put("delaySendingAppLoad", true); // send app load data with Crittercism.sendAppLoadData()
            crittercismConfig.put("shouldCollectLogcat", true); // send logcat data for devices with API Level 16 and higher
        	crittercismConfig.put("includeVersionCode", true); // include version code in version name
        }
        catch (JSONException je){}
        //Crittercism.init(getApplicationContext(), "51ccf765558d6a0c25000003", crittercismConfig);
        
        rlLoading = (RelativeLayout)findViewById(R.id.rl_loading);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(120, (int) (displayMetrics.heightPixels*0.65), 120
        		, (int) (displayMetrics.heightPixels - (displayMetrics.heightPixels*0.85)));
        rlLoading.setLayoutParams(lp);
        
        loading = (TextView)findViewById(R.id.textview_load);
        loading.setText("檢查版本中");
        imageviewPBar = (ImageView)findViewById(R.id.imageview_progressbar);
        
        checkNotNull(SERVER_URL, "SERVER_URL");
        checkNotNull(SENDER_ID, "SENDER_ID");
        GCMRegistrar.checkDevice(getApplicationContext());
        final String regId = GCMRegistrar.getRegistrationId(getApplicationContext());
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

        taskVersion = new CheckVersionTask();
        if(Build.VERSION.SDK_INT < 11)
        	taskVersion.execute();
        else
        	taskVersion.executeOnExecutor(CheckVersionTask.THREAD_POOL_EXECUTOR, 0);
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
        
        ArrayList<Drama> dramas = api.getDramasIdViewsEpsV2();
        if(dramas != null) {
                		
    		//SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(this);
    		SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
    		instance.createDataBase();
    		SQLiteDatabase db = instance.getWritableDatabase();
    		db.beginTransaction();
    		
    		
        	ArrayList<Integer> a = new ArrayList<Integer>(100);
        	for(int i=0; i<dramas.size(); i++)
        		a.add(dramas.get(i).getId());
	        ArrayList<Integer> dramasInsertId = new ArrayList<Integer>();
	        ArrayList<Integer> dramasShowId = new ArrayList<Integer>();
	        
	        Log.d(TAG, "begin transation");
	        //dramasInsertId = sqlTvDrama.findDramasIdNotInDB(a);
	        dramasInsertId = instance.findDramasIdNotInDB(db, a);
	        
	        Log.d(TAG, "Need Insert Id : " + dramasInsertId);
	        
	        dramasShowId = (ArrayList<Integer>) a.clone();
	        
	        if (dramasInsertId.size() > 0){
	        	String idLst = "";
		        for(int i=0; i<dramasInsertId.size(); i++)
		           idLst = dramasInsertId.get(i) + "," +idLst;
		        api.AddDramasFromInfo(instance, db, idLst);
	        }	        
	        
	        
	        /*sqlTvDrama.updateDramaIsShow(dramasShowId);
	        sqlTvDrama.updateDramaViews(dramas);
	        sqlTvDrama.updateDramaEps(dramas);
	        sqlTvDrama.closeDB();*/
	        instance.updateDramaIsShow(db, dramasShowId);
	        instance.updateDramaViews(db, dramas);
	        instance.updateDramaEps(db, dramas);
            db.setTransactionSuccessful();
            db.endTransaction();
            db.close();
	        instance.closeHelper();
	        
	        
	        long endTime = System.currentTimeMillis();
	        Log.e(TAG, "sample method took（movie time activity) %%%%%%%%%%%%%%%%%%%%%%%%%%%%"+(endTime-startTime)+"ms");

			return "progress end";
        } else {
        	SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
    		instance.createDataBase();
    		SQLiteDatabase db = instance.getWritableDatabase();
            db.close();
	        instance.closeHelper();
	        
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
	
	class CheckVersionTask extends AsyncTask<Integer, Integer, String>{  
        
		int[] mVersionCode = new int[]{-1};
		String[] message = new String[]{""};
		
        @Override  
        protected void onPreExecute() {
        	super.onPreExecute();  
        }  
          
        @Override  
        protected String doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	DramaAPI api = new DramaAPI();
        	Log.d("", "version code " + mVersionCode[0]);
        	api.getVersionCode(mVersionCode, message);
        	Log.d("", "version code " + mVersionCode[0]);
            return "progress end";
        }  
 

		@Override  
        protected void onProgressUpdate(Integer... progress) {    
            super.onProgressUpdate(progress);  
        }  
  
        @Override  
        protected void onPostExecute(String result) {
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
			
			if(tmpVersionCode > -1 && tmpVersionCode < mVersionCode[0]) {
	        	new AlertDialog.Builder(TvDrama.this).setTitle("已有新版電視連續劇")
	    		.setMessage(message[0])
	            .setPositiveButton("前往更新", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface arg0, int arg1) {
	                	startActivity(new Intent(Intent.ACTION_VIEW, 
	    			    		Uri.parse("market://details?id=com.jumplife.tvdrama")));
	                	TvDrama.this.finish();
	                }
	            })
	            .setNegativeButton("下次再說", new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface arg0, int arg1) {
	                    loading.setText(getResources().getString(R.string.loading));
	                	taskLoad = new LoadDataTask();
	                    if(Build.VERSION.SDK_INT < 11)
	                    	taskLoad.execute();
	                    else
	                    	taskLoad.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
	                }
	            })
	            .show();
		        super.onPostExecute(result);
			} else {
		        loading.setText(getResources().getString(R.string.loading));
				taskLoad = new LoadDataTask();
		        if(Build.VERSION.SDK_INT < 11)
		        	taskLoad.execute();
		        else
		        	taskLoad.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
			}
				
        }  
          
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
      if (taskVersion!= null && taskVersion.getStatus() != AsyncTask.Status.FINISHED)
    	  taskVersion.cancel(true);
    }
}
