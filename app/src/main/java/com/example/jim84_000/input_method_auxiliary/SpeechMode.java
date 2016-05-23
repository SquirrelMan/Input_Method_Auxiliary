package com.example.jim84_000.input_method_auxiliary;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by jim84_000 on 2016/5/19.
 */
public class SpeechMode extends ListActivity implements AdapterView.OnItemClickListener, TextToSpeech.OnInitListener {

    private String parentPath;
    File _CurrentFilePath;
    Handler handler=new Handler();
    ProgressDialog dialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Don't forget to shutdown!
        if (tw != null) {
            tw.stop();
            tw.shutdown();
        }

        if (en != null) {
            en.stop();
            en.shutdown();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_list);
        tw=new TextToSpeech(this,this);
        en=new TextToSpeech(this,this);
        this.setListAdapter(this.createListAdapter());
        ListView lv = (ListView) this.findViewById(android.R.id.list);
        lv.setOnItemClickListener(this);
    }

    private ListAdapter createListAdapter() {
        List<String> list = new ArrayList<String>();
        File sdDir = Environment.getExternalStorageDirectory();
        File cwDir = new File(sdDir, "Download");
        this.parentPath = cwDir.getPath();
        Log.d(TAG, "根目錄：" + this.parentPath);
        File[] files = cwDir.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }
            list.add(f.getName());
            Log.d(TAG, "加入檔案：" + f.getName());
        }
        return new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, list);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        File file = new File(this.parentPath,
                ((TextView) view).getText().toString());
        _CurrentFilePath=file;

        new Thread(SpeakFile).start();

        // 開啟檔案
        //Intent it = new Intent(Intent.ACTION_VIEW);
        //String ext = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        //Log.d(TAG, "ext: " + ext);
        //if (ext == null || "".equals(ext)) {
         //   return;
        //}
        //String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
        //        ext);
        //Log.d(TAG, "mimeType: " + mimeType);

        // data 與 type 一定要一起呼叫
        //it.setDataAndType(Uri.fromFile(file), mimeType);
        // 不可以分開呼叫
        // it.setData(Uri.fromFile(file));
        // it.setType(mimeType);
        //this.startActivity(it);
    }

    //從File讀取data
    private Runnable SpeakFile = new Runnable() {
        @Override
        public void run() {
            try {
                File myFile = _CurrentFilePath;
                FileInputStream fIn = new FileInputStream(myFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                String aDataRow = "";
                while ((aDataRow = myReader.readLine()) != null) {
                    if(aDataRow.length()==0)
                        continue;
                    char ch=aDataRow.charAt(0);
                    if((ch>='a' && ch<='z') || (ch>='A' && ch<='Z'))
                    {
                        //System.out.println("EN Line");
                        sayHello(aDataRow,0);
                    }
                    else{
                        //System.out.println("TW Line");
                        sayHello(aDataRow,1);
                    }

                }
                myReader.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e(TAG, "Can not read file: " + e.toString());
            }
        }
    };

    //=============================================語音==============================================
    private TextToSpeech tw,en;
    private static final String TAG = "SPEAKER";
    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            language();
        }

        else {
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }
    private void language(){
        float speed=(float)0.8;
        int result;

        result = tw.setLanguage(Locale.TAIWAN);//<<<===================================
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e(TAG, "Language is not available.");
        }
        else {
            tw.setSpeechRate(speed);
            System.out.println("Success TW");
        }

        result = en.setLanguage(Locale.US);//<<<===================================
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.e(TAG, "Language is not available.");
        }
        else {
            en.setSpeechRate(speed);
            System.out.println("Success EN");
            }

    }

    public void sayHello(String hello,int mode) {
        if(mode==1)
            tw.speak(hello, TextToSpeech.QUEUE_ADD, null);
        else
            en.speak(hello, TextToSpeech.QUEUE_ADD, null);
    }
}
