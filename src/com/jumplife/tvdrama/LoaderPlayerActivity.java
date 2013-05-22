package com.jumplife.tvdrama;

import java.util.ArrayList;

import com.jumplife.tvdrama.DramaSectionActivity.LoadDataTask;
import com.jumplife.videoloader.DailymotionLoader;
import com.jumplife.videoloader.YoutubeLoader;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.LinearLayout.LayoutParams;

public class LoaderPlayerActivity extends Activity {

    public final static String MSG_INIT = "com.keyes.video.msg.init";
    protected String mMsgInit = "初始化";

    public final static String MSG_DETECT = "com.keyes.video.msg.detect";
    protected String mMsgDetect = "偵測頻寬";

    public final static String MSG_ERROR_TITLE = "com.keyes.video.msg.error.title";
    protected String mMsgErrorTitle = "連線錯誤";
    /**
     * Background task on which all of the interaction with YouTube is done
     */
    protected QueryVideoTask mQueryVideoTask;
    protected Dialog mDialogLoader;
    //protected ProgressBar mProgressBar;
    protected ImageView mProgressImage;
    protected TextView mProgressMessage;
	private AnimationDrawable animationDrawable;
    private RelativeLayout rlAd;
    private VideoView mVideoView;
    
    private int currentPart = 1;
    private ArrayList<String> videoIds = new ArrayList<String>();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    
    @Override  
    public void onWindowFocusChanged(boolean hasFocus) {  
        super.onWindowFocusChanged(hasFocus);
        animationDrawable.start();
    }
    
    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // create the layout of the view
        initView();
        // determine the messages to be displayed as the view loads the video
        extractMessages();
        
        /*mProgressBar.bringToFront();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressMessage.setText(mMsgInit);*/
        mDialogLoader.show();
        mProgressMessage.setText(mMsgInit);
        animationDrawable.start();
    
        Bundle extra = getIntent().getExtras();//.getStringArrayListExtra("");
        currentPart = extra.getInt("currentPart", 1);
        videoIds = extra.getStringArrayList("videoIds");
        
        if (videoIds == null || videoIds.size() <= 0) {
            Log.i(this.getClass().getSimpleName(), "Unable to extract video ID from the intent.  Closing video activity.");
            LoaderPlayerActivity.this.finish();
        }
        
