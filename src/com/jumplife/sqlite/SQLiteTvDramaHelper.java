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
    //private static final String   TicketTable         = "tickets";
    public  static final String   DB_PATH             = "/data/com.jumplife.tvdrama/databases/";
    public  static final String   DB_NAME             = "dramas.sqlite";                            // 資料庫名稱
    private static final int      DATABASE_VERSION    = 48; // Version 44 is the new sqlite helper
    private final  Context 		  mContext;
    public  static String 		  DB_PATH_DATA;                                         // 資料庫版本
    private static SQLiteTvDramaHelper helper;

    public static synchronized SQLiteTvDramaHelper getInstance(Context context) {
        if(helper == null) {
            helper = new SQLiteTvDramaHelper(context.getApplicationContext());
        }

        return helper;
    }
    
    public SQLiteTvDramaHelper(Context context) {
    	super(context.getApplicationContext(), DB_NAME, null, DATABASE_VERSION);
    	this.mContext = context.getApplicationContext();
    	//DB_PATH_DATA = Environment.getDataDirectory() + DB_PATH;
    	DB_PATH_DATA = mContext.getFilesDir().getAbsolutePath().replace("files", "databases") + "/";
    	Log.d(null, "data path : " + DB_PATH_DATA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(null, "onCreate");
        /*String DATABASE_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TicketTable + " ("
        	+ "url VARCHAR,"
	   		+ "title VARCHAR,"
	   		+ "description VARCHAR,"
	   		+ "serial_num INTEGER,"
	   		+ "has_watch BOOLEAN"
	   		+ ")";
        db.execSQL(DATABASE_CREATE_TABLE);*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(null, "onUpgrade");
        /*db.execSQL("DROP TABLE IF EXISTS " + TicketTable);
        onCreate(db);*/
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
	                		drama.getIntroduction(), drama.getPosterUrl(), drama.getEps(),	drama.getReleaseDate(), "f"});
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
    	
    	/*Cursor cursor = db.rawQuery("SELECT section FROM " + DramaTable + " WHERE id = '" + dramaId + "'", null);
        
        String section = "";
        if (cursor != null) {
        	while(cursor.moveToNext()) {
	            section = cursor.getString(0);
        	}
            cursor.close();
        }*/

    	Cursor cursor;
    	String section = "";
        
    	try {
    		cursor = db.rawQuery("SELECT section FROM " + DramaTable + " WHERE id = '" + dramaId + "'", null);
            
            if (cursor != null) {
            	while(cursor.moveToNext()) {
    	            section = cursor.getString(0);
            	}
                cursor.close();
            }
    	} catch (Exception e) {
    	     return section;
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
    	try {
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
	    } catch (Exception e) {
		     return drama_lst;
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
        try {
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
	    } catch (Exception e) {
		     return drama_lst;
		}
        return drama_lst;
    }

    public ArrayList<Drama> getDramaList(SQLiteDatabase db, int filter) throws SQLException {
    	ArrayList<Drama> drama_lst = new ArrayList<Drama>();
    	try {
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
	    } catch (Exception e) {
		     return drama_lst;
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
        try {
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
        } catch (Exception e) {
		     return drama_lst;
		}
        return drama_lst;
    }
    
    /*public void insertTicket(SQLiteDatabase db, Ticket ticket) {   		
    	db.execSQL(
                "INSERT OR IGNORE INTO " + TicketTable + " VALUES(?,?,?,?,?)",
                new String[] {ticket.getUrl(), ticket.getTitle(), ticket.getDescription(), ticket.getSerialNum()+"", "f"});
    }
    
    public void updateHasWatch(SQLiteDatabase db) {
        
    	ContentValues localContentValues = new ContentValues();
        localContentValues.put("has_watch", "t");
        db.update(TicketTable, localContentValues, null, null);
    }

    public int getUnReadCount(SQLiteDatabase db) {
    	Cursor cursor = db.rawQuery("SELECT url FROM " + TicketTable + " WHERE has_watch = 'f'", null);
        int count = 0;
        if (cursor != null) {
        	if(cursor.moveToFirst())
        		count = cursor.getCount();
            cursor.close();
        }
        return count;
    }
    
    public ArrayList<Ticket> getTicketList(SQLiteDatabase db) throws SQLException {
    	ArrayList<Ticket> ticketList = new ArrayList<Ticket>();
    	try {
	        Cursor cursor = null;
	        cursor = db.rawQuery("SELECT url, title, description, serial_num FROM " + TicketTable, null);
	        if (cursor != null) {
	        	while(cursor.moveToNext()) {
	        		Ticket ticket = new Ticket();
	        		ticket.setUrl(cursor.getString(0));
	        		ticket.setTitle(cursor.getString(1));
	        		ticket.setDescription(cursor.getString(2));
	        		ticket.setSerialNum(cursor.getInt(3));
		            ticketList.add(ticket);
	        	}
	            cursor.close();
	        }
	    } catch (Exception e) {
		     return ticketList;
		}
        return ticketList;
    }*/
}