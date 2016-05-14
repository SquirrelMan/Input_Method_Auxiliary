package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.content.ContentValues;
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

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
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
    private DataOutputStream out;
    private Socket socket;
    Button btn_send,btn_next,btn_lv1,btn_load,btn_clear,btn_mwm,btn_speech;
    boolean status_speech=false;
    Button[] btn=new Button[9];
    EditText editText;
    TextView tv_status;
    InputData[][] Data=new InputData[3][18];
    public static boolean con=false;
    int level=0,offset=0; //level=0,1,2 offset=0,9
    protected Dictionary dic;
    private String [] storewordspilt=new String[256];
    private int pointer_storewordspilt=0;
    DBConnection helper= new DBConnection(this);
    public int id_this;
    public int id_this2;

    private TextToSpeech mTts;
    private boolean tw=true;
    private static final String TAG = InputActivity.class.getName();

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
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_client);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            ThreadPolicy policy = new ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }
        System.setProperty("mmseg.dic.path", "./src/HelloChinese/data");
        dic = Dictionary.getInstance();
        clear_storeword_spilt();

        btn_send=(Button)findViewById(R.id.btn_send);
        btn_next=(Button)findViewById(R.id.btn_next);
        btn_lv1=(Button)findViewById(R.id.btn_lv1);
        btn_clear=(Button)findViewById(R.id.btn_clear);
        btn_load=(Button)findViewById(R.id.btn_load);
        btn_mwm=(Button)findViewById(R.id.btn_mwm);
        btn_speech=(Button)findViewById(R.id.btn_speech);

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
        btn_speech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status_speech==false) {
                    status_speech = true;
                    Toast.makeText(getApplicationContext(), "開啟語音", Toast.LENGTH_SHORT).show();
                }
                else {
                    status_speech = false;
                    Toast.makeText(getApplicationContext(), "關閉語音", Toast.LENGTH_SHORT).show();
                }
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

        mTts = new TextToSpeech(this,this); //TextToSpeech.OnInitListener
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
            run(message);
            System.out.println(message);
            int i=1;
            while(check_id_ifexist(i)){
                i++;
            }
            int m=1;
            while(check_idrelation_ifexist(m)){
                m++;
            }
            for(int j = 0 ; j < pointer_storewordspilt+1 ; j++){
                ContentValues values = new ContentValues();
                SQLiteDatabase db = helper.getWritableDatabase();
                String tem_word;
                if(j == pointer_storewordspilt){
                    tem_word="#";
                }
                else{
                    tem_word=storewordspilt[j];
                }
                if(check_voc_ifexist(tem_word)){
                    System.out.println("check_voc_ifexist:" + tem_word);
                    //Cursor c = db.query("Voc", FROM_VOC, "content='" + storewordspilt[j] + "'", null, null, null, null);

                    Cursor c = db.rawQuery("select * from " + VocSchema.TABLE_NAME + " where content='" + tem_word + "'", null);
                    c.moveToFirst();
                    String id_thist = c.getString(0);
                    String content_this = c.getString(1);
                    int count_this = c.getInt(2);
                    c.close();
                    count_this++;
                    values.put(VocSchema.ID, id_thist);
                    values.put(VocSchema.CONTENT, content_this);
                    values.put(VocSchema.COUNT, String.valueOf(count_this));
                    String where = VocSchema.ID+ " = " + id_thist;
                    db.update(VocSchema.TABLE_NAME, values, where, null);
                }
                else{
                    values.put(VocSchema.ID, String.valueOf(i++));
                    values.put(VocSchema.CONTENT, tem_word);
                    values.put(VocSchema.COUNT, String.valueOf(1));
                    db.insert(VocSchema.TABLE_NAME, null, values);
                }
                db.close();
            }
            for(int j = 0 ; j < pointer_storewordspilt ; j++){
                ContentValues values = new ContentValues();
                SQLiteDatabase db = helper.getWritableDatabase();
                String tem_secondword;
                if(j == pointer_storewordspilt-1){
                    tem_secondword="#";
                }
                else{
                    tem_secondword=storewordspilt[j+1];
                }

                if(check_vocrelation_ifexist(storewordspilt[j],tem_secondword)){
                    System.out.println("check_vocrelation_ifexist:" + storewordspilt[j] + tem_secondword);
                    //Cursor c = db.query("Voc", FROM_VOC, "content='" + storewordspilt[j] + "'", null, null, null, null);
                    Cursor c1 = db.rawQuery("select * from " + VocSchema.TABLE_NAME + " where content='" + storewordspilt[j] + "'", null);
                    c1.moveToFirst();
                    String id_c1= c1.getString(0);
                    c1.close();
                    Cursor c2 = db.rawQuery("select * from " + VocSchema.TABLE_NAME + " where content='" + tem_secondword + "'", null);
                    c2.moveToFirst();
                    String id_c2= c2.getString(0);
                    c2.close();

                    Cursor c = db.rawQuery("select * from " + RelationSchema.TABLE_NAME + " where id1='" + id_c1 + "'and " + "id2='" + id_c2 + "'", null);
                    c.moveToFirst();
                    String id_thist = c.getString(0);
                    String id1_this = c.getString(1);
                    String id2_this = c.getString(2);
                    int count_this = c.getInt(3);
                    c.close();
                    count_this++;
                    values.put(RelationSchema.ID, id_thist);
                    values.put(RelationSchema.ID1, id1_this);
                    values.put(RelationSchema.ID2, id2_this);
                    values.put(RelationSchema.COUNT, String.valueOf(count_this));
                    String where = RelationSchema.ID+ " = " + id_thist;
                    db.update(RelationSchema.TABLE_NAME, values, where, null);
                }
                else{
                    System.out.println("check_vocrelation_ifnotexist:" + storewordspilt[j] + tem_secondword);
                    Cursor c1 = db.rawQuery("select * from " + VocSchema.TABLE_NAME + " where content='" + storewordspilt[j] + "'", null);
                    c1.moveToFirst();
                    String id_c1= c1.getString(0);
                    c1.close();
                    Cursor c2 = db.rawQuery("select * from " + VocSchema.TABLE_NAME + " where content='" + tem_secondword + "'", null);
                    c2.moveToFirst();
                    String id_c2= c2.getString(0);
                    c2.close();

                    values.put(RelationSchema.ID, String.valueOf(m++));
                    values.put(RelationSchema.ID1, String.valueOf(id_c1));
                    values.put(RelationSchema.ID2, String.valueOf(id_c2));
                    values.put(RelationSchema.COUNT, String.valueOf(1));
                    db.insert(DBActivity.RelationSchema.TABLE_NAME, null, values);
                }
                db.close();
            }
            clear_storeword_spilt();
        }
        catch (IOException e){
            Toast.makeText(getApplicationContext(), "斷字失敗", Toast.LENGTH_SHORT).show();
        }
        if(status_speech == true)
            sayHello(message);
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
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
            status_speech=false;
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(con)
            terminate();
    }

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
    
    
    
    //斷字系統
    private void clear_storeword_spilt(){
        for(int i = 0 ; i < 256 ; i++){
            storewordspilt[i]="";
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
            }
            String w = word.getString();
            storewordspilt[pointer_storewordspilt]=w;
            pointer_storewordspilt++;
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

    public boolean check_id_ifexist(int tid){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor mCount = db.rawQuery("select count(*) from " + DBActivity.VocSchema.TABLE_NAME + " where _id='" + tid + "'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        System.out.println("id_Count:"+String.valueOf(count));
        if(count >= 1)
            return true;
        else
            return false;
    }
    public boolean check_idrelation_ifexist(int tid){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor mCount = db.rawQuery("select count(*) from " + DBActivity.RelationSchema.TABLE_NAME + " where _id='" + tid + "'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        System.out.println("Relation_id_Count:"+String.valueOf(count));
        if(count >= 1)
            return true;
        else
            return false;
    }
    public boolean check_voc_ifexist(String _content){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor mCount = db.rawQuery("select count(*) from " + DBActivity.VocSchema.TABLE_NAME + " where content='" + _content + "'", null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();
        System.out.println("voc_Count:"+String.valueOf(count));
        if(count >= 1)
            return true;
        else
            return false;
    }
    public boolean check_vocrelation_ifexist(String _content,String _content2){
        SQLiteDatabase db = helper.getWritableDatabase();
        Cursor c1 = db.rawQuery("select * from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + _content + "'", null);
        c1.moveToFirst();
        int id_c1= c1.getInt(0);
        c1.close();
        Cursor c2 = db.rawQuery("select * from " + DBConnection.VocSchema.TABLE_NAME + " where content='" + _content2 + "'", null);
        c2.moveToFirst();
        int id_c2= c2.getInt(0);
        c2.close();
        Cursor mcount = db.rawQuery("select count(*) from " + DBConnection.RelationSchema.TABLE_NAME + " where id1='" + id_c1 + "' and " + "id2='" + id_c2 + "'", null);
        mcount.moveToFirst();
        int count=mcount.getInt(0);
        mcount.close();
        System.out.println("vocrelation_Count:"+String.valueOf(count));
        if(count >= 1)
            return true;
        else
            return false;
    }
}
