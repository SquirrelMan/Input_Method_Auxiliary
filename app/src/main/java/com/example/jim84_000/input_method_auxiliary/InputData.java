package com.example.jim84_000.input_method_auxiliary;


public class InputData {
    public String text="";
    public int id=0;
    public InputData(){

    }

    public InputData(String s){
        setText(s);
    }

    public InputData(String s,int sid){
        text=s;
        id=sid;
    }

    public void setText(String s){
        text=s;
    }
}
