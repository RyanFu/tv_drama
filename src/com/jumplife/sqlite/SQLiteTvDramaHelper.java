package com.jumplife.sqlite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.tvdrama.entity.Drama;

public class SQLiteTvDramaHelper extends SQLiteOpenHelper {
    private static final String   DramaTable          = "dramas";
    public  static final String   DB_PATH             = "/data/com.jumplife.tvdrama/databases/";
    public  static final String   DB_NAME             = "dramas.sqlite";                            // 資料庫名稱
    private static final int      DATABASE_VERSION    = 44;
    private final  Context 		  mContext;
    public  static String 		  DB_PATH_DATA;                                         // 資料庫版本
    private static SQLiteTvDramaHelper helper;

    public static synchronized SQLiteTvDramaHelper getInstance(Context context) {
        if(helper == null) {
            helper = new SQLiteTvDramaHelper(context);
        }

        return helper;
    }
    
    public SQLiteTvDramaHelper(Context context) {
    	super(context, DB_NAME, null, DATABASE_VERSION);
    	this.mContext = context;
    	//DB_PATH_DATA = Environment.getDataDirectory() + DB_PATH;
    	DB_PATH_DATA = context.getFilesDir().getAbsolutePath().replace("files", "databases") + "/";
    	Log.d(null, "data path : " + DB_PATH_DATA);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.d(null, "onCreate");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(null, "onUpgrade");
    }

    public void closeHelper() {
        if(helper != null)
        	helper.close();
    }

    public void createDataBase() {

    	SharePreferenceIO shareIO = new SharePreferenceIO(mContext);
        if (shareIO != null) {
        	int checkVersion = DATABASE_VERSION;
        	if(shareIO.SharePreferenceO("checkversion", 0) < checkVersion) {
		        File dbf = new File(DB_PATH_DATA + DB_NAME);
				if(dbf.exists()){
				    dbf.delete();
				    shareIO.SharePreferenceI("checkversion", checkVersion);
					Log.d(null, "delete dbf");
				}
        	}
        }
		
    	boolean dbExist = checkDataBase();
        if(dbExist){
            Log.d(null, "db exist");
        }else{
            File dir = new File(DB_PATH_DATA);
			if(!dir.exists()){
			    dir.mkdirs();
			}
			Log.d(null, "copy database");
			copyDataBase();			
        }
    }
    
    private boolean checkDataBase(){
    	File dbFile = new File(DB_PATH_DATA + DB_NAME);
        return dbFile.exists();
    }
    
    /**
     * Copies your database from your local assets-folder to the just created empty database in the
     * system folder, from where it can be accessed and handled.
     * This is done by transfering bytestream.
     * */
    private void copyDataBase() {
    	try {
            InputStream is = mContext.getAssets().open(DB_NAME);
            OutputStream os = new FileOutputStream(DB_PATH_DATA + DB_NAME);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            os.flush();
            os.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(null, "copy data base failed");
        }
    }

    public boolean insertDramas(SQLiteDatabase db, ArrayList<Drama> dramas) {
        for (int i = 0; i < dramas.size(); i++) {
            Drama drama = dramas.get(i);
            if(drama != null)
            	insertDrama(db, drama);
        }
        return true;
    }

    public long insertDrama(SQLiteDatabase db, Drama drama) {
        if(!drama.getChineseName().equals("") && !drama.getIntroduction().equals("") && 
        		!drama.getPosterUrl().equals("") && !drama.getEps().equals("") && !drama.getReleaseDate().equals("")) {
        	db.execSQL(
	                "INSERT OR IGNORE INTO " + DramaTable + " VALUES(?,?,?,?,?,?,?,?,?,?,?)",
	                new String[] {0+"", -1+"", -1+"", drama.getId()+"", drama.getChineseName(), drama.getAreId()+"",
	                		drama.getIntroduction(), drama.getPosterUrl(), drama.getEps(),	drama.getReleaseDate(), "'f'"});
        }
        return 0;
    }

    /*public int deleteDrama(long rowId) {
    	final SQLiteDatabase db = getWritableDatabase();
        int deleteId = db.delete(DramaTable, "id = " + rowId, null);
        return deleteId;
    }

    public void deleteDrama_lst() {
    	final SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DramaTable);
    }*/

