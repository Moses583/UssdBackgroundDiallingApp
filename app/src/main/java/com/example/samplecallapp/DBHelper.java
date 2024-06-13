package com.example.samplecallapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {
    public DBHelper(@Nullable Context context) {
        super(context, "DatabaseSeven.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("Create table Responses(id INTEGER PRIMARY KEY AUTOINCREMENT, response TEXT,ussdCode TEXT, subscriptionId INTEGER)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Responses");
    }

    public Boolean insertResponse( String response, String ussdCode, int subscriptionId){
        SQLiteDatabase database = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("response",response);
        contentValues.put("ussdCode",ussdCode);
        contentValues.put("subscriptionId",subscriptionId);
        long result = database.insert("Responses",null,contentValues);
        return result != -1;

    }

    public Cursor getResponses(){
        SQLiteDatabase database = this.getWritableDatabase();
        return database.rawQuery("Select * from Responses ORDER BY id DESC",null);
    }

    public Cursor getFailedResponses(){
        SQLiteDatabase database = this.getWritableDatabase();
        String pattern = "Failed%";
        return database.rawQuery("SELECT * FROM Responses WHERE response LIKE ?", new String[]{pattern});
    }

    public Boolean deleteData () {
        SQLiteDatabase DB = this.getWritableDatabase();
        String pattern = "Failed%";
        long result = DB.delete("Responses", "response LIKE ?", new String[]{pattern});
        return result != -1;
    }




}
