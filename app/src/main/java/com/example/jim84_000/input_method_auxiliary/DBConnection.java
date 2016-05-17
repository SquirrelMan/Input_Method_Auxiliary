package com.example.jim84_000.input_method_auxiliary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
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
                + VocSchema.ID  + " INTEGER primary key autoincrement not null, "
                + VocSchema.CONTENT + " text unique not null, "
                + VocSchema.COUNT + " INTEGER not null default 1" + ");";
        //Log.i("haiyang:createDB=", sql);
        db.execSQL(sql);

        String sql2 = "CREATE TABLE " + RelationSchema.TABLE_NAME + " ("
                + RelationSchema.ID  + " INTEGER primary key autoincrement, "
                + RelationSchema.ID1 + " INTEGER not null, "
                + RelationSchema.ID2 + " INTEGER not null, "
                + RelationSchema.COUNT + " INTEGER not null default 1" + ");";
        //Log.i("haiyang:createDB=", sql);
        db.execSQL(sql2);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }

    public long insert(String content, SQLiteDatabase db){
        long newid=0;
        ContentValues values=new ContentValues();
        values.put(VocSchema.CONTENT,content);
        try{
            newid=db.insert(VocSchema.TABLE_NAME,null,values);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Insert Failed");
        }
        return newid;
    }

    public long insert(String content,int count, SQLiteDatabase db){
        long newid=0;
        ContentValues values=new ContentValues();
        values.put(VocSchema.CONTENT,content);
        values.put(VocSchema.COUNT,count);
        try{
            newid=db.insert(VocSchema.TABLE_NAME,null,values);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Insert Failed");
        }
        return newid;
    }

    public long insert(int id1,int id2, SQLiteDatabase db){
        long newid=0;
        ContentValues values=new ContentValues();
        values.put(RelationSchema.ID1,id1);
        values.put(RelationSchema.ID2,id2);
        try{
            newid=db.insert(RelationSchema.TABLE_NAME,null,values);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Insert Failed");
        }
        return newid;
    }

    public long insert(int id1,int id2,int count, SQLiteDatabase db){
        long newid=0;
        ContentValues values=new ContentValues();
        values.put(RelationSchema.ID1,id1);
        values.put(RelationSchema.ID2,id2);
        values.put(RelationSchema.COUNT,count);
        try{
            newid=db.insert(RelationSchema.TABLE_NAME,null,values);
        }catch (Exception e){
            e.printStackTrace();
            System.out.println("Insert Failed");
        }
        return newid;
    }

    public boolean update(String content, SQLiteDatabase db){
        boolean ret=false;
        String query="select * from "+VocSchema.TABLE_NAME+" where "
                +VocSchema.CONTENT+" = '"+content+"';";
        Cursor c=db.rawQuery(query,null);
        if(c.getCount()>0){
            c.moveToFirst();
            int id=c.getInt(c.getColumnIndex(VocSchema.ID));
            int count=c.getInt(c.getColumnIndex(VocSchema.COUNT))+1;
            ContentValues values=new ContentValues();
            values.put(VocSchema.COUNT,count);
            String selection=VocSchema.ID+ " = ";
            String[] selectionArgs={String.valueOf(id)};
            try {
                db.update(VocSchema.TABLE_NAME,values,selection,selectionArgs);
                ret=true;
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("UPDATE FAIL");
            }
        }
        c.close();
        return ret;
    }

    public boolean update(int id1,int id2, SQLiteDatabase db){
        boolean ret=false;
        String query="select * from "+RelationSchema.TABLE_NAME+" where "
                +RelationSchema.ID1+" = '"+id1+"' and "+RelationSchema.ID2+" = '"+id2+"';";
        Cursor c=db.rawQuery(query,null);
        if(c.getCount()>0){
            c.moveToFirst();
            int id=c.getInt(c.getColumnIndex(RelationSchema.ID));
            int count=c.getInt(c.getColumnIndex(RelationSchema.COUNT))+1;
            ContentValues values=new ContentValues();
            values.put(RelationSchema.COUNT,count);
            String selection=RelationSchema.ID+ " = ";
            String[] selectionArgs={String.valueOf(id)};
            try {
                db.update(RelationSchema.TABLE_NAME,values,selection,selectionArgs);
                ret=true;
            }catch (Exception e){
                e.printStackTrace();
                System.out.println("UPDATE FAIL");
            }
        }
        c.close();
        return ret;
    }
}