package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static android.os.StrictMode.ThreadPolicy;
import static android.os.StrictMode.setThreadPolicy;

public class InputActivity extends Activity{
    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;
    private DataOutputStream out;
    private Socket socket;
    Button btn;
    EditText editText;
    TextView test;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            ThreadPolicy policy = new ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }
        btn=(Button)findViewById(R.id.button);
        editText=(EditText)findViewById(R.id.editText);
        test=(TextView)findViewById(R.id.textView2);
        test.setText("");
        btn.setText("SEND");
        String str="test測試123";
        editText.setText(str);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
    }


    private void send(){

        test.setText("");
        //要傳送的字串
        String message = editText.getText().toString();
        try {
            //傳送資料
            out.writeUTF(message);
            Toast.makeText(this,"成功傳送!",Toast.LENGTH_SHORT).show();
        } catch (IOException e){
            Toast.makeText(getApplicationContext(), "傳送失敗", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart(){
        super.onStart();
        try {
            InetAddress serverAddr = null;
            SocketAddress sc_add = null;            //設定Server IP位置
            serverAddr = InetAddress.getByName(IP_SERVER);
            //設定port:1234
            sc_add= new InetSocketAddress(serverAddr,PORT);

            socket = new Socket();
            //與Server連線，timeout時間2秒
            socket.connect(sc_add, 2000);
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e){
            Toast.makeText(getApplicationContext(), "傳送失敗", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        try {
            out.close();
            socket.close();
        }catch (Exception e){
            test.setText(e.toString());
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        try {
            out.close();
            socket.close();
        }catch (Exception e){
            test.setText(e.toString());
        }
    }
}
