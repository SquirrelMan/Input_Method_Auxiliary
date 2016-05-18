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
            String msg=SpiltString(message,helper.getReadableDatabase());
            System.out.println(msg);

            int k=1;
            while(check_idsentence_ifexist(k)){
                k++;
            }
            SQLiteDatabase db2 = helper.getWritableDatabase();
            if(check_sentence_ifexist(message)){
                System.out.println("check_sentence_ifexist:" + message);
                //Cursor c = db.query("Voc", FROM_VOC, "content='" + storewordspilt[j] + "'", null, null, null, null);

                Cursor c = db2.rawQuery("select * from " + DBConnection.SentenceSchema.TABLE_NAME + " where content='" + message + "'", null);
                c.moveToFirst();
                String id_thist = c.getString(0);
                String content_this = c.getString(1);
                int count_this = c.getInt(2);
                c.close();
                count_this++;
                ContentValues values = new ContentValues();
                values.put(DBConnection.SentenceSchema.ID, id_thist);
                values.put(DBConnection.SentenceSchema.CONTENT, content_this);
                values.put(DBConnection.SentenceSchema.COUNT, String.valueOf(count_this));
                String where = DBConnection.SentenceSchema.ID+ " = " + id_thist;
                db2.update(DBConnection.SentenceSchema.TABLE_NAME, values, where, null);
            }
            else{
                ContentValues values = new ContentValues();
                values.put(DBConnection.SentenceSchema.ID, String.valueOf(k++));
                values.put(DBConnection.SentenceSchema.CONTENT, message);
                values.put(DBConnection.SentenceSchema.COUNT, String.valueOf(1));
                db2.insert(DBConnection.SentenceSchema.TABLE_NAME, null, values);
            }
            db2.close();


            for(int j = 0 ; j < pointer_storewordspilt+1 ; j++){

                SQLiteDatabase db = helper.getWritableDatabase();
                String tem_word=((j==pointer_storewordspilt)?"#":storewordspilt[j]);

                System.out.println(tem_word);
                if(!helper.update(tem_word,db)){
                    helper.insert(tem_word,db);
                }
                db.close();
            }

            for(int j = 0 ; j < pointer_storewordspilt ; j++){
                SQLiteDatabase db = helper.getWritableDatabase();
                String tem_secondword=((j==pointer_storewordspilt-1)?"#":storewordspilt[j+1]);
                int id1=helper.getVocID(storewordspilt[j],db);
                int id2=helper.getVocID(tem_secondword,db);

                System.out.println(tem_secondword);
                if(!helper.update(id1,id2,db)){
                    helper.insert(id1,id2,db);
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
            char ch=s.charAt(i);
            String str = String.valueOf(ch);
            if((ch==32 )||(ch==44)){
                tmp+=" ";
                i++;
                continue;
            }
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

    public boolean check_idsentence_ifexist(int tid){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor mCount = db.rawQuery("select count(*) from " + DBConnection.SentenceSchema.TABLE_NAME + " where _id='" + tid + "'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        System.out.println("Sentence_id_Count:"+String.valueOf(count));
        if(count >= 1)
            return true;
        else
            return false;
    }
    public boolean check_sentence_ifexist(String _content){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor c1 = db.rawQuery("select count(*) from " + DBConnection.SentenceSchema.TABLE_NAME + " where content='" + _content + "'", null);
        c1.moveToFirst();
        int count=c1.getInt(0);
        c1.close();
        System.out.println("sentence_Count:" + String.valueOf(count));
        if(count >= 1)
            return true;
        else
            return false;
    }
}
