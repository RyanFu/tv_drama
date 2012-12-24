package com.jumplife.ad;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.ads.AdSize;
import com.google.ads.mediation.MediationAdRequest;
import com.google.ads.mediation.customevent.CustomEventBanner;
import com.google.ads.mediation.customevent.CustomEventBannerListener;

import com.vpon.adon.android.AdListener;
import com.vpon.adon.android.AdOnPlatform;
import com.vpon.adon.android.AdView;

public class VponBanner implements CustomEventBanner {
	private AdView adView;
	public void requestBannerAd(final CustomEventBannerListener listener,
	                              final Activity activity,
	                              String label,
	                              String serverParameter,
	                              AdSize adSize,
	                              MediationAdRequest request,
	                              Object customEventExtra) {
		
		Log.i("AdOn", "requestBannerAd");
		
		DisplayMetrics dm = new DisplayMetrics();
		WindowManager windowManager = ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE));
		windowManager.getDefaultDisplay().getMetrics(dm);
		
		Log.d("AdOn", "width: " + dm.widthPixels);
		
		if (dm.widthPixels <= 320) {
			adView = new AdView(activity, 320, 48);
		}
		else if(dm.widthPixels <= 480) {
			adView = new AdView(activity, 480, 72);
		}
		else {
			adView = new AdView(activity, 720, 108);
		}
		
		//windowManager.getDefaultDisplay().;
		  
		//adView = new AdView(this, 320, 48);
		
		
		//String vponKey = 
		
		boolean autoRefreshAd = false;
		
		adView.setLicenseKey(serverParameter, AdOnPlatform.TW , autoRefreshAd);
		//adView.setLicenseKey("8a8081823b63f8ab013b69e650df0380", AdOnPlatform.TW , autoRefreshAd);
		
		
		AdListener adListener = new AdListener() { //4
			public void onRecevieAd(AdView adView) {
				//廣告抓取成功
				Log.i("AdOn", "OnRecevieAd");
				/*
				 * 廣告抓取成功時,我們建議您可以在這函式中替AdView增加一些簡單的動畫, 動畫範例程式碼如下 
				 */
				//rotationHoriztion(0, 360, adView);
				
				listener.onReceivedAd(adView);
			}
			
			public void onFailedToRecevieAd(AdView adView) {
				//廣告抓取失敗
				Log.i("AdOn", "OnFailesToRecevieAd");
				listener.onFailedToReceiveAd();
			}
		};
		
		adView.setAdListener(adListener);
		
	 }
		  
		/*  
	ImageView imageView = new ImageView(activity);
	imageView.setImageResource(R.drawable.floodit_ad);
	imageView.setOnClickListener(new OnClickListener() {
	  @Override
	  public void onClick(View v) {
	    try {
	      listener.onClick();
	      listener.onPresentScreen();
	      listener.onLeaveApplication();
	      Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.labpixies.flood"));
	      activity.startActivity(intent);
	    } catch (Throwable t) {
	      // Something went wrong, oh well.
	    }
	  }
	});
	listener.onReceivedAd(imageView);*/
	/* This custom event will always succeed, so we haven't called the onFailedToReceiveAd method */
	  //}
	
	
	
	public void destroy()
	{
		// TODO Auto-generated method stub
		
	}
}
