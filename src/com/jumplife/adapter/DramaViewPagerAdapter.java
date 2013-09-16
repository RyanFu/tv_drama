package com.jumplife.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.jumplife.tvdrama.DramaInfoChapterActivity;
import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.TicketCenterActivity;
import com.jumplife.tvdrama.entity.Drama;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public class DramaViewPagerAdapter extends PagerAdapter{

	private Activity mActivty;
	private List<ArrayList<Drama>> mDramaLists;
	private List<Integer> mRecommendList;
	private Animation animation;
	
	public DramaViewPagerAdapter(Activity activty, List<ArrayList<Drama>> dramaLists, List<Integer> mRecommendList) {
		this.mActivty = activty;
		this.mDramaLists = dramaLists;
		this.mRecommendList = mRecommendList;
	}
	
	@Override
	public int getCount() {
		return mDramaLists.size();
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
        
		animation = AnimationUtils.loadAnimation(mActivty, R.anim.item_movies_anim);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		mActivty.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        
        View view = View.inflate(mActivty, R.layout.viewpage_item, null);
        GridView varietyGridView = (GridView)view.findViewById(R.id.gridview_tvchannel);
		DramaGridAdapter adapter = new DramaGridAdapter(mActivty, mDramaLists.get(pos), mRecommendList,
        		((screenWidth / 2)), (int) (((screenWidth / 2)) * 0.6));
        varietyGridView.setAdapter(adapter);
        itemOnClickListener itemclick = new itemOnClickListener(pos);
        varietyGridView.setOnItemClickListener(itemclick);
        varietyGridView.setOnScrollListener(new PauseOnScrollListener(ImageLoader.getInstance(), true, true));
        
        //((ViewPager)pager).addView(view, pos);
        ((ViewPager)pager).addView(view, ((ViewPager)pager).getChildCount() > pos ? pos : ((ViewPager)pager).getChildCount());

	    return view;
	}
	
	public class itemOnClickListener implements OnItemClickListener {
		private int index = 0;
		public itemOnClickListener(int i) {
			index = i;
		}
		
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			
			view.startAnimation(animation);
			view.postDelayed(createRunnable(position, view), 30);
			/*Intent newAct = new Intent();
			newAct.putExtra("drama_id", mDramaLists.get(index).get(position).getId());
            newAct.putExtra("drama_name", mDramaLists.get(index).get(position).getChineseName());
            newAct.putExtra("drama_poster", mDramaLists.get(index).get(position).getPosterUrl());
            newAct.setClass(mActivty, DramaInfoChapterActivity.class);
            mActivty.startActivity(newAct);*/
			 
		}
		
		private Runnable createRunnable(final int position, final View view){
		    Runnable aRunnable = new Runnable(){
		        public void run(){
		        	if(mDramaLists.get(index).get(position).getId() != -1) {
			        	Intent newAct = new Intent();
						newAct.putExtra("drama_id", mDramaLists.get(index).get(position).getId());
			            newAct.putExtra("drama_name", mDramaLists.get(index).get(position).getChineseName());
			            newAct.putExtra("drama_poster", mDramaLists.get(index).get(position).getPosterUrl());
			            newAct.setClass(mActivty, DramaInfoChapterActivity.class);
			            mActivty.startActivity(newAct);
			            mActivty.overridePendingTransition(android.R.anim.fade_out, android.R.anim.fade_in);
			            //view.clearAnimation();
		        	} else {
						Intent newAct = new Intent();
						newAct.putExtra("advertisement_type", "優惠活動-戲劇列表");
						newAct.setClass(mActivty, TicketCenterActivity.class );
						mActivty. startActivity( newAct );
					}
		        }
		    };
		    return aRunnable;
		}
	};
	
	
}
