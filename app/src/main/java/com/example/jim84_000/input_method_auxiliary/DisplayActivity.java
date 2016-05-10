package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static android.os.StrictMode.setThreadPolicy;


public class DisplayActivity extends Activity {

    public static final String IP_SERVER = "192.168.49.143";
    public static int PORT = 8988;
    public static String str="";
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
        tv=(TextView)findViewById(R.id.textView);
        test=(TextView)findViewById(R.id.textView3);
        tv.setText(str);
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
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

    @Override
    protected void onStart(){
        super.onStart();
        handler=new Handler();

        //建立Thread
        Thread fst = new Thread(socket_server);
        //啟動Thread
        fst.start();
    }

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
                    //接收資料
                    do{
                        i++;
                        System.out.println(i);
                        line = in.readUTF();
                        System.out.println(i);
                        handler.post(new Runnable() {
                            public void run() {tv.setText(line);
                            }
                        });
                    }while (line!=null);
                    System.out.println("END");
                } catch (Exception e) {
                    handler.post(new Runnable() {
                        public void run() {
                            test.setText("傳送失敗");
                        }
                    });
                    in.close();
                }
            }catch(IOException e) {
                handler.post(new Runnable() {
                    public void run() {
                        test.setText("建立socket失敗");
                    }
                });
            }
        }
    };
}
