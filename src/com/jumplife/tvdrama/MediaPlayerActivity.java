package com.jumplife.tvdrama;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.customplayer.MediaControllerView;
import com.jumplife.sqlite.SQLiteTvDramaHelper;
import com.jumplife.tvdrama.DramaSectionActivity.LoadDataTask;
import com.jumplife.videoloader.DailymotionLoader;
import com.jumplife.videoloader.YoutubeLoader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnInfoListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("InlinedApi")
public class MediaPlayerActivity extends Activity implements MediaControllerView.MediaPlayerControl {

    public final static String MSG_INIT = "com.keyes.video.msg.init";
    protected String mMsgInit = "初始化";
    public final static String MSG_DETECT = "com.keyes.video.msg.detect";
    protected String mMsgDetect = "偵測頻寬";
    public final static String MSG_ERROR_TITLE = "com.keyes.video.msg.error.title";
    protected String mMsgErrorTitle = "連線錯誤";
    
    private final static String TAG = "MediaPlayerActivity";
    /**
     * Background task on which all of the interaction with YouTube is done
     */
    protected QueryVideoTask mQueryVideoTask;
    protected Dialog mDialogLoader;
    protected ImageView mProgressImage;
    protected TextView mProgressMessage;
	private AnimationDrawable animationDrawable;
    
	private SurfaceView videoSurface;
	private MediaPlayer player;
	private MediaControllerView controller;
    
    private final static int filter = 30000;
    
    private ArrayList<String> videoIds = new ArrayList<String>();
    private HashMap<String, String> YoutubeQuiltyLink = new HashMap<String, String>();;
    private boolean youtubeHightQuality = false;
    
    private int dramaId = 0;
    private int currentPart = 1;
    private static int stopPosition = 0;
    
    private Handler stopPositionHandler;
    private Runnable stopPositionRunnable;
    
