package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ScrollView;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;
import com.neurosky.thinkgear.TGDevice;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Locale;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import static android.os.StrictMode.ThreadPolicy;
import static android.os.StrictMode.setThreadPolicy;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import static android.os.StrictMode.setThreadPolicy;

public class InputActivity extends Activity{
    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;
    private DataOutputStream out;
    private Socket socket;
    Button btn_send,btn_next,btn_lv1,btn_load,btn_clear,btn_mwm;
    Button[] btn=new Button[9];
    EditText editText;
    TextView tv_status;
    InputData[][] Data=new InputData[3][18];
    public static boolean con=false;
    int level=0,offset=0; //level=0,1,2 offset=0,9
    protected Dictionary dic;
    private String [] storewordspilt=new String[256];
    private int pointer_storewordspilt=0;

    private void LoadData(){
        String[][] tmp=new String[3][18];
        for(int i=0;i<54;i++)
            tmp[i/18][i%18]="";
        tmp[0]= new String[]{"我","你","爸爸","媽媽","姊姊","老師","助教","同學","不","電腦","手機","平板","USB","硬碟","作業","程式","這邊","問題"};
        tmp[1]= new String[]{"想","要","是","的","們","好","好像","不舒服","請","有","沒","了解","謝謝!","","","","",""};
        tmp[2]= new String[]{"上廁所","吃飯","喝水","用","拿","睡覺","幫忙","嗎","說","問","寫","了","麻煩","一個","","","",""};
        for(int i=0;i<54;i++)
            Data[i/18][i%18]=new InputData(tmp[i/18][i%18]);
    }



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_client);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            ThreadPolicy policy = new ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }
        System.setProperty("mmseg.dic.path", "./src/HelloChinese/data");
        dic = Dictionary.getInstance();

        btn_send=(Button)findViewById(R.id.btn_send);
        btn_next=(Button)findViewById(R.id.btn_next);
        btn_lv1=(Button)findViewById(R.id.btn_lv1);
        btn_clear=(Button)findViewById(R.id.btn_clear);
        btn_load=(Button)findViewById(R.id.btn_load);
        btn_mwm=(Button)findViewById(R.id.btn_mwm);

        int[] btnid={R.id.btn1,R.id.btn2,R.id.btn3,R.id.btn4,R.id.btn5,R.id.btn6,R.id.btn7,R.id.btn8,R.id.btn9};
        for(int i=0;i<9;i++)
            btn[i]=(Button)findViewById(btnid[i]);

        editText=(EditText)findViewById(R.id.editText);
        tv_status=(TextView)findViewById(R.id.sender_status);
        tv_status.setText("");
        String str="test測試123";
        // editText.setText(str);
        //editText.setSelection(str.length());
        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editText.setText("");
            }
        });
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });

        try{
            LoadData();
        }catch (Exception e){
            System.out.println(e.toString());
        }
        System.out.println(Data.length*Data[2].length);

        setBtnText();

        for(int i=0;i<9;i++){
            final int arg = i;
            btn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s=editText.getText().toString() + btn[arg].getText().toString();
                    editText.setText(s);
                    editText.setSelection(s.length());
                    level=(level+1)%3;
                    offset=0;
                    setBtnText();
                }
            });

        }

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                next_page();
            }
        });
        btn_lv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                level = 0;
                offset = 0;
                setBtnText();
            }
        });
    }

    private void next_page(){
        level=(level+offset/9)%3;
        offset=((offset+9)%2)*9;
        setBtnText();
    }

    private void setBtnText()
    {
        for(int i=0;i<9;i++){
            btn[i].setText(Data[level][i+offset].text);
        }
    }

    private void send(){

        tv_status.setText("");
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

    private void ConnectToDisplay(){
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

    private void terminate(){
        try {
            out.close();
            socket.close();
        }catch (Exception e){
            tv_status.setText(e.toString());
        }
    }
    @Override
    protected void onStart(){
        super.onStart();
        if(con)
            ConnectToDisplay();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(con)
            terminate();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(con)
            terminate();
    }
    
    
    
    //斷字系統
    private clear_storeword_spilt(){
        for(int i = 0 ; i < 256 ; i++){
            storewordspilt[pointer_storewordspilt]="";
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
                storewordspilt[pointer_storewordspilt]=wordSpilt;
                pointer_storewordspilt++;
            }
            String w = word.getString();
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
}
