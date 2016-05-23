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

            SQLiteDatabase db = helper.getWritableDatabase();
            //handle sentence
            if (message.length()>3){
                if(!helper.update(false,message,db)){
                    helper.insert(false,message,db);
                }
            }

            String msg=SpiltString(message, db);
            System.out.println(msg);

            //handle vocabulary
            for(int i = 0 ; i < pointer_storewordspilt+1 ; i++){
                String tem_word=((i==pointer_storewordspilt)?"#":storewordspilt[i]);
                System.out.println(tem_word);
                if(!helper.update(true,tem_word,db)){
                    helper.insert(true,tem_word,db);
                }
            }
            //handle relation
            for(int i = 0 ; i < pointer_storewordspilt ; i++){
                String tem_secondword=((i==pointer_storewordspilt-1)?"#":storewordspilt[i+1]);
                int id1=helper.getVocID(storewordspilt[i],db);
                int id2=helper.getVocID(tem_secondword,db);
                System.out.println(tem_secondword);
                if(!helper.update(id1,id2,db)){
                    helper.insert(id1,id2,db);
                }

            }
            clear_storeword_spilt();

            db.close();
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
                tmp+=String.valueOf(ch);
                i++;
                continue;
            }

            else if ((ch>='A' && ch<='Z') || (ch>='a' && ch<='z')){
                tmp+=String.valueOf(ch);
                i++;
                continue;
            }
            Cursor c = db.rawQuery("SELECT * FROM "+DBConnection.VocSchema.TABLE_NAME+
                    " WHERE "+DBConnection.VocSchema.CONTENT+" = '" + str + "';", null);
            if (c.getCount() > 0) {
                c.moveToFirst();
                tmp=tmp+" "+str+" ";
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
}
