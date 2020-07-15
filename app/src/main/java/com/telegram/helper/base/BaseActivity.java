package com.telegram.helper.base;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.telegram.helper.R;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        initView();
        BaseApplication.getInstance().addTopActivity(this);
    }

    protected void initView() {
    }

    protected abstract int getLayoutId();

    protected BaseActivity getActivity() {
        return this;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BaseApplication.getInstance().removeTopActivity(this);
    }

    @Override
    public void setTitle(int titleId) {
        TextView titleView = findViewById(R.id.float_title);
        if (titleView != null) {
            titleView.setText(getString(titleId));
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView titleView = findViewById(R.id.float_title);
        if (titleView != null) {
            titleView.setText(title);
        }
    }
}
