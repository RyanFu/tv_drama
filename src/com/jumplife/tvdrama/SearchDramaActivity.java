package com.jumplife.tvdrama;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.analytics.tracking.android.EasyTracker;
import com.jumplife.adapter.SearchListAdapter;
import com.jumplife.sqlite.SQLiteTvDramaHelper;
import com.jumplife.tvdrama.entity.Drama;
import com.jumplife.tvdrama.promote.PromoteAPP;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchDramaActivity extends Activity {
	private ListView listviewSearch;
	private EditText edittextSearch;
	private ArrayList<Drama> dramas = new ArrayList<Drama>();
	private ArrayList<Drama> temps;
	private ArrayList<String> arr_sort = new ArrayList<String>();
	private SearchListAdapter adapter;
	int textlength=0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		JSONObject crittercismConfig = new JSONObject();
        try {
        	crittercismConfig.put("delaySendingAppLoad", true); // send app load data with Crittercism.sendAppLoadData()
            crittercismConfig.put("shouldCollectLogcat", true); // send logcat data for devices with API Level 16 and higher
        	crittercismConfig.put("includeVersionCode", true); // include version code in version name
        }
        catch (JSONException je){}
        //Crittercism.init(getApplicationContext(), "51ccf765558d6a0c25000003", crittercismConfig);
        
		setContentView(R.layout.activity_searchdrama);
		
		initView();
	}

	private void initView() {
		
		/*SQLiteTvDrama sqlTvDrama = new SQLiteTvDrama(this); 
		dramas = sqlTvDrama.getDramaList();
		sqlTvDrama.closeDB();*/
		SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
        SQLiteDatabase db = instance.getWritableDatabase();
        dramas = instance.getDramaList(db);
        db.close();
        instance.closeHelper();
        
		temps = new ArrayList<Drama>(dramas);
		for(int i=0; i<dramas.size(); i++)
			arr_sort.add(dramas.get(i).getChineseName());
		
		listviewSearch=(ListView)findViewById(R.id.ListView_search);
		edittextSearch=(EditText)findViewById(R.id.EditText_search);
		
		View footer = (View) View.inflate(this, R.layout.listview_search_footer, null);
		
		edittextSearch.addTextChangedListener(filterTextWatcher);
		adapter = new SearchListAdapter(this, arr_sort);
		listviewSearch.addFooterView(footer, null, false);
		listviewSearch.setAdapter(adapter);
		
		listviewSearch.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent newAct = new Intent();
                newAct.putExtra("drama_id", temps.get(position).getId());
                newAct.putExtra("drama_name", temps.get(position).getChineseName());
                newAct.putExtra("drama_poster", temps.get(position).getPosterUrl());
                newAct.setClass(SearchDramaActivity.this, DramaInfoChapterActivity.class);
                startActivity(newAct);
            }
        });
	}
	
	private TextWatcher filterTextWatcher = new TextWatcher() {

	    public void afterTextChanged(Editable s) {
	    }

	    public void beforeTextChanged(CharSequence s, int start, int count,
	            int after) {
	    }

	    public void onTextChanged(CharSequence s, int start, int before,
	            int count) {
	    	boolean hasItem = false;
	    	textlength = edittextSearch.getText().length();
	    	temps.clear();
			arr_sort.clear();
			for(int i=0; i<dramas.size(); i++) {
				if(textlength <= dramas.get(i).getChineseName().length()) {
					for(int j=0; j<=(dramas.get(i).getChineseName().length()-textlength); j++)
						if(edittextSearch.getText().toString().equalsIgnoreCase((String) dramas.get(i).getChineseName().subSequence(j, j+textlength))) 
							hasItem = true;
					if(hasItem) {
						temps.add(dramas.get(i));
						arr_sort.add(dramas.get(i).getChineseName());
						hasItem = false;
					}
				}
				
			}
			adapter.notifyDataSetChanged();
	    }

	};

	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    edittextSearch.removeTextChangedListener(filterTextWatcher);
	}
	

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {

        	if(!edittextSearch.getText().toString().equals(""))
        		edittextSearch.setText("");
        	else {
	        	PromoteAPP promoteAPP = new PromoteAPP(SearchDramaActivity.this);
	        	if(!promoteAPP.isPromote) {
		        	new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.leave_app))
		            .setPositiveButton(getResources().getString(R.string.leave), new DialogInterface.OnClickListener() {
		                // do something when the button is clicked
		                public void onClick(DialogInterface arg0, int arg1) {
		                	SearchDramaActivity.this.finish();
		                }
		            }).setNegativeButton(getResources().getString(R.string.cancel), null)
		            .show();
			    } else
			    	promoteAPP.promoteAPPExe();
        	}

            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
      	EasyTracker.getInstance().activityStart(this);
      	
      	if(dramas == null || dramas.size() <= 0) {
      		SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(this);
            SQLiteDatabase db = instance.getWritableDatabase();
            dramas = instance.getDramaList(db);
            db.close();
            instance.closeHelper();
      	}
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	EasyTracker.getInstance().activityStop(this);
    }
}
