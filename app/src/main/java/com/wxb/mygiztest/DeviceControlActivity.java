package com.wxb.mygiztest;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.wxb.mygizlib.ui.BaseDeviceControlActivity;

import java.util.concurrent.ConcurrentHashMap;

//子类
public class DeviceControlActivity extends BaseDeviceControlActivity implements View.OnClickListener {
    private  String LEDNAME="";
    private  String LED1NAME="";
    private  String LED2NAME="";
    private Switch mLED1;
    private Switch mLED2;
    private Switch mLED;
    private SeekBar mWd;
    private TextView mTempreature;
    private  static final String KEY_TEMPERATURE="Temperature";
    private  static final String KEY_MJSJ="MJSJ";
    private  static final String KEY_LED="LED";
    private  static final String KEY_LED_DATA="LED_Data";
    private  static final String KEY_LED1="LED1";
    private  static final String KEY_LED2="LED2";
    private  static final String KEY_CFDENG="CFDENG";
    // End Of Content View Elements
    private int  tempTemperature=0;
    private boolean  ledSwitch=false;
    private boolean  led1Switch=false;
    private boolean  led2Switch=false;
    private boolean  leddataSwitch=false;

    private void bindViews() {
        mLED1 = (Switch) findViewById(R.id.LED1);
        mLED2 = (Switch) findViewById(R.id.LED2);
        mLED = (Switch) findViewById(R.id.LED);
        mWd = (SeekBar) findViewById(R.id.wd);
        mTempreature=(TextView)findViewById(R.id.mTemper);
        mLED1.setOnClickListener(this);
        mLED2.setOnClickListener(this);
        mLED.setOnClickListener(this);


    }
    private Handler mHandler =new Handler(){
        @Override
        public void dispatchMessage(Message msg) {
            super.dispatchMessage(msg);

            if(msg.what==108){
                updateUI();

            }
        }
    };

    private void updateUI() {
     mTempreature.setText(tempTemperature+"度");
     mLED.setChecked(ledSwitch);
     mLED1.setChecked(led1Switch);
     mLED2.setChecked(led2Switch);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_control);
        initView();
    }

    private void initView() {
        mTopBar=findViewById(R.id.topBarCon);
        mTopBar.addLeftImageButton(com.wxb.mygizlib.R.mipmap.ic_back,com.wxb.mygizlib.R.id.topbar_left_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mTopBar.addRightTextButton("编辑节点名称",com.wxb.mygizlib.R.id.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showReNamePointDialogClick();

            }
        });
        //判断名字改过没有，如果改过，就同步新的名字
        String tempTitle=mDevice.getAlias().isEmpty() ? mDevice.getProductName():mDevice.getAlias();
        mTopBar.setTitle(tempTitle);
        bindViews();
        pointNameInit();
    }
private  void pointNameInit(){
    SharedPreferences editor=getSharedPreferences("data",MODE_PRIVATE);
    //节点名字获取
    LEDNAME=editor.getString("LEDNAME","");
    LED1NAME=editor.getString("LED1NAME","");
    LED2NAME=editor.getString("LED2NAME","");
    mLED.setText(LEDNAME);
    mLED1.setText(LED1NAME);
    mLED2.setText(LED2NAME);
}


    /******
     * 弹出节点名字修改选择对话框，选择节点
     */
    private void showReNamePointDialogClick() {
        //显示弹窗
        String[] items=new String[]{mLED.getText().toString(),mLED1.getText().toString(),mLED2.getText().toString()};

        new QMUIDialog.MenuDialogBuilder(this).addItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        showReNamePointDialog(mLED);
                        break;
                    case 1:
                        showReNamePointDialog(mLED1);
                        break;
                    case 2:
                        showReNamePointDialog(mLED2);
                        break;
                }
                dialogInterface.dismiss();
            }
        }).show();

    }
    //重命名节点
    private void showReNamePointDialog(final  Switch switchId) {
        final QMUIDialog.EditTextDialogBuilder builder=new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("重命名")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setPlaceholder("在此输入新名称")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确认", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        String newName= builder.getEditText().getText().toString().trim();
                        if(newName.isEmpty()){
                            Toast.makeText(DeviceControlActivity.this,"输入为空",Toast.LENGTH_SHORT).show();

                        }
                        else{
                            switchId.setText(newName);
                            SharedPreferences.Editor editor=getSharedPreferences("data",MODE_PRIVATE).edit();
                            switch (switchId.getId()){
                                case R.id.LED:
                                    editor.putString("LEDNAME",newName);
                                    break;
                                case R.id.LED1:
                                    editor.putString("LED1NAME",newName);
                                    break;
                                case R.id.LED2:
                                    editor.putString("LED2NAME",newName);
                                    break;
                            }
                            editor.apply();

                        }
                        dialog.dismiss();

                    }
                })
                .show();
    }

    @Override
    protected void receiveCloudData(GizWifiErrorCode result, ConcurrentHashMap<String, Object> dataMap) {
        super.receiveCloudData(result, dataMap);
        Log.e("ZNJJ","子类界面"+dataMap);
        if(result==GizWifiErrorCode.GIZ_SDK_SUCCESS){
            if(dataMap!=null){

                parseReceiveData(dataMap);
            }

        }
    }
//解析数据
    private void parseReceiveData(ConcurrentHashMap<String, Object> dataMap) {
        if(dataMap.get("data")!=null){
            ConcurrentHashMap<String, Object> tempperDataMap= ( ConcurrentHashMap<String, Object>)dataMap.get("data");
                for(String dataKey:tempperDataMap.keySet()){
                    //温度
                    //通过云端定义的标识符来同步数据如LED1
                    if(dataKey.equals(KEY_TEMPERATURE)){
                        tempTemperature= (int) tempperDataMap.get(KEY_TEMPERATURE);

                    }
                    if(dataKey.equals(KEY_LED)){
                            ledSwitch= (boolean) tempperDataMap.get(KEY_LED);
                    }
                    if(dataKey.equals(KEY_LED1)){
                        led1Switch= (boolean) tempperDataMap.get(KEY_LED1);
                    }
                   if(dataKey.equals(KEY_LED2)){
                        led2Switch= (boolean) tempperDataMap.get(KEY_LED2);
                    }
                    if(dataKey.equals(KEY_CFDENG)){
                        led2Switch= (boolean) tempperDataMap.get(KEY_CFDENG);
                    }

                   /* if(dataKey.equals(KEY_LED_DATA)){
                        leddataSwitch= (boolean) tempperDataMap.get(KEY_LED_DATA);
                    }*/

                }
                mHandler.sendEmptyMessage(108);
        }

    }

    //按钮回调
    @Override
    public void onClick(View view) {
      if(view.getId()== R.id.LED) {
          sendCommand(KEY_LED, mLED.isChecked());
      }
      else if(view.getId()== R.id.LED1){
              sendCommand(KEY_LED1,mLED1.isChecked());
      }
      else if(view.getId()== R.id.LED2){
              sendCommand(KEY_LED2,mLED2.isChecked());
              sendCommand(KEY_CFDENG,mLED2.isChecked());
        }



    }
}
