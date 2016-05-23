package com.example.jim84_000.input_method_auxiliary;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;

import java.io.File;

public class WelcomePage extends Activity{
    Button goto_startmenu;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);
        goto_startmenu=(Button)findViewById(R.id.btn_goto_start_menu);
        File sdDir = Environment.getExternalStorageDirectory();
        File buildMySpeaker = new File(sdDir, "MySpeaker");
        File builddatabase = new File(sdDir, "MySpeaker/Database");
        File buildlearn = new File(sdDir, "MySpeaker/Learn");
        File buildmain = new File(sdDir, "MySpeaker/Main");
        File buildsub1 = new File(sdDir, "MySpeaker/Sub1");
        File buildsub2 = new File(sdDir, "MySpeaker/Sub2");
        File buildsub3 = new File(sdDir, "MySpeaker/Sub3");
        File buildsub4 = new File(sdDir, "MySpeaker/Sub4");
        isExist(buildMySpeaker.toString());
        isExist(builddatabase.toString());
        isExist(buildlearn.toString());
        isExist(buildmain.toString());
        isExist(buildsub1.toString());
        isExist(buildsub2.toString());
        isExist(buildsub3.toString());
        isExist(buildsub4.toString());


        goto_startmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), StartMenu.class);
                startActivity(intent);
                WelcomePage.this.finish();
            }
        });
    }

    public void isExist(String path) {
        File file = new File(path);
        //判斷文件夾是否存在,如果不存在則建立文件夾
        if (!file.exists()) {
            file.mkdir();
        }
    }
}
