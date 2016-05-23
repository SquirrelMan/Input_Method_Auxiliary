package com.example.jim84_000.input_method_auxiliary;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_list);
        tw=new TextToSpeech(this,this);
        this.setListAdapter(this.createListAdapter());
        ListView lv = (ListView) this.findViewById(android.R.id.list);
        lv.setOnItemClickListener(this);
    }

    private ListAdapter createListAdapter() {
        List<String> list = new ArrayList<String>();
        File sdDir = Environment.getExternalStorageDirectory();
        File cwDir = new File(sdDir, "MySpeaker/Main");
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
        Intent it = new Intent(Intent.ACTION_VIEW);
        String ext = MimeTypeMap.getFileExtensionFromUrl(Uri.fromFile(file).toString());
        Log.d(TAG, "ext: " + ext);
        if (ext == null || "".equals(ext)) {
            return;
        }
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                ext);
        Log.d(TAG, "mimeType: " + mimeType);

        // data 與 type 一定要一起呼叫
        it.setDataAndType(Uri.fromFile(file), mimeType);
        // 不可以分開呼叫
        // it.setData(Uri.fromFile(file));
        // it.setType(mimeType);
        this.startActivity(it);
    }

    //從File讀取data
    private Runnable SpeakFile = new Runnable() {
        @Override
        public void run() {
            String ret = "";
            try {
                File myFile = _CurrentFilePath;
                FileInputStream fIn = new FileInputStream(myFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                String aDataRow = "";
                String aBuffer = "";
                while ((aDataRow = myReader.readLine()) != null) {
                    aBuffer += aDataRow + "\n";
                }
                ret=aBuffer;
                myReader.close();
                sayHello(ret);
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e(TAG, "Can not read file: " + e.toString());
            }
        }
    };

    //=============================================語音==============================================
    private TextToSpeech tw;
    private static final String TAG = "SPEAKER";
    boolean mode=true;
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
        if(mode){
            result = tw.setLanguage(Locale.TAIWAN);//<<<===================================
            mode=!mode;
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not available.");
            }
            else tw.setSpeechRate(speed);
        }

        else {
            result = tw.setLanguage(Locale.US);//<<<===================================
            mode=!mode;
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not available.");
            }
            else tw.setSpeechRate(speed);
        }

    }

    public void sayHello(String hello) {
        // Select a random hello.
        // Drop allpending entries in the playback queue
        long start=System.currentTimeMillis();
        tw.speak(hello, TextToSpeech.QUEUE_FLUSH, null);
        //en.speak("hello", TextToSpeech.QUEUE_FLUSH, null);
        long duration=System.currentTimeMillis()- start;
        System.out.println(duration);
    }
}
