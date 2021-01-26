package com.tt.easyble.sample;

import android.app.Activity;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.orhanobut.logger.Logger;
import com.tt.easyble.R;

import java.util.List;

public class LogAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    Activity activity;

    public LogAdapter(Activity activity, List data) {
        super(R.layout.item_log, data);
        this.activity = activity;
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        helper.setText(R.id.item_log_tv, item);
    }
}