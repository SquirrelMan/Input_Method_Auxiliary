package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * Created by jim84_000 on 2016/5/14.
 */
public class DataMove extends Activity {
    public static final int _DBVersion = 1; //<-- 版本
    public static final String _DBName="Database.db";
    public static final String _LDName="LearnData.data";
    View.OnClickListener listener_moveintoout = null;
    View.OnClickListener listener_copyintoout = null;
    View.OnClickListener listener_moveouttoin = null;
    View.OnClickListener listener_copyouttoin = null;
    View.OnClickListener listener_deletein = null;
    View.OnClickListener listener_learndata = null;
    Button button_moveintoout;
    Button button_copyintoout;
    Button button_moveouttoin;
    Button button_copyouttoin;
    Button button_deletein;
    Button button_learndata;
    String Path_out = "/sdcard/Download/";
    String Path_in = "/data/data/com.example.jim84_000.input_method_auxiliary/databases/";
    private static final String TAG = DataMove.class.getName();
    DBConnection helper= new DBConnection(this);
    
    private String [] storewordspilt=new String[256];
    private int pointer_storewordspilt=0;
    protected Dictionary dic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move_menu);

        System.setProperty("mmseg.dic.path", "./src/HelloChinese/data");
        dic = Dictionary.getInstance();
        clear_storeword_spilt();

        listener_moveintoout = new View.OnClickListener() {
            public void onClick(View v) {
                moveFile(Path_in,_DBName,Path_out);
            }
        };
        //
        listener_copyintoout = new View.OnClickListener() {
            public void onClick(View v) {
                //System.out.println(Path_in);
                copyFile(Path_in, _DBName, Path_out);
            }
        };

        listener_moveouttoin = new View.OnClickListener() {
            public void onClick(View v) {
                moveFile(Path_out, _DBName, Path_in);
            }
        };
        //
        listener_copyouttoin = new View.OnClickListener() {
            public void onClick(View v) {
                //System.out.println(Path_in);
                copyFile(Path_out, _DBName, Path_in);
            }
        };
        //
        listener_deletein = new View.OnClickListener() {
            public void onClick(View v) {
                deleteFile(Path_in, _DBName);
            }
        };
        listener_learndata = new View.OnClickListener() {
            public void onClick(View v) {
                readFromFile();
            }
        };

        button_moveintoout = (Button)findViewById(R.id.btn_moveintoout);
        button_moveintoout.setOnClickListener(listener_moveintoout);
        button_copyintoout = (Button)findViewById(R.id.btn_copyintoout);
        button_copyintoout.setOnClickListener(listener_copyintoout);
        button_moveouttoin = (Button)findViewById(R.id.btn_moveouttoin);
        button_moveouttoin.setOnClickListener(listener_moveouttoin);
        button_copyouttoin = (Button)findViewById(R.id.btn_copyouttoin);
        button_copyouttoin.setOnClickListener(listener_copyouttoin);
        button_deletein = (Button)findViewById(R.id.btn_deletein);
        button_deletein.setOnClickListener(listener_deletein);
        button_learndata = (Button)findViewById(R.id.btn_learndata);
        button_learndata.setOnClickListener(listener_learndata);
    }
    private void moveFile(String inputPath, String inputFile, String outputPath) {
        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file
            out.flush();
            out.close();
            out = null;

            // delete the original file
            new File(inputPath + inputFile).delete();


        }

        catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }

    private void deleteFile(String inputPath, String inputFile) {
        try {
            // delete the original file
            new File(inputPath + inputFile).delete();
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }
    }

    private void copyFile(String inputPath, String inputFile, String outputPath) {

        InputStream in = null;
        OutputStream out = null;
        try {

            //create output directory if it doesn't exist
            File dir = new File (outputPath);
            if (!dir.exists())
            {
                dir.mkdirs();
            }


            in = new FileInputStream(inputPath + inputFile);
            out = new FileOutputStream(outputPath + inputFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            in.close();
            in = null;

            // write the output file (You have now copied the file)
            out.flush();
            out.close();
            out = null;

        }  catch (FileNotFoundException fnfe1) {
            Log.e("tag", fnfe1.getMessage());
        }
        catch (Exception e) {
            Log.e("tag", e.getMessage());
        }

    }
    //從File讀取data
    private String readFromFile() {
        String ret = "";
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            path.mkdir();
            // create the file in which we will write the contents
            File myFile = new File(path, _LDName);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
                run(aDataRow);
                System.out.println(aDataRow);
                int i=1;
                while(check_id_ifexist(i)){
                    i++;
                }
                for(int j = 0 ; j < pointer_storewordspilt ; j++){
                    ContentValues values = new ContentValues();
                    SQLiteDatabase db = helper.getWritableDatabase();
                    if(check_voc_ifexist(storewordspilt[j])){
                        System.out.println("check_voc_ifexist:" + storewordspilt[j]);
                        //Cursor c = db.query("Voc", FROM_VOC, "content='" + storewordspilt[j] + "'", null, null, null, null);

                        Cursor c = db.rawQuery("select * from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + storewordspilt[j] + "'", null);
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
                        values.put(DBConnection.VocSchema.CONTENT, storewordspilt[j]);
                        values.put(DBConnection.VocSchema.COUNT, String.valueOf(1));
                        db.insert(DBConnection.VocSchema.TABLE_NAME, null, values);
                    }
                    db.close();
                }
                clear_storeword_spilt();
            }
            ret=aBuffer;
            myReader.close();
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return ret;
    }
    
    //斷字系統
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

    public String run(String args) throws IOException {
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
}
