package com.example.jim84_000.input_method_auxiliary;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class StartMenu extends Activity{
    Button[] mode=new Button[6];

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_menu);

        mode[0]=(Button)findViewById(R.id.btn_mode1);
        mode[1]=(Button)findViewById(R.id.btn_mode2);
        mode[2]=(Button)findViewById(R.id.btn_mode3);
        mode[3]=(Button)findViewById(R.id.btn_mode4);
        mode[4]=(Button)findViewById(R.id.btn_mode5);
        mode[5]=(Button)findViewById(R.id.btn_mode6);


        mode[0].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputActivity.con=true;
                SpeechMode.con=true;
                Intent intent =new Intent();
                intent.setClass(getApplicationContext(),WiFiDirectActivity.class);
                startActivity(intent);
                StartMenu.this.finish();
            }
        });

        mode[1].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InputActivity.con=false;
                Intent intent =new Intent();
                intent.setClass(getApplicationContext(),InputActivity.class);
                startActivity(intent);
                //StartMenu.this.finish();
            }
        });

        mode[2].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent =new Intent();
                intent.setClass(getApplicationContext(), DBActivity.class);
                startActivity(intent);
            }
        });

        mode[3].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), DataMove.class);
                startActivity(intent);
            }
        });

        mode[4].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SpeechMode.con=false;
                SpeechMode.path="Main";
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SpeechMode.class);
                startActivity(intent);
            }
        });
        mode[5].setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), Info.class);
                startActivity(intent);
            }
        });
    }
}
