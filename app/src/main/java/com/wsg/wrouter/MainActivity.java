package com.wsg.wrouter;

import android.app.Activity;
import android.os.*;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import com.wsg.annotation.Router;
import com.wsg.core.URouter;


@Router(path = "/main/appMain1")
public class MainActivity extends Activity {
    private Handler handler;
    Looper looper;
    private static final String TAG = "maha";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                 Looper.prepare();
                handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        Log.e(TAG, "handleMessage: " + msg.obj);
                    }
                };
                Log.e(TAG, "run: "+"1211" );
                Looper.loop();
            }
        });
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                Message msg = Message.obtain();
                msg.obj = Thread.currentThread().toString();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                handler.obtainMessage(1,msg).sendToTarget();
            }
        });
        t.start();
        t2.start();
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("dd:","onclick");
                URouter.Companion.getInstance().jump("/modle/app").navigation();
            }
        });

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }
}
