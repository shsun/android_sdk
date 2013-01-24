package com.emediate.android.database;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.emediate.android.model.Ads;

public class DBHandler {
	/**
	 * Log Name
	 */

	public static final int TABLE_VERSION = 1;
	public static final String DATABASE_NAME = "EmediateSDK";

	public static final String TABLE_NAME = "AdsTable";

	public static final String KEY_ROWID = "_id";
	public static final String COLUMN_NAME = "ADSNAME";
	public static final String COLUMN_URL = "URL";
	public static final String COLUMN_TYPE = "TYPE";
	public static final String DATABASE_CREATE = "create table " + TABLE_NAME
			+ "(" + KEY_ROWID + " integer primary key autoincrement, "
			+ COLUMN_NAME + " text not null," + COLUMN_TYPE + " text not null," + COLUMN_URL + " text not null);";

	private static String newData[] = new String[] { COLUMN_NAME, COLUMN_URL, COLUMN_TYPE};

	private DBHelper DBHelper;
	private SQLiteDatabase db;

	public DBHandler(Context context) {
		DBHelper = new DBHelper(context, DATABASE_NAME, null, TABLE_VERSION);
	}

	// ---opens the database---
	public DBHandler open() throws SQLException {
		db = DBHelper.getWritableDatabase();
		return this;
	}

	// ---closes the database---
	public void close() {
		DBHelper.close();
	}

	/**
	 * Get All Tables
	 */
	public ArrayList<Ads> getAllAds() {
		ArrayList<Ads> ads = new ArrayList<Ads>();
		open();
		Cursor c = db.query(TABLE_NAME, newData, null, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				Ads ad = new Ads();
				ad.name = c.getString(0);
				ad.url = c.getString(1);
				ad.type = c.getString(2);
				ads.add(ad);
			} while (c.moveToNext());

		}
		close();
		return ads;
	}
	
	/**
	 * Get All Tables by ads Type
	 */
	public ArrayList<Ads> getAllAdsByType(String adsType) {
		ArrayList<Ads> ads = new ArrayList<Ads>();
		open();
		String[] whereArgs = { String.valueOf(adsType) };
		Cursor c = db.query(true, TABLE_NAME, newData, COLUMN_TYPE + "=?", whereArgs, null, null, null, null);
		if (c.moveToFirst()) {
			do {
				Ads ad = new Ads();
				ad.name = c.getString(0);
				ad.url = c.getString(1);
				ad.type = c.getString(2);
				ads.add(ad);
			} while (c.moveToNext());

		}
		close();
		return ads;
	}
	
	/**
	 * Get All Tables
	 */
	public boolean hasThisAd(String adsName) {
		open();
		String[] whereArgs = { String.valueOf(adsName) };
		Cursor c = db.query(true, TABLE_NAME, newData, COLUMN_NAME + "=?", whereArgs, null, null, null, null);
		if (c.moveToFirst())
			return true;

		return false;
	}
	

	// ---insert a new Ads into the database---
	public long insert(String adsName, String adsUrl, String adsType) {
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(COLUMN_NAME, adsName);
		initialValues.put(COLUMN_URL, adsUrl);
		initialValues.put(COLUMN_TYPE, adsType);
		
		return db.insert(TABLE_NAME, null, initialValues);
	}

	// ---delete Ads from the db
	public void delete(String adsName) throws SQLException {
		open();
		String[] whereArgs = { String.valueOf(adsName) };
		db.delete(TABLE_NAME, COLUMN_NAME + "=?", whereArgs);

		close();
	}


}
