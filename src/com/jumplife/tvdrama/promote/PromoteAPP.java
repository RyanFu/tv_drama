package com.jumplife.tvdrama.promote;

import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.jumplife.tvdrama.R;

public class PromoteAPP {
	private int[] image = new int[]{
		R.drawable.movie_64,
		R.drawable.movietime,
		R.drawable.tvdrama,
		R.drawable.tvvariety
	};
	
	private String[] title = {
			"《電影櫃》強力推薦",
			"《電影時刻表》強力推薦",
			"《電視連續劇》強力推薦",
			"《電視綜藝》強力推薦"
	};
	
	private String[] content = {
			"瀏覽最多電影評價 收藏自己的電影歷程",
			"最完整的電影電視時刻 隨時掌握電影資訊",
			"收錄最新最完整台韓日陸連續劇",
			"收錄最新最完整台灣及韓國綜藝節目"
	};
	
	private String[] market = {
			"com.jumplife.moviediary",
			"com.jumplife.movieinfo",
			"com.jumplife.tvdrama",
			"com.jumplife.tvvariety"
	};
	
	private Activity mAcitivty;
	private int promoteId;
	private ArrayList<Integer> imagePromote = new ArrayList<Integer>();
	private ArrayList<String> titlePromote = new ArrayList<String>();
	private ArrayList<String> contentPromote = new ArrayList<String>();
	private ArrayList<String> marketPromote = new ArrayList<String>();
		
	public boolean isPromote;
	
	public PromoteAPP(Activity mActivity) {
		this.mAcitivty = mActivity;
		isPromote = false;
		
		PackageManager pm = mAcitivty.getPackageManager();
	    
		for(int i=0; i<market.length; i++) {
			Intent appStartIntent = pm.getLaunchIntentForPackage(market[i]);
			if(appStartIntent == null) {
				imagePromote.add(image[i]);
				titlePromote.add(title[i]);
				contentPromote.add(content[i]);
				marketPromote.add(market[i]);
				isPromote = true;
			}
		}
		
		Random id = new Random();
		if(isPromote)
			promoteId = id.nextInt(marketPromote.size());
	}
	
	public void promoteAPPExe() {
    	final AlertDialog dialogPromotion;
    	View viewPromotion;
    	
        LayoutInflater factory = LayoutInflater.from(mAcitivty);
        viewPromotion = factory.inflate(R.layout.dialog_promotion_app,null);
        dialogPromotion = new AlertDialog.Builder(mAcitivty).create();
        dialogPromotion.setView(viewPromotion);
        ImageView imageView = (ImageView)viewPromotion.findViewById(R.id.imageView1);
        TextView textviewTitle = (TextView)viewPromotion.findViewById(R.id.textView1);
        TextView textviewDescription = (TextView)viewPromotion.findViewById(R.id.textView2);
        
        imageView.setBackgroundResource(imagePromote.get(promoteId));
        textviewTitle.setText(titlePromote.get(promoteId));
        textviewDescription.setText(contentPromote.get(promoteId));
        
        ((Button)viewPromotion.findViewById(R.id.button1))
        .setOnClickListener(
            new OnClickListener(){
                public void onClick(View v) {
                	mAcitivty.startActivity(new Intent(Intent.ACTION_VIEW, 
				    		Uri.parse("market://details?id=" + marketPromote.get(promoteId))));    			    	
                }
            }
        );
        ((Button)viewPromotion.findViewById(R.id.button2))
        .setOnClickListener(
            new OnClickListener(){
                public void onClick(View v) {
                	mAcitivty.finish();
                }
            }
        );
        dialogPromotion.setCanceledOnTouchOutside(false);
        dialogPromotion.show();
    }
}
