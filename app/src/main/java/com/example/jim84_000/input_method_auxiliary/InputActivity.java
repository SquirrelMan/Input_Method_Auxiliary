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
    Button button_add;
    Button button_update;
    Button button_delete;
    Button button_clear;
    DBConnection helper;
    public int id_this;
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
        //�إ߸�ƮwPhoneBookDB�M���Table:Users
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

        //���USER_NAME�bSpinner���-spinner�W
        Spinner spinner = (Spinner)findViewById(R.id.Spinner01);
        spinner.setAdapter(new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, list));
        //�bSpinner���-spinner�W��w�d�߸�ơA��ܩҦ���Ʀb�e���W
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String content = ((Spinner) parent).getSelectedItem().toString();
                Cursor c = db.query("Voc", FROM_VOC, "content='" + content + "'", null, null, null, null);
                c.moveToFirst();
                id_this = Integer.parseInt(c.getString(0));
                String id_thist=c.getString(0);
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

        //���U[Add]���s�ɡA�s�W�@�����
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
        //���U[Update]���s�ɡA��s�@�����
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
        //���U[Delete]���s�ɡA�R���@�����
        listener_delete = new View.OnClickListener() {
            public void onClick(View v) {
                String where = VocSchema.ID + " = " + id_this;
                SQLiteDatabase db = helper.getWritableDatabase();
                db.delete(VocSchema.TABLE_NAME, where ,null);
                db.close();
                onCreate(savedInstanceState);
            }
        };
        //���U[Clear]���s�ɡA�M�ſ�J���
        listener_clear = new View.OnClickListener() {
            public void onClick(View v) {
                mEditText01.setText("");
                mEditText02.setText("");
                mEditText03.setText("");
            }
        };
        //�]�wBUTTON0i,i=1,2,3,4��OnClickListener
        button_add = (Button)findViewById(R.id.Button01);
        button_add.setOnClickListener(listener_add);
        button_update = (Button)findViewById(R.id.Button02);
        button_update.setOnClickListener(listener_update);
        button_delete = (Button)findViewById(R.id.Button03);
        button_delete.setOnClickListener(listener_delete);
        button_clear = (Button)findViewById(R.id.Button04);
        button_clear.setOnClickListener(listener_clear);
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
        }
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO Auto-generated method stub
        }
    }
}
