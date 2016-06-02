package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.neurosky.thinkgear.TGDevice;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Locale;

import static android.os.StrictMode.ThreadPolicy;
import static android.os.StrictMode.setThreadPolicy;

public class InputActivity extends Activity implements TextToSpeech.OnInitListener {
    public static final String IP_SERVER = "192.168.49.1";
    public static int PORT = 8988;
    private DataOutputStream out; //for transfer
    private Socket socket;


    Button btn_send, btn_next, btn_lv1, btn_load, btn_clear, btn_mwm, btn_speech;
    boolean status_speech = false;
    Button[] btn = new Button[9];
    EditText editText;
    TextView tv_status;
    public static boolean con = false;

    InputData[] datas=new InputData[1], currentDatas = new InputData[9];
    int[] map=new int[1];
    //SQLiteDatabase db;
    int[][] next_id=new int[1][1];
    int current_id = 0;//0 denote main level
    int offset = 0; // offset=9n

    DBConnection helper = new DBConnection(this);
    Learn learn = new Learn(this, helper);

    TGDevice tgDevice;
    BluetoothAdapter btAdapter;
    boolean flag = false;//使button一鍵二用
    final boolean rawEnabled = false;

    private Handler uihandler = new Handler();
    private ProgressDialog progressDialog = null;

    CharSequence[] list = new CharSequence[11];
    Spinner spinner;
    Button btn_sentence;

