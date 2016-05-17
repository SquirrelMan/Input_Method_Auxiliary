package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class DataMove extends Activity {
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
    Learn learn=new Learn(this,helper);

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
                learn.Learning(aDataRow);
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

}
