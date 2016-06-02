package com.example.jim84_000.input_method_auxiliary;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.os.StrictMode.setThreadPolicy;

/**
 * Created by jim84_000 on 2016/5/19.
 */
public class SpeechMode extends ListActivity implements AdapterView.OnItemClickListener, TextToSpeech.OnInitListener {
    public static String path="Main";
    private String parentPath;
    File _CurrentFilePath;
    Handler handler=new Handler();
    ProgressDialog dialog;
    private DataOutputStream out; //for transfer
    public static boolean con = false;
    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;
    private Socket socket;

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
        terminate();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.speech_list);
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }
        tw=new TextToSpeech(this,this);
        en=new TextToSpeech(this,this);
        this.setListAdapter(this.createListAdapter());
        ListView lv = (ListView) this.findViewById(android.R.id.list);
        lv.setOnItemClickListener(this);
    }

    private ListAdapter createListAdapter() {
        List<String> list = new ArrayList<String>();
        File sdDir = Environment.getExternalStorageDirectory();
        File cwDir = new File(sdDir, "MySpeaker/"+path);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (con)
            ConnectToDisplay();
    }

    private void ConnectToDisplay() {
        try {
            InetAddress serverAddr;
            SocketAddress sc_add;            //設定Server IP位置
            serverAddr = InetAddress.getByName(IP_SERVER);
            //設定port
            sc_add = new InetSocketAddress(serverAddr, PORT);

            socket = new Socket();
            //與Server連線，timeout時間2秒
            socket.connect(sc_add, 2000);
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "連線失敗", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    private void terminate() {
        if (con) {
            try {
                out.close();
                socket.close();
            } catch (Exception e) {
            }
        }
    }

    //從File讀取data
    private Runnable SpeakFile = new Runnable() {
        @Override
        public void run() {
            Looper.prepare();
            try {
                File myFile = _CurrentFilePath;
                FileInputStream fIn = new FileInputStream(myFile);
                BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
                String aDataRow = "";
                while ((aDataRow = myReader.readLine()) != null) {
                    if(aDataRow.length()==0)
                        continue;
                    if (con) {
                        try {
                            //傳送資料
                            out.writeUTF(aDataRow);
                            Toast.makeText(getApplicationContext(), "成功傳送!", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(), "傳送失敗", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        char ch=aDataRow.charAt(0);
                        if(Check.check_eng(ch))
                        {
                            //System.out.println("EN Line");
                            sayHello(aDataRow,0);
                        }
                        else{
                            //System.out.println("TW Line");
                            sayHello(aDataRow,1);
                        }
                    }

                }
                myReader.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.toString());
            } catch (IOException e) {
                Log.e(TAG, "Can not read file: " + e.toString());
            }
            Looper.loop();
        }
    };

    //=============================================語音==============================================
    private TextToSpeech tw,en;
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
        if(!mode){
            result = en.setLanguage(Locale.US);//<<<===================================
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not available.");
            }
            else{
                en.setSpeechRate(speed);
                //Toast.makeText(this,"EN",Toast.LENGTH_SHORT).show();
                System.out.println("EN READY");
            }
        }

        else {
            result = tw.setLanguage(Locale.TAIWAN);//<<<===================================
            mode=!mode;
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not available.");
            }
            else{
                tw.setSpeechRate(speed);
                //Toast.makeText(this,"TW",Toast.LENGTH_SHORT).show();
                System.out.println("TW READY");
            }
        }

    }

    public void sayHello(String hello,int mode) {
        if(mode==1)
            tw.speak(hello, TextToSpeech.QUEUE_ADD, null);
        else
            en.speak(hello, TextToSpeech.QUEUE_ADD, null);
    }
}
