package com.example.peiqi.adapter;

import android.content.Context;

import com.example.peiqi.R;
import com.example.peiqi.bean.MessageBean;
import com.example.peiqi.common.BaseRecyclerAdapter;
import com.example.peiqi.common.BaseViewHolder;

import java.util.List;

/**
 * 聊天界面适配器
 *
 * @author bohan.chen
 */
public class ChatAdapter extends BaseRecyclerAdapter<MessageBean> {

    /**
     * 是否为16进制展示
     */
    private boolean isShowHex = false;
    /**
     * 需要过滤的
     */
    private String filterStr = "";

    public void setShowHex(boolean showHex) {
        isShowHex = showHex;
        notifyDataSetChanged();
    }

    public ChatAdapter(Context mContext, List<MessageBean> mDataList) {
        super(mContext, mDataList, R.layout.item_chat);
    }

    @Override
    protected void covert(BaseViewHolder holder, MessageBean itemData, int position) {
        holder.setText(R.id.tvName, itemData.getName());
        String showData;
        if (isShowHex) {
            showData = itemData.getHexData();
        } else {
            showData = itemData.getNormalData();
        }
        holder.setText(R.id.tvContent, showData);
    }

    public void setFilterStr(String filterStr) {
        this.filterStr = filterStr;
    }
}
