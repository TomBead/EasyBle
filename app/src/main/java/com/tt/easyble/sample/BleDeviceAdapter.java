package com.tt.easyble.sample;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.tt.easyble.R;

import java.util.List;

public class BleDeviceAdapter extends BaseQuickAdapter<BluetoothDevice, BaseViewHolder> {

    Activity activity;

    public BleDeviceAdapter(Activity activity, List data) {
        super(R.layout.item_bluetooth_device, data);
        this.activity = activity;
    }

    @Override
    protected void convert(BaseViewHolder helper, BluetoothDevice item) {
        helper.setText(R.id.item_ble_name, item.getName())
                .setText(R.id.item_ble_mac, item.getAddress());
        TextView name = helper.getView(R.id.item_ble_name);
        helper.addOnClickListener(R.id.item_ble_ll);
    }
}