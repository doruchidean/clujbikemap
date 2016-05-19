package com.doruchidean.clujbikemap.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.doruchidean.clujbikemap.activities.MapsActivity;
import com.doruchidean.clujbikemap.helpers.Factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by Doru on 12/05/16.
 *
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	public static final int
		DATABASE_VERSION = 1; //todo increment with each database update (update onUpgrade callback)

	public static final String
		DATABASE_NAME="clujbikemap.db",
		BUS_TABLE_NAME="BusSchedules",
		COLUMN_BUS_NUMBER="BusNumber",
		COLUMN_BUS_CAPAT_1="Capat1",
		COLUMN_BUS_CAPAT_2="Capat2",
		COLUMN_ORAR_LV_CAPAT_1 ="OrarLVCapat1",
		COLUMN_ORAR_LV_CAPAT_2 ="OrarLVCapat2",
		COLUMN_ORAR_S_CAPAT1="OrarSCapat1",
		COLUMN_ORAR_S_CAPAT2="OrarSCapat2",
		COLUMN_ORAR_D_CAPAT1="OrarDCapat1",
		COLUMN_ORAR_D_CAPAT2="OrarDCapat2";

	private static DatabaseHandler mInstance;

	public static DatabaseHandler getInstance(Context context){
		if(mInstance == null){
			mInstance = new DatabaseHandler(context);
		}
		return mInstance;
	}

	private DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override public void onCreate(SQLiteDatabase db) {
		db.execSQL(
			"CREATE TABLE " + BUS_TABLE_NAME +
				"(" +
				COLUMN_BUS_NUMBER + " TEXT UNIQUE PRIMARY KEY, " +
				COLUMN_BUS_CAPAT_1 + " TEXT, " +
				COLUMN_BUS_CAPAT_2 + " TEXT, " +
				COLUMN_ORAR_LV_CAPAT_1 + " TEXT, " +
				COLUMN_ORAR_LV_CAPAT_2 + " TEXT," +
				COLUMN_ORAR_S_CAPAT1 + " TEXT," +
				COLUMN_ORAR_S_CAPAT2 + " TEXT, " +
				COLUMN_ORAR_D_CAPAT1 + " TEXT, " +
				COLUMN_ORAR_D_CAPAT2 + " TEXT" +
				");"
		);
	}

	@Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//todo update this handling
		Log.w(DatabaseHandler.class.getName(),
			"Upgrading database from version " + oldVersion + " to "
				+ newVersion + ", which will destroy all old data ==== create a .sql file " +
				"that updates the old database to the new one, and save it in res folder");

		db.execSQL("DROP TABLE IF EXISTS " + BUS_TABLE_NAME);
		onCreate(db);
	}

	public int getBusTableCount(){

		SQLiteDatabase db = getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + BUS_TABLE_NAME, null);
		int result = c.getCount();
		c.close();
		return result;
	}

	public void insertBusScheduleForToday(String busNumber, String capat1, String capat2,
																				String orarCapat1Today, String orarCapat2Today){

		SQLiteDatabase db = getWritableDatabase();

		ContentValues rowValues = new ContentValues();
		rowValues.put(COLUMN_BUS_NUMBER, busNumber);
		rowValues.put(COLUMN_BUS_CAPAT_1, capat1);
		rowValues.put(COLUMN_BUS_CAPAT_2, capat2);
		switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
			case Calendar.SATURDAY:
				rowValues.put(COLUMN_ORAR_S_CAPAT1, orarCapat1Today);
				rowValues.put(COLUMN_ORAR_S_CAPAT2, orarCapat2Today);
				break;
			case Calendar.SUNDAY:
				rowValues.put(COLUMN_ORAR_D_CAPAT1, orarCapat1Today);
				rowValues.put(COLUMN_ORAR_D_CAPAT2, orarCapat2Today);
			default:
				rowValues.put(COLUMN_ORAR_LV_CAPAT_1, orarCapat1Today);
				rowValues.put(COLUMN_ORAR_LV_CAPAT_2, orarCapat2Today);
		}

		if(hasBusNumber(busNumber)){
			db.update(BUS_TABLE_NAME, rowValues, COLUMN_BUS_NUMBER + " = ? ", new String[]{busNumber});
		}else{
			db.insert(BUS_TABLE_NAME, null, rowValues);
		}
	}

	public void insertBusScheduleNotExistent(String busNumber){
		ContentValues rowValues = new ContentValues();
		rowValues.put(COLUMN_BUS_NUMBER, busNumber);

		if(hasBusNumber(busNumber)){
			//trace
			Cursor c = getReadableDatabase().query(BUS_TABLE_NAME, new String[]{COLUMN_ORAR_LV_CAPAT_1}, COLUMN_BUS_NUMBER, new String[]{busNumber}, null, null, null);
			String trace = c.getString(c.getColumnIndex(COLUMN_ORAR_LV_CAPAT_1));
			c.close();
			MapsActivity.trace("column lv inainte : "  + trace);

			getWritableDatabase().update(
				BUS_TABLE_NAME,
				rowValues,
				COLUMN_BUS_NUMBER, new String[]{busNumber}
			);

			Cursor c1 = getReadableDatabase().query(BUS_TABLE_NAME, new String[]{COLUMN_ORAR_LV_CAPAT_1}, COLUMN_BUS_NUMBER, new String[]{busNumber}, null, null, null);
			String trace1 = c1.getString(c1.getColumnIndex(COLUMN_ORAR_LV_CAPAT_1));
			c1.close();
			MapsActivity.trace("column lv dupa: " + trace1);

		}else{
			getWritableDatabase().insert(BUS_TABLE_NAME, null, rowValues);
		}
	}

	public boolean hasBusNumber(String busNumber){
		boolean result;

		Cursor c = getReadableDatabase().query(
			BUS_TABLE_NAME,
			new String[]{COLUMN_BUS_NUMBER},
			COLUMN_BUS_NUMBER + " = ? ", new String[]{busNumber},
			null, null, null
		);

		result = c.getCount() > 0;
		c.close();
		return result;
	}

	public boolean hasBusScheduleForToday(String busNumber){
		boolean result;
		String[] columnsForToday = new String[2];
		switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
			case Calendar.SATURDAY:
				columnsForToday[0] = COLUMN_ORAR_S_CAPAT1;
				columnsForToday[1] = COLUMN_ORAR_S_CAPAT2;
				break;
			case Calendar.SUNDAY:
				columnsForToday[0] = COLUMN_ORAR_D_CAPAT1;
				columnsForToday[1] = COLUMN_ORAR_D_CAPAT2;
				break;
			default:
				columnsForToday[0] = COLUMN_ORAR_LV_CAPAT_1;
				columnsForToday[1] = COLUMN_ORAR_LV_CAPAT_2;
		}

		Cursor c = getReadableDatabase().query(
			BUS_TABLE_NAME,
			columnsForToday,
			COLUMN_BUS_NUMBER + " = ?", new String[]{busNumber},
			null, null, null
		);

		result = c.getCount() > 0;

		MapsActivity.trace("hasBusScheduleForToday - " + busNumber + " has count: " + c.getCount());
		c.close();

		return result;
	}

	/**
	 * This method returns a row from the bus table
	 * @param busNumber row identifier
	 * @return HashMap{NUME_CAPETE:{0,1}, PLECARI_CAPAT_1:{...}, PLECARI_CAPAT_2:{...}
	 */
	public HashMap<String,ArrayList<String>> getBusScheduleForToday(String busNumber){

		HashMap<String, ArrayList<String>> result = new HashMap<>();

		ArrayList<String> numeCapete = new ArrayList<>();
		ArrayList<String> plecariCapat1 = new ArrayList<>();
		ArrayList<String> plecariCapat2 = new ArrayList<>();

		String columnOrarCapat1Today, columnOrarCapat2Today;

		switch (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)){
			case Calendar.SATURDAY:
				columnOrarCapat1Today = COLUMN_ORAR_S_CAPAT1;
				columnOrarCapat2Today = COLUMN_ORAR_S_CAPAT2;
				break;
			case Calendar.SUNDAY:
				columnOrarCapat1Today = COLUMN_ORAR_D_CAPAT1;
				columnOrarCapat2Today = COLUMN_ORAR_D_CAPAT2;
				break;
			default:
				columnOrarCapat1Today = COLUMN_ORAR_LV_CAPAT_1;
				columnOrarCapat2Today = COLUMN_ORAR_LV_CAPAT_2;
		}

		Cursor c = getReadableDatabase().query(
			BUS_TABLE_NAME,
			new String[]{COLUMN_BUS_CAPAT_1, COLUMN_BUS_CAPAT_2, columnOrarCapat1Today, columnOrarCapat2Today},
			COLUMN_BUS_NUMBER + " = ? ", new String[]{busNumber},
			null, null, null);

		if(c.getCount() > 0) {
			c.moveToFirst();
			numeCapete.add(c.getString(c.getColumnIndex(COLUMN_BUS_CAPAT_1)));
			numeCapete.add(c.getString(c.getColumnIndex(COLUMN_BUS_CAPAT_2)));

			String csvPlecariCapat1 = c.getString(c.getColumnIndex(columnOrarCapat1Today));
			if (csvPlecariCapat1 != null) {
				Collections.addAll(plecariCapat1, csvPlecariCapat1.split(", "));
			}
			String csvPlecariCapat2 = c.getString(c.getColumnIndex(columnOrarCapat2Today));
			if (csvPlecariCapat2 != null) {
				Collections.addAll(plecariCapat2, csvPlecariCapat2.split(", "));
			}

			result.put(Factory.NUME_CAPETE, numeCapete);

			result.put(Factory.PLECARI_CAPAT_1, plecariCapat1);
			result.put(Factory.PLECARI_CAPAT_2, plecariCapat2);
		}

		c.close();

		return result;
	}

}
