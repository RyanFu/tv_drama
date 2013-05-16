package com.jumplife.sectionlistview;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.entity.News;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

public class NewsAdapter extends BaseAdapter{
	private Context mContext;
	private ArrayList<News> newsList;
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
	
	public NewsAdapter(Context mContext,  ArrayList<News> newsList){
		this.newsList = newsList;
		this.mContext = mContext;
		
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
		
		return newsList.size();
	}

	public Object getItem(int position) {

		return newsList.get(position);
	}

	public long getItemId(int position) {
	
		return position;
	}

	@SuppressLint("SimpleDateFormat")
	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.listview_news_item, null);
		ImageView imageviewThumbnail = (ImageView)converView.findViewById(R.id.news_image);
		imageviewThumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
		
		TextView textvieTitle = (TextView)converView.findViewById(R.id.textview_title);
		TextView textViewRealeaseDate = (TextView)converView.findViewById(R.id.textviewofreleasedate);
		if (newsList.get(position).getThumbnailUrl() != null) {
			//Log.d("NewsAdapter", "Thumbnail: " + newsList.get(position).getThumbnailUrl());
			imageLoader.displayImage(newsList.get(position).getThumbnailUrl(), imageviewThumbnail, options);
		}
		textvieTitle.setText(newsList.get(position).getTitle());
		
		DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		if(newsList.get(position).getSource() != null) 
			textViewRealeaseDate.setText(releaseFormatter.format(newsList.get(position).getReleaseDate()) + 
					mContext.getResources().getString(R.string.content_source) + newsList.get(position).getSource());
		else
			textViewRealeaseDate.setText(releaseFormatter.format(newsList.get(position).getReleaseDate()));
		
		return converView;

	}
}
