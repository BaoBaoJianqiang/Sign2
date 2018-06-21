package jianqiang.com.receiverhook;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends Activity {

    // 发送广播到插件之后, 插件如果受到, 那么会回传一个ACTION 为这个值的广播;
    static final String ACTION = "com.weishu.upf.demo.app2.PLUGIN_ACTION";

    static final String apkName = "receivertest.apk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Button t = new Button(this);
        setContentView(t);
        t.setText("send broadcast to plugin: demo");
        t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "插件插件!收到请回答!!", Toast.LENGTH_SHORT).show();
                sendBroadcast(new Intent("baobao2"));
            }
        });

        //解压到本地
        Utils.extractAssets(this, apkName);


        File testPlugin = getFileStreamPath(apkName);
        try {
            ReceiverHelper.preLoadReceiver(this, testPlugin);
            Log.i(getClass().getSimpleName(), "hook success");
        } catch (Exception e) {
            throw new RuntimeException("receiver load failed", e);
        }

        // 注册插件收到我们发送的广播之后, 回传的广播
        registerReceiver(mReceiver, new IntentFilter(ACTION));

        String dexpath = testPlugin.getPath();
        File fileRelease = getDir("dex", 0); //0 表示Context.MODE_PRIVATE
        DexClassLoader classLoader = new DexClassLoader(dexpath,
                fileRelease.getAbsolutePath(), null, getClassLoader());

        try {
            Class mLoadClass = classLoader.loadClass("jianqiang.com.receivertest.MainActivity");
            Object mainActivity = mLoadClass.newInstance();

            Method getNameMethod = mLoadClass.getMethod("doSomething");
            getNameMethod.setAccessible(true);
            String name = (String) getNameMethod.invoke(mainActivity);

            int a = 1;
        } catch (Exception e) {
            Log.e("DEMO", "msg:" + e.getMessage());
        }
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "插件插件,我是主程序,握手完成!", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
