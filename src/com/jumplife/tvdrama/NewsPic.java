package com.jumplife.tvdrama;

import com.google.analytics.tracking.android.TrackedActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebView;
import android.widget.TextView;

public class NewsPic extends TrackedActivity {
	
	//private ImageView imageviewNewsPic;
	private TextView textviewContent;
	private WebView webviewPic;
	//private ImageLoader imageLoader;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_news_pic);
		
		Bundle extras = getIntent().getExtras();
		String pictureUrl = extras.getString("picture_url");
		String content = extras.getString("content");
		
		//imageviewNewsPic = (ImageView) findViewById(R.id.imageview_newspic);
		textviewContent = (TextView) findViewById(R.id.textview_content);
		webviewPic = (WebView) findViewById(R.id.webview_pic);
		
		webviewPic.loadUrl(pictureUrl);
		
		DisplayMetrics displayMetrics = new DisplayMetrics();
		
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		
		webviewPic.getSettings().setSupportZoom(true);
		webviewPic.getSettings().setBuiltInZoomControls(true);
		webviewPic.setInitialScale(150);
		webviewPic.getSettings().setDefaultZoom(ZoomDensity.FAR);

		textviewContent.setText(content);

	}
	@Override
	protected void onStart() {
	    super.onStart();
	}
	@Override
	protected void onStop() {
	    super.onStop();
    }
	@Override
	protected void onResume(){
        super.onResume();
   }

}
