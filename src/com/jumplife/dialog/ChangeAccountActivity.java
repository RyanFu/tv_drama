package com.jumplife.dialog;

import java.util.ArrayList;
import java.util.regex.Pattern;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.TicketCenterActivity;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ChangeAccountActivity extends Activity {
	
	private TextView tvEmail;
	private Spinner spinner;
	private Button buttonSend;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_change_account);		
		initViews();
	}
	
	private void initViews(){
		ArrayList<String> emailAcoounts = getEmailAccount();
		SharePreferenceIO shIO = new SharePreferenceIO(ChangeAccountActivity.this);			
		String email = shIO.SharePreferenceO("ticket_email", "");
		Log.d(null, email);
		int spinnerPosition = emailAcoounts.indexOf(email);
		if(spinnerPosition < 0)
			spinnerPosition = 0;
		Log.d(null, spinnerPosition + "");
		tvEmail = (TextView)findViewById(R.id.tv_change_account_email);
		if(email.equalsIgnoreCase(""))
			tvEmail.setText("你目前沒有使用任何帳號");
		else
			tvEmail.setText("你目前使用的帳號是 : " + email);
		
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
			SharePreferenceIO shIO = new SharePreferenceIO(ChangeAccountActivity.this);			
			shIO.SharePreferenceI("ticket_email", spinner.getSelectedItem().toString());
			
			Toast.makeText(getApplicationContext(), "更換帳戶成功", Toast.LENGTH_LONG).show();
        	Intent intent = new Intent();
            setResult(TicketCenterActivity.CHANGEACCOUNT_SUCCESS, intent);  
        	ChangeAccountActivity.this.finish();
		}
		
	}

    @Override
    public void onDestroy() {
      super.onDestroy();
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
