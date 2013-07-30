package com.jumplife.adapter;

import java.util.ArrayList;

import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.entity.Ticket;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import android.app.Activity;
import android.graphics.Bitmap.Config;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

public class MyTicketAdapter extends BaseAdapter {

	private Activity mActivity;
	private ArrayList<Ticket> tickets;
    private LayoutInflater myInflater;
	private int mLcdWidth = 0;  
    private float mDensity = 0;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private static ArrayList<Integer> mOpenItem = new ArrayList<Integer>(2);
	  
	
	private class ItemView {
		RelativeLayout rlMyTicket;
		ImageView ivPoster;
		TextView tvTitle;
		TextView tvContent;
		TextView tvSerial;
		LinearLayout llSerial;
	}
	
	public MyTicketAdapter(Activity mActivity, ArrayList<Ticket> tickets) {
		this.mActivity = mActivity;
		this.tickets = tickets;
		
		myInflater = LayoutInflater.from(mActivity); 
		
		DisplayMetrics dm = mActivity.getResources().getDisplayMetrics();  
        mLcdWidth = dm.widthPixels;  
        mDensity = dm.density;   
        
		options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.stub)
		.showImageForEmptyUri(R.drawable.stub)
		.showImageOnFail(R.drawable.stub)
		.imageScaleType(ImageScaleType.EXACTLY)
		.bitmapConfig(Config.RGB_565)
		.cacheOnDisc()
		.displayer(new SimpleBitmapDisplayer())
		.build();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return tickets.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return tickets.get(tickets.size() - position - 1);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemView itemView;
		if(convertView != null) {
			itemView = (ItemView)convertView.getTag();
		} else {
			convertView = myInflater.inflate(R.layout.listview_myticket_item, null);
			itemView = new ItemView();
			itemView.rlMyTicket = (RelativeLayout)convertView.findViewById(R.id.rl_myticket);
			itemView.ivPoster = (ImageView)convertView.findViewById(R.id.iv_myticket_poster);
			itemView.tvTitle = (TextView)convertView.findViewById(R.id.tv_myticket_itle);
			itemView.tvContent = (TextView)convertView.findViewById(R.id.tv_myticket_content);
			itemView.tvSerial = (TextView)convertView.findViewById(R.id.tv_myticket_serialnum);
			itemView.llSerial = (LinearLayout)convertView.findViewById(R.id.ll_myticket_serialnum);
			convertView.setTag(itemView);
		}
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        		displayMetrics.widthPixels, displayMetrics.widthPixels * 7 / 30, Gravity.CENTER);
        itemView.ivPoster.setLayoutParams(params);
        itemView.ivPoster.setScaleType(ScaleType.CENTER_CROP);
        
		imageLoader.displayImage(tickets.get(tickets.size() - position - 1).getUrl(), itemView.ivPoster, options);
		itemView.tvTitle.setText(tickets.get(tickets.size() - position - 1).getTitle());
		itemView.tvContent.setText(Html.fromHtml(tickets.get(tickets.size() - position - 1).getDescription()));
		itemView.tvSerial.setText(Html.fromHtml(
				"<small>你的序號</small>   <big>" + tickets.get(tickets.size() - position - 1).getSerialNum() + "</big>"));
		
		LayoutParams layoutParams = (LayoutParams)itemView.rlMyTicket.getLayoutParams();
		layoutParams.setMargins(30, 0, 30, 0);
		itemView.rlMyTicket.setLayoutParams(layoutParams);  
		
		/*
		 * Hide Expandable Descriptions
		 */
		int widthSpec = MeasureSpec.makeMeasureSpec((int) (mLcdWidth - 10 * mDensity), MeasureSpec.EXACTLY);  
		itemView.llSerial.measure(widthSpec, 0);  
        LinearLayout.LayoutParams paramsDescription = (LinearLayout.LayoutParams) itemView.llSerial.getLayoutParams();
		/*
		 * 判斷 Expandable 的部分是隱藏還是展開 
		 */
		if(getItemStatus(tickets.size() - position - 1) == false){  
			paramsDescription.bottomMargin = -itemView.llSerial.getMeasuredHeight();  
	        itemView.llSerial.setVisibility(View.GONE);  
	    }else{  
	    	paramsDescription.bottomMargin = 0;  
	        itemView.llSerial.setVisibility(View.VISIBLE);  
	    }
        
		return convertView;
	}
	
	public static void itemStatusChanged(int pos){  
	    for(int i = 0; i < mOpenItem.size(); i ++){  
	        int ipos = mOpenItem.get(i);  
	        if(ipos == pos){  
	            mOpenItem.remove(i);  
	            return;  
	        }  
	    }
	    Log.d(null, "pos : " + pos);
	    mOpenItem.add(pos);
	} 
	
	private boolean getItemStatus(int pos){  
	    for(int i = 0; i < mOpenItem.size(); i ++){  
	        int ipos = mOpenItem.get(i);  
	        if(ipos == pos){  
	            return true;  
	        }  
	    }  
	    return false;  
	}
}
