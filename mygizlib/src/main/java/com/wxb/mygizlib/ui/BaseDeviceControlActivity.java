package com.wxb.mygizlib.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

//import com.wxb.mygizlib.InfoConfig.InfoConfig;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.concurrent.ConcurrentHashMap;

public abstract  class BaseDeviceControlActivity  extends AppCompatActivity {
    private QMUITipDialog dialog;
    protected GizWifiDevice mDevice;
    protected QMUITopBar mTopBar;
    private  String PRODUCTSECRET="";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getInfoconfig();
        initDevice();
    }

    private void getInfoconfig() {
        SharedPreferences getcontent=getSharedPreferences("data",MODE_PRIVATE);
        PRODUCTSECRET=getcontent.getString("PRODUCTSECRET","");
    }

    protected  void initDevice(){
        //拿到上个界面传过来的device
        mDevice=this.getIntent().getParcelableExtra("_device");
        //设置设备的云端回调结果监听
        mDevice.setListener(mListener);
        dialog=new QMUITipDialog.Builder(this)
                .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                .setTipWord("同步中......")
                .create();
         dialog.show();
         getStatus();
        Log.e("Tag","上个界面的设备"+mDevice);

    }
//主动获取最新状态
    protected  void getStatus(){
        //如果设备可控，那么就获取最新状态
        if(mDevice.getNetStatus()==GizWifiDeviceNetStatus.GizDeviceControlled){
            mDevice.getNetStatus();
            dialog.dismiss();

        }

    }
/*控制命令下发
 *key:标志名
 * value；数值
 *
 *
 * */
    protected void sendCommand(String key,Object value){
        if(value==null){
            return;
        }
        ConcurrentHashMap<String, Object> dataMap=new ConcurrentHashMap<>();
        dataMap.put(key,value);
        mDevice.write(dataMap,5);
    }

    //接受云端数据
    protected void receiveCloudData(GizWifiErrorCode result,ConcurrentHashMap<String, Object> dataMap){

        if(result==GizWifiErrorCode.GIZ_SDK_SUCCESS){
            dialog.dismiss();


        }
    }

    private  void updateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus){
        if(netStatus==GizWifiDeviceNetStatus.GizDeviceOffline){//设备离线
            Toast.makeText(this,"设备无法同步",Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            finish();
        }


    }
    private GizWifiDeviceListener mListener=new GizWifiDeviceListener(){

        @Override
        public void didReceiveData(GizWifiErrorCode result, GizWifiDevice device, ConcurrentHashMap<String, Object> dataMap, int sn) {
            super.didReceiveData(result, device, dataMap, sn);
            receiveCloudData(result,dataMap);
            Log.e("ZNJJ","控制界面的下发"+dataMap);
        }
        //设备状态回调，离线。在线

        @Override
        public void didUpdateNetStatus(GizWifiDevice device, GizWifiDeviceNetStatus netStatus) {
            super.didUpdateNetStatus(device, netStatus);
            updateNetStatus(device, netStatus);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //取消订阅云端消息
        mDevice.setListener(null);
        //mDevice.setSubscribe("d90a1304a2d4464a9b41e2916e9ad7a0",false);
        //mDevice.setSubscribe(InfoConfig.PRODUCTSECRET,false);
        mDevice.setSubscribe(PRODUCTSECRET,false);
    }
}
