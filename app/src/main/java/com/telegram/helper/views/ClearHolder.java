package com.telegram.helper.views;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;


import com.telegram.helper.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class ClearHolder {
    public View view;
    private BaseAdapter mAdapter;
    private ListView results;
    private View bgResult;
    private WaveView progress;
    private List<String> mDatas = new ArrayList<>();

    public ClearHolder(View view) {
        this.view = view;
        progress = view.findViewById(R.id.progress);
        bgResult = view.findViewById(R.id.bg_result);
        results = view.findViewById(R.id.results);
        mAdapter = getAdapter();
        results.setAdapter(mAdapter);
        bgResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        dismiss();
    }

    public int getLayoutId() {
        return R.layout.layout_clear;
    }

    public void startLoad() {
        results.setVisibility(View.GONE);
        bgResult.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
    }

    public void stopLoad() {
        results.setVisibility(View.GONE);
        bgResult.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
    }

    public ListView getResults() {
        return results;
    }

    public void stopLoad(List<String> datas) {
        if (datas == null || datas.isEmpty()) {
            datas = new ArrayList<>();
        }
        results.setVisibility(View.VISIBLE);
        bgResult.setVisibility(View.VISIBLE);
        progress.setVisibility(View.GONE);
        mDatas.clear();
        mDatas.addAll(datas);
        mAdapter.notifyDataSetChanged();
    }

    public void dismiss() {
        results.setVisibility(View.GONE);
        bgResult.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
    }

    private BaseAdapter getAdapter() {
        return new BaseAdapter() {
            @Override
            public int getCount() {
                return mDatas.size();
            }

            @Override
            public Object getItem(int position) {
                return mDatas.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View view, ViewGroup parent) {
                ViewHolder viewHolder;
                if (view == null) {
                    viewHolder = new ViewHolder();
                    view = LayoutInflater.from(ClearHolder.this.view.getContext()).inflate(R.layout.layout_item_path, parent, false);
                    viewHolder.pathText = view.findViewById(R.id.path_text);
                    view.setTag(viewHolder);
                } else {
                    viewHolder = (ViewHolder) view.getTag();
                }
                viewHolder.pathText.setText(mDatas.get(position));
                return view;
            }

            class ViewHolder {
                TextView pathText;
            }
        };
    }

}
