package com.jumplife.tvdrama;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.sectionlistview.DramaGridAdapter;
import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.sqlite.SQLiteTvDrama;
import com.jumplife.tvdrama.entity.Drama;
import com.jumplife.tvdrama.promote.PromoteAPP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TvChannelWaterFallActivity extends Activity {
    public final static int  SETTING          = 1;

    private GridView         dramaGridView;
    private ImageButton      imageButtonRefresh;
    private Button		     buttonTaiwan;
    private Button		     buttonKoera;
    private Button		     buttonJapan;
    private Button		     buttonChina;
    private Button           selectButton;
    private QuickAction      quickAction;
    private ImageView	     imageviewTaiwan;
    private ImageView	     imageviewKoera;
    private ImageView	     imageviewJapan;
    private ImageView	     imageviewChina;
    private LoadDataTask     loadTask;
    private updateDataTask	 updatetask;
	private TextView 		 topbar_text;

    private ArrayList<Drama> dramaList;
    private SQLiteTvDrama sqliteTvDrama;
    private DramaGridAdapter adapter;
    private SharePreferenceIO shIO;
    
    private final int FLAG_TAIWAN = 1;
    private final int FLAG_CHINA = 2;
    private final int FLAG_KOERA = 3;
    private final int FLAG_JAPAN = 4;
    
    private final int FLAG_FIRSTROUND  = 1;
    private final int FLAG_SECONDROUND = 2;
    private final int FLAG_THIS_WEEK   = 3;
    private final int FLAG_RECENT      = 4;
    private final int FLAG_TOP         = 5;
    
    private int functionFlag = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_tvchannelwaterfall);
        shIO = new SharePreferenceIO(this);
        functionFlag = shIO.SharePreferenceO("tvchannel_flag", 1);

        initViews();

        loadTask = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	loadTask.execute();
        else
        	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

        	PromoteAPP promoteAPP = new PromoteAPP(TvChannelWaterFallActivity.this);
        	if(!promoteAPP.isPromote) {
	        	new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.leave_app))
	            .setPositiveButton(getResources().getString(R.string.leave), new DialogInterface.OnClickListener() {
	                // do something when the button is clicked
	                public void onClick(DialogInterface arg0, int arg1) {
	                	TvChannelWaterFallActivity.this.finish();
	                }
	            }).setNegativeButton(getResources().getString(R.string.cancel), null)
	            .show();
		    } else
		    	promoteAPP.promoteAPPExe();

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    private void initViews() {
    	ActionItem firstItem = new ActionItem(FLAG_FIRSTROUND, "首輪電影");
        ActionItem secondItem = new ActionItem(FLAG_SECONDROUND, "二輪電影");
        ActionItem thisweekItem = new ActionItem(FLAG_THIS_WEEK, "本周新片");
        ActionItem recentItem = new ActionItem(FLAG_RECENT, "近期上映");
        ActionItem topItem = new ActionItem(FLAG_TOP, "票房排行");

        quickAction = new QuickAction(this, QuickAction.VERTICAL);

        // add action items into QuickAction
        quickAction.addActionItem(firstItem);
        quickAction.addActionItem(secondItem);
        quickAction.addActionItem(thisweekItem);
        quickAction.addActionItem(recentItem);
        quickAction.addActionItem(topItem);

        // Set listener for action item clicked
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickAction.getActionItem(pos);

                if (actionId == FLAG_FIRSTROUND) {
                	EasyTracker.getTracker().trackEvent("電影打卡瀑布流", "首輪電影", "", (long)0);
                    functionFlag = FLAG_FIRSTROUND;
                } else if (actionId == FLAG_SECONDROUND) {
                	EasyTracker.getTracker().trackEvent("電影打卡瀑布流", "二輪電影", "", (long)0);
                    functionFlag = FLAG_SECONDROUND;
                } else if (actionId == FLAG_THIS_WEEK) {
                	EasyTracker.getTracker().trackEvent("電影打卡瀑布流", "本周新片", "", (long)0);
                    functionFlag = FLAG_THIS_WEEK;
                } else if (actionId == FLAG_RECENT) {
                	EasyTracker.getTracker().trackEvent("電影打卡瀑布流", "近期上映", "", (long)0);
                    functionFlag = FLAG_RECENT;
                } else if (actionId == FLAG_TOP) {
                	EasyTracker.getTracker().trackEvent("電影打卡瀑布流", "票房排行", "", (long)0);
                    functionFlag = FLAG_TOP;
                }

                loadTask = new LoadDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	loadTask.execute();
                else
                	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });

        // set listnener for on dismiss event, this listener will be called only if QuickAction dialog was dismissed
        // by clicking the area outside the dialog.
        quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            public void onDismiss() {
                // Toast.makeText(getApplicationContext(), "Dismissed", Toast.LENGTH_SHORT).show();
            }
        });
        
        selectButton = (Button) findViewById(R.id.button_select);
        selectButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                quickAction.show(v);
            }
        });

        topbar_text = (TextView)findViewById(R.id.topbar_text);
        topbar_text.setText(getResources().getString(R.string.app_name));
        
    	sqliteTvDrama = new SQLiteTvDrama(this);
    	
    	dramaGridView = (GridView)findViewById(R.id.gridview_tvchannel);
        imageviewTaiwan = (ImageView)findViewById(R.id.arrow_1);
    	imageviewKoera = (ImageView)findViewById(R.id.arrow_2);
    	imageviewJapan = (ImageView)findViewById(R.id.arrow_3);
    	imageviewChina = (ImageView)findViewById(R.id.arrow_4);
    	
        imageButtonRefresh = (ImageButton) findViewById(R.id.refresh);
        imageButtonRefresh.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                updateDataTask task = new updateDataTask();
                if(Build.VERSION.SDK_INT < 11)
            		task.execute();
                else
                	task.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
        
        buttonTaiwan = (Button)findViewById(R.id.button_drama_taiwan);
        buttonTaiwan.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	EasyTracker.getTracker().trackEvent("連續劇", "種類", "台劇", (long)0);
            	initBottonTextViewColor();
            	initBottonImageViewVisible();
            	functionFlag = FLAG_TAIWAN;
            	setBottonView();
            	shIO.SharePreferenceI("tvchannel_flag", functionFlag);
            	updatetask = new updateDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	updatetask.execute();
                else
                	updatetask.executeOnExecutor(updateDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
        buttonKoera = (Button)findViewById(R.id.button_drama_koera);
        buttonKoera.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	EasyTracker.getTracker().trackEvent("連續劇", "種類", "韓劇", (long)0);
            	initBottonTextViewColor();
            	initBottonImageViewVisible();
            	functionFlag = FLAG_KOERA;
            	setBottonView();
            	shIO.SharePreferenceI("tvchannel_flag", functionFlag);
            	updatetask = new updateDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	updatetask.execute();
                else
                	updatetask.executeOnExecutor(updateDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
        buttonJapan = (Button)findViewById(R.id.button_drama_japan);
        buttonJapan.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	EasyTracker.getTracker().trackEvent("連續劇", "種類", "日劇", (long)0);
            	initBottonTextViewColor();
            	initBottonImageViewVisible();
            	functionFlag = FLAG_JAPAN;
            	setBottonView();
            	shIO.SharePreferenceI("tvchannel_flag", functionFlag);
            	updatetask = new updateDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	updatetask.execute();
                else
                	updatetask.executeOnExecutor(updateDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
        buttonChina = (Button)findViewById(R.id.button_drama_china);
        buttonChina.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
            	EasyTracker.getTracker().trackEvent("連續劇", "種類", "大陸劇", (long)0);
            	initBottonTextViewColor();
            	initBottonImageViewVisible();
            	functionFlag = FLAG_CHINA;
            	setBottonView();
            	shIO.SharePreferenceI("tvchannel_flag", functionFlag);
            	updatetask = new updateDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	updatetask.execute();
                else
                	updatetask.executeOnExecutor(updateDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });
        
        initBottonTextViewColor();
    	initBottonImageViewVisible();
    	setBottonView();
    }

    private void initBottonTextViewColor() {
    	buttonTaiwan.setTextColor(TvChannelWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_normal));
    	buttonKoera.setTextColor(TvChannelWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_normal));
    	buttonJapan.setTextColor(TvChannelWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_normal));
    	buttonChina.setTextColor(TvChannelWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_normal));
    }
    
    private void initBottonImageViewVisible() {
    	imageviewTaiwan.setVisibility(View.INVISIBLE);
    	imageviewKoera.setVisibility(View.INVISIBLE);
    	imageviewJapan.setVisibility(View.INVISIBLE);
    	imageviewChina.setVisibility(View.INVISIBLE);
    }
    
    private void setBottonView() {
    	if(functionFlag == FLAG_TAIWAN) {
    		buttonTaiwan.setTextColor(TvChannelWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_press));
    		imageviewTaiwan.setVisibility(View.VISIBLE);
    	} else if(functionFlag == FLAG_KOERA) {	    	
	    	buttonKoera.setTextColor(TvChannelWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_press));
	    	imageviewKoera.setVisibility(View.VISIBLE);
    	} else if(functionFlag == FLAG_JAPAN) {
	    	buttonJapan.setTextColor(TvChannelWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_press));
	    	imageviewJapan.setVisibility(View.VISIBLE);
    	} else {
	    	buttonChina.setTextColor(TvChannelWaterFallActivity.this.getResources().getColor(R.color.channel_button_text_press));
	    	imageviewChina.setVisibility(View.VISIBLE);
    	}
    	
    }
    
    // 設定畫面上的UI
    private void setViews() {
        dramaGridView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	//目前是哪一種種類的戲劇
            	String dramaType = "";
            	if(functionFlag == FLAG_TAIWAN) {
            		dramaType = "台劇";
            	} else if(functionFlag == FLAG_KOERA) {	    	
            		dramaType = "韓劇";
            	} else if(functionFlag == FLAG_JAPAN) {
            		dramaType = "日劇";
            	} else {
            		dramaType = "大陸劇";
            	}
            	
            	if(position < dramaList.size()) {
	            	EasyTracker.getTracker().trackEvent(dramaType, "節目名稱", dramaList.get(position).getChineseName(), (long)0);
	            	
	                Intent newAct = new Intent();
	                newAct.putExtra("drama_id", dramaList.get(position).getId());
	                newAct.putExtra("drama_name", dramaList.get(position).getChineseName());
	                newAct.putExtra("drama_poster", dramaList.get(position).getPosterUrl());
	                //newAct.setClass(TvChannelWaterFallActivity.this, DramaTabActivities.class);
	                newAct.setClass(TvChannelWaterFallActivity.this, DramaInfoChapterActivity.class);
	                startActivity(newAct);
            	}
            }
        });
        
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        
        adapter = new DramaGridAdapter(TvChannelWaterFallActivity.this, dramaList,
        		((screenWidth / 2)), (int) (((screenWidth / 2)) * 0.6));
        dramaGridView.setAdapter(adapter);
    }

    private void fetchData() {
    	dramaList = new ArrayList<Drama>(30);
    	dramaList = sqliteTvDrama.getDramaList(functionFlag);
    	Collections.sort(dramaList, new Comparator<Drama>(){
    		public int compare(Drama obj1,Drama obj2){
        		if(obj1.getViews() < obj2.getViews()) 
        			return 1;   
        		else if(obj1.getViews() == obj2.getViews()) 
        			return 0;
        		else
        			return -1;
    		}
    	});
    }
    
    class LoadDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
              public void onCancel(DialogInterface arg0) {
                  LoadDataTask.this.cancel(true);
                  dramaGridView.setVisibility(View.GONE);
                  imageButtonRefresh.setVisibility(View.VISIBLE);
              }
          };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(TvChannelWaterFallActivity.this);
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

            if (dramaList == null) {
            	dramaGridView.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	dramaGridView.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
                setViews();
            }

            super.onPostExecute(result);
        }
        
        public void closeProgressDilog() {
        	if(TvChannelWaterFallActivity.this != null && !TvChannelWaterFallActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }

    }

    class updateDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
              public void onCancel(DialogInterface arg0) {
            	  updateDataTask.this.cancel(true);
            	  dramaGridView.setVisibility(View.GONE);
                  imageButtonRefresh.setVisibility(View.VISIBLE);
              }
          };

        @Override
        protected void onPreExecute() {
            progressdialogInit = new ProgressDialog(TvChannelWaterFallActivity.this);
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

            if (dramaList == null) {
            	dramaGridView.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	dramaGridView.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
                setViews();
            }

            super.onPostExecute(result);
        }

        public void closeProgressDilog() {
        	if(TvChannelWaterFallActivity.this != null && !TvChannelWaterFallActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }
    
    public void showReloadDialog(final Context context) {
        AlertDialog.Builder alt_bld = new AlertDialog.Builder(context);

        alt_bld.setMessage(getResources().getString(R.string.reload_or_not))
        .setCancelable(true).setPositiveButton(getResources().getString(R.string.confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                loadTask = new LoadDataTask();
                loadTask.execute();
                dialog.dismiss();
            }
        });

        AlertDialog alert = alt_bld.create();
        alert.setTitle(getResources().getString(R.string.load_error));
        alert.setCancelable(false);
        alert.show();

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
