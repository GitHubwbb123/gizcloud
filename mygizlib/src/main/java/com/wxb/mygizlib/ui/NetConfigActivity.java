package com.wxb.mygizlib.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.wxb.mygizlib.R;
import com.wxb.mygizlib.Utils.SharePreUtils;
import com.wxb.mygizlib.Utils.WifiAdminUtils;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizWifiConfigureMode;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.enumration.GizWifiGAgentType;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.qmuiteam.qmui.widget.QMUITopBar;

import java.util.ArrayList;
import java.util.List;

import static com.gizwits.gizwifisdk.enumration.GizWifiGAgentType.GizGAgentESP;

public class NetConfigActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText mEtAPPsw;
    private CheckBox mCbPaw;
    public TextView tvAPssid;
    public Button searchAp;
    private WifiAdminUtils adminUtils;
    //进度弹窗
    private ProgressDialog dialog;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==105){

                dialog.setMessage("配网成功");
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);

            }
            if(msg.what==106){

                dialog.setMessage("配网失败");
                dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.VISIBLE);
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.VISIBLE);
            }

        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_net_config);
        initView();
        String uid=SharePreUtils.getString(NetConfigActivity.this,"_uid",null);
        String token=SharePreUtils.getString(NetConfigActivity.this,"_token",null);
       // Tost("uid: "+uid);
      //  Tost("token: "+token);
    }

    private void initView() {
        adminUtils=new WifiAdminUtils(this);
        QMUITopBar topBar=(QMUITopBar)findViewById(R.id.topBarnet);
        topBar.setTitle("添加设备");
        topBar.addLeftImageButton(R.mipmap.ic_back,R.id.topbar_left_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        mEtAPPsw=(EditText) findViewById(R.id.etApPassord);
        mEtAPPsw.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            //编辑中的回调
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                if(!charSequence.toString().isEmpty()){
                    mCbPaw.setVisibility(View.VISIBLE);

                }
                else{


                }
            }
            //编辑后的回调
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        mCbPaw=(CheckBox) findViewById(R.id.cbPaw);
        mCbPaw.setVisibility(View.GONE);
        mCbPaw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    mEtAPPsw.setInputType(View.TEXT_DIRECTION_INHERIT);//显示密码

                }
                else{
                    mEtAPPsw.setInputType(0X81);

                }
            }
        });
        searchAp=(Button)findViewById(R.id.search);
        tvAPssid=(TextView)findViewById(R.id.tvApssid);
        searchAp.setOnClickListener(this);

    }
    public void Tost(String string){
        Toast.makeText(NetConfigActivity.this,string,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        //library中的R的ID不是finally,不能用switch，只能用if,else
        //开始配网
        if(view.getId()== R.id.search){
                String tvSSID=tvAPssid.getText().toString().intern();//wifi名字
                String tvPASS=mEtAPPsw.getText().toString().intern();//wifi密码
                if(!tvPASS.isEmpty()){

                    dialog=new ProgressDialog(NetConfigActivity.this);
                    dialog.setMessage("正在配网中.......");
                    dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    dialog.setCancelable(false);//屏幕外不可点击

                    dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setButton(DialogInterface.BUTTON_POSITIVE, "好的", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    dialog.show();
                    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setVisibility(View.GONE);
                    dialog.getButton(DialogInterface.BUTTON_POSITIVE).setVisibility(View.GONE);
                    startAirLink(tvSSID,tvPASS);
                }


        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        //拿到手机当前连接WIfi的名字
        String ssid=adminUtils.getWifiConnectedSsid();
        //Tost(ssid);
        if(ssid!=null){
            tvAPssid.setText(ssid);
        }
        else{
            tvAPssid.setText("");
        }
        boolean isEmptyAPSSID=TextUtils.isEmpty(ssid);
        if(isEmptyAPSSID){
            mEtAPPsw.setEnabled(false);
            searchAp.setEnabled(false);
        }
        else
        {
            searchAp.setEnabled(true);
        }
    }

    private  void  startAirLink(String apssid,String pass){
        List<GizWifiGAgentType> types =new ArrayList<>();
        types.add(GizGAgentESP);
        GizWifiSDK.sharedInstance().setListener(listener);
        //GizWifiSDK.sharedInstance().setDeviceOnboardingDeploy();
        GizWifiSDK.sharedInstance().setDeviceOnboarding(apssid,pass,GizWifiConfigureMode.GizWifiAirLink,null,60,types);

    }
    private GizWifiSDKListener listener =new GizWifiSDKListener() {
        @Override
        public void didSetDeviceOnboarding(GizWifiErrorCode result, GizWifiDevice device) {
            super.didSetDeviceOnboarding(result, device);
            if(result==GizWifiErrorCode.GIZ_SDK_SUCCESS){
                mHandler.sendEmptyMessage(105);
            }
            else {

                mHandler.sendEmptyMessage(106);
            }

        }
    };
}

