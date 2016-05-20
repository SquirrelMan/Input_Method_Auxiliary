package com.example.jim84_000.input_method_auxiliary;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;

public class Speaker implements TextToSpeech.OnInitListener {

    private TextToSpeech mTts;
    private static final String TAG = "SPEAKER";
    String[] msg=new String[50];

    public Speaker(Context ctx,TextToSpeech.OnInitListener listener){
        mTts = new TextToSpeech(ctx,listener);
    }

    //=============================================語音==============================================
    @Override
    public void onInit(int status) {
        // status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
        if (status == TextToSpeech.SUCCESS) {
            int result;
            result = mTts.setLanguage(Locale.TAIWAN);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Lanuage data is missing or the language is not supported.
                Log.e(TAG, "Language is not available.");
            }
        } else {
            // Initialization failed.
            Log.e(TAG, "Could not initialize TextToSpeech.");
        }
    }

    private void clear(){
        for(int i=0;i<50;i++)
            msg[i]="";
    }

    public void sayHello(String hello) {
        // Select a random hello.
        // Drop allpending entries in the playback queue.
        mTts.speak(hello, TextToSpeech.QUEUE_FLUSH, null);
    }
    //===============================================================================================
}
