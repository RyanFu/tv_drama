package com.jumplife.dialog;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.android.gcm.GCMRegistrar;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.TicketCenterActivity;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.Ticket;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class GetSerialActivity extends Activity {
	
	private Spinner spinner;
	private Button buttonSend;
	
	private LoadDataTask loadTask;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_get_serial);		
		initViews();
	}
	
	private void initViews(){
		ArrayList<String> emailAcoounts = getEmailAccount();
		SharePreferenceIO shIO = new SharePreferenceIO(GetSerialActivity.this);			
		String email = shIO.SharePreferenceO("ticket_email", "");
		int spinnerPosition = emailAcoounts.indexOf(email);
		
		spinner = (Spinner)findViewById(R.id.spinner_email);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, emailAcoounts);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter);
		spinner.setSelection(spinnerPosition);
		
		buttonSend = (Button)findViewById(R.id.button_send);
		buttonSend.setOnClickListener(new itemClick(spinner));
	}

	private ArrayList<String> getEmailAccount() {
		ArrayList<String> emailAccounts = new ArrayList<String>();
		Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
		Account[] accounts = AccountManager.get(this).getAccounts();
		for (Account account : accounts) {
		    if (emailPattern.matcher(account.name).matches()) {
		        String possibleEmail = account.name;
		        if(!emailAccounts.contains(possibleEmail))
		        	emailAccounts.add(possibleEmail);
		    }
		}
		return emailAccounts;
	}
	
	class itemClick implements OnClickListener {

		Spinner spinner;
		
		itemClick(Spinner spinner) {
			this.spinner = spinner;
		}
		
		@Override
		public void onClick(View v) {
			loadTask = new LoadDataTask();
	        if(Build.VERSION.SDK_INT < 11)
	        	loadTask.execute();
	        else
	        	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
		}
		
	}
	
	class LoadDataTask extends AsyncTask<Integer, Integer, Ticket> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
              public void onCancel(DialogInterface arg0) {
                  LoadDataTask.this.cancel(true);
              }
          };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(GetSerialActivity.this);
            progressdialogInit.setTitle("Load");
            progressdialogInit.setMessage("Loading…");
            progressdialogInit.setOnCancelListener(cancelListener);
            progressdialogInit.setCanceledOnTouchOutside(false);
            progressdialogInit.show();
            super.onPreExecute();
        }

        @Override
        protected Ticket doInBackground(Integer... params) {
        	Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        	DramaAPI dramaAPI = new DramaAPI(GetSerialActivity.this);
        	
        	String regId = GCMRegistrar.getRegistrationId(getApplicationContext());
        	if(regId == null) {
            	Log.d(null, "regId　= null");
        		TelephonyManager tm = (TelephonyManager) GetSerialActivity.this.getSystemService(Context.TELEPHONY_SERVICE);
        		if(tm.getDeviceId() != null && !tm.getDeviceId().equals(""))
        			regId = tm.getDeviceId();
        		else 
        			regId = tm.getLine1Number();
        	}
        	Log.d(null, "regId　= " + regId);
        	
        	Bundle bundle = getIntent().getExtras();
        	
            return dramaAPI.requestTicket("", spinner.getSelectedItem().toString(), regId, bundle.getInt("campaign_id"));
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(Ticket ticket) {
        	if(ticket == null) {
        		closeProgressDilog();
        		Toast.makeText(getApplicationContext(), "網路不穩，請再嘗試一次", Toast.LENGTH_LONG).show();
            	EasyTracker.getTracker().trackEvent(getIntent().getExtras().getString("advertisement_type"), 
            			"優惠ID:" + getIntent().getExtras().getInt("campaign_id"),
            			"優惠申請失敗", (long)0);
            } else if(ticket != null && ticket.getSerialNum() < 0) {
            	closeProgressDilog();
            	Toast.makeText(getApplicationContext(), "此帳號已經申請過本活動，請使用其他帳號申請", Toast.LENGTH_LONG).show();
            	EasyTracker.getTracker().trackEvent(getIntent().getExtras().getString("advertisement_type"), 
            			"優惠ID:" + getIntent().getExtras().getInt("campaign_id"), 
            			"優惠重覆申請", (long)0);
            } else {
            	SharePreferenceIO shIO = new SharePreferenceIO(GetSerialActivity.this);			
        		String email = shIO.SharePreferenceO("ticket_email", "");            	
            	shIO.SharePreferenceI("ticket_email", spinner.getSelectedItem().toString());
                
            	Toast.makeText(getApplicationContext(), "送交成功，請至我的票劵檢視", Toast.LENGTH_LONG).show();
            	EasyTracker.getTracker().trackEvent(getIntent().getExtras().getString("advertisement_type"), 
            			"優惠ID:" + getIntent().getExtras().getInt("campaign_id"),
            			"優惠申請成功", (long)0);
				
        		if(email.equals(spinner.getSelectedItem().toString())) {            	
	            	int count = shIO.SharePreferenceO("unread_ticket_count", 0);
	            	count += 1;            	
	            	shIO.SharePreferenceI("unread_ticket_count", count);
	            	
	            	Intent intent = new Intent();
	            	Bundle bundle = new Bundle();
	            	bundle.putSerializable("new_ticket", ticket);
	            	intent.putExtras(bundle);
	                setResult(TicketCenterActivity.GETTICKET_CURRENT_ACCOUNT, intent);  
	                closeProgressDilog();
	            	GetSerialActivity.this.finish();
        		}else {
        			Intent intent = new Intent();
	                setResult(TicketCenterActivity.GETTICKET_OTHER_ACCOUNT, intent);  
	                closeProgressDilog();
	            	GetSerialActivity.this.finish();
        		}
            }

            super.onPostExecute(ticket);
        }
        
        public void closeProgressDilog() {
        	if(GetSerialActivity.this != null && !GetSerialActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      if (loadTask!= null && loadTask.getStatus() != AsyncTask.Status.FINISHED) {
    	  loadTask.closeProgressDilog();
    	  loadTask.cancel(true);
      }
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
}
