package com.jumplife.tvdrama;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import net.londatiga.android.ActionItem;
import net.londatiga.android.QuickAction;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.adapter.DramaViewPagerAdapter;
import com.jumplife.sqlite.SQLiteTvDramaHelper;
import com.jumplife.tvdrama.api.DramaAPI;
import com.jumplife.tvdrama.entity.Advertisement;
import com.jumplife.tvdrama.entity.Drama;
import com.jumplife.tvdrama.promote.PromoteAPP;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.TextView;

public class TvChannelViewPagerActivity extends Activity {
	
	private int currIndex;
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
    private SortDataTask	 sortDataTask;
    private LinearLayout     llSelect;
    private QuickAction      quickAction;
    private TextView 		 tvSelect;
	private TextView 		 topbar_text;
	private DramaViewPagerAdapter viewpageradapter;
	
    private List<ArrayList<Drama>> dramaLists;
    private static List<ArrayList<Drama>> hotDramaLists;
    private static List<ArrayList<Drama>> newDramaLists;
    private static List<ArrayList<Drama>> thirteenDramaLists;
    private static List<ArrayList<Drama>> twelveDramaLists;
    private static List<ArrayList<Drama>> beforeDramaLists;
    private static List<ArrayList<Drama>> recommendDramaLists;
	private List<Integer> mRecommendList;
    
    private final int FLAG_HOT = 0;
    private final int FLAG_NEW = 1;
    private final int FLAG_2013 = 2;
    private final int FLAG_2012 = 3;
    private final int FLAG_BEFORE = 4;
    private final int FLAG_RECOMMEND = 5;
    
