package com.example.jim84_000.input_method_auxiliary;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class Speaker implements TextToSpeech.OnInitListener {

    private TextToSpeech tw,en;
    private static final String TAG = "SPEAKER";
    String[] msg=new String[50];
    boolean mode=true;

    public Speaker(Context ctx,TextToSpeech.OnInitListener listener){
        tw = new TextToSpeech(ctx,listener);
        en = new TextToSpeech(ctx,listener);
    }

    //=============================================語音==============================================
    // Implements TextToSpeech.OnInitListener.
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            language();
        }

        else {
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }
    private void language(){
        int result;
        if(mode){
            result = tw.setLanguage(Locale.TAIWAN);//<<<===================================
            mode=!mode;
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language is not available.");
            }
        }

        else {
            result = en.setLanguage(Locale.US);//<<<===================================
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                System.out.println("Language is not available.");
            }
        }

    }

    private void clear(){
        for(int i=0;i<50;i++)
            msg[i]="";
    }

    public void sayHello(String hello) {
        // Select a random hello.
        // Drop allpending entries in the playback queue.
        long start=System.currentTimeMillis();
        tw.speak(hello, TextToSpeech.QUEUE_FLUSH, null);
        en.speak("hello", TextToSpeech.QUEUE_FLUSH, null);
        long duration=System.currentTimeMillis()-start;
        System.out.println(duration);
    }
    //===============================================================================================
}
