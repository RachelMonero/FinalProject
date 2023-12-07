package com.example.finalproject;

import android.content.Context;
import android.content.SharedPreferences;

//Save user visit as shared preferences and count.
public class VisitCount{
    private static final String User = "user";
    private static final String Visit = "visitCount";


    public static void countVisitPlusOne(Context context){
        SharedPreferences sp = context.getSharedPreferences(User,context.MODE_PRIVATE);
        int count= sp.getInt(Visit,0)+1;
        sp.edit().putInt(Visit,count).apply();
    }
    public static int getVisitCount(Context context){
        SharedPreferences sp = context.getSharedPreferences(User,context.MODE_PRIVATE);
        return sp.getInt(Visit,0);
    }

}
