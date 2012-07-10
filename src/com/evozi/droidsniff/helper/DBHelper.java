package com.evozi.droidsniff.helper;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBHelper {

	private static SQLiteDatabase droidsniffDB = null;
	public static final String DROIDSNIFF_DBNAME = "droidsniff";

	public static final String CREATE_PREFERENCES = "CREATE TABLE IF NOT EXISTS DROIDSNIFF_PREFERENCES "
			+ "(id integer primary key autoincrement, " + "name  varchar(100)," + "value varchar(100));";

	public static final String CREATE_BLACKLIST = "CREATE TABLE IF NOT EXISTS DROIDSNIFF_BLACKLIST "
			+ "(id integer primary key autoincrement, " + "domain varchar(100));";

	public static void initDB(Context c) {
		DBHelper.droidsniffDB = c.openOrCreateDatabase(DROIDSNIFF_DBNAME, Context.MODE_PRIVATE, null);
		droidsniffDB.execSQL(CREATE_PREFERENCES);
		droidsniffDB.execSQL(CREATE_BLACKLIST);
	}

	public static boolean getGeneric(Context c) {
		initDB(c);
		Cursor cur = droidsniffDB.rawQuery("SELECT * FROM DROIDSNIFF_PREFERENCES WHERE name = 'generic';", new String[] {});
		if (cur.moveToNext()) {
			String s = cur.getString(cur.getColumnIndex("value"));
			cur.close();
			droidsniffDB.close();
			return Boolean.parseBoolean(s);
		} else {
			cur.close();
			droidsniffDB.close();
			return false;
		}
	}

	public static HashMap<String, Object> getBlacklist(Context c) {
		HashMap<String, Object> map = new HashMap<String, Object>();
		initDB(c);
		Cursor cur = droidsniffDB.rawQuery("SELECT domain FROM DROIDSNIFF_BLACKLIST;", new String[] {});

		while (cur.moveToNext()) {
			String s = cur.getString(cur.getColumnIndex("domain"));
			map.put(s, null);
		}

		cur.close();
		droidsniffDB.close();
		return map;
	}

	public static void addBlacklistEntry(Context c, String name) {
		initDB(c);
		droidsniffDB.execSQL("INSERT INTO DROIDSNIFF_BLACKLIST (domain) VALUES (?);", new Object[] { name });
		droidsniffDB.close();
	}

	public static void setGeneric(Context c, boolean b) {
		initDB(c);
		Cursor cur = droidsniffDB.rawQuery("SELECT count(id) as count FROM DROIDSNIFF_PREFERENCES where name = 'generic';",
				new String[] {});
		cur.moveToFirst();
		int count = (int) cur.getLong(cur.getColumnIndex("count"));
		if (count == 0) {
			droidsniffDB.execSQL("INSERT INTO DROIDSNIFF_PREFERENCES (name, value) values ('generic', ?);",
					new String[] { Boolean.toString(b) });
		} else {
			droidsniffDB.execSQL("UPDATE DROIDSNIFF_PREFERENCES SET value=? WHERE name='generic';",
					new String[] { Boolean.toString(b) });
		}
		droidsniffDB.close();
	}

	public static void clearBlacklist(Context c) {
		initDB(c);
		droidsniffDB.execSQL("DELETE FROM DROIDSNIFF_BLACKLIST;", new Object[] {});
		droidsniffDB.close();
	}

/**
	public static long getLastDonateMessage(Context c) {
		try {
			initDB(c);
			Cursor cur = droidsniffDB.rawQuery("SELECT value FROM DROIDSNIFF_PREFERENCES where name = 'donate';",
					new String[] {});
			cur.moveToFirst();
			long datetime = cur.getLong(cur.getColumnIndex("value"));
			return datetime;
		} catch (Exception e) {
			Log.d(Constants.APPLICATION_TAG, "Could not load last donate datetime: " + e.getLocalizedMessage());
		} finally {
			droidsniffDB.close();
		}
		return 0L;
	}

	public static void setLastDonateMessage(Context c, long date) {
		initDB(c);
		Cursor cur = droidsniffDB.rawQuery("SELECT count(id) as count FROM DROIDSNIFF_PREFERENCES where name = 'donate';", new String[] {});
		cur.moveToFirst();
		int count = (int) cur.getLong(cur.getColumnIndex("count"));
		if (count == 0) {
			droidsniffDB.execSQL("INSERT INTO DROIDSNIFF_PREFERENCES (name, value) values ('donate', ?);",
					new String[] { Long.toString(date) });
		} else {
			droidsniffDB.execSQL("UPDATE DROIDSNIFF_PREFERENCES SET value=? WHERE name='donate';",
					new String[] { Long.toString(date) });
		}
		droidsniffDB.close();
	}
**/
	
	public static void setLastUpdateCheck(Context c, long date) {
		initDB(c);
		Cursor cur = droidsniffDB.rawQuery("SELECT count(id) as count FROM DROIDSNIFF_PREFERENCES where name = 'update';", new String[] {});
		cur.moveToFirst();
		int count = (int) cur.getLong(cur.getColumnIndex("count"));
		if (count == 0) {
			droidsniffDB.execSQL("INSERT INTO DROIDSNIFF_PREFERENCES (name, value) values ('update', ?);",
					new String[] { Long.toString(date) });
		} else {
			droidsniffDB.execSQL("UPDATE DROIDSNIFF_PREFERENCES SET value=? WHERE name='update';",
					new String[] { Long.toString(date) });
		}
		droidsniffDB.close();
	}
	
	public static long getLastUpdateMessage(Context c) {
		try {
			initDB(c);
			Cursor cur = droidsniffDB.rawQuery("SELECT value FROM DROIDSNIFF_PREFERENCES where name = 'update';",
					new String[] {});
			cur.moveToFirst();
			long datetime = cur.getLong(cur.getColumnIndex("value"));
			return datetime;
		} catch (Exception e) {
			Log.d(Constants.APPLICATION_TAG, "Could not load last update datetime: " + e.getLocalizedMessage());
		} finally {
			droidsniffDB.close();
		}
		return 0L;
	}
}
