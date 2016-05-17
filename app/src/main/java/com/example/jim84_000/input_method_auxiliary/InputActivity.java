package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import com.neurosky.thinkgear.*;


import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Locale;

import static android.os.StrictMode.ThreadPolicy;
import static android.os.StrictMode.setThreadPolicy;

public class InputActivity extends Activity implements TextToSpeech.OnInitListener{
    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;
    private DataOutputStream out; //for transfer
    private Socket socket;
    Button btn_send,btn_next,btn_lv1,btn_load,btn_clear,btn_mwm,btn_speech;
    boolean status_speech=false;
    Button[] btn=new Button[9];
    EditText editText;
    TextView tv_status;
    public static boolean con=false;

    InputData[] datas,currentDatas=new InputData[9];
    int[] map;
    SQLiteDatabase db;
    int[][] next_id;
    int current_id=0;//0 denote main level
    int offset=0; // offset=9n
    String sentence1="";

    DBConnection helper= new DBConnection(this);
    Learn learn=new Learn(this,helper);

    private TextToSpeech mTts;
    private static final String TAG = InputActivity.class.getName();

    TGDevice tgDevice;
    BluetoothAdapter btAdapter;
    boolean flag=false;//使button一鍵二用
    final boolean rawEnabled = false;

    //=====================================oncreate===================================================
    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            ThreadPolicy policy = new ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }
        btn_send=(Button)findViewById(R.id.btn_send);
        btn_next=(Button)findViewById(R.id.btn_next);
        btn_lv1=(Button)findViewById(R.id.btn_lv1);
        btn_clear=(Button)findViewById(R.id.btn_clear);
        btn_load=(Button)findViewById(R.id.btn_load);
        btn_mwm=(Button)findViewById(R.id.btn_mwm);
        btn_speech=(Button)findViewById(R.id.btn_speech);

        int[] btnid={R.id.btn1,R.id.btn2,R.id.btn3,R.id.btn4,R.id.btn5,R.id.btn6,R.id.btn7,R.id.btn8,R.id.btn9};
        for(int i=0;i<9;i++)
        {
            btn[i]=(Button)findViewById(btnid[i]);
            currentDatas[i]=new InputData();
        }

        if(!con) {
            btn_send.setText("SPEAK");
            btn_speech.setVisibility(View.GONE);
        }
        status_speech=!con;

        editText=(EditText)findViewById(R.id.editText);
        tv_status=(TextView)findViewById(R.id.sender_status);
        tv_status.setText("");

        btAdapter = BluetoothAdapter.getDefaultAdapter();//取用系統藍芽
        if (btAdapter != null) {
            Toast.makeText(this,"BT Adapter is supported.",Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(this,"BT Adapter is NULL.",Toast.LENGTH_LONG).show();
            btn_mwm.setEnabled(false);
        }
        btn_mwm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!flag){
                    tgDevice = new TGDevice(btAdapter, handler);//相當於建socket及設定處理message的事件
                    try {
                        doStuff(v);
                    }
                    catch (Exception e)
                    {
                        tv_status.setText(e.getMessage());
                    }
                }
                else {
                    tgDevice.close();
                    tv_status.setText("Disonnected.");
                    flag=false;
                }
            }
        });

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
        btn_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status_speech) {
                    Toast.makeText(getApplicationContext(), "開啟語音", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getApplicationContext(), "關閉語音", Toast.LENGTH_SHORT).show();
                }
                status_speech=!status_speech;
            }
        });
        mTts = new TextToSpeech(this,this); //TextToSpeech.OnInitListener

        btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datas=null; //RELEASE
                next_id=null; //RELEASE
                LoadData();
            }
        });
        try{
            LoadData();
        }catch (Exception e){
            System.out.println(e.toString());
        }

        setCurrentDatas(current_id);

        for(int i=0;i<9;i++){
            final int arg = i;
            btn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s=editText.getText().toString() + btn[arg].getText().toString();
                    editText.setText(s);
                    editText.setSelection(s.length());
                    sentence1+=btn[arg].getText().toString();
                    offset=0;
                    current_id=currentDatas[arg].id;
                    setCurrentDatas(current_id);
                }
            });

        }

        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCurrentDatas(current_id);
            }
        });
        btn_lv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                current_id = 0;
                offset = 0;
                setCurrentDatas(current_id);
            }
        });
    }
    //===============================================================================================

    //===================================onstart======================================================
    @Override
    protected void onStart(){
        super.onStart();
        if(con)
            ConnectToDisplay();
    }
    //===============================================================================================

    private void LoadData(){
        long stime=System.currentTimeMillis();
        SQLiteDatabase db = helper.getReadableDatabase();
        //db=SQLiteDatabase.openDatabase("/sdcard/DB/Database.db",null,SQLiteDatabase.OPEN_READWRITE);
        Cursor c=db.rawQuery("SELECT * FROM "+DBConnection.VocSchema.TABLE_NAME+" ORDER BY "+DBConnection.VocSchema.COUNT+" DESC;",null);
        int size=c.getCount();
        if(size>0)
        {
            c.moveToFirst();
            datas=new InputData[size];
            map=new int[size+1];
            next_id=new int[size+1][];
            next_id[0]=new int[size];

            for(int i=0;i<size;i++)
            {
                final int id=Integer.parseInt(c.getString(c.getColumnIndex(DBConnection.VocSchema.ID)));
                next_id[0][i]=id;
                map[id]=i;
                datas[i]=new InputData(c.getString(c.getColumnIndex(DBConnection.VocSchema.CONTENT)),id);
                //==============================
                //LoadRelation(id);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        LoadRelation(id);
                    }
                }).start();
                //==============================
                c.moveToNext();
            }
        }
        c.close();
       long avg=(System.currentTimeMillis()-stime);
        Toast.makeText(this,"載入時間 : "+String.valueOf(avg)+" msec",Toast.LENGTH_SHORT).show();
    }
    
    private void LoadRelation(int id){
        SQLiteDatabase db=helper.getReadableDatabase();
        String query1="select "+DBConnection.RelationSchema.ID2+" from "+DBConnection.RelationSchema.TABLE_NAME
                +" where "+DBConnection.RelationSchema.ID1+" = '"+String.valueOf(id)+"' order by "+DBConnection.RelationSchema.COUNT+" desc;";
        Cursor c=db.rawQuery(query1,null);
        int size=c.getCount();
        next_id[id]=new int[size];
        if(size>0)
        {
            c.moveToFirst();
            for(int i=0;i<size;i++)
            {
                next_id[id][i]=Integer.parseInt(c.getString(0));
                c.moveToNext();
            }
        }
        c.close();
    }

    //===============================================================================================
    private void setCurrentDatas(int id) //OR NEXT PAGE
    {
        int size=next_id[id].length;
        if(size==0)
        {
            offset = 0;
            current_id =id= 0;
            size=next_id[0].length;
        }
        else if(size==1)
        {
            int position=map[next_id[id][0]];
            String str=datas[position].text;
            if(str.equals("#"))
            {
                //sentence1+="#";
                offset = 0;
                current_id =id= 0;
                size=next_id[0].length;
            }
        }
        for(int i=0;i<9;)
        {
            if(offset+i<size)
            {
                int position=map[next_id[id][i+offset]];
                String str=datas[position].text;
                if(str.equals("#"))
                {
                    offset+=1;
                    continue;
                }
                currentDatas[i].text=str;
                currentDatas[i].id=datas[position].id;
            }
            else
            {
                currentDatas[i].id=0;
                currentDatas[i].text="";
            }
            i++;
        }
        offset+=9;
        if(offset > size) {
            offset = 0;
            current_id = 0;
        }
        setBtnText();
    }
    private void setBtnText()
    {
        int count=0;
        for(int i=0;i<9;i++){
            btn[i].setText(currentDatas[i].text);
            if(currentDatas[i].text.equals(""))
            {
                btn[i].setEnabled(false);
                count++;
            }
            else
            btn[i].setEnabled(true);
        }
        if(count==9)
            setCurrentDatas(current_id);
    }

    private void send(){

        tv_status.setText("");
        //要傳送的字串
        String message = editText.getText().toString();
        final String thread_msg=message;
        new Thread(new Runnable() {
            @Override
            public void run() {
                learn.Learning(thread_msg);
            }
        }).start();
        if(status_speech)
            sayHello(message);
        if(con) {
            try {
                //傳送資料
                out.writeUTF(message);
                Toast.makeText(this,"成功傳送!",Toast.LENGTH_SHORT).show();
            } catch (IOException e){
                Toast.makeText(getApplicationContext(), "傳送失敗", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void ConnectToDisplay(){
        try {
            InetAddress serverAddr = null;
            SocketAddress sc_add = null;            //設定Server IP位置
            serverAddr = InetAddress.getByName(IP_SERVER);
            //設定port
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
        if(con)
        {
            try {
                out.close();
                socket.close();
            }catch (Exception e){
                tv_status.setText(e.toString());
            }
        }
    }


    @Override
    protected void onDestroy(){
        super.onDestroy();
        terminate();
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
            status_speech=false;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        terminate();
    }
    //=============================================語音==============================================
    @Override
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            int result;
            result = mTts.setLanguage(Locale.TAIWAN);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Lanuage data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    private void sayHello(String hello) {
        // Select a random hello.
        // Drop allpending entries in the playback queue.
        mTts.speak(hello, TextToSpeech.QUEUE_FLUSH, null);
    }
    //===============================================================================================

    //背景處理所收到的message
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //判斷message的type
            switch (msg.what) {
                //處理連線的message
                case TGDevice.MSG_STATE_CHANGE:
                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            tv_status.setText("Connecting...");
                            break;
                        case TGDevice.STATE_CONNECTED:
                            flag=true;
                            tv_status.setText("Connected.");
                            tgDevice.start();
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            tv_status.setText("Can't find");
                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            tv_status.setText("not paired");
                            break;
                        default:
                            break;
                    }
                    break;

                //專注度
                case TGDevice.MSG_ATTENTION:

                    break;
                //放鬆度
                case TGDevice.MSG_MEDITATION:

                    break;
                //眨眼強度
                case TGDevice.MSG_BLINK:
                    if(msg.arg1>60)
                        setCurrentDatas(current_id);
                    break;
                //未知
                case TGDevice.MSG_RAW_COUNT:
                    //tv.append("Raw Count: " + msg.arg1 + "\n");
                    break;
                //電力不足
                case TGDevice.MSG_LOW_BATTERY:
                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };
    //建連線
    public void doStuff(View view) {
        if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
            tgDevice.connect(rawEnabled);
    }
}
