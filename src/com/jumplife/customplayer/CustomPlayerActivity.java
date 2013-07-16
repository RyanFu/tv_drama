package com.jumplife.customplayer;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;


import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.jumplife.tvdrama.DramaSectionActivity;
import com.jumplife.tvdrama.R;
import com.jumplife.videoloader.DailymotionLoader;
import com.jumplife.videoloader.YoutubeLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CustomPlayerActivity extends Activity 
		implements 	SurfaceHolder.Callback,
					VideoControllerView.MediaPlayerControl,
					MediaPlayer.OnPreparedListener,
					MediaPlayer.OnCompletionListener,
					MediaPlayer.OnErrorListener { //AdWhirlInterface

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
    protected ImageView mProgressImage;
    protected TextView mProgressMessage;
	private AnimationDrawable animationDrawable;
    private RelativeLayout rlAd;
	//private AdWhirlLayout adWhirlLayout;
    
    private boolean youtubeHightQuality = false;
    private HashMap<String, String> YoutubeQuiltyLink = new HashMap<String, String>();;
    private int currentPart = 1;
    private static int stopPosition = 0;
    private ArrayList<String> videoIds = new ArrayList<String>();
    private AdView adView;
    
    SurfaceView videoSurface;
    MediaPlayer player;
    VideoControllerView controller;


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setSurfaceViewSize();
    }
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
        setContentView(R.layout.activity_custom_player);
        
        initView();
        extractMessages();
        
        mProgressMessage.setText(mMsgInit);
        mProgressImage.post(new Runnable() {
		    @Override
		    public void run() {
		        animationDrawable.start();
		    }
		});
        mDialogLoader.show();
    
        Bundle extra = getIntent().getExtras();//.getStringArrayListExtra("");
        currentPart = extra.getInt("currentPart", 1);
        videoIds = extra.getStringArrayList("videoIds");
        
        if (videoIds == null || videoIds.size() <= 0) {
            Log.i(this.getClass().getSimpleName(), "Unable to extract video ID from the intent.  Closing video activity.");
            CustomPlayerActivity.this.finish();
        }
        
        mQueryVideoTask = new QueryVideoTask();
        if(Build.VERSION.SDK_INT < 11)
        	mQueryVideoTask.execute(videoIds.get(currentPart-1));
        else
        	mQueryVideoTask.executeOnExecutor(QueryVideoTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1));
    }

    private void setSurfaceViewSize() {
    	DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        float boxWidth = width;
        float boxHeight = height;

        float videoWidth = player.getVideoWidth();
        float videoHeight = player.getVideoHeight();

        float wr = boxWidth / videoWidth;
        float hr = boxHeight / videoHeight;
        float ar = videoWidth / videoHeight;

        if (wr > hr)
            width = (int) (boxHeight * ar);
        else
            height = (int) (boxWidth / ar);
        
        Log.d("", "height : " + height + " width : " + width);
        Log.d("", "height : " + height + " width : " + width);
        
        android.view.ViewGroup.LayoutParams lp = videoSurface.getLayoutParams();
        lp.width = width;
        lp.height = height;
        videoSurface.setLayoutParams(lp);
    }
    
    @SuppressWarnings("deprecation")
	private void initView() {
        
        videoSurface = (SurfaceView) findViewById(R.id.videoSurface);
        controller = new VideoControllerView(this);
        player = new MediaPlayer();
        
        SurfaceHolder videoHolder = videoSurface.getHolder();
        videoHolder.setFixedSize(0, 0);
        videoHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        videoHolder.addCallback(this);
        
        //controller.setMediaPlayer(this);
        controller.setAnchorView((FrameLayout)findViewById(R.id.videoSurfaceContainer));        
        
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnCompletionListener(this);
        player.setOnPreparedListener(this);
        player.setOnErrorListener(this);
        
        mDialogLoader = new Dialog(this, R.style.dialogLoader);
        mDialogLoader.setContentView(R.layout.dialog_loader);
        mDialogLoader.setCanceledOnTouchOutside(false);
        mDialogLoader.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_BACK
						&& event.getAction() == KeyEvent.ACTION_DOWN){
					if(player.isPlaying())
						player.stop();
					if(mDialogLoader != null && mDialogLoader.isShowing())
						mDialogLoader.cancel();
					CustomPlayerActivity.this.finish();
					return true;
				}
				return false;
			}			    	
        });
        
        mProgressImage = (ImageView)mDialogLoader.findViewById(R.id.imageview_progressbar);
        mProgressMessage = (TextView)mDialogLoader.findViewById(R.id.textview_load);
        rlAd = (RelativeLayout)mDialogLoader.findViewById(R.id.ad_layout);

        animationDrawable = (AnimationDrawable) mProgressImage.getBackground();        
        
        setAd();
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
    	controller.show();
	    
        return false;
    }

    // Implement SurfaceHolder.Callback
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    	player.setDisplay(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        
    }
    // End SurfaceHolder.Callback



	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		if(mp.isPlaying())
			mp.stop();
		/*Builder alertDialog = new AlertDialog.Builder(CustomPlayerActivity.this);
		alertDialog.setTitle("此段影片播放錯誤")
			.setPositiveButton("下一段", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mProgressImage.post(new Runnable() {
	        		    @Override
	        		    public void run() {
	        		        animationDrawable.start();
	        		    }
	        		});
					CustomPlayerActivity.this.mDialogLoader.show();
	                currentPart+=1;
	                if(currentPart > videoIds.size()) {
	                	Toast.makeText(CustomPlayerActivity.this, "本集已撥放完畢",  Toast.LENGTH_SHORT).show();
	                	Bundle bundle = new Bundle();  
	                    bundle.putInt("currentPart", currentPart);  
	                    Intent intent = new Intent();  
	                    intent.putExtras(bundle);  
	                    setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);
	                    CustomPlayerActivity.this.finish();
	                } else {
	                	Toast.makeText(CustomPlayerActivity.this, "即將撥放Part" + currentPart,  Toast.LENGTH_SHORT).show();
	                    mQueryVideoTask = new QueryVideoTask();
	                    if(Build.VERSION.SDK_INT < 11)
	                    	mQueryVideoTask.execute(videoIds.get(currentPart-1));
	                    else
	                    	mQueryVideoTask.executeOnExecutor(QueryVideoTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1));
	                }
				}        						
			})
			.setNeutralButton("外部撥放",  new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					Uri uri = Uri.parse(videoIds.get(currentPart-1));
            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
            		startActivity(it);
            		
        			Bundle bundle = new Bundle();  
                    bundle.putInt("currentPart", currentPart);  
                    Intent intent = new Intent();  
                    intent.putExtras(bundle);  
                    setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                    CustomPlayerActivity.this.finish(); 
				}
			})
			.setNegativeButton("結束", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Bundle bundle = new Bundle();  
		            bundle.putInt("currentPart", currentPart);  
		            Intent intent = new Intent();  
		            intent.putExtras(bundle);  
		            setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
		            CustomPlayerActivity.this.finish(); 
				}        						
			})
			.show();*/
		Uri uri = Uri.parse(videoIds.get(currentPart-1));
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(it);
		
		Bundle bundle = new Bundle();  
        bundle.putInt("currentPart", currentPart);  
        Intent intent = new Intent();  
        intent.putExtras(bundle);  
        setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
        CustomPlayerActivity.this.finish(); 
					
        return true;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		mProgressImage.post(new Runnable() {
		    @Override
		    public void run() {
		        animationDrawable.start();
		    }
		});
    	CustomPlayerActivity.this.mDialogLoader.show();
        currentPart+=1;
        if(currentPart > videoIds.size()) {
        	Toast.makeText(CustomPlayerActivity.this, "本集已撥放完畢",  Toast.LENGTH_SHORT).show();
        	Bundle bundle = new Bundle();  
            bundle.putInt("currentPart", currentPart);  
            Intent intent = new Intent();  
            intent.putExtras(bundle);  
            setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);
            CustomPlayerActivity.this.finish();
        } else {
        	Toast.makeText(CustomPlayerActivity.this, "即將撥放Part" + currentPart,  Toast.LENGTH_SHORT).show();
            mQueryVideoTask = new QueryVideoTask();
            if(Build.VERSION.SDK_INT < 11)
            	mQueryVideoTask.execute(videoIds.get(currentPart-1));
            else
            	mQueryVideoTask.executeOnExecutor(QueryVideoTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1)); 
        }
	}

	@Override
	public void onPrepared(MediaPlayer mp) {
		player.seekTo(stopPosition);
        player.start();
    	CustomPlayerActivity.this.mDialogLoader.cancel();
    	CustomPlayerActivity.this.animationDrawable.stop();
		setSurfaceViewSize();
	}
	
    // Implement VideoMediaController.MediaPlayerControl
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
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return player.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return player.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return player.isPlaying();
    }

    @Override
    public void pause() {
        player.pause();
    }

    @Override
    public void seekTo(int i) {
        player.seekTo(i);
    }

    @Override
    public void start() {
        player.start();
    }

    @Override
    public boolean isFullScreen() {
    	if(CustomPlayerActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		return false;
		} else {
			return true;
		}        
    }

    @Override
    public void toggleFullScreen() {
    	if(CustomPlayerActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		CustomPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			CustomPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				CustomPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		}, 2000);
    }
    // End VideoMediaController.MediaPlayerControl
    
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
            
            WifiManager lWifiManager = (WifiManager) CustomPlayerActivity.this.getSystemService(Context.WIFI_SERVICE);
            TelephonyManager lTelephonyManager = (TelephonyManager) CustomPlayerActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
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
            if (videoUrl.contains("dailymotion")) {
            	controller.mYoutubeQualitySwitch.setVisibility(View.INVISIBLE);
            	controller.mYoutubeQualitySwitch.setClickable(false);
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
    				YoutubeQuiltyLink = YoutubeLoader.Loader(lYouTubeFmtQuality, true, videoId);
    				if(YoutubeQuiltyLink != null && YoutubeQuiltyLink.size() > 0)
    					lUriStr = YoutubeQuiltyLink.get("medium");
    				//lUriStr = YoutubeLoader.calculateYouTubeUrl(lYouTubeFmtQuality, true, videoId);
    			}
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

            CustomPlayerActivity.this.updateProgress(pValues[0].mMsg);
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

                    if(videoUrl.contains("youtube") && YoutubeQuiltyLink.size() > 1) {
                    	controller.mYoutubeQualitySwitch.setVisibility(View.VISIBLE);
                    	controller.mYoutubeQualitySwitch.setClickable(true);
                    	controller.mYoutubeQualitySwitch.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								// show loading dialog
				            	mProgressImage.post(new Runnable() {
				        		    @Override
				        		    public void run() {
				        		        animationDrawable.start();
				        		    }
				        		});
				            	CustomPlayerActivity.this.mDialogLoader.show();
				            	
				            	// change quailty
				            	int msec = player.getCurrentPosition();
								if(!youtubeHightQuality) {
									controller.mYoutubeQualitySwitch.setBackgroundResource(R.drawable.hq_press);
									if(YoutubeQuiltyLink.containsKey("hd1080")) {
										playVideo(Uri.parse(YoutubeQuiltyLink.get("hd1080")), msec);
									} else if(YoutubeQuiltyLink.containsKey("hd720")) {
										playVideo(Uri.parse(YoutubeQuiltyLink.get("hd720")), msec);
									} else if(YoutubeQuiltyLink.containsKey("large")) {
										playVideo(Uri.parse(YoutubeQuiltyLink.get("large")), msec);
									}
								} else {
									controller.mYoutubeQualitySwitch.setBackgroundResource(R.drawable.hq_normal);
									playVideo(Uri.parse(YoutubeQuiltyLink.get("medium")), msec);
								}
								youtubeHightQuality = !youtubeHightQuality;
							}    						
    					});
    				} else {
    					controller.mYoutubeQualitySwitch.setVisibility(View.INVISIBLE);
    					controller.mYoutubeQualitySwitch.setClickable(false);
    				}

                	if (isCancelled())
	                    return;
                	
                	playVideo(pResult, 0);
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error playing video!", e);
                Bundle bundle = new Bundle();  
                bundle.putInt("currentPart", currentPart);  
                Intent intent = new Intent();  
                intent.putExtras(bundle);  
                setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                CustomPlayerActivity.this.finish();
            }
        }
    }

    private void playVideo(Uri uri, int msec) {
    	if(player.isPlaying())
    		player.stop();
    	player.reset();
    	try {
            player.setDataSource(CustomPlayerActivity.this, uri);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    	player.prepareAsync();
        stopPosition = msec;
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

    @Override
    protected void onDestroy() {
        if(animationDrawable != null && animationDrawable.isRunning())
        	animationDrawable.stop();
        
        if (mQueryVideoTask != null) {
            mQueryVideoTask.cancel(true);
        }

        if (player != null && player.isPlaying()) {
        	player.stop();
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
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
    
    protected void onPause() {
    	if(player != null) {
    		stopPosition = player.getCurrentPosition();
    		player.pause();
    	}
    	super.onPause();
    }
    
    @Override
    protected void onRestart() {
    	if(player != null) {
    		player.start();
    	}
        super.onRestart();
    }

    protected void onResume() {
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
            CustomPlayerActivity.this.finish(); 
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    /*
	@Override
	public void adWhirlGeneric() {
		// TODO Auto-generated method stub
		
	}
	*/

}