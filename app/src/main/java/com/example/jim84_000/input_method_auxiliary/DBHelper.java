package com.example.jim84_000.input_method_auxiliary;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.InputStream;

public class DBHelper extends SQLiteOpenHelper{


    public static final int _DBVersion = 1; //<-- 版本
    public static final String _DBName="Vocabulary.db";
    public static final String _TableName="DATA";

    public DBHelper(Context context) {
        super(context, _DBName, null, _DBVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String cmd1="";
        String cmd2="";
        for(int i=1;i<=9;i++){
            cmd1+="NEXT_"+String.valueOf(i)+" INTEGER DEFAULT 1, ";
            if(i<9)
                cmd2+="FOREIGN KEY(NEXT_"+String.valueOf(i)+") REFERENCES "+_TableName+"(_ID), ";
            else
                cmd2+="FOREIGN KEY(NEXT_"+String.valueOf(i)+") REFERENCES "+_TableName+"(_ID) );";
        }
        final String SQL="CREATE TABLE IF NOT EXISTS "+_TableName+"( "+
                "_ID INTEGER PRIMARY KEY AUTOINCREMENT, "+
                "VOCABULARY NVARCHAR(3) NOT NULL, "+
                "WEIGHT INTEGER DEFAULT 1 NOT NULL, "+cmd1+cmd2;
        db.execSQL(SQL);
    }

    public  void copyDBtoSD(){
        final String SDCard_PATH=android.os.Environment.getExternalStorageDirectory().getPath();
        File DataPath_dest=new File(SDCard_PATH+"/MyDB");
        if(!DataPath_dest.exists())
            DataPath_dest.mkdir();
        File ToDBFile=new File(DataPath_dest.getPath()+_DBName);
        if(ToDBFile.exists())
            ToDBFile.delete();
        InputStream inputStream;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL = "DROP TABLE " + _TableName;
        db.execSQL(SQL);
    }
}
