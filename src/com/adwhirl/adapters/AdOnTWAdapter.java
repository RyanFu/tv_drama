package com.adwhirl.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.adwhirl.AdWhirlLayout;
import com.adwhirl.AdWhirlLayout.ViewAdRunnable;
import com.adwhirl.adapters.AdWhirlAdapter;
import com.adwhirl.obj.Ration;
import com.adwhirl.util.AdWhirlUtil;
import com.vpon.adon.android.AdListener;
import com.vpon.adon.android.AdOnPlatform;
import com.vpon.adon.android.AdView;

public class AdOnTWAdapter extends AdWhirlAdapter implements AdListener {

	public AdOnTWAdapter(AdWhirlLayout adWhirlLayout, Ration ration) {
		super(adWhirlLayout, ration);
	}

	@Override
	public void handle() {
		Log.i("AdOnTWAdapter", "handler");
		AdWhirlLayout adWhirlLayout = adWhirlLayoutReference.get();
		if (adWhirlLayout == null) {
			Log.i("AdOnTWAdapter", "adWhirlLayout null");
			return;
		}

		Activity activity = adWhirlLayout.activityReference.get();
		if (activity == null) {
			Log.i("AdOnTWAdapter", "activity null");
			return;
		}
		
		

		try {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager windowManager = ((WindowManager)activity.getSystemService(Context.WINDOW_SERVICE));
			windowManager.getDefaultDisplay().getMetrics(dm);
			
			Log.d("AdOn", "width: " + dm.widthPixels);
			
			AdView adView;
			
			if (dm.widthPixels <= 320) {
				adView = new AdView(activity, 320, 48);
			}
			else if(dm.widthPixels < 720) {
				adView = new AdView(activity, 480, 72);
			}
			else {
				adView = new AdView(activity, 720, 108);
			}
			//AdView adView = new AdView(activity, Width, Height);
//			AdView adView = new AdView(activity);
			//如果對於填寫adWidth和adHeight有任何疑問，請到wiki.vpon.com查詢。
	        //If there is any question about adWidth and adHeight, please check wiki.vpon.com.
			boolean autoRefreshAd = false;
			adView.setLicenseKey(ration.key, AdOnPlatform.TW, autoRefreshAd);
			adView.setAdListener(this);
			
//			 adWhirlLayout.addView(adView, new LayoutParams(
//					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			 
			//adWhirlLayout.addView(adView);
			
		} catch (IllegalArgumentException e) {
			Log.i("AdOnTWAdapter", "IllegalArgumentException");
			adWhirlLayout.rollover();
			return;
		}
	}
	
	/*
	public void onRecevieAd(AdView adView) {
		Log.d(AdWhirlUtil.ADWHIRL, "AdOnTWAdapter success");

		AdWhirlLayout adWhirlLayout = adWhirlLayoutReference.get();
		if (adWhirlLayout == null) {
			return;
		}

		adWhirlLayout.adWhirlManager.resetRollover();
		adWhirlLayout.handler.post(new ViewAdRunnable(adWhirlLayout, adView));
		adWhirlLayout.rotateThreadedDelayed();
	}

	public void onFailedToRecevieAd(AdView adView) {
		Log.d(AdWhirlUtil.ADWHIRL, "AdOnTWAdapter failure");

		adView.setAdListener(null);

		AdWhirlLayout adWhirlLayout = adWhirlLayoutReference.get();
		if (adWhirlLayout == null) {
			return;
		}

		adWhirlLayout.rollover();
	}
	*/
	public void onRecevieAd(AdView adView) {
		Log.d(AdWhirlUtil.ADWHIRL, "AdOnTWAdapter success");

		AdWhirlLayout adWhirlLayout = adWhirlLayoutReference.get();
		if (adWhirlLayout == null) {
			return;
		}
		
		adWhirlLayout.adWhirlManager.resetRollover();
		adWhirlLayout.handler.post(new ViewAdRunnable(adWhirlLayout, adView));
		adWhirlLayout.rotateThreadedDelayed();		
	}

	public void onFailedToRecevieAd(AdView adView) {
		Log.d(AdWhirlUtil.ADWHIRL, "AdOnTWAdapter failure");

		adView.setAdListener(null);

		AdWhirlLayout adWhirlLayout = adWhirlLayoutReference.get();
		if (adWhirlLayout == null) {
			return;
		}

		adWhirlLayout.rollover();
	}
	

}
