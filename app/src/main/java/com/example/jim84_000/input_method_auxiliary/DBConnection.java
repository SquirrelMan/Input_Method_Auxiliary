package com.example.jim84_000.input_method_auxiliary;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DBConnection extends SQLiteOpenHelper {

    public static final int _DBVersion = 1; //<-- 版本
    public static final String _DBName="Database.db";

    public interface VocSchema {
        String TABLE_NAME = "Voc";          //Table Name
        String ID = "_id";                    //ID
        String CONTENT = "content";       //CONTENT
        String COUNT = "count";           //COUNT
    }

    public interface RelationSchema {
        String TABLE_NAME = "Relation";          //Table Name
        String ID = "_id";                    //ID
        String ID1 = "id1";       //ID1
        String ID2 = "id2";           //ID2
        String COUNT = "count";       //COUNT
    }
    public DBConnection(Context ctx) {
        super(ctx, _DBName,null, _DBVersion);
    }
    public void onCreate(SQLiteDatabase db) {

        String sql = "CREATE TABLE " + VocSchema.TABLE_NAME + " ("
                + VocSchema.ID  + " INTEGER primary key autoincrement, "
                + VocSchema.CONTENT + " text unique not null, "
                + VocSchema.COUNT + " INTEGER not null" + ");";
        //Log.i("haiyang:createDB=", sql);
        db.execSQL(sql);

        String sql2 = "CREATE TABLE " + RelationSchema.TABLE_NAME + " ("
                + RelationSchema.ID  + " INTEGER primary key autoincrement, "
                + RelationSchema.ID1 + " INTEGER not null, "
                + RelationSchema.ID2 + " INTEGER not null, "
                + RelationSchema.COUNT + " INTEGER not null" + ");";
        //Log.i("haiyang:createDB=", sql);
        db.execSQL(sql2);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }
}