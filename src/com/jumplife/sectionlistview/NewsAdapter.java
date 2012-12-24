package com.jumplife.sectionlistview;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jumplife.imageload.ImageLoader;
import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.entity.News;

public class NewsAdapter extends BaseAdapter{
	private Context mContext;
	 private ArrayList<News> newsList;
	 private ImageLoader imageLoader;
	public NewsAdapter(Context mContext,  ArrayList<News> newsList){
		this.newsList = newsList;
		this.mContext = mContext;
		imageLoader = new ImageLoader(mContext); 
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

	public View getView(int position, View convertView, ViewGroup parent) {
		
		LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.listview_news_item, null);
		ImageView imageviewThumbnail = (ImageView)converView.findViewById(R.id.news_image);
		imageviewThumbnail.setScaleType(ImageView.ScaleType.FIT_XY);
		
		TextView textvieTitle = (TextView)converView.findViewById(R.id.textview_title);
		TextView textViewRealeaseDate = (TextView)converView.findViewById(R.id.textviewofreleasedate);
		if (newsList.get(position).getThumbnailUrl() != null) {
			//Log.d("NewsAdapter", "Thumbnail: " + newsList.get(position).getThumbnailUrl());
			imageLoader.DisplayImage(newsList.get(position).getThumbnailUrl(), imageviewThumbnail);
		}
		textvieTitle.setText(newsList.get(position).getTitle());
		
		DateFormat releaseFormatter = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		if(newsList.get(position).getSource() != null) 
			textViewRealeaseDate.setText(releaseFormatter.format(newsList.get(position).getReleaseDate()) + 
					"　　文章來源：" + newsList.get(position).getSource());
		else
			textViewRealeaseDate.setText(releaseFormatter.format(newsList.get(position).getReleaseDate()));
		
		return converView;

	}
}
