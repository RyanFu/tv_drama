package com.jumplife.tvdrama;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.tvdrama.promote.PromoteAPP;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

public class AboutUsActivity extends Activity {
	
	private LinearLayout llFeedback;
	private LinearLayout llFacebook;
	private LinearLayout llDeclare;
	private LinearLayout llNews;
	private LinearLayout llMoviediary;
	private LinearLayout llMovietime;
	private LinearLayout llTvVariety;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_aboutme);
		initView();
		setClickListener();
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

	private void initView(){
		llFeedback = (LinearLayout)findViewById(R.id.ll_feedback);
		llFacebook = (LinearLayout)findViewById(R.id.ll_facebook);
		llDeclare = (LinearLayout)findViewById(R.id.ll_declare);
		llNews = (LinearLayout)findViewById(R.id.ll_news);
		llMoviediary = (LinearLayout)findViewById(R.id.ll_moviediary);
		llMovietime = (LinearLayout)findViewById(R.id.ll_movietime);
		llTvVariety = (LinearLayout)findViewById(R.id.ll_tvvariety);
	}
	
	private void setClickListener(){
		llFeedback.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "建議回饋", "", (long)0);
				Uri uri = Uri.parse("mailto:jumplives@gmail.com");  
				String[] ccs={"abooyaya@gmail.com, raywu07@gmail.com, supermfb@gmail.com, form.follow.fish@gmail.com"};
				Intent it = new Intent(Intent.ACTION_SENDTO, uri);
				it.putExtra(Intent.EXTRA_CC, ccs); 
				it.putExtra(Intent.EXTRA_SUBJECT, "[電視連續劇] 建議回饋"); 
				startActivity(it);  
			}			
		});
		llFacebook.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "FB粉絲團", "", (long)0);
				Uri uri = Uri.parse("http://www.facebook.com/movietalked");
                Intent it = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(it);
			}			
		});
		llDeclare.setOnClickListener(new OnClickListener(){
			@SuppressWarnings("deprecation")
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "免責聲明", "", (long)0);
				// TODO Auto-generated method stub
				AlertDialog dialog = new AlertDialog.Builder(AboutUsActivity.this).create();
		        dialog.setTitle(AboutUsActivity.this.getResources().getString(R.string.liability_disclaimer));
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
		        dialog.setButton(AboutUsActivity.this.getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int which) {
		                // TODO Auto-generated method stub
		            }
		        });
		        dialog.show();
			}			
		});
		llNews.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "影劇報", "", (long)0);
				Intent newAct = new Intent();
				newAct.setClass( AboutUsActivity.this, NewsActivity.class );
                startActivity( newAct );
			}			
		});
		llMoviediary.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "電影櫃", "", (long)0);
				PackageManager pm = AboutUsActivity.this.getPackageManager();
			    Intent appStartIntent = pm.getLaunchIntentForPackage("com.jumplife.moviediary");
			    if(null != appStartIntent) {
			    	appStartIntent.addCategory("android.intent.category.LAUNCHER");
			    	appStartIntent.setComponent(new ComponentName("com.jumplife.moviediary",
				    		"com.jumplife.moviediary.MovieTabActivities"));
			    	appStartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    	AboutUsActivity.this.startActivity(appStartIntent);
			    } else
			    	startActivity(new Intent(Intent.ACTION_VIEW, 
				    		Uri.parse("market://details?id=com.jumplife.moviediary")));
			}			
		});
		llMovietime.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "電影時刻表", "", (long)0);
				PackageManager pm = AboutUsActivity.this.getPackageManager();
			    Intent appStartIntent = pm.getLaunchIntentForPackage("com.jumplife.movieinfo");
			    if(null != appStartIntent) {
			    	appStartIntent.addCategory("android.intent.category.LAUNCHER");
			    	appStartIntent.setComponent(new ComponentName("com.jumplife.movieinfo",
				    		"com.jumplife.movieinfo.MovieTime"));
			    	appStartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    	AboutUsActivity.this.startActivity(appStartIntent);
			    } else
			    	startActivity(new Intent(Intent.ACTION_VIEW, 
				    		Uri.parse("market://details?id=com.jumplife.movieinfo")));
			}			
		});
		llTvVariety.setOnClickListener(new OnClickListener(){
			public void onClick(View arg0) {
				EasyTracker.getTracker().trackEvent("關於我們", "電視綜藝", "", (long)0);
				PackageManager pm = AboutUsActivity.this.getPackageManager();
			    Intent appStartIntent = pm.getLaunchIntentForPackage("com.jumplife.tvvariety");
			    if(null != appStartIntent) {
			    	appStartIntent.addCategory("android.intent.category.LAUNCHER");
			    	appStartIntent.setComponent(new ComponentName("com.jumplife.tvvariety",
				    		"com.jumplife.tvvariety.TvVariety"));
			    	appStartIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			    	AboutUsActivity.this.startActivity(appStartIntent);
			    } else
			    	startActivity(new Intent(Intent.ACTION_VIEW, 
				    		Uri.parse("market://details?id=com.jumplife.tvvariety")));
			}			
		});
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
