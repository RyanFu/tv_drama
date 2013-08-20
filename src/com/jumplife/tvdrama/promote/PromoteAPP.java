package com.jumplife.tvdrama.promote;

import java.util.ArrayList;
import java.util.Random;

import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.sqlite.SQLiteTvDramaHelper;
import com.jumplife.tvdrama.R;
import com.jumplife.tvdrama.entity.AppProject;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class PromoteAPP {
	private Activity mAcitivty;
	private int promoteId;
	private int probability;
	private ArrayList<AppProject> appProjects = new ArrayList<AppProject>();
	
	private ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options;
		
	public boolean isPromote;
	
	public PromoteAPP(Activity mActivity) {
		this.mAcitivty = mActivity;
		isPromote = false;
		
		SQLiteTvDramaHelper instance = SQLiteTvDramaHelper.getInstance(mActivity);
		SQLiteDatabase db = instance.getReadableDatabase();
		ArrayList<AppProject> tmpAppProjects = instance.getAppProjectList(db);
		db.close();
		instance.closeHelper();
		
		PackageManager pm = mAcitivty.getPackageManager();
	    
		for(int i=0; i<tmpAppProjects.size(); i++) {
			Intent appStartIntent = pm.getLaunchIntentForPackage(tmpAppProjects.get(i).getPack());
			if(appStartIntent == null) {
				appProjects.add(tmpAppProjects.get(i));
				isPromote = true;
			}
		}
		
		SharePreferenceIO shIO = new SharePreferenceIO(mActivity);
		probability = shIO.SharePreferenceO("app_promote_probability", 4);
		
		Random promoteRate = new Random();
		int promote = promoteRate.nextInt(probability);
		if(isPromote && promote == 1)
			isPromote = true;
		else
			isPromote = false;
		
		Random id = new Random();
		if(isPromote)
			promoteId = id.nextInt(appProjects.size());
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
        
        options = new DisplayImageOptions.Builder()
		.showStubImage(R.drawable.stub)
		.showImageForEmptyUri(R.drawable.stub)
		.showImageOnFail(R.drawable.stub)
		.imageScaleType(ImageScaleType.IN_SAMPLE_INT)
		.cacheOnDisc()
		.cacheInMemory()
		.displayer(new SimpleBitmapDisplayer())
		.build();
        
        imageLoader.displayImage(appProjects.get(promoteId).getIconUrl(), imageView, options);
        textviewTitle.setText(appProjects.get(promoteId).getTitle());
        textviewDescription.setText(appProjects.get(promoteId).getDescription());
        
        ((Button)viewPromotion.findViewById(R.id.button1))
        .setOnClickListener(
            new OnClickListener(){
                public void onClick(View v) {
                	mAcitivty.startActivity(new Intent(Intent.ACTION_VIEW, 
				    		Uri.parse("market://details?id=" + appProjects.get(promoteId).getPack())));    			    	
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
