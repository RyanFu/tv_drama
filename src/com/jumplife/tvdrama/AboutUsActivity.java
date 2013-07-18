package com.jumplife.tvdrama;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.AppProject;
import com.jumplife.tvdrama.promote.PromoteAPP;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;

public class AboutUsActivity extends Activity {
	
	/*private LinearLayout llFeedback;
	private LinearLayout llFacebook;
	private LinearLayout llDeclare;
	private LinearLayout llNews;
	private LinearLayout llMoviediary;
	private LinearLayout llMovietime;
	private LinearLayout llTvVariety;*/
	
	private ImageView ivNotification;
	private ImageView ivRepeat;
	private LinearLayout llAboutUs;
	private ProgressBar pbInit;
	private ArrayList<AppProject> appProject;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
    private SharePreferenceIO  shIO;
    private static String updateDiary = "";
	
	private Activity mActivity;
	
	private LoadDataTask loadtask;
	private DiaryDataTask diaryDataTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		JSONObject crittercismConfig = new JSONObject();
        try {
        	crittercismConfig.put("delaySendingAppLoad", true); // send app load data with Crittercism.sendAppLoadData()
            crittercismConfig.put("shouldCollectLogcat", true); // send logcat data for devices with API Level 16 and higher
        	crittercismConfig.put("includeVersionCode", true); // include version code in version name
        }
        catch (JSONException je){}
        //Crittercism.init(getApplicationContext(), "51ccf765558d6a0c25000003", crittercismConfig);
        
		setContentView(R.layout.activity_aboutme);
		
		mActivity = this;
		
