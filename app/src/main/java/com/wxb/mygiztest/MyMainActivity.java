package com.wxb.mygiztest;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
//import com.wxb.mygizlib.InfoConfig.InfoConfig;
import com.wxb.mygizlib.Utils.SharePreUtils;
import com.wxb.mygizlib.adapter.LvDeviceAdapter;
import com.wxb.mygizlib.ui.NetConfigActivity;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.api.GizWifiSDK;
import com.gizwits.gizwifisdk.enumration.GizEventType;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;
import com.gizwits.gizwifisdk.enumration.GizWifiErrorCode;
import com.gizwits.gizwifisdk.listener.GizWifiDeviceListener;
import com.gizwits.gizwifisdk.listener.GizWifiSDKListener;
import com.qmuiteam.qmui.widget.QMUITopBar;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class  MyMainActivity extends AppCompatActivity {
    String uid;
    String token;
    private  String APPID="";
    private  String APPSECRET="";
    private  String PRODUCTKEY="";
    private  String PRODUCTSECRET="";


    private static final String TAG = "MainActivity";
    private ListView listView;
    private LvDeviceAdapter adapter;
    private  List<GizWifiDevice> gizWifiDeviceList;
    private SwipeRefreshLayout mSwitchRefreshLayout;
    //刷新的弹窗
    private QMUITipDialog reflashTipDialog;
    private QMUITipDialog mTipDialog;
    private Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what==105){
                adapter.notifyDataSetChanged();

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_activity_main);
        getInfoconfig();
        initSDK();
        initView();
    }

    private void getInfoconfig() {
        SharedPreferences getcontent=getSharedPreferences("data",MODE_PRIVATE);
        APPID=getcontent.getString("APPID","");
        APPSECRET=getcontent.getString("APPSECRET","");
        PRODUCTKEY=getcontent.getString("PRODUCTKEY","");
        PRODUCTSECRET=getcontent.getString("PRODUCTSECRET","");

    }

    private void initView() {
        QMUITopBar topBar=(QMUITopBar)findViewById(R.id.topBar);
        topBar.setTitle("智能家居");
        //右边添加+图标
        topBar.addRightImageButton(com.wxb.mygizlib.R.mipmap.ic_add,com.wxb.mygizlib.R.id.topbar_right_icon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MyMainActivity.this,NetConfigActivity.class);
                startActivity(intent);
            }
        });


        gizWifiDeviceList=new ArrayList<>();
        listView=(ListView) findViewById(R.id.listview);
        adapter=new LvDeviceAdapter(this,gizWifiDeviceList);
        listView.setAdapter(adapter);
        //轻触的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startControl(gizWifiDeviceList.get(i));
            }
        });
        //弹出重命名和解绑
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
               showLongDialogClick(gizWifiDeviceList.get(i));
                return true;
            }
        });
        getBundleDevices();
        mSwitchRefreshLayout=findViewById(R.id.switchRefreshLayout);
        //设置下拉的颜色
        mSwitchRefreshLayout.setColorSchemeResources(android.R.color.white);
        mSwitchRefreshLayout.setColorSchemeResources(com.wxb.mygizlib.R.color.lib_app_color_theme_1,
                                                       com.wxb.mygizlib.R.color.lib_app_color_theme_2,
                                                       com.wxb.mygizlib.R.color.lib_app_color_theme_3,
                                                       com.wxb.mygizlib.R.color.lib_app_color_theme_4,
                                                       com.wxb.mygizlib.R.color.lib_app_color_theme_5,
                                                       com.wxb.mygizlib.R.color.lib_app_color_theme_6,
                                                       com.wxb.mygizlib.R.color.lib_app_color_theme_7);
        //调用系统测量下滑
        mSwitchRefreshLayout.measure(0,0);
        //打开页面就是下拉的状态
        mSwitchRefreshLayout.setRefreshing(true);
        //设置手动下拉监听事件
        mSwitchRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reflashTipDialog=new QMUITipDialog.Builder(MyMainActivity.this)
                        .setTipWord("正在刷新")
                        .setIconType(QMUITipDialog.Builder.ICON_TYPE_LOADING)
                        .create();
                reflashTipDialog.show();
                mSwitchRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //拿到SDK里面的设备
                        if(GizWifiSDK.sharedInstance().getDeviceList().size()!=0){
                            gizWifiDeviceList.clear();
                            gizWifiDeviceList.addAll(GizWifiSDK.sharedInstance().getDeviceList());
                            adapter.notifyDataSetChanged();
                        }

                        reflashTipDialog.dismiss();
                        mSwitchRefreshLayout.setRefreshing(false);
                        //显示另外一个弹窗
                        if(gizWifiDeviceList.size()==0) {
                            mTipDialog = new QMUITipDialog.Builder(MyMainActivity.this)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_NOTHING)
                                    .setTipWord("暂无设备")
                                    .create();
                            mTipDialog.show();
                        }
                        else{
                            mTipDialog = new QMUITipDialog.Builder(MyMainActivity.this)
                                    .setIconType(QMUITipDialog.Builder.ICON_TYPE_NOTHING)
                                    .setTipWord("获取成功")
                                    .create();
                            mTipDialog.show();
                        }
                        mSwitchRefreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mTipDialog.dismiss();
                            }
                        },1500);

                    }
                },3000);
            }
        });
        //3s后自动收回下滑
        listView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwitchRefreshLayout.setRefreshing(false);
            }
        },3000);

    }
