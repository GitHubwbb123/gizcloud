package com.wxb.mygizlib.Utils;

import android.content.Context;
import android.content.SharedPreferences;

//存储UID和token
public class SharePreUtils {

    private  static  final String SP_NAME="config";
    public static  void putString(Context mContext, String key, String value){
//拿到本地的SharedPreferences对象，设置为只能本地应用才能读取
        SharedPreferences sp=mContext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor=sp.edit();
        editor.putString(key,value);
        editor.apply();
    }

    public  static  String getString(Context mContext, String key,String defValue){
        SharedPreferences sp=mContext.getSharedPreferences(SP_NAME,Context.MODE_PRIVATE);
        //如果该键值为空的话，则默认为defValue
        return sp.getString(key,defValue);

    }
}
