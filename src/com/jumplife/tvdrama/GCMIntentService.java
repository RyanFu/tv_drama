/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jumplife.tvdrama;

import static com.jumplife.tvdrama.CommonUtilities.SENDER_ID;
import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        ServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        
        /*
         * There is two type of Push.
         * One update information center.
         * Another notifies new dramas. 
         */
        if(intent.hasExtra("info_notify")) {
        	Intent infoIntent = new Intent("info_center");
        	
        	/*
        	 * Using AsyncTask to request new information 
        	 */
            LocalBroadcastManager.getInstance(context).sendBroadcast(infoIntent);
        } else {
	        int typeId;        
	        int sortId;
	        
	        if(intent.hasExtra("type_id"))
	        	typeId = Integer.parseInt(intent.getStringExtra("type_id"));
	        else
	        	typeId = 0;
	        
	        if(intent.hasExtra("sort_id"))
	        	sortId = Integer.parseInt(intent.getStringExtra("sort_id"));
	        else
	        	sortId = 0;
	        
	        Log.d(TAG, "type_id : " + typeId + " Extra : " + intent.getStringExtra("type_id"));
	        Log.d(TAG, "sort_id : " + sortId + " Extra : " + intent.getStringExtra("sort_id"));
	        
	        String message = intent.getStringExtra("message");
	        
	        boolean shareKey = true;;
	        shareKey = TvDramaApplication.shIO.getBoolean("notification_key", shareKey);
	        if(shareKey)
	        	generateNotification(context, typeId, sortId, message);
        }
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    @SuppressWarnings("deprecation")
	private static void generateNotification(Context context, int typeId, int sortId, String message) {
        int icon = R.drawable.tvdrama;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, TvDrama.class);
        TvDramaApplication.shIO.edit().putInt("type_id", typeId).commit();
        TvDramaApplication.shIO.edit().putInt("sort_id", sortId).commit();
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
    }

}
