package com.example.finalproject;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class DBManager extends SQLiteOpenHelper {
    private static DBManager dbManager;
    private static  String DATABASE_NAME = "savedListDB";
    private static  int VERSION = 1;
    public final static String TABLE_NAME = "savedList";
    public final static String COUNTER = "Counter";
    public final static String COL_TITLE = "Title";
    public final static String COL_DATE = "Date";
    public final static String COL_URL = "Url";
    public final static String COL_HDURL = "Hdurl";


    public DBManager(Context ctx){

        super(ctx, DATABASE_NAME, null, VERSION);
        Log.d("DBManager", "Database manager initialized");
    }
    public static DBManager instanceOfDB(Context context){
        if(dbManager == null)
            dbManager = new DBManager(context);

        return dbManager;
    }
    //Creation of Database Connection
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ( " +
                COUNTER + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITLE + " TEXT, " +
                COL_DATE + " TEXT, " +
                COL_URL + " TEXT, " +
                COL_HDURL + " TEXT )";
        db.execSQL(createTableQuery);
        Log.d("DBManager", "Database table has created, successfully");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        String onUpgradeQuery = "DROP TABLE IF EXISTS "+TABLE_NAME;
        db.execSQL(onUpgradeQuery);
        onCreate(db);
    }
}
