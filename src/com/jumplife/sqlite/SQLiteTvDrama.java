package com.jumplife.sqlite;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;

import android.app.Activity;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.jumplife.sharedpreferenceio.SharePreferenceIO;
import com.jumplife.tvdrama.entity.Drama;

public class SQLiteTvDrama extends SQLiteOpenHelper {
    private static final String   DramaTable          = "dramas";
    private static final String   DB_PATH             = "/data/com.jumplife.tvdrama/databases/";
    private static final String   DB_NAME             = "dramas.sqlite";                            // 資料庫名稱
    private static final int      DATABASE_VERSION    = 5;                                          // 資料庫版本
    private static SQLiteDatabase db;
    private static String DB_PATH_DATA;

    public SQLiteTvDrama(Activity mActivity) {
    	super(mActivity, "dramas", null, DATABASE_VERSION);
    	
    	DB_PATH_DATA = Environment.getDataDirectory() + DB_PATH;
        if (mActivity != null) {
            SharePreferenceIO shareIO = new SharePreferenceIO(mActivity);
            if (shareIO != null) {
            	int checkVersion = DATABASE_VERSION;
            	if(shareIO.SharePreferenceO("checkversion", 0) < checkVersion) {
            		Log.d(DramaTable, "delete database");
                    shareIO.SharePreferenceI("checkversion", checkVersion);
            		shareIO.SharePreferenceI("checkfile", true);
            		mActivity.deleteDatabase(DB_NAME);
            	}
                boolean checkFile = shareIO.SharePreferenceO("checkfile", true);
                if (checkFile) {
                	Log.d(DramaTable, "check file");
                    checkFileSystem(mActivity);
                    shareIO.SharePreferenceI("checkfile", false);
                }
            }
        }

        if (!checkDataBase())
            db = this.getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        // TODO Auto-generated method stub
    	createDrama(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS " + DramaTable);
        Log.d("SQLiteTvDrama", "Delete old Database");
        onCreate(db);
    }

    public SQLiteDatabase GetDB() {
        openDataBase();
        return db;
    }

    public static void openDataBase() {
        if (db == null || !db.isOpen())
            db = SQLiteDatabase.openOrCreateDatabase(DB_PATH_DATA + DB_NAME, null);
    }

    public static void closeDB() {
        if (db != null)
            db.close();
    }

    private boolean checkDataBase() {
        File dbtest = new File(DB_PATH_DATA + DB_NAME);
        if (dbtest.exists())
            return true;
        else
            return false;
    }

    private void checkFileSystem(Activity mActivity) {
        // if((new File(DB_PATH + DB_NAME)).exists() == false) {
        // 如 SQLite 数据库文件不存在，再检查一下 database 目录是否存在
        File f = new File(DB_PATH_DATA);
        // 如 database 目录不存在，新建该目录
        if (!f.exists()) {
            f.mkdir();
        }

        try {
            // 得到 assets 目录下我们实现准备好的 SQLite 数据库作为输入流
            InputStream is = mActivity.getBaseContext().getAssets().open(DB_NAME);
            // 输出流
            OutputStream os = new FileOutputStream(DB_PATH_DATA + DB_NAME);

            // 文件写入
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }

            // 关闭文件流
            os.flush();
            os.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // }
    }

    
    /*
     * drama data
     */
    public static void createDrama(SQLiteDatabase database) {
        String DATABASE_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + DramaTable + " (" 
        		+ " id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE,"
                + " name VARCHAR,"
        		+ " poster_url VARCHAR,"
                + " introduction VARCHAR,"
                + " eps_num_str VARCHAR,"
                + " area_id INTEGER,"
                + " release_date VARCHAR,"
                + " chapter INTEGER,"
                + " section VARCHAR"
                + " is_show BOOLEAN"
                + " views INTEGER"
                + " );";

        database.execSQL(DATABASE_CREATE_TABLE);
    }

    public boolean insertDramas(ArrayList<Drama> dramas) {
        for (int i = 0; i < dramas.size(); i++) {
            Drama drama = dramas.get(i);
            if(drama != null)
            	insertDrama(drama);
        }
        return true;
    }

    public static long insertDrama(Drama drama) {
        openDataBase();
        if(!drama.getChineseName().equals("") && !drama.getIntroduction().equals("") && 
        		!drama.getPosterUrl().equals("") && !drama.getEps().equals("") && !drama.getReleaseDate().equals("")) {
	        Cursor cursor = db.rawQuery(
	                "INSERT OR IGNORE INTO " + DramaTable + " VALUES(?,?,?,?,?,?,?,?,?,?,?)",
	                new String[] {0+"", -1+"", -1+"", drama.getId()+"", drama.getChineseName(), drama.getAreId()+"",
	                		drama.getIntroduction(), drama.getPosterUrl(), drama.getEps(),	drama.getReleaseDate(), "'f'"});
			
	        cursor.moveToFirst();
	        cursor.close();
        }
        return 0;
    }

    public int deleteDrama(long rowId) {
        openDataBase();
        int deleteId = db.delete(DramaTable, "id = " + rowId, null);
        return deleteId;
    }

    public static void deleteDrama_lst() {
        openDataBase();
        db.execSQL("DROP TABLE IF EXISTS " + DramaTable);
    }

    public ArrayList<Integer> findDramasIdNotInDB(ArrayList<Integer> dramaId) {
        ArrayList<Integer> returnsID = new ArrayList<Integer>();
        ArrayList<Integer> dbsID = new ArrayList<Integer>();
        openDataBase();
        Cursor cursor = db.rawQuery("SELECT id FROM " + DramaTable, null);
        if (cursor.getCount() > 0) {
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
    
    public void updateDramaIsShow(ArrayList<Integer> a) {
    	String idLst = "";
        for (int i = 0; i < a.size(); i++)
            idLst = a.get(i) + "," + idLst;
        idLst = idLst.substring(0, idLst.length() - 1);
        
    	openDataBase();
        Cursor cursor = db.rawQuery("UPDATE " + DramaTable + " SET 'is_show' = CASE WHEN id IN (" + idLst + ")  THEN 't' ELSE 'f' END;", null);
        cursor.moveToFirst();
        cursor.close();
    }
    
    public void updateDramaViews(ArrayList<Drama> dramas) {
    	String updateViews = "UPDATE " + DramaTable + " SET views = CASE";
        for(int i=0; i<dramas.size(); i++)
        	updateViews = updateViews + " WHEN id = " + dramas.get(i).getId() + " THEN " + dramas.get(i).getViews() + " ";
        updateViews = updateViews + "END ;";
        	
    	openDataBase();
    	Cursor cursor = db.rawQuery(updateViews, null);
    	cursor.moveToFirst();
        cursor.close();
    }
    
    public void updateDramaEps(ArrayList<Drama> dramas) {
    	String updateViews = "UPDATE " + DramaTable + " SET eps_num_str = CASE";
        for(int i=0; i<dramas.size(); i++)
        	updateViews = updateViews + " WHEN id = " + dramas.get(i).getId() + " THEN '" + dramas.get(i).getEps() + "' ";
        updateViews = updateViews + "END ;";
        	
    	openDataBase();
    	Cursor cursor = db.rawQuery(updateViews, null);
    	cursor.moveToFirst();
        cursor.close();
    }
    
    public Drama getDrama(int dramaId) throws SQLException {
        openDataBase();
        Cursor cursor = db.rawQuery("SELECT id, name, introduction, poster_url FROM " + DramaTable + " WHERE id = \'" + dramaId + "\' AND is_show = 't';", null);
        Drama drama = new Drama();
        while (cursor.moveToNext()) {
        	drama.setId(cursor.getInt(0));
        	drama.setChineseName(cursor.getString(1));
        	drama.setIntroduction(cursor.getString(2));
        	drama.setPosterUrl(cursor.getString(3));
        }
        cursor.close();
        return drama;
    }
    
    public int getDramaChapterRecord(int dramaId) throws SQLException {
        openDataBase();
        Cursor cursor = db.rawQuery("SELECT chapter FROM " + DramaTable + " WHERE id = " + dramaId, null);
        int chapter = -1;
        while (cursor.moveToNext()) {
        	chapter = cursor.getInt(0);
        }
        cursor.close();
        return chapter;
    }
    
    public String getDramaChapter(int dramaId) throws SQLException {
        openDataBase();
        Cursor cursor = db.rawQuery("SELECT eps_num_str FROM " + DramaTable + " WHERE id = " + dramaId, null);
        String chapters = "";
        while (cursor.moveToNext()) {
        	chapters = cursor.getString(0);
        }
        cursor.close();
        return chapters;
    }
    
    public boolean updateDramaChapterRecord(int dramaId, int current_chapter) {
        openDataBase();
        
        Cursor cursor = db.rawQuery("UPDATE " + DramaTable + " SET chapter = ? WHERE id = ?", 
        									new String[] {current_chapter + "", dramaId + ""});
        
        cursor.moveToFirst();
        cursor.close();
        return true;
    }
    
    public String getDramaSectionRecord(int dramaId) throws SQLException {
        openDataBase();
        Cursor cursor = db.rawQuery("SELECT section FROM " + DramaTable + " WHERE id = " + dramaId, null);
        String section = "";
        while (cursor.moveToNext()) {
        	section = cursor.getString(0);
        }
        cursor.close();
        return section;
    }
    
    public boolean updateDramaSectionRecord(int dramaId, String current_section) {
        openDataBase();
        
        Cursor cursor = db.rawQuery("UPDATE " + DramaTable + " SET section = ? WHERE id = ?", 
        									new String[] {current_section + "", dramaId + ""});
        
        cursor.moveToFirst();
        cursor.close();
        return true;
    }

    public ArrayList<Drama> getDramaList() throws SQLException {
        openDataBase();
        ArrayList<Drama> drama_lst = new ArrayList<Drama>();
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT id, name, poster_url, views FROM " + DramaTable + " WHERE is_show = 't';", null);
        while (cursor.moveToNext()) {
            Drama drama = new Drama();
            drama.setId(cursor.getInt(0));
            drama.setChineseName(cursor.getString(1));
            drama.setPosterUrl(cursor.getString(2));
            drama.setViews(cursor.getInt(3));
            drama_lst.add(drama);
        }

        cursor.close();
        return drama_lst;
    }
    
    public ArrayList<Drama> getDramaList(ArrayList<Integer> dramaIds) throws SQLException {
        openDataBase();
        String idLst = "";
        for (int i = 0; i < dramaIds.size(); i++)
            idLst = dramaIds.get(i) + "," + idLst;
        idLst = idLst.substring(0, idLst.length() - 1);

        ArrayList<Drama> drama_lst = new ArrayList<Drama>();
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT id, name, poster_url, views FROM " + DramaTable + " WHERE id in (" + idLst + ") AND is_show = 't';", null);
        while (cursor.moveToNext()) {
            Drama drama = new Drama();
            drama.setId(cursor.getInt(0));
            drama.setChineseName(cursor.getString(1));
            drama.setPosterUrl(cursor.getString(2));
            drama.setViews(cursor.getInt(3));
            drama_lst.add(drama);
        }

        cursor.close();
        return drama_lst;
    }

    public ArrayList<Drama> getDramaList(int filter) throws SQLException {
        openDataBase();
        ArrayList<Drama> drama_lst = new ArrayList<Drama>();
        Cursor cursor = null;
        cursor = db.rawQuery("SELECT id, name, poster_url, views FROM " + DramaTable + " WHERE area_id = " + filter + " AND is_show = 't';", null);
        while (cursor.moveToNext()) {
            Drama drama = new Drama();
            drama.setId(cursor.getInt(0));
            drama.setChineseName(cursor.getString(1));
            drama.setPosterUrl(cursor.getString(2));
            drama.setViews(cursor.getInt(3));
            drama_lst.add(drama);
        }

        cursor.close();
        closeDB();
        return drama_lst;
    }
    
    public ArrayList<Drama> getDramaList(String dramaID) {
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
        openDataBase();
        cursor = db.rawQuery("SELECT id, name, poster_url, views FROM " + DramaTable + " WHERE id in (" + dramaIDs + ") AND is_show = 't';", null);
        while (cursor.moveToNext()) {
            Drama drama = new Drama();
            drama.setId(cursor.getInt(0));
            drama.setChineseName(cursor.getString(1));
            drama.setPosterUrl(cursor.getString(2));
            drama.setViews(cursor.getInt(3));
            drama_lst.add(drama);
        }

        cursor.close();
        closeDB();
        return drama_lst;
    }
}