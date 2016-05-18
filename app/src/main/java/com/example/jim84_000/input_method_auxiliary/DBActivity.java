package com.example.jim84_000.input_method_auxiliary;


import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class DBActivity extends Activity{

    View.OnClickListener listener_add = null;
    View.OnClickListener listener_update = null;
    View.OnClickListener listener_delete = null;
    View.OnClickListener listener_clear = null;
    View.OnClickListener listener_add2 = null;
    View.OnClickListener listener_update2 = null;
    View.OnClickListener listener_delete2 = null;
    View.OnClickListener listener_clear2 = null;
    View.OnClickListener listener_add3 = null;
    View.OnClickListener listener_update3 = null;
    View.OnClickListener listener_delete3 = null;
    View.OnClickListener listener_clear3 = null;
    Button button_add;
    Button button_update;
    Button button_delete;
    Button button_clear;
    Button button_add2;
    Button button_update2;
    Button button_delete2;
    Button button_clear2;
    Button button_add3;
    Button button_update3;
    Button button_delete3;
    Button button_clear3;
    DBConnection helper;
    public int id_this;
    public int id_this2;
    public int id_this3;
    

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

        final EditText mEditText21 = (EditText)findViewById(R.id.EditText21);
        final EditText mEditText22 = (EditText)findViewById(R.id.EditText22);
        final EditText mEditText23 = (EditText)findViewById(R.id.EditText23);
        //
        helper = new DBConnection(this);
        final SQLiteDatabase db = helper.getWritableDatabase();
        final String[] FROM_VOC =
        {
            DBConnection.VocSchema.ID,
            DBConnection.VocSchema.CONTENT,
            DBConnection.VocSchema.COUNT
        };
        final String[] FROM_RELATION =
        {
            DBConnection.RelationSchema.ID,
            DBConnection.RelationSchema.ID1,
            DBConnection.RelationSchema.ID2,
            DBConnection.RelationSchema.COUNT
        };
        final String[] FROM_SENTENCE =
        {
            DBConnection.SentenceSchema.ID,
            DBConnection.SentenceSchema.CONTENT,
            DBConnection.SentenceSchema.COUNT
        };

        Cursor c = db.query(DBConnection.VocSchema.TABLE_NAME, new String[] {DBConnection.VocSchema.CONTENT}, null, null, null, null, null);
        c.moveToFirst();
        CharSequence[] list = new CharSequence[c.getCount()];
        for (int i = 0; i < list.length; i++) {
            list[i] = c.getString(0);
            c.moveToNext();
        }
        c.close();

        Cursor c2 = db.query(DBConnection.RelationSchema.TABLE_NAME, new String[] {DBConnection.RelationSchema.ID}, null, null, null, null, null);
        c2.moveToFirst();
        CharSequence[] list2 = new CharSequence[c2.getCount()];
        for (int i = 0; i < list2.length; i++) {
            list2[i] = c2.getString(0);
            c2.moveToNext();
        }
        c2.close();

        Cursor c3 = db.query(DBConnection.SentenceSchema.TABLE_NAME, new String[] {DBConnection.SentenceSchema.CONTENT}, null, null, null, null, null);
        c3.moveToFirst();
        CharSequence[] list3 = new CharSequence[c3.getCount()];
        for (int i = 0; i < list3.length; i++) {
            list3[i] = c3.getString(0);
            c3.moveToNext();
        }
        c3.close();

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

        Spinner spinner3 = (Spinner)findViewById(R.id.Spinner21);
        spinner3.setAdapter(new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, list3));
        //
        spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String content = ((Spinner) parent).getSelectedItem().toString();
                Cursor c = db.query("Sentence", FROM_SENTENCE, "content='" + content + "'", null, null, null, null);
                c.moveToFirst();
                id_this3 = Integer.parseInt(c.getString(0));
                String id_thist = c.getString(0);
                String content_this = c.getString(1);
                String count_this = c.getString(2);
                c.close();
                mEditText21.setText(id_thist);
                mEditText22.setText(content_this);
                mEditText23.setText(count_this);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        //
        listener_add = new View.OnClickListener() {
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(DBConnection.VocSchema.ID, mEditText01.getText().toString());
                values.put(DBConnection.VocSchema.CONTENT, mEditText02.getText().toString());
                values.put(DBConnection.VocSchema.COUNT, mEditText03.getText().toString());
                SQLiteDatabase db = helper.getWritableDatabase();
                db.insert(DBConnection.VocSchema.TABLE_NAME, null, values);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_update = new View.OnClickListener() {
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(DBConnection.VocSchema.ID, mEditText01.getText().toString());
                values.put(DBConnection.VocSchema.CONTENT, mEditText02.getText().toString());
                values.put(DBConnection.VocSchema.COUNT, mEditText03.getText().toString());
                String where = DBConnection.VocSchema.ID + " = " + id_this;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.update(DBConnection.VocSchema.TABLE_NAME, values, where ,null);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_delete = new View.OnClickListener() {
            public void onClick(View v) {
                String where = DBConnection.VocSchema.ID + " = " + id_this;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(DBConnection.VocSchema.TABLE_NAME, where ,null);
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
                values.put(DBConnection.RelationSchema.ID, mEditText11.getText().toString());
                values.put(DBConnection.RelationSchema.ID1, mEditText12.getText().toString());
                values.put(DBConnection.RelationSchema.ID2, mEditText13.getText().toString());
                values.put(DBConnection.RelationSchema.COUNT, mEditText14.getText().toString());
                SQLiteDatabase db = helper.getWritableDatabase();
                db.insert(DBConnection.RelationSchema.TABLE_NAME, null, values);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_update2 = new View.OnClickListener() {
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(DBConnection.RelationSchema.ID, mEditText11.getText().toString());
                values.put(DBConnection.RelationSchema.ID1, mEditText12.getText().toString());
                values.put(DBConnection.RelationSchema.ID2, mEditText13.getText().toString());
                values.put(DBConnection.RelationSchema.COUNT, mEditText14.getText().toString());
                String where = DBConnection.RelationSchema.ID + " = " + id_this2;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.update(DBConnection.RelationSchema.TABLE_NAME, values, where ,null);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_delete2 = new View.OnClickListener() {
            public void onClick(View v) {
                String where = DBConnection.RelationSchema.ID + " = " + id_this2;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(DBConnection.RelationSchema.TABLE_NAME, where ,null);
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
        listener_add3 = new View.OnClickListener() {
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(DBConnection.SentenceSchema.ID, mEditText21.getText().toString());
                values.put(DBConnection.SentenceSchema.CONTENT, mEditText22.getText().toString());
                values.put(DBConnection.SentenceSchema.COUNT, mEditText23.getText().toString());
                SQLiteDatabase db = helper.getWritableDatabase();
                db.insert(DBConnection.SentenceSchema.TABLE_NAME, null, values);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_update3 = new View.OnClickListener() {
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put(DBConnection.SentenceSchema.ID, mEditText21.getText().toString());
                values.put(DBConnection.SentenceSchema.CONTENT, mEditText22.getText().toString());
                values.put(DBConnection.SentenceSchema.COUNT, mEditText23.getText().toString());
                String where = DBConnection.SentenceSchema.ID + " = " + id_this3;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.update(DBConnection.SentenceSchema.TABLE_NAME, values, where, null);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_delete3 = new View.OnClickListener() {
            public void onClick(View v) {
                String where = DBConnection.SentenceSchema.ID + " = " + id_this3;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(DBConnection.SentenceSchema.TABLE_NAME, where, null);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //
        listener_clear3 = new View.OnClickListener() {
            public void onClick(View v) {
                mEditText21.setText("");
                mEditText22.setText("");
                mEditText23.setText("");
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

        button_add3 = (Button)findViewById(R.id.Button21);
        button_add3.setOnClickListener(listener_add3);
        button_update3 = (Button)findViewById(R.id.Button22);
        button_update3.setOnClickListener(listener_update3);
        button_delete3 = (Button)findViewById(R.id.Button23);
        button_delete3.setOnClickListener(listener_delete3);
        button_clear3 = (Button)findViewById(R.id.Button24);
        button_clear3.setOnClickListener(listener_clear3);
    }

    
}
