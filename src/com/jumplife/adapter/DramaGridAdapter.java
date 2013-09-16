package com.jumplife.adapter;

import java.util.ArrayList;
import java.util.List;

import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.entity.Drama;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import android.content.Context;
import android.graphics.Bitmap.Config;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DramaGridAdapter extends BaseAdapter{

	private ArrayList<Drama> dramas;
	private List<Integer> mRecommendList;
	private Context mContext;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private LayoutInflater myInflater;
	
	private int width;
	private int height;
	private class ItemView {
		RelativeLayout rlDrama;
		ImageView poster;
		ImageView recommend;
		TextView name;
		TextView view;
	}
	
	public DramaGridAdapter(Context mContext, ArrayList<Drama> dramas, List<Integer> mRecommendList, int width, int height){
		this.dramas = dramas;
		this.mRecommendList = mRecommendList;
		this.mContext = mContext;
		this.width = width;
		this.height = height;
		//imageLoader=new ImageLoader(mContext, width);
		myInflater = LayoutInflater.from(mContext);
		
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
	
	public int getCount() {
		return dramas.size();
	}

	public Object getItem(int position) {
		return dramas.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ItemView itemView = new ItemView();;
		
		if (convertView != null) {
			itemView = (ItemView) convertView.getTag();
		} else {
			convertView = myInflater.inflate(R.layout.gridview_drama_item, null);
			itemView.rlDrama = (RelativeLayout)convertView.findViewById(R.id.drama_item);
			itemView.poster = (ImageView)convertView.findViewById(R.id.drama_poster);
			itemView.recommend = (ImageView)convertView.findViewById(R.id.drama_recommend);
			itemView.name = (TextView)convertView.findViewById(R.id.drama_name);
			itemView.view = (TextView)convertView.findViewById(R.id.drama_view);
			
			convertView.setTag(itemView);
		}
		
		if(dramas.get(position).getId() == -1) {
			itemView.rlDrama.getLayoutParams().height = width * 29 / 30;
			itemView.rlDrama.getLayoutParams().width = width;
			itemView.poster.setScaleType(ImageView.ScaleType.FIT_CENTER);
			itemView.recommend.setVisibility(View.INVISIBLE);
			if(dramas.get(position).getChineseName().equals("")) {
				itemView.poster.getLayoutParams().height = width * 29 / 30;
				itemView.poster.getLayoutParams().width = width;
				itemView.name.setVisibility(View.GONE);
				itemView.view.setVisibility(View.GONE);
			} else {
				itemView.poster.getLayoutParams().height = height;
				itemView.poster.getLayoutParams().width = width;
				itemView.name.setText(dramas.get(position).getChineseName());
				itemView.view.setText(dramas.get(position).getIntroduction());	
				itemView.name.setVisibility(View.VISIBLE);
				itemView.view.setVisibility(View.VISIBLE);	
			}
		} else {			
			itemView.rlDrama.getLayoutParams().height = width * 29 / 30;
			itemView.rlDrama.getLayoutParams().width = width;
			itemView.poster.setScaleType(ImageView.ScaleType.FIT_CENTER);
			itemView.poster.getLayoutParams().height = height;
			itemView.poster.getLayoutParams().width = width;
			itemView.name.setText(dramas.get(position).getChineseName());
			itemView.view.setText(mContext.getResources().getString(R.string.play_time) + dramas.get(position).getViews());	
			itemView.name.setVisibility(View.VISIBLE);
			itemView.view.setVisibility(View.VISIBLE);
			if(mRecommendList != null && mRecommendList.contains(dramas.get(position).getId()))
				itemView.recommend.setVisibility(View.VISIBLE);
			else
				itemView.recommend.setVisibility(View.INVISIBLE);
			
		}
		imageLoader.displayImage(dramas.get(position).getPosterUrl(), itemView.poster, options);
		
		
		
		return convertView;
	}

}
