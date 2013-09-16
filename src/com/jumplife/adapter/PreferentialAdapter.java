package com.jumplife.adapter;

import java.util.ArrayList;

import com.jumplife.dialog.GetSerialActivity;
import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.TicketCenterActivity;
import com.jumplife.tvdrama.animation.ViewExpandAnimation;
import com.jumplife.tvdrama.entity.Ticket;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap.Config;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public class PreferentialAdapter extends BaseAdapter {
	
	private Activity mActivity;
	private ArrayList<Ticket> tickets;
    private LayoutInflater myInflater;
	private int mLcdWidth = 0;  
    private float mDensity = 0;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private ArrayList<Integer> mOpenItem;
	private String advertisementType;
	
	private class ItemView {
		LinearLayout llPreferential;
		ImageView ivPoster;
		ImageView ivMore;
		TextView tvMore;
		TextView tvTitle;
		TextView tvDescription;
		TextView tvCount;
		Button buttonSerial;
		LinearLayout llMore;
	}
	
	public PreferentialAdapter(Activity mActivity, ArrayList<Ticket> tickets, String advertisementType) {
		this.mActivity = mActivity;
		this.tickets = tickets;
		this.advertisementType = advertisementType;
		this.mOpenItem = new ArrayList<Integer>(2);
		
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
		.cacheOnDisc(true)
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
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemView itemView;
		if(convertView != null) {
			itemView = (ItemView)convertView.getTag();
		} else {
			convertView = myInflater.inflate(R.layout.listview_preferential_item, null);
			itemView = new ItemView();
			itemView.llPreferential = (LinearLayout)convertView.findViewById(R.id.ll_preferential);
			itemView.ivPoster = (ImageView)convertView.findViewById(R.id.iv_preferential);
			itemView.ivMore = (ImageView)convertView.findViewById(R.id.iv_preferential_more);
			itemView.tvMore = (TextView)convertView.findViewById(R.id.tv_preferential_more);
			itemView.tvTitle = (TextView)convertView.findViewById(R.id.tv_preferential_title);
			itemView.tvDescription = (TextView)convertView.findViewById(R.id.tv_preferential_description);
			itemView.tvCount = (TextView)convertView.findViewById(R.id.tv_preferential_count);
			itemView.buttonSerial = (Button)convertView.findViewById(R.id.button_serial);
			itemView.llMore = (LinearLayout)convertView.findViewById(R.id.ll_more);
			convertView.setTag(itemView);
		}
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
        		displayMetrics.widthPixels, displayMetrics.widthPixels / 2, Gravity.CENTER);
        itemView.ivPoster.setLayoutParams(params);
        itemView.ivPoster.setScaleType(ScaleType.CENTER_CROP);
        
		imageLoader.displayImage(tickets.get(tickets.size() - position - 1).getUrl(), itemView.ivPoster, options);
		itemView.tvCount.setText(Html.fromHtml(tickets.get(tickets.size() - position - 1).getSerialNum() + "<small>人已兌換</small>"));
		itemView.tvTitle.setText(tickets.get(tickets.size() - position - 1).getTitle());
		itemView.tvDescription.setText(Html.fromHtml(tickets.get(tickets.size() - position - 1).getDescription()));
		itemView.tvDescription.setMovementMethod(LinkMovementMethod.getInstance());
		itemView.buttonSerial.setOnClickListener(new itemSerialClick(tickets.get(tickets.size() - position - 1).getId()));
		itemView.llMore.setOnClickListener(new itemMoreClick(itemView, tickets.size() - position - 1));
		
		LayoutParams layoutParams = (LayoutParams)itemView.llPreferential.getLayoutParams();
		layoutParams.setMargins(15, 0, 15, 0);
		itemView.llPreferential.setLayoutParams(layoutParams);
		
		/*
		 * Hide Expendable Descriptions
		 */
		int widthSpec = MeasureSpec.makeMeasureSpec((int) (mLcdWidth - 10 * mDensity), MeasureSpec.EXACTLY);  
		itemView.tvDescription.measure(widthSpec, 0);  
        LinearLayout.LayoutParams paramsDescription = (LinearLayout.LayoutParams) itemView.tvDescription.getLayoutParams(); 
		/*
		 * 判斷 Expandable 的部分是隱藏還是展開 
		 */
		if(getItemStatus(tickets.size() - position - 1) == false){  
			paramsDescription.bottomMargin = -itemView.tvDescription.getMeasuredHeight();
			itemView.tvMore.setText("展開活動詳情");
	        itemView.tvDescription.setVisibility(View.GONE);  
	        itemView.ivMore.setImageResource(R.drawable.open);
	    }else{  
	    	paramsDescription.bottomMargin = 0;
	    	itemView.tvMore.setText("收起活動詳情");
	        itemView.tvDescription.setVisibility(View.VISIBLE);
	        itemView.ivMore.setImageResource(R.drawable.close);
	    }
        
		return convertView;
	}
	
	private class itemSerialClick implements OnClickListener {
		int campaignId;
		
		public itemSerialClick(int campaignId) {
			this.campaignId = campaignId;
		}
		
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(mActivity, GetSerialActivity.class);
			intent.putExtra("campaign_id", campaignId);
			intent.putExtra("advertisement_type", advertisementType);
			mActivity.startActivityForResult(intent, TicketCenterActivity.GETTICKET);
		}		
	}

	private class itemMoreClick implements OnClickListener {
		ItemView itemView;
		int pos;
		
		public itemMoreClick(ItemView itemView, int pos) {
			this.itemView = itemView;
			this.pos = pos;
		}
		
		@Override
		public void onClick(View v) {
			itemStatusChanged(pos);
			if(getItemStatus(pos) == false){  
				itemView.tvMore.setText("展開活動詳情");
				itemView.tvDescription.setVisibility(View.GONE);  
		        itemView.ivMore.setImageResource(R.drawable.open);
		    }else{  
		    	itemView.tvMore.setText("收起活動詳情");
		    	itemView.tvDescription.setVisibility(View.VISIBLE);
		        itemView.ivMore.setImageResource(R.drawable.close);
		    }
			itemView.tvDescription.startAnimation(new ViewExpandAnimation(itemView.tvDescription));
		}		
	}
	
	private void itemStatusChanged(int pos){  
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
