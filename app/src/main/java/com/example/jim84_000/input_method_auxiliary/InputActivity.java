package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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
    Spilt spilt=new Spilt(this,helper);

    private TextToSpeech mTts;
    private static final String TAG = InputActivity.class.getName();

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

        if(!con) btn_send.setText("SPEAK");

        editText=(EditText)findViewById(R.id.editText);
        tv_status=(TextView)findViewById(R.id.sender_status);
        tv_status.setText("");

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
                    status_speech = true;
                    Toast.makeText(getApplicationContext(), "開啟語音", Toast.LENGTH_SHORT).show();
                }
                else {
                    status_speech = false;
                    Toast.makeText(getApplicationContext(), "關閉語音", Toast.LENGTH_SHORT).show();
                }
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
                sentence1+="#";
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
        for(int i=0;i<9;i++){
            btn[i].setText(currentDatas[i].text);
        }
    }

    private void send(){

        tv_status.setText("");
        //要傳送的字串
        String message = editText.getText().toString();
        spilt.spilt(sentence1);
        if(!con)
            sayHello(message);
        else {
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
}
