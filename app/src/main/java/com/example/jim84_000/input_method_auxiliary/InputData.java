package com.example.jim84_000.input_method_auxiliary;

import android.util.Log;

/**
 * Created by jim84_000 on 2016/3/10.
 */
public class InputData{
    private int Level;
    private int Root_ID;
    private String[] TextofButton=new String[13];
    private int [][]AddressofButton=new int[13][];
    private int [] SizeofAddressofButton=new int[13];
    private String TAG="InputData";
    public InputData(){
        Level=0;
        Root_ID=0;
        for(int i = 0 ; i < 13 ; i++){
            TextofButton[i]="";
            for(int j = 0 ; j <= 255 ; j++) {
                AddressofButton[i]=new int[256];
                AddressofButton[i][j] =0;
            }
            SizeofAddressofButton[i]=0;
        }
    }
    public void setLevel(int _Level){
        if(_Level>=1 && _Level <= 9)
            Level = _Level;
        else
            Log.d(TAG, "Not among the acquired levels.");
    }
    public int getLevel(){
        return Level;
    }
    public void setRoot_ID(int _Root_ID){
        if(_Root_ID>=1 && _Root_ID <= 99)
            Root_ID = _Root_ID;
        else
            Log.d(TAG,"Not among the acquired root_ids.");
    }
    public int getRoot_ID(){
        return Root_ID;
    }
    public void setSizeofAddressofButton(int _Size,int _index){
        if(_index>=1 && _index <= 12) {
            if(_Size>=0 && _Size<=256){
                SizeofAddressofButton[_index]=_Size;
            }
            else{
                Log.d(TAG,"Not among the acquired _SizeofAddressofButton.");
            }
        }
        else
            Log.d(TAG,"Not among the acquired _indexofAddressofButton.");
    }
    public int getSizeofAddressofButton(int _index){
        return SizeofAddressofButton[_index];
    }
    public void setTextofButton(String _Text,int  _index){
        if(_index>=1 && _index <= 12) {
            TextofButton[_index]=_Text;
        }
        else
            Log.d(TAG,"Not among the acquired _indexofTextofButton.");
    }
    public String getTextofButton(int _index){
        if(_index>=1 && _index <= 12) {
            return TextofButton[_index];
        }
        else {
            Log.d(TAG, "Not among the acquired _indexofTextofButton.");
            return "";
        }
    }

    public void setAddressofButton(int _index,int []_Address,int _size){
        if(_index>=1 && _index <= 12) {
            if(_size >= 0 && _size<=256){
                SizeofAddressofButton[_index]=_size;
                for(int i = 0 ; i < SizeofAddressofButton[_index] ; i++){
                    AddressofButton[_index][i]=_Address[i];
                }
            }
            else {
                Log.d(TAG, "Not among the acquired _sizeofAddressofButton.");
            }
        }
        else {
            Log.d(TAG, "Not among the acquired _indexofAddressofButton.");
        }
    }

    public int getoneAddressofButton(int _indexofButton,int _indexofAddress){
        if(_indexofButton>=1 && _indexofButton <= 12) {
            if(_indexofAddress>=0 && _indexofAddress<SizeofAddressofButton[_indexofButton]) {
                return AddressofButton[_indexofButton][_indexofAddress];
            }
            else{
                Log.d(TAG, "Not among the acquired _indexofAddress.");
                return -1;
            }

        }
        else {
            Log.d(TAG, "Not among the acquired _indexofButton.");
            return -1;
        }
    }
}
