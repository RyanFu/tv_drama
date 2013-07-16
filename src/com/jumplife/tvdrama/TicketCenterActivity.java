package com.jumplife.tvdrama;

import java.util.ArrayList;
import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.adapter.TicketCenterViewPagerAdapter;
import com.jumplife.dialog.ChangeAccountActivity;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.Ticket;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TicketCenterActivity extends Activity {
	
	private int currIndex = 0;
	private int cursorWidth;
	private int cursorOffset = 0;
	private int listnumber = 2;
	private int unReadCount = 0;
	
	private ViewPager 		viewpager;
	private View 			cursor;	
	private ImageButton		imageButtonRefresh;	
	private ImageButton		imageButtonChangeAccount;
    private Button			buttonPreferential;
	private TextView		tvUnRead;
	private TextView		tvUnReadCount;
    private RelativeLayout	rlMyTicketUnRead;
	private TextView		topbar_text;
	private ArrayList<Ticket> mPreferentialtList;
	private ArrayList<Ticket> mMyTicketList;
	private TicketCenterViewPagerAdapter viewpageradapter;
	private SharePreferenceIO shIO;
	
	private LoadDataTask loadTask;
	private RefreshTask refreshTask;
	
	public final static int GETTICKET = 100;
	public final static int GETTICKET_CURRENT_ACCOUNT = 101;
	public final static int GETTICKET_OTHER_ACCOUNT = 102;
	
	public final static int CHANGEACCOUNT = 200;
	public final static int CHANGEACCOUNT_SUCCESS = 201;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_ticket_center);
		
		initViews();

        loadTask = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	loadTask.execute();
        else
        	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        switch (requestCode) {
        case GETTICKET:
        	if (resultCode == GETTICKET_CURRENT_ACCOUNT) {
        		/*Log.d(null, "success get serial num");
        		SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(TicketCenterActivity.this);
            	SQLiteDatabase db = instance.getReadableDatabase();
            	db.beginTransaction();
            	
            	mMyTicketList = instance.getTicketList(db);
            	if(mMyTicketList != null && mMyTicketList.size() > 0)
            		unReadCount = instance.getUnReadCount(db);
            	Log.d(null, "unreadcount : " + unReadCount);

                db.setTransactionSuccessful();
                db.endTransaction();
            	db.close();
                instance.closeHelper();*/    	

           		unReadCount = shIO.SharePreferenceO("unread_ticket_count", 0);
           		
            	Ticket ticket = (Ticket) data.getSerializableExtra("new_ticket");
            	mMyTicketList.add(ticket);

                setViews();
            } else if(resultCode == GETTICKET_OTHER_ACCOUNT) {
            	
            	unReadCount = 1;
            	shIO.SharePreferenceI("unread_ticket_count", unReadCount);
        		
           		if(!loadTask.isCancelled())
           			loadTask.cancel(true);
           		loadTask = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	loadTask.execute();
                else
                	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);

                setViews();
            }
            break;
        case CHANGEACCOUNT:
        	if (resultCode == CHANGEACCOUNT_SUCCESS) {
        		/*SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(TicketCenterActivity.this);
            	SQLiteDatabase db = instance.getReadableDatabase();
            	db.beginTransaction();
            	
            	mMyTicketList = instance.getTicketList(db);

                db.setTransactionSuccessful();
                db.endTransaction();
            	db.close();
                instance.closeHelper();*/
        		
        		shIO.SharePreferenceI("unread_ticket_count", 0);
        		
           		if(!loadTask.isCancelled())
           			loadTask.cancel(true);
           		loadTask = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	loadTask.execute();
                else
                	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);

                setViews();
            }
            break;
        }
    }
	
	private void initViews() {
		shIO = new SharePreferenceIO(this);
		
		topbar_text = (TextView)findViewById(R.id.topbar_text);
        topbar_text.setText(getResources().getString(R.string.counpons));
    	
        DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		cursorWidth = dm.widthPixels / listnumber - cursorOffset * 2;

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cursorWidth, 10);
		lp.setMargins(cursorOffset, 0, cursorOffset, 0);
		cursor = (View) findViewById(R.id.view_1);
		cursor.setLayoutParams(lp);
		
		buttonPreferential = (Button)findViewById(R.id.button_preferential);
		rlMyTicketUnRead = (RelativeLayout)findViewById(R.id.rl_my_ticket_unread);
		tvUnRead = (TextView)findViewById(R.id.tv_my_ticket_unread);
		tvUnReadCount = (TextView)findViewById(R.id.tv_my_ticket_unread_count);
		
		viewpager = (ViewPager) findViewById(R.id.vp_ticket);
		viewpager.setOffscreenPageLimit(listnumber);
		
		imageButtonRefresh = (ImageButton) findViewById(R.id.refresh);
        imageButtonRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	refreshTask = new RefreshTask();
                if(Build.VERSION.SDK_INT < 11)
                	refreshTask.execute();
                else
                	refreshTask.executeOnExecutor(RefreshTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
        
        imageButtonChangeAccount = (ImageButton) findViewById(R.id.ib_change_account);
        imageButtonChangeAccount.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	Intent intent = new Intent(TicketCenterActivity.this, ChangeAccountActivity.class);
            	TicketCenterActivity.this.startActivityForResult(intent, TicketCenterActivity.CHANGEACCOUNT);
            }
        });
	}
	
	private void fetchData(){
		DramaAPI dramaAPI = new DramaAPI();
		
		mPreferentialtList = new ArrayList<Ticket>(30);
    	mMyTicketList = new ArrayList<Ticket>(30);
    	
    	mPreferentialtList = dramaAPI.getCampaignList();
    	
    	String email = shIO.SharePreferenceO("ticket_email", "");
    	mMyTicketList = dramaAPI.getMyTicketList(email);

   		unReadCount = shIO.SharePreferenceO("unread_ticket_count", 0);
	}
	
	// 設定畫面上的UI
    private void setViews() {
    	
    	if(unReadCount > 0) {
    		tvUnReadCount.setText(String.valueOf(unReadCount));
			tvUnReadCount.setVisibility(View.VISIBLE);
			Animation unReadCountAnim = AnimationUtils.loadAnimation(this, R.anim.alpha);
			tvUnReadCount.startAnimation(unReadCountAnim);
    	} else
    		tvUnReadCount.setVisibility(View.GONE);
    	
    	buttonPreferential.setOnClickListener(new itemOnClickListener(0));
    	rlMyTicketUnRead.setOnClickListener(new itemOnClickListener(1));
        
    	viewpageradapter = new TicketCenterViewPagerAdapter(this, mPreferentialtList, mMyTicketList);
        viewpager.setAdapter(viewpageradapter);
        viewpager.setPageMargin(20);
		viewpager.setCurrentItem(currIndex);
		viewpager.getAdapter().notifyDataSetChanged();
		viewpager.setOnPageChangeListener(new ListOnPageChangeListener());
    }
    
	private class itemOnClickListener implements OnClickListener {
		private int index = 0;
		public itemOnClickListener(int i) {
			index = i;
		}
		public void onClick(View v) {
			viewpager.setCurrentItem(index);
		}
	};
	
	private void initBottonTextViewColor() {
		buttonPreferential.setTextColor(TicketCenterActivity.this.getResources().getColor(R.color.black));
		tvUnRead.setTextColor(TicketCenterActivity.this.getResources().getColor(R.color.black));
    }
	
	private void setBottonView(int index) {
    	if(index == 0) {
    		buttonPreferential.setTextColor(TicketCenterActivity.this.getResources().getColor(R.color.channel_button_text_press));
    	} else if(index == 1) {	    	
    		tvUnRead.setTextColor(TicketCenterActivity.this.getResources().getColor(R.color.channel_button_text_press));
    	}
    }
	
	private void setCursor(int index) {
		int offset = cursorWidth + cursorOffset * 2;
		
		Animation animation = null;
		animation = new TranslateAnimation(currIndex * offset, index * offset, 0, 0);
		currIndex = index;
		animation.setFillAfter(true);
		animation.setDuration(300);
		cursor.startAnimation(animation);
		initBottonTextViewColor();
		setBottonView(index);
    }
	
	public class ListOnPageChangeListener implements OnPageChangeListener {
	
		public void onPageSelected(int index) {
			
			if(index == 1 && unReadCount > 0) {
				shIO.SharePreferenceI("unread_ticket_count", 0);
                if(index == 1)
                	tvUnReadCount.setVisibility(View.GONE);
			}
			
			setCursor(index);
		}
		
		public void onPageScrolled(int position, float arg1, int arg2) {
		}
	
		public void onPageScrollStateChanged(int state) {
		}
	}
	
	class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
              public void onCancel(DialogInterface arg0) {
                  LoadDataTask.this.cancel(true);
                  viewpager.setVisibility(View.GONE);
                  imageButtonRefresh.setVisibility(View.VISIBLE);
              }
          };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(TicketCenterActivity.this);
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
            fetchData();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {

        	if (mPreferentialtList == null || mMyTicketList == null || mPreferentialtList.size() <= 0) {
            	viewpager.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	viewpager.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
                setViews();
                setCursor(currIndex);
            }
        	closeProgressDilog();

            super.onPostExecute(result);
        }
        
        public void closeProgressDilog() {
        	if(TicketCenterActivity.this != null && !TicketCenterActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }

    class RefreshTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
              public void onCancel(DialogInterface arg0) {
            	  RefreshTask.this.cancel(true);
            	  viewpager.setVisibility(View.GONE);
                  imageButtonRefresh.setVisibility(View.VISIBLE);
              }
          };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(TicketCenterActivity.this);
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
            fetchData();
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {

        	if (mPreferentialtList == null || mMyTicketList == null) {
            	viewpager.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	viewpager.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
                setViews();
            }
        	closeProgressDilog();

            super.onPostExecute(result);
        }

        public void closeProgressDilog() {
        	if(TicketCenterActivity.this != null && !TicketCenterActivity.this.isFinishing() 
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
      if (refreshTask!= null && refreshTask.getStatus() != AsyncTask.Status.FINISHED) {
    	  refreshTask.closeProgressDilog();
    	  refreshTask.cancel(true);
      }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance().activityStop(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