    private int functionFlag;
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tvchannelviewpager);
		
		JSONObject crittercismConfig = new JSONObject();
        try {
            crittercismConfig.put("shouldCollectLogcat", true); // send logcat data for devices with API Level 16 and higher
        }
        catch (JSONException je){}
        //Crittercism.init(getApplicationContext(), "51ccf765558d6a0c25000003", crittercismConfig);
		
		/*Bundle extras = getIntent().getExtras();
		currIndex = extras.getInt("type_id", 0);
		if(currIndex > listnumber)
			currIndex = 0;
		
		functionFlag = extras.getInt("sort_id", 2);*/
        currIndex = TvDramaApplication.shIO.getInt("type_id", 0);
		if(currIndex > listnumber)
			currIndex = 0;
		
		functionFlag = TvDramaApplication.shIO.getInt("sort_id", 2);
				
        initViews();

        loadTask = new LoadDataTask();
        if(Build.VERSION.SDK_INT < 11)
        	loadTask.execute();
        else
        	loadTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
	}
    
    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance().activityStart(this);
        Thread thread = new Thread() { 
            @Override
            public void run() {
            	//Promote Test
                DramaAPI dramaAPI = new DramaAPI(TvChannelViewPagerActivity.this);
                ArrayList<Advertisement> advertisements = dramaAPI.getAdvertisementList(0);
                if(advertisements != null && advertisements.size() > 0) {
                	TvDramaApplication.shIO.edit().putInt("advertisemsent_id", -1).commit();
                	TvDramaApplication.shIO.edit().putString("advertisemsent_url", advertisements.get(0).getUrl()).commit();
                	TvDramaApplication.shIO.edit().putString("advertisemsent_title", advertisements.get(0).getTitle()).commit();
                	TvDramaApplication.shIO.edit().putString("advertisemsent_description", advertisements.get(0).getDescription()).commit();
                } else {
                	TvDramaApplication.shIO.edit().putInt("advertisemsent_id", 0).commit();
                }
            }
        };
        //開始執行執行緒
        thread.start();
    }
	
	private void initViews(){
		tvSelect = (TextView)findViewById(R.id.tv_select);
		//Drawable icon = getResources().getDrawable(R.drawable.check);
		
		ActionItem commentItem = new ActionItem(FLAG_RECOMMEND, "⊕影片推薦");
    	ActionItem firstItem = new ActionItem(FLAG_HOT, "依播放次數");
        ActionItem secondItem = new ActionItem(FLAG_NEW, "依上架時間");
        ActionItem thisweekItem = new ActionItem(FLAG_2013, "2013年");
        ActionItem recentItem = new ActionItem(FLAG_2012, "2012年");
        ActionItem topItem = new ActionItem(FLAG_BEFORE, "2011年以前");

        quickAction = new QuickAction(this, QuickAction.VERTICAL);

        // add action items into QuickAction
        quickAction.addActionItem(commentItem);
        quickAction.addActionItem(firstItem);
        quickAction.addActionItem(secondItem);
        quickAction.addActionItem(thisweekItem);
        quickAction.addActionItem(recentItem);
        quickAction.addActionItem(topItem);

        // Set listener for action item clicked
        quickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            public void onItemClick(QuickAction source, int pos, int actionId) {
                quickAction.getActionItem(pos);                
                setActionText(actionId);
                sortDataTask = new SortDataTask();
                if(Build.VERSION.SDK_INT < 11)
                	sortDataTask.execute();
                else
                	sortDataTask.executeOnExecutor(LoadDataTask.THREAD_POOL_EXECUTOR, 0);
            }
        });

        // set listnener for on dismiss event, this listener will be called only if QuickAction dialog was dismissed
        // by clicking the area outside the dialog.
        quickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            public void onDismiss() {
                // Toast.makeText(getApplicationContext(), "Dismissed", Toast.LENGTH_SHORT).show();
            }
        });
        
        llSelect = (LinearLayout) findViewById(R.id.ll_select);
        llSelect.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                quickAction.show(v);
            }
        });

        topbar_text = (TextView)findViewById(R.id.topbar_text);
        topbar_text.setText(getResources().getString(R.string.app_name));
        
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
		viewpager.setOffscreenPageLimit(listnumber);
		
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
        
    	viewpageradapter = new DramaViewPagerAdapter(this, dramaLists, mRecommendList);
        viewpager.setAdapter(viewpageradapter);
		viewpager.setCurrentItem(currIndex);
		viewpager.getAdapter().notifyDataSetChanged();
		viewpager.setOnPageChangeListener(new ListOnPageChangeListener());
    }

    @SuppressWarnings("unchecked")
	private void fetchData() {
        /*
         * get recommend id list
         */
    	String idLst = TvDramaApplication.shIO.getString("recommend_ids", "");
        mRecommendList = new ArrayList<Integer>();
        if(idLst != null && !idLst.equalsIgnoreCase("")) {
	        String[] recommendIds = idLst.split(",");
	        if(recommendIds.length > 0) {
		        for(int i=0; i<recommendIds.length; i++)
		        	mRecommendList.add(Integer.parseInt(recommendIds[i]));
	        }
        }
    	
    	//SQLiteTvDrama sqliteTvDrama = new SQLiteTvDrama(this);
    	SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
    	SQLiteDatabase db = instance.getReadableDatabase();
        db.beginTransaction();
        
    	dramaLists = new ArrayList<ArrayList<Drama>>();
    	recommendDramaLists = new ArrayList<ArrayList<Drama>>();
    	hotDramaLists = new ArrayList<ArrayList<Drama>>();
        newDramaLists = new ArrayList<ArrayList<Drama>>();
        thirteenDramaLists = new ArrayList<ArrayList<Drama>>();
        twelveDramaLists = new ArrayList<ArrayList<Drama>>();
        beforeDramaLists = new ArrayList<ArrayList<Drama>>();
        
    	ArrayList<Drama> dramaList = new ArrayList<Drama>(30);
    	ArrayList<Drama> newDramaList = new ArrayList<Drama>(30);
    	//dramaList = sqliteTvDrama.getDramaList(1);
    	dramaList = instance.getDramaList(db, 1);
    	newDramaList = (ArrayList<Drama>) dramaList.clone();
    	
    	recommendDramaLists.add(instance.getDramaList(db, idLst, 1));
    	hotSort(dramaList);
    	hotDramaLists.add(dramaList);
    	newSort(newDramaList);
        newDramaLists.add(newDramaList);
        thirteenDramaLists.add(yearSort(dramaList, 2013));
        twelveDramaLists.add(yearSort(dramaList, 2012));
        beforeDramaLists.add(yearBeforeSort(dramaList, 2011));
        
    	//dramaList = sqliteTvDrama.getDramaList(3);
    	dramaList = instance.getDramaList(db, 3);
    	recommendDramaLists.add(instance.getDramaList(db, idLst, 3));
    	newDramaList = (ArrayList<Drama>) dramaList.clone();
    	hotSort(dramaList);
    	hotDramaLists.add(dramaList);
    	newSort(newDramaList);
        newDramaLists.add(newDramaList);
        thirteenDramaLists.add(yearSort(dramaList, 2013));
        twelveDramaLists.add(yearSort(dramaList, 2012));
        beforeDramaLists.add(yearBeforeSort(dramaList, 2011));
        
    	//dramaList = sqliteTvDrama.getDramaList(4);
    	dramaList = instance.getDramaList(db, 4);
    	recommendDramaLists.add(instance.getDramaList(db, idLst, 4));
    	newDramaList = (ArrayList<Drama>) dramaList.clone();
    	hotSort(dramaList);
    	hotDramaLists.add(dramaList);
    	newSort(newDramaList);
        newDramaLists.add(newDramaList);
        thirteenDramaLists.add(yearSort(dramaList, 2013));
        twelveDramaLists.add(yearSort(dramaList, 2012));
        beforeDramaLists.add(yearBeforeSort(dramaList, 2011));
        
    	//dramaList = sqliteTvDrama.getDramaList(2);
    	dramaList = instance.getDramaList(db, 2);
    	recommendDramaLists.add(instance.getDramaList(db, idLst, 2));
    	newDramaList = (ArrayList<Drama>) dramaList.clone();
    	hotSort(dramaList);
    	hotDramaLists.add(dramaList);
    	newSort(newDramaList);
        newDramaLists.add(newDramaList);
        thirteenDramaLists.add(yearSort(dramaList, 2013));
        twelveDramaLists.add(yearSort(dramaList, 2012));
        beforeDramaLists.add(yearBeforeSort(dramaList, 2011));

        setSortList();
        
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        instance.closeHelper();
        
        
        //Promote Test
        int id = TvDramaApplication.shIO.getInt("advertisemsent_id", 0);
        if(id == -1) {
	    	Drama drama = new Drama();
	    	drama.setId(-1);
	    	drama.setPosterUrl(TvDramaApplication.shIO.getString("advertisemsent_url", ""));
	    	drama.setChineseName(TvDramaApplication.shIO.getString("advertisemsent_title", ""));
	    	drama.setIntroduction(TvDramaApplication.shIO.getString("advertisemsent_description", ""));
	    	

	    	for(int i=0; i<recommendDramaLists.size(); i++)
	    		recommendDramaLists.get(i).add(3, drama);
	    	for(int i=0; i<hotDramaLists.size(); i++)
	    		hotDramaLists.get(i).add(3, drama);
	    	for(int i=0; i<newDramaLists.size(); i++)
	    		newDramaLists.get(i).add(3, drama);
	    	for(int i=0; i<thirteenDramaLists.size(); i++)
	    		thirteenDramaLists.get(i).add(3, drama);
	    	for(int i=0; i<twelveDramaLists.size(); i++)
	    		twelveDramaLists.get(i).add(3, drama);
	    	for(int i=0; i<beforeDramaLists.size(); i++)
	    		beforeDramaLists.get(i).add(3, drama);
        }
    }
    
    private void setSortList() {
    	switch(functionFlag) {
	        case FLAG_RECOMMEND:
	        	dramaLists = recommendDramaLists;
	        	break;
	        case FLAG_HOT:
	        	dramaLists = hotDramaLists;
	        	break;
	        case FLAG_NEW:
	        	dramaLists = newDramaLists;
	        	break;
	        case FLAG_2013:
	        	dramaLists = thirteenDramaLists;
	        	break;
	        case FLAG_2012:
	        	dramaLists = twelveDramaLists;
	        	break;
	        case FLAG_BEFORE:
	        	dramaLists = beforeDramaLists;
	        	break;
	        default:
	        	dramaLists = hotDramaLists;
	        	break;
    	}
    }
    
    private void setActionText(int flag) {
    	switch(flag) {
	    	case FLAG_RECOMMEND:
	        	tvSelect.setText("⊕影片推薦");
	        	functionFlag = FLAG_RECOMMEND;
	        	break;
	        case FLAG_HOT:
	        	tvSelect.setText("依播放次數");
	        	functionFlag = FLAG_HOT;
	        	break;
	        case FLAG_NEW:
	        	tvSelect.setText("依上架時間");
	        	functionFlag = FLAG_NEW;
	        	break;
	        case FLAG_2013:
	        	tvSelect.setText("2013年");
	        	functionFlag = FLAG_2013;
	        	break;
	        case FLAG_2012:
	        	tvSelect.setText("2012年");
	        	functionFlag = FLAG_2012;
	        	break;
	        case FLAG_BEFORE:
	        	tvSelect.setText("2011年以前");
	        	functionFlag = FLAG_BEFORE;
	        	break;
    	}
        TvDramaApplication.shIO.edit().putInt("sort_id", functionFlag).commit();
    }
	
    private void hotSort(ArrayList<Drama> dramaList) {
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
    
    private void newSort(ArrayList<Drama> dramaList) {
    	Collections.sort(dramaList, new Comparator<Drama>(){
    		public int compare(Drama obj1,Drama obj2){
        		if(obj1.getId() < obj2.getId()) 
        			return 1;   
        		else if(obj1.getId() == obj2.getId()) 
        			return 0;
        		else
        			return -1;
    		}
    	});
    }
    
    @SuppressWarnings("deprecation")
	@SuppressLint("SimpleDateFormat")
	private ArrayList<Drama> yearSort(ArrayList<Drama> dramaList, int yy) {
    	ArrayList<Drama> tmpList = new ArrayList<Drama>(10);
    	for(int i=0; i<dramaList.size(); i++) {
    		if(!dramaList.get(i).getReleaseDate().equals("")) {
    			SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        		Date date = new Date();
				try {
					date = sdf.parse(dramaList.get(i).getReleaseDate());
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
				if((date.getYear()+1900) == yy)
	    			tmpList.add(dramaList.get(i));
    		}
    	}    	
		return tmpList;
    }
    
    @SuppressWarnings("deprecation")
	@SuppressLint("SimpleDateFormat")
	private ArrayList<Drama> yearBeforeSort(ArrayList<Drama> dramaList, int yy) {
    	ArrayList<Drama> tmpList = new ArrayList<Drama>(10);
    	for(int i=0; i<dramaList.size(); i++) {
    		if(!dramaList.get(i).getReleaseDate().equals("")) {
	    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
				Date date = new Date();
				try {
					date = sdf.parse(dramaList.get(i).getReleaseDate());
				} catch (ParseException e) {
					e.printStackTrace();
					continue;
				}
				if((date.getYear()+1900) <= yy)
	    			tmpList.add(dramaList.get(i));
    		}
    	}    	
		return tmpList;
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
			setCursor(index);
	        TvDramaApplication.shIO.edit().putInt("type_id", index).commit();
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

        	if (dramaLists.get(0) == null || dramaLists.get(1) == null || 
        			dramaLists.get(2) == null || dramaLists.get(3) == null) {
            	viewpager.setVisibility(View.GONE);
                imageButtonRefresh.setVisibility(View.VISIBLE);
            } else {
            	viewpager.setVisibility(View.VISIBLE);
                imageButtonRefresh.setVisibility(View.GONE);
                setViews();
                setActionText(functionFlag);
                setCursor(currIndex);
            }
        	closeProgressDilog();

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

        	if (dramaLists.get(0) == null || dramaLists.get(1) == null || 
        			dramaLists.get(2) == null || dramaLists.get(3) == null) {
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
        	if(TvChannelViewPagerActivity.this != null && !TvChannelViewPagerActivity.this.isFinishing() 
        			&& progressdialogInit != null && progressdialogInit.isShowing())
        		progressdialogInit.dismiss();
        }
    }
    
    class SortDataTask extends AsyncTask<Integer, Integer, String> {

        private ProgressDialog         progressdialogInit;
        private final OnCancelListener cancelListener = new OnCancelListener() {
              public void onCancel(DialogInterface arg0) {
            	  SortDataTask.this.cancel(true);
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
        	setSortList();
        	
            return "progress end";
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {

        	if (dramaLists != null && 
        			(dramaLists.get(0) == null || dramaLists.get(1) == null || 
        			dramaLists.get(2) == null || dramaLists.get(3) == null)) {
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