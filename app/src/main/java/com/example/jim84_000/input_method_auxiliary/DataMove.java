package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
    final int REQUEST_CODE=0;
    static final String _path="/sdcard/Download";
    public static final String _DBName="Database.db";
    public static String _LDName=_path+"/LearnData.data";
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

    private Handler uihandler = new Handler();
    private ProgressDialog progressDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.move_menu);

        listener_moveintoout = new View.OnClickListener() {
            public void onClick(View v) {
                moveFile(Path_in,_DBName,Path_out);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        };
        //
        listener_copyintoout = new View.OnClickListener() {
            public void onClick(View v) {
                //System.out.println(Path_in);
                copyFile(Path_in, _DBName, Path_out);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        };

        listener_moveouttoin = new View.OnClickListener() {
            public void onClick(View v) {
                moveFile(Path_out, _DBName, Path_in);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        };
        //
        listener_copyouttoin = new View.OnClickListener() {
            public void onClick(View v) {
                //System.out.println(Path_in);
                copyFile(Path_out, _DBName, Path_in);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        };
        //
        listener_deletein = new View.OnClickListener() {
            public void onClick(View v) {
                deleteFile(Path_in, _DBName);
                Toast.makeText(DataMove.this,"Success",Toast.LENGTH_SHORT).show();
            }
        };
        listener_learndata = new View.OnClickListener() {
            public void onClick(View v) {
                final String mimeType = "*/*";
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType(mimeType);
                //startActivityForResult(intent, REQUEST_CODE);

                learnfromfile();
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
    private boolean readFromFile() {
        boolean success=false;
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            path.mkdir();
            // create the file in which we will write the contents
            File myFile = new File(_LDName);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
                learn.Learning(aDataRow);
            }
            success=true;
            myReader.close();
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return success;
    }

    private void learnfromfile(){
        System.out.println(_LDName);
        progressDialog = ProgressDialog.show(DataMove.this, "請稍後", "學習中...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(readFromFile())
                    uihandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DataMove.this,"Success Learn",Toast.LENGTH_SHORT).show();
                        }
                    });
                else
                    uihandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DataMove.this, "Fail Learn", Toast.LENGTH_SHORT).show();
                        }
                    });
                progressDialog.dismiss();
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("6666666666666666666666666666");
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println("777777777777777777777777");
        // 有選擇檔案
        if ( resultCode == RESULT_OK  && requestCode == REQUEST_CODE)
        {
            System.out.println("8888888888888888888888888");
            // 取得檔案的 Uri
            Uri uri = data.getData();
            if( uri != null )
            {
                _LDName=uri.getPath();
                System.out.println(_LDName);
                //learnfromfile();
            }
            else
            {
                System.out.println("9999999999999999999999");
                setTitle("無效的檔案路徑 !!");
            }
        }
        else
        {
            System.out.println("0000000000000000000000000000");
            setTitle("取消選擇檔案 !!");
            //learnfromfile();
        }
    }

}