//跳转控制
    private void startControl(GizWifiDevice device) {
        if(device.getNetStatus()==GizWifiDeviceNetStatus.GizDeviceOffline){
            return;
        }
        device.setListener(mWifiDeviceListener);
        //device.setSubscribe("d90a1304a2d4464a9b41e2916e9ad7a0",true);
        device.setSubscribe(PRODUCTSECRET,true);



    }

    private void showLongDialogClick(final GizWifiDevice device) {
    //显示弹窗
        String[] items=new String[]{"重命名","解绑设备"};

        new QMUIDialog.MenuDialogBuilder(this).addItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        showReNameDialog(device);
                        break;
                    case 1:
                        showDelateDialog(device);
                        break;

                }
                dialogInterface.dismiss();
            }
        }).show();

    }
//解绑设备
    private void showDelateDialog(final GizWifiDevice device) {
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle("您可解绑远程设备").setMessage("确定要解绑设备？")

                .addAction("取消", new QMUIDialogAction.ActionListener() {
                @Override
                public void onClick(QMUIDialog dialog, int index) {
                    dialog.dismiss();
                }
                })
                .addAction("删除", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        GizWifiSDK.sharedInstance().unbindDevice(uid,token,device.getDid());
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //重命名
    private void showReNameDialog(final GizWifiDevice device) {
        final QMUIDialog.EditTextDialogBuilder builder=new QMUIDialog.EditTextDialogBuilder(this);
        builder.setTitle("重命名")
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setPlaceholder("在此输入新名字")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {

                    }
                })
                .addAction("确认", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                      String newName= builder.getEditText().getText().toString().trim();
                      if(newName.isEmpty()){
                          Tost("输入为空");

                      }
                      else{
                          device.setListener(mWifiDeviceListener);
                          device.setCustomInfo(null,newName);
                      }
                        dialog.dismiss();

                    }
                })
                .show();
    }


    private void getBundleDevices() {
         uid=SharePreUtils.getString(this,"_uid",null);
         token=SharePreUtils.getString(this,"_token",null);
        if(uid!=null&&token!=null) {
            GizWifiSDK.sharedInstance().getBoundDevices(uid, token);
        }
    }

    private void initSDK(){
        // 设置 SDK 监听
        GizWifiSDK.sharedInstance().setListener(mListener);
        // 设置 AppInfo
        ConcurrentHashMap<String, String> appInfo= new ConcurrentHashMap<>();
        //appInfo.put("appId", "6d9b43bf2be74bc0919dac6f0952b0a1");//机智云查看
        //appInfo.put("appId",InfoConfig.APPID);//机智云查看
        appInfo.put("appId",APPID);//机智云查看
        //appInfo.put("appSecret", "4da1df136c5f4a578f13c28067833bdd");//机智云查看
        //appInfo.put("appSecret",InfoConfig.APPSECRET);//机智云查看
        appInfo.put("appSecret",APPSECRET);//机智云查看
        // 设置要过滤的设备 productKey 列表。不过滤则直接传 null
        List<ConcurrentHashMap<String, String>> productInfo = new ArrayList<>();
        ConcurrentHashMap<String,String> product = new ConcurrentHashMap<>();
        // product.put("productKey", "ea312435a255445d8ccb9932f638f402");//机智云查看
        // product.put("productKey",InfoConfig.PRODUCTKEY);//机智云查看
        product.put("productKey",PRODUCTKEY);//机智云查看
        // product.put("productSecret", "d90a1304a2d4464a9b41e2916e9ad7a0");//机智云查看
        // product.put("productSecret",InfoConfig.PRODUCTSECRET);//机智云查看
        product.put("productSecret",PRODUCTSECRET);//机智云查看

        productInfo.add(product);
        // 调用SDK 的启动接口
        GizWifiSDK.sharedInstance().startWithAppInfo(this, appInfo, productInfo, null, false);

    }
    // 实现系统事件通知回调
    private GizWifiSDKListener mListener=new GizWifiSDKListener() {

        @Override
        public void didUnbindDevice(GizWifiErrorCode result, String did) {
            super.didUnbindDevice(result, did);
            if(result==GizWifiErrorCode.GIZ_SDK_SUCCESS){

                Tost("恭喜解绑成功");
            }
            else{
                Tost("解绑失败"+result);


            }
        }

        @Override
        public void didBindDevice(GizWifiErrorCode result, String did) {
            super.didBindDevice(result, did);
            if(result==GizWifiErrorCode.GIZ_SDK_SUCCESS){
                //绑定成功
                Tost("绑定成功");
            }
            else{

                Tost("绑定失败");
            }
        }

        @Override
        public void didNotifyEvent(GizEventType eventType, Object eventSource, GizWifiErrorCode eventID, String eventMessage) {
            super.didNotifyEvent(eventType, eventSource, eventID, eventMessage);
            Log.d("wangxianbin","didNotifyEvent:" + eventType.toString());
            //Tost(eventType.toString());

            //如果SDK初始化成功
            if(eventType==GizEventType.GizEventSDK){
                GizWifiSDK.sharedInstance().userLoginAnonymous();
            }
        }

        @Override
        public void didUserLogin(GizWifiErrorCode result, String uid, String token) {
            super.didUserLogin(result, uid, token);
            if(result==GizWifiErrorCode.GIZ_SDK_SUCCESS){
                Tost("登录成功");
               // Tost("uid: "+uid);
               // Tost("token: "+token);
                SharePreUtils.putString(MyMainActivity.this,"_uid",uid);
                SharePreUtils.putString(MyMainActivity.this,"_token",token);
            }
            else{

                Tost("登录失败");
            }
        }
        //局域网发现的设备
        @Override
        public void didDiscovered(GizWifiErrorCode result, List<GizWifiDevice> deviceList) {
            super.didDiscovered(result, deviceList);
            //Tost("已经"+deviceList);
          //  Log.d("Tag","bangong"+deviceList);
            for(int i=0;i<deviceList.size();i++){
                //判断此设备是否已绑定
                if(!deviceList.get(i).isBind()){
                    //开始绑定
                    startBindDevice(deviceList.get(i));


                }

            }
            gizWifiDeviceList.clear();
            gizWifiDeviceList.addAll(deviceList);
            mHandler.sendEmptyMessage(105);
        }
    };

    private void startBindDevice(GizWifiDevice device) {
        if(uid!=null&&token!=null){
           /* GizWifiSDK.sharedInstance().bindRemoteDevice(uid,token,device.getMacAddress(),
                    "ea312435a255445d8ccb9932f638f402",
                    "d90a1304a2d4464a9b41e2916e9ad7a0");*/
            //GizWifiSDK.sharedInstance().bindRemoteDevice(uid,token,device.getMacAddress(),
            //        InfoConfig.PRODUCTKEY,
              //      InfoConfig.PRODUCTSECRET);

            GizWifiSDK.sharedInstance().bindRemoteDevice(uid,token,device.getMacAddress(),
                    PRODUCTKEY,
                    PRODUCTSECRET);

        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        GizWifiSDK.sharedInstance().setListener(mListener);
    }

    public void Tost(String string){
        Toast.makeText(MyMainActivity.this,string,Toast.LENGTH_SHORT).show();
    }

    private GizWifiDeviceListener mWifiDeviceListener=new GizWifiDeviceListener(){
//设备订阅，解除订阅的回调
        @Override
        public void didSetSubscribe(GizWifiErrorCode result, GizWifiDevice device, boolean isSubscribed) {
            super.didSetSubscribe(result, device, isSubscribed);
           // Tost("订阅结果"+result);
           // Tost("订阅设备"+device);
            //如果订阅成功，则可以跳转到控制界面
            if(result==GizWifiErrorCode.GIZ_SDK_SUCCESS){
                Intent intent=new Intent(MyMainActivity.this,DeviceControlActivity.class);
                intent.putExtra("_device",device);
                startActivity(intent);
            }
        }

        @Override
        public void didSetCustomInfo(GizWifiErrorCode result, GizWifiDevice device) {
            super.didSetCustomInfo(result, device);
            if(result==GizWifiErrorCode.GIZ_SDK_SUCCESS){

                //修改成功
                if(GizWifiSDK.sharedInstance().getDeviceList().size()!=0){
                   gizWifiDeviceList.clear();
                   gizWifiDeviceList.addAll(GizWifiSDK.sharedInstance().getDeviceList());
                   adapter.notifyDataSetChanged();
                   Tost("修改成功");

                }
            }
            else{
                //修改失败
                Tost("修改失败");

            }
        }
    };
}
