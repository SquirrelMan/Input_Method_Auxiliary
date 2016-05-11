
package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.chenlb.mmseg4j.ComplexSeg;
import com.chenlb.mmseg4j.Dictionary;
import com.chenlb.mmseg4j.MMSeg;
import com.chenlb.mmseg4j.Seg;
import com.chenlb.mmseg4j.Word;
import com.neurosky.thinkgear.TGDevice;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Locale;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import static android.os.StrictMode.setThreadPolicy;

public class InputMenu extends Activity implements TextToSpeech.OnInitListener{
    private static final String TAGSpeech = "TextToSpeechDemo";
    private TextToSpeech mTts;
    private boolean tw=true;
    public static final String IP_SERVER = "192.168.49.143";
    public static int PORT = 8988;
    private DataOutputStream out;
    private Socket socket;
    protected Dictionary dic;

    private static final String TAG = InputMenu.class.getName();
    private static final String FILENAME = "_IMA.txt";
    private static final String FILENAME_SEND="_SEND.txt";
    private static final String FILENAME_DB="_IMA.xls";
    //private Long startTime;
    private String originaldata="";
    private Button button_one;
    private Button button_two;
    private Button button_three;
    private Button button_four;
    private Button button_five;
    private Button button_six;
    private Button button_seven;
    private Button button_eight;
    private Button button_nine;
    private Button button_ten;
    private Button button_eleven;
    private Button button_twelve;
    private Button buttonSend;
    private Button buttonClearInputMessage;
    private Button button_nextpage;
    private Button button_home;
    private EditText inputedittext;
    private String Message="";
    private InputData_back[][] IDB=new InputData_back[10][];
    private int currentlevel;
    private int currentrid;
    private int lastlevel;
    private int lastrid;
    private int lastbutton;
    private int flowid;
    private int MAX_FirstLevel=2;
    private static final int INPUTSET=1;

    BluetoothAdapter bluetoothAdapter;
    TextView tv;
    //Button b;
    TGDevice tgDevice;
    final boolean rawEnabled = false;
    private int eye_strength;
    private int eye_click;

    private ScrollView mindwave_scroll;

