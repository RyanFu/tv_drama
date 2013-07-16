package com.jumplife.adapter;

import java.util.ArrayList;

import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.entity.Chapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DramaChapterGridAdapter extends BaseAdapter{

	private ArrayList<Chapter> chapters;
	private Context mContext;
	private int current;
	
	public DramaChapterGridAdapter(Context mContext, ArrayList<Chapter> chapters, int current){
		this.mContext = mContext;
		this.chapters = chapters;
		this.current = current;
	}
	
	public int getCount() {
		return chapters.size();
	}

	public Object getItem(int position) {
		return chapters.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.gridview_dramachapter_item, null);
		TextView chapterNo = (TextView)converView.findViewById(R.id.chapter_no);
		ImageView chapterCurrent = (ImageView)converView.findViewById(R.id.mark);
		
		chapterNo.setText(String.valueOf(chapters.get(position).getNumber()));
		if(current == -1) {
			if(chapters.get(position).getNumber() == 1)
				chapterCurrent.setVisibility(View.VISIBLE);
			else
				chapterCurrent.setVisibility(View.GONE);
		} else {
			if(chapters.get(position).getNumber() == current)
				chapterCurrent.setVisibility(View.VISIBLE);
			else
				chapterCurrent.setVisibility(View.GONE);
		}
		
		return converView;
	}

}
