package com.example.jim84_000.input_method_auxiliary;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class InputSet extends Activity {
    private Button button_BacktoInputMenu;
    private Long startTime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.inputset);

        //Bundle data_fromInputMenu=this.getIntent().getExtras();
        //startTime=data_fromInputMenu.getLong("StartT");


        button_BacktoInputMenu = (Button) findViewById(R.id.Button_BacktoInputMenu);
        button_BacktoInputMenu.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(InputSet.this, InputMenu.class);
                //Bundle bundle=new Bundle();
                //bundle.putLong("StartT", startTime);
                //intent.putExtras(bundle);
                startActivity(intent);
                //setResult(RESULT_OK,intent);
                InputSet.this.finish();
            }
        });
    }
}
