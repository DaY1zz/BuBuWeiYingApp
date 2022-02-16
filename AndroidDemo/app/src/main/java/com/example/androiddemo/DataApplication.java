package com.example.androiddemo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class DataApplication extends Application{
    public static final String MAC_ADDR = "MACAddr";

    public String getBTMAC() {
        SharedPreferences checkSP = getSharedPreferences("set", MODE_PRIVATE);
        return checkSP.getString(MAC_ADDR, null);
    }

    public void setBTMAC( String value) {
        SharedPreferences checkSP = getSharedPreferences("set", MODE_PRIVATE);
        SharedPreferences.Editor editor = checkSP.edit();
        editor.putString(MAC_ADDR, value);
        editor.commit();
    }
}
