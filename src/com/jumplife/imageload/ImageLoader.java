package com.jumplife.imageload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.jumplife.tvdrama.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader {
    
	private MemoryCache memoryCache=new MemoryCache();
    private FileCache fileCache;
    private int REQUIRED_SIZE=70;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    private ExecutorService executorService;
    private int width;
    private static Bitmap btStub;
    
    public ImageLoader(Context context){
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(3);
        btStub = BitmapFactory.decodeResource(context.getResources(), R.drawable.stub);
    }
    
    public ImageLoader(Context context, int size){
    	fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(3);
        REQUIRED_SIZE = size;
        btStub = BitmapFactory.decodeResource(context.getResources(), R.drawable.stub);
    }
    
    final int stub_id= R.drawable.stub;
    public void DisplayImage(String url, ImageView imageView)
    {
    	imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        
        if(bitmap!=null) {
            imageView.setImageBitmap(bitmap);        	
        } else {
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
    }
    
    public void DisplayImage(String url, ImageView imageView, int width)
    {
        imageViews.put(imageView, url);
        Bitmap bitmap=memoryCache.get(url);
        
        this.width = width;
        btStub = Bitmap.createScaledBitmap(btStub, width, btStub.getHeight() * width / btStub.getWidth(), true);
        
        if(bitmap!=null) {
            imageView.setImageBitmap(bitmap);
        
            //Ben Test
        	/*double height = (double)(width * ((double)bitmap.getHeight() / (double)bitmap.getWidth()));
        
        	imageView.getLayoutParams().height = (int)height;
        	imageView.getLayoutParams().width = width;
        	
        	imageView.setScaleType(ImageView.ScaleType.FIT_XY);*/

        }
        else {
            //queuePhoto(url, imageView);
        	queuePhoto(url, imageView, width);
            imageView.setImageResource(stub_id);
        }
    }
        
    private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
    private void queuePhoto(String url, ImageView imageView, int width)
    {
        PhotoToLoad p=new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
    public Bitmap getBitmap(String url) 
    {
        File f=fileCache.getFile(url);
        
        //from SD cache
        Bitmap bitmap = decodeFile(f);
        if(bitmap!=null)
            return bitmap;
        
        //from web
        try {
        	bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is=conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Exception ex){
           ex.printStackTrace();
           return null;
        }
    }
    
    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f){
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            //final int REQUIRED_SIZE=70;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url=u; 
            imageView=i;
        }
    }
    
    private class FillPhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public int width;
        public FillPhotoToLoad(String u, ImageView i, int width){
            url=u; 
            imageView=i;
            width = width;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        public void run() {
            if(imageViewReused(photoToLoad))
                return;
            Bitmap bitmap = getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bitmap);
            if(imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd=new BitmapDisplayer(bitmap, photoToLoad);
            Activity a=(Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }
    
    class FillPhotosLoader implements Runnable {
    	FillPhotoToLoad photoToLoad;
    	FillPhotosLoader(FillPhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        public void run() {
            if(imageViewReused(photoToLoad))
                return;
            Bitmap bitmap = getBitmap(photoToLoad.url);
            
            double height = (double)(width * ((double)bitmap.getHeight() / (double)bitmap.getWidth()));
            
        	
            
            memoryCache.put(photoToLoad.url, bitmap);
            if(imageViewReused(photoToLoad))
                return;
            FillBitmapDisplayer bd=new FillBitmapDisplayer(bitmap, photoToLoad, width, (int)height);
            Activity a=(Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    
    boolean imageViewReused(FillPhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
        	bitmap = b;
        	photoToLoad = p;
        }
        public void run() {
            if(imageViewReused(photoToLoad))
                return;
            
            /*AnimationSet anim = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(0.2f, 1, 0.2f, 1,
            		Animation.RELATIVE_TO_SELF, 0.5f,
            		Animation.RELATIVE_TO_SELF, 0.5f);
            anim.addAnimation(scaleAnimation);
            anim.setDuration(600);*/
            
            if(bitmap!=null) {
                photoToLoad.imageView.setImageBitmap(bitmap);
                //photoToLoad.imageView.startAnimation(anim);
            } else {
                photoToLoad.imageView.setImageResource(stub_id);
                //photoToLoad.imageView.startAnimation(anim);
            }
        }
    }
    
    class FillBitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        FillPhotoToLoad photoToLoad;
        int width;
        int height;
        public FillBitmapDisplayer(Bitmap b, FillPhotoToLoad p, int width, int height){
        	bitmap = b;
        	photoToLoad = p;
        	this.width = width;
        	this.height = height;
        }
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            
            Log.d("Ben", "width: " + width);
        	Log.d("Ben", "height: " + (int)height);
            
            photoToLoad.imageView.getLayoutParams().width = width + 50;
            photoToLoad.imageView.getLayoutParams().height = height;
            photoToLoad.imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            
      
            /*
             *  imageView.getLayoutParams().height = height;
        imageView.getLayoutParams().width = width;
		
		imageView.setScaleType(ImageView.ScaleType.FIT_XY);
             */
            
            /*AnimationSet anim = new AnimationSet(true);
            ScaleAnimation scaleAnimation = new ScaleAnimation(0.2f, 1, 0.2f, 1,
            		Animation.RELATIVE_TO_SELF, 0.5f,
            		Animation.RELATIVE_TO_SELF, 0.5f);
            anim.addAnimation(scaleAnimation);
            anim.setDuration(600);*/
            
            if(bitmap!=null) {
                photoToLoad.imageView.setImageBitmap(bitmap);
                //photoToLoad.imageView.startAnimation(anim);
            } else {
                photoToLoad.imageView.setImageResource(stub_id);
                //photoToLoad.imageView.startAnimation(anim);
            }
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}
