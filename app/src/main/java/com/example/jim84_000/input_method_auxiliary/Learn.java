package com.example.jim84_000.input_method_auxiliary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public class Learn {

    DBConnection helper;
    protected Dictionary dic;
    private String [] storewordspilt=new String[256];
    private int pointer_storewordspilt=0;
    Context context;

    public Learn(Context ctx,DBConnection dbConnection)
    {
        this.context=ctx;
        this.helper=dbConnection;
        System.setProperty("mmseg.dic.path", "./src/HelloChinese/data");
        dic = Dictionary.getInstance();
    }

    //=======================================學習===============================================
    public void Learning(String message){
        try {
            SpiltString(message,helper.getReadableDatabase());
            System.out.println(message);
            int i=1;
            while(check_id_ifexist(i)){
                i++;
            }
            int m=1;
            while(check_idrelation_ifexist(m)){
                m++;
            }
            for(int j = 0 ; j < pointer_storewordspilt+1 ; j++){
                ContentValues values = new ContentValues();
                SQLiteDatabase db = helper.getWritableDatabase();
                String tem_word;
                if(j == pointer_storewordspilt){
                    tem_word="#";
                }
                else{
                    tem_word=storewordspilt[j];
                }
                if(check_voc_ifexist(tem_word)){
                    System.out.println("check_voc_ifexist:" + tem_word);
                    //Cursor c = db.query("Voc", FROM_VOC, "content='" + storewordspilt[j] + "'", null, null, null, null);

                    Cursor c = db.rawQuery("select * from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + tem_word + "'", null);
                    c.moveToFirst();
                    String id_thist = c.getString(0);
                    String content_this = c.getString(1);
                    int count_this = c.getInt(2);
                    c.close();
                    count_this++;
                    values.put(DBConnection.VocSchema.ID, id_thist);
                    values.put(DBConnection.VocSchema.CONTENT, content_this);
                    values.put(DBConnection.VocSchema.COUNT, String.valueOf(count_this));
                    String where = DBConnection.VocSchema.ID+ " = " + id_thist;
                    db.update(DBConnection.VocSchema.TABLE_NAME, values, where, null);
                }
                else{
                    values.put(DBConnection.VocSchema.ID, String.valueOf(i++));
                    values.put(DBConnection.VocSchema.CONTENT, tem_word);
                    values.put(DBConnection.VocSchema.COUNT, String.valueOf(1));
                    db.insert(DBConnection.VocSchema.TABLE_NAME, null, values);
                }
                db.close();
            }
            for(int j = 0 ; j < pointer_storewordspilt ; j++){
                ContentValues values = new ContentValues();
                SQLiteDatabase db = helper.getWritableDatabase();
                String tem_secondword;
                if(j == pointer_storewordspilt-1){
                    tem_secondword="#";
                }
                else{
                    tem_secondword=storewordspilt[j+1];
                }

                if(check_vocrelation_ifexist(storewordspilt[j],tem_secondword)){
                    System.out.println("check_vocrelation_ifexist:" + storewordspilt[j] + tem_secondword);
                    //Cursor c = db.query("Voc", FROM_VOC, "content='" + storewordspilt[j] + "'", null, null, null, null);
                    Cursor c1 = db.rawQuery("select * from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + storewordspilt[j] + "'", null);
                    c1.moveToFirst();
                    String id_c1= c1.getString(0);
                    c1.close();
                    Cursor c2 = db.rawQuery("select * from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + tem_secondword + "'", null);
                    c2.moveToFirst();
                    String id_c2= c2.getString(0);
                    c2.close();

                    Cursor c = db.rawQuery("select * from " + DBConnection.RelationSchema.TABLE_NAME + " where id1='" + id_c1 + "'and " + "id2='" + id_c2 + "'", null);
                    c.moveToFirst();
                    String id_thist = c.getString(0);
                    String id1_this = c.getString(1);
                    String id2_this = c.getString(2);
                    int count_this = c.getInt(3);
                    c.close();
                    count_this++;
                    values.put(DBConnection.RelationSchema.ID, id_thist);
                    values.put(DBConnection.RelationSchema.ID1, id1_this);
                    values.put(DBConnection.RelationSchema.ID2, id2_this);
                    values.put(DBConnection.RelationSchema.COUNT, String.valueOf(count_this));
                    String where = DBConnection.RelationSchema.ID+ " = " + id_thist;
                    db.update(DBConnection.RelationSchema.TABLE_NAME, values, where, null);
                }
                else{
                    System.out.println("check_vocrelation_ifnotexist:" + storewordspilt[j] + tem_secondword);
                    Cursor c1 = db.rawQuery("select * from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + storewordspilt[j] + "'", null);
                    c1.moveToFirst();
                    String id_c1= c1.getString(0);
                    c1.close();
                    Cursor c2 = db.rawQuery("select * from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + tem_secondword + "'", null);
                    c2.moveToFirst();
                    String id_c2= c2.getString(0);
                    c2.close();

                    values.put(DBConnection.RelationSchema.ID, String.valueOf(m++));
                    values.put(DBConnection.RelationSchema.ID1, String.valueOf(id_c1));
                    values.put(DBConnection.RelationSchema.ID2, String.valueOf(id_c2));
                    values.put(DBConnection.RelationSchema.COUNT, String.valueOf(1));
                    db.insert(DBConnection.RelationSchema.TABLE_NAME, null, values);
                }
                db.close();
            }
            clear_storeword_spilt();
        }
        catch (Exception e){
            Toast.makeText(context, "斷字失敗", Toast.LENGTH_SHORT).show();
        }

    }
    //===============================================================================================

    //斷字系統=======================================================================================
    private void clear_storeword_spilt(){
        for(int i = 0 ; i < 256 ; i++){
            storewordspilt[i]="";
        }
        pointer_storewordspilt=0;
    }

    protected Seg getSeg() {
        return new ComplexSeg(dic);
    }

    public String segWords(String txt, String wordSpilt) throws IOException {
        Reader input = new StringReader(txt);
        StringBuilder sb = new StringBuilder();
        Seg seg = getSeg();
        MMSeg mmSeg = new MMSeg(input, seg);
        Word word = null;
        boolean first = true;
        while((word=mmSeg.next())!=null) {
            if(!first) {
                sb.append(wordSpilt);
            }
            String w = word.getString();
            storewordspilt[pointer_storewordspilt]=w;
            pointer_storewordspilt++;
            sb.append(w);
            first = false;

        }
        return sb.toString();
    }

    public String SpiltString(String s,SQLiteDatabase db){
        String tmp="";
        int strlen=s.length();
        //check for content whether in database
        for(int i=0;i<strlen;) {
            boolean flag = true;
            String str = String.valueOf(s.charAt(i));
            Cursor c = db.rawQuery("SELECT * FROM "+DBConnection.VocSchema.TABLE_NAME+
                    " WHERE "+DBConnection.VocSchema.CONTENT+" = '" + str + "';", null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                tmp=tmp+str+" ";
                i++;
                continue;
            }
            c.close();

            if (i + 1 == strlen)
                flag = false;
            //check the vocabulary in combination of current and next character whether is in the database
            //if yes ,update weight in database, and continue loop
            if (flag) {
                str += String.valueOf(s.charAt(i + 1));
                c = db.rawQuery("SELECT * FROM "+DBConnection.VocSchema.TABLE_NAME+
                        " WHERE "+DBConnection.VocSchema.CONTENT+" = '" + str + "';", null);
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    tmp+=str+" ";
                    i += 2;
                    c.close();
                    continue;
                }
            }
            c.close();
            str = String.valueOf(s.charAt(i));
            tmp+=str;
            i++;
        }
        try {
            return spilt(tmp);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tmp;
    }

    public String spilt(String args) throws IOException {
        String txt = "";

        if(args.length() > 0) {
            txt = args;
            return segWords(txt, "|");
        }
        else
            return "";
    }



    public boolean check_id_ifexist(int tid){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor mCount = db.rawQuery("select count(*) from " + DBConnection.VocSchema.TABLE_NAME + " where _id='" + tid + "'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        System.out.println("id_Count:"+String.valueOf(count));
        if(count >= 1)
            return true;
        else
            return false;
    }
    public boolean check_idrelation_ifexist(int tid){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor mCount = db.rawQuery("select count(*) from " + DBConnection.RelationSchema.TABLE_NAME + " where _id='" + tid + "'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        System.out.println("Relation_id_Count:"+String.valueOf(count));
        if(count >= 1)
            return true;
        else
            return false;
    }
    public boolean check_voc_ifexist(String _content){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor mCount = db.rawQuery("select count(*) from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + _content + "'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        System.out.println("voc_Count:"+String.valueOf(count));
        if(count >= 1)
            return true;
        else
            return false;
    }
    public boolean check_vocrelation_ifexist(String _content,String _content2){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor c1 = db.rawQuery("select * from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + _content + "'", null);
        c1.moveToFirst();
        int id_c1= c1.getInt(0);
        c1.close();
        Cursor c2 = db.rawQuery("select * from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + _content2 + "'", null);
        c2.moveToFirst();
        int id_c2= c2.getInt(0);
        c2.close();
        Cursor mcount = db.rawQuery("select count(*) from " + DBConnection.RelationSchema.TABLE_NAME + " where id1='" + id_c1 + "' and " + "id2='" + id_c2 + "'", null);
        mcount.moveToFirst();
        int count=mcount.getInt(0);
        mcount.close();
        System.out.println("vocrelation_Count:"+String.valueOf(count));
        if(count >= 1)
            return true;
        else
            return false;
    }
}
