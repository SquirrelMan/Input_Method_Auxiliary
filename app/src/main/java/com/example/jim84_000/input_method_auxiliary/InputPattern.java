package com.example.jim84_000.input_method_auxiliary;

import android.util.Log;

/**
 * Created by jim84_000 on 2016/4/21.
 */
public class InputPattern {
    private String[] ContentofButton=new String[13];
    private int[] IDofButton=new int[13];
    private String TAG="InputPattern";
    public InputPattern(){
        for(int i = 0 ; i < 13 ; i++){
            ContentofButton[i]="";
            IDofButton[i]=0;
        }
    }
    public void setContentofButton(String _Text,int  _index){
        if(_index>=1 && _index <= 12) {
            ContentofButton[_index]=_Text;
        }
        else
            Log.d(TAG,"Not among the acquired _indexofContentofButton.");
    }
    public String getContentofButton(int _index){
        if(_index>=1 && _index <= 12) {
            return ContentofButton[_index];
        }
        else {
            Log.d(TAG, "Not among the acquired _indexofContentofButton.");
            return "";
        }
    }

    public void setIDofButton(int _ID,int  _index){
        if(_index>=1 && _index <= 12) {
            IDofButton[_index]=_ID;
        }
        else
            Log.d(TAG,"Not among the acquired _indexofIDofButton.");
    }
    public int getIDofButton(int _index){
        if(_index>=1 && _index <= 12) {
            return IDofButton[_index];
        }
        else {
            Log.d(TAG, "Not among the acquired _indexofIDofButton.");
            return 0;
        }
    }
}
