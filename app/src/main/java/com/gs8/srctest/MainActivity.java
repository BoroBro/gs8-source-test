package com.gs8.srctest;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.logic.common.aidl.ILogicCallback;
import com.logic.common.aidl.ILogicInterface;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final String TAG = "SRCTEST";
    private static final int DOMAIN_SOURCE = 2;
    private static final String SVC_PKG = "com.zhonghong.logic";
    private static final String SVC_CLS = "com.zhonghong.logic.service.LogicService";

    private ILogicInterface mLogic;
    private boolean mBound = false;
    private boolean mRegistered = false;
    private TextView mLog;
    private final Handler mUi = new Handler(Looper.getMainLooper());

    private final ILogicCallback.Stub mCallback = new ILogicCallback.Stub() {
        @Override
        public void onEventChanged(String event, String value, String extra) {
            Log.i(TAG, "EVT " + event + " v=" + value + " x=" + extra);
            log("EVT  " + event + "  v=" + value + "  x=" + extra);
        }
    };

    private final ServiceConnection mConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mLogic = ILogicInterface.Stub.asInterface(service);
            mBound = true;
            log(">> SERVICE CONNECTED");
            try {
                mLogic.registerLogicCallback(DOMAIN_SOURCE, mCallback);
                mRegistered = true;
                log(">> registerLogicCallback(domain=2) OK");
            } catch (RemoteException e) {
                log("!! register error: " + e);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
            mRegistered = false;
            mLogic = null;
            log(">> SERVICE DISCONNECTED");
        }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(16, 16, 16, 16);

        addBtn(root, "1. BIND + register", v -> doBind());
        addBtn(root, "2. setSourceId = 15 (third app)", v -> cmd("setSourceId", "15"));
        addBtn(root, "setSourceId = 3 (BT music)", v -> cmd("setSourceId", "3"));
        addBtn(root, "setSourceId = 0 (radio)", v -> cmd("setSourceId", "0"));
        addBtn(root, "releaseSourceId = 15", v -> cmd("releaseSourceId", "15"));
        addBtn(root, "closeSourceId = 15", v -> cmd("closeSourceId", "15"));
        addBtn(root, "query getSourceId", v -> query("getSourceId", ""));
        addBtn(root, "UNBIND", v -> doUnbind());
        addBtn(root, "clear log", v -> { mLog.setText(""); });

        mLog = new TextView(this);
        mLog.setTextSize(11);
        mLog.setTextColor(Color.parseColor("#00E676"));
        mLog.setBackgroundColor(Color.BLACK);
        mLog.setTypeface(android.graphics.Typeface.MONOSPACE);

        ScrollView sv = new ScrollView(this);
        sv.addView(mLog);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
        root.addView(sv, lp);

        setContentView(root);
        log("GS8 Source Test ready. Tap BIND first.");
    }

    private void addBtn(LinearLayout parent, String text, View.OnClickListener l) {
        Button btn = new Button(this);
        btn.setText(text);
        btn.setAllCaps(false);
        btn.setOnClickListener(l);
        parent.addView(btn);
    }

    private void doBind() {
        if (mBound) { log("already bound"); return; }
        Intent it = new Intent(SVC_CLS);
        it.setComponent(new ComponentName(SVC_PKG, SVC_CLS));
        boolean ok = bindService(it, mConn, Context.BIND_AUTO_CREATE);
        log("bindService returned " + ok);
    }

    private void doUnbind() {
        if (!mBound) { log("not bound"); return; }
        try {
            if (mRegistered && mLogic != null) {
                mLogic.unregisterLogicCallback(DOMAIN_SOURCE, mCallback);
                log(">> unregister OK");
            }
        } catch (RemoteException e) { log("!! unregister err: " + e); }
        try { unbindService(mConn); } catch (Exception e) { log("!! unbind err: " + e); }
        mBound = false; mRegistered = false; mLogic = null;
        log(">> unbound");
    }

    private void cmd(String cmd, String value) {
        if (mLogic == null) { log("!! not bound, tap BIND"); return; }
        try {
            mLogic.setCmd(cmd, value, null);
            log("-> setCmd(" + cmd + ", " + value + ")");
        } catch (RemoteException e) { log("!! setCmd err: " + e); }
    }

    private void query(String k, String p) {
        if (mLogic == null) { log("!! not bound"); return; }
        try {
            String r = mLogic.getValue(k, p);
            log("<- getValue(" + k + ") = " + r);
        } catch (RemoteException e) { log("!! getValue err: " + e); }
    }

    private final SimpleDateFormat mFmt = new SimpleDateFormat("HH:mm:ss", Locale.US);
    private void log(final String s) {
        final String line = mFmt.format(new Date()) + "  " + s + "\n";
        mUi.post(() -> { mLog.append(line); });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbind();
    }
}
