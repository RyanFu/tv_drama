package com.jumplife.tvdrama;

import java.util.ArrayList;
import java.util.HashMap;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlManager;
import com.adwhirl.AdWhirlTargeting;
import com.adwhirl.AdWhirlLayout.AdWhirlInterface;
import com.bugsense.trace.BugSenseHandler;
import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.customplayer.VideoControllerView;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
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
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

@SuppressLint("InlinedApi")
public class LoaderPlayerActivity extends Activity implements AdWhirlInterface, VideoControllerView.MediaPlayerControl {

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
	private AdWhirlLayout adWhirlLayout;
    private VideoView mVideoView;
    VideoControllerView controller;
    //private Button imYoutubeQualitySwitch;
    //private ButtonMediaController lMediaController;
    
    private boolean youtubeHightQuality = false;
    private HashMap<String, String> YoutubeQuiltyLink = new HashMap<String, String>();;
    private int currentPart = 1;
    private static int stopPosition = 0;
    private ArrayList<String> videoIds = new ArrayList<String>();

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);        	
    }
    
    @Override
    protected void onCreate(Bundle pSavedInstanceState) {
        super.onCreate(pSavedInstanceState);

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //LoaderPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		//LoaderPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        
        setContentView(R.layout.activity_loader_player);
        BugSenseHandler.initAndStartSession(this, "72a249b7");
        
        initView();
        extractMessages();
        
        /*mProgressBar.bringToFront();
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressMessage.setText(mMsgInit);*/
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
    
    public void setAd() {
    	
    	Resources res = getResources();
    	String adwhirlKey = res.getString(R.string.adwhirl_key);
    	
    	AdWhirlManager.setConfigExpireTimeout(1000 * 30); 
        AdWhirlTargeting.setTestMode(false);   		
        adWhirlLayout = new AdWhirlLayout(this, adwhirlKey);        
        adWhirlLayout.setAdWhirlInterface(this);    	
        adWhirlLayout.setGravity(Gravity.CENTER_HORIZONTAL);	 	
        rlAd.addView(adWhirlLayout);   	

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

		SharePreferenceIO sharePreferenceIO = new SharePreferenceIO(LoaderPlayerActivity.this);
		youtubeHightQuality = sharePreferenceIO.SharePreferenceO("youtube_quality", youtubeHightQuality);

        controller = new VideoControllerView(this);
        mVideoView = (VideoView)findViewById(R.id.videoview);
        
        controller.setMediaPlayer(mVideoView);
        controller.setAnchorView((FrameLayout)findViewById(R.id.videoSurfaceContainer));

        mVideoView.setOnCompletionListener(new OnCompletionListener() {	
            public void onCompletion(MediaPlayer pMp) {
                /*LoaderPlayerActivity.this.mProgressBar.setVisibility(View.VISIBLE);
                LoaderPlayerActivity.this.mProgressMessage.setVisibility(View.VISIBLE);*/
            	mProgressImage.post(new Runnable() {
        		    @Override
        		    public void run() {
        		        animationDrawable.start();
        		    }
        		});
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
        mVideoView.setOnErrorListener(new OnErrorListener() {
        	@Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
        		if(mVideoView.isPlaying())
        			mVideoView.stopPlayback();
        		/*Builder alertDialog = new AlertDialog.Builder(LoaderPlayerActivity.this);
        		alertDialog.setTitle("此段影片播放錯誤")
					.setPositiveButton("下一段", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							EasyTracker.getTracker().trackEvent("連續撥放頁面", "影片撥放錯誤點擊", "下一段", (long)0);
							mProgressImage.post(new Runnable() {
			        		    @Override
			        		    public void run() {
			        		        animationDrawable.start();
			        		    }
			        		});
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
					})
					.setNeutralButton("外部撥放",  new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							EasyTracker.getTracker().trackEvent("連續撥放頁面", "影片撥放錯誤點擊", "外部撥放", (long)0);
							
							Uri uri = Uri.parse(videoIds.get(currentPart-1));
		            		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		            		startActivity(it);
		            		
		        			Bundle bundle = new Bundle();  
		                    bundle.putInt("currentPart", currentPart);  
		                    Intent intent = new Intent();  
		                    intent.putExtras(bundle);  
		                    setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
		                    LoaderPlayerActivity.this.finish(); 
						}
					})
					.setNegativeButton("段落列表", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							EasyTracker.getTracker().trackEvent("連續撥放頁面", "影片撥放錯誤點擊", "段落列表", (long)0);
							Bundle bundle = new Bundle();  
				            bundle.putInt("currentPart", currentPart);  
				            Intent intent = new Intent();  
				            intent.putExtras(bundle);  
				            setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
				            LoaderPlayerActivity.this.finish(); 
						}        						
					})
					.show();*/
            
        		//Uri uri = Uri.parse("http://www.youtube.com/watch?v=JW8DbZ49mEM");
        		Uri uri = Uri.parse(videoIds.get(currentPart-1));
        		Intent it = new Intent(Intent.ACTION_VIEW, uri);
        		startActivity(it);
        		
    			Bundle bundle = new Bundle();  
                bundle.putInt("currentPart", currentPart);  
                Intent intent = new Intent();  
                intent.putExtras(bundle);  
                setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                LoaderPlayerActivity.this.finish(); 
        					
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
						mVideoView.stopPlayback();
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
        
        setAd();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
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
            		
        			Bundle bundle = new Bundle();  
                    bundle.putInt("currentPart", currentPart);  
                    Intent intent = new Intent();  
                    intent.putExtras(bundle);  
                    setResult(DramaSectionActivity.LOADERPLAYER_CHANGE, intent);  
                    LoaderPlayerActivity.this.finish(); 
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
				            	mProgressImage.post(new Runnable() {
				        		    @Override
				        		    public void run() {
				        		        animationDrawable.start();
				        		    }
				        		});
				            	LoaderPlayerActivity.this.mDialogLoader.show();
				            	
				            	// change quailty
				            	int msec = mVideoView.getCurrentPosition();
								if(!youtubeHightQuality) {
									//lMediaController.imYoutubeQualitySwitch.setBackgroundResource(R.drawable.hq_press);
									controller.mYoutubeQualitySwitch.setImageResource(R.drawable.hq_press);
									if(YoutubeQuiltyLink.containsKey("hd1080")) {
										playVideo(Uri.parse(YoutubeQuiltyLink.get("hd1080")), msec);
									} else if(YoutubeQuiltyLink.containsKey("hd720")) {
										playVideo(Uri.parse(YoutubeQuiltyLink.get("hd720")), msec);
									} else if(YoutubeQuiltyLink.containsKey("large")) {
										playVideo(Uri.parse(YoutubeQuiltyLink.get("large")), msec);
									}
								} else {
									//lMediaController.imYoutubeQualitySwitch.setBackgroundResource(R.drawable.hq_normal);
									controller.mYoutubeQualitySwitch.setImageResource(R.drawable.hq_normal);
									playVideo(Uri.parse(YoutubeQuiltyLink.get("medium")), msec);
								}
								youtubeHightQuality = !youtubeHightQuality;
								SharePreferenceIO sharePreferenceIO = new SharePreferenceIO(LoaderPlayerActivity.this);
						    	sharePreferenceIO.SharePreferenceI("youtube_quality", youtubeHightQuality);
							}    						
    					});
    				} else {
    					//lMediaController.imYoutubeQualitySwitch.setVisibility(View.GONE);
    					controller.mYoutubeQualitySwitch.setVisibility(View.INVISIBLE);
    	            	controller.mYoutubeQualitySwitch.setClickable(false);
    				}

                    controller.mFullscreenButton.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							toggleFullScreen();
						}
                    });
		            	
                	if (isCancelled())
	                    return;
                	
                	if(videoUrl.contains("youtube") && YoutubeQuiltyLink.size() > 1 && youtubeHightQuality) {
                		controller.mYoutubeQualitySwitch.setImageResource(R.drawable.hq_press);
						if(YoutubeQuiltyLink.containsKey("hd1080")) {
							playVideo(Uri.parse(YoutubeQuiltyLink.get("hd1080")), 0);
						} else if(YoutubeQuiltyLink.containsKey("hd720")) {
							playVideo(Uri.parse(YoutubeQuiltyLink.get("hd720")), 0);
						} else if(YoutubeQuiltyLink.containsKey("large")) {
							playVideo(Uri.parse(YoutubeQuiltyLink.get("large")), 0);
						} else
		                	playVideo(pResult, 0);
                	} else
                    	playVideo(pResult, 0);
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

    private void playVideo(Uri uri, int msec) {
    	if(mVideoView.isPlaying())
    		mVideoView.stopPlayback();
    	mVideoView.clearFocus();
    	mVideoView.setVideoURI(uri);    
        mVideoView.requestFocus();
        mVideoView.start();
        mVideoView.seekTo(msec);
     }
    
    @Override
    protected void onStart() {
        super.onStart();
        BugSenseHandler.startSession(this);
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BugSenseHandler.closeSession(this);
        EasyTracker.getInstance().activityStop(this);
    }
    
    protected void onPause() {
    	if(mVideoView != null) {
    		stopPosition = mVideoView.getCurrentPosition();
    		mVideoView.pause();
    	}
    	super.onPause();
    }

    protected void onResume() {
    	if(mVideoView != null) {
    	    mVideoView.seekTo(stopPosition);    
    	    mVideoView.start();
    	}
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

	@Override
	public void adWhirlGeneric() {
		// TODO Auto-generated method stub
		
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
        return mVideoView.getDuration();
	}

	@Override
	public int getCurrentPosition() {
		return mVideoView.getCurrentPosition();
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
		if(LoaderPlayerActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
    		return false;
		} else {
			return true;
		}
	}

	@Override
	public void toggleFullScreen() {
		if(LoaderPlayerActivity.this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			LoaderPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		} else {
			LoaderPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				LoaderPlayerActivity.this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			}
		}, 2000);
	}
}