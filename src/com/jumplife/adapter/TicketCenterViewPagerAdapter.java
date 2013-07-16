package com.jumplife.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.animation.ViewExpandAnimation;
import com.jumplife.tvdrama.entity.Ticket;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public class TicketCenterViewPagerAdapter extends PagerAdapter{

	private Activity mActivty;
	private ArrayList<Ticket> preferentialLists;
	private ArrayList<Ticket> myTicketLists;
	
	public TicketCenterViewPagerAdapter(Activity activty, ArrayList<Ticket> preferentialLists, ArrayList<Ticket> myTicketLists) {
		this.mActivty = activty;
		this.preferentialLists = preferentialLists;
		this.myTicketLists = myTicketLists;
	}
	
	@Override
	public int getCount() {
		return 2;
	}
	
	@Override
	public void destroyItem(View pager, int position, Object view) {
		((ViewPager) pager).removeView(((ViewPager)pager).getChildAt(position));
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == (arg1);
	}	
	
	@Override
	public int getItemPosition(Object object) {
	    return POSITION_NONE;
	}
	
	@Override
	public Object instantiateItem(View pager, int pos) {
        
		
        View view = View.inflate(mActivty, R.layout.viewpage_ticket_center_item, null);
        ListView lvTicketCenter = (ListView)view.findViewById(R.id.lv_ticket_center);
        switch(pos) {
	        case 0 :
	        	PreferentialAdapter preferentialAdapter = new PreferentialAdapter(mActivty, preferentialLists);
	        	if(preferentialAdapter != null) {
	        		lvTicketCenter.setAdapter(preferentialAdapter);
	        		lvTicketCenter.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
	            }
	        	break;
	        case 1 :
	        	MyTicketAdapter myTicketAdapter = new MyTicketAdapter(mActivty, myTicketLists);
	        	if(myTicketAdapter != null) {
	        		lvTicketCenter.setAdapter(myTicketAdapter);
	        		lvTicketCenter.setOnItemClickListener(new OnItemClickListener(){
						@Override
						public void onItemClick(AdapterView<?> arg0, View v, int pos, long arg3) {
							MyTicketAdapter.itemStatusChanged(pos);
							View expandView = v.findViewById(R.id.ll_myticket_serialnum);  
							expandView.startAnimation(new ViewExpandAnimation(expandView));  
						}	        			
	        		});
	        		lvTicketCenter.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
	            }
	        	break;
		}
        
        
        //((ViewPager)pager).addView(view, pos);
        ((ViewPager)pager).addView(view, ((ViewPager)pager).getChildCount() > pos ? pos : ((ViewPager)pager).getChildCount());

	    return view;
	}	
	
}
