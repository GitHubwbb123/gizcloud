package com.wxb.mygizlib.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.wxb.mygizlib.R;
import com.gizwits.gizwifisdk.api.GizWifiDevice;
import com.gizwits.gizwifisdk.enumration.GizWifiDeviceNetStatus;

import java.util.List;

public class LvDeviceAdapter extends BaseAdapter {

    private Context mContext;
    private List<GizWifiDevice> gizWifiDeviceList;
    private LayoutInflater mLayoutInflater;


    public LvDeviceAdapter(Context mContext, List<GizWifiDevice> gizWifiDeviceList) {
        this.mContext = mContext;
        this.gizWifiDeviceList = gizWifiDeviceList;
        mLayoutInflater= (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return gizWifiDeviceList.size();
    }

    @Override
    public Object getItem(int i) {
        return gizWifiDeviceList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        ViewHolderListView viewHolderListView =null;
        View view1;
        GizWifiDevice device=gizWifiDeviceList.get(i);
        if(view==null){

            view1=mLayoutInflater.inflate(R.layout.item_list_devices,null);
            viewHolderListView=new ViewHolderListView();
            viewHolderListView.mDeviceName=view1.findViewById(R.id.ivDeviceName);
            viewHolderListView.mDeviceStatus=view1.findViewById(R.id.ivDeviceStatus);
            viewHolderListView.mDeviceIcon=view1.findViewById(R.id.ivDeviceIcon);
            viewHolderListView.mIvNext=view1.findViewById(R.id.ivNext);
            view1.setTag(viewHolderListView);
        }
        else{
        view1=view;
        viewHolderListView= (ViewHolderListView) view1.getTag();
        }
        //获取设备名称
        if(device.getAlias().isEmpty()){//如果重新设置了名字
            viewHolderListView.mDeviceName.setText(device.getProductName());
        }
        else{
            viewHolderListView.mDeviceName.setText(device.getAlias());
        }
        if(device.getNetStatus()==GizWifiDeviceNetStatus.GizDeviceOffline){
            viewHolderListView.mDeviceStatus.setText("离线");
            viewHolderListView.mDeviceStatus.setTextColor(mContext.getResources().getColor(R.color.lib_bar_divider));
            viewHolderListView.mIvNext.setVisibility(View.INVISIBLE);
        }
        else{
            if(device.isLAN()){
                viewHolderListView.mDeviceStatus.setText("本地在线");
            }
            else{

                viewHolderListView.mDeviceStatus.setText("远程在线");
                viewHolderListView.mDeviceStatus.setTextColor(mContext.getResources().getColor(R.color.lib_black));
                viewHolderListView.mIvNext.setVisibility(View.VISIBLE);
            }

        }

        return view1;
    }

    private class ViewHolderListView{
        ImageView mDeviceIcon,mIvNext;
        TextView  mDeviceName,mDeviceStatus;


    }
}