    private int []spilt_local=new int[15];
    private int spilt_count=0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inputmenu);
        /*
        try {
            seg_ch=new SegChinese();
            String sentence = seg_ch.run("我是黃瀚勳");
            System.out.println(sentence);
        }
        catch (IOException e) {
            Toast.makeText(getApplicationContext(), "fffff", Toast.LENGTH_SHORT).show();
        }
        */
        System.setProperty("mmseg.dic.path", "./src/HelloChinese/data");
        dic = Dictionary.getInstance();

        createIDBFile();
        tv = (TextView)findViewById(R.id.Mindwave_Read);
        tv.setText("");
        tv.append("Android version: " + Integer.valueOf(android.os.Build.VERSION.SDK) + "\n");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) {
            // Alert user that Bluetooth is not available
            Toast.makeText(this, "Bluetooth not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }else {
        	/* create the TGDevice */
            tgDevice = new TGDevice(bluetoothAdapter, handler);
        }

        mindwave_scroll=(ScrollView)findViewById(R.id.ScrollView_Mindwave_Read);
        eye_strength=0;
        eye_click=0;
        //writeIDBcode();
        for(int i = 0 ; i < 10 ; i++){
            IDB[i]=new InputData_back[100];
            for(int j = 0 ; j < 100 ; j ++){
                IDB[i][j]=new InputData_back();
            }
        }
        currentlevel=1;
        currentrid=1;
        lastlevel=-1;
        lastbutton=-1;
        lastrid=-1;
        flowid=0;

        button_one = (Button)findViewById(R.id.Button_InputMenu_One);
        button_one.getBackground().setAlpha(100);//0~255透明度值

        button_two = (Button)findViewById(R.id.Button_InputMenu_Two);
        button_three = (Button)findViewById(R.id.Button_InputMenu_Three);
        button_four = (Button)findViewById(R.id.Button_InputMenu_Four);
        button_five = (Button)findViewById(R.id.Button_InputMenu_Five);
        button_six = (Button)findViewById(R.id.Button_InputMenu_Six);
        button_seven = (Button)findViewById(R.id.Button_InputMenu_Seven);
        button_eight = (Button)findViewById(R.id.Button_InputMenu_Eight);
        button_nine = (Button)findViewById(R.id.Button_InputMenu_Nine);
        button_ten = (Button)findViewById(R.id.Button_InputMenu_Ten);
        button_eleven = (Button)findViewById(R.id.Button_InputMenu_Eleven);
        button_twelve = (Button)findViewById(R.id.Button_InputMenu_Twelve);
        button_nextpage = (Button)findViewById(R.id.Button_InputMenu_Nextpage);
        button_nextpage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(lastlevel != -1) {
                    int flowlevel = 0, flowrid = 0;
                    if (IDB[lastlevel][lastrid].getSizeofAddressofButton(lastbutton) > 0) {
                        flowid++;
                        flowlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                        flowrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                    }

                    if ((flowlevel > 0 && flowrid > 0) && flowid < IDB[lastlevel][lastrid].getSizeofAddressofButton(1)) {
                        button_one.setText(IDB[flowlevel][flowrid].getTextofButton(1));
                        button_two.setText(IDB[flowlevel][flowrid].getTextofButton(2));
                        button_three.setText(IDB[flowlevel][flowrid].getTextofButton(3));
                        button_four.setText(IDB[flowlevel][flowrid].getTextofButton(4));
                        button_five.setText(IDB[flowlevel][flowrid].getTextofButton(5));
                        button_six.setText(IDB[flowlevel][flowrid].getTextofButton(6));
                        button_seven.setText(IDB[flowlevel][flowrid].getTextofButton(7));
                        button_eight.setText(IDB[flowlevel][flowrid].getTextofButton(8));
                        button_nine.setText(IDB[flowlevel][flowrid].getTextofButton(9));
                        button_ten.setText(IDB[flowlevel][flowrid].getTextofButton(10));
                        button_eleven.setText(IDB[flowlevel][flowrid].getTextofButton(11));
                        button_twelve.setText(IDB[flowlevel][flowrid].getTextofButton(12));
                        currentrid=flowrid;
                    } else if (flowid == IDB[lastlevel][lastrid].getSizeofAddressofButton(1)) {
                        flowid = 0;
                        flowlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                        flowrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                        button_one.setText(IDB[flowlevel][flowrid].getTextofButton(1));
                        button_two.setText(IDB[flowlevel][flowrid].getTextofButton(2));
                        button_three.setText(IDB[flowlevel][flowrid].getTextofButton(3));
                        button_four.setText(IDB[flowlevel][flowrid].getTextofButton(4));
                        button_five.setText(IDB[flowlevel][flowrid].getTextofButton(5));
                        button_six.setText(IDB[flowlevel][flowrid].getTextofButton(6));
                        button_seven.setText(IDB[flowlevel][flowrid].getTextofButton(7));
                        button_eight.setText(IDB[flowlevel][flowrid].getTextofButton(8));
                        button_nine.setText(IDB[flowlevel][flowrid].getTextofButton(9));
                        button_ten.setText(IDB[flowlevel][flowrid].getTextofButton(10));
                        button_eleven.setText(IDB[flowlevel][flowrid].getTextofButton(11));
                        button_twelve.setText(IDB[flowlevel][flowrid].getTextofButton(12));
                        currentrid=flowrid;
                    }
                }
                else{
                    currentrid++;
                    if(currentrid<=MAX_FirstLevel) {
                        button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                        button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                        button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                        button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                        button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                        button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                        button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                        button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                        button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                        button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                        button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                        button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                    }
                    else{
                        currentrid=1;
                        button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                        button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                        button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                        button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                        button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                        button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                        button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                        button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                        button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                        button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                        button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                        button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                    }
                }
            }
        });
        button_one.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                /*
                button_one.setText("好美麗");
                String textToSaveString = button_one.getText().toString();
                writeToFile(textToSaveString);
                String textFromFileString = readFromFile();
                Toast.makeText(getApplicationContext(),textFromFileString, Toast.LENGTH_SHORT).show();*/
                lastbutton = 1;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_two.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 2;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_three.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 3;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_four.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 4;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_five.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 5;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_six.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 6;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_seven.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 7;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_eight.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 8;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_nine.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 9;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_ten.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 10;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_eleven.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 11;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        button_twelve.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                lastbutton = 12;
                lastlevel = currentlevel;
                lastrid = currentrid;
                storemessage(IDB[lastlevel][lastrid].getTextofButton(lastbutton));
                currentlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                currentrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                flowid = 0;
                if (currentlevel > 0 && currentrid > 0) {
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                } else {
                    currentlevel = 1;
                    currentrid = 1;
                    lastlevel = -1;
                    lastbutton = -1;
                    lastrid = -1;
                    button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                    button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                    button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                    button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                    button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                    button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                    button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                    button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                    button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                    button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                    button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                    button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                }
            }
        });
        buttonSend = (Button)findViewById(R.id.ButtonSend);
        buttonSend.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendmessage();
                /*
                Intent intent=new Intent();
                //intent.setClass(InputMenu.this, InputSet.class);
                intent.setClass(InputMenu.this, BluetoothActivity.class);
                //Bundle bundle=new Bundle();
                //bundle.putLong("StartT", startTime);
                //intent.putExtras(bundle);
                startActivity(intent);
                //InputMenu.this.finish();*/
            }
        });
        buttonClearInputMessage = (Button)findViewById(R.id.ButtonClearInputTextView);
        buttonClearInputMessage.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearmessage();
            }
        });
        button_home = (Button)findViewById(R.id.Button_InputMenu_Home);
        button_home.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentlevel=1;
                currentrid=1;
                lastlevel=-1;
                lastbutton=-1;
                lastrid=-1;
                flowid=0;

                button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
            }
        });
        inputedittext=(EditText)findViewById(R.id.EditText_Input);
        inputedittext.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0) {
                    Message = inputedittext.getText().toString();
                }
            }
        });

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            setThreadPolicy(policy);
        }

        try {
            InetAddress serverAddr = null;
            SocketAddress sc_add = null;            //設定Server IP位置
            serverAddr = InetAddress.getByName(IP_SERVER);
            //設定port:1234
            sc_add = new InetSocketAddress(serverAddr, PORT);
            socket = new Socket();
            //與Server連線，timeout時間2秒
            socket.connect(sc_add, 2000);
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "建立socket失敗", Toast.LENGTH_SHORT).show();
        }

        mTts = new TextToSpeech(this,this); //TextToSpeech.OnInitListener
    }

    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case INPUTSET:
                startTime=data.getExtras().getLong("StartT");
        }
    }*/
    //讀取data，並建構資料庫
    @Override
    public void onStart(){
        super.onStart();
        // put your code here...
        /*
        String textToSaveString="L01B1";
        writeToFile(textToSaveString);
        String textFromFileString = readFromFile();
        textFromFileString.substring(1,2);*/
        readandbuildIDB();
        currentlevel=1;
        currentrid=1;
        lastlevel=-1;
        lastbutton=-1;
        lastrid=-1;
        flowid=0;

        button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
        button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
        button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
        button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
        button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
        button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
        button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
        button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
        button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
        button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
        button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
        button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
        Message="";
        inputedittext.setText("");
    }

    @Override
    public void onResume(){
        super.onResume();
        // put your code here...
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            out.close();
            socket.close();
            tgDevice.close();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
        // Don't forget to shutdown!
        if (mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }

    public void onPause() {
        super.onPause();
        try {
            tgDevice.close();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * Handles messages from TGDevice
     */
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            mindwave_scroll.fullScroll(View.FOCUS_DOWN);
            switch (msg.what) {
                case TGDevice.MSG_STATE_CHANGE:

                    switch (msg.arg1) {
                        case TGDevice.STATE_IDLE:
                            break;
                        case TGDevice.STATE_CONNECTING:
                            tv.append("Connecting...\n");
                            break;
                        case TGDevice.STATE_CONNECTED:
                            tv.append("Connected.\n");
                            tgDevice.start();
                            break;
                        case TGDevice.STATE_NOT_FOUND:
                            tv.append("Can't find\n");
                            break;
                        case TGDevice.STATE_NOT_PAIRED:
                            tv.append("not paired\n");
                            break;
                        case TGDevice.STATE_DISCONNECTED:
                            tv.append("Disconnected mang\n");
                    }

                    break;
                case TGDevice.MSG_POOR_SIGNAL:
                    //signal = msg.arg1;
                    tv.append("PoorSignal: " + msg.arg1 + "\n");
                    eye_click=0;
                    break;
                case TGDevice.MSG_RAW_DATA:
                    //raw1 = msg.arg1;
                    //tv.append("Got raw: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_HEART_RATE:
                    tv.append("Heart rate: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_ATTENTION:
                    //att = msg.arg1;
                    tv.append("Attention: " + msg.arg1 + "\n");
                    //Log.v("HelloA", "Attention: " + att + "\n");
                    break;
                case TGDevice.MSG_MEDITATION:

                    break;
                case TGDevice.MSG_BLINK:
                    tv.append("Blink: " + msg.arg1 + "\n");
                    eye_strength=msg.arg1;
                    break;
                case TGDevice.MSG_RAW_COUNT:
                    //tv.append("Raw Count: " + msg.arg1 + "\n");
                    break;
                case TGDevice.MSG_LOW_BATTERY:
                    Toast.makeText(getApplicationContext(), "Low battery!", Toast.LENGTH_SHORT).show();
                    break;
                case TGDevice.MSG_RAW_MULTI:
                    //TGRawMulti rawM = (TGRawMulti)msg.obj;
                    //tv.append("Raw1: " + rawM.ch1 + "\nRaw2: " + rawM.ch2);
                default:
                    break;
            }
            if(eye_strength>=40) {
               if (lastlevel != -1) {
                    int flowlevel = 0, flowrid = 0;
                    if (IDB[lastlevel][lastrid].getSizeofAddressofButton(lastbutton) > 0) {
                        flowid++;
                        flowlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                        flowrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                    }

                    if ((flowlevel > 0 && flowrid > 0) && flowid < IDB[lastlevel][lastrid].getSizeofAddressofButton(1)) {
                        button_one.setText(IDB[flowlevel][flowrid].getTextofButton(1));
                        button_two.setText(IDB[flowlevel][flowrid].getTextofButton(2));
                        button_three.setText(IDB[flowlevel][flowrid].getTextofButton(3));
                        button_four.setText(IDB[flowlevel][flowrid].getTextofButton(4));
                        button_five.setText(IDB[flowlevel][flowrid].getTextofButton(5));
                        button_six.setText(IDB[flowlevel][flowrid].getTextofButton(6));
                        button_seven.setText(IDB[flowlevel][flowrid].getTextofButton(7));
                        button_eight.setText(IDB[flowlevel][flowrid].getTextofButton(8));
                        button_nine.setText(IDB[flowlevel][flowrid].getTextofButton(9));
                        button_ten.setText(IDB[flowlevel][flowrid].getTextofButton(10));
                        button_eleven.setText(IDB[flowlevel][flowrid].getTextofButton(11));
                        button_twelve.setText(IDB[flowlevel][flowrid].getTextofButton(12));
                        currentrid = flowrid;
                    } else if (flowid == IDB[lastlevel][lastrid].getSizeofAddressofButton(1)) {
                        flowid = 0;
                        flowlevel = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) / 100;
                        flowrid = (IDB[lastlevel][lastrid].getoneAddressofButton(1, 0 + flowid)) % 100;
                        button_one.setText(IDB[flowlevel][flowrid].getTextofButton(1));
                        button_two.setText(IDB[flowlevel][flowrid].getTextofButton(2));
                        button_three.setText(IDB[flowlevel][flowrid].getTextofButton(3));
                        button_four.setText(IDB[flowlevel][flowrid].getTextofButton(4));
                        button_five.setText(IDB[flowlevel][flowrid].getTextofButton(5));
                        button_six.setText(IDB[flowlevel][flowrid].getTextofButton(6));
                        button_seven.setText(IDB[flowlevel][flowrid].getTextofButton(7));
                        button_eight.setText(IDB[flowlevel][flowrid].getTextofButton(8));
                        button_nine.setText(IDB[flowlevel][flowrid].getTextofButton(9));
                        button_ten.setText(IDB[flowlevel][flowrid].getTextofButton(10));
                        button_eleven.setText(IDB[flowlevel][flowrid].getTextofButton(11));
                        button_twelve.setText(IDB[flowlevel][flowrid].getTextofButton(12));
                        currentrid = flowrid;
                    }
               }
               else {
                    currentrid++;
                    if (currentrid <= MAX_FirstLevel) {
                        button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                        button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                        button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                        button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                        button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                        button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                        button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                        button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                        button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                        button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                        button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                        button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                    } else {
                        currentrid = 1;
                        button_one.setText(IDB[currentlevel][currentrid].getTextofButton(1));
                        button_two.setText(IDB[currentlevel][currentrid].getTextofButton(2));
                        button_three.setText(IDB[currentlevel][currentrid].getTextofButton(3));
                        button_four.setText(IDB[currentlevel][currentrid].getTextofButton(4));
                        button_five.setText(IDB[currentlevel][currentrid].getTextofButton(5));
                        button_six.setText(IDB[currentlevel][currentrid].getTextofButton(6));
                        button_seven.setText(IDB[currentlevel][currentrid].getTextofButton(7));
                        button_eight.setText(IDB[currentlevel][currentrid].getTextofButton(8));
                        button_nine.setText(IDB[currentlevel][currentrid].getTextofButton(9));
                        button_ten.setText(IDB[currentlevel][currentrid].getTextofButton(10));
                        button_eleven.setText(IDB[currentlevel][currentrid].getTextofButton(11));
                        button_twelve.setText(IDB[currentlevel][currentrid].getTextofButton(12));
                    }
                }
                eye_strength = 0;
            }
        }
    };


    //把data存入File
    private void writeToFile(String data) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            path.mkdir();
            // create the file in which we will write the contents
            File myFile = new File(path, FILENAME);
            FileOutputStream fOut = new FileOutputStream(myFile,false); //append:true
            //FileOutputStream fOut = new FileOutputStream(myFile);
            BufferedWriter myWriter = new BufferedWriter(new OutputStreamWriter(fOut));
            myWriter.write(data);;
            //myWriter.append(data.subSequence(0,data.length()));
            myWriter.newLine();
            myWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }

    }
    //把data存入File_SEND
    private void writeToFileSEND(String data) {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            path.mkdir();
            // create the file in which we will write the contents
            File myFile = new File(path, FILENAME_SEND);
            FileOutputStream fOut = new FileOutputStream(myFile,false); //append:true
            //FileOutputStream fOut = new FileOutputStream(myFile);
            BufferedWriter myWriter = new BufferedWriter(new OutputStreamWriter(fOut));
            myWriter.write(data);
            //myWriter.append(data.subSequence(0,data.length()));
            myWriter.newLine();
            myWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, "File write failed: " + e.toString());
        }

    }
    //從File讀取data
    private String readFromFile() {
        String ret = "";
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            path.mkdir();
            // create the file in which we will write the contents
            File myFile = new File(path, FILENAME);
            FileInputStream fIn = new FileInputStream(myFile);
            BufferedReader myReader = new BufferedReader(new InputStreamReader(fIn));
            String aDataRow = "";
            String aBuffer = "";
            while ((aDataRow = myReader.readLine()) != null) {
                aBuffer += aDataRow + "\n";
            }
            ret=aBuffer;
            myReader.close();
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
        return ret;
    }
    //string to int
    private int ChartoInt_onebyte(char onebyte){
        return onebyte-48;
    }
    //建立資料庫
    private void readandbuildIDB(){
        originaldata = readFromFile();

        for(int i = 0 ; i < originaldata.length() ; i++){
            if(originaldata.charAt(i)=='L'){
                i++;
                int level=ChartoInt_onebyte(originaldata.charAt(i));
                i+=2;
                int rid=ChartoInt_onebyte(originaldata.charAt(i));
                i++;
                rid=rid*10+ChartoInt_onebyte(originaldata.charAt(i));
                (IDB[level][rid]).setLevel(level);
                (IDB[level][rid]).setRoot_ID(rid);
                i+=2;
                int button_one_end = originaldata.indexOf("B2", i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i + 1, button_one_end), 1);
                i=button_one_end+2;
                int button_two_end=originaldata.indexOf("B3",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_two_end), 2);
                i=button_two_end+2;
                int button_three_end=originaldata.indexOf("B4",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_three_end), 3);
                i=button_three_end+2;
                int button_four_end=originaldata.indexOf("B5",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_four_end), 4);
                i=button_four_end+2;
                int button_five_end=originaldata.indexOf("B6",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_five_end), 5);
                i=button_five_end+2;
                int button_six_end=originaldata.indexOf("B7",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_six_end), 6);
                i=button_six_end+2;
                int button_seven_end=originaldata.indexOf("B8",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_seven_end), 7);
                i=button_seven_end+2;
                int button_eight_end=originaldata.indexOf("B9",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_eight_end), 8);
                i=button_eight_end+2;
                int button_nine_end=originaldata.indexOf("B10",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_nine_end), 9);
                i=button_nine_end+3;
                int button_ten_end=originaldata.indexOf("B11",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_ten_end), 10);
                i=button_ten_end+3;
                int button_eleven_end=originaldata.indexOf("B12",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_eleven_end), 11);
                i=button_eleven_end+3;
                int button_twelve_end=originaldata.indexOf("A1",i);
                (IDB[level][rid]).setTextofButton(originaldata.substring(i, button_twelve_end-1), 12);
                i=button_twelve_end;
                for(int j = 1 ; j <= 12 ; j++){
                    if(originaldata.charAt(i)=='A'){
                        int i_tem=i+1;
                        int finial=originaldata.indexOf("\nA",i_tem)+1;
                        int tem=originaldata.indexOf("\nL",i_tem)+1;
                        if(finial == -1 && tem == -1){
                            finial=originaldata.length();
                        }
                        else if(finial>tem)
                            finial=tem;
                        int check=originaldata.indexOf("C000",i_tem);
                        if(check != -1 && check < finial){
                            (IDB[level][rid]).setSizeofAddressofButton(0,j);
                        }
                        else{
                            if(j<=9) {
                                int amount = (finial - i - 3) / 4;
                                int[] address = new int[256];
                                for (int k = 0; k < amount; k++) {
                                    address[k] = ChartoInt_onebyte(originaldata.charAt(i + 3 + 4 * k)) * 100 + ChartoInt_onebyte(originaldata.charAt(i + 4 + 4 * k)) * 10 + ChartoInt_onebyte(originaldata.charAt(i + 5 + 4 * k));
                                }
                                (IDB[level][rid]).setAddressofButton(j, address, amount);
                            }
                            else{
                                int amount = (finial - i - 4) / 4;
                                int[] address = new int[256];
                                for (int k = 0; k < amount; k++) {
                                    address[k] = ChartoInt_onebyte(originaldata.charAt(i + 4 + 4 * k)) * 100 + ChartoInt_onebyte(originaldata.charAt(i + 5 + 4 * k)) * 10 + ChartoInt_onebyte(originaldata.charAt(i + 6 + 4 * k));
                                }
                                (IDB[level][rid]).setAddressofButton(j, address, amount);
                            }
                        }
                        i=finial;
                        if(i == originaldata.length()){
                            i--;
                            break;
                        }
                    }
                }
            }
        }
