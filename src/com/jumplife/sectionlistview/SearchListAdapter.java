package com.jumplife.sectionlistview;

import java.util.ArrayList;

import com.jumplife.tvdrama.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

public class SearchListAdapter extends BaseAdapter implements Filterable{
	
    Context mContext;
    private ArrayList<String> arr_sort;
    private class ItemView {
		TextView textviewTitle;
	}
    
	public SearchListAdapter(Context mContext, ArrayList<String> arr_sort){
		this.arr_sort = arr_sort;
		this.mContext = mContext;
	}

	public int getCount() {
		
		return arr_sort.size();
	}

	public Object getItem(int position) {

		return arr_sort.get(position);
	}

	public long getItemId(int position) {
	
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ItemView itemView;
		
		if (convertView != null) {
			itemView = (ItemView) convertView.getTag();
		} else {
			LayoutInflater myInflater = LayoutInflater.from(mContext);
			convertView = myInflater.inflate(R.layout.listview_movies, null);
			itemView = new ItemView();
			itemView.textviewTitle = (TextView)convertView.findViewById(R.id.movie_name);
			
			convertView.setTag(itemView);
		}
		itemView.textviewTitle.setText(arr_sort.get(position));
		
		return convertView;
		
		/*LayoutInflater myInflater = LayoutInflater.from(mContext);
		View converView = myInflater.inflate(R.layout.listview_movies, null);
		
		TextView name = (TextView)converView.findViewById(R.id.movie_name);
		name.setText(arr_sort.get(position));
		
		return converView;*/

	}

	public Filter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}
}
