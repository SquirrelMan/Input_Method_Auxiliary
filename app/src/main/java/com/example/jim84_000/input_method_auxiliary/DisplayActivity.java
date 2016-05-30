package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;

import static android.os.StrictMode.setThreadPolicy;


public class DisplayActivity extends Activity implements TextToSpeech.OnInitListener {

    public static final String IP_SERVER = "192.168.49.143";
    public static int PORT = 8988;
    public static String str="";
    String[] buf=new String[100];
    int count=0;
    TextView tv;
    TextView test;
    private  Handler handler;
    private ServerSocket serverSocket=null;
    private String line;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }
        for(int i=0;i<100;i++)
            buf[i]="";
        tw=new TextToSpeech(this,this);
        en=new TextToSpeech(this,this);
        tv=(TextView)findViewById(R.id.textView);
        test=(TextView)findViewById(R.id.textView3);
        tv.setText(str);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (tw != null) {
            tw.stop();
            tw.shutdown();
        }
        if (en != null) {
            en.stop();
            en.shutdown();
        }
        tw=en=null;
        try {
            serverSocket.close();
        }catch (IOException e){
            Log.d(WiFiDirectActivity.TAG,e.toString());
        }
        this.finish();
    }

    @Override
    protected void onPause(){
        super.onPause();
        try {
            serverSocket.close();
        }catch (IOException e){
            Log.d(WiFiDirectActivity.TAG,e.toString());
        }
        this.finish();
    }
    int current=0;
    @Override
    protected void onStart(){
        super.onStart();
        handler=new Handler();

        //建立Thread
        Thread fst = new Thread(socket_server);
        //啟動Thread
        fst.start();
    }
    boolean end=false;
    ReentrantLock lock=new ReentrantLock();
    String msg2="";
    Thread display=new Thread(new Runnable() {
        @Override
        public void run() {

            while (!end){

                if(count>0 &&  (!tw.isSpeaking() || !en.isSpeaking())){
                    msg2=buf[current];
                    System.out.println("CURRENT : "+current);
                    System.out.println("COUNT : "+count);
                    handler.post(new Runnable() {
                        public void run() {
                            int size=msg2.length();
                            float font=120;
                            if(size>30)
                                font=100;
                            else if(size>80)
                                font=75;
                            else if(size>120)
                                font=50;
                            if(msg2.length()>0) {
                                tv.setTextSize(font);
                                tv.setText(msg2);
                                sayHello(" " + msg2);
                                msg2="";
                                buf[current]="";
                                current++;
                                current%=100;
                                lock.lock();
                                count--;
                                lock.unlock();
                            }
                        }
                    });
                }
            }
        }
    });
    private Runnable socket_server = new Runnable(){
        public void run(){
            try{
                //建立serverSocket
                serverSocket = new ServerSocket(PORT);
                //接收連線
                handler.post(new Runnable() {
                    public void run() {
                        test.setText("Listening....");
                    }
                });
                Socket client = serverSocket.accept();
                handler.post(new Runnable() {
                    public void run() {
                        test.setText("Connected.");
                    }
                });
                DataInputStream in = new DataInputStream(client.getInputStream());
                try {
                    int i=0;
                    display.start();
                    //接收資料
                    do{
                        line = in.readUTF();
                        if(line.length()>0){
                            System.out.println("COUNT : "+count);
                            buf[i]=line;
                            System.out.println("Receive "+i+" : "+buf[i]);
                            i++;
                            i%=100;
                            lock.lock();
                            count++;
                            lock.unlock();
                        }
                    }while (line!=null);
                    System.out.println("END");
                } catch (Exception e) {
                    handler.post(new Runnable() {
                        public void run() {
                            test.setText("傳送失敗");
                        }
                    });
                    in.close();
                    end=true;
                    DisplayActivity.this.finish();
                }
            }catch(IOException e) {
                handler.post(new Runnable() {
                    public void run() {
                        test.setText("建立socket失敗");
                    }
                });
                end=true;
                DisplayActivity.this.finish();
            }
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

    public void sayHello(String hello) {
        //  tw.speak(hello,TextToSpeech.QUEUE_ADD,null);
        String[] msg=new String[50];
        for(int i=0;i<50;i++)
            msg[i]="";
        int previous=0,count1=0,speaker=0,current;
        char first=hello.charAt(0);
        if(first>='a' && first<='z' || first>='A' && first<='Z')
            speaker=1;
        for(int i=0;i<hello.length();i++)
        {
            current=0;
            char ch1=hello.charAt(i);
            if(ch1>='a' && ch1<='z' || ch1>='A' && ch1<='Z')
                current=1;
            else if(ch1==' '||ch1==','||ch1=='-')
                current=previous;

            if(current!=previous)
            {
                previous=current;
                count1++;
            }
            msg[count1]+=String.valueOf(ch1);
        }

        for(int i=0;i<=count1;i++)
        {
            System.out.println("SPEAK : "+msg[i]);
            if(speaker==0){
                tw.speak(msg[i],TextToSpeech.QUEUE_ADD,null);
                speaker++;
                speaker%=2;
            }
            else {
                en.speak(msg[i],TextToSpeech.QUEUE_ADD,null);
                speaker++;
                speaker%=2;
            }
        }
    }
}
