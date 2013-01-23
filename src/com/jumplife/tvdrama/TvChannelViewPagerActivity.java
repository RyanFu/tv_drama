package com.jumplife.tvdrama;

import java.util.ArrayList;
import java.util.List;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.sectionlistview.ViewPagerAdapter;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.sqlite.SQLiteTvDrama;
import com.jumplife.tvdrama.entity.Drama;
import com.jumplife.tvdrama.promote.PromoteAPP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class TvChannelViewPagerActivity extends Activity {
	
	private int currIndex = 0;
	private int cursorWidth;
	private int cursorOffset = 25;
	private int listnumber = 4;
	
	private ViewPager viewpager;
	private ImageView cursor;	
	private ImageButton      imageButtonRefresh;
    private Button		     buttonTaiwan;
    private Button		     buttonKoera;
    private Button		     buttonJapan;
    private Button		     buttonChina;
    private LoadDataTask     loadTask;
    private updateDataTask   updatetask;

    private List<ArrayList<Drama>> dramaLists;
    private SQLiteTvDrama sqliteTvDrama;
    private SharePreferenceIO shIO;
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tvchannelviewpager);

        initViews();

        loadTask = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	loadTask.execute();
        else
        	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
	}
	
	private void initViews(){
		sqliteTvDrama = new SQLiteTvDrama(this);
        shIO = new SharePreferenceIO(this);
        shIO.SharePreferenceO("tvchannel_flag", 1);
    	
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		cursorWidth = dm.widthPixels / listnumber - cursorOffset * 2;

		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(cursorWidth, 15);
		lp.setMargins(cursorOffset, 0, cursorOffset, 0);
		cursor = (ImageView) findViewById(R.id.arrow_1);
		cursor.setLayoutParams(lp);
		
		buttonTaiwan = (Button)findViewById(R.id.button_drama_taiwan);
		buttonKoera = (Button)findViewById(R.id.button_drama_koera);
		buttonJapan = (Button)findViewById(R.id.button_drama_japan);
		buttonChina = (Button)findViewById(R.id.button_drama_china);
		
		viewpager = (ViewPager) findViewById(R.id.viewpager_tvchannel);
		viewpager.setOffscreenPageLimit(3);
		
		imageButtonRefresh = (ImageButton) findViewById(R.id.refresh);
        imageButtonRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	updatetask = new updateDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	updatetask.execute();
                else
                	updatetask.executeOnExecutor(updateDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
	}
	
	// 設定畫面上的UI
    private void setViews() {        
    	buttonTaiwan.setOnClickListener(new itemOnClickListener(0));
    	buttonKoera.setOnClickListener(new itemOnClickListener(1));
    	buttonJapan.setOnClickListener(new itemOnClickListener(2));
    	buttonChina.setOnClickListener(new itemOnClickListener(3));
        
        viewpager.setAdapter(new ViewPagerAdapter(this, dramaLists));
		viewpager.setCurrentItem(0);
		
		viewpager.setOnPageChangeListener(new ListOnPageChangeListener());
    }

    private void fetchData() {
    	dramaLists = new ArrayList<ArrayList<Drama>>();
    	ArrayList<Drama> dramaList = new ArrayList<Drama>(30);
    	dramaList = sqliteTvDrama.getDramaList(1);
    	dramaLists.add(dramaList);
    	dramaList = sqliteTvDrama.getDramaList(2);
    	dramaLists.add(dramaList);
    	dramaList = sqliteTvDrama.getDramaList(3);
    	dramaLists.add(dramaList);
    	dramaList = sqliteTvDrama.getDramaList(4);
    	dramaLists.add(dramaList);
    }
	
	public class itemOnClickListener implements OnClickListener {
		private int index = 0;
		public itemOnClickListener(int i) {
			index = i;
		}
		public void onClick(View v) {
			viewpager.setCurrentItem(index);
		}
	};
	
	private void initBottonTextViewColor() {
		buttonTaiwan.setTextColor(TvChannelViewPagerActivity.this.getResources().getColor(R.color.channel_button_text_normal));
		buttonKoera.setTextColor(TvChannelViewPagerActivity.this.getResources().getColor(R.color.channel_button_text_normal));
		buttonJapan.setTextColor(TvChannelViewPagerActivity.this.getResources().getColor(R.color.channel_button_text_normal));
		buttonChina.setTextColor(TvChannelViewPagerActivity.this.getResources().getColor(R.color.channel_button_text_normal));
    }
	
	private void setBottonView(int index) {
    	if(index == 0) {
    		buttonTaiwan.setTextColor(TvChannelViewPagerActivity.this.getResources().getColor(R.color.channel_button_text_press));
    	} else if(index == 1) {	    	
    		buttonKoera.setTextColor(TvChannelViewPagerActivity.this.getResources().getColor(R.color.channel_button_text_press));
    	} else if(index == 2) {
    		buttonJapan.setTextColor(TvChannelViewPagerActivity.this.getResources().getColor(R.color.channel_button_text_press));
    	} else if(index == 3) {
    		buttonChina.setTextColor(TvChannelViewPagerActivity.this.getResources().getColor(R.color.channel_button_text_press));
    	}
    }
	
	private void setCursor(int index) {
		int one = cursorWidth + cursorOffset * 2;
		int two = one * 2;
		int three = one * 3;
		
		Animation animation = null;
		switch (index) {
			case 0:
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, 0, 0, 0);
				} if (currIndex == 3) {
					animation = new TranslateAnimation(three, 0, 0, 0);
				}
				break;
			case 1:
				if (currIndex == 0) {
					animation = new TranslateAnimation(0, one, 0, 0);
				} else if (currIndex == 2) {
					animation = new TranslateAnimation(two, one, 0, 0);
				}
				break;
			case 2:
				if (currIndex == 1) {
					animation = new TranslateAnimation(one, two, 0, 0);
				} else if (currIndex == 3){
					animation = new TranslateAnimation(three, two, 0, 0);
				}
				break;
			case 3:
				if (currIndex == 2) {
					animation = new TranslateAnimation(two, three, 0, 0);
				} else if (currIndex == 0){
					animation = new TranslateAnimation(0, three, 0, 0);
				}
				break;
		}
		currIndex = index;
		animation.setFillAfter(true);
		animation.setDuration(300);
		cursor.startAnimation(animation);
		initBottonTextViewColor();
		setBottonView(index);
    }
	
	public class ListOnPageChangeListener implements OnPageChangeListener {
	
		public void onPageSelected(int index) {
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
            progressdialogInit = new ProgressDialog(TvChannelViewPagerActivity.this);
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
        	closeProgressDilog();

        	if (dramaLists.get(0) == null || dramaLists.get(1) == null || 
        			dramaLists.get(2) == null || dramaLists.get(3) == null) {
            	viewpager.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	viewpager.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
                setViews();
            }

            super.onPostExecute(result);
        }
        
        public void closeProgressDilog() {
        	if(TvChannelViewPagerActivity.this != null && !TvChannelViewPagerActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }

    class updateDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
              public void onCancel(DialogInterface arg0) {
            	  updateDataTask.this.cancel(true);
            	  viewpager.setVisibility(View.GONE);
                  imageButtonRefresh.setVisibility(View.VISIBLE);
              }
          };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(TvChannelViewPagerActivity.this);
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
        	closeProgressDilog();

        	if (dramaLists.get(0) == null || dramaLists.get(1) == null || 
        			dramaLists.get(2) == null || dramaLists.get(3) == null) {
            	viewpager.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	viewpager.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
                setViews();
            }

            super.onPostExecute(result);
        }

        public void closeProgressDilog() {
        	if(TvChannelViewPagerActivity.this != null && !TvChannelViewPagerActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

        	PromoteAPP promoteAPP = new PromoteAPP(TvChannelViewPagerActivity.this);
        	if(!promoteAPP.isPromote) {
	        	new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.leave_app))
	            .setPositiveButton(getResources().getString(R.string.leave), new DialogInterface.OnClickListener() {
	                // do something when the button is clicked
	                public void onClick(DialogInterface arg0, int arg1) {
	                	TvChannelViewPagerActivity.this.finish();
	                }
	            }).setNegativeButton(getResources().getString(R.string.cancel), null)
	            .show();
		    } else
		    	promoteAPP.promoteAPPExe();

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      if (loadTask!= null && loadTask.getStatus() != AsyncTask.Status.FINISHED) {
    	  loadTask.closeProgressDilog();
    	  loadTask.cancel(true);
      }
      if (updatetask!= null && updatetask.getStatus() != AsyncTask.Status.FINISHED) {
    	  updatetask.closeProgressDilog();
    	  updatetask.cancel(true);
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