    //=====================================oncreate===================================================
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
        if (Build.VERSION.SDK_INT > 9) {
            ThreadPolicy policy = new ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_next = (Button) findViewById(R.id.btn_next);
        btn_lv1 = (Button) findViewById(R.id.btn_lv1);
        btn_clear = (Button) findViewById(R.id.btn_clear);
        btn_load = (Button) findViewById(R.id.btn_load);
        btn_mwm = (Button) findViewById(R.id.btn_mwm);
        btn_speech = (Button) findViewById(R.id.btn_speech);

        int[] btnid = {R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4, R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9};
        for (int i = 0; i < 9; i++) {
            btn[i] = (Button) findViewById(btnid[i]);
            btn[i].setTextSize(25);//<===========================BTN TEXT SIZE
            btn[i].setEnabled(false);
            currentDatas[i] = new InputData();
        }
        datas[0]=new InputData();

        if (!con) {
            btn_send.setText("TALK");
            btn_speech.setVisibility(View.GONE);
        }
        status_speech = !con;

        editText = (EditText) findViewById(R.id.editText);
        tv_status = (TextView) findViewById(R.id.sender_status);
        tv_status.setText("");

        btn_mwm.setVisibility(View.GONE);
        btAdapter = BluetoothAdapter.getDefaultAdapter();//取用系統藍芽
        if (btAdapter != null) {
            btn_mwm.setVisibility(View.VISIBLE);
        } else {
            btn_mwm.setVisibility(View.GONE);
        }
        btn_mwm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!flag) {
                    tgDevice = new TGDevice(btAdapter, handler);//相當於建socket及設定處理message的事件
                    try {
                        doStuff(v);
                    } catch (Exception e) {
                        tv_status.setText(e.getMessage());
                    }
                } else {
                    tgDevice.close();
                    tv_status.setText("Disonnected.");
                    flag = false;
                }
            }
        });

        btn_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clear();
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
                String text=(status_speech?"關閉語音":"開啟語音");
                btn_speech.setText(text);
                status_speech = !status_speech;
            }
        });

        tw = new TextToSpeech(this,this);
        en = new TextToSpeech(this,this);

        btn_load.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Update();
            }
        });

        for (int i = 0; i < 9; i++) {
            final int arg = i;
            btn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String s = editText.getText().toString();
                    int index=editText.getSelectionStart();
                    String part1=spilt(s,0,index),part2=spilt(s,index,s.length());
                    s=part1+btn[arg].getText().toString()+part2;
                    editText.setText(s);
                    editText.setSelection(part1.length()+btn[arg].getText().toString().length());
                    offset = 0;
                    current_id = currentDatas[arg].id;
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

        btn_sentence=(Button)findViewById(R.id.Button_sentence);
        spinner=(Spinner)findViewById(R.id.Spinner_sentence);
        btn_sentence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sentence();
                ArrayAdapter adapter=new ArrayAdapter<CharSequence>(InputActivity.this, R.layout.myspinner, list);
                adapter.setDropDownViewResource(R.layout.myspinner);
                spinner.setAdapter(adapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        String _content = ((Spinner) parent).getSelectedItem().toString();
                        //editText.postInvalidate();
                        if(_content.length()>0){
                            editText.setText(_content);
                            editText.setSelection(editText.length());
                        }

                    }

                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        });
    }
    //===============================================================================================
    String spilt(String s,int start,int end)
    {
        String tmp="";
        for(int i=start;i<end;i++)
            tmp+=String.valueOf(s.charAt(i));
        return tmp;
    }
    //===================================onstart======================================================
    @Override
    protected void onStart() {
        super.onStart();
        Update();
        if (con)
            ConnectToDisplay();
    }

    //===============================================================================================
    private void Update() {
        try {
            progressDialog = ProgressDialog.show(InputActivity.this, "請稍後", "載入資料中...");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    LoadData(); progressDialog.dismiss();
                }
            }).start();
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    private void LoadData() {
        long stime = System.currentTimeMillis();
        SQLiteDatabase db = helper.getReadableDatabase();
        //db=SQLiteDatabase.openDatabase("/sdcard/DB/Database.db",null,SQLiteDatabase.OPEN_READWRITE);
        Cursor c = db.rawQuery("SELECT * FROM " + DBConnection.VocSchema.TABLE_NAME + " ORDER BY " + DBConnection.VocSchema.COUNT + " DESC;", null);
        int size = c.getCount();
        if (size > 0) {
            c.moveToFirst();
            datas = null; //RELEASE
            next_id = null; //RELEASE
            map=null; //RELEASE
            datas = new InputData[size];
            map = new int[size + 1];
            next_id = new int[size + 1][];
            next_id[0] = new int[size];
            for (int i = 0; i < size; i++) {
                final int id = Integer.parseInt(c.getString(c.getColumnIndex(DBConnection.VocSchema.ID)));
                next_id[0][i] = id;
                map[id] = i;
                datas[i] = new InputData(c.getString(c.getColumnIndex(DBConnection.VocSchema.CONTENT)), id);
                //==============================
                LoadRelation(id);
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
        final long avg = (System.currentTimeMillis() - stime);
        current_id = offset = 0;
        uihandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(InputActivity.this, "載入時間 : " + String.valueOf(avg) + " msec", Toast.LENGTH_SHORT).show();
                setCurrentDatas(current_id);
            }
        });
    }

    private void LoadRelation(int id) {
        SQLiteDatabase db = helper.getReadableDatabase();
        String query1 = "select " + DBConnection.RelationSchema.ID2 + " from " + DBConnection.RelationSchema.TABLE_NAME
                + " where " + DBConnection.RelationSchema.ID1 + " = '" + String.valueOf(id) + "' order by " + DBConnection.RelationSchema.COUNT + " desc;";
        Cursor c = db.rawQuery(query1, null);
        int size = c.getCount();
        next_id[id] = new int[size];
        if (size > 0) {
            c.moveToFirst();
            for (int i = 0; i < size; i++) {
                next_id[id][i] = Integer.parseInt(c.getString(0));
                c.moveToNext();
            }
        }
        c.close();


    }

    //===============================================================================================
    private void setCurrentDatas(int id) //OR NEXT PAGE
    {
        int size = next_id[id].length;
        if (size == 0) {
            offset = 0;
            current_id = id = 0;
            size = next_id[0].length;
        } else if (size == 1) {
            int position = map[next_id[id][0]];
            String str = datas[position].text;
            if (str.equals("#")) {
                offset = 0;
                current_id = id = 0;
                size = next_id[0].length;
            }
        }
        for (int i = 0; i < 9; ) {
            if (offset + i < size) {
                int position = map[next_id[id][i + offset]];
                String str = datas[position].text;
                if (str.equals("#")) {
                    offset += 1;
                    continue;
                }
                currentDatas[i].text = str;
                currentDatas[i].id = datas[position].id;
            } else {
                currentDatas[i].id = 0;
                currentDatas[i].text = "";
            }
            i++;
        }
        offset += 9;
        if (offset > size) {
            offset = 0;
            current_id = 0;
        }
        setBtnText();
    }

    private void setBtnText() {
        int count = 0;
        for (int i = 0; i < 9; i++) {
            btn[i].setText(currentDatas[i].text);
            if (currentDatas[i].text.equals("")) {
                btn[i].setEnabled(false);
                count++;
            } else
                btn[i].setEnabled(true);
        }
        if (count == 9 && current_id != 0)
            setCurrentDatas(current_id);
    }

    private void send() {

        tv_status.setText("");
        //要傳送的字串
        final String message = editText.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                learn.Learning(message);
            }
        }).start();
        if (status_speech && message.length()>0)
            sayHello(" "+message);
        if (con) {
            try {
                //傳送資料
                out.writeUTF(message);
                Toast.makeText(this, "成功傳送!", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "傳送失敗", Toast.LENGTH_SHORT).show();
            }
        }

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

    private void clear(){
        editText.setText("");
        current_id=offset=0;
        setCurrentDatas(current_id);
    }

    private void terminate() {
        if (con) {
            try {
                out.close();
                socket.close();
            } catch (Exception e) {
                tv_status.setText(e.toString());
            }
        }
    }


    @Override
    protected void onDestroy() {
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
        terminate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        terminate();
    }

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
                            flag = true;
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
                    if (msg.arg1 > 60)
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
        if (tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
            tgDevice.connect(rawEnabled);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    private void sentence () {
        String message = editText.getText().toString();
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor c = db.rawQuery("select content from " + DBConnection.SentenceSchema.TABLE_NAME + " where content LIKE '%" + message + "%'  ORDER BY " + DBConnection.SentenceSchema.COUNT + " desc;", null);
        c.moveToFirst();
        int SIZE = c.getCount();
        for (int i = 0; i < 11; i++) {
            if (i > SIZE) {
                list[i] = "";
            } else if (i == 0) {
                list[i] = message;
            } else {
                list[i] = c.getString(0);
                c.moveToNext();
            }
        }
        c.close();
        db.close();
    }

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
       //tw.speak(hello,TextToSpeech.QUEUE_ADD,null);
       String[] msg=new String[50];
        for(int i=0;i<50;i++)
            msg[i]="";
        int previous=0,count=0,speaker=0,current;
        char first=hello.charAt(0);
        if(first>='a' && first<='z' || first>='A' && first<='Z')
            speaker=1;
        for(int i=0;i<hello.length();i++)
        {
            current=0;
            char ch1=hello.charAt(i);
            if(Check.check_eng(ch1))
                current=1;
            else if(Check.check_sign(ch1))
                current=previous;

            if(current!=previous)
            {
                previous=current;
                count++;
            }
            msg[count]+=String.valueOf(ch1);
        }

        for(int i=0;i<=count;i++)
        {
            System.out.println(msg[i]);
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