        mQueryVideoTask = new QueryVideoTask();
        if(Build.VERSION.SDK_INT < 11)
        	mQueryVideoTask.execute(videoIds.get(currentPart-1));
        else
        	mQueryVideoTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1));  
    }

    private void extractMessages() {
        Intent lInvokingIntent = getIntent();
        String lMsgInit = lInvokingIntent.getStringExtra(MSG_INIT);
        if (lMsgInit != null) {
            mMsgInit = lMsgInit;
        }
        String lMsgDetect = lInvokingIntent.getStringExtra(MSG_DETECT);
        if (lMsgDetect != null) {
            mMsgDetect = lMsgDetect;
        }
        String lMsgErrTitle = lInvokingIntent.getStringExtra(MSG_ERROR_TITLE);
        if (lMsgErrTitle != null) {
            mMsgErrorTitle = lMsgErrTitle;
        }
    }

    private void initView() {
        LinearLayout lLinLayout = new LinearLayout(this);
        lLinLayout.setId(1);
        lLinLayout.setOrientation(LinearLayout.VERTICAL);
        lLinLayout.setGravity(Gravity.CENTER);
        lLinLayout.setBackgroundResource(R.color.background);
        LayoutParams lLinLayoutParms = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lLinLayout.setLayoutParams(lLinLayoutParms);

        this.setContentView(lLinLayout);


        RelativeLayout lRelLayout = new RelativeLayout(this);
        lRelLayout.setId(2);
        lRelLayout.setGravity(Gravity.CENTER);
        lRelLayout.setBackgroundColor(Color.BLACK);
        android.widget.RelativeLayout.LayoutParams lRelLayoutParms = 
        		new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lRelLayout.setLayoutParams(lRelLayoutParms);
        lLinLayout.addView(lRelLayout);

        mVideoView = new VideoView(this);
        mVideoView.setId(3);
        android.widget.RelativeLayout.LayoutParams lVidViewLayoutParams = 
        		new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lVidViewLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        mVideoView.setLayoutParams(lVidViewLayoutParams);
        lRelLayout.addView(mVideoView);
    	
        final MediaController lMediaController = new MediaController(LoaderPlayerActivity.this);
        mVideoView.setMediaController(lMediaController);
        mVideoView.setOnCompletionListener(new OnCompletionListener() {	
            public void onCompletion(MediaPlayer pMp) {
                /*LoaderPlayerActivity.this.mProgressBar.setVisibility(View.VISIBLE);
                LoaderPlayerActivity.this.mProgressMessage.setVisibility(View.VISIBLE);*/
            	LoaderPlayerActivity.this.animationDrawable.start();
            	LoaderPlayerActivity.this.mDialogLoader.show();
                currentPart+=1;
                if(currentPart > videoIds.size()) {
                	Toast.makeText(LoaderPlayerActivity.this, "本集已撥放完畢",  Toast.LENGTH_SHORT).show();
                	Bundle bundle = new Bundle();  
                    bundle.putInt("currentPart", currentPart);  
                    Intent intent = new Intent();  
                    intent.putExtras(bundle);  
                    setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);
                	LoaderPlayerActivity.this.finish();
                } else {
                	Toast.makeText(LoaderPlayerActivity.this, "即將撥放Part" + currentPart,  Toast.LENGTH_SHORT).show();
                    mQueryVideoTask = new QueryVideoTask();
                    if(Build.VERSION.SDK_INT < 11)
                    	mQueryVideoTask.execute(videoIds.get(currentPart-1));
                    else
                    	mQueryVideoTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1)); 
                }
            }	
        });
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {	
            public void onPrepared(MediaPlayer pMp) {
                /*LoaderPlayerActivity.this.mDialogLoader.setVisibility(View.GONE);
                LoaderPlayerActivity.this.mProgressMessage.setVisibility(View.GONE);*/
            	LoaderPlayerActivity.this.mDialogLoader.cancel();
            	LoaderPlayerActivity.this.animationDrawable.stop();
            }

        });
        
        mDialogLoader = new Dialog(this, R.style.dialogLoader);
        mDialogLoader.setContentView(R.layout.dialog_loader);
        mDialogLoader.setCanceledOnTouchOutside(false);
        mDialogLoader.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK
						&& event.getAction() == KeyEvent.ACTION_DOWN){
					if(mDialogLoader != null && mDialogLoader.isShowing())
						mDialogLoader.cancel();
					LoaderPlayerActivity.this.finish();
					return true;
				}
				return false;
			}			    	
        });
        
        mProgressImage = (ImageView)mDialogLoader.findViewById(R.id.imageview_progressbar);
        mProgressMessage = (TextView)mDialogLoader.findViewById(R.id.textview_load);
        rlAd = (RelativeLayout)mDialogLoader.findViewById(R.id.ad_layout);

        animationDrawable = (AnimationDrawable) mProgressImage.getBackground();        

        /*mProgressBar = new ProgressBar(this);
        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressBar.setEnabled(true);
        mProgressBar.setId(4);
        android.widget.RelativeLayout.LayoutParams lProgressBarLayoutParms = 
        		new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lProgressBarLayoutParms.addRule(RelativeLayout.CENTER_IN_PARENT);
        mProgressBar.setLayoutParams(lProgressBarLayoutParms);
        lRelLayout.addView(mProgressBar);

        mProgressMessage = new TextView(this);
        mProgressMessage.setId(5);
        android.widget.RelativeLayout.LayoutParams lProgressMsgLayoutParms = 
        		new android.widget.RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lProgressMsgLayoutParms.addRule(RelativeLayout.CENTER_HORIZONTAL);
        lProgressMsgLayoutParms.addRule(RelativeLayout.BELOW, 4);
        mProgressMessage.setLayoutParams(lProgressMsgLayoutParms);
        mProgressMessage.setTextColor(Color.LTGRAY);
        mProgressMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        mProgressMessage.setText("...");
        lRelLayout.addView(mProgressMessage);*/

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        if(animationDrawable != null && animationDrawable.isRunning())
        	animationDrawable.stop();
        
        if (mQueryVideoTask != null) {
            mQueryVideoTask.cancel(true);
        }

        if (mVideoView != null) {
            mVideoView.stopPlayback();
        }

        // clear the flag that keeps the screen ON
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.mQueryVideoTask = null;
        this.mVideoView = null;

    }

    public void updateProgress(String pProgressMsg) {
        try {
            mProgressMessage.setText(pProgressMsg);
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "Error updating video status!", e);
        }
    }

    private class ProgressUpdateInfo {

        public String mMsg;

        public ProgressUpdateInfo(String pMsg) {
            mMsg = pMsg;
        }
    }

    /**
     * Task to figure out details by calling out to YouTube GData API.  We only use public methods that
     * don't require authentication.
     */
    private class QueryVideoTask extends AsyncTask<String, ProgressUpdateInfo, Uri> {

        private String videoUrl = null;
        
        @Override
        protected Uri doInBackground(String... pParams) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            
            String lUriStr = null;
            String lYouTubeFmtQuality = "17";            
            String videoId = null;
            
            videoUrl = pParams[0];
            
            if (isCancelled())
                return null;
            publishProgress(new ProgressUpdateInfo(mMsgDetect));
            
            WifiManager lWifiManager = (WifiManager) LoaderPlayerActivity.this.getSystemService(Context.WIFI_SERVICE);
            TelephonyManager lTelephonyManager = (TelephonyManager) LoaderPlayerActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
            // if we have a fast connection (wifi or 3g), then we'll get a high quality YouTube video
            if ((lWifiManager.isWifiEnabled() && lWifiManager.getConnectionInfo() != null && lWifiManager.getConnectionInfo().getIpAddress() != 0) ||
                    ((lTelephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_UMTS ||
			   /* icky... using literals to make backwards compatible with 1.5 and 1.6 */
                            lTelephonyManager.getNetworkType() == 9 /*HSUPA*/ || 
                            lTelephonyManager.getNetworkType() == 10 /*HSPA*/ ||
                            lTelephonyManager.getNetworkType() == 8 /*HSDPA*/ ||
                            lTelephonyManager.getNetworkType() == 5 /*EVDO_0*/ ||
                            lTelephonyManager.getNetworkType() == 6 /*EVDO A*/) &&
                            lTelephonyManager.getDataState() == TelephonyManager.DATA_CONNECTED)
                    ) {
                lYouTubeFmtQuality = "18";
            }
            
            if (isCancelled())
                return null;
            publishProgress(new ProgressUpdateInfo("讀取中..."));

            Log.d("player", "videoUrl : " + videoUrl);
            // calculate the actual URL of the video, encoded with proper YouTube token
            if (videoUrl.contains("dailymotion.com/")) {
    			if(videoUrl.contains("embed/video/")) {
    				String url = videoUrl.substring(39);
        			String[] tmpUrls = url.split("\\?");	            			
        			if(tmpUrls.length > 0)
        				videoId = tmpUrls[0];
    			} else if(videoUrl.contains("touch")) {
    				String url = videoUrl.substring(35);
        			String[] tmpUrls = url.split("&");	            			
        			if(tmpUrls.length > 0)
        				videoId = tmpUrls[0];
    			}else {
    				String url = videoUrl.substring(33);
        			String[] tmpUrls = url.split("&");	            			
        			if(tmpUrls.length > 0)
        				videoId = tmpUrls[0];
    			}
    			if(videoId != null) {
    				videoUrl = "http://www.dailymotion.com/embed/video/" + videoId;
    				lUriStr = DailymotionLoader.Loader(videoUrl);
    			}
    		} else if (videoUrl.contains("http://www.youtube.com/")) {
    			String[] tmpUrls = videoUrl.split("v=");
    			Log.d("player", tmpUrls[1]);
    			if(tmpUrls.length > 1)
    				videoId = tmpUrls[1];
    			if(videoId != null)
    				//lUriStr = YoutubeLoader.Loader(lYouTubeFmtQuality, true, videoId);
    				lUriStr = YoutubeLoader.calculateYouTubeUrl(lYouTubeFmtQuality, true, videoId);
    		}
            Log.d("player", "videoId : " + videoId);
            
            if (lUriStr != null) {
            	return Uri.parse(lUriStr);
            } else {
                return null;
            }
            
        }

        @Override
        protected void onProgressUpdate(ProgressUpdateInfo... pValues) {
            super.onProgressUpdate(pValues);

            LoaderPlayerActivity.this.updateProgress(pValues[0].mMsg);
        }

        @Override
        protected void onPostExecute(Uri pResult) {
            super.onPostExecute(pResult);

            try {
                if (isCancelled())
                    return;

                if (pResult == null) {
                    Uri uri = Uri.parse(videoUrl);
            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
            		startActivity(it);
                    throw new RuntimeException("Invalid NULL Url.");
                } else {

                	if (isCancelled())
	                    return;
                	
                	playVideo(pResult);
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error playing video!", e);
                Bundle bundle = new Bundle();  
                bundle.putInt("currentPart", currentPart);  
                Intent intent = new Intent();  
                intent.putExtras(bundle);  
                setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                LoaderPlayerActivity.this.finish();
            }
        }
    }

    private void playVideo(Uri uri) {
    	mVideoView.stopPlayback();
    	mVideoView.clearFocus();	                            
    	mVideoView.setVideoURI(uri);    
        mVideoView.requestFocus();
        mVideoView.start();
     }
    
    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
    protected void onPause() {
    	mVideoView.suspend();
    	super.onPause();
    }

    protected void onResume() {
    	mVideoView.resume();
    	super.onResume();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Bundle bundle = new Bundle();  
            bundle.putInt("currentPart", currentPart);  
            Intent intent = new Intent();  
            intent.putExtras(bundle);  
            setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
            LoaderPlayerActivity.this.finish(); 
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}