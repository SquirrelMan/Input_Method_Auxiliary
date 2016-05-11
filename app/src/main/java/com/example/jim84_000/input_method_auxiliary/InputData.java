package com.example.jim84_000.input_method_auxiliary;


public class InputData {
    public String text="";
    public int freq=0;
    public InputData(){

    }

    public InputData(String s){
        setText(s);
    }

    public InputData(String s,int f){
        text=s;
        freq=f;
    }

    public void setText(String s){
        text=s;
    }
}
