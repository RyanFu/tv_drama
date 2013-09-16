package com.jumplife.tvdrama;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import com.google.analytics.tracking.android.AnalyticsReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/*public class GPCampaignRecevier extends BroadcastReceiver {

	static final String INSTALL_ACTION = "com.android.vending.INSTALL_REFERRER";
	static final String CAMPAIGN_KEY = "referrer";
	
	@Override
	public void onReceive(Context context, Intent intent) {

        String referrerString = intent.getStringExtra("referrer");
        Log.d(null, "referrerString : " + referrerString);
        try {    // Remove any url encoding
        	referrerString = URLDecoder.decode(referrerString, "UTF-8");
        }
        catch (UnsupportedEncodingException e) { 
        	return; 
        }
        
        if(referrerString != null) {
        	SharePreferenceIO sharePreferenceIO = new SharePreferenceIO(context);
	    	sharePreferenceIO.SharePreferenceI("referrerString", referrerString);
	    	
	        Map<String, String> getParams = getQueryMap(referrerString);
	        String source = getParams.get("utm_source");
	
	        if (source != null) {
	            sharePreferenceIO.SharePreferenceI("utm_source", source);
	        }
	
	        // Google Analytics
	        new CampaignTrackingReceiver().onReceive(context, intent);
	        // Pass along to google
	        //new CampaignTrackingReceiver().onReceive(context, intent);
        }
	}

	private static Map<String, String> getQueryMap(String query) {  
	    String[] params = query.split("&");  
	    Map<String, String> map = new HashMap<String, String>();  
	    for (String param : params)  
	    {  
	        String name = param.split("=")[0];  
	        String value = param.split("=")[1];  
	        map.put(name, value);  
	    }  
	    return map;  
	}  
}*/

public final class GPCampaignRecevier extends BroadcastReceiver {
	
	static final String INSTALL_ACTION = "com.android.vending.INSTALL_REFERRER";
	static final String CAMPAIGN_KEY = "referrer";
	
	public void onReceive(Context ctx, Intent intent) {
		
		String campaign = intent.getStringExtra(CAMPAIGN_KEY);
		
		if ((!INSTALL_ACTION.equals(intent.getAction())) || (campaign == null)) {
			return;
		}
		
		String referrerString = campaign;
        Log.d(null, "referrerString : " + referrerString);
        try {    // Remove any url encoding
        	referrerString = URLDecoder.decode(referrerString, "UTF-8");
        }
        catch (UnsupportedEncodingException e) { 
        	return; 
        }        
       
        TvDramaApplication.shIO.edit().putString("referrerString", referrerString).commit();
    	
        Map<String, String> getParams = getQueryMap(referrerString);
        String source = getParams.get("utm_source");
        if (source != null) {
        	TvDramaApplication.shIO.edit().putString("utm_source", source).commit();
        }
         
        //--------Method 0----------
        new AnalyticsReceiver().onReceive(ctx, intent);
        
        //--------Method 1----------
        // Re-write CampaignTrackingReceiver
		/*Intent serviceIntent = new Intent(ctx, CampaignTrackingService.class);
		serviceIntent.putExtra(CAMPAIGN_KEY, Campaign);
		ctx.startService(serviceIntent);*/
		
        //--------Method 2----------
        // Simulated broadcast send Campaign to CampaignTrackingReceiver
        /*Intent recevierIntent = new Intent(ctx, CampaignTrackingReceiver.class);
        recevierIntent.putExtra(CAMPAIGN_KEY, Campaign);
        recevierIntent.setAction(INSTALL_ACTION);
        ctx.sendBroadcast(recevierIntent);*/
		
        //--------Method 3----------
        // Pass to other recevier from meta data
		/*ActivityInfo ai = null;
		try {
			ai = ctx.getPackageManager().getReceiverInfo(new ComponentName(ctx, "com.jumplife.movienews.GPCampaignRecevier"), 
					PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		//extract meda-data
		if(ai != null) { 
			Bundle bundle = ai.metaData;
			Set<String> keys = bundle.keySet();
			Iterator<String> it = keys.iterator();
			while(it.hasNext()){
				String k = it.next();
				String v = bundle.getString(k);
				try {
					((BroadcastReceiver)Class.forName(v).newInstance()).onReceive(ctx, intent);
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				//send intent by dynamically creating instance of receiver
			 
				Log.i("PASS REFERRER TO...", v);
			}
		}*/
	}

	private static Map<String, String> getQueryMap(String query) {  
	    String[] params = query.split("&");  
	    Map<String, String> map = new HashMap<String, String>();  
	    for (String param : params)  
	    {  
	        String name = param.split("=")[0];  
	        String value = param.split("=")[1];  
	        map.put(name, value);  
	    }  
	    return map;  
	} 
}
