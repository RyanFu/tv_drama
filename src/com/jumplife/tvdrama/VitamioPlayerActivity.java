package com.jumplife.tvdrama;

import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnErrorListener;
import io.vov.vitamio.MediaPlayer.OnInfoListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.VideoView;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.customplayer.VitamioControllerView;
import com.jumplife.sqlite.SQLiteTvDramaHelper;
import com.jumplife.tvdrama.DramaSectionActivity.LoadDataTask;
import com.jumplife.videoloader.DailymotionLoader;
import com.jumplife.videoloader.FiveSixLoader;
import com.jumplife.videoloader.YoutubeLoader;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
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
public class VitamioPlayerActivity extends Activity implements VitamioControllerView.MediaPlayerControl {

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
    
	private VideoView mVideoView;
    VitamioControllerView controller;
    
    private final static int filter = 30000;
    
    private ArrayList<String> videoIds = new ArrayList<String>();
	ArrayList<String> VideoQuiltyLink = new ArrayList<String>(2);
    //private HashMap<String, String> YoutubeQuiltyLink = new HashMap<String, String>();;
    private boolean hightQuality = false;
    
    private int dramaId = 0;
    private int currentPart = 1;
    private static int stopPosition = 0;
    
    private AdView adView;
    private RelativeLayout rlAd;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);      
        /*
         * vitamio lib
         */
        if(mVideoView != null) {
        	float aspectRatio = mVideoView.getVideoAspectRatio();
        	mVideoView.setVideoLayout(VideoView.VIDEO_LAYOUT_SCALE, aspectRatio);
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
         */
        if (!LibsChecker.checkVitamioLibs(this))
			return;
        
        setContentView(R.layout.activity_vitamio_player);
        
        initView();
        
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

    private void initView() {
    	Bundle extras = getIntent().getExtras();
		if(extras != null) {
        	dramaId = extras.getInt("drama_id");
    		currentPart = extras.getInt("current_part", 1);
        	videoIds = extras.getStringArrayList("video_ids");
        }                
        if (videoIds == null || videoIds.size() <= 0) {
            VitamioPlayerActivity.this.finish();
        }
		
    	SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
        SQLiteDatabase db = instance.getWritableDatabase();
        stopPosition = instance.getDramaTimeRecord(db, dramaId);
		db.close();
        instance.closeHelper();
        Log.d(null, "stop position : " + stopPosition);
        
		hightQuality = TvDramaApplication.shIO.getBoolean("youtube_quality", hightQuality);

        controller = new VitamioControllerView(this);
        mVideoView = (VideoView)findViewById(R.id.videoview);
        
        controller.setMediaPlayer(mVideoView);
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

        /*
         * Init VideoView
         */
        mVideoView.setOnInfoListener(new OnInfoListener(){
			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				stopPosition = (int) mp.getCurrentPosition();
		        return true;
			}
        });
        mVideoView.setOnPreparedListener(new OnPreparedListener() {	
        	@Override
        	public void onPrepared(MediaPlayer pMp) {
                mVideoView.start();
                mVideoView.seekTo(stopPosition);
        		cancelProgressImage();
            }
        });
        mVideoView.setOnCompletionListener(new OnCompletionListener() {	
			@Override
			public void onCompletion(MediaPlayer mp) {
				
        		if(mVideoView.isPlaying())
        			mVideoView.pause();
				
        		showProgressImage();
            	
            	if(stopPosition < mVideoView.getDuration() - filter) {                    
                    mQueryVideoTask = new QueryVideoTask();
                    if(Build.VERSION.SDK_INT < 11)
                    	mQueryVideoTask.execute(videoIds.get(currentPart-1));
                    else
                    	mQueryVideoTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1));  
				} else {
					stopPosition = 0;
					currentPart += 1;
	                if(currentPart > videoIds.size()) {
	                	Toast.makeText(VitamioPlayerActivity.this, "本集已撥放完畢",  Toast.LENGTH_SHORT).show();
	                    Intent intent = new Intent();
	                    intent.putExtra("current_part_return", currentPart);
	                    setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);
	                	VitamioPlayerActivity.this.finish();
	                } else {
	                	Toast.makeText(VitamioPlayerActivity.this, "即將撥放Part" + currentPart,  Toast.LENGTH_SHORT).show();
	                    mQueryVideoTask = new QueryVideoTask();
	                    if(Build.VERSION.SDK_INT < 11)
	                    	mQueryVideoTask.execute(videoIds.get(currentPart-1));
	                    else
	                    	mQueryVideoTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, videoIds.get(currentPart-1)); 
	                }
				}
			}	
        });
        mVideoView.setOnErrorListener(new OnErrorListener() {
        	@Override
        	public boolean onError(MediaPlayer mp, int what, int extra) {
				
        		if(mVideoView.isPlaying())
        			mVideoView.pause();
        		
        		Uri uri = Uri.parse(videoIds.get(currentPart-1));
        		Intent it = new Intent(Intent.ACTION_VIEW, uri);
        		startActivity(it);
        		
    			Bundle bundle = new Bundle();  
                bundle.putInt("current_part", currentPart);  
                Intent intent = new Intent();  
                intent.putExtras(bundle);  
                setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                VitamioPlayerActivity.this.finish(); 
        					
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
					if(mVideoView.isPlaying())
						mVideoView.pause();
					if(mDialogLoader != null && mDialogLoader.isShowing())
						mDialogLoader.cancel();
					VitamioPlayerActivity.this.finish();
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
    private class QueryVideoTask extends AsyncTask<String, ProgressUpdateInfo, String> {

        private String videoUrl = null;
        
        @Override
        protected String doInBackground(String... pParams) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	
            String videoId = null;
            
            videoUrl = pParams[0];
            
            if (isCancelled())
                return null;
            publishProgress(new ProgressUpdateInfo(mMsgDetect));
            
            if (isCancelled())
                return null;
            publishProgress(new ProgressUpdateInfo("讀取中..."));
            
            VideoQuiltyLink.clear();
            // calculate the actual URL of the video, encoded with proper YouTube token
            if (videoUrl.contains("dailymotion")) {
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
    				VideoQuiltyLink = DailymotionLoader.Loader(videoUrl);
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
    				VideoQuiltyLink = YoutubeLoader.Loader(true, videoId);
    			}
    		} else if (videoUrl.toLowerCase(Locale.getDefault()).contains(".mp4")) {
    			VideoQuiltyLink.add(videoUrl);
    		} else if (videoUrl.toLowerCase(Locale.getDefault()).contains(".tudou") ||
    				videoUrl.toLowerCase(Locale.getDefault()).contains("youku")) {
    			VideoQuiltyLink.add(videoUrl);
    		} else if (videoUrl.toLowerCase(Locale.getDefault()).contains(".flv")) {
    			VideoQuiltyLink.add(videoUrl);
    		} else if (videoUrl.toLowerCase(Locale.getDefault()).contains("56.com") ||
    				videoUrl.toLowerCase(Locale.getDefault()).contains("56.pptv.com")) {
    			VideoQuiltyLink = FiveSixLoader.Loader(videoUrl);
    		}
			return null;
        }

        @Override
        protected void onProgressUpdate(ProgressUpdateInfo... pValues) {
            super.onProgressUpdate(pValues);
            VitamioPlayerActivity.this.updateProgress(pValues[0].mMsg);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            try {
                if (isCancelled())
                    return;

                if (VideoQuiltyLink == null || VideoQuiltyLink.size() < 1) {
                    Uri uri = Uri.parse(videoUrl);
            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
            		startActivity(it);
            		
        			Bundle bundle = new Bundle();  
                    bundle.putInt("current_part", currentPart);  
                    Intent intent = new Intent();  
                    intent.putExtras(bundle);  
                    setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                    VitamioPlayerActivity.this.finish(); 
                    throw new RuntimeException("Invalid NULL Url.");
                } else {

                    if(VideoQuiltyLink.size() > 1) {
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
								if(!hightQuality) {
									controller.mYoutubeQualitySwitch.setImageResource(R.drawable.hq_press);
									playVideo(Uri.parse(VideoQuiltyLink.get(0)));
								} else {
									controller.mYoutubeQualitySwitch.setImageResource(R.drawable.hq_normal);
									playVideo(Uri.parse(VideoQuiltyLink.get(VideoQuiltyLink.size()-1)));
								}
								hightQuality = !hightQuality;
								TvDramaApplication.shIO.edit().putBoolean("youtube_quality", hightQuality).commit();
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
							
			        		if(mVideoView.isPlaying())
			        			mVideoView.pause();
							
			        		showProgressImage();
			                currentPart += 1;
			                stopPosition = 0;
			                if(currentPart <= videoIds.size()) {
			                	Toast.makeText(VitamioPlayerActivity.this, "即將撥放Part" + currentPart,  Toast.LENGTH_SHORT).show();
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
							
			        		if(mVideoView.isPlaying())
			        			mVideoView.pause();
							
			        		showProgressImage();
			        		
			                currentPart -= 1;
			                stopPosition = 0;
			                if(currentPart > 0) {
			                	Toast.makeText(VitamioPlayerActivity.this, "即將撥放Part" + currentPart,  Toast.LENGTH_SHORT).show();
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
                	

                	
                	if(hightQuality) {
						controller.mYoutubeQualitySwitch.setImageResource(R.drawable.hq_press);
						playVideo(Uri.parse(VideoQuiltyLink.get(0)));
					} else {
						controller.mYoutubeQualitySwitch.setImageResource(R.drawable.hq_normal);
						playVideo(Uri.parse(VideoQuiltyLink.get(VideoQuiltyLink.size()-1)));
					}
                	
                	timeToast();
                }
            } catch (Exception e) {
                Log.e(this.getClass().getSimpleName(), "Error playing video!", e);  
                Intent intent = new Intent();  
                intent.putExtra("current_part_return", currentPart);
                setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                VitamioPlayerActivity.this.finish();
            }
        }
    }

    private void playVideo(Uri uri) {
    	mVideoView.clearFocus();
        mVideoView.setSaveEnabled(true);
    	mVideoView.setVideoURI(uri);    
        mVideoView.requestFocus();
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
    	if(mVideoView != null) {
    	    /*mVideoView.seekTo(stopPosition);    
    	    mVideoView.start();*/
    		mVideoView.requestFocus();
    	}
    	super.onResume();
    }
    
    protected void onPause() {
    	if(mVideoView != null) {
    		mVideoView.pause();
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
    	
        Log.d(null, "stop position : " + stopPosition);
    	
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

        if (mVideoView != null && mVideoView.isPlaying()) {
            mVideoView.stopPlayback();
        }

        // clear the flag that keeps the screen ON
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(this.mVideoView != null && this.mVideoView.isFocused())
        	this.mVideoView.clearFocus();
        this.mVideoView = null;
        this.mQueryVideoTask = null;
        
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
            VitamioPlayerActivity.this.finish(); 
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
    
    private void showProgressImage() {
    	if(VitamioPlayerActivity.this.mDialogLoader != null && !VitamioPlayerActivity.this.mDialogLoader.isShowing()) {
	    	mProgressImage.post(new Runnable() {
			    @Override
			    public void run() {
			        animationDrawable.start();
			    }
			});
	    	VitamioPlayerActivity.this.mDialogLoader.show();  
    	}
    }
    
    private void cancelProgressImage() {
    	if(VitamioPlayerActivity.this.mDialogLoader != null && VitamioPlayerActivity.this.mDialogLoader.isShowing()) {
	    	VitamioPlayerActivity.this.mDialogLoader.cancel();
	    	VitamioPlayerActivity.this.animationDrawable.stop();
    	}
    }


	@Override
	public void start() {
		mVideoView.start();		
	}

	@Override
	public void pause() {
		mVideoView.pause();
	}

	@Override
	public int getDuration() {
        return (int) mVideoView.getDuration();
	}

	@Override
	public int getCurrentPosition() {
		return (int) mVideoView.getCurrentPosition();
	}

	@Override
	public void seekTo(int pos) {
		mVideoView.seekTo(pos);
	}

	@Override
	public boolean isPlaying() {
		return mVideoView.isPlaying();
	}

	@Override
	public int getBufferPercentage() {
		return mVideoView.getBufferPercentage();
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
		if(VitamioPlayerActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		return false;
		} else {
			return true;
		}
	}

	@Override
	public void toggleFullScreen() {
		if(VitamioPlayerActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			VitamioPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			VitamioPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				VitamioPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		}, 2000);
	}
}