    public ArrayList<Integer> findDramasIdNotInDB(SQLiteDatabase db, ArrayList<Integer> dramaId) {
        ArrayList<Integer> returnsID = new ArrayList<Integer>();
        ArrayList<Integer> dbsID = new ArrayList<Integer>();
        
        Cursor cursor = db.rawQuery("SELECT id FROM " + DramaTable, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                dbsID.add(cursor.getInt(0));
            }
        }
        cursor.close();

        HashSet<Integer> hashSet = new HashSet<Integer>(dbsID);
        for (Integer id : dramaId) {
            if (!hashSet.contains(id))
                returnsID.add(id);
        }
        return returnsID;
    }
    
    public void updateDramaIsShow(SQLiteDatabase db, ArrayList<Integer> a) {
    	String idLst = "";
        for (int i = 0; i < a.size(); i++)
            idLst = a.get(i) + "," + idLst;
        idLst = idLst.substring(0, idLst.length() - 1);
        
        if(a.size() > 0) {
        	db.execSQL("UPDATE " + DramaTable + " SET 'is_show' = CASE WHEN id IN (" + idLst + ")  THEN 't' ELSE 'f' END;");
        }
    }
    
    public void updateDramaViews(SQLiteDatabase db, ArrayList<Drama> dramas) {
    	String updateViews = "UPDATE " + DramaTable + " SET views = CASE";
        for(int i=0; i<dramas.size(); i++)
        	updateViews = updateViews + " WHEN id = " + dramas.get(i).getId() + " THEN " + dramas.get(i).getViews() + " ";
        updateViews = updateViews + "END ;";
        	
        db.execSQL(updateViews);
    }
    
    public void updateDramaEps(SQLiteDatabase db, ArrayList<Drama> dramas) {
    	StringBuilder updateViews = new StringBuilder();
    	updateViews.append("UPDATE ").append(DramaTable).append(" SET eps_num_str = CASE");
        for(int i=0; i<dramas.size(); i++)
        	updateViews.append(" WHEN id = ").append(dramas.get(i).getId()).append(" THEN '").append(dramas.get(i).getEps()).append("' ");
        updateViews.append("END ;");
        	
        db.execSQL(updateViews.toString());
    }
    
    public void updateDramaEps(SQLiteDatabase db, int dramaId, String eps) {
    	db.execSQL("UPDATE " + DramaTable + " SET eps_num_str = ? WHERE id = ?", 
        									new String[] {eps + "", dramaId + ""});
    }
    
    public Drama getDrama(SQLiteDatabase db, int dramaId) throws SQLException {
    	Cursor cursor = db.rawQuery("SELECT id, name, introduction, poster_url FROM " + DramaTable + " WHERE id = '" + dramaId + "' AND is_show = 't';", null);
        Drama drama = new Drama();
        if (cursor != null) {
	        while (cursor.moveToNext()) {
	        	drama.setId(cursor.getInt(0));
	        	drama.setChineseName(cursor.getString(1));
	        	drama.setIntroduction(cursor.getString(2));
	        	drama.setPosterUrl(cursor.getString(3));
	        }
	        cursor.close();
        }
        return drama;
    }
    
    public int getDramaChapterRecord(SQLiteDatabase db, int dramaId) throws SQLException {
    	Cursor cursor = db.rawQuery("SELECT chapter FROM " + DramaTable + " WHERE id = '" + dramaId + "'", null);
        int chapter = -1;
        if (cursor != null) {
        	while(cursor.moveToNext()) {
	            chapter = cursor.getInt(0);
        	}
            cursor.close();
        }
        return chapter;
    }
    
    public String getDramaChapter(SQLiteDatabase db, int dramaId) throws SQLException {
    	Cursor cursor = db.rawQuery("SELECT eps_num_str FROM " + DramaTable + " WHERE id = '" + dramaId + "'", null);
        String chapters = "";
        if (cursor != null) {
        	while(cursor.moveToNext()) {
	            chapters = cursor.getString(0);
        	}
            cursor.close();
        }
        return chapters;
    }
    
    public boolean updateDramaChapterRecord(SQLiteDatabase db, int dramaId, int current_chapter) {
        
    	String[] arrayOfString = new String[1];
        
        arrayOfString[0] = String.valueOf(dramaId);
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("chapter", current_chapter);
        db.update(DramaTable, localContentValues, "id = ?", arrayOfString);
        
        return true;
    }
    
    public String getDramaSectionRecord(SQLiteDatabase db, int dramaId) throws SQLException {
    	
    	Cursor cursor = db.rawQuery("SELECT section FROM " + DramaTable + " WHERE id = '" + dramaId + "'", null);
        
        String section = "";
        if (cursor != null) {
        	while(cursor.moveToNext()) {
	            section = cursor.getString(0);
        	}
            cursor.close();
        }
        return section;
    }
    
    public boolean updateDramaSectionRecord(SQLiteDatabase db, int dramaId, String current_section) {
    	String[] arrayOfString = new String[1];
        
        arrayOfString[0] = String.valueOf(dramaId);
        ContentValues localContentValues = new ContentValues();
        localContentValues.put("section", current_section);
        db.update(DramaTable, localContentValues, "id = ?", arrayOfString);
        
        return true;
    }

    public ArrayList<Drama> getDramaList(SQLiteDatabase db) throws SQLException {
    	ArrayList<Drama> drama_lst = new ArrayList<Drama>();
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT id, name, poster_url, views FROM " + DramaTable + " WHERE is_show = 't';", null);
        if (cursor != null) {
        	while(cursor.moveToNext()) {
	            Drama drama = new Drama();
	            drama.setId(cursor.getInt(0));
	            drama.setChineseName(cursor.getString(1));
	            drama.setPosterUrl(cursor.getString(2));
	            drama.setViews(cursor.getInt(3));
	            drama_lst.add(drama);
        	}
            cursor.close();
        }
        return drama_lst;
    }
    
    public ArrayList<Drama> getDramaList(ArrayList<Integer> dramaIds) throws SQLException {
    	final SQLiteDatabase db = getReadableDatabase();
        String idLst = "";
        for (int i = 0; i < dramaIds.size(); i++)
            idLst = dramaIds.get(i) + "," + idLst;
        idLst = idLst.substring(0, idLst.length() - 1);

        ArrayList<Drama> drama_lst = new ArrayList<Drama>();
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT id, name, poster_url, views FROM " + DramaTable + " WHERE id in (" + idLst + ") AND is_show = 't';", null);
        if (cursor != null) {
        	while(cursor.moveToNext()) {
	            Drama drama = new Drama();
	            drama.setId(cursor.getInt(0));
	            drama.setChineseName(cursor.getString(1));
	            drama.setPosterUrl(cursor.getString(2));
	            drama.setViews(cursor.getInt(3));
	            drama_lst.add(drama);
        	}
            cursor.close();
        }
        return drama_lst;
    }

    public ArrayList<Drama> getDramaList(SQLiteDatabase db, int filter) throws SQLException {
    	ArrayList<Drama> drama_lst = new ArrayList<Drama>();
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT id, name, poster_url, views, release_date FROM " + DramaTable + " WHERE area_id = " + filter + " AND is_show = 't';", null);
        if (cursor != null) {
        	while(cursor.moveToNext()) {
	            Drama drama = new Drama();
	            drama.setId(cursor.getInt(0));
	            drama.setChineseName(cursor.getString(1));
	            drama.setPosterUrl(cursor.getString(2));
	            drama.setViews(cursor.getInt(3));
	            drama.setReleaseDate(cursor.getString(4));
	            drama_lst.add(drama);
        	}
            cursor.close();
        }

        return drama_lst;
    }
    
    public ArrayList<Drama> getDramaList(SQLiteDatabase db, String dramaID) {
    	String[] likeDramas = dramaID.split(",");
        String dramaIDs = "";
        for (int i = 0; i < likeDramas.length; i++) {
            if (!likeDramas[i].equals("")) {
            	dramaIDs = dramaIDs + likeDramas[i];
                if (i < likeDramas.length - 1)
                	dramaIDs = dramaIDs + ",";
            }
        }
        ArrayList<Drama> drama_lst = new ArrayList<Drama>();
        Cursor cursor = null;
    	cursor = db.rawQuery("SELECT id, name, poster_url, views FROM " + DramaTable + " WHERE id in (" + dramaIDs + ") AND is_show = 't';", null);
        if (cursor != null) {
        	while(cursor.moveToNext()) {
	            Drama drama = new Drama();
	            drama.setId(cursor.getInt(0));
	            drama.setChineseName(cursor.getString(1));
	            drama.setPosterUrl(cursor.getString(2));
	            drama.setViews(cursor.getInt(3));
	            drama_lst.add(drama);
        	}
            cursor.close();
        }
        return drama_lst;
    }
}