    private AdView adView;
    private RelativeLayout rlAd;    
	//private AdWhirlLayout adWhirlLayout;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSurfaceViewSize();
    }

    private void setSurfaceViewSize() {
    	DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        float boxWidth = width;
        float boxHeight = height;

        if(player != null) {
	        float videoWidth = player.getVideoWidth();
	        float videoHeight = player.getVideoHeight();
	
	        float wr = boxWidth / videoWidth;
	        float hr = boxHeight / videoHeight;
	        float ar = videoWidth / videoHeight;
	
	        if (wr > hr)
	            width = (int) (boxHeight * ar);
	        else
	            height = (int) (boxWidth / ar);
	        
	        Log.d(TAG, "height : " + height + " width : " + width);
	        Log.d(TAG, "height : " + height + " width : " + width);
	        
	        android.view.ViewGroup.LayoutParams lp = videoSurface.getLayoutParams();
	        lp.width = width;
	        lp.height = height;
	        videoSurface.setLayoutParams(lp);
        }
    }
    
    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);
		
        JSONObject crittercismConfig = new JSONObject();
        try {
        	crittercismConfig.put("delaySendingAppLoad", true); // send app load data with Crittercism.sendAppLoadData()
            crittercismConfig.put("shouldCollectLogcat", true); // send logcat data for devices with API Level 16 and higher
        	crittercismConfig.put("includeVersionCode", true); // include version code in version name
        }
        catch (JSONException je){}
        //Crittercism.init(getApplicationContext(), "51ccf765558d6a0c25000003", crittercismConfig);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        /*
         * vitamio lib
        if (!LibsChecker.checkVitamioLibs(this))
			return;
         */
        
        setContentView(R.layout.activity_custom_player);
        
        initView();
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
   
    public void setAd() {
    	
    	
    	Resources res = getResources();
    	String admoblKey = res.getString(R.string.admob_key);
    	
    	// Create the adView
    	adView = new AdView(this, AdSize.BANNER, admoblKey);

    	// Add the adView to it
    	rlAd.addView(adView);
    	
    	// Initiate a generic request to load it with an ad
        adView.loadAd(new AdRequest());

    	/*
    	Resources res = getResources();
    	String adwhirlKey = res.getString(R.string.adwhirl_key);
    	
    	AdWhirlManager.setConfigExpireTimeout(1000 * 30); 
        AdWhirlTargeting.setTestMode(false);   		
        adWhirlLayout = new AdWhirlLayout(this, adwhirlKey);        
        adWhirlLayout.setAdWhirlInterface(this);    	
        adWhirlLayout.setGravity(Gravity.CENTER_HORIZONTAL);	 	
        rlAd.addView(adWhirlLayout);   	
		*/
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	if(event.getAction() == MotionEvent.ACTION_UP) {
    		if(!controller.isShowing()) {
	    		controller.show();
	    	} else {
	    		controller.hide();
	    	}
	    	return true;
    	}
        return false;
    }

    @SuppressWarnings("deprecation")
	private void initView() {
    	Bundle extras = getIntent().getExtras();
		if(extras != null) {
        	dramaId = extras.getInt("drama_id");
    		currentPart = extras.getInt("current_part", 1);
        	videoIds = extras.getStringArrayList("video_ids");
        }                
        if (videoIds == null || videoIds.size() <= 0) {
            MediaPlayerActivity.this.finish();
        }
		
    	SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
        SQLiteDatabase db = instance.getWritableDatabase();
        stopPosition = instance.getDramaTimeRecord(db, dramaId);
		db.close();
        instance.closeHelper();
        Log.d(TAG, "stop position : " + stopPosition);
        
		youtubeHightQuality = TvDramaApplication.shIO.getBoolean("youtube_quality", youtubeHightQuality);
		
        player = new MediaPlayer();        
        controller = new MediaControllerView(this);
		controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout)findViewById(R.id.videoSurfaceContainer));        
        controller.setOnTouchListener(new OnTouchListener(){
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if(event.getAction() == MotionEvent.ACTION_UP) {
		    		if(!controller.isShowing()) {
			    		controller.show();
			    	} else {
			    		controller.hide();
			    	}
			    	return true;
		    	}
		        return false;
			}        	
        });

		videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        SurfaceHolder videoHolder = videoSurface.getHolder();
        if(Build.VERSION.SDK_INT < 11)
        	videoHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        videoHolder.addCallback(new Callback() {
			@Override
			public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub				
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				player.setDisplay(holder);
		        
		        mQueryVideoTask = new QueryVideoTask();
		        if(Build.VERSION.SDK_INT < 11)
		        	mQueryVideoTask.execute(videoIds.get(currentPart-1));
		        else
		        	mQueryVideoTask.executeOnExecutor(QueryVideoTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1));
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				//holder.setFormat(PixelFormat.TRANSPARENT);
			}
        });

        
        /*
         * record stop position / second
         */
        stopPositionHandler = new Handler();
        stopPositionRunnable = new Runnable() {
            @Override
            public void run() {
                if(player != null && player.isPlaying())
                	stopPosition = (int) player.getCurrentPosition();
            	stopPositionHandler.postDelayed(this, 2000);
            }
        };
        /*
         * MediaPlayer Init
         */
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        /*player.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer arg0, int arg1) {
				if(player != null && isGetStopPosition)
					stopPosition = (int) player.getCurrentPosition();
				Log.d(TAG, "in buffer, stop position : " + stopPosition);
			}        	
        });*/
        player.setOnInfoListener(new OnInfoListener(){
			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				switch(what){  
		        case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE :  
		        case MediaPlayer.MEDIA_INFO_BUFFERING_START :
		        	if(MediaPlayerActivity.this.mDialogLoader != null && !MediaPlayerActivity.this.mDialogLoader.isShowing()) {
		        		showProgressImage();  
		        	}
		            break;
		        case MediaPlayer.MEDIA_INFO_BUFFERING_END :
		        	if(MediaPlayerActivity.this.mDialogLoader != null && MediaPlayerActivity.this.mDialogLoader.isShowing()) {
		        		cancelProgressImage();
		        	}
	            	break;
		        default :
		        	if(MediaPlayerActivity.this.mDialogLoader != null && !MediaPlayerActivity.this.mDialogLoader.isShowing()) {
		        		cancelProgressImage();
		        	}
	            	break;
		        }
		        return true;
			}
        });
        player.setOnPreparedListener(new OnPreparedListener() {	
        	@Override
        	public void onPrepared(MediaPlayer mp) {                
        		cancelProgressImage();

            	Log.d(TAG, "stop position : " + stopPosition);
            	player.seekTo(stopPosition);
            	player.start();
                setSurfaceViewSize();
                
                stopPositionHandler.post(stopPositionRunnable);
            }
        });
        player.setOnCompletionListener(new OnCompletionListener() {	
			@Override
			public void onCompletion(MediaPlayer mp) {
				stopPositionHandler.removeCallbacks(stopPositionRunnable);
				
		    	if(player != null && player.isPlaying()) {
					player.pause();
		    	}
				
		    	showProgressImage();
            	
            	if(stopPosition < player.getDuration() - filter) {                    
                    mQueryVideoTask = new QueryVideoTask();
                    if(Build.VERSION.SDK_INT < 11)
                    	mQueryVideoTask.execute(videoIds.get(currentPart-1));
                    else
                    	mQueryVideoTask.executeOnExecutor(QueryVideoTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1));  
				} else {
					stopPosition = 0;
					currentPart += 1;
	                if(currentPart > videoIds.size()) {
	                	Toast.makeText(MediaPlayerActivity.this, "本集已撥放完畢",  Toast.LENGTH_SHORT).show();
	                    Intent intent = new Intent();
	                    intent.putExtra("current_part_return", currentPart);
	                    setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);
	                	MediaPlayerActivity.this.finish();
	                } else {
	                	Toast.makeText(MediaPlayerActivity.this, "即將撥放Part" + currentPart,  Toast.LENGTH_SHORT).show();
	                    mQueryVideoTask = new QueryVideoTask();
	                    if(Build.VERSION.SDK_INT < 11)
	                    	mQueryVideoTask.execute(videoIds.get(currentPart-1));
	                    else
	                    	mQueryVideoTask.executeOnExecutor(QueryVideoTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1)); 
	                }
				}
			}	
        });
        player.setOnErrorListener(new OnErrorListener() {
        	@Override
        	public boolean onError(MediaPlayer mp, int what, int extra) {
				stopPositionHandler.removeCallbacks(stopPositionRunnable);
				
		    	if(player != null && player.isPlaying()) {
					player.pause();
		    	}
        		
        		Uri uri = Uri.parse(videoIds.get(currentPart-1));
        		Intent it = new Intent(Intent.ACTION_VIEW, uri);
        		startActivity(it);
        		
    			Bundle bundle = new Bundle();  
                bundle.putInt("current_part", currentPart);  
                Intent intent = new Intent();  
                intent.putExtras(bundle);  
                setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                MediaPlayerActivity.this.finish(); 
        					
                return true;
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
					if(player.isPlaying())
						player.pause();
					if(mDialogLoader != null && mDialogLoader.isShowing())
						mDialogLoader.cancel();
					MediaPlayerActivity.this.finish();
					return true;
				}
				return false;
			}			    	
        });
        
        mProgressImage = (ImageView)mDialogLoader.findViewById(R.id.imageview_progressbar);
        mProgressMessage = (TextView)mDialogLoader.findViewById(R.id.textview_load);

        animationDrawable = (AnimationDrawable) mProgressImage.getBackground();        
        

        extractMessages();        
        mProgressMessage.setText(mMsgInit);
        
        rlAd = (RelativeLayout)mDialogLoader.findViewById(R.id.ad_layout);
        setAd();

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
            
            WifiManager lWifiManager = (WifiManager) MediaPlayerActivity.this.getSystemService(Context.WIFI_SERVICE);
            TelephonyManager lTelephonyManager = (TelephonyManager) MediaPlayerActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
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

            Log.d(TAG, "videoUrl : " + videoUrl);
            // calculate the actual URL of the video, encoded with proper YouTube token
            if (videoUrl.contains("dailymotion")) {
            	controller.mYoutubeQualitySwitch.setVisibility(View.INVISIBLE);
            	controller.mYoutubeQualitySwitch.setClickable(false);
            	//lMediaController.imYoutubeQualitySwitch.setVisibility(View.GONE);
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
    		} else if (videoUrl.contains("youtube")) {
    			if(videoUrl.contains("youtube-nocookie")) {
    				String[] tmpUrls = videoUrl.split("\\/");
	    			if(tmpUrls.length > 0)
	    				videoId = tmpUrls[tmpUrls.length-1];
    			} else if(videoUrl.contains("embed")) {
    				String[] tmpUrls = videoUrl.split("\\/");	            			
        			if(tmpUrls.length > 0) {
        				String[] tmpId = tmpUrls[tmpUrls.length-1].split("\\?");
        				if(tmpId.length > 0)
        					videoId = tmpId[0];
        			}
    			} else {
	    			String[] tmpUrls = videoUrl.split("v=");
	    			if(tmpUrls.length > 1)
	    				videoId = tmpUrls[1];
    			}
    			if(videoId != null) {
    				YoutubeQuiltyLink.clear();
    				YoutubeQuiltyLink = YoutubeLoader.Loader(lYouTubeFmtQuality, true, videoId);
    				if(YoutubeQuiltyLink != null && YoutubeQuiltyLink.size() > 0)
    					lUriStr = YoutubeQuiltyLink.get("medium");
    			}
    		} else if (videoUrl.toLowerCase(Locale.getDefault()).contains(".mp4")) {
    			lUriStr = videoUrl;
    		}
            
            if (lUriStr != null) {
            	return Uri.parse(lUriStr);
            } else {
                return null;
            }
            
        }

        @Override
        protected void onProgressUpdate(ProgressUpdateInfo... pValues) {
            super.onProgressUpdate(pValues);

            MediaPlayerActivity.this.updateProgress(pValues[0].mMsg);
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
            		
        			Bundle bundle = new Bundle();  
                    bundle.putInt("current_part", currentPart);  
                    Intent intent = new Intent();  
                    intent.putExtras(bundle);  
                    setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                    MediaPlayerActivity.this.finish(); 
                    throw new RuntimeException("Invalid NULL Url.");
                } else {

                    if(videoUrl.contains("youtube") && YoutubeQuiltyLink.size() > 1) {
                    	controller.mYoutubeQualitySwitch.setVisibility(View.VISIBLE);
                    	controller.mYoutubeQualitySwitch.setClickable(true);
                    	controller.mYoutubeQualitySwitch.setOnClickListener(new OnClickListener() {
    					/*lMediaController.imYoutubeQualitySwitch.setVisibility(View.VISIBLE);
    					lMediaController.imYoutubeQualitySwitch.setOnClickListener(new OnClickListener() {*/
							@Override
							public void onClick(View arg0) {
								// show loading dialog
								showProgressImage();
				            	
				            	// change quailty
								if(!youtubeHightQuality) {
									//lMediaController.imYoutubeQualitySwitch.setBackgroundResource(R.drawable.hq_press);
									controller.mYoutubeQualitySwitch.setImageResource(R.drawable.hq_press);
									if(YoutubeQuiltyLink.containsKey("hd1080")) {
										playVideo(Uri.parse(YoutubeQuiltyLink.get("hd1080")));
									} else if(YoutubeQuiltyLink.containsKey("hd720")) {
										playVideo(Uri.parse(YoutubeQuiltyLink.get("hd720")));
									} else if(YoutubeQuiltyLink.containsKey("large")) {
										playVideo(Uri.parse(YoutubeQuiltyLink.get("large")));
									}
								} else {
									controller.mYoutubeQualitySwitch.setImageResource(R.drawable.hq_normal);
									playVideo(Uri.parse(YoutubeQuiltyLink.get("medium")));
								}
								youtubeHightQuality = !youtubeHightQuality;
								TvDramaApplication.shIO.edit().putBoolean("youtube_quality", youtubeHightQuality).commit();
							}    						
    					});
    				} else {
    					controller.mYoutubeQualitySwitch.setVisibility(View.INVISIBLE);
    	            	controller.mYoutubeQualitySwitch.setClickable(false);
    				}

                    controller.mFullscreenButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							toggleFullScreen();
						}
                    });
                    
                    if(currentPart > videoIds.size()-1)
                    	controller.ivNextPart.setVisibility(View.INVISIBLE);
                    else
                    	controller.ivNextPart.setVisibility(View.VISIBLE);
                    
                    if(currentPart < 2)
                    	controller.ivPrePart.setVisibility(View.INVISIBLE);
                    else
                    	controller.ivPrePart.setVisibility(View.VISIBLE);
                    
                    controller.ivNextPart.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							stopPositionHandler.removeCallbacks(stopPositionRunnable);
							
					    	if(player != null && player.isPlaying()) {
								player.pause();
					    	}
			        		
					    	showProgressImage();
					    	
			                currentPart += 1;
			                stopPosition = 0;
			                if(currentPart <= videoIds.size()) {
			                	Toast.makeText(MediaPlayerActivity.this, "即將撥放Part" + currentPart,  Toast.LENGTH_SHORT).show();
			                    mQueryVideoTask = new QueryVideoTask();
			                    if(Build.VERSION.SDK_INT < 11)
			                    	mQueryVideoTask.execute(videoIds.get(currentPart-1));
			                    else
			                    	mQueryVideoTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1)); 
			                }
						}
                    });
                    
                    controller.ivPrePart.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							stopPositionHandler.removeCallbacks(stopPositionRunnable);
							
					    	if(player != null && player.isPlaying()) {
								player.pause();
					    	}
							
					    	showProgressImage();
					    	
			                currentPart -= 1;
			                stopPosition = 0;
			                if(currentPart > 0) {
			                	Toast.makeText(MediaPlayerActivity.this, "即將撥放Part" + currentPart,  Toast.LENGTH_SHORT).show();
			                    mQueryVideoTask = new QueryVideoTask();
			                    if(Build.VERSION.SDK_INT < 11)
			                    	mQueryVideoTask.execute(videoIds.get(currentPart-1));
			                    else
			                    	mQueryVideoTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1)); 
			                }
						}
                    });
		            	
                	if (isCancelled())
	                    return;
                	
                	if(videoUrl.contains("youtube") && YoutubeQuiltyLink.size() > 1 && youtubeHightQuality) {
                		controller.mYoutubeQualitySwitch.setImageResource(R.drawable.hq_press);
						if(YoutubeQuiltyLink.containsKey("hd1080")) {
							playVideo(Uri.parse(YoutubeQuiltyLink.get("hd1080")));
						} else if(YoutubeQuiltyLink.containsKey("hd720")) {
							playVideo(Uri.parse(YoutubeQuiltyLink.get("hd720")));
						} else if(YoutubeQuiltyLink.containsKey("large")) {
							playVideo(Uri.parse(YoutubeQuiltyLink.get("large")));
						} else
		                	playVideo(pResult);
                	} else
                    	playVideo(pResult);
                	
                	timeToast();
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error playing video!", e);  
                Intent intent = new Intent();  
                intent.putExtra("current_part_return", currentPart);
                setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                MediaPlayerActivity.this.finish();
            }
        }
    }

    private void playVideo(Uri uri) {
		Log.d(TAG, "reset player");
    	player.reset();
    	try {
			player.setDataSource(uri.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        player.prepareAsync();
     }
    
    private void timeToast() {
    	String timeStr = "";
    	int hou = stopPosition / (1000 * 60 * 60);    	
    	if(hou != 0)
    		timeStr = timeStr + hou + "時";
    	
    	int min = (stopPosition - hou * (1000 * 60 * 60)) / (1000 * 60);
    	if(min != 0)
    		timeStr = timeStr + min + "分";
    	
    	int sec = (stopPosition - hou * (1000 * 60 * 60) - min * (1000 * 60)) / 1000;
    	if(sec != 0)
    		timeStr = timeStr + sec + "秒";
    	
    	if(timeStr == "")
    		timeStr = "頭";
    		
    	String message = "Part" + currentPart + " 將從 " + timeStr + " 開始撥放";
    	Toast.makeText(this, message,  Toast.LENGTH_SHORT).show();
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    protected void onResume() {
    	showProgressImage();
    	super.onResume();
    }
    
    protected void onPause() {
		stopPositionHandler.removeCallbacks(stopPositionRunnable);
		
    	if(player != null && player.isPlaying()) {
			player.pause();
    	}
    	super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    protected void onDestroy() {
		stopPositionHandler.removeCallbacks(stopPositionRunnable);			
        Log.d(TAG, "stop position : " + stopPosition);

    	SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
        SQLiteDatabase db = instance.getWritableDatabase();
        instance.updateDramaTimeRecord(db, dramaId, stopPosition);
		db.close();
        instance.closeHelper();
        
        if(animationDrawable != null && animationDrawable.isRunning())
        	animationDrawable.stop();
        
        if (mQueryVideoTask != null) {
            mQueryVideoTask.cancel(true);
        }

        if (player != null) {
            player.release();
        }

        // clear the flag that keeps the screen ON
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        this.mQueryVideoTask = null;
        this.player = null;
        
        if (adView != null) {
            adView.destroy();
        }
        
        super.onDestroy();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            Intent intent = new Intent();
            intent.putExtra("current_part_return", currentPart);
            setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
            MediaPlayerActivity.this.finish(); 
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
    
    private void showProgressImage() {
    	mProgressImage.post(new Runnable() {
		    @Override
		    public void run() {
		        animationDrawable.start();
		    }
		});
    	MediaPlayerActivity.this.mDialogLoader.show();  
    }
    
    private void cancelProgressImage() {
    	MediaPlayerActivity.this.mDialogLoader.cancel();
    	MediaPlayerActivity.this.animationDrawable.stop();
    }

	@Override
	public void start() {
		player.start();		
	}

	@Override
	public void pause() {
		player.pause();
	}

	@Override
	public void showLoading() {
		showProgressImage();
	}

	@Override
	public void hideLoading() {
		cancelProgressImage();
	}

	@Override
	public int getDuration() {
		int duration = 0;
		if(player != null)
			duration = player.getDuration();
        return duration;
	}

	@Override
	public int getCurrentPosition() {
		int position = 0;
		if(player != null)
			position = player.getCurrentPosition();
		return position;
	}

	@Override
	public void seekTo(int pos) {
		player.seekTo(pos);
	}

	@Override
	public boolean isPlaying() {
		boolean isPlay = false;
		if(player != null)
			isPlay = player.isPlaying();
		return isPlay;
	}

	@Override
	public int getBufferPercentage() {
		return 0;
	}

	@Override
	public boolean canPause() {
		return true;
	}

	@Override
	public boolean canSeekBackward() {
		return true;
	}

	@Override
	public boolean canSeekForward() {
		return true;
	}

	@Override
	public boolean isFullScreen() {
		if(MediaPlayerActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		return false;
		} else {
			return true;
		}
	}

	@Override
	public void toggleFullScreen() {
		if(MediaPlayerActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			MediaPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			MediaPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				MediaPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		}, 2000);
	}
}