package com.wsg.wrouter;

import android.app.Application;
import com.wsg.core.URouter;

public class BaseApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        URouter.Companion.getInstance().initRouter(this);
    }
}