/*
        for(int i = 1 ; i < 10 ; i++){
            for(int j = 1 ; j < 100 ; j++) {
                if(IDB[i][j].getLevel()>0) {
                    String buf="";
                    buf="Level:" + String.valueOf((IDB[i][j]).getLevel()) +
                            "R_ID:" + String.valueOf((IDB[i][j]).getRoot_ID()) +
                            "B1:" + (IDB[i][j]).getTextofButton(1) +
                            "B2:" + (IDB[i][j]).getTextofButton(2) +
                            "B3:" + (IDB[i][j]).getTextofButton(3) +
                            "B4:" + (IDB[i][j]).getTextofButton(4) +
                            "B5:" + (IDB[i][j]).getTextofButton(5) +
                            "B6:" + (IDB[i][j]).getTextofButton(6) +
                            "B7:" + (IDB[i][j]).getTextofButton(7) +
                            "B8:" + (IDB[i][j]).getTextofButton(8) +
                            "B9:" + (IDB[i][j]).getTextofButton(9) + "\n";
                    for(int k = 1 ; k <=9 ; k++){
                        buf+=("A"+String.valueOf(k)+":");
                        for(int m=0;m < IDB[i][j].getSizeofAddressofButton(k);m++){
                            buf+=("C"+String.valueOf(IDB[i][j].getoneAddressofButton(k,m)));
                        }
                        if (IDB[i][j].getSizeofAddressofButton(k)==0){
                            buf+="C000";
                        }
                        buf+="\n";
                    }
                    Toast.makeText(getApplicationContext(), buf, Toast.LENGTH_SHORT).show();
                }
                else
                    break;
            }
        }*/
    }
    void writeIDBcode(){
        String data = "L1R01B1我B2你B3他B4小姐B5老師B6同學B7姊姊B8爸爸B9媽媽B10我們B11你們B12他們\n" +
                "A1C201C202C203\n" +
                "A2C201C202C203\n" +
                "A3C201C202C203\n" +
                "A4C201C202C203\n" +
                "A5C201C202C203\n" +
                "A6C201C202C203\n" +
                "A7C201C202C203\n" +
                "A8C201C202C203\n" +
                "A9C201C202C203\n" +
                "A10C201C202C203\n" +
                "A11C201C202C203\n" +
                "A12C201C202C203\n" +
                "L1R02B1星期一B2星期二B3星期三B4星期四B5星期五B6星期六B7星期日B8上B9下B10明天B11今天B12後天\n" +
                "A1C201C202C203\n" +
                "A2C201C202C203\n" +
                "A3C201C202C203\n" +
                "A4C201C202C203\n" +
                "A5C201C202C203\n" +
                "A6C201C202C203\n" +
                "A7C201C202C203\n" +
                "A8C000\n" +
                "A9C000\n" +
                "A10C201C202C203\n" +
                "A11C201C202C203\n" +
                "A12C201C202C203\n" +
                "L2R01B1是B2不是B3要B4不要B5會B6不會B7有B8沒有B9去B10不去B11知道B12不知道\n" +
                "A1C301C302\n" +
                "A2C301C302\n" +
                "A3C301C302\n" +
                "A4C301C302\n" +
                "A5C301C302\n" +
                "A6C301C302\n" +
                "A7C301C302\n" +
                "A8C301C302\n" +
                "A9C301C302\n" +
                "A10C301C302\n" +
                "A11C301C302\n" +
                "A12C301C302\n" +
                "L2R02B1想B2不想B3好B4不好B5可以B6不可以B7是不是B8要不要B9會不會B10有沒有B11去不去B12知不知道\n" +
                "A1C301C302\n" +
                "A2C301C302\n" +
                "A3C301C302\n" +
                "A4C301C302\n" +
                "A5C301C302\n" +
                "A6C301C302\n" +
                "A7C301C302\n" +
                "A8C301C302\n" +
                "A9C301C302\n" +
                "A10C301C302\n" +
                "A11C301C302\n" +
                "A12C301C302\n" +
                "L2R03B1開心B2不開心B3傷心B4不傷心B5生氣B6不生氣B7內心OSB8煩惱B9超開心B10好爽B11好累B12不想講話\n" +
                "A1C000\n" +
                "A2C000\n" +
                "A3C000\n" +
                "A4C000\n" +
                "A5C000\n" +
                "A6C000\n" +
                "A7C000\n" +
                "A8C000\n" +
                "A9C000\n" +
                "A10C000\n" +
                "A11C000\n" +
                "A12C000\n" +
                "L3R01B1吃飯B2睡覺B3寫作業B4用平板B5用電腦B6用手機B7上廁所B8需要幫忙B9說B10喝水B11頭痛B12腳抽筋\n" +
                "A1C000\n" +
                "A2C000\n" +
                "A3C000\n" +
                "A4C000\n" +
                "A5C000\n" +
                "A6C000\n" +
                "A7C000\n" +
                "A8C000\n" +
                "A9C000\n" +
                "A10C000\n" +
                "A11C000\n" +
                "A12C000\n" +
                "L3R02B1問問題B2再講一次嗎B3說錯B4沒事B5交作業B6弄椅子B7弄桌子B8開燈B9用衛生紙B10看動畫B11看小說B12看電視\n" +
                "A1C000\n" +
                "A2C000\n" +
                "A3C000\n" +
                "A4C000\n" +
                "A5C000\n" +
                "A6C000\n" +
                "A7C000\n" +
                "A8C000\n" +
                "A9C000\n" +
                "A10C000\n" +
                "A11C000\n" +
                "A12C000\n";
        writeToFile(data);
    }
    private void storemessage(String mes){
        Message+=mes;
        inputedittext.setText(Message);
    }
    private void sendmessage(){
        /*
        writeToFileSEND(Message);
        Message="";
        inputedittext.setText("");*/


        try {
            //傳送資料
            sayHello();
            out.writeUTF(Message);
            Message="";
            inputedittext.setText("");
            Toast.makeText(this,"成功傳送!",Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "傳送失敗", Toast.LENGTH_SHORT).show();
            this.finish();
        }
    }
    private void clearmessage(){
        Message="";
        inputedittext.setText("");
    }

    public void doStuff(View view) {
        if(tgDevice.getState() != TGDevice.STATE_CONNECTING && tgDevice.getState() != TGDevice.STATE_CONNECTED)
            tgDevice.connect(rawEnabled);
        //tgDevice.ena
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

    private void sayHello() {
        // Select a random hello.
        // Drop allpending entries in the playback queue.
        mTts.speak(Message, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void createIDBFile() {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            path.mkdir();
            // create the file in which we will write the contents
            File myFile = new File(path, FILENAME_DB);
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("UTF8");
            WritableWorkbook workbook = Workbook.createWorkbook(myFile, ws);
            WritableSheet sheet_ChineseC = workbook.createSheet("Chinese_Charater", 0);  //工作表名稱
            WritableSheet sheet_CombineCC = workbook.createSheet("Combine_ChineseC", 1);  //工作表名稱

            jxl.write.Label label = new jxl.write.Label(0,0,"");

            label.setString("ID");
            sheet_ChineseC.addCell(label.copyTo(0, 0));
            label.setString("Content");
            sheet_ChineseC.addCell(label.copyTo(1, 0));
            label.setString("Count");
            sheet_ChineseC.addCell(label.copyTo(2, 0));

            String sentence=run("我是黃瀚勳");
            System.out.println(sentence);
            label.setString(sentence.substring(0, spilt_local[0]));
            System.out.println(sentence.substring(0, spilt_local[0]));
            sheet_ChineseC.addCell(label.copyTo(0, 1));

            label.setString(sentence.substring(spilt_local[0]+1,spilt_local[1]));
            System.out.println(sentence.substring(spilt_local[0]+1,spilt_local[1]));
            sheet_ChineseC.addCell(label.copyTo(1, 1));

            label.setString("ID1");
            sheet_CombineCC.addCell(label.copyTo(0, 0));
            label.setString("ID2");
            sheet_CombineCC.addCell(label.copyTo(1, 0));
            label.setString("Count");
            sheet_CombineCC.addCell(label.copyTo(2,0));


            workbook.write();
            workbook.close();
        }
        catch(IOException e) {
            System.out.println(e.toString());
        }
        catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    public void readIDBFile() {
        try {
            File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            path.mkdir();
            // create the file in which we will write the contents
            File myFile = new File(path, FILENAME_DB);
            WorkbookSettings ws = new WorkbookSettings();
            ws.setEncoding("UTF8");
            Workbook book = Workbook.getWorkbook(myFile, ws);
            Sheet sheet = book.getSheet(0);
            for(int i = 1 ; i < sheet.getColumns() ; i++){
                Cell[] cella = sheet.getRow(i);
                for(Cell tmp:cella){
                    tmp.getContents();
                }
            }

            /*
            //讀取第0個工作表
            int rows = sheet.getRows() ;
            //取得總筆數(行數)(直)
            System.out.println("rows = " + rows );
            int columns = sheet.getColumns();
            //取得總共欄位數(列數)(橫)
            System.out.println("Columns = " + columns );
            */
            //Cell[] cella = sheet.getRow(0);
            // ****Cell tmp=sheet.findCell("20");
            //取得第一筆資料的所有內容
            //for(Cell tmp:cella) {
            //****System.out.printf(tmp.getRow()+"\t\t"+tmp.getColumn());                       //輸出內容
            //}
            //****Cell cellb = sheet.getCell(1, 0);
            //輸出指定的位置
            //*****String result = cellb.getContents();
            // 獲得單元格的內容
            //*****System.out.println("\n" + result);
            book.close();   //關閉
        }
        catch(IOException e) {
            System.out.println(e.toString());
        }
        catch(Exception e) {
            System.out.println(e.toString());
        }
    }

    /*
    private Drawable loadImageFromURL(String url){
        try{
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable draw = Drawable.createFromStream(is, "src");
            return draw;
        }catch (Exception e) {
            //TODO handle error
            Log.i("loadingImg", e.toString());
            return null;
        }
    }*/
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
                spilt_local[spilt_count]=sb.length();
                sb.append(wordSpilt);
                spilt_count++;
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

