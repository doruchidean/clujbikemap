package com.doruchidean.clujbikemap.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.doruchidean.clujbikemap.helpers.Factory;
import com.doruchidean.clujbikemap.models.BusSchedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Doru on 12/05/16.
 *
 */
public class DatabaseHandler extends SQLiteOpenHelper {

    private static final int
            DATABASE_VERSION = 3,
            BUS_TABLE_REFRESH_INTERVAL = 10; //days
    public static final String
            DATABASE_NAME="clujbikemap.db",
            BUS_TABLE_NAME="BusSchedules",
            COLUMN_BUS_NAME="BusName",
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
                        "(_id INTEGER PRIMARY KEY, " +
                        COLUMN_BUS_NUMBER + " TEXT, " +
                        COLUMN_BUS_NAME + " TEXT, " +
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
        db.execSQL("DROP TABLE IF EXISTS " + BUS_TABLE_NAME);
        onCreate(db);
    }

    public String getBusNumber(String busName) {
        Cursor c = getReadableDatabase().query(
                BUS_TABLE_NAME, new String[]{COLUMN_BUS_NUMBER}, COLUMN_BUS_NAME + " = ?",
                new String[]{busName}, null, null, null);
        String result = "";
        try {
            if (c != null && c.moveToFirst()) {
                result = c.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }

        return result;
    }

    public int getBusTableCount(){

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + BUS_TABLE_NAME, null);
        int result = c.getCount();
        c.close();
        return result;
    }

    public boolean isActualized(int busTableCreatedDay){
        boolean result;
        result = (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) - busTableCreatedDay) < BUS_TABLE_REFRESH_INTERVAL;

        if(!result){
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + BUS_TABLE_NAME);
            onCreate(db);
        }

        return result;
    }

    public void insertBusNames(List<BusSchedule> list) {
        SQLiteDatabase db = getWritableDatabase();

        db.beginTransaction();
        try {
            for (BusSchedule b : list) {
                db.insert(BUS_TABLE_NAME, null, getContentValues(b));
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public List<String> getAllBuses() {
        List<String> result = new ArrayList<>();

        Cursor c = getReadableDatabase().query(
                BUS_TABLE_NAME, new String[]{COLUMN_BUS_NAME}, null, null, null, null, null);

        try {
            if (c != null && c.moveToFirst()) {
                do {
                    result.add(c.getString(0));
                } while (c.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }

        return result;
    }

    public void updateBusDepartures(BusSchedule b) {
        getWritableDatabase().update(BUS_TABLE_NAME, getContentValues(b),
                COLUMN_BUS_NUMBER+"=?", new String[]{b.getBusNumber()});
    }

    public BusSchedule getBusSchedule(String busName) {
        BusSchedule result = null;

        Cursor c = getReadableDatabase().query(BUS_TABLE_NAME, null,
                COLUMN_BUS_NAME + "=?", new String[]{busName}, null, null, null);

        try {
            if (c != null && c.moveToFirst()) {
                result = buildBusSchedule(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) c.close();
        }

        return result;
    }

    private BusSchedule buildBusSchedule(Cursor c) {
        BusSchedule result = new BusSchedule();

        result.setName(c.getString(c.getColumnIndex(COLUMN_BUS_NAME)));
        result.setBusNumber(c.getString(c.getColumnIndex(COLUMN_BUS_NUMBER)));
        result.setNameCapat1(c.getString(c.getColumnIndex(COLUMN_BUS_CAPAT_1)));
        result.setNameCapat2(c.getString(c.getColumnIndex(COLUMN_BUS_CAPAT_2)));
        result.setPlecariCapat1LV(c.getString(c.getColumnIndex(COLUMN_ORAR_LV_CAPAT_1)));
        result.setPlecariCapat2LV(c.getString(c.getColumnIndex(COLUMN_ORAR_LV_CAPAT_2)));
        result.setPlecariCapat1S(c.getString(c.getColumnIndex(COLUMN_ORAR_S_CAPAT1)));
        result.setPlecariCapat2S(c.getString(c.getColumnIndex(COLUMN_ORAR_S_CAPAT2)));
        result.setPlecariCapat1D(c.getString(c.getColumnIndex(COLUMN_ORAR_D_CAPAT1)));
        result.setPlecariCapat2D(c.getString(c.getColumnIndex(COLUMN_ORAR_D_CAPAT2)));

        return result;
    }

    private ContentValues getContentValues(BusSchedule b) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_BUS_NAME, b.getName());
        values.put(COLUMN_BUS_NUMBER, b.getBusNumber());
        values.put(COLUMN_BUS_CAPAT_1, b.getNameCapat1());
        values.put(COLUMN_BUS_CAPAT_2, b.getNameCapat2());
        values.put(COLUMN_ORAR_LV_CAPAT_1, b.getPlecariCapat1LV());
        values.put(COLUMN_ORAR_LV_CAPAT_2, b.getPlecariCapat2LV());
        values.put(COLUMN_ORAR_S_CAPAT1, b.getPlecariCapat1S());
        values.put(COLUMN_ORAR_S_CAPAT2, b.getPlecariCapat2S());
        values.put(COLUMN_ORAR_D_CAPAT1, b.getPlecariCapat1D());
        values.put(COLUMN_ORAR_D_CAPAT2, b.getPlecariCapat2D());

        return values;
    }

    public void insertBusScheduleForToday(BusSchedule busSchedule){

        SQLiteDatabase db = getWritableDatabase();

        if(hasBusNumber(busSchedule.getBusNumber())){
            db.update(BUS_TABLE_NAME, getContentValues(busSchedule),
                    COLUMN_BUS_NUMBER + " = ? ", new String[]{busSchedule.getBusNumber()});
        }else{
            db.insert(BUS_TABLE_NAME, null, getContentValues(busSchedule));
        }
    }

    public void insertBusScheduleNotExistent(String busNumber){
        ContentValues rowValues = new ContentValues();
        rowValues.put(COLUMN_BUS_NUMBER, busNumber);

        if(hasBusNumber(busNumber)){
            //trace
            Cursor c = getReadableDatabase().query(BUS_TABLE_NAME, new String[]{COLUMN_ORAR_LV_CAPAT_1}, COLUMN_BUS_NUMBER+"=?", new String[]{busNumber}, null, null, null);
            c.close();

            getWritableDatabase().update(
                    BUS_TABLE_NAME,
                    rowValues,
                    COLUMN_BUS_NUMBER+"=?", new String[]{busNumber}
            );

            Cursor c1 = getReadableDatabase().query(BUS_TABLE_NAME, new String[]{COLUMN_ORAR_LV_CAPAT_1}, COLUMN_BUS_NUMBER+"=?", new String[]{busNumber}, null, null, null);
            c1.close();

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

    public boolean hasBusSchedule(String busName){
        if (busName.length() == 0) return false;

        boolean result;

        Cursor c = getReadableDatabase().query(
                BUS_TABLE_NAME,
                new String[]{COLUMN_ORAR_LV_CAPAT_1},
                COLUMN_BUS_NAME + " = ?", new String[]{busName},
                null, null, null
        );

        result = c.getCount() > 0;

        c.close();

        return result;
    }

    public HashMap<String,ArrayList<String>> getBusScheduleForTodayByNr(String busNumber){
        return getBusScheduleForToday(COLUMN_BUS_NUMBER, busNumber);
    }

    public HashMap<String, ArrayList<String>> getBusScheduleForTodayByName(String busName) {
        return getBusScheduleForToday(COLUMN_BUS_NAME, busName);
    }

    private HashMap<String, ArrayList<String>> getBusScheduleForToday(String column, String value) {
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
                column + " = ? ", new String[]{value},
                null, null, null);

        if(c.getCount() > 0) {
            c.moveToFirst();

            numeCapete.add(c.getString(c.getColumnIndex(COLUMN_BUS_CAPAT_1)));
            numeCapete.add(c.getString(c.getColumnIndex(COLUMN_BUS_CAPAT_2)));

            String csvPlecariCapat1 = c.getString(c.getColumnIndex(columnOrarCapat1Today));
            if (csvPlecariCapat1 != null) {
                Collections.addAll(plecariCapat1, csvPlecariCapat1.split(","));
            }

            String csvPlecariCapat2 = c.getString(c.getColumnIndex(columnOrarCapat2Today));
            if (csvPlecariCapat2 != null) {
                Collections.addAll(plecariCapat2, csvPlecariCapat2.split(","));
            }
        }

        c.close();

        result.put(Factory.NUME_CAPETE, numeCapete);
        result.put(Factory.PLECARI_CAPAT_1, plecariCapat1);
        result.put(Factory.PLECARI_CAPAT_2, plecariCapat2);

        return result;
    }

}
