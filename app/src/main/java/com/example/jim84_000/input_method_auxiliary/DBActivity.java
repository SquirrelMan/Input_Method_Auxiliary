package com.example.jim84_000.input_method_auxiliary;


import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class DBActivity extends Activity{

    DBHelper mDBHelper;
    TextView tv,tv_time;
    Button btnAdd;
    int[] nextid=new int[9];
    int count=0;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);
        tv=(TextView)findViewById(R.id.tv_data);
        tv_time=(TextView)findViewById(R.id.tv_time);
        btnAdd=(Button)findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                long stime=System.currentTimeMillis();
                addData(String.valueOf(count));
                readDB();
                long ptime=System.currentTimeMillis()-stime;
                tv_time.setText(String.valueOf(ptime) + " msec");
            }
        });
        long stime=System.currentTimeMillis();
        openDB();
        readDB();
        long ptime=System.currentTimeMillis()-stime;
        tv_time.setText(String.valueOf(ptime)+" msec");
    }

    private void openDB(){
        mDBHelper=new DBHelper(getApplicationContext());
    }

    private void readDB(){
        SQLiteDatabase db=mDBHelper.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+DBHelper._TableName+" ORDER BY _ID ASC;", null);
        String data="";
        int size=c.getCount();
        if(size>0){
            c.moveToFirst();
            for(int i=0;i<size;i++){
                data+="(";
                for(int j=0;j<12;j++){
                    data+=c.getString(j);
                    if(j<11)
                        data+=",";
                }
                data+=")\n";
                c.moveToNext();
            }
        }
        tv.setText(data);
        c.close();
    }

    private void addData(String vocabulary){
        SQLiteDatabase db=mDBHelper.getWritableDatabase();
        ContentValues values=new ContentValues();

        values.put("Vocabulary",vocabulary);
        values.put("Weight",1);
        values.put("NEXT_1",0);
        values.put("NEXT_2",0);
        values.put("NEXT_3",0);
        values.put("NEXT_4",0);
        values.put("NEXT_5",0);
        values.put("NEXT_6",0);
        values.put("NEXT_7",0);
        values.put("NEXT_8",0);
        values.put("NEXT_9", 0);

        try{
            long newid=db.insert(DBHelper._TableName,null,values);
            System.out.println(newid);
        }catch (Exception e)
        {
            Toast.makeText(this,"Insert Failed!",Toast.LENGTH_SHORT).show();
        }

    }
}
