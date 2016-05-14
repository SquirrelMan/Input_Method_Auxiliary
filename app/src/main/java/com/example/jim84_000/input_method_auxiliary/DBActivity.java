package com.example.jim84_000.input_method_auxiliary;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class DBActivity extends Activity{

    public static final int _DBVersion = 1; //<-- 版本
    public static final String _DBName="Database.db";
    View.OnClickListener listener_add = null;
    View.OnClickListener listener_update = null;
    View.OnClickListener listener_delete = null;
    View.OnClickListener listener_clear = null;
    View.OnClickListener listener_add2 = null;
    View.OnClickListener listener_update2 = null;
    View.OnClickListener listener_delete2 = null;
    View.OnClickListener listener_clear2 = null;
    Button button_add;
    Button button_update;
    Button button_delete;
    Button button_clear;
    Button button_add2;
    Button button_update2;
    Button button_delete2;
    Button button_clear2;
    DBConnection helper;
    public int id_this;
    public int id_this2;
    public interface VocSchema {
        String TABLE_NAME = "Voc";          //Table Name
        String ID = "_id";                    //ID
        String CONTENT = "content";       //CONTENT
        String COUNT = "count";           //COUNT
    }

    public interface RelationSchema {
        String TABLE_NAME = "Relation";          //Table Name
        String ID = "_id";                    //ID
        String ID1 = "id1";       //ID1
        String ID2 = "id2";           //ID2
        String COUNT = "count";       //COUNT
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.db_manager);
        final EditText mEditText01 = (EditText)findViewById(R.id.EditText01);
        final EditText mEditText02 = (EditText)findViewById(R.id.EditText02);
        final EditText mEditText03 = (EditText)findViewById(R.id.EditText03);

        final EditText mEditText11 = (EditText)findViewById(R.id.EditText11);
        final EditText mEditText12 = (EditText)findViewById(R.id.EditText12);
        final EditText mEditText13 = (EditText)findViewById(R.id.EditText13);
        final EditText mEditText14 = (EditText)findViewById(R.id.EditText14);
        //
        helper = new DBConnection(this);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final String[] FROM_VOC =
                {
                        VocSchema.ID,
                        VocSchema.CONTENT,
                        VocSchema.COUNT
                };
        final String[] FROM_RELATION =
                {
                        RelationSchema.ID,
                        RelationSchema.ID1,
                        RelationSchema.ID2,
                        RelationSchema.COUNT
                };

        Cursor c = db.query(VocSchema.TABLE_NAME, new String[] {VocSchema.CONTENT}, null, null, null, null, null);
        c.moveToFirst();
        CharSequence[] list = new CharSequence[c.getCount()];
        for (int i = 0; i < list.length; i++) {
            list[i] = c.getString(0);
            c.moveToNext();
        }
        c.close();

        Cursor c2 = db.query(RelationSchema.TABLE_NAME, new String[] {RelationSchema.ID}, null, null, null, null, null);
        c2.moveToFirst();
        CharSequence[] list2 = new CharSequence[c2.getCount()];
        for (int i = 0; i < list2.length; i++) {
            list2[i] = c2.getString(0);
            c2.moveToNext();
        }
        c2.close();

        //
        Spinner spinner = (Spinner)findViewById(R.id.Spinner01);
        spinner.setAdapter(new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, list));
        //
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String content = ((Spinner) parent).getSelectedItem().toString();
                Cursor c = db.query("Voc", FROM_VOC, "content='" + content + "'", null, null, null, null);
                c.moveToFirst();
                id_this = Integer.parseInt(c.getString(0));
                String id_thist = c.getString(0);
                String content_this = c.getString(1);
                String count_this = c.getString(2);
                c.close();
                mEditText01.setText(id_thist);
                mEditText02.setText(content_this);
                mEditText03.setText(count_this);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //
        Spinner spinner2 = (Spinner)findViewById(R.id.Spinner11);
        spinner2.setAdapter(new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, list2));
        //
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String ID = ((Spinner) parent).getSelectedItem().toString();
                Cursor c = db.query("Relation", FROM_RELATION, "_id='" + ID + "'", null, null, null, null);
                c.moveToFirst();
                id_this2 = Integer.parseInt(c.getString(0));
                String id_thist = c.getString(0);
                String id1_this = c.getString(1);
                String id2_this = c.getString(2);
                String count_this = c.getString(3);
                c.close();
                mEditText11.setText(id_thist);
                mEditText12.setText(id1_this);
                mEditText13.setText(id2_this);
                mEditText14.setText(count_this);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //
        listener_add = new View.OnClickListener() {
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(VocSchema.ID, mEditText01.getText().toString());
                values.put(VocSchema.CONTENT, mEditText02.getText().toString());
                values.put(VocSchema.COUNT, mEditText03.getText().toString());
                SQLiteDatabase db = helper.getWritableDatabase();
                db.insert(VocSchema.TABLE_NAME, null, values);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_update = new View.OnClickListener() {
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(VocSchema.ID, mEditText01.getText().toString());
                values.put(VocSchema.CONTENT, mEditText02.getText().toString());
                values.put(VocSchema.COUNT, mEditText03.getText().toString());
                String where = VocSchema.ID + " = " + id_this;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.update(VocSchema.TABLE_NAME, values, where ,null);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_delete = new View.OnClickListener() {
            public void onClick(View v) {
                String where = VocSchema.ID + " = " + id_this;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(VocSchema.TABLE_NAME, where ,null);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_clear = new View.OnClickListener() {
            public void onClick(View v) {
                mEditText01.setText("");
                mEditText02.setText("");
                mEditText03.setText("");
            }
        };
        //
        listener_add2 = new View.OnClickListener() {
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(RelationSchema.ID, mEditText11.getText().toString());
                values.put(RelationSchema.ID1, mEditText12.getText().toString());
                values.put(RelationSchema.ID2, mEditText13.getText().toString());
                values.put(RelationSchema.COUNT, mEditText14.getText().toString());
                SQLiteDatabase db = helper.getWritableDatabase();
                db.insert(RelationSchema.TABLE_NAME, null, values);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_update2 = new View.OnClickListener() {
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(RelationSchema.ID, mEditText11.getText().toString());
                values.put(RelationSchema.ID1, mEditText12.getText().toString());
                values.put(RelationSchema.ID2, mEditText13.getText().toString());
                values.put(RelationSchema.COUNT, mEditText14.getText().toString());
                String where = RelationSchema.ID + " = " + id_this2;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.update(RelationSchema.TABLE_NAME, values, where ,null);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_delete2 = new View.OnClickListener() {
            public void onClick(View v) {
                String where = RelationSchema.ID + " = " + id_this2;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(RelationSchema.TABLE_NAME, where ,null);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_clear2 = new View.OnClickListener() {
            public void onClick(View v) {
                mEditText11.setText("");
                mEditText12.setText("");
                mEditText13.setText("");
                mEditText14.setText("");
            }
        };
        //
        button_add = (Button)findViewById(R.id.Button01);
        button_add.setOnClickListener(listener_add);
        button_update = (Button)findViewById(R.id.Button02);
        button_update.setOnClickListener(listener_update);
        button_delete = (Button)findViewById(R.id.Button03);
        button_delete.setOnClickListener(listener_delete);
        button_clear = (Button)findViewById(R.id.Button04);
        button_clear.setOnClickListener(listener_clear);
        //
        button_add2 = (Button)findViewById(R.id.Button11);
        button_add2.setOnClickListener(listener_add2);
        button_update2 = (Button)findViewById(R.id.Button12);
        button_update2.setOnClickListener(listener_update2);
        button_delete2 = (Button)findViewById(R.id.Button13);
        button_delete2.setOnClickListener(listener_delete2);
        button_clear2 = (Button)findViewById(R.id.Button14);
        button_clear2.setOnClickListener(listener_clear2);
    }

    public static class DBConnection extends SQLiteOpenHelper {
        private DBConnection(Context ctx) {
            super(ctx, _DBName,null, _DBVersion);
        }
        public void onCreate(SQLiteDatabase db) {

            String sql = "CREATE TABLE " + VocSchema.TABLE_NAME + " ("
                    + VocSchema.ID  + " INTEGER primary key autoincrement, "
                    + VocSchema.CONTENT + " text not null, "
                    + VocSchema.COUNT + " INTEGER not null" + ");";
            //Log.i("haiyang:createDB=", sql);
            db.execSQL(sql);

            String sql2 = "CREATE TABLE " + RelationSchema.TABLE_NAME + " ("
                    + RelationSchema.ID  + " INTEGER primary key autoincrement, "
                    + RelationSchema.ID1 + " INTEGER not null, "
                    + RelationSchema.ID2 + " INTEGER not null, "
                    + RelationSchema.COUNT + " INTEGER not null" + ");";
            //Log.i("haiyang:createDB=", sql);
            db.execSQL(sql2);
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
        }
    }
}
