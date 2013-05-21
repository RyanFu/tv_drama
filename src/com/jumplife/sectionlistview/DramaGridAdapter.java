package com.jumplife.sectionlistview;

import java.util.ArrayList;

import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.entity.Drama;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DramaGridAdapter extends BaseAdapter{

	private ArrayList<Drama> dramas;
	private Context mContext;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	private LayoutInflater myInflater;
	
	private int width;
	private int height;
	private class ItemView {
		ImageView poster;
		TextView name;
		TextView view;
	}
	
	public DramaGridAdapter(Context mContext, ArrayList<Drama> dramas){
		this.dramas = dramas;
		this.mContext = mContext;
		width = 80;
		height = 120;
		//imageLoader=new ImageLoader(mContext, width);
		myInflater = LayoutInflater.from(mContext);
		
		options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.stub)
		.showImageForEmptyUri(R.drawable.stub)
		.showImageOnFail(R.drawable.stub)
		.imageScaleType(ImageScaleType.EXACTLY)
		.cacheOnDisc()
		.cacheInMemory()
		.displayer(new SimpleBitmapDisplayer())
		.build();
	}
	
	public DramaGridAdapter(Context mContext, ArrayList<Drama> dramas, int width, int height){
		this.dramas = dramas;
		this.mContext = mContext;
		this.width = width;
		this.height = height;
		//imageLoader=new ImageLoader(mContext, width);
		myInflater = LayoutInflater.from(mContext);
		
		options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.stub)
		.showImageForEmptyUri(R.drawable.stub)
		.showImageOnFail(R.drawable.stub)
		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		.cacheOnDisc()
		.cacheInMemory()
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
			itemView.poster = (ImageView)convertView.findViewById(R.id.drama_poster);
			itemView.name = (TextView)convertView.findViewById(R.id.drama_name);
			itemView.view = (TextView)convertView.findViewById(R.id.drama_view);
			
			convertView.setTag(itemView);
		}
		
		itemView.poster.setScaleType(ImageView.ScaleType.FIT_CENTER);
		itemView.poster.getLayoutParams().height = height;
		itemView.poster.getLayoutParams().width = width;
		itemView.name.setText(dramas.get(position).getChineseName());
		itemView.view.setText(mContext.getResources().getString(R.string.play_time) + dramas.get(position).getViews());
		imageLoader.displayImage(dramas.get(position).getPosterUrl(), itemView.poster, options);
		
		return convertView;
	}

}
