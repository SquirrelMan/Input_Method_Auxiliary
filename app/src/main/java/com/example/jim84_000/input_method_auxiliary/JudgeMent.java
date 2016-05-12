package com.example.jim84_000.input_method_auxiliary;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class JudgeMent {
    SQLiteDatabase db;

    public JudgeMent(SQLiteDatabase tmpDB){
        this.db=tmpDB;
    }

    public void judge(String s){
        int strlen=s.length();
        int[] id=new int[strlen];
        int count=0;
        //check for content whether in database
        for(int i=0;i<strlen;)
        {
            boolean flag=true;
            String str=String.valueOf(s.charAt(i));
            Cursor c=db.rawQuery("SELECT * FROM pattern WHERE content = '"+str+"';",null);
            if(c.getCount()>0)
            {
                c.moveToFirst();
                //update weight
                int key=id[count]=Integer.parseInt(c.getString(c.getColumnIndex("_id")));
                int weight=Integer.parseInt(c.getString(c.getColumnIndex("weights")))+1;
                ContentValues values=new ContentValues();
                String selection="_id=";
                String[] selectionArgs=new String[]{String.valueOf(key)};
                values.put("weights",weight);
                db.update("pattern",values,selection,selectionArgs);
                //end update
                i++;
                count++;
                continue;
            }
            c.close();

            if(i+1==strlen)
                flag=false;
            //check the vocabulary in combination of current and next character whether is in the database
            //if yes ,update weight in database, and continue loop
            if(flag) {
                str += String.valueOf(s.charAt(i + 1));
                c = db.rawQuery("SELECT * FROM pattern WHERE content = '" + str + "';", null);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    //update weight
                    int key = id[count] = Integer.parseInt(c.getString(c.getColumnIndex("_id")));
                    int weight = Integer.parseInt(c.getString(c.getColumnIndex("weights"))) + 1;
                    ContentValues values = new ContentValues();
                    String selection = "_id=";
                    String[] selectionArgs = new String[]{String.valueOf(key)};
                    values.put("weights", weight);
                    db.update("pattern", values, selection, selectionArgs);
                    //end update
                    i += 2;
                    count++;
                    c.close();
                    continue;
                }
            }
            c.close();
            str=String.valueOf(s.charAt(i));
            //database has no data for current position character and insert it
            ContentValues values=new ContentValues();
            values.put("content",str);
            values.put("weights",1);
            int key=(int)db.insert("pattern",null,values);
            id[count]=key;
            count++;
            i++;

        }
        //end check

        //handle relation
        handle_relation(id,count);
    }

    private void handle_relation(int[] key,int count){
        //check if relation i between i+1 exists in database, if no insert into db
        for(int i=0;i<count-1;i++)
        {
            int id1=key[i];
            int id2=key[i+1];
            String sql="SELECT * FROM relation WHERE id1 = "+String.valueOf(id1)+" AND id2 = "+String.valueOf(id2)+";";
            Cursor c=db.rawQuery(sql,null);
            if (c.getCount()>0)
            {
                c.close();
                continue;
            }
            c.close();
            ContentValues values=new ContentValues();
            values.put("id1",id1);
            values.put("id2",id2);
            db.insert("relation",null,values);
        }
    }
}