		options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.stub)
		.showImageForEmptyUri(R.drawable.stub)
		.showImageOnFail(R.drawable.stub)
		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		.cacheOnDisc()
		.cacheInMemory()
		.displayer(new SimpleBitmapDisplayer())
		.build();
		
		initView();
        loadtask = new LoadDataTask();
	    if(Build.VERSION.SDK_INT < 11)
	    	loadtask.execute();
        else
        	loadtask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
	    
		//setClickListener();
	}
	
	@Override
    public void onStart() {
      super.onStart();
      EasyTracker.getInstance().activityStart(mActivity);
    }
    
    @Override
    public void onStop() {
      super.onStop();
      EasyTracker.getInstance().activityStop(mActivity);
    }

	private void initView(){
		/*llFeedback = (LinearLayout)findViewById(R.id.ll_feedback);
		llFacebook = (LinearLayout)findViewById(R.id.ll_facebook);
		llDeclare = (LinearLayout)findViewById(R.id.ll_declare);
		llNews = (LinearLayout)findViewById(R.id.ll_news);
		llMoviediary = (LinearLayout)findViewById(R.id.ll_moviediary);
		llMovietime = (LinearLayout)findViewById(R.id.ll_movietime);
		llTvVariety = (LinearLayout)findViewById(R.id.ll_tvvariety);*/
        shIO = new SharePreferenceIO(this);
		//imageLoader = new ImageLoader(mActivity);
		
		pbInit = (ProgressBar)findViewById(R.id.pb_about_us);
		llAboutUs = (LinearLayout)findViewById(R.id.ll_aboutus);
        
		initBasicView();
	}
	
	private void initBasicView() {
		TableRow Schedule_row_first = new TableRow(mActivity);
		TableRow Schedule_row_second = new TableRow(mActivity);
		TableRow Schedule_row_third = new TableRow(mActivity);
		

        
        DisplayMetrics displayMetrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels / 3;
		TableRow.LayoutParams Params = new TableRow.LayoutParams
				(screenWidth, LayoutParams.MATCH_PARENT, 0.33f);				
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels / 6;
        LinearLayout.LayoutParams llIvParams = new LinearLayout.LayoutParams(width, width);
        //rlIvParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        //rlIvParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        llIvParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));				
		
		
		TextView tvFeed = new TextView(mActivity);
		ImageView ivFeed = new ImageView(mActivity);
		LinearLayout llFeed = new LinearLayout(mActivity);		
			
		ivFeed.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivFeed.setImageResource(R.drawable.feedback);
        llFeed.addView(ivFeed, llIvParams);
		
        LinearLayout.LayoutParams llTvFeedParams = new LinearLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//rlTvFeedParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		//rlTvFeedParams.addRule(RelativeLayout.BELOW, ivFeed.getId());
		//rlTvFeedParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		llTvFeedParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				0, 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
		tvFeed.setText(mActivity.getResources().getString(R.string.advice_and_feedback));
		tvFeed.setTextSize(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_title));
		//tvFeed.setTextColor(mActivity.getResources().getColor(R.color.about_us_tv));
		llFeed.addView(tvFeed, llTvFeedParams);		
		
		llFeed.setBackgroundResource(R.drawable.button_aboutus_bg);
		llFeed.setOrientation(LinearLayout.VERTICAL);
		llFeed.setGravity(Gravity.CENTER_HORIZONTAL);
		llFeed.setLayoutParams(Params);
		llFeed.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
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
    			
				//EasyTracker.getTracker().sendEvent("關於我們", "點擊", "建議回饋", (long)0);
				Uri uri = Uri.parse("mailto:jumplives@gmail.com");  
				String[] ccs={"abooyaya@gmail.com, raywu07@gmail.com, supermfb@gmail.com, form.follow.fish@gmail.com"};
				Intent it = new Intent(Intent.ACTION_SENDTO, uri);
				it.putExtra(Intent.EXTRA_CC, ccs); 
				it.putExtra(Intent.EXTRA_SUBJECT, "[電視連續劇] 建議回饋");
				it.putExtra(Intent.EXTRA_TEXT, "\n\n請詳述發生情況 : " +
									"\n\n\n\nAPP版本號 : " + tmpVersionCode +
	        						"\n\nAndroid版本號 : " + Build.VERSION.RELEASE +
	        						"\n\n裝置型號 : " + Build.MANUFACTURER + " " + Build.PRODUCT + "(" + Build.MODEL + ")");
				startActivity(it);  
			}			
		});
		Schedule_row_first.addView(llFeed);
		
		
		
		
		TextView tvDeclare = new TextView(mActivity);
		ImageView ivDeclare = new ImageView(mActivity);
		LinearLayout llDeclare = new LinearLayout(mActivity);		
			
		ivDeclare.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivDeclare.setImageResource(R.drawable.declare);
        llDeclare.addView(ivDeclare, llIvParams);
		
        LinearLayout.LayoutParams llTvDeclareParams = new LinearLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		//rlTvDeclareParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		//rlTvDeclareParams.addRule(RelativeLayout.BELOW, ivDeclare.getId());
		//rlTvDeclareParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
		llTvDeclareParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				0, 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
		tvDeclare.setText(mActivity.getResources().getString(R.string.liability_disclaimer));
		tvDeclare.setTextSize(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_title));
		//tvDeclare.setTextColor(mActivity.getResources().getColor(R.color.about_us_tv));
		llDeclare.addView(tvDeclare, llTvDeclareParams);		
		
		llDeclare.setBackgroundResource(R.drawable.button_aboutus_bg);
		llDeclare.setOrientation(LinearLayout.VERTICAL);
		llDeclare.setGravity(Gravity.CENTER_HORIZONTAL);
		llDeclare.setLayoutParams(Params);
		llDeclare.setOnClickListener(new OnClickListener(){
			@SuppressWarnings("deprecation")
			public void onClick(View arg0) {
				//EasyTracker.getTracker().sendEvent("關於我們", "點擊", "免責稱明", (long)0);
				AlertDialog dialog = new AlertDialog.Builder(mActivity).create();
		        dialog.setTitle(mActivity.getResources().getString(R.string.liability_disclaimer));
		        dialog.setMessage(Html.fromHtml("<b>電視連續劇為JumpLife所開發之第三方影音共享播放清單彙整軟體，作為影音內容" +
						"的索引和影視庫的發現，影片來源取自於網路上之Youtube、DailyMotion、WatTV等網站" +
        				"網址。電視連續劇僅提供搜尋結果，不會上傳任何影片，也不提供任何影片下載，更不會" +
        				"鼓勵他人自行上傳影片，所有影片僅供網絡測試，個人影視製作的學習，交流之用。電視" +
        				"連續劇不製播、不下載、不發布、不更改、不存儲任何節目，所有內容均由網友自行發佈" +
        				"，電視連續劇不承擔網友託管在第三方網站的內容之責任，版權均為原電視台所有，請各" +
        				"位多多準時轉至各電視台收看。" +
		        		"<br/><br/>本APP所有文章、影片、圖片之著作權皆為原創作人所擁有請勿複製使用，" +
		        		"以免侵犯第三人權益，內容若有不妥，或是部分內容侵犯了您的合法權益，請洽上述節目" +
		        		"來源網站或聯繫本站，Jumplife僅持有軟體本身著作權。"));
		        dialog.setButton(mActivity.getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		                // TODO Auto-generated method stub
		            }
		        });
		        dialog.show();
			}			
		});
		Schedule_row_first.addView(llDeclare);
		
		
		
		
		TextView tvFacebook = new TextView(mActivity);
		ImageView ivFacebook = new ImageView(mActivity);
		LinearLayout llFacebook = new LinearLayout(mActivity);		
			
		ivFacebook.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivFacebook.setImageResource(R.drawable.facebook);
        llFacebook.addView(ivFacebook, llIvParams);
		
        LinearLayout.LayoutParams llTvFacebookParams = new LinearLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //rlTvFacebookParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //rlTvFacebookParams.addRule(RelativeLayout.BELOW, ivFacebook.getId());
        //rlTvFacebookParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        llTvFacebookParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				0, 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
		tvFacebook.setText(mActivity.getResources().getString(R.string.facebook));
		tvFacebook.setTextSize(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_title));
		//tvFacebook.setTextColor(mActivity.getResources().getColor(R.color.about_us_tv));
		llFacebook.addView(tvFacebook, llTvFacebookParams);		
		
		llFacebook.setBackgroundResource(R.drawable.button_aboutus_bg);
		llFacebook.setOrientation(LinearLayout.VERTICAL);
		llFacebook.setGravity(Gravity.CENTER_HORIZONTAL);
		llFacebook.setLayoutParams(Params);
		llFacebook.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				//EasyTracker.getTracker().sendEvent("關於我們", "點擊", "FB粉絲團", (long)0);
				Uri uri = Uri.parse("http://www.facebook.com/movietalked");
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
			}			
		});
		Schedule_row_first.addView(llFacebook);
		Schedule_row_first.setLayoutParams(new LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		llAboutUs.addView(Schedule_row_first);
		
		
		
		
		TextView tvNews = new TextView(mActivity);
		ImageView ivNews= new ImageView(mActivity);
		LinearLayout llNews = new LinearLayout(mActivity);		
			
		ivNews.setScaleType(ImageView.ScaleType.CENTER_CROP);
		ivNews.setImageResource(R.drawable.news);
		llNews.addView(ivNews, llIvParams);
		
		LinearLayout.LayoutParams llTvNewsParams = new LinearLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //rlTvNewsParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //rlTvNewsParams.addRule(RelativeLayout.BELOW, ivNews.getId());
        //rlTvNewsParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        llTvNewsParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				0, 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
        tvNews.setText(mActivity.getResources().getString(R.string.entertainment_news));
        tvNews.setTextSize(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_title));
        //tvNews.setTextColor(mActivity.getResources().getColor(R.color.about_us_tv));
        llNews.addView(tvNews, llTvNewsParams);		
		
        llNews.setBackgroundResource(R.drawable.button_aboutus_bg);
        llNews.setOrientation(LinearLayout.VERTICAL);
        llNews.setGravity(Gravity.CENTER_HORIZONTAL);
        llNews.setLayoutParams(Params);
        llNews.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) {
        		EasyTracker.getTracker().trackEvent("關於我們", "影劇報", "", (long)0);
				Intent newAct = new Intent();
				newAct.setClass( AboutUsActivity.this, NewsActivity.class );
                startActivity( newAct );
			}				
		});
        Schedule_row_second.addView(llNews);
		
        
		
		TextView tvNotification = new TextView(mActivity);
		ivNotification = new ImageView(mActivity);
		LinearLayout llNotification = new LinearLayout(mActivity);		
			
		ivNotification.setScaleType(ImageView.ScaleType.CENTER_CROP);
		setNotificationDrawable();
		llNotification.addView(ivNotification, llIvParams);
		
		LinearLayout.LayoutParams llTvNotificationParams = new LinearLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //rlTvNotificationParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //rlTvNotificationParams.addRule(RelativeLayout.BELOW, ivNotification.getId());
        //rlTvNotificationParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        llTvNotificationParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				0, 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
        tvNotification.setText(mActivity.getResources().getString(R.string.notification_switch));
        tvNotification.setTextSize(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_title));
        //tvNotification.setTextColor(mActivity.getResources().getColor(R.color.about_us_tv));
        llNotification.addView(tvNotification, llTvNotificationParams);		
		
        llNotification.setBackgroundResource(R.drawable.button_aboutus_bg);
        llNotification.setOrientation(LinearLayout.VERTICAL);
        llNotification.setGravity(Gravity.CENTER_HORIZONTAL);
        llNotification.setLayoutParams(Params);
        llNotification.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) {
        		String message;
        		boolean shareKey = true;;
                shareKey = shIO.SharePreferenceO("notification_key", shareKey);
                if(shareKey)
                	message = "目前狀態 : 通知開啟";
                else
                	message = "目前狀態 : 通知關閉";
                
        		new AlertDialog.Builder(AboutUsActivity.this).setTitle("新劇通知開關")
        		.setMessage(message)
	            .setPositiveButton(getResources().getString(R.string.open), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface arg0, int arg1) {
	                	shIO.SharePreferenceI("notification_key", true);
	                	setNotificationDrawable();
	                }
	            }).setNegativeButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface arg0, int arg1) {
	                	shIO.SharePreferenceI("notification_key", false);
	                	setNotificationDrawable();
	                }
	            })
	            .show();
			}				
		});
        Schedule_row_second.addView(llNotification);

        
		
		TextView tvRepeat = new TextView(mActivity);
		ivRepeat = new ImageView(mActivity);
		LinearLayout llRepeat = new LinearLayout(mActivity);		
			
		ivRepeat.setScaleType(ImageView.ScaleType.CENTER_CROP);
		setRepeatDrawable();
		llRepeat.addView(ivRepeat, llIvParams);
		
		LinearLayout.LayoutParams llTvRepeatParams = new LinearLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //rlTvRepeatParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //rlTvRepeatParams.addRule(RelativeLayout.BELOW, ivRepeat.getId());
        //rlTvRepeatParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        llTvRepeatParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				0, 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
        tvRepeat.setText(mActivity.getResources().getString(R.string.continuous_switch));
        tvRepeat.setTextSize(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_title));
        //tvRepeat.setTextColor(mActivity.getResources().getColor(R.color.about_us_tv));
        llRepeat.addView(tvRepeat, llTvRepeatParams);		
		
        llRepeat.setBackgroundResource(R.drawable.button_aboutus_bg);
        llRepeat.setOrientation(LinearLayout.VERTICAL);
        llRepeat.setGravity(Gravity.CENTER_HORIZONTAL);
        llRepeat.setLayoutParams(Params);
        llRepeat.setOnClickListener(new OnClickListener(){
        	public void onClick(View arg0) {
        		String message;
        		boolean shareKey = true;;
                shareKey = shIO.SharePreferenceO("notification_key", shareKey);
                if(shareKey)
                	message = "目前狀態 : 連續撥放開啟";
                else
                	message = "目前狀態 : 連續撥放關閉";
                
        		new AlertDialog.Builder(AboutUsActivity.this).setTitle("連續撥放開關")
        		.setMessage(message)
	            .setPositiveButton(getResources().getString(R.string.open), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface arg0, int arg1) {
	                	shIO.SharePreferenceI("repeat_key", true);
	                	setRepeatDrawable();
	                }
	            }).setNegativeButton(getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface arg0, int arg1) {
	                	shIO.SharePreferenceI("repeat_key", false);
	                	setRepeatDrawable();
	                }
	            })
	            .show();
			}				
		});
        Schedule_row_second.addView(llRepeat);
		
		
		
        Schedule_row_second.setLayoutParams(new LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		llAboutUs.addView(Schedule_row_second);


		TextView tvClear = new TextView(mActivity);
		ImageView ivClear = new ImageView(mActivity);
		LinearLayout llClear = new LinearLayout(mActivity);		
			
		ivClear.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivClear.setImageResource(R.drawable.delete);
        llClear.addView(ivClear, llIvParams);
		
        LinearLayout.LayoutParams llTvClearParams = new LinearLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //rlTvClearParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //rlTvClearParams.addRule(RelativeLayout.BELOW, ivClear.getId());
        //rlTvClearParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        llTvClearParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				0, 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
		tvClear.setText(mActivity.getResources().getString(R.string.clear));
		tvClear.setTextSize(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_title));
		//tvClear.setTextColor(mActivity.getResources().getColor(R.color.about_us_tv));
		llClear.addView(tvClear, llTvClearParams);		
		
		llClear.setBackgroundResource(R.drawable.button_aboutus_bg);
		llClear.setOrientation(LinearLayout.VERTICAL);
		llClear.setGravity(Gravity.CENTER_HORIZONTAL);
		llClear.setLayoutParams(Params);
		llClear.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				imageLoader.clearMemoryCache();
				imageLoader.clearDiscCache();
				Toast toast = Toast.makeText(mActivity, 
	            		mActivity.getResources().getString(R.string.clear_finish), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
			}			
		});
		Schedule_row_third.addView(llClear);
        
		
		
		TextView tvDiary = new TextView(mActivity);
		ImageView ivDiary= new ImageView(mActivity);
		LinearLayout llDiary = new LinearLayout(mActivity);		
			
		ivDiary.setScaleType(ImageView.ScaleType.CENTER_CROP);
		ivDiary.setImageResource(R.drawable.version);
		llDiary.addView(ivDiary, llIvParams);
		
		LinearLayout.LayoutParams llTvDiaryParams = new LinearLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //rlTvDiaryParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //rlTvDiaryParams.addRule(RelativeLayout.BELOW, ivDiary.getId());
        //rlTvDiaryParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        llTvDiaryParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				0, 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
        tvDiary.setText(mActivity.getResources().getString(R.string.diary));
        tvDiary.setTextSize(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_title));
        //tvDiary.setTextColor(mActivity.getResources().getColor(R.color.about_us_tv));
        llDiary.addView(tvDiary, llTvDiaryParams);		
		
        llDiary.setBackgroundResource(R.drawable.button_aboutus_bg);
        llDiary.setOrientation(LinearLayout.VERTICAL);
        llDiary.setGravity(Gravity.CENTER_HORIZONTAL);
        llDiary.setLayoutParams(Params);
        llDiary.setOnClickListener(new OnClickListener(){
        	
        	public void onClick(View arg0) {
        		diaryDataTask = new DiaryDataTask();
        	    if(Build.VERSION.SDK_INT < 11)
        	    	diaryDataTask.execute();
                else
                	diaryDataTask.executeOnExecutor(DiaryDataTask.THREAD_POOL_EXECUTOR, 0);
			}				
		});
		Schedule_row_third.addView(llDiary);
		
		
		
		TextView tvTicket = new TextView(mActivity);
		ImageView ivTicket= new ImageView(mActivity);
		LinearLayout llTicket = new LinearLayout(mActivity);		
			
		ivTicket.setScaleType(ImageView.ScaleType.CENTER_CROP);
		ivTicket.setImageResource(R.drawable.ticket_3);
		llTicket.addView(ivTicket, llIvParams);
		
		LinearLayout.LayoutParams llTvTicketParams = new LinearLayout.LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        //rlTvTicketParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        //rlTvTicketParams.addRule(RelativeLayout.BELOW, ivTicket.getId());
        //rlTvTicketParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        llTvTicketParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				0, 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
				mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
        tvTicket.setText(mActivity.getResources().getString(R.string.counpons));
        tvTicket.setTextSize(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_title));
        //tvTicket.setTextColor(mActivity.getResources().getColor(R.color.about_us_tv));
        llTicket.addView(tvTicket, llTvTicketParams);		
		
        llTicket.setBackgroundResource(R.drawable.button_aboutus_bg);
        llTicket.setOrientation(LinearLayout.VERTICAL);
        llTicket.setGravity(Gravity.CENTER_HORIZONTAL);
        llTicket.setLayoutParams(Params);
        llTicket.setOnClickListener(new OnClickListener(){        	
        	public void onClick(View arg0) {
        		EasyTracker.getTracker().trackEvent("關於我們", "票劵中心", "", (long)0);
				Intent newAct = new Intent();
				newAct.putExtra("advertisement_type", "優惠活動-關於我們");
				newAct.setClass( AboutUsActivity.this, TicketCenterActivity.class );
                startActivity( newAct );
			}				
		});
		Schedule_row_third.addView(llTicket);
		
		/*RelativeLayout rltmps2 = new RelativeLayout(mActivity);
		rltmps2.setBackgroundResource(R.drawable.button_aboutus_bg);	
		rltmps2.setLayoutParams(Params);
		Schedule_row_third.addView(rltmps2);*/
		
		
		Schedule_row_third.setLayoutParams(new LayoutParams
				(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		llAboutUs.addView(Schedule_row_third);
	}
	
	private void setNotificationDrawable() {
		SharePreferenceIO shIO = new SharePreferenceIO(this);
        boolean shareKey = true;;
        shareKey = shIO.SharePreferenceO("notification_key", shareKey);
        if(shareKey)
        	ivNotification.setImageResource(R.drawable.notification);
        else
        	ivNotification.setImageResource(R.drawable.no_notification);
        
	}
	
	private void setRepeatDrawable() {
		SharePreferenceIO shIO = new SharePreferenceIO(this);
        boolean shareKey = true;;
        shareKey = shIO.SharePreferenceO("repeat_key", shareKey);
        if(shareKey)
        	ivRepeat.setImageResource(R.drawable.repeat_on);
        else
        	ivRepeat.setImageResource(R.drawable.repeat_off);
        
	}
	
	private String fetchData() {
		DramaAPI api = new DramaAPI();
		appProject = api.getAppProjectList(mActivity);
		return "progress end";
	}
	
	private void setView(){
		
		for(int i=0; i<appProject.size(); i+=3){
			TableRow Schedule_row = new TableRow(mActivity);
			for(int j=0; j<3; j++){
				int index = i + j;
				
				TextView tv = new TextView(mActivity);
				ImageView iv = new ImageView(mActivity);
				LinearLayout ll = new LinearLayout(mActivity);
				
				if(index < appProject.size()) {
					
					DisplayMetrics displayMetrics = new DisplayMetrics();
					mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
			        int width = displayMetrics.widthPixels / 6;
			        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
			        LinearLayout.LayoutParams llIvParams = new LinearLayout.LayoutParams(width, width);
			        //rlIvParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
			        //rlIvParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			        llIvParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin),
			        		mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
							mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
							mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
					ll.addView(iv, llIvParams);
			        iv.getLayoutParams().width = width;
			        iv.getLayoutParams().height = width;
			        imageLoader.displayImage(appProject.get(index).getIconUrl(), iv, options);
					
			        LinearLayout.LayoutParams llTvParams = new LinearLayout.LayoutParams
							(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					//rlTvParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
					//rlTvParams.addRule(RelativeLayout.BELOW, iv.getId());
					//rlTvParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
					llTvParams.setMargins(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
							0, 
							mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin), 
							mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_margin));
					tv.setText(appProject.get(index).getName());
					tv.setTextSize(mActivity.getResources().getDimensionPixelSize(R.dimen.about_us_title));
					ll.addView(tv, llTvParams);
				} else
					ll.setVisibility(View.INVISIBLE);
				
				ll.setBackgroundResource(R.drawable.button_aboutus_bg);
				ll.setOrientation(LinearLayout.VERTICAL);
				ll.setGravity(Gravity.CENTER_HORIZONTAL);
				
				DisplayMetrics displayMetrics = new DisplayMetrics();
				mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		        int screenWidth = displayMetrics.widthPixels / 3;
				TableRow.LayoutParams Params = new TableRow.LayoutParams
						(screenWidth, LayoutParams.MATCH_PARENT, 0.33f);				
				ll.setLayoutParams(Params);
				ll.setOnClickListener(new ItemButtonClick(index, mActivity));
				Schedule_row.addView(ll);
			}
			Schedule_row.setLayoutParams(new LayoutParams
					(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			llAboutUs.addView(Schedule_row);
		}        
	}
	
	class ItemButtonClick implements OnClickListener {
		private int position;
		private Activity mActivity;
		
		ItemButtonClick(int pos, Activity mActivity) {
			position = pos;
			this.mActivity = mActivity;
		}

		public void onClick(View v) {
			PackageManager pm = mActivity.getPackageManager();
		    Intent appStartIntent = pm.getLaunchIntentForPackage(appProject.get(position).getPack());
		    if(null != appStartIntent) {
		    	appStartIntent.addCategory("android.intent.category.LAUNCHER");
		    	appStartIntent.setComponent(new ComponentName(appProject.get(position).getPack(),
		    			appProject.get(position).getClas()));
		    	appStartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    	mActivity.startActivity(appStartIntent);
		    } 
		    else {
		    	startActivity(new Intent(Intent.ACTION_VIEW, 
			    		Uri.parse("market://details?id=" + appProject.get(position).getPack())));
		    }
		}
	}
	
	class LoadDataTask extends AsyncTask<Integer, Integer, String>{  
        
    	@Override  
        protected void onPreExecute() {
    		pbInit.setVisibility(View.VISIBLE);
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
        	pbInit.setVisibility(View.GONE);
        	if(appProject != null){
        		setView();		
    		}

	        super.onPostExecute(result);  
        }
    }
	
	class DiaryDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
              public void onCancel(DialogInterface arg0) {
            	  DiaryDataTask.this.cancel(true);
              }
          };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(AboutUsActivity.this);
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
        	if(updateDiary == null) {
        		DramaAPI dramaAPI = new DramaAPI();
        		updateDiary = dramaAPI.getDramasHistory();
        	} else {
        		if(updateDiary.equals("")) {
        			DramaAPI dramaAPI = new DramaAPI();
        			updateDiary = dramaAPI.getDramasHistory();
        		}
        	}
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
        	if (updateDiary != null && !updateDiary.equals("")) {
        		new AlertDialog.Builder(AboutUsActivity.this).setTitle(getResources().getString(R.string.diary))
        		.setMessage(Html.fromHtml(updateDiary))
	            .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface arg0, int arg1) {	                	
	                }
	            })
	            .show();
        		shIO.SharePreferenceI("UpdateDiary", updateDiary);
            } else {
            	new AlertDialog.Builder(AboutUsActivity.this).setTitle(getResources().getString(R.string.diary))
        		.setMessage(Html.fromHtml(shIO.SharePreferenceO("UpdateDiary", "尚未有更新")))
	            .setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
	                public void onClick(DialogInterface arg0, int arg1) {	                	
	                }
	            })
	            .show();
            }
        	closeProgressDilog();

            super.onPostExecute(result);
        }
        
        public void closeProgressDilog() {
        	if(AboutUsActivity.this != null && !AboutUsActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

        	PromoteAPP promoteAPP = new PromoteAPP(AboutUsActivity.this);
        	if(!promoteAPP.isPromote) {
	        	new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.leave_app))
	            .setPositiveButton(getResources().getString(R.string.leave), new DialogInterface.OnClickListener() {
	                // do something when the button is clicked
	                public void onClick(DialogInterface arg0, int arg1) {
	                	AboutUsActivity.this.finish();
	                }
	            }).setNegativeButton(getResources().getString(R.string.cancel), null)
	            .show();
		    } else
		    	promoteAPP.promoteAPPExe();

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
}
