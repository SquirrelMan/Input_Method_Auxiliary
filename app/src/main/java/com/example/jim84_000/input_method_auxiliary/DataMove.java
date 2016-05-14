package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by jim84_000 on 2016/5/14.
 */
public class DataMove extends Activity {
    public static final int _DBVersion = 1; //<-- 版本
    public static final String _DBName="Database.db";
    View.OnClickListener listener_moveintoout = null;
    View.OnClickListener listener_copyintoout = null;
    View.OnClickListener listener_moveouttoin = null;
    View.OnClickListener listener_copyouttoin = null;
    View.OnClickListener listener_deletein = null;
    Button button_moveintoout;
    Button button_copyintoout;
    Button button_moveouttoin;
    Button button_copyouttoin;
    Button button_deletein;
    String Path_out = "/sdcard/Download/";
    String Path_in = "/data/data/com.example.jim84_000.input_method_auxiliary/databases/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move_menu);

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
                moveFile(Path_out,_DBName,Path_in);
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